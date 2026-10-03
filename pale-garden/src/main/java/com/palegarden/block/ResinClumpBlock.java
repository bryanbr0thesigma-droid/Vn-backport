package com.palegarden.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

public class ResinClumpBlock extends MultifaceBlock implements SimpleWaterloggedBlock {
   private static final net.minecraft.world.level.block.state.properties.BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private final net.minecraft.world.level.block.MultifaceSpreader spreader = new net.minecraft.world.level.block.MultifaceSpreader(this);

   public ResinClumpBlock(BlockBehaviour.Properties properties) {
      super(properties);
      this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(WATERLOGGED);
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      if (state.getValue(WATERLOGGED)) {
         level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
      }

      return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
   }

   public FluidState getFluidState(BlockState state) {
      return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
   }

   public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
      return state.getFluidState().isEmpty();
   }

   public net.minecraft.world.level.block.MultifaceSpreader getSpreader() {
      return this.spreader;
   }
}
