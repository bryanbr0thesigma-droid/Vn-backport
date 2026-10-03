package com.backport.entity;

import com.backport.BackportEntities;
import com.backport.BackportSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class Nautilus extends AbstractNautilus {
   public Nautilus(EntityType<? extends Nautilus> type, Level level) {
      super(type, level);
   }

   @Nullable
   public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
      Nautilus baby = BackportEntities.NAUTILUS.create(level);
      if (baby != null && this.isTame()) {
         baby.setOwnerUUID(this.getOwnerUUID());
         baby.setTame(true);
      }
      return baby;
   }

   public EntityDimensions getDimensions(Pose pose) {
      EntityDimensions d = super.getDimensions(pose);
      return this.isBaby() ? d.scale(0.5F) : d;
   }

   protected SoundEvent getAmbientSound() {
      if (this.isBaby()) return this.isUnderWater() ? BackportSounds.ENTITY_BABY_NAUTILUS_AMBIENT : BackportSounds.ENTITY_BABY_NAUTILUS_AMBIENT_LAND;
      return this.isUnderWater() ? BackportSounds.ENTITY_NAUTILUS_AMBIENT : BackportSounds.ENTITY_NAUTILUS_AMBIENT_LAND;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      if (this.isBaby()) return this.isUnderWater() ? BackportSounds.ENTITY_BABY_NAUTILUS_HURT : BackportSounds.ENTITY_BABY_NAUTILUS_HURT_LAND;
      return this.isUnderWater() ? BackportSounds.ENTITY_NAUTILUS_HURT : BackportSounds.ENTITY_NAUTILUS_HURT_LAND;
   }

   protected SoundEvent getDeathSound() {
      if (this.isBaby()) return this.isUnderWater() ? BackportSounds.ENTITY_BABY_NAUTILUS_DEATH : BackportSounds.ENTITY_BABY_NAUTILUS_DEATH_LAND;
      return this.isUnderWater() ? BackportSounds.ENTITY_NAUTILUS_DEATH : BackportSounds.ENTITY_NAUTILUS_DEATH_LAND;
   }

   protected SoundEvent getDashSound() {
      return this.isUnderWater() ? BackportSounds.ENTITY_NAUTILUS_DASH : BackportSounds.ENTITY_NAUTILUS_DASH_LAND;
   }

   protected SoundEvent getDashReadySound() {
      return this.isUnderWater() ? BackportSounds.ENTITY_NAUTILUS_DASH_READY : BackportSounds.ENTITY_NAUTILUS_DASH_READY_LAND;
   }

   public boolean removeWhenFarAway(double dist) {
      return !this.isTame();
   }

   public boolean canBeAffected(MobEffectInstance effect) {
      return effect.getEffect() == MobEffects.POISON ? false : super.canBeAffected(effect);
   }
}
