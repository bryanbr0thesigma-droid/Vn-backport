package com.backport.ice;

import com.backport.Backport;
import com.backport.sulfur.SulfurSpikeBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/** Ice Caves content from Bedrock preview 26.60 (Drop 4 of 2026): icicles, ice crystals, ice balls, Freezing, Frostbite. */
public final class IceCaves {
   private static final java.util.UUID FREEZING_SLOW = java.util.UUID.fromString("5d3b8a40-7c1e-4f6a-9a77-1f0c2e4b9d11");
   public static final MobEffect FREEZING = Registry.register(BuiltInRegistries.MOB_EFFECT, Backport.id("freezing"), new MobEffect(MobEffectCategory.HARMFUL, 0x9FDFFF) {
      @Override
      public boolean isDurationEffectTick(int duration, int amplifier) {
         return true;
      }

      @Override
      public void applyEffectTick(LivingEntity entity, int amplifier) {
         net.minecraft.world.entity.ai.attributes.AttributeInstance speed = entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
         if (speed != null) {
            speed.removeModifier(FREEZING_SLOW);
         }
         // fire and lava thaw you instantly; leather armor and cold-adapted mobs are immune
         boolean warm = entity.isInLava() || entity.level().getBlockState(entity.blockPosition()).is(net.minecraft.tags.BlockTags.FIRE);
         if (warm) {
            entity.setTicksFrozen(0);
            return;
         }
         if (!entity.canFreeze()) {
            return;
         }
         // vanilla thaws 2 ticks per tick outside powder snow, so +3 nets +1 and fully freezes in 7 seconds
         entity.setTicksFrozen(Math.min(entity.getTicksRequiredToFreeze() + 10, entity.getTicksFrozen() + 3));
         double pct = Math.min(1.0, entity.getTicksFrozen() / (double) entity.getTicksRequiredToFreeze());
         if (speed != null && pct > 0.0) {
            speed.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(FREEZING_SLOW, "Freezing slowdown", -0.5 * pct, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL));
         }
      }

      @Override
      public void removeAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap map, int amplifier) {
         super.removeAttributeModifiers(entity, map, amplifier);
         net.minecraft.world.entity.ai.attributes.AttributeInstance speed = map.getInstance(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
         if (speed != null) {
            speed.removeModifier(FREEZING_SLOW);
         }
      }
   });
   public static final Potion FREEZING_POTION = Registry.register(BuiltInRegistries.POTION, Backport.id("freezing"), new Potion("freezing", new MobEffectInstance(FREEZING, 900)));
   public static final Potion LONG_FREEZING_POTION = Registry.register(BuiltInRegistries.POTION, Backport.id("long_freezing"), new Potion("freezing", new MobEffectInstance(FREEZING, 1800)));

   public static final Block ICE_CRYSTAL = Backport.block("ice_crystal", new AmethystClusterBlock(7, 3, BlockBehaviour.Properties.of().mapColor(MapColor.ICE).forceSolidOn()
      .noOcclusion().sound(SoundType.GLASS).strength(1.5F).lightLevel(s -> 3).pushReaction(PushReaction.DESTROY)));
   public static final Block ICICLE = Backport.block("icicle", new SulfurSpikeBlock(Blocks.PACKED_ICE, BlockBehaviour.Properties.of().mapColor(MapColor.ICE).forceSolidOn()
      .instrument(NoteBlockInstrument.CHIME).noOcclusion().sound(SoundType.GLASS).randomTicks().strength(1.5F, 3.0F).dynamicShape()
      .offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY).isRedstoneConductor((s, l, p) -> false), 5, true));

   public static final Item ICE_BALL = Backport.item("ice_ball", new IceBallItem(new FabricItemSettings().maxCount(16)));
   public static final EntityType<IceBallEntity> ICE_BALL_ENTITY = Registry.register(BuiltInRegistries.ENTITY_TYPE, Backport.id("ice_ball"),
      FabricEntityTypeBuilder.<IceBallEntity>create(MobCategory.MISC, IceBallEntity::new).dimensions(EntityDimensions.fixed(0.25F, 0.25F)).trackRangeBlocks(4).trackedUpdateRate(10).build());
   public static final EntityType<Frostbite> FROSTBITE = Registry.register(BuiltInRegistries.ENTITY_TYPE, Backport.id("frostbite"),
      FabricEntityTypeBuilder.createMob().entityFactory(Frostbite::new).spawnGroup(MobCategory.MONSTER)
         .spawnRestriction(SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules)
         .dimensions(EntityDimensions.fixed(0.6F, 1.95F)).trackRangeBlocks(8).build());
   public static final Item FROSTBITE_SPAWN_EGG = Backport.item("frostbite_spawn_egg", new SpawnEggItem(FROSTBITE, 0x9FD3E8, 0xE8F6FF, new FabricItemSettings()));

   private IceCaves() {
   }

   public static void init() {
      net.minecraft.world.item.alchemy.PotionBrewing.addMix(net.minecraft.world.item.alchemy.Potions.AWKWARD, ICE_BALL, FREEZING_POTION);
      net.minecraft.world.item.alchemy.PotionBrewing.addMix(FREEZING_POTION, net.minecraft.world.item.Items.REDSTONE, LONG_FREEZING_POTION);
      FabricDefaultAttributeRegistry.register(FROSTBITE, Frostbite.createAttributes());
      net.fabricmc.fabric.api.biome.v1.BiomeModifications.addSpawn(net.fabricmc.fabric.api.biome.v1.BiomeSelectors.includeByKey(Biomes.ICE_SPIKES),
         MobCategory.MONSTER, FROSTBITE, 30, 1, 2);
   }
}
