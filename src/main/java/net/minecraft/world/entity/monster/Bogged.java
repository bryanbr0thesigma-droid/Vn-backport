package net.minecraft.world.entity.monster;

import com.backport.BackportSounds;
import com.backport.mixin.AbstractSkeletonAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class Bogged extends AbstractSkeleton {
   private static final EntityDataAccessor<Boolean> DATA_SHEARED = SynchedEntityData.defineId(Bogged.class, EntityDataSerializers.BOOLEAN);

   public static AttributeSupplier.Builder createAttributes() {
      return AbstractSkeleton.createAttributes().add(Attributes.MAX_HEALTH, 16.0);
   }

   public Bogged(EntityType<? extends Bogged> type, Level level) {
      super(type, level);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_SHEARED, false);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putBoolean("sheared", this.isSheared());
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setSheared(tag.getBoolean("sheared"));
   }

   public boolean isSheared() {
      return this.entityData.get(DATA_SHEARED);
   }

   public void setSheared(boolean sheared) {
      this.entityData.set(DATA_SHEARED, sheared);
   }

   protected InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (stack.is(Items.SHEARS) && !this.isSheared()) {
         if (this.level() instanceof ServerLevel level) {
            level.playSound(null, this, BackportSounds.ENTITY_BOGGED_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
            int reds = 1 + this.random.nextInt(2);
            int browns = 1 + this.random.nextInt(2);
            this.spawnAtLocation(new ItemStack(Items.RED_MUSHROOM, reds), this.getBbHeight());
            this.spawnAtLocation(new ItemStack(Items.BROWN_MUSHROOM, browns), this.getBbHeight());
            this.setSheared(true);
            this.gameEvent(GameEvent.SHEAR, player);
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
         }

         return InteractionResult.sidedSuccess(this.level().isClientSide);
      }

      return super.mobInteract(player, hand);
   }

   protected SoundEvent getAmbientSound() {
      return BackportSounds.ENTITY_BOGGED_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return BackportSounds.ENTITY_BOGGED_HURT;
   }

   protected SoundEvent getDeathSound() {
      return BackportSounds.ENTITY_BOGGED_DEATH;
   }

   protected SoundEvent getStepSound() {
      return BackportSounds.ENTITY_BOGGED_STEP;
   }

   protected AbstractArrow getArrow(ItemStack stack, float power) {
      AbstractArrow arrow = super.getArrow(stack, power);
      if (arrow instanceof Arrow normal) {
         normal.addEffect(new MobEffectInstance(MobEffects.POISON, 100));
      }

      return arrow;
   }

   public void reassessWeaponGoal() {
      super.reassessWeaponGoal();
      if (this.level() != null && !this.level().isClientSide) {
         int interval = this.level().getDifficulty() != Difficulty.HARD ? 70 : 50;
         ((AbstractSkeletonAccessor)this).backport$getBowGoal().setMinAttackInterval(interval);
      }
   }
}
