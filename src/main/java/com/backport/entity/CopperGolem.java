package com.backport.entity;

import com.backport.BackportSounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class CopperGolem extends AbstractGolem {
   private static final long IGNORE_WEATHERING_TICK = -2L;
   private static final long UNSET_WEATHERING_TICK = -1L;
   private static final EntityDataAccessor<Integer> DATA_WEATHER_STATE = SynchedEntityData.defineId(CopperGolem.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData.defineId(CopperGolem.class, EntityDataSerializers.INT);
   private long nextWeatheringTick = UNSET_WEATHERING_TICK;
   private int idleAnimationStartTick = 0;
   private java.util.UUID lastLightningBolt;
   public final AnimationState idleAnimationState = new AnimationState();
   public final AnimationState interactionGetItemAnimationState = new AnimationState();
   public final AnimationState interactionGetNoItemAnimationState = new AnimationState();
   public final AnimationState interactionDropItemAnimationState = new AnimationState();
   public final AnimationState interactionDropNoItemAnimationState = new AnimationState();

   public enum State {
      IDLE,
      GETTING_ITEM,
      GETTING_NO_ITEM,
      DROPPING_ITEM,
      DROPPING_NO_ITEM
   }

   public CopperGolem(EntityType<? extends AbstractGolem> type, Level level) {
      super(type, level);
      ((net.minecraft.world.entity.ai.navigation.GroundPathNavigation)this.getNavigation()).setCanOpenDoors(true);
      this.setPersistenceRequired();
      this.setMaxUpStep(1.0F);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.2F).add(Attributes.MAX_HEALTH, 12.0);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(2, new CopperGolemTransportGoal(this));
      this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
      this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
      this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_WEATHER_STATE, 0);
      this.entityData.define(DATA_STATE, 0);
   }

   public State getState() {
      return State.values()[Math.floorMod(this.entityData.get(DATA_STATE), State.values().length)];
   }

   public void setState(State state) {
      this.entityData.set(DATA_STATE, state.ordinal());
   }

   public WeatheringCopper.WeatherState getWeatherState() {
      return WeatheringCopper.WeatherState.values()[Math.floorMod(this.entityData.get(DATA_WEATHER_STATE), 4)];
   }

   public void setWeatherState(WeatheringCopper.WeatherState state) {
      this.entityData.set(DATA_WEATHER_STATE, state.ordinal());
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putLong("next_weather_age", this.nextWeatheringTick);
      tag.putInt("weather_state", this.getWeatherState().ordinal());
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.nextWeatheringTick = tag.contains("next_weather_age") ? tag.getLong("next_weather_age") : UNSET_WEATHERING_TICK;
      this.entityData.set(DATA_WEATHER_STATE, tag.getInt("weather_state"));
   }

   public void tick() {
      super.tick();
      if (this.level().isClientSide()) {
         if (!this.isNoAi()) {
            this.setupAnimationStates();
         }
      } else {
         this.updateWeathering((ServerLevel)this.level(), this.level().getRandom(), this.level().getGameTime());
      }
   }

   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (stack.isEmpty()) {
         ItemStack held = this.getMainHandItem();
         if (!held.isEmpty()) {
            if (!this.level().isClientSide) {
               this.spawnAtLocation(held.copy());
               this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }

            return InteractionResult.sidedSuccess(this.level().isClientSide);
         }
      }

      Level level = this.level();
      if (level.isClientSide()) {
         return InteractionResult.PASS;
      } else if (stack.is(Items.HONEYCOMB) && this.nextWeatheringTick != IGNORE_WEATHERING_TICK) {
         BlockPos pos = this.blockPosition();
         level.levelEvent(null, 3003, pos, 0);
         level.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F);
         this.nextWeatheringTick = IGNORE_WEATHERING_TICK;
         if (!player.getAbilities().instabuild) {
            stack.shrink(1);
         }

         return InteractionResult.SUCCESS;
      } else if (stack.is(ItemTags.AXES) && this.nextWeatheringTick == IGNORE_WEATHERING_TICK) {
         level.playSound(null, this, SoundEvents.AXE_SCRAPE, this.getSoundSource(), 1.0F, 1.0F);
         level.levelEvent(null, 3004, this.blockPosition(), 0);
         this.nextWeatheringTick = UNSET_WEATHERING_TICK;
         stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
         return InteractionResult.SUCCESS;
      } else {
         if (stack.is(ItemTags.AXES)) {
            WeatheringCopper.WeatherState weather = this.getWeatherState();
            if (weather != WeatheringCopper.WeatherState.UNAFFECTED) {
               level.playSound(null, this, SoundEvents.AXE_SCRAPE, this.getSoundSource(), 1.0F, 1.0F);
               level.levelEvent(null, 3005, this.blockPosition(), 0);
               this.nextWeatheringTick = UNSET_WEATHERING_TICK;
               this.setWeatherState(WeatheringCopper.WeatherState.values()[weather.ordinal() - 1]);
               stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
               return InteractionResult.SUCCESS;
            }
         }

         return super.mobInteract(player, hand);
      }
   }

   private void updateWeathering(ServerLevel level, RandomSource random, long gameTime) {
      if (this.nextWeatheringTick != IGNORE_WEATHERING_TICK) {
         if (this.nextWeatheringTick == UNSET_WEATHERING_TICK) {
            this.nextWeatheringTick = gameTime + random.nextIntBetweenInclusive(504000, 552000);
         } else {
            WeatheringCopper.WeatherState weather = this.getWeatherState();
            boolean fully = weather == WeatheringCopper.WeatherState.OXIDIZED;
            if (gameTime >= this.nextWeatheringTick && !fully) {
               WeatheringCopper.WeatherState next = WeatheringCopper.WeatherState.values()[weather.ordinal() + 1];
               this.setWeatherState(next);
               this.nextWeatheringTick = next == WeatheringCopper.WeatherState.OXIDIZED ? 0L : this.nextWeatheringTick + random.nextIntBetweenInclusive(504000, 552000);
            }

            if (fully && this.canTurnToStatue(level)) {
               this.turnToStatue(level);
            }
         }
      }
   }

   private boolean canTurnToStatue(Level level) {
      return com.backport.CopperGolemStatues.enabled() && level.getBlockState(this.blockPosition()).isAir() && level.getRandom().nextFloat() <= 0.0058F;
   }

   private void turnToStatue(ServerLevel level) {
      com.backport.CopperGolemStatues.turnToStatue(this, level);
   }

   private void setupAnimationStates() {
      switch (this.getState()) {
         case IDLE -> {
            this.interactionGetNoItemAnimationState.stop();
            this.interactionGetItemAnimationState.stop();
            this.interactionDropItemAnimationState.stop();
            this.interactionDropNoItemAnimationState.stop();
            if (this.idleAnimationStartTick == this.tickCount) {
               this.idleAnimationState.start(this.tickCount);
            } else if (this.idleAnimationStartTick == 0) {
               this.idleAnimationStartTick = this.tickCount + this.random.nextInt(200, 240);
            }

            if (this.tickCount == this.idleAnimationStartTick + 10) {
               this.playHeadSpinSound();
               this.idleAnimationStartTick = 0;
            }
         }
         case GETTING_ITEM -> {
            this.idleAnimationState.stop();
            this.idleAnimationStartTick = 0;
            this.interactionGetNoItemAnimationState.stop();
            this.interactionDropItemAnimationState.stop();
            this.interactionDropNoItemAnimationState.stop();
            this.interactionGetItemAnimationState.startIfStopped(this.tickCount);
         }
         case GETTING_NO_ITEM -> {
            this.idleAnimationState.stop();
            this.idleAnimationStartTick = 0;
            this.interactionGetItemAnimationState.stop();
            this.interactionDropNoItemAnimationState.stop();
            this.interactionDropItemAnimationState.stop();
            this.interactionGetNoItemAnimationState.startIfStopped(this.tickCount);
         }
         case DROPPING_ITEM -> {
            this.idleAnimationState.stop();
            this.idleAnimationStartTick = 0;
            this.interactionGetItemAnimationState.stop();
            this.interactionGetNoItemAnimationState.stop();
            this.interactionDropNoItemAnimationState.stop();
            this.interactionDropItemAnimationState.startIfStopped(this.tickCount);
         }
         case DROPPING_NO_ITEM -> {
            this.idleAnimationState.stop();
            this.idleAnimationStartTick = 0;
            this.interactionGetItemAnimationState.stop();
            this.interactionGetNoItemAnimationState.stop();
            this.interactionDropItemAnimationState.stop();
            this.interactionDropNoItemAnimationState.startIfStopped(this.tickCount);
         }
      }
   }

   public void setWaxed() {
      this.nextWeatheringTick = IGNORE_WEATHERING_TICK;
   }

   public void spawn(WeatheringCopper.WeatherState weatherState) {
      this.setWeatherState(weatherState);
      this.playSpawnSound();
   }

   public void playSpawnSound() {
      this.playSound(BackportSounds.ENTITY_COPPER_GOLEM_SPAWN, 1.0F, 1.0F);
   }

   private void playHeadSpinSound() {
      if (!this.isSilent()) {
         this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), this.spinSound(), this.getSoundSource(), 1.0F, 1.0F, false);
      }
   }

   private SoundEvent spinSound() {
      return switch (this.getWeatherState()) {
         case UNAFFECTED, EXPOSED -> BackportSounds.ENTITY_COPPER_GOLEM_SPIN;
         case WEATHERED -> BackportSounds.ENTITY_COPPER_GOLEM_WEATHERED_SPIN;
         case OXIDIZED -> BackportSounds.ENTITY_COPPER_GOLEM_OXIDIZED_SPIN;
      };
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return switch (this.getWeatherState()) {
         case UNAFFECTED, EXPOSED -> BackportSounds.ENTITY_COPPER_GOLEM_HURT;
         case WEATHERED -> BackportSounds.ENTITY_COPPER_GOLEM_WEATHERED_HURT;
         case OXIDIZED -> BackportSounds.ENTITY_COPPER_GOLEM_OXIDIZED_HURT;
      };
   }

   protected SoundEvent getDeathSound() {
      return switch (this.getWeatherState()) {
         case UNAFFECTED, EXPOSED -> BackportSounds.ENTITY_COPPER_GOLEM_DEATH;
         case WEATHERED -> BackportSounds.ENTITY_COPPER_GOLEM_WEATHERED_DEATH;
         case OXIDIZED -> BackportSounds.ENTITY_COPPER_GOLEM_OXIDIZED_DEATH;
      };
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      SoundEvent sound = switch (this.getWeatherState()) {
         case UNAFFECTED, EXPOSED -> BackportSounds.ENTITY_COPPER_GOLEM_STEP;
         case WEATHERED -> BackportSounds.ENTITY_COPPER_GOLEM_WEATHERED_STEP;
         case OXIDIZED -> BackportSounds.ENTITY_COPPER_GOLEM_OXIDIZED_STEP;
      };
      this.playSound(sound, 1.0F, 1.0F);
   }

   public Vec3 getLeashOffset() {
      return new Vec3(0.0, 0.75F * this.getEyeHeight(), 0.0);
   }

   protected void actuallyHurt(DamageSource source, float amount) {
      super.actuallyHurt(source, amount);
      this.setState(State.IDLE);
   }

   public void thunderHit(ServerLevel level, LightningBolt bolt) {
      super.thunderHit(level, bolt);
      if (!bolt.getUUID().equals(this.lastLightningBolt)) {
         this.lastLightningBolt = bolt.getUUID();
         WeatheringCopper.WeatherState weather = this.getWeatherState();
         if (weather != WeatheringCopper.WeatherState.UNAFFECTED) {
            this.nextWeatheringTick = UNSET_WEATHERING_TICK;
            this.setWeatherState(WeatheringCopper.WeatherState.values()[weather.ordinal() - 1]);
         }
      }
   }
}
