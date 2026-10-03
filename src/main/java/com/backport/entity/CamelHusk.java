package com.backport.entity;

import com.backport.BackportEntities;
import com.backport.BackportSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.monster.Parched;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.tags.BlockTags;
import org.jetbrains.annotations.Nullable;

public class CamelHusk extends Camel {
   public CamelHusk(EntityType<? extends Camel> type, Level level) {
      super(type, level);
   }

   public boolean removeWhenFarAway(double distSqr) {
      return !this.isSaddled() && this.getPassengers().isEmpty() ? true : false;
   }

   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      this.setPersistenceRequired();
      return super.mobInteract(player, hand);
   }

   public boolean isFood(ItemStack stack) {
      return stack.is(Items.ROTTEN_FLESH);
   }

   public boolean canMate(Animal partner) {
      return false;
   }

   public boolean canFallInLove() {
      return false;
   }

   @Nullable
   public Camel getBreedOffspring(ServerLevel level, AgeableMob partner) {
      return null;
   }

   public boolean isBaby() {
      return false;
   }

   public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
      SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data, tag);
      if (reason == MobSpawnType.NATURAL && this.getPassengers().isEmpty()) {
         Parched rider = BackportEntities.PARCHED.create(this.level());
         if (rider != null) {
            rider.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
            rider.finalizeSpawn(level, difficulty, MobSpawnType.JOCKEY, null, null);
            rider.startRiding(this, false);
            level.addFreshEntity(rider);
         }
      }
      return result;
   }

   protected SoundEvent getAmbientSound() {
      return BackportSounds.ENTITY_CAMEL_HUSK_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      return BackportSounds.ENTITY_CAMEL_HUSK_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return BackportSounds.ENTITY_CAMEL_HUSK_HURT;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      if (state.is(BlockTags.SAND)) {
         this.playSound(BackportSounds.ENTITY_CAMEL_HUSK_STEP_SAND, 0.4F, 1.0F);
      } else {
         this.playSound(BackportSounds.ENTITY_CAMEL_HUSK_STEP, 0.4F, 1.0F);
      }
   }
}
