package com.backport;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class CopperBulbBlock extends Block {
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final BooleanProperty LIT = BlockStateProperties.LIT;

   public CopperBulbBlock(BlockBehaviour.Properties properties) {
      super(properties);
      this.registerDefaultState(this.defaultBlockState().setValue(LIT, false).setValue(POWERED, false));
   }

   public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
      if (oldState.getBlock() != state.getBlock() && level instanceof ServerLevel serverLevel) {
         this.checkAndFlip(state, serverLevel, pos);
      }
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean movedByPiston) {
      if (level instanceof ServerLevel serverLevel) {
         this.checkAndFlip(state, serverLevel, pos);
      }
   }

   public void checkAndFlip(BlockState state, ServerLevel level, BlockPos pos) {
      boolean signal = level.hasNeighborSignal(pos);
      if (signal != state.getValue(POWERED)) {
         BlockState newState = state;
         if (!state.getValue(POWERED)) {
            newState = state.cycle(LIT);
            level.playSound(null, pos, newState.getValue(LIT) ? SoundEvents.COPPER_PLACE : SoundEvents.COPPER_HIT, SoundSource.BLOCKS, 1.0F, 1.0F);
         }

         level.setBlock(pos, newState.setValue(POWERED, signal), 3);
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(LIT, POWERED);
   }

   public boolean hasAnalogOutputSignal(BlockState state) {
      return true;
   }

   public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
      return level.getBlockState(pos).getValue(LIT) ? 15 : 0;
   }
}
