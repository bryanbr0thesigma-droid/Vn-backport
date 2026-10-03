package com.backport.entity;

import com.backport.BackportSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

public class ZombieNautilus extends AbstractNautilus {
   private static final EntityDataAccessor<Boolean> CORAL = SynchedEntityData.defineId(ZombieNautilus.class, EntityDataSerializers.BOOLEAN);

   public ZombieNautilus(EntityType<? extends ZombieNautilus> type, Level level) {
      super(type, level);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return AbstractNautilus.createAttributes().add(Attributes.MOVEMENT_SPEED, 1.1F);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(CORAL, false);
   }

   public boolean isCoral() {
      return this.entityData.get(CORAL);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putBoolean("coral", this.isCoral());
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.entityData.set(CORAL, tag.getBoolean("coral"));
   }

   public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
      this.entityData.set(CORAL, level.getBiome(this.blockPosition()).is(net.minecraft.world.level.biome.Biomes.WARM_OCEAN));
      return super.finalizeSpawn(level, difficulty, reason, data, tag);
   }

   @Nullable
   public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
      return null;
   }

   public boolean isBaby() {
      return false;
   }

   public void aiStep() {
      if (this.isAlive() && !this.level().isClientSide && this.getArmorItem().isEmpty() && this.isSunBurnTick()) {
         this.setSecondsOnFire(8);
      }
      super.aiStep();
   }

   protected SoundEvent getAmbientSound() {
      return this.isUnderWater() ? BackportSounds.ENTITY_ZOMBIE_NAUTILUS_AMBIENT : BackportSounds.ENTITY_ZOMBIE_NAUTILUS_AMBIENT_LAND;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return this.isUnderWater() ? BackportSounds.ENTITY_ZOMBIE_NAUTILUS_HURT : BackportSounds.ENTITY_ZOMBIE_NAUTILUS_HURT_LAND;
   }

   protected SoundEvent getDeathSound() {
      return this.isUnderWater() ? BackportSounds.ENTITY_ZOMBIE_NAUTILUS_DEATH : BackportSounds.ENTITY_ZOMBIE_NAUTILUS_DEATH_LAND;
   }

   protected SoundEvent getDashSound() {
      return this.isUnderWater() ? BackportSounds.ENTITY_ZOMBIE_NAUTILUS_DASH : BackportSounds.ENTITY_ZOMBIE_NAUTILUS_DASH_LAND;
   }

   protected SoundEvent getDashReadySound() {
      return this.isUnderWater() ? BackportSounds.ENTITY_ZOMBIE_NAUTILUS_DASH_READY : BackportSounds.ENTITY_ZOMBIE_NAUTILUS_DASH_READY_LAND;
   }

   public boolean removeWhenFarAway(double dist) {
      return !this.isTame();
   }
}
