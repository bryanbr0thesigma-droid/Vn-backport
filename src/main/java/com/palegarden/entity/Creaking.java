package com.palegarden.entity;

import com.palegarden.PaleBlocks;
import com.palegarden.PaleSounds;
import com.palegarden.block.CreakingHeartBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Creaking extends Monster {
   private static final EntityDataAccessor<Boolean> CAN_MOVE = SynchedEntityData.defineId(Creaking.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> IS_ACTIVE = SynchedEntityData.defineId(Creaking.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> IS_TEARING_DOWN = SynchedEntityData.defineId(Creaking.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Optional<BlockPos>> HOME_POS = SynchedEntityData.defineId(Creaking.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
   private static final double ACTIVATION_RANGE_SQ = 144.0;
   private int attackAnimationRemainingTicks;
   public final AnimationState attackAnimationState = new AnimationState();
   public final AnimationState invulnerabilityAnimationState = new AnimationState();
   public final AnimationState deathAnimationState = new AnimationState();
   private int invulnerabilityAnimationRemainingTicks;
   private boolean eyesGlowing;
   private int nextFlickerTime;
   private int playerStuckCounter;
   private List<Player> nearestPlayers = List.of();

   public Creaking(EntityType<? extends Creaking> type, Level level) {
      super(type, level);
      this.lookControl = new Creaking.CreakingLookControl(this);
      this.moveControl = new Creaking.CreakingMoveControl(this);
      this.jumpControl = new Creaking.CreakingJumpControl(this);
      ((GroundPathNavigation)this.getNavigation()).setCanFloat(true);
      this.xpReward = 0;
      this.setMaxUpStep(1.0625F);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this) {
         public boolean canUse() {
            return Creaking.this.canMove() && super.canUse();
         }
      });
      this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true) {
         public boolean canUse() {
            return Creaking.this.canMove() && Creaking.this.isActive() && super.canUse();
         }

         public boolean canContinueToUse() {
            return Creaking.this.canMove() && Creaking.this.isActive() && super.canContinueToUse();
         }

         protected int getAttackInterval() {
            return 40;
         }
      });
      this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.3) {
         public boolean canUse() {
            return Creaking.this.canMove() && !Creaking.this.isActive() && super.canUse();
         }
      });
      this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F) {
         public boolean canUse() {
            return Creaking.this.canMove() && super.canUse();
         }
      });
   }

   public void setTransient(BlockPos pos) {
      this.setHomePos(pos);
      this.setPathfindingMalus(BlockPathTypes.DAMAGE_OTHER, 8.0F);
      this.setPathfindingMalus(BlockPathTypes.POWDER_SNOW, 8.0F);
      this.setPathfindingMalus(BlockPathTypes.LAVA, 8.0F);
      this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0F);
      this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
   }

   public void playCreakingSound(SoundEvent sound) {
      this.playSound(sound, this.getSoundVolume(), this.getVoicePitch());
   }

   public boolean isHeartBound() {
      return this.getHomePos() != null;
   }

   protected BodyRotationControl createBodyControl() {
      return new Creaking.CreakingBodyRotationControl(this);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(CAN_MOVE, true);
      this.entityData.define(IS_ACTIVE, false);
      this.entityData.define(IS_TEARING_DOWN, false);
      this.entityData.define(HOME_POS, Optional.empty());
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 1.0)
         .add(Attributes.MOVEMENT_SPEED, 0.4F)
         .add(Attributes.ATTACK_DAMAGE, 3.0)
         .add(Attributes.FOLLOW_RANGE, 32.0);
   }

   public boolean canMove() {
      return this.entityData.get(CAN_MOVE);
   }

   public boolean doHurtTarget(Entity target) {
      if (!(target instanceof LivingEntity)) {
         return false;
      } else {
         this.attackAnimationRemainingTicks = 15;
         this.level().broadcastEntityEvent(this, (byte)4);
         return super.doHurtTarget(target);
      }
   }

   public boolean hurt(DamageSource source, float amount) {
      BlockPos home = this.getHomePos();
      if (home == null || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
         return super.hurt(source, amount);
      } else if (this.level().isClientSide) {
         return false;
      } else if (!this.isInvulnerableTo(source) && this.invulnerabilityAnimationRemainingTicks <= 0 && !this.isDeadOrDying()) {
         Player player = source.getEntity() instanceof Player p ? p : null;
         Entity direct = source.getDirectEntity();
         if (!(direct instanceof LivingEntity) && !(direct instanceof Projectile) && player == null) {
            return false;
         } else {
            this.invulnerabilityAnimationRemainingTicks = 8;
            this.level().broadcastEntityEvent(this, (byte)66);
            if (this.level().getBlockEntity(home) instanceof CreakingHeartBlockEntity heart && heart.isProtector(this)) {
               if (player != null) {
                  heart.creakingHurt();
               }

               this.playHurtSound(source);
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public boolean isPushable() {
      return super.isPushable() && this.canMove();
   }

   public void push(double x, double y, double z) {
      if (this.canMove()) {
         super.push(x, y, z);
      }
   }

   protected void customServerAiStep() {
      super.customServerAiStep();
      if (this.tickCount % 10 == 0) {
         double range = this.getAttributeValue(Attributes.FOLLOW_RANGE);
         List<Player> found = new ArrayList<>(this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(range), EntitySelectorHolder.ATTACKABLE));
         found.sort((a, b) -> Double.compare(a.distanceToSqr(this), b.distanceToSqr(this)));
         this.nearestPlayers = found;
      }
   }

   public void aiStep() {
      if (this.invulnerabilityAnimationRemainingTicks > 0) {
         this.invulnerabilityAnimationRemainingTicks--;
      }

      if (this.attackAnimationRemainingTicks > 0) {
         this.attackAnimationRemainingTicks--;
      }

      if (!this.level().isClientSide) {
         boolean wasMoving = this.entityData.get(CAN_MOVE);
         boolean canMoveNow = this.checkCanMove();
         if (canMoveNow != wasMoving) {
            this.gameEvent(GameEvent.ENTITY_INTERACT);
            if (canMoveNow) {
               this.playSound(PaleSounds.ENTITY_CREAKING_UNFREEZE, this.getSoundVolume(), this.getVoicePitch());
            } else {
               this.getNavigation().stop();
               this.setDeltaMovement(Vec3.ZERO);
               this.playSound(PaleSounds.ENTITY_CREAKING_FREEZE, this.getSoundVolume(), this.getVoicePitch());
            }
         }

         this.entityData.set(CAN_MOVE, canMoveNow);
      }

      super.aiStep();
   }

   public void tick() {
      if (!this.level().isClientSide) {
         BlockPos home = this.getHomePos();
         if (home != null) {
            boolean ok = this.level().getBlockEntity(home) instanceof CreakingHeartBlockEntity heart && heart.isProtector(this);
            if (!ok) {
               this.setHealth(0.0F);
            }
         }
      }

      super.tick();
      if (this.level().isClientSide) {
         this.setupAnimationStates();
         this.checkEyeBlink();
      }
   }

   protected void tickDeath() {
      if (this.isHeartBound() && this.isTearingDown()) {
         this.deathTime++;
         if (!this.level().isClientSide() && this.deathTime > 45 && !this.isRemoved()) {
            this.tearDown();
         }
      } else {
         super.tickDeath();
      }
   }

   protected void updateWalkAnimation(float partial) {
      float speed = Math.min(partial * 25.0F, 3.0F);
      this.walkAnimation.update(speed, 0.4F);
   }

   private void setupAnimationStates() {
      this.attackAnimationState.animateWhen(this.attackAnimationRemainingTicks > 0, this.tickCount);
      this.invulnerabilityAnimationState.animateWhen(this.invulnerabilityAnimationRemainingTicks > 0, this.tickCount);
      this.deathAnimationState.animateWhen(this.isTearingDown(), this.tickCount);
   }

   public void tearDown() {
      if (this.level() instanceof ServerLevel serverLevel) {
         AABB box = this.getBoundingBox();
         Vec3 center = box.getCenter();
         double dx = box.getXsize() * 0.3;
         double dy = box.getYsize() * 0.3;
         double dz = box.getZsize() * 0.3;
         serverLevel.sendParticles(
            new BlockParticleOption(ParticleTypes.BLOCK, PaleBlocks.PALE_OAK_WOOD.defaultBlockState()), center.x, center.y, center.z, 100, dx, dy, dz, 0.0
         );
         serverLevel.sendParticles(
            new BlockParticleOption(ParticleTypes.BLOCK, PaleBlocks.CREAKING_HEART.defaultBlockState().setValue(CreakingHeartBlock.ACTIVE, true)),
            center.x, center.y, center.z, 10, dx, dy, dz, 0.0
         );
      }

      this.playSound(this.getDeathSound(), this.getSoundVolume(), this.getVoicePitch());
      this.remove(Entity.RemovalReason.DISCARDED);
   }

   public void creakingDeathEffects(DamageSource source) {
      this.die(source);
      this.playSound(PaleSounds.ENTITY_CREAKING_TWITCH, this.getSoundVolume(), this.getVoicePitch());
   }

   public void handleEntityEvent(byte id) {
      if (id == 66) {
         this.invulnerabilityAnimationRemainingTicks = 8;
         this.playHurtSound(this.damageSources().generic());
      } else if (id == 4) {
         this.attackAnimationRemainingTicks = 15;
         this.playSound(PaleSounds.ENTITY_CREAKING_ATTACK, this.getSoundVolume(), this.getVoicePitch());
      } else {
         super.handleEntityEvent(id);
      }
   }

   public boolean fireImmune() {
      return this.isHeartBound() || super.fireImmune();
   }

   public boolean canBeLeashed(Player player) {
      return !this.isHeartBound() && super.canBeLeashed(player);
   }

   protected boolean canAddPassenger(Entity passenger) {
      return !this.isHeartBound() && super.canAddPassenger(passenger);
   }

   public boolean canChangeDimensions() {
      return !this.isHeartBound() && super.canChangeDimensions();
   }

   protected PathNavigation createNavigation(Level level) {
      return new Creaking.CreakingPathNavigation(this, level);
   }

   public boolean playerIsStuckInYou() {
      if (this.nearestPlayers.isEmpty()) {
         this.playerStuckCounter = 0;
         return false;
      } else {
         AABB box = this.getBoundingBox();

         for (Player player : this.nearestPlayers) {
            if (box.contains(player.getEyePosition())) {
               this.playerStuckCounter++;
               return this.playerStuckCounter > 4;
            }
         }

         this.playerStuckCounter = 0;
         return false;
      }
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      if (tag.contains("home_pos")) {
         this.setTransient(NbtUtils.readBlockPos(tag.getCompound("home_pos")));
      }
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      BlockPos home = this.getHomePos();
      if (home != null) {
         tag.put("home_pos", NbtUtils.writeBlockPos(home));
      }
   }

   public void setHomePos(BlockPos pos) {
      this.entityData.set(HOME_POS, Optional.of(pos));
   }

   @Nullable
   public BlockPos getHomePos() {
      return this.entityData.get(HOME_POS).orElse(null);
   }

   public void setTearingDown() {
      this.entityData.set(IS_TEARING_DOWN, true);
   }

   public boolean isTearingDown() {
      return this.entityData.get(IS_TEARING_DOWN);
   }

   public boolean hasGlowingEyes() {
      return this.eyesGlowing;
   }

   public void checkEyeBlink() {
      if (this.deathTime > this.nextFlickerTime) {
         this.nextFlickerTime = this.deathTime
            + this.getRandom().nextIntBetweenInclusive(this.eyesGlowing ? 2 : this.deathTime / 4, this.eyesGlowing ? 8 : this.deathTime / 2);
         this.eyesGlowing = !this.eyesGlowing;
      }
   }

   protected SoundEvent getAmbientSound() {
      return this.isActive() ? null : PaleSounds.ENTITY_CREAKING_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return this.isHeartBound() ? PaleSounds.ENTITY_CREAKING_SWAY : super.getHurtSound(source);
   }

   protected SoundEvent getDeathSound() {
      return PaleSounds.ENTITY_CREAKING_DEATH;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(PaleSounds.ENTITY_CREAKING_STEP, 0.15F, 1.0F);
   }

   public void knockback(double strength, double x, double z) {
      if (this.canMove()) {
         super.knockback(strength, x, z);
      }
   }

   private boolean isLookingAtMe(Player player, double tolerance, double... ys) {
      Vec3 view = player.getViewVector(1.0F).normalize();

      for (double y : ys) {
         Vec3 toMe = new Vec3(this.getX() - player.getX(), y - player.getEyeY(), this.getZ() - player.getZ());
         toMe = toMe.normalize();
         double dot = view.dot(toMe);
         if (dot > 1.0 - tolerance) {
            Vec3 from = player.getEyePosition();
            Vec3 to = new Vec3(this.getX(), y, this.getZ());
            if (this.level().clip(new ClipContext(from, to, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, player)).getType() == HitResult.Type.MISS) {
               return true;
            }
         }
      }

      return false;
   }

   private static boolean notWearingDisguise(Player player) {
      return !player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).is(Items.CARVED_PUMPKIN);
   }

   public boolean checkCanMove() {
      boolean active = this.isActive();
      if (this.nearestPlayers.isEmpty()) {
         if (active) {
            this.deactivate();
         }

         return true;
      } else {
         boolean anyTarget = false;

         for (Player player : this.nearestPlayers) {
            if (this.canAttack(player) && !this.isAlliedTo(player)) {
               anyTarget = true;
               if ((!active || notWearingDisguise(player))
                  && this.isLookingAtMe(player, 0.5, this.getEyeY(), this.getY() + 0.5 * this.getScale(), (this.getEyeY() + this.getY()) / 2.0)) {
                  if (active) {
                     return false;
                  }

                  if (player.distanceToSqr(this) < ACTIVATION_RANGE_SQ) {
                     this.activate(player);
                     return false;
                  }
               }
            }
         }

         if (!anyTarget && active) {
            this.deactivate();
         }

         return true;
      }
   }

   public void activate(Player player) {
      this.setTarget(player);
      this.gameEvent(GameEvent.ENTITY_INTERACT);
      this.playSound(PaleSounds.ENTITY_CREAKING_ACTIVATE, this.getSoundVolume(), this.getVoicePitch());
      this.setIsActive(true);
   }

   public void deactivate() {
      this.setTarget(null);
      this.gameEvent(GameEvent.ENTITY_INTERACT);
      this.playSound(PaleSounds.ENTITY_CREAKING_DEACTIVATE, this.getSoundVolume(), this.getVoicePitch());
      this.setIsActive(false);
   }

   public void setIsActive(boolean active) {
      this.entityData.set(IS_ACTIVE, active);
   }

   public boolean isActive() {
      return this.entityData.get(IS_ACTIVE);
   }

   public float getWalkTargetValue(BlockPos pos, LevelReader level) {
      return 0.0F;
   }

   protected float getStandingEyeHeight(net.minecraft.world.entity.Pose pose, net.minecraft.world.entity.EntityDimensions dimensions) {
      return 2.3F;
   }

   static final class EntitySelectorHolder {
      static final java.util.function.Predicate<Player> ATTACKABLE = p -> !p.isCreative() && !p.isSpectator() && p.isAlive();
   }

   class CreakingBodyRotationControl extends BodyRotationControl {
      public CreakingBodyRotationControl(Creaking creaking) {
         super(creaking);
      }

      public void clientTick() {
         if (Creaking.this.canMove()) {
            super.clientTick();
         }
      }
   }

   class CreakingJumpControl extends JumpControl {
      public CreakingJumpControl(Creaking creaking) {
         super(creaking);
      }

      public void tick() {
         if (Creaking.this.canMove()) {
            super.tick();
         } else {
            Creaking.this.setJumping(false);
         }
      }
   }

   class CreakingLookControl extends LookControl {
      public CreakingLookControl(Creaking creaking) {
         super(creaking);
      }

      public void tick() {
         if (Creaking.this.canMove()) {
            super.tick();
         }
      }
   }

   class CreakingMoveControl extends MoveControl {
      public CreakingMoveControl(Creaking creaking) {
         super(creaking);
      }

      public void tick() {
         if (Creaking.this.canMove()) {
            super.tick();
         }
      }
   }

   class CreakingPathNavigation extends GroundPathNavigation {
      CreakingPathNavigation(Creaking creaking, Level level) {
         super(creaking, level);
      }

      public void tick() {
         if (Creaking.this.canMove()) {
            super.tick();
         }
      }

      protected PathFinder createPathFinder(int maxVisitedNodes) {
         this.nodeEvaluator = Creaking.this.new HomeNodeEvaluator();
         this.nodeEvaluator.setCanPassDoors(true);
         return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
      }
   }

   class HomeNodeEvaluator extends WalkNodeEvaluator {
      private static final int MAX_DISTANCE_TO_HOME_SQ = 1024;

      public BlockPathTypes getBlockPathType(BlockGetter level, int x, int y, int z) {
         BlockPos home = Creaking.this.getHomePos();
         if (home == null) {
            return super.getBlockPathType(level, x, y, z);
         } else {
            double d = home.distSqr(new Vec3i(x, y, z));
            return d > MAX_DISTANCE_TO_HOME_SQ && d >= home.distSqr(Creaking.this.blockPosition()) ? BlockPathTypes.BLOCKED : super.getBlockPathType(level, x, y, z);
         }
      }
   }
}
