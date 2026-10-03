package com.palegarden.block;

import com.palegarden.PaleBlocks;
import com.palegarden.PaleSounds;
import com.palegarden.entity.CreakingHeartBlockEntity;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class CreakingHeartBlock extends BaseEntityBlock {
   public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
   public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
   public static final BooleanProperty NATURAL = BooleanProperty.create("natural");

   public CreakingHeartBlock(BlockBehaviour.Properties properties) {
      super(properties);
      this.registerDefaultState(this.defaultBlockState().setValue(AXIS, Direction.Axis.Y).setValue(ACTIVE, false).setValue(NATURAL, false));
   }

   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new CreakingHeartBlockEntity(pos, state);
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      if (level.isClientSide) {
         return null;
      } else {
         return state.getValue(ACTIVE) ? createTickerHelper(type, CreakingHeartBlockEntity.TYPE, CreakingHeartBlockEntity::serverTick) : null;
      }
   }

   public static boolean isNaturalNight(Level level) {
      return level.dimensionType().natural() && level.isNight();
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if (isNaturalNight(level) && state.getValue(ACTIVE) && random.nextInt(16) == 0 && isSurroundedByLogs(level, pos)) {
         level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), PaleSounds.BLOCK_CREAKING_HEART_IDLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
      }
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      BlockState updated = super.updateShape(state, direction, neighbor, level, pos, neighborPos);
      return updateState(updated, level, pos);
   }

   private static BlockState updateState(BlockState state, LevelReader level, BlockPos pos) {
      boolean hasLogs = hasRequiredLogs(state, level, pos);
      boolean inactive = !state.getValue(ACTIVE);
      return hasLogs && inactive ? state.setValue(ACTIVE, true) : state;
   }

   public static boolean hasRequiredLogs(BlockState state, LevelReader level, BlockPos pos) {
      Direction.Axis axis = state.getValue(AXIS);

      for (Direction direction : new Direction[]{Direction.get(Direction.AxisDirection.POSITIVE, axis), Direction.get(Direction.AxisDirection.NEGATIVE, axis)}) {
         BlockState other = level.getBlockState(pos.relative(direction));
         if (!other.is(PaleBlocks.PALE_OAK_LOGS) || other.getValue(AXIS) != axis) {
            return false;
         }
      }

      return true;
   }

   private static boolean isSurroundedByLogs(LevelAccessor level, BlockPos pos) {
      for (Direction direction : Direction.values()) {
         if (!level.getBlockState(pos.relative(direction)).is(PaleBlocks.PALE_OAK_LOGS)) {
            return false;
         }
      }

      return true;
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return updateState(this.defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis()), context.getLevel(), context.getClickedPos());
   }

   public BlockState rotate(BlockState state, Rotation rotation) {
      return RotatedPillarBlock.rotatePillar(state, rotation);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(AXIS, ACTIVE, NATURAL);
   }

   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof CreakingHeartBlockEntity heart) {
         heart.removeProtector(null);
      }

      super.onRemove(state, level, pos, newState, moved);
   }

   public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
      if (level.getBlockEntity(pos) instanceof CreakingHeartBlockEntity heart) {
         heart.removeProtector(explosion.getDamageSource());
         if (explosion.getIndirectSourceEntity() instanceof Player player) {
            this.tryAwardExperience(player, level.getBlockState(pos), level, pos);
         }
      }

      super.wasExploded(level, pos, explosion);
   }

   public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
      if (level.getBlockEntity(pos) instanceof CreakingHeartBlockEntity heart) {
         heart.removeProtector(player.damageSources().playerAttack(player));
         this.tryAwardExperience(player, state, level, pos);
      }

      super.playerWillDestroy(level, pos, state, player);
   }

   private void tryAwardExperience(Player player, BlockState state, Level level, BlockPos pos) {
      if (!player.isCreative() && !player.isSpectator() && state.getValue(NATURAL) && level instanceof ServerLevel serverLevel) {
         this.popExperience(serverLevel, pos, level.random.nextIntBetweenInclusive(20, 24));
      }
   }

   public boolean hasAnalogOutputSignal(BlockState state) {
      return true;
   }

   public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
      if (!state.getValue(ACTIVE)) {
         return 0;
      } else {
         return level.getBlockEntity(pos) instanceof CreakingHeartBlockEntity heart ? heart.getAnalogOutputSignal() : 0;
      }
   }
}
