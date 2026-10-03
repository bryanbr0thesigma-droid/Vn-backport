package com.palegarden.block;

import com.palegarden.PaleGarden;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class PaleOakTreeGrower extends AbstractMegaTreeGrower {
   private static final ResourceKey<ConfiguredFeature<?, ?>> MEGA = ResourceKey.create(Registries.CONFIGURED_FEATURE, PaleGarden.id("pale_oak_bonemeal"));

   @Nullable
   protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean flowers) {
      return null;
   }

   protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredMegaFeature(RandomSource random) {
      return MEGA;
   }
}
