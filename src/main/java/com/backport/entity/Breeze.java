package com.backport.entity;

import com.backport.BackportEntities;
import com.backport.BackportSounds;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Breeze extends Monster {
   public static final int STANDING = 0;
   public static final int SHOOTING = 1;
   public static final int INHALING = 2;
   public static final int SLIDING = 3;
   public static final int LONG_JUMPING = 4;
   private static final EntityDataAccessor<Integer> DATA_BREEZE_POSE = SynchedEntityData.defineId(Breeze.class, EntityDataSerializers.INT);
   public final AnimationState idle = new AnimationState();
   public final AnimationState slide = new AnimationState();
   public final AnimationState slideBack = new AnimationState();
   public final AnimationState longJump = new AnimationState();
   public final AnimationState shoot = new AnimationState();
   public final AnimationState inhale = new AnimationState();
   private int jumpTrail = 0;
   private int soundTick = 0;

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes()
         .add(Attributes.MOVEMENT_SPEED, 0.63F)
         .add(Attributes.MAX_HEALTH, 30.0)
         .add(Attributes.FOLLOW_RANGE, 24.0)
         .add(Attributes.ATTACK_DAMAGE, 3.0);
   }

   public Breeze(EntityType<? extends Monster> type, Level level) {
      super(type, level);
      this.xpReward = 10;
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_BREEZE_POSE, 0);
   }

   public int getBreezePose() {
      return this.entityData.get(DATA_BREEZE_POSE);
   }

   public void setBreezePose(int pose) {
      this.entityData.set(DATA_BREEZE_POSE, pose);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(1, new BreezeCombatGoal(this));
      this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
      if (this.level().isClientSide() && DATA_BREEZE_POSE.equals(accessor)) {
         this.shoot.stop();
         this.idle.stop();
         this.inhale.stop();
         this.longJump.stop();
         switch (this.getBreezePose()) {
            case SHOOTING -> this.shoot.startIfStopped(this.tickCount);
            case INHALING -> this.inhale.startIfStopped(this.tickCount);
            case SLIDING -> this.slide.startIfStopped(this.tickCount);
            default -> {
            }
         }
      }

      super.onSyncedDataUpdated(accessor);
   }

   public void tick() {
      int pose = this.getBreezePose();
      if (this.level().isClientSide) {
         switch (pose) {
            case SHOOTING, INHALING, STANDING -> {
               this.jumpTrail = 0;
               this.emitGroundParticles(1);
            }
            case SLIDING -> this.emitGroundParticles(20);
            case LONG_JUMPING -> {
               this.longJump.startIfStopped(this.tickCount);
               if (++this.jumpTrail <= 5) {
                  BlockState ground = !this.getFeetBlockState().isAir() ? this.getFeetBlockState() : this.level().getBlockState(this.blockPosition().below());
                  for (int i = 0; i < 3; i++) {
                     this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, ground), this.getX(), this.getY() + 0.1, this.getZ(), 0.0, 0.0, 0.0);
                  }
               }
            }
            default -> {
            }
         }

         this.idle.startIfStopped(this.tickCount);
         if (pose != SLIDING && this.slide.isStarted()) {
            this.slideBack.start(this.tickCount);
            this.slide.stop();
         }
      }

      this.soundTick = this.soundTick == 0 ? this.random.nextIntBetweenInclusive(1, 80) : this.soundTick - 1;
      if (this.soundTick == 0) {
         this.playSound(this.onGround() ? BackportSounds.ENTITY_BREEZE_IDLE_GROUND : BackportSounds.ENTITY_BREEZE_IDLE_AIR, 1.0F, 1.0F);
      }

      super.tick();
   }

   private void emitGroundParticles(int amount) {
      BlockState ground = this.level().getBlockState(this.blockPosition().below());
      if (ground.getRenderShape() != RenderShape.INVISIBLE) {
         for (int i = 0; i < amount; i++) {
            this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, ground), this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
         }
      }
   }

   public boolean hurt(DamageSource source, float amount) {
      if (source.getDirectEntity() instanceof Projectile projectile && !(projectile instanceof WindCharge) && !this.level().isClientSide) {
         this.level().playSound(null, this, BackportSounds.ENTITY_BREEZE_DEFLECT, this.getSoundSource(), 1.0F, 1.0F);
         projectile.setDeltaMovement(projectile.getDeltaMovement().scale(-0.5));
         projectile.setOwner(this);
         return false;
      }

      if (source.getDirectEntity() instanceof WindCharge) {
         return false;
      }

      return super.hurt(source, amount);
   }

   public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
      if (distance > 3.0F) {
         this.playSound(BackportSounds.ENTITY_BREEZE_LAND, 1.0F, 1.0F);
      }

      return false;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return BackportSounds.ENTITY_BREEZE_HURT;
   }

   protected SoundEvent getDeathSound() {
      return BackportSounds.ENTITY_BREEZE_DEATH;
   }

   public boolean isNoGravity() {
      return false;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
   }

   private static final class BreezeCombatGoal extends Goal {
      private final Breeze breeze;
      private int phaseTicks;
      private int cooldown = 20;
      private int mode;

      BreezeCombatGoal(Breeze breeze) {
         this.breeze = breeze;
         this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
      }

      public boolean canUse() {
         LivingEntity target = this.breeze.getTarget();
         return target != null && target.isAlive();
      }

      public boolean canContinueToUse() {
         return this.canUse();
      }

      public void start() {
         this.mode = 0;
         this.phaseTicks = 0;
         this.cooldown = 20;
      }

      public void stop() {
         this.breeze.setBreezePose(STANDING);
         this.breeze.getNavigation().stop();
      }

      public void tick() {
         LivingEntity target = this.breeze.getTarget();
         if (target == null) {
            return;
         }

         this.breeze.getLookControl().setLookAt(target, 30.0F, 30.0F);
         double distance = this.breeze.distanceTo(target);
         switch (this.mode) {
            case 0 -> {
               this.breeze.setBreezePose(STANDING);
               if (--this.cooldown <= 0) {
                  if (distance > 16.0 && this.breeze.onGround()) {
                     this.mode = 4;
                  } else if (distance < 4.0 && this.breeze.getRandom().nextInt(3) == 0) {
                     this.mode = 3;
                  } else if (this.breeze.hasLineOfSight(target)) {
                     this.mode = 1;
                  } else {
                     this.mode = 3;
                  }

                  this.phaseTicks = 0;
               }
            }
            case 1 -> {
               this.breeze.setBreezePose(INHALING);
               this.breeze.getNavigation().stop();
               if (this.phaseTicks == 0) {
                  this.breeze.playSound(BackportSounds.ENTITY_BREEZE_INHALE, 1.0F, 1.0F);
               }

               if (++this.phaseTicks >= 10) {
                  this.mode = 2;
                  this.phaseTicks = 0;
               }
            }
            case 2 -> {
               this.breeze.setBreezePose(SHOOTING);
               if (this.phaseTicks == 0) {
                  BreezeWindCharge charge = new BreezeWindCharge(this.breeze, this.breeze.level());
                  Vec3 from = this.breeze.position().add(0.0, this.breeze.getBbHeight() * 0.7, 0.0);
                  charge.setPos(from.x, from.y, from.z);
                  Vec3 dir = new Vec3(target.getX() - from.x, target.getY(0.5) - from.y, target.getZ() - from.z).normalize().scale(0.7);
                  charge.setDeltaMovement(dir);
                  this.breeze.level().addFreshEntity(charge);
                  this.breeze.playSound(BackportSounds.ENTITY_BREEZE_SHOOT, 1.5F, 1.0F);
               }

               if (++this.phaseTicks >= 15) {
                  this.mode = 0;
                  this.cooldown = 30 + this.breeze.getRandom().nextInt(20);
               }
            }
            case 3 -> {
               this.breeze.setBreezePose(SLIDING);
               if (this.phaseTicks == 0) {
                  this.breeze.playSound(BackportSounds.ENTITY_BREEZE_SLIDE, 1.0F, 1.0F);
                  double angle = this.breeze.getRandom().nextDouble() * Math.PI * 2.0;
                  double radius = 4.0 + this.breeze.getRandom().nextDouble() * 4.0;
                  this.breeze.getNavigation().moveTo(target.getX() + Math.cos(angle) * radius, target.getY(), target.getZ() + Math.sin(angle) * radius, 1.3);
               }

               if (++this.phaseTicks >= 25 || this.breeze.getNavigation().isDone()) {
                  this.mode = 0;
                  this.cooldown = 10;
               }
            }
            default -> {
               this.breeze.setBreezePose(LONG_JUMPING);
               if (this.phaseTicks == 0) {
                  this.breeze.playSound(BackportSounds.ENTITY_BREEZE_JUMP, 1.0F, 1.0F);
                  Vec3 toTarget = target.position().subtract(this.breeze.position());
                  Vec3 horizontal = new Vec3(toTarget.x, 0.0, toTarget.z).normalize().scale(1.1);
                  this.breeze.setDeltaMovement(horizontal.x, 1.2, horizontal.z);
               }

               if (++this.phaseTicks >= 30 || (this.phaseTicks > 5 && this.breeze.onGround())) {
                  this.mode = 0;
                  this.cooldown = 10;
               }
            }
         }
      }
   }
}
