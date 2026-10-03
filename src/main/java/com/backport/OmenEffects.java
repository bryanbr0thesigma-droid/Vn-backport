package com.backport;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** 26.x status effects: Wind Charged, Weaving, Oozing, Infested, Raid Omen, Trial Omen, plus their potions. */
public final class OmenEffects {
   public static final MobEffect WIND_CHARGED = register("wind_charged", new MobEffect(MobEffectCategory.HARMFUL, 12438015) { });
   public static final MobEffect WEAVING = register("weaving", new MobEffect(MobEffectCategory.HARMFUL, 7891290) { });
   public static final MobEffect OOZING = register("oozing", new MobEffect(MobEffectCategory.HARMFUL, 10092451) { });
   public static final MobEffect INFESTED = register("infested", new MobEffect(MobEffectCategory.HARMFUL, 9214860) { });
   public static final MobEffect TRIAL_OMEN = register("trial_omen", new MobEffect(MobEffectCategory.NEUTRAL, 1484454) { });
   public static final MobEffect RAID_OMEN = register("raid_omen", new MobEffect(MobEffectCategory.NEUTRAL, 14565464) {
      @Override
      public boolean isDurationEffectTick(int duration, int amplifier) {
         return duration == 1;
      }

      @Override
      public void applyEffectTick(LivingEntity entity, int amplifier) {
         if (entity instanceof ServerPlayer player && !player.isSpectator()) {
            ServerLevel level = player.serverLevel();
            BYPASS_RAID_OMEN = true;
            try {
               level.getRaids().createOrExtendRaid(player);
            } finally {
               BYPASS_RAID_OMEN = false;
            }
         }
      }
   });
   /** True while the raid omen finishes and may really start the raid (see RaidsMixin). */
   public static boolean BYPASS_RAID_OMEN;

   public static final Potion WIND_CHARGING = Registry.register(BuiltInRegistries.POTION, Backport.id("wind_charging"), new Potion("wind_charging", new MobEffectInstance(WIND_CHARGED, 3600)));
   public static final Potion WEAVING_POTION = Registry.register(BuiltInRegistries.POTION, Backport.id("weaving"), new Potion("weaving", new MobEffectInstance(WEAVING, 3600)));
   public static final Potion OOZING_POTION = Registry.register(BuiltInRegistries.POTION, Backport.id("oozing"), new Potion("oozing", new MobEffectInstance(OOZING, 3600)));
   public static final Potion INFESTED_POTION = Registry.register(BuiltInRegistries.POTION, Backport.id("infested"), new Potion("infested", new MobEffectInstance(INFESTED, 3600)));

   private OmenEffects() {
   }

   private static MobEffect register(String name, MobEffect effect) {
      return Registry.register(BuiltInRegistries.MOB_EFFECT, Backport.id(name), effect);
   }

