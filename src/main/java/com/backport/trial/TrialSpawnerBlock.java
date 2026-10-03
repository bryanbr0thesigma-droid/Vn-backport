package com.backport.trial;

import org.jetbrains.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class TrialSpawnerBlock extends BaseEntityBlock {
   public static final EnumProperty<TrialSpawnerState> STATE = EnumProperty.create("trial_spawner_state", TrialSpawnerState.class);
   public static final BooleanProperty OMINOUS = BooleanProperty.create("ominous");

   public TrialSpawnerBlock(Properties properties) {
      super(properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(STATE, TrialSpawnerState.INACTIVE).setValue(OMINOUS, false));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(STATE, OMINOUS);
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TrialSpawnerBlockEntity(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      if (level instanceof ServerLevel sl) {
         return createTickerHelper(type, TrialSpawnerBlockEntity.TYPE, (l, pos, s, be) -> be.tickServer(sl, pos, s.getValue(OMINOUS)));
      }
      return createTickerHelper(type, TrialSpawnerBlockEntity.TYPE, (l, pos, s, be) -> be.tickClient(l, pos, s.getValue(OMINOUS)));
   }
}
