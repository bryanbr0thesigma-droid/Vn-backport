package com.backport.entity;

import com.backport.BackportItems;
import com.backport.BackportSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class HappyGhast extends Animal {
   private static final EntityDataAccessor<ItemStack> DATA_HARNESS = SynchedEntityData.defineId(HappyGhast.class, EntityDataSerializers.ITEM_STACK);
   public static final float BABY_SCALE = 0.2375F;
   private static final Vec3[] SEATS = {new Vec3(0.0, 0.0, 0.0), new Vec3(-1.3, 0.0, -1.3), new Vec3(1.3, 0.0, -1.3), new Vec3(0.0, 0.0, 1.5)};

   private boolean wasBaby;

   public HappyGhast(EntityType<? extends HappyGhast> type, Level level) {
      super(type, level);
      this.moveControl = new FlyingMoveControl(this, 20, true);
      this.setNoGravity(true);
   }

   protected PathNavigation createNavigation(Level level) {
      FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
      nav.setCanOpenDoors(false);
      nav.setCanFloat(true);
      nav.setCanPassDoors(true);
      return nav;
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, Ingredient.of(Items.SNOWBALL), false));
      this.goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Animal.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.FLYING_SPEED, 0.05).add(Attributes.MOVEMENT_SPEED, 0.05).add(Attributes.FOLLOW_RANGE, 16.0);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_HARNESS, ItemStack.EMPTY);
   }

   public ItemStack getHarness() {
      return this.entityData.get(DATA_HARNESS);
   }

   public boolean isWearingHarness() {
      return !this.getHarness().isEmpty();
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      if (this.isWearingHarness()) tag.put("harness", this.getHarness().save(new CompoundTag()));
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(DATA_HARNESS, tag.contains("harness") ? ItemStack.of(tag.getCompound("harness")) : ItemStack.EMPTY);
   }

   public EntityDimensions getDimensions(Pose pose) {
      EntityDimensions dims = super.getDimensions(pose);
      return this.isBaby() ? dims.scale(BABY_SCALE) : dims;
   }

   protected float getStandingEyeHeight(Pose pose, EntityDimensions dims) {
      return dims.height * 0.5F;
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
      super.onSyncedDataUpdated(key);
      if (this.wasBaby != this.isBaby()) {
         this.wasBaby = this.isBaby();
         this.refreshDimensions();
      }
   }

   protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
   }

   public boolean onClimbable() {
      return false;
   }

   public boolean isFood(ItemStack stack) {
      return stack.is(Items.SNOWBALL);
   }

   public boolean canFallInLove() {
      return false;
   }

   @Nullable
   public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
      return com.backport.BackportEntities.HAPPY_GHAST.create(level);
   }

   public int getMaxSpawnClusterSize() {
      return 1;
   }

   public SoundSource getSoundSource() {
      return SoundSource.NEUTRAL;
   }

   protected float getSoundVolume() {
      return this.isBaby() ? 1.0F : 4.0F;
   }

   protected SoundEvent getAmbientSound() {
      return this.isBaby() ? BackportSounds.ENTITY_GHASTLING_AMBIENT : BackportSounds.ENTITY_HAPPY_GHAST_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return this.isBaby() ? BackportSounds.ENTITY_GHASTLING_HURT : BackportSounds.ENTITY_HAPPY_GHAST_HURT;
   }

   protected SoundEvent getDeathSound() {
      return this.isBaby() ? BackportSounds.ENTITY_GHASTLING_DEATH : BackportSounds.ENTITY_HAPPY_GHAST_DEATH;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
   }

   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      if (this.isBaby()) return super.mobInteract(player, hand);
      ItemStack stack = player.getItemInHand(hand);
      if (stack.getItem() instanceof BackportItems.HarnessItem && !this.isWearingHarness()) {
         if (!this.level().isClientSide) {
            this.entityData.set(DATA_HARNESS, stack.copyWithCount(1));
            this.playSound(BackportSounds.ENTITY_HAPPY_GHAST_EQUIP, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) stack.shrink(1);
         }
         return InteractionResult.sidedSuccess(this.level().isClientSide);
      }
      if (stack.is(Items.SHEARS) && this.isWearingHarness() && !this.isVehicle()) {
         if (!this.level().isClientSide) {
            this.spawnAtLocation(this.getHarness());
            this.entityData.set(DATA_HARNESS, ItemStack.EMPTY);
            this.playSound(BackportSounds.ENTITY_HAPPY_GHAST_UNEQUIP, 1.0F, 1.0F);
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
         }
         return InteractionResult.sidedSuccess(this.level().isClientSide);
      }
      if (this.isWearingHarness() && !player.isSecondaryUseActive() && !stack.is(Items.SNOWBALL)) {
         if (!this.level().isClientSide) player.startRiding(this);
         return InteractionResult.sidedSuccess(this.level().isClientSide);
      }
      return super.mobInteract(player, hand);
   }

   protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
      super.dropCustomDeathLoot(source, looting, recentlyHit);
      if (this.isWearingHarness()) this.spawnAtLocation(this.getHarness());
   }

   protected void addPassenger(Entity passenger) {
      if (!this.isVehicle()) this.playSound(BackportSounds.ENTITY_HAPPY_GHAST_HARNESS_GOGGLES_DOWN, 1.0F, 1.0F);
      super.addPassenger(passenger);
   }

   protected void removePassenger(Entity passenger) {
      super.removePassenger(passenger);
      if (!this.isVehicle()) this.playSound(BackportSounds.ENTITY_HAPPY_GHAST_HARNESS_GOGGLES_UP, 1.0F, 1.0F);
   }

   protected boolean canAddPassenger(Entity passenger) {
      return this.getPassengers().size() < 4;
   }

   @Nullable
   public LivingEntity getControllingPassenger() {
      return this.isWearingHarness() && this.getFirstPassenger() instanceof Player p ? p : null;
   }

   protected void positionRider(Entity passenger, Entity.MoveFunction move) {
      int i = Math.max(0, this.getPassengers().indexOf(passenger));
      Vec3 seat = SEATS[Math.min(i, 3)].scale(this.getScale());
      Vec3 rot = seat.yRot(-this.getYRot() * (float) (Math.PI / 180.0));
      move.accept(passenger, this.getX() + rot.x, this.getY() + this.getBbHeight() * 0.98 - 0.2, this.getZ() + rot.z);
   }

   public void travel(Vec3 input) {
      LivingEntity controller = this.getControllingPassenger();
      if (this.isVehicle() && controller instanceof Player p) {
         this.setYRot(this.getYRot() + Mth.wrapDegrees(p.getYRot() - this.getYRot()) * 0.08F);
         this.setXRot(p.getXRot() * 0.5F);
         this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
         float forward = 0.0F;
         float up = 0.0F;
         if (p.zza != 0.0F) {
            float fl = Mth.cos(p.getXRot() * (float) (Math.PI / 180.0));
            float ul = -Mth.sin(p.getXRot() * (float) (Math.PI / 180.0));
            if (p.zza < 0.0F) {
               fl *= -0.5F;
               ul *= -0.5F;
            }
            forward = fl;
            up = ul;
         }
         if (this.jumping(p)) up += 0.5F;
         Vec3 in = new Vec3(p.xxa, up, forward).scale(3.9F * this.getAttributeValue(Attributes.FLYING_SPEED));
         if (this.isControlledByLocalInstance()) {
            this.setSpeed((float) this.getAttributeValue(Attributes.FLYING_SPEED));
            this.moveRelative(1.0F, in);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.91));
         } else {
            this.setDeltaMovement(Vec3.ZERO);
         }
         this.calculateEntityAnimation(false);
         return;
      }
      float speed = (float) this.getAttributeValue(Attributes.FLYING_SPEED) * 5.0F / 3.0F;
      if (this.isInWater()) {
         this.moveRelative(0.02F, input);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().scale(0.8F));
      } else {
         this.moveRelative(speed, input);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().scale(0.91F));
      }
      this.calculateEntityAnimation(false);
   }

   private boolean jumping(Player p) {
      return ((com.backport.mixin.LivingEntityAccessor) p).backport$isJumping();
   }

   public boolean isNoGravity() {
      return true;
   }
}
