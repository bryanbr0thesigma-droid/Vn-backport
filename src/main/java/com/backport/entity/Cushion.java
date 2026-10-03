package com.backport.entity;

import com.backport.BackportEntities;
import com.backport.BackportItems;
import com.backport.BackportSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Cushion extends Entity {
   private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(Cushion.class, EntityDataSerializers.INT);

   public Cushion(EntityType<? extends Cushion> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_COLOR, DyeColor.WHITE.getId());
   }

   public DyeColor getColor() {
      return DyeColor.byId(this.entityData.get(DATA_COLOR));
   }

   public void setColor(DyeColor color) {
      this.entityData.set(DATA_COLOR, color.getId());
   }

   protected void readAdditionalSaveData(CompoundTag tag) {
      DyeColor c = DyeColor.byName(tag.getString("color"), DyeColor.WHITE);
      this.setColor(c);
   }

   protected void addAdditionalSaveData(CompoundTag tag) {
      tag.putString("color", this.getColor().getName());
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return new ClientboundAddEntityPacket(this);
   }

   public boolean isPickable() {
      return true;
   }

   public boolean isNoGravity() {
      return true;
   }

   public double getPassengersRidingOffset() {
      return 0.0;
   }

   public boolean isPushable() {
      return false;
   }

   public ItemStack getPickResult() {
      return new ItemStack(BackportItems.cushion(this.getColor()));
   }

   public boolean skipAttackInteraction(Entity attacker) {
      return attacker instanceof Player p && !p.mayBuild();
   }

   public boolean hurt(DamageSource source, float amount) {
      if (this.isInvulnerableTo(source) || this.level().isClientSide || this.isRemoved()) return false;
      if (source.getEntity() instanceof Player p && !p.mayBuild()) return false;
      this.dropItem(source.getEntity());
      this.discard();
      return true;
   }

   public InteractionResult interact(Player player, InteractionHand hand) {
      if (player.isSecondaryUseActive() || this.isVehicle()) return InteractionResult.PASS;
      if (!this.level().isClientSide && player.startRiding(this)) {
         this.playSound(BackportSounds.ENTITY_CUSHION_SIT, 1.0F, 1.0F);
         return InteractionResult.SUCCESS;
      }
      return InteractionResult.CONSUME;
   }

   protected void removePassenger(Entity passenger) {
      super.removePassenger(passenger);
      if (!this.level().isClientSide && this.getRemovalReason() == null) {
         this.playSound(BackportSounds.ENTITY_CUSHION_GET_UP, 1.0F, 1.0F);
      }
   }

   public void dropItem(Entity causedBy) {
      this.playSound(BackportSounds.ENTITY_CUSHION_BREAK, 1.0F, 1.0F);
      if (this.level() instanceof ServerLevel sl) {
         sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.WHITE_WOOL.defaultBlockState()), this.getX(), this.getY(0.6667), this.getZ(), 10, this.getBbWidth() / 4.0F, this.getBbHeight() / 4.0F, this.getBbWidth() / 4.0F, 0.05);
         if (!(causedBy instanceof Player p && p.getAbilities().instabuild)) {
            this.spawnAtLocation(new ItemStack(BackportItems.cushion(this.getColor())));
         }
      }
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide && this.tickCount % 10 == 0 && !this.isRemoved()) {
         if (!wouldSurviveAt(this.level(), this.getBoundingBox()) || inFire()) {
            this.dropItem(null);
            this.discard();
         }
      }
   }

   private boolean inFire() {
      AABB box = this.getBoundingBox().deflate(1.0E-5);
      for (BlockPos p : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ), BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
         if (this.level().getBlockState(p).is(BlockTags.FIRE)) return true;
      }
      return false;
   }

   public static boolean wouldSurviveAt(Level level, AABB box) {
      return hasAnchorBelow(level, box) && !coveredBySuffocating(level, box);
   }

   public static boolean canBePlacedAt(Level level, AABB box) {
      return wouldSurviveAt(level, box);
   }

   private static boolean hasAnchorBelow(Level level, AABB box) {
      AABB anchor = new AABB(box.minX, box.minY - 0.015625, box.minZ, Math.nextDown(box.maxX), box.minY, Math.nextDown(box.maxZ));
      AABB search = anchor.expandTowards(0.0, -0.125, 0.0);
      for (BlockPos p : BlockPos.betweenClosed(BlockPos.containing(search.minX, search.minY, search.minZ), BlockPos.containing(search.maxX, search.maxY, search.maxZ))) {
         BlockState s = level.getBlockState(p);
         VoxelShape shape = s.getShape(level, p);
         if (!shape.isEmpty() && shape.bounds().move(p).intersects(anchor)) return true;
      }
      return false;
   }

   private static boolean coveredBySuffocating(Level level, AABB box) {
      AABB d = box.deflate(1.0E-5);
      for (BlockPos p : BlockPos.betweenClosed(BlockPos.containing(d.minX, d.minY, d.minZ), BlockPos.containing(d.maxX, d.maxY, d.maxZ))) {
         if (!level.getBlockState(p).isSuffocating(level, p)) return false;
      }
      return true;
   }
}
