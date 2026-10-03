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
   }
}
