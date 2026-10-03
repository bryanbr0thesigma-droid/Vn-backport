package com.backport.entity;

import com.backport.Backport;
import com.backport.BackportEntities;
import com.backport.BackportItems;
import com.backport.BackportParticles;
import com.backport.BackportSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SulfurCube extends AgeableMob {
   private static final EntityDataAccessor<ItemStack> BODY = SynchedEntityData.defineId(SulfurCube.class, EntityDataSerializers.ITEM_STACK);
   private static final EntityDataAccessor<Integer> FUSE = SynchedEntityData.defineId(SulfurCube.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> FROM_BUCKET = SynchedEntityData.defineId(SulfurCube.class, EntityDataSerializers.BOOLEAN);
   public static final TagKey<net.minecraft.world.item.Item> SWALLOWABLE = TagKey.create(Registries.ITEM, Backport.id("sulfur_cube_swallowable"));
   public static final TagKey<net.minecraft.world.item.Item> FOOD = TagKey.create(Registries.ITEM, Backport.id("sulfur_cube_food"));

   /** bounce, frictionMod, airDragMod, kbResist, kbHorizontal, kbVertical, buoyant, fuse(-1 none), power, hot, pushCooldown, pushThreshold, sounds */
   public record Archetype(String name, double bounce, double friction, double airDrag, double kbResist, double kbH, double kbV,
                           boolean buoyant, int fuse, float power, boolean hot, int pushCooldown, double pushThreshold) {
      public TagKey<net.minecraft.world.item.Item> tag() {
         return TagKey.create(Registries.ITEM, Backport.id("sulfur_cube_archetype/" + this.name));
      }

      public SoundEvent hitSound() {
         return BackportSounds.byName("entity.sulfur_cube." + dotted() + ".hit");
      }

      public SoundEvent pushSound() {
         return BackportSounds.byName("entity.sulfur_cube." + dotted() + ".push");
      }

      private String dotted() {
         return this.name;
      }
   }

   public static final List<Archetype> ARCHETYPES = List.of(
      new Archetype("bouncy", 0.9, -0.7, -0.99, -1.0, 0.4125, 0.105, true, -1, 0, false, 14, 0.3),
      new Archetype("explosive", 0.5, -0.7, -0.7, -1.0, 0.4125, 0.09, true, 120, 3.0F, false, 14, 0.1),
      new Archetype("fast_flat", 0.5, -0.8, -0.99, -1.0, 0.9125, 0.09, false, -1, 0, false, 18, 0.03),
      new Archetype("fast_sliding", 0.1, -0.95, -0.99, 0.5, 0.6625, 0.09, false, -1, 0, false, 20, 0.05),
      new Archetype("high_resistance", 0.2, 0.0, -0.99, 0.7, 0.4125, 0.09, false, -1, 0, false, 14, 0.03),
      new Archetype("hot", 0.5, -0.7, -0.9, -1.0, 0.4125, 0.09, true, -1, 0, true, 14, 0.2),
      new Archetype("light", 1.0, -0.7, 0.8, -1.0, 0.4125, 0.18, true, -1, 0, false, 14, 0.2),
      new Archetype("regular", 0.5, -0.7, -0.9, -1.0, 0.4125, 0.09, true, -1, 0, false, 10, 0.2),
      new Archetype("slow_bouncy", 0.6, -0.7, -0.95, 0.4, 0.4125, 0.24, false, -1, 0, false, 10, 0.05),
      new Archetype("slow_flat", 0.4, -0.6, -0.9, 0.5, 0.4125, 0.105, false, -1, 0, false, 18, 0.03),
      new Archetype("slow_sliding", 0.1, -0.95, -0.99, 0.8, 0.4125, 0.09, false, -1, 0, false, 20, 0.02),
      new Archetype("sticky", 0.0, 1.0, -0.99, -1.0, 0.4125, 0.09, false, -1, 0, false, 10, 0.05));

   private Archetype archetype;
   private ItemStack archetypeFor = ItemStack.EMPTY;
   private int pickupTimer;
   private int pushSoundCooldown;
   private int hopDelay = 20;
   private double lastVy;
   private double lastVx;
   private double lastVz;
   private int localFuse = -1;

   public SulfurCube(EntityType<? extends SulfurCube> type, Level level) {
      super(type, level);
      this.setPersistenceRequired();
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 8.0).add(Attributes.MOVEMENT_SPEED, 0.3);
   }

   @Override
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(BODY, ItemStack.EMPTY);
      this.entityData.define(FUSE, -1);
      this.entityData.define(FROM_BUCKET, false);
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(2, new TemptGoal(this, 1.0, Ingredient.of(FOOD), false) {
         @Override
         public boolean canUse() {
            return !SulfurCube.this.hasBodyItem() && super.canUse();
         }
      });
      this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8) {
         @Override
         public boolean canUse() {
            return !SulfurCube.this.hasBodyItem() && super.canUse();
         }
      });
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
   }

   public ItemStack getBody() {
      return this.entityData.get(BODY);
   }

   public boolean hasBodyItem() {
      return !this.getBody().isEmpty();
   }

   public int getFuse() {
      return this.entityData.get(FUSE);
   }

   public boolean isPrimed() {
      return this.getFuse() >= 0;
   }

   public boolean fromBucket() {
      return this.entityData.get(FROM_BUCKET);
   }

   public void setFromBucket(boolean b) {
      this.entityData.set(FROM_BUCKET, b);
   }

   private Archetype archetype() {
      ItemStack body = this.getBody();
      if (body.isEmpty()) {
         return null;
      }
      if (this.archetype != null && ItemStack.isSameItem(body, this.archetypeFor)) {
         return this.archetype;
      }
      this.archetypeFor = body.copy();
      this.archetype = null;
      for (Archetype a : ARCHETYPES) {
         if (body.is(a.tag())) {
            this.archetype = a;
         }
      }
      if (this.archetype == null) {
         this.archetype = ARCHETYPES.get(7);
      }
      return this.archetype;
   }

   @Override
   public EntityDimensions getDimensions(Pose pose) {
      EntityDimensions d = super.getDimensions(pose);
      return this.isBaby() ? d.scale(0.5F) : d;
   }

   @Override
   protected float getStandingEyeHeight(Pose pose, EntityDimensions dims) {
      return dims.height * 0.5F;
   }

   public void setBodyItem(ItemStack stack) {
      this.entityData.set(BODY, stack);
      this.archetype = null;
      this.archetypeFor = ItemStack.EMPTY;
   }

   @Override
   public void tick() {
      if (!this.level().isClientSide) {
         int fuse = this.getFuse();
         if (fuse > 0) {
            this.entityData.set(FUSE, fuse - 1);
         } else if (fuse == 0 && this.isAlive()) {
            this.explodeNow();
            return;
         }
         Archetype a = this.archetype();
         if (a != null && a.fuse() > 0 && !this.isPrimed() && this.level().getBestNeighborSignal(this.blockPosition()) > 0) {
            this.prime(false);
         }
         if (this.pickupTimer > 0) {
            this.pickupTimer--;
         }
      }
      if (this.pushSoundCooldown > 0) {
         this.pushSoundCooldown--;
      }
      this.lastVx = this.getDeltaMovement().x;
      this.lastVy = this.getDeltaMovement().y;
      this.lastVz = this.getDeltaMovement().z;
      super.tick();
      if (this.level().isClientSide && this.tickCount % 6 == 0 && !this.hasBodyItem() && this.getDeltaMovement().y > 0.1) {
         this.level().addParticle(BackportParticles.SULFUR_CUBE_GOO, this.getRandomX(0.5), this.getY(0.2), this.getRandomZ(0.5), 0, 0, 0);
      }
   }

   private void explodeNow() {
      Archetype a = this.archetype();
      float power = a != null && a.power() > 0 ? a.power() : 3.0F;
      this.dead = true;
      if (this.level() instanceof ServerLevel sl && true) {
         sl.explode(this, this.getX(), this.getY(0.0625), this.getZ(), power, Level.ExplosionInteraction.TNT);
      }
      this.discard();
   }

   public boolean prime(boolean imminent) {
      Archetype a = this.archetype();
      if (a == null || a.fuse() <= 0 || this.isPrimed() || !this.isAlive() || this.level().isClientSide) {
         return false;
      }
      int fuse = imminent ? this.random.nextInt(a.fuse() / 4) + a.fuse() / 8 : a.fuse();
      this.entityData.set(FUSE, fuse);
      this.setInvulnerable(true);
      this.playSound(SoundEvents.TNT_PRIMED, 1.0F, 1.0F);
      this.gameEvent(GameEvent.PRIME_FUSE);
      return true;
   }

   @Override
   public boolean hurt(DamageSource source, float amount) {
      if (this.hasBodyItem() && !this.level().isClientSide) {
         Archetype a = this.archetype();
         if (a != null && a.fuse() > 0 && !this.isPrimed()) {
            Entity direct = source.getDirectEntity();
            if (source.is(DamageTypeTags.IS_FIRE) || direct instanceof AbstractArrow arrow && arrow.isOnFire()) {
               this.prime(false);
            } else if (source.is(DamageTypeTags.IS_EXPLOSION)) {
               this.prime(true);
            }
         }
         if (source.is(DamageTypeTags.IS_PROJECTILE) || source.is(DamageTypeTags.IS_FALL) || source.getDirectEntity() instanceof LivingEntity) {
            if (source.getDirectEntity() != null && a != null) {
               this.hitKnockback(source.getDirectEntity(), a);
               this.playSound(a.hitSound(), 1.0F, 0.9F + this.random.nextFloat() * 0.2F);
            }
            return false;
         }
      }
      return super.hurt(source, amount);
   }

   private void hitKnockback(Entity from, Archetype a) {
      double dx = this.getX() - from.getX();
      double dz = this.getZ() - from.getZ();
      double len = Math.max(Math.sqrt(dx * dx + dz * dz), 0.0001);
      double strength = a.kbH() * (1.0 - Mth.clamp(a.kbResist(), -2.0, 1.0)) * 1.2;
      Vec3 v = this.getDeltaMovement();
      this.setDeltaMovement(v.x * 0.5 + dx / len * strength, Math.max(v.y, a.kbV() * 3.5), v.z * 0.5 + dz / len * strength);
      this.hasImpulse = true;
      this.hurtMarked = true;
   }

   @Override
   public void knockback(double strength, double x, double z) {
      if (!this.hasBodyItem()) {
         super.knockback(strength, x, z);
      }
   }

   @Override
   public void push(Entity other) {
      super.push(other);
      if (!this.hasBodyItem() || this.level().isClientSide) {
         return;
      }
      Archetype a = this.archetype();
      if (a == null) {
         return;
      }
      if (a.hot() && other instanceof LivingEntity le && !(other instanceof SulfurCube) && le.invulnerableTime <= 10) {
         DamageSource ds = new DamageSource(this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
            .getHolderOrThrow(net.minecraft.resources.ResourceKey.create(Registries.DAMAGE_TYPE, Backport.id("sulfur_cube_hot"))),
            other instanceof Player ? this : null);
         le.hurt(ds, 1.0F);
         le.setSecondsOnFire(2);
      }
      Vec3 ov = other.getDeltaMovement();
      double speed = Math.sqrt(ov.x * ov.x + ov.z * ov.z);
      Entity pusher = other.getControllingPassenger() != null ? other.getControllingPassenger() : other;
      if (pusher instanceof Player || other instanceof LivingEntity) {
         double dx = this.getX() - other.getX();
         double dz = this.getZ() - other.getZ();
         double len = Math.max(Math.sqrt(dx * dx + dz * dz), 0.0001);
         double impulse = Math.min(0.5, (speed + 0.03) * 0.8) * (1.0 - Mth.clamp(a.kbResist(), -2.0, 1.0) * 0.5);
         this.setDeltaMovement(this.getDeltaMovement().add(dx / len * impulse * 0.5, speed > 0.1 ? 0.04 : 0.0, dz / len * impulse * 0.5));
         this.hasImpulse = true;
         if (this.pushSoundCooldown <= 0 && speed > a.pushThreshold()) {
            this.pushSoundCooldown = a.pushCooldown();
            this.playSound(a.pushSound(), 1.0F, 0.9F + this.random.nextFloat() * 0.2F);
         }
      }
   }

   @Override
   public void travel(Vec3 input) {
      if (this.hasBodyItem() && !this.isInWater() && !this.isInLava() && !this.isNoGravity()) {
         Archetype a = this.archetype();
         double bounce = a.bounce();
         Vec3 d = this.getDeltaMovement();
         double y = (d.y - 0.08) * (1.0 - 0.02 * Math.max(0.0, 1.0 + a.airDrag()));
         double h;
         if (this.onGround()) {
            h = Mth.clamp(1.0 - 0.454 * (1.0 + a.friction()), 0.0, 0.995);
         } else {
            h = Mth.clamp(1.0 - 0.09 * (1.0 + a.airDrag()), 0.0, 0.999);
         }
         double px = d.x * h;
         double pz = d.z * h;
         this.setDeltaMovement(px, y, pz);
         Vec3 before = this.getDeltaMovement();
         this.move(MoverType.SELF, before);
         Vec3 after = this.getDeltaMovement();
         double nx = after.x;
         double ny = after.y;
         double nz = after.z;
         if (this.verticalCollision && before.y < -0.1) {
            ny = -before.y * bounce;
            if (ny < 0.08) {
               ny = 0.0;
            } else if (!this.level().isClientSide && this.tickCount % 2 == 0) {
               this.playSound(BackportSounds.ENTITY_SULFUR_CUBE_BOUNCE, Mth.clamp((float) ny * 2.0F, 0.2F, 1.0F), 1.0F);
            }
         } else if (this.verticalCollision) {
            ny = 0.0;
         }
         if (this.horizontalCollision) {
            if (Math.abs(before.x) > Math.abs(after.x) + 0.0001) {
               nx = -before.x * bounce;
            }
            if (Math.abs(before.z) > Math.abs(after.z) + 0.0001) {
               nz = -before.z * bounce;
            }
         }
         this.setDeltaMovement(nx, ny, nz);
         this.calculateEntityAnimation(false);
         return;
      }
      if (this.hasBodyItem()) {
         Archetype a = this.archetype();
         super.travel(input);
         if (a.buoyant() && (this.isInWater() || this.isInLava())) {
            double fluid = this.getFluidHeight(this.isInWater() ? FluidTags.WATER : FluidTags.LAVA);
            double vibe = 0.2 * Mth.sin(this.tickCount * 0.4F);
            double immersion = fluid - this.getBbHeight() * 0.2 + vibe;
            if (immersion > 0.0) {
               this.setDeltaMovement(this.getDeltaMovement().add(0.0, Math.min(1.0, immersion) * 0.04, 0.0));
            }
         }
         return;
      }
      super.travel(input);
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide && !this.hasBodyItem()) {
         if (this.hopDelay > 0) {
            this.hopDelay--;
         }
         boolean wants = this.getNavigation().isInProgress() || this.getMoveControl().hasWanted();
         if (this.onGround() && wants && this.hopDelay <= 0) {
            this.hopDelay = 12 + this.random.nextInt(14);
            float yaw = this.getYRot() * Mth.DEG_TO_RAD;
            double f = this.isBaby() ? 0.2 : 0.24;
            this.setDeltaMovement(-Mth.sin(yaw) * f, this.isBaby() ? 0.38 : 0.42, Mth.cos(yaw) * f);
            this.hasImpulse = true;
            this.playSound(this.isBaby() ? BackportSounds.ENTITY_SMALL_SULFUR_CUBE_JUMP : BackportSounds.ENTITY_SULFUR_CUBE_JUMP, 0.8F, 1.0F);
         }
         if (!this.onGround() && wants) {
            // keep forward speed in the air
            this.setSpeed((float) this.getAttributeValue(Attributes.MOVEMENT_SPEED));
         }
         if (this.pickupTimer <= 0 && !this.isBaby() && this.isAlive()) {
            for (ItemEntity ie : this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(1.0, 0.0, 1.0),
               e -> !e.hasPickUpDelay() && e.isAlive() && e.getItem().is(SWALLOWABLE))) {
               this.setBodyItem(ie.getItem().split(1));
               if (ie.getItem().isEmpty()) {
                  ie.discard();
               }
               this.playSound(BackportSounds.ENTITY_SULFUR_CUBE_ABSORB, 1.0F, 1.0F);
               this.getNavigation().stop();
               break;
            }
         }
      }
   }

   @Override
   protected float getJumpPower() {
      return 0.42F;
   }

   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack held = player.getItemInHand(hand);
      if (this.isBaby()) {
         if (held.is(FOOD)) {
            if (!this.level().isClientSide) {
               if (!player.getAbilities().instabuild) {
                  held.shrink(1);
               }
               this.ageUp(AgeableMob.getSpeedUpSecondsWhenFeeding(-this.getAge()), true);
               this.playSound(BackportSounds.ENTITY_SMALL_SULFUR_CUBE_EAT, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
         }
         return InteractionResult.PASS;
      }
      if (this.isPrimed()) {
         return InteractionResult.PASS;
      }
      Archetype a = this.archetype();
      boolean explosive = a != null && a.fuse() > 0;
      if (explosive && (held.is(Items.FLINT_AND_STEEL) || held.is(Items.FIRE_CHARGE))) {
         if (!this.level().isClientSide) {
            this.prime(false);
            if (held.is(Items.FLINT_AND_STEEL)) {
               held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            } else if (!player.getAbilities().instabuild) {
               held.shrink(1);
            }
         }
         return InteractionResult.sidedSuccess(this.level().isClientSide);
      }
      if (held.is(Items.SHEARS) && this.hasBodyItem()) {
         if (!this.level().isClientSide) {
            this.spawnAtLocation(this.getBody().copy(), this.getBbHeight() * 0.5F);
            this.setBodyItem(ItemStack.EMPTY);
            this.playSound(BackportSounds.ENTITY_SULFUR_CUBE_EJECT, 1.0F, 1.0F);
            this.pickupTimer = 100;
            held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            this.gameEvent(GameEvent.SHEAR, player);
         }
         return InteractionResult.sidedSuccess(this.level().isClientSide);
      }
      if (held.is(SWALLOWABLE)) {
         if (this.hasBodyItem() && ItemStack.isSameItem(held, this.getBody())) {
            return InteractionResult.PASS;
         }
         if (!this.level().isClientSide) {
            if (this.hasBodyItem()) {
               this.spawnAtLocation(this.getBody().copy(), this.getBbHeight() * 0.5F);
            }
            ItemStack one = held.copy();
            one.setCount(1);
            this.setBodyItem(one);
            if (!player.getAbilities().instabuild) {
               held.shrink(1);
            }
            this.playSound(BackportSounds.ENTITY_SULFUR_CUBE_ABSORB, 1.0F, 1.0F);
            this.getNavigation().stop();
            this.gameEvent(GameEvent.ENTITY_INTERACT);
         }
         return InteractionResult.sidedSuccess(this.level().isClientSide);
      }
      if (held.is(Items.BUCKET) && this.isAlive() && !this.hasBodyItem()) {
         this.playSound(BackportSounds.ITEM_BUCKET_FILL_SULFUR_CUBE, 1.0F, 1.0F);
         ItemStack bucket = new ItemStack(BackportItems.SULFUR_CUBE_BUCKET);
         CompoundTag tag = bucket.getOrCreateTag();
         tag.putInt("Age", this.getAge());
         tag.putBoolean("Baby", this.isBaby());
         if (this.hasCustomName()) {
            bucket.setHoverName(this.getCustomName());
         }
         ItemStack result = net.minecraft.world.item.ItemUtils.createFilledResult(held, player, bucket, false);
         player.setItemInHand(hand, result);
         if (!this.level().isClientSide) {
            this.discard();
         }
         return InteractionResult.sidedSuccess(this.level().isClientSide);
      }
      return super.mobInteract(player, hand);
   }

   @Override
   public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
      SulfurCube c = BackportEntities.SULFUR_CUBE.create(level);
      if (c != null) {
         c.setBaby(true);
      }
      return c;
   }

   @Override
   protected void ageBoundaryReached() {
      super.ageBoundaryReached();
      this.refreshDimensions();
   }

   @Override
   protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
      super.dropCustomDeathLoot(source, looting, recentlyHit);
      if (this.hasBodyItem()) {
         this.spawnAtLocation(this.getBody().copy());
      }
   }

   @Override
   public int getExperienceReward() {
      return this.isBaby() ? 0 : 1 + this.random.nextInt(2);
   }

   @Override
   public boolean removeWhenFarAway(double dist) {
      return false;
   }

   @Override
   public SoundSource getSoundSource() {
      return SoundSource.NEUTRAL;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource s) {
      return this.isBaby() ? BackportSounds.ENTITY_SMALL_SULFUR_CUBE_HURT : BackportSounds.ENTITY_SULFUR_CUBE_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return this.isBaby() ? BackportSounds.ENTITY_SMALL_SULFUR_CUBE_DEATH : BackportSounds.ENTITY_SULFUR_CUBE_DEATH;
   }

   @Override
   public boolean canBreatheUnderwater() {
      return this.hasBodyItem() || super.canBreatheUnderwater();
   }

   @Override
   public boolean canFreeze() {
      return !this.hasBodyItem() && super.canFreeze();
   }

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.put("body", this.getBody().save(new CompoundTag()));
      tag.putInt("pickup_timer", this.pickupTimer);
      tag.putBoolean("from_bucket", this.fromBucket());
      tag.putInt("fuse", this.getFuse());
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setBodyItem(ItemStack.of(tag.getCompound("body")));
      this.pickupTimer = tag.getInt("pickup_timer");
      this.setFromBucket(tag.getBoolean("from_bucket"));
      this.entityData.set(FUSE, tag.contains("fuse") ? tag.getInt("fuse") : -1);
   }
}
