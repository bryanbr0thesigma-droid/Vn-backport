package com.palegarden.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class BonemealableFeaturePlacerBlock extends Block implements BonemealableBlock {
   private final ResourceKey<ConfiguredFeature<?, ?>> feature;

   public BonemealableFeaturePlacerBlock(ResourceKey<ConfiguredFeature<?, ?>> feature, BlockBehaviour.Properties properties) {
      super(properties);
      this.feature = feature;
   }

   public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
      return level.getBlockState(pos.above()).isAir();
   }

   public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
      return true;
   }

   public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
      level.registryAccess()
         .registryOrThrow(Registries.CONFIGURED_FEATURE)
         .getHolder(this.feature)
         .ifPresent(ref -> ref.value().place(level, level.getChunkSource().getGenerator(), random, pos.above()));
   }
}
