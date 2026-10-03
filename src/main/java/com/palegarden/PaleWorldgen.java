package com.palegarden;

import com.mojang.serialization.Codec;
import com.palegarden.worldgen.CreakingHeartDecorator;
import com.palegarden.worldgen.PaleMossDecorator;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public final class PaleWorldgen {
   public static final TreeDecoratorType<PaleMossDecorator> PALE_MOSS = Registry.register(
      BuiltInRegistries.TREE_DECORATOR_TYPE, PaleGarden.id("pale_moss"), new TreeDecoratorType<>(PaleMossDecorator.CODEC)
   );
   public static final TreeDecoratorType<CreakingHeartDecorator> CREAKING_HEART = Registry.register(
      BuiltInRegistries.TREE_DECORATOR_TYPE, PaleGarden.id("creaking_heart"), new TreeDecoratorType<>(CreakingHeartDecorator.CODEC)
   );

   private PaleWorldgen() {
   }

   public static void init() {
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
         if (entity instanceof net.minecraft.world.entity.animal.Wolf wolf && com.palegarden.WolfVariants.of(wolf) < 0) {
            ((com.palegarden.WolfVariants.Holder2)wolf).palegarden$setVariant(com.palegarden.WolfVariants.forBiome(level.getBiome(wolf.blockPosition())));
         }
      });
      net.fabricmc.fabric.api.biome.v1.BiomeModifications.addSpawn(
         net.fabricmc.fabric.api.biome.v1.BiomeSelectors.includeByKey(
            net.minecraft.world.level.biome.Biomes.WOODED_BADLANDS, net.minecraft.world.level.biome.Biomes.SAVANNA_PLATEAU,
            net.minecraft.world.level.biome.Biomes.SPARSE_JUNGLE, net.minecraft.world.level.biome.Biomes.JUNGLE),
         net.minecraft.world.entity.MobCategory.CREATURE, net.minecraft.world.entity.EntityType.WOLF, 8, 4, 4);
   }
}
