package com.backport.entity;

import com.backport.BackportEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

/** Hovers for 3-6 seconds, then releases its item (arrows and potions are fired downwards). */
public class OminousItemSpawner extends Entity {
   private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(OminousItemSpawner.class, EntityDataSerializers.ITEM_STACK);
   private long spawnItemAfterTicks;

   public OminousItemSpawner(EntityType<? extends OminousItemSpawner> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   public static OminousItemSpawner create(Level level, ItemStack item) {
      OminousItemSpawner spawner = new OminousItemSpawner(BackportEntities.OMINOUS_ITEM_SPAWNER, level);
      spawner.spawnItemAfterTicks = 60 + level.getRandom().nextInt(61);
      spawner.setItem(item);
      return spawner;
   }

   @Override
   public void tick() {
      super.tick();
      if (this.level() instanceof ServerLevel level) {
         if (this.tickCount == this.spawnItemAfterTicks - 36L) {
            level.playSound(null, this.blockPosition(), com.backport.BackportSounds.BLOCK_TRIAL_SPAWNER_ABOUT_TO_SPAWN_ITEM, SoundSource.NEUTRAL, 1.0F, 1.0F);
         }
         if (this.tickCount >= this.spawnItemAfterTicks) {
            this.spawnItem(level);
            this.discard();
         }
      } else if (this.level().getGameTime() % 5L == 0L) {
         int n = 1 + this.random.nextInt(3);
         for (int i = 0; i < n; i++) {
            Vec3 from = new Vec3(this.getX() + 0.4 * (this.random.nextGaussian() - this.random.nextGaussian()), this.getY() + 0.4 * (this.random.nextGaussian() - this.random.nextGaussian()),
               this.getZ() + 0.4 * (this.random.nextGaussian() - this.random.nextGaussian()));
            Vec3 d = this.position().vectorTo(from);
            this.level().addParticle(ParticleTypes.PORTAL, this.getX(), this.getY(), this.getZ(), d.x, d.y, d.z);
         }
      }
   }

   private void spawnItem(ServerLevel level) {
      ItemStack item = this.getItem();
      if (item.isEmpty()) {
         return;
      }
      Entity spawned;
      if (item.getItem() instanceof ArrowItem) {
         Arrow arrow = new Arrow(level, this.getX(), this.getY(), this.getZ());
         arrow.setEffectsFromItem(item);
         arrow.pickup = AbstractArrow.Pickup.ALLOWED;
         arrow.shoot(0.0, -1.0, 0.0, 1.5F, 5.9F);
         level.addFreshEntity(arrow);
         spawned = arrow;
      } else if (item.getItem() instanceof PotionItem) {
         ThrownPotion potion = new ThrownPotion(level, this.getX(), this.getY(), this.getZ());
         potion.setItem(item);
         potion.shoot(0.0, -1.0, 0.0, 0.5F, 3.0F);
         level.addFreshEntity(potion);
         spawned = potion;
      } else {
         spawned = new ItemEntity(level, this.getX(), this.getY(), this.getZ(), item);
         level.addFreshEntity(spawned);
      }
      level.levelEvent(3021, this.blockPosition(), 1);
      level.gameEvent(spawned, GameEvent.ENTITY_PLACE, this.position());
      this.setItem(ItemStack.EMPTY);
   }

   @Override
   protected void defineSynchedData() {
      this.entityData.define(DATA_ITEM, ItemStack.EMPTY);
   }

   @Override
   protected void readAdditionalSaveData(CompoundTag tag) {
      this.setItem(ItemStack.of(tag.getCompound("item")));
      this.spawnItemAfterTicks = tag.getLong("spawn_item_after_ticks");
   }

   @Override
   protected void addAdditionalSaveData(CompoundTag tag) {
      if (!this.getItem().isEmpty()) {
         tag.put("item", this.getItem().save(new CompoundTag()));
      }
      tag.putLong("spawn_item_after_ticks", this.spawnItemAfterTicks);
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return new ClientboundAddEntityPacket(this);
   }

   public ItemStack getItem() {
      return this.getEntityData().get(DATA_ITEM);
   }

   private void setItem(ItemStack stack) {
      this.getEntityData().set(DATA_ITEM, stack);
   }

   @Override
   public boolean hurt(DamageSource source, float amount) {
      return false;
   }

   @Override
   public boolean isIgnoringBlockTriggers() {
      return true;
   }
}
