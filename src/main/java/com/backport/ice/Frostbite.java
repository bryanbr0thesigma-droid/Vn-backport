package com.backport.ice;

import org.jetbrains.annotations.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/** Zombie variant of the Ice Caves: freezes on melee, throws ice balls from range. */
public class Frostbite extends Zombie implements RangedAttackMob {
   public Frostbite(EntityType<? extends Zombie> type, Level level) {
      super(type, level);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Zombie.createAttributes().add(Attributes.MAX_HEALTH, 24.0);
   }

   @Override
   protected void addBehaviourGoals() {
      super.addBehaviourGoals();
      this.goalSelector.addGoal(1, new IceThrowGoal(this));
   }

   @Override
   public boolean canFreeze() {
      return false;
   }

   @Override
   public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
      boolean hit = super.doHurtTarget(target);
      if (hit && target instanceof LivingEntity living) {
         living.addEffect(new MobEffectInstance(IceCaves.FREEZING, 60), this);
      }
      return hit;
   }

   @Nullable
   @Override
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data, @Nullable net.minecraft.nbt.CompoundTag tag) {
      SpawnGroupData out = super.finalizeSpawn(level, difficulty, reason, data, tag);
      if (this.getMainHandItem().isEmpty() && reason != MobSpawnType.CONVERSION) {
         this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(IceCaves.ICE_BALL));
         this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
      }
      return out;
   }

   @Override
   public void performRangedAttack(LivingEntity target, float power) {
      IceBallEntity ball = new IceBallEntity(this.level(), this);
      ball.setItem(new ItemStack(IceCaves.ICE_BALL));
      double dx = target.getX() - this.getX();
      double dz = target.getZ() - this.getZ();
      double dy = target.getEyeY() - 1.1 - ball.getY();
      double horiz = Math.sqrt(dx * dx + dz * dz);
      ball.shoot(dx, dy + horiz * 0.2, dz, 1.6F, 14 - this.level().getDifficulty().getId() * 4);
      this.playSound(SoundEvents.SNOWBALL_THROW, 1.0F, 0.4F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
      this.level().addFreshEntity(ball);
   }

   private static class IceThrowGoal extends Goal {
      private final Frostbite mob;
      private int cooldown = 20;

      IceThrowGoal(Frostbite mob) {
         this.mob = mob;
      }

      @Override
      public boolean canUse() {
         LivingEntity t = this.mob.getTarget();
         if (t == null || !t.isAlive() || !this.mob.getMainHandItem().is(IceCaves.ICE_BALL)) {
            return false;
         }
         double d = this.mob.distanceToSqr(t);
         return d >= 9.0 && d <= 196.0 && this.mob.getSensing().hasLineOfSight(t);
      }

      @Override
      public boolean canContinueToUse() {
         return this.canUse();
      }

      @Override
      public void tick() {
         LivingEntity t = this.mob.getTarget();
         if (t == null) {
            return;
         }
         this.mob.getLookControl().setLookAt(t, 30.0F, 30.0F);
         if (--this.cooldown <= 0) {
            this.mob.performRangedAttack(t, 1.0F);
            this.cooldown = 50;
         }
      }
   }
}