   public static void init() {
      PotionBrewing.addMix(Potions.AWKWARD, BackportItems.BREEZE_ROD, WIND_CHARGING);
      PotionBrewing.addMix(Potions.AWKWARD, Blocks.COBWEB.asItem(), WEAVING_POTION);
      PotionBrewing.addMix(Potions.AWKWARD, Blocks.SLIME_BLOCK.asItem(), OOZING_POTION);
      PotionBrewing.addMix(Potions.AWKWARD, Blocks.STONE.asItem(), INFESTED_POTION);
      ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
         if (!(entity.level() instanceof ServerLevel level)) {
            return;
         }
         if (entity.hasEffect(WIND_CHARGED)) {
            windBurst(level, entity, 3.0F + entity.getRandom().nextFloat() * 2.0F);
         }
         if (entity.hasEffect(WEAVING) && (entity instanceof Player || level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING))) {
            weave(level, entity.getRandom(), entity.blockPosition());
         }
         if (entity.hasEffect(OOZING)) {
            ooze(level, entity);
         }
      });
      ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
         if (entity.level() instanceof ServerLevel level && entity.hasEffect(INFESTED) && entity.getRandom().nextFloat() <= 0.1F) {
            int count = Mth.randomBetweenInclusive(entity.getRandom(), 1, 2);
            for (int i = 0; i < count; i++) {
               spawnSilverfish(level, entity);
            }
         }
         return true;
      });
   }

   private static void windBurst(ServerLevel level, LivingEntity mob, float radius) {
      Vec3 center = new Vec3(mob.getX(), mob.getY() + mob.getBbHeight() / 2.0F, mob.getZ());
      level.sendParticles(ParticleTypes.POOF, center.x, center.y, center.z, 30, 0.8, 0.8, 0.8, 0.12);
      level.sendParticles(ParticleTypes.CLOUD, center.x, center.y, center.z, 15, 0.5, 0.5, 0.5, 0.2);
      level.playSound(null, center.x, center.y, center.z, BackportSounds.ENTITY_WIND_CHARGE_WIND_BURST, SoundSource.NEUTRAL, 1.5F, 1.0F);
      for (Entity e : level.getEntities(mob, AABB.ofSize(center, radius * 2, radius * 2, radius * 2))) {
         Vec3 offset = e.getBoundingBox().getCenter().subtract(center);
         double d = offset.length();
         if (d < radius && !(e instanceof com.backport.entity.WindCharge)) {
            double power = (1.0 - d / radius) * 1.5;
            Vec3 push = d < 1.0E-4 ? new Vec3(0.0, 1.0, 0.0) : offset.normalize();
            if (e instanceof LivingEntity living) {
               power *= 1.0 - living.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE);
            }
            e.setDeltaMovement(e.getDeltaMovement().add(push.scale(power)));
            e.hurtMarked = true;
            e.fallDistance = 0.0F;
         }
      }
   }

   private static void weave(ServerLevel level, RandomSource random, BlockPos pos) {
      int max = Mth.randomBetweenInclusive(random, 2, 3);
      java.util.Set<BlockPos> targets = new java.util.HashSet<>();
      for (BlockPos p : BlockPos.randomInCube(random, 15, pos, 1)) {
         BlockPos below = p.below();
         if (!targets.contains(p) && level.getBlockState(p).canBeReplaced() && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
            targets.add(p.immutable());
            if (targets.size() >= max) {
               break;
            }
         }
      }
      for (BlockPos p : targets) {
         level.setBlockAndUpdate(p, Blocks.COBWEB.defaultBlockState());
         level.levelEvent(3018, p, 0);
      }
   }

   private static void ooze(ServerLevel level, LivingEntity mob) {
      int cramming = level.getGameRules().getInt(GameRules.RULE_MAX_ENTITY_CRAMMING);
      int nearby = level.getEntities(EntityType.SLIME, mob.getBoundingBox().inflate(2.0), s -> s != mob).size();
      int count = cramming < 1 ? 2 : Mth.clamp(cramming - nearby, 0, 2);
      for (int i = 0; i < count; i++) {
         Slime slime = EntityType.SLIME.create(level);
         if (slime != null) {
            slime.setSize(2, true);
            slime.moveTo(mob.getX(), mob.getY() + 0.5, mob.getZ(), level.getRandom().nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(slime);
         }
      }
   }

   private static void spawnSilverfish(ServerLevel level, LivingEntity mob) {
      Silverfish fish = EntityType.SILVERFISH.create(level);
      if (fish != null) {
         RandomSource random = mob.getRandom();
         float angle = Mth.randomBetween(random, (float) (-Math.PI / 2), (float) (Math.PI / 2));
         Vec3 look = mob.getLookAngle().multiply(0.3, 0.45, 0.3).yRot(angle);
         fish.moveTo(mob.getX(), mob.getY() + mob.getBbHeight() / 2.0, mob.getZ(), random.nextFloat() * 360.0F, 0.0F);
         fish.setDeltaMovement(look);
         level.addFreshEntity(fish);
         fish.playSound(net.minecraft.sounds.SoundEvents.SILVERFISH_HURT, 1.0F, 1.0F);
      }
   }
}
