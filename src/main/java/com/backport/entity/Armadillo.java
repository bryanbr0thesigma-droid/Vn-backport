package com.backport.entity;

import com.backport.BackportItems;
import com.backport.BackportEntities;
import com.backport.BackportSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class Armadillo extends Animal {
   private static final EntityDataAccessor<Integer> ARMADILLO_STATE = SynchedEntityData.defineId(Armadillo.class, EntityDataSerializers.INT);
   private long inStateTicks = 0L;
   public final AnimationState rollOutAnimationState = new AnimationState();
   public final AnimationState rollUpAnimationState = new AnimationState();
   public final AnimationState peekAnimationState = new AnimationState();
   private int scuteTime;
   private int scareCheck;

   public enum State {
      IDLE(false, 0),
      ROLLING(true, 10),
      SCARED(true, 50),
      UNROLLING(true, 30);

      private final boolean threatened;
      private final int animationDuration;

      State(boolean threatened, int animationDuration) {
         this.threatened = threatened;
         this.animationDuration = animationDuration;
      }

      public boolean shouldHideInShell(long ticks) {
         return switch (this) {
            case IDLE -> false;
            case ROLLING -> ticks > 5L;
            case SCARED -> true;
            case UNROLLING -> ticks < 26L;
         };
      }

      public int animationDuration() {
         return this.animationDuration;
      }
   }

   public Armadillo(EntityType<? extends Animal> type, Level level) {
      super(type, level);
      this.scuteTime = this.pickNextScuteDropTime();
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(1, new PanicGoal(this, 2.0) {
         public boolean canUse() {
            return !Armadillo.this.isScared() && super.canUse();
         }
      });
      this.goalSelector.addGoal(2, new Goal() {
         {
            this.setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
         }

         public boolean canUse() {
            return Armadillo.this.isScared();
         }

         public void start() {
            Armadillo.this.getNavigation().stop();
         }

         public void tick() {
            Armadillo.this.getNavigation().stop();
         }
      });
      this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
      this.goalSelector.addGoal(4, new TemptGoal(this, 1.1, Ingredient.of(Items.SPIDER_EYE), false));
      this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.0));
      this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
      this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
      this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
   }

   public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
      return BackportEntities.ARMADILLO.create(level);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return net.minecraft.world.entity.Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 12.0).add(Attributes.MOVEMENT_SPEED, 0.14);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(ARMADILLO_STATE, 0);
   }

   public boolean isScared() {
      return this.getState() != State.IDLE;
   }

   public boolean shouldHideInShell() {
      return this.getState().shouldHideInShell(this.inStateTicks);
   }

   public State getState() {
      return State.values()[Math.floorMod(this.entityData.get(ARMADILLO_STATE), State.values().length)];
   }

   public void switchToState(State state) {
      this.entityData.set(ARMADILLO_STATE, state.ordinal());
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
      if (ARMADILLO_STATE.equals(accessor)) {
         this.inStateTicks = 0L;
      }

      super.onSyncedDataUpdated(accessor);
   }

   protected void customServerAiStep() {
      super.customServerAiStep();
      if (this.isAlive() && --this.scuteTime <= 0 && !this.isBaby()) {
         this.playSound(BackportSounds.ENTITY_ARMADILLO_SCUTE_DROP, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
         this.spawnAtLocation(BackportItems.ARMADILLO_SCUTE);
         this.gameEvent(GameEvent.ENTITY_PLACE);
         this.scuteTime = this.pickNextScuteDropTime();
      }

      if (this.tickCount % 20 == 0) {
         this.updateScare();
      }

      if (this.getState() == State.ROLLING && this.inStateTicks > State.ROLLING.animationDuration()) {
         this.switchToState(State.SCARED);
         this.level().broadcastEntityEvent(this, (byte)64);
      }
   }

   private void updateScare() {
      boolean scared = false;
      for (LivingEntity other : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(7.0, 2.0, 7.0), e -> e != this)) {
         if (this.isScaredBy(other)) {
            scared = true;
            break;
         }
      }

      if (scared) {
         this.scareCheck = 80;
         if (this.getState() == State.IDLE && this.canStayRolledUp()) {
            this.rollUp();
         }
      } else if (this.scareCheck > 0) {
         this.scareCheck -= 20;
      } else if (this.getState() == State.SCARED) {
         this.rollOutStart();
      }

      if (this.getState() == State.UNROLLING && this.inStateTicks > State.UNROLLING.animationDuration()) {
         this.rollOut();
      }
   }

   private int pickNextScuteDropTime() {
      return this.random.nextInt(20 * 60 * 5) + 20 * 60 * 5;
   }

   public void tick() {
      super.tick();
      if (this.level().isClientSide()) {
         this.setupAnimationStates();
      }

      this.inStateTicks++;
   }

   public float getScale() {
      return this.isBaby() ? 0.6F : 1.0F;
   }

   private void setupAnimationStates() {
      switch (this.getState()) {
         case IDLE -> {
            this.rollOutAnimationState.stop();
            this.rollUpAnimationState.stop();
            this.peekAnimationState.stop();
         }
         case ROLLING -> {
            this.rollOutAnimationState.stop();
            this.rollUpAnimationState.startIfStopped(this.tickCount);
            this.peekAnimationState.stop();
         }
         case SCARED -> {
            this.rollOutAnimationState.stop();
            this.rollUpAnimationState.stop();
            if (this.inStateTicks == 0L) {
               this.peekAnimationState.start(this.tickCount);
            } else {
               this.peekAnimationState.startIfStopped(this.tickCount);
            }
         }
         case UNROLLING -> {
            this.rollOutAnimationState.startIfStopped(this.tickCount);
            this.rollUpAnimationState.stop();
            this.peekAnimationState.stop();
         }
      }
   }

   public void handleEntityEvent(byte id) {
      if (id == 64 && this.level().isClientSide()) {
         this.peekAnimationState.stop();
         this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), BackportSounds.ENTITY_ARMADILLO_PEEK, this.getSoundSource(), 1.0F, 1.0F, false);
      } else {
         super.handleEntityEvent(id);
      }
   }

   public boolean isFood(ItemStack stack) {
      return stack.is(Items.SPIDER_EYE);
   }

   public boolean isScaredBy(LivingEntity entity) {
      if (!this.getBoundingBox().inflate(7.0, 2.0, 7.0).intersects(entity.getBoundingBox())) {
         return false;
      } else if (entity.getType().is(EntityTypeTags.SKELETONS) || entity.getMobType() == net.minecraft.world.entity.MobType.UNDEAD) {
         return true;
      } else if (this.getLastHurtByMob() == entity) {
         return true;
      } else if (entity instanceof Player player) {
         return !player.isSpectator() && (player.isSprinting() || player.isPassenger());
      } else {
         return false;
      }
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("state", this.getState().ordinal());
      tag.putInt("scute_time", this.scuteTime);
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(ARMADILLO_STATE, tag.getInt("state"));
      if (tag.contains("scute_time")) {
         this.scuteTime = tag.getInt("scute_time");
      }
   }

   public void rollUp() {
      if (!this.isScared()) {
         this.getNavigation().stop();
         this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
         this.resetLove();
         this.gameEvent(GameEvent.ENTITY_INTERACT);
         this.playSound(BackportSounds.ENTITY_ARMADILLO_ROLL, 1.0F, 1.0F);
         this.switchToState(State.ROLLING);
      }
   }

   private void rollOutStart() {
      this.switchToState(State.UNROLLING);
      this.playSound(BackportSounds.ENTITY_ARMADILLO_UNROLL_START, 1.0F, 1.0F);
   }

   public void rollOut() {
      if (this.isScared()) {
         this.gameEvent(GameEvent.ENTITY_INTERACT);
         this.playSound(BackportSounds.ENTITY_ARMADILLO_UNROLL_FINISH, 1.0F, 1.0F);
         this.switchToState(State.IDLE);
      }
   }

   public boolean hurt(DamageSource source, float damage) {
      if (this.isScared()) {
         damage = (damage - 1.0F) / 2.0F;
      }

      return super.hurt(source, damage);
   }

   protected void actuallyHurt(DamageSource source, float amount) {
      super.actuallyHurt(source, amount);
      if (!this.isNoAi() && !this.isDeadOrDying()) {
         if (source.getEntity() instanceof LivingEntity && this.canStayRolledUp()) {
            this.rollUp();
         } else if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_FREEZING)) {
            this.rollOut();
         }
      }
   }

   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (stack.is(Items.BRUSH) && !this.isBaby()) {
         if (!this.level().isClientSide) {
            this.spawnAtLocation(BackportItems.ARMADILLO_SCUTE);
            this.playSound(BackportSounds.ENTITY_ARMADILLO_BRUSH, 1.0F, 1.0F);
            this.gameEvent(GameEvent.ENTITY_INTERACT);
            stack.hurtAndBreak(16, player, p -> p.broadcastBreakEvent(hand));
         }

         return InteractionResult.sidedSuccess(this.level().isClientSide);
      }

      return this.isScared() ? InteractionResult.FAIL : super.mobInteract(player, hand);
   }

   public boolean canStayRolledUp() {
      return !this.isInWaterOrBubble() && !this.isLeashed() && !this.isPassenger() && !this.isVehicle();
   }

   public boolean canFallInLove() {
      return super.canFallInLove() && !this.isScared();
   }

   protected SoundEvent getAmbientSound() {
      return this.isScared() ? null : BackportSounds.ENTITY_ARMADILLO_AMBIENT;
   }

   protected void playEatingSound() {
      this.playSound(BackportSounds.ENTITY_ARMADILLO_EAT, 1.0F, 1.0F);
   }

   protected SoundEvent getDeathSound() {
      return BackportSounds.ENTITY_ARMADILLO_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return this.isScared() ? BackportSounds.ENTITY_ARMADILLO_HURT_REDUCED : BackportSounds.ENTITY_ARMADILLO_HURT;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(BackportSounds.ENTITY_ARMADILLO_STEP, 0.15F, 1.0F);
   }

   public int getMaxHeadYRot() {
      return this.isScared() ? 0 : 32;
   }

   protected BodyRotationControl createBodyControl() {
      return new BodyRotationControl(this) {
         public void clientTick() {
            if (!Armadillo.this.isScared()) {
               super.clientTick();
            }
         }
      };
   }
}
