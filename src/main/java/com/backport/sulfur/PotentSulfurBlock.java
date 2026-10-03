package com.backport.sulfur;

import org.jetbrains.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class PotentSulfurBlock extends BaseEntityBlock {
   public static final EnumProperty<State> STATE = EnumProperty.create("potent_sulfur_state", State.class);

   public enum State implements StringRepresentable {
      DRY("dry"), WET("wet"), DORMANT("dormant"), ERUPTING("erupting"), CONTINUOUS("continuous");

      private final String name;

      State(String n) {
         this.name = n;
      }

      @Override
      public String getSerializedName() {
         return this.name;
      }
   }

   public PotentSulfurBlock(Properties props) {
      super(props);
      this.registerDefaultState(this.stateDefinition.any().setValue(STATE, State.DRY));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> b) {
      b.add(STATE);
   }

   @Override
   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new PotentSulfurBlockEntity(pos, state);
   }

   @Override
   public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      return valid(state, level, pos);
   }

   @Nullable
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      return valid(this.defaultBlockState(), ctx.getLevel(), ctx.getClickedPos());
   }

   private static BlockState valid(BlockState state, LevelReader level, BlockPos pos) {
      if (level.getFluidState(pos.above()).getType() != Fluids.WATER) {
         return state.setValue(STATE, State.DRY);
      }
      BlockState below = level.getBlockState(pos.below());
      if (below.is(Blocks.LAVA) || below.getFluidState().is(net.minecraft.tags.FluidTags.LAVA) && below.getFluidState().isSource()) {
         return state.setValue(STATE, State.CONTINUOUS);
      }
      if (below.is(Blocks.MAGMA_BLOCK)) {
         boolean geyser = state.getValue(STATE) == State.ERUPTING || state.getValue(STATE) == State.DORMANT;
         if (!geyser && level.getBlockEntity(pos) instanceof PotentSulfurBlockEntity be) {
            be.waitingCountdown = -1;
         }
         return state.getValue(STATE) == State.ERUPTING ? state : state.setValue(STATE, State.DORMANT);
      }
      return state.setValue(STATE, State.WET);
   }

   @Override
   public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
      super.onPlace(state, level, pos, old, moved);
      State s = state.getValue(STATE);
      if (s == State.ERUPTING || s == State.CONTINUOUS) {
         level.blockEvent(pos, this, 0, 0);
         level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 0.5F, 1.5F);
         level.gameEvent(GameEvent.BLOCK_ACTIVATE, pos, GameEvent.Context.of(state));
      }
   }

   @Override
   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if (state.getValue(STATE) != State.DRY && level.getFluidState(pos.above()).getType() == Fluids.WATER) {
         for (int i = 0; i < 2; i++) {
            level.addAlwaysVisibleParticle(ParticleTypes.BUBBLE, pos.getX() + random.nextFloat(), pos.getY() + 1 + random.nextFloat(), pos.getZ() + random.nextFloat(), 0, 0.05, 0);
         }
      }
   }

   @Override
   public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int a, int b) {
      if (level.getBlockEntity(pos) instanceof PotentSulfurBlockEntity be) {
         be.eruptionTick = level.getGameTime();
      }
      return true;
   }

   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      if (type != PotentSulfurBlockEntity.TYPE) {
         return null;
      }
      return (BlockEntityTicker<T>) (BlockEntityTicker<PotentSulfurBlockEntity>) PotentSulfurBlockEntity::tick;
   }
}
