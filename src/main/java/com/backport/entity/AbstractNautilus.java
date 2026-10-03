package com.backport.entity;

import com.backport.BackportEffects;
import com.backport.BackportItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractNautilus extends TamableAnimal implements PlayerRideableJumping {
   private static final EntityDataAccessor<Boolean> DASH = SynchedEntityData.defineId(AbstractNautilus.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<ItemStack> SADDLE = SynchedEntityData.defineId(AbstractNautilus.class, EntityDataSerializers.ITEM_STACK);
   private static final EntityDataAccessor<ItemStack> ARMOR = SynchedEntityData.defineId(AbstractNautilus.class, EntityDataSerializers.ITEM_STACK);
   private int dashCooldown;
   protected float playerJumpPendingScale;
   private boolean wasBaby;

   protected AbstractNautilus(EntityType<? extends AbstractNautilus> type, Level level) {
      super(type, level);
      this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.011F, 0.0F, true);
      this.lookControl = new SmoothSwimmingLookControl(this, 10);
      this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Animal.createMobAttributes().add(Attributes.MAX_HEALTH, 15.0).add(Attributes.MOVEMENT_SPEED, 1.0).add(Attributes.ATTACK_DAMAGE, 3.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.3F).add(Attributes.FOLLOW_RANGE, 16.0);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, true));
      this.goalSelector.addGoal(3, new TemptGoal(this, 1.1, Ingredient.of(Items.PUFFERFISH, Items.COD, Items.SALMON, Items.TROPICAL_FISH, Items.PUFFERFISH_BUCKET, Items.COD_BUCKET, Items.SALMON_BUCKET, Items.TROPICAL_FISH_BUCKET), false));
      this.goalSelector.addGoal(5, new RandomSwimmingGoal(this, 1.0, 10));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
   }

   protected PathNavigation createNavigation(Level level) {
      return new WaterBoundPathNavigation(this, level);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DASH, false);
      this.entityData.define(SADDLE, ItemStack.EMPTY);
      this.entityData.define(ARMOR, ItemStack.EMPTY);
   }

   public ItemStack getSaddleItem() {
      return this.entityData.get(SADDLE);
   }

   public ItemStack getArmorItem() {
      return this.entityData.get(ARMOR);
   }

   public boolean isSaddled() {
      return !this.getSaddleItem().isEmpty();
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      if (this.isSaddled()) tag.put("saddle", this.getSaddleItem().save(new CompoundTag()));
      if (!this.getArmorItem().isEmpty()) tag.put("armor", this.getArmorItem().save(new CompoundTag()));
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(SADDLE, tag.contains("saddle") ? ItemStack.of(tag.getCompound("saddle")) : ItemStack.EMPTY);
      this.entityData.set(ARMOR, tag.contains("armor") ? ItemStack.of(tag.getCompound("armor")) : ItemStack.EMPTY);
   }

   public int getArmorValue() {
      int base = super.getArmorValue();
      ItemStack a = this.getArmorItem();
      return a.getItem() instanceof BackportItems.NautilusArmorItem na ? base + na.defense : base;
   }

   public boolean isPushedByFluid() {
      return false;
   }

   public float getWalkTargetValue(BlockPos pos, LevelReader level) {
      return 0.0F;
   }

   public static boolean checkNautilusSpawnRules(EntityType<? extends AbstractNautilus> type, LevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
      int sea = level.getSeaLevel();
      return pos.getY() >= sea - 25 && pos.getY() <= sea - 5 && level.getFluidState(pos.below()).is(FluidTags.WATER) && level.getBlockState(pos.above()).is(Blocks.WATER);
   }

   public boolean checkSpawnObstruction(LevelReader level) {
      return level.isUnobstructed(this);
   }

   public boolean isFood(ItemStack stack) {
      if (!this.isTame() && !this.isBaby()) return this.isTamingItem(stack);
      return stack.is(Items.COD) || stack.is(Items.SALMON) || stack.is(Items.TROPICAL_FISH) || stack.is(Items.PUFFERFISH) || this.isBucketFood(stack);
   }

   private boolean isBucketFood(ItemStack s) {
      return s.is(Items.PUFFERFISH_BUCKET) || s.is(Items.COD_BUCKET) || s.is(Items.SALMON_BUCKET) || s.is(Items.TROPICAL_FISH_BUCKET);
   }

   private boolean isTamingItem(ItemStack s) {
      return s.is(Items.PUFFERFISH) || s.is(Items.PUFFERFISH_BUCKET);
   }

   protected boolean canAddPassenger(Entity passenger) {
      return !this.isVehicle();
   }

   public LivingEntity getControllingPassenger() {
      return this.isSaddled() && this.getFirstPassenger() instanceof Player p ? p : null;
   }

   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      this.setPersistenceRequired();
      if (this.isBaby()) return super.mobInteract(player, hand);
      if (this.isTame() && player.isSecondaryUseActive() && stack.isEmpty()) {
         if (!this.level().isClientSide) {
            if (this.isSaddled()) this.spawnAtLocation(this.getSaddleItem());
            if (!this.getArmorItem().isEmpty()) this.spawnAtLocation(this.getArmorItem());
            this.entityData.set(SADDLE, ItemStack.EMPTY);
            this.entityData.set(ARMOR, ItemStack.EMPTY);
         }
         return InteractionResult.sidedSuccess(this.level().isClientSide);
      }
      if (!stack.isEmpty()) {
         if (!this.isTame() && this.isTamingItem(stack)) {
            if (!this.level().isClientSide) {
               this.useItem(player, hand, stack);
               if (this.random.nextInt(3) == 0) {
                  this.tame(player);
                  this.navigation.stop();
                  this.level().broadcastEntityEvent(this, (byte) 7);
               } else {
                  this.level().broadcastEntityEvent(this, (byte) 6);
               }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
         }
         if (this.isFood(stack) && this.getHealth() < this.getMaxHealth()) {
            if (!this.level().isClientSide) {
               this.heal(2.0F);
               this.useItem(player, hand, stack);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
         }
         if (this.isTame() && stack.is(Items.SADDLE) && !this.isSaddled()) {
            if (!this.level().isClientSide) {
               this.entityData.set(SADDLE, stack.copyWithCount(1));
               if (!player.getAbilities().instabuild) stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
         }
         if (this.isTame() && stack.getItem() instanceof BackportItems.NautilusArmorItem && this.getArmorItem().isEmpty()) {
            if (!this.level().isClientSide) {
               this.entityData.set(ARMOR, stack.copyWithCount(1));
               if (!player.getAbilities().instabuild) stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
         }
      }
      if (this.isTame() && !player.isSecondaryUseActive() && !this.isFood(stack)) {
         if (!this.level().isClientSide) player.startRiding(this);
         return InteractionResult.sidedSuccess(this.level().isClientSide);
      }
      return super.mobInteract(player, hand);
   }

   private void useItem(Player player, InteractionHand hand, ItemStack stack) {
      if (isBucketFood(stack)) {
         if (!player.getAbilities().instabuild) player.setItemInHand(hand, new ItemStack(Items.WATER_BUCKET));
      } else if (!player.getAbilities().instabuild) {
         stack.shrink(1);
      }
   }

   protected void dropCustomDeathLoot(net.minecraft.world.damagesource.DamageSource source, int looting, boolean recent) {
      super.dropCustomDeathLoot(source, looting, recent);
      if (this.isSaddled()) this.spawnAtLocation(this.getSaddleItem());
      if (!this.getArmorItem().isEmpty()) this.spawnAtLocation(this.getArmorItem());
   }

   public boolean isDashing() {
      return this.entityData.get(DASH);
   }

   public void setDashing(boolean d) {
      this.entityData.set(DASH, d);
   }

   public boolean canJump() {
      return this.isSaddled();
   }

   public void onPlayerJump(int amount) {
      if (this.isSaddled() && this.dashCooldown <= 0) {
         this.playerJumpPendingScale = amount < 0 ? 0.0F : amount >= 90 ? 1.0F : 0.4F + 0.4F * amount / 90.0F;
      }
   }

   public void handleStartJump(int scale) {
      SoundEvent s = this.getDashSound();
      if (s != null) this.playSound(s, 1.0F, 1.0F);
      this.setDashing(true);
   }

   public void handleStopJump() {
   }

   public int getJumpCooldown() {
      return this.dashCooldown;
   }

   public void travel(Vec3 input) {
      LivingEntity controller = this.getControllingPassenger();
      if (this.isVehicle() && controller instanceof Player p) {
         this.setYRot(this.getYRot() + Mth.wrapDegrees(p.getYRot() - this.getYRot()) * 0.5F);
         this.setXRot(Mth.clamp(p.getXRot() * 0.5F, -45.0F, 45.0F));
         this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
         if (this.playerJumpPendingScale > 0.0F && this.dashCooldown <= 0) {
            this.setDeltaMovement(this.getDeltaMovement().add(p.getLookAngle().scale((this.isInWater() ? 1.2F : 0.5F) * this.playerJumpPendingScale * this.getAttributeValue(Attributes.MOVEMENT_SPEED))));
            this.dashCooldown = 40;
            this.setDashing(true);
            this.hasImpulse = true;
         }
         this.playerJumpPendingScale = 0.0F;
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
         float speed = (this.isInWater() ? 0.0325F : 0.02F) * (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED);
         if (this.isControlledByLocalInstance()) {
            Vec3 in = new Vec3(p.xxa, up, forward);
            if (this.isInWater()) {
               this.moveRelative(speed, in);
               this.move(MoverType.SELF, this.getDeltaMovement());
               this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
            } else {
               this.setSpeed(speed * 5.0F);
               super.travel(new Vec3(p.xxa, 0.0, forward));
            }
         } else {
            this.setDeltaMovement(Vec3.ZERO);
         }
         this.calculateEntityAnimation(false);
         return;
      }
      if (this.isInWater()) {
         this.moveRelative(this.getSpeed(), input);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
         this.calculateEntityAnimation(false);
      } else {
         super.travel(input);
      }
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide && this.getFirstPassenger() instanceof Player rider) {
         if (!rider.hasEffect(BackportEffects.BREATH_OF_THE_NAUTILUS) || this.level().getGameTime() % 40L == 0L) {
            rider.addEffect(new MobEffectInstance(BackportEffects.BREATH_OF_THE_NAUTILUS, 60, 0, true, true, true));
         }
      }
      if (this.isDashing() && this.dashCooldown < 35) this.setDashing(false);
      if (this.dashCooldown > 0) {
         this.dashCooldown--;
         if (this.dashCooldown == 0) {
            SoundEvent s = this.getDashReadySound();
            if (s != null) this.playSound(s, 1.0F, 1.0F);
         }
      }
      if (this.isInWater() && this.level().isClientSide) {
         double speed = this.getDeltaMovement().length();
         if (this.random.nextFloat() < Mth.clamp(speed * 2.0, 0.15F, 1.0)) {
            Vec3 mouth = this.calculateViewVector(Mth.clamp(this.getXRot(), -10.0F, 10.0F), this.getYRot());
            double spread = this.random.nextDouble() * 0.8 * (1.0 + speed);
            this.level().addParticle(ParticleTypes.BUBBLE, this.getX() - mouth.x * 1.1, this.getY() - mouth.y + 0.25, this.getZ() - mouth.z * 1.1,
               (this.random.nextFloat() - 0.5) * spread, (this.random.nextFloat() - 0.5) * spread, (this.random.nextFloat() - 0.5) * spread);
         }
      }
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
      super.onSyncedDataUpdated(key);
      if (this.wasBaby != this.isBaby()) {
         this.wasBaby = this.isBaby();
         this.refreshDimensions();
      }
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
   }

   protected SoundEvent getDashSound() {
      return null;
   }

   protected SoundEvent getDashReadySound() {
      return null;
   }

   public boolean requiresCustomPersistence() {
      return super.requiresCustomPersistence() || this.isTame();
   }

   public int getMaxAirSupply() {
      return 300;
   }

   protected void handleAirSupply(int air) {
      if (this.isAlive() && !this.isInWaterOrBubble()) {
         this.setAirSupply(air - 1);
         if (this.getAirSupply() <= -20) {
            this.setAirSupply(0);
            this.hurt(this.damageSources().dryOut(), 2.0F);
         }
      } else {
         this.setAirSupply(this.getMaxAirSupply());
      }
   }

   public void baseTick() {
      int air = this.getAirSupply();
      super.baseTick();
      if (!this.isNoAi()) this.handleAirSupply(air);
   }

   public boolean canBreatheUnderwater() {
      return true;
   }
}
