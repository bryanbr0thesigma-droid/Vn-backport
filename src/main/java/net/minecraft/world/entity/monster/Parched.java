package net.minecraft.world.entity.monster;

import com.backport.BackportSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Parched extends AbstractSkeleton {
   public Parched(EntityType<? extends Parched> type, Level level) {
      super(type, level);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return AbstractSkeleton.createAttributes().add(Attributes.MAX_HEALTH, 16.0);
   }

   protected AbstractArrow getArrow(ItemStack stack, float power) {
      AbstractArrow arrow = super.getArrow(stack, power);
      if (arrow instanceof Arrow normal) {
         normal.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600));
      }
      return arrow;
   }

   protected SoundEvent getAmbientSound() {
      return BackportSounds.ENTITY_PARCHED_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return BackportSounds.ENTITY_PARCHED_HURT;
   }

   protected SoundEvent getDeathSound() {
      return BackportSounds.ENTITY_PARCHED_DEATH;
   }

   protected SoundEvent getStepSound() {
      return BackportSounds.ENTITY_PARCHED_STEP;
   }

   public boolean canBeAffected(MobEffectInstance effect) {
      return effect.getEffect() == MobEffects.WEAKNESS ? false : super.canBeAffected(effect);
   }

   public void reassessWeaponGoal() {
      super.reassessWeaponGoal();
      if (this.level() != null && !this.level().isClientSide) {
         int interval = this.level().getDifficulty() != net.minecraft.world.Difficulty.HARD ? 70 : 50;
         ((com.backport.mixin.AbstractSkeletonAccessor) this).backport$getBowGoal().setMinAttackInterval(interval);
      }
   }
}
