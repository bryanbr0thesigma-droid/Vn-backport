package com.backport.sulfur;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Port of 26.x SpeleothemBlock / SulfurSpikeBlock (a pointed-dripstone clone that grows under sulfur). */
public class SulfurSpikeBlock extends Block implements SimpleWaterloggedBlock, Fallable {
   public static final EnumProperty<Direction> TIP_DIRECTION = BlockStateProperties.VERTICAL_DIRECTION;
   public static final EnumProperty<DripstoneThickness> THICKNESS = BlockStateProperties.DRIPSTONE_THICKNESS;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private static final VoxelShape SHAPE_TIP_MERGE = Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);
   private static final VoxelShape SHAPE_TIP_UP = Block.box(5.0, 0.0, 5.0, 11.0, 11.0, 11.0);
   private static final VoxelShape SHAPE_TIP_DOWN = Block.box(5.0, 5.0, 5.0, 11.0, 16.0, 11.0);
   private static final VoxelShape SHAPE_FRUSTUM = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
   private static final VoxelShape SHAPE_MIDDLE = Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
   private static final VoxelShape SHAPE_BASE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
   private final Block growOn;

   public SulfurSpikeBlock(Block growOn, Properties props) {
      super(props);
      this.growOn = growOn;
      this.registerDefaultState(this.stateDefinition.any().setValue(TIP_DIRECTION, Direction.UP).setValue(THICKNESS, DripstoneThickness.TIP).setValue(WATERLOGGED, false));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
      b.add(TIP_DIRECTION, THICKNESS, WATERLOGGED);
   }

   @Override
   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      return this.isValidPlacement(level, pos, state.getValue(TIP_DIRECTION));
   }

   @Override
   public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      if (state.getValue(WATERLOGGED)) {
         level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
      }
      if (dir != Direction.UP && dir != Direction.DOWN) {
         return state;
      }
      Direction tip = state.getValue(TIP_DIRECTION);
      if (tip == Direction.DOWN && level.getBlockTicks().hasScheduledTick(pos, this)) {
         return state;
      }
      if (dir == tip.getOpposite() && !this.canSurvive(state, level, pos)) {
         level.scheduleTick(pos, this, tip == Direction.DOWN ? 2 : 1);
         return state;
      }
      boolean merge = state.getValue(THICKNESS) == DripstoneThickness.TIP_MERGE;
      return state.setValue(THICKNESS, this.thickness(level, pos, tip, merge));
   }

   @Nullable
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      LevelAccessor level = ctx.getLevel();
      BlockPos pos = ctx.getClickedPos();
      Direction def = ctx.getNearestLookingVerticalDirection().getOpposite();
      Direction tip;
      if (this.isValidPlacement(level, pos, def)) {
         tip = def;
      } else if (this.isValidPlacement(level, pos, def.getOpposite())) {
         tip = def.getOpposite();
      } else {
         return null;
      }
      DripstoneThickness t = this.thickness(level, pos, tip, !ctx.isSecondaryUseActive());
      return this.defaultBlockState().setValue(TIP_DIRECTION, tip).setValue(THICKNESS, t).setValue(WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER);
   }

   private DripstoneThickness thickness(LevelReader level, BlockPos pos, Direction tip, boolean merge) {
      Direction base = tip.getOpposite();
      BlockState front = level.getBlockState(pos.relative(tip));
      if (this.withDirection(front, base)) {
         return !merge && front.getValue(THICKNESS) != DripstoneThickness.TIP_MERGE ? DripstoneThickness.TIP : DripstoneThickness.TIP_MERGE;
      } else if (!this.withDirection(front, tip)) {
         return DripstoneThickness.TIP;
      }
      DripstoneThickness ft = front.getValue(THICKNESS);
      if (ft != DripstoneThickness.TIP && ft != DripstoneThickness.TIP_MERGE) {
         BlockState behind = level.getBlockState(pos.relative(base));
         return !this.withDirection(behind, tip) ? DripstoneThickness.BASE : DripstoneThickness.MIDDLE;
      }
      return DripstoneThickness.FRUSTUM;
   }

   private boolean isValidPlacement(LevelReader level, BlockPos pos, Direction tip) {
      BlockPos behindPos = pos.relative(tip.getOpposite());
      BlockState behind = level.getBlockState(behindPos);
      return behind.isFaceSturdy(level, behindPos, tip) || this.withDirection(behind, tip);
   }

   private boolean withDirection(BlockState s, Direction tip) {
      return s.is(this) && s.getValue(TIP_DIRECTION) == tip;
   }

   @Override
   public void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
      if (!level.isClientSide) {
         BlockPos p = hit.getBlockPos();
         if (projectile instanceof ThrownTrident && projectile.getDeltaMovement().length() > 0.6) {
            level.destroyBlock(p, true);
         }
      }
   }

   @Override
   public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (this.withDirection(state, Direction.UP) && !this.canSurvive(state, level, pos)) {
         level.destroyBlock(pos, true);
      } else {
         this.spawnFalling(state, level, pos);
      }
   }

   private void spawnFalling(BlockState state, ServerLevel level, BlockPos pos) {
      BlockPos.MutableBlockPos fallPos = pos.mutable();
      BlockState fallState = state;
      while (this.withDirection(fallState, Direction.DOWN)) {
         FallingBlockEntity e = FallingBlockEntity.fall(level, fallPos, fallState);
         if (this.isTip(fallState, true)) {
            int size = Math.max(1 + pos.getY() - fallPos.getY(), 6);
            e.setHurtsEntities(1.0F * size, 40);
            break;
         }
         fallPos.move(Direction.DOWN);
         fallState = level.getBlockState(fallPos);
      }
   }

   private boolean isTip(BlockState s, boolean includeMerge) {
      if (!s.is(this)) {
         return false;
      }
      DripstoneThickness t = s.getValue(THICKNESS);
      return t == DripstoneThickness.TIP || includeMerge && t == DripstoneThickness.TIP_MERGE;
   }

   @Override
   public void onBrokenAfterFall(Level level, BlockPos pos, FallingBlockEntity entity) {
      if (!entity.isSilent()) {
         level.levelEvent(1045, pos, 0);
      }
   }

   @Override
   public DamageSource getFallDamageSource(Entity entity) {
      return entity.damageSources().fallingStalactite(entity);
   }

   @Override
   public FluidState getFluidState(BlockState state) {
      return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
   }

   @Override
   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      VoxelShape s = switch (state.getValue(THICKNESS)) {
         case TIP_MERGE -> SHAPE_TIP_MERGE;
         case TIP -> state.getValue(TIP_DIRECTION) == Direction.DOWN ? SHAPE_TIP_DOWN : SHAPE_TIP_UP;
         case FRUSTUM -> SHAPE_FRUSTUM;
         case MIDDLE -> SHAPE_MIDDLE;
         case BASE -> SHAPE_BASE;
      };
      return s.move(state.getOffset(level, pos).x, 0.0, state.getOffset(level, pos).z);
   }

   @Override
   public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
      return false;
   }

   @Override
   public float getMaxHorizontalOffset() {
      return 0.125F;
   }

   @Override
   public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
      return false;
   }

   @Override
   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (random.nextFloat() < 0.011377778F && this.withDirection(state, Direction.DOWN) && !level.getBlockState(pos.above()).is(this)) {
         this.growIfPossible(state, level, pos, random);
      }
   }

   private void growIfPossible(BlockState startState, ServerLevel level, BlockPos startPos, RandomSource random) {
      if (!level.getBlockState(startPos.above()).is(this.growOn)) {
         return;
      }
      BlockPos tipPos = this.findTip(startState, level, startPos, 2, false);
      if (tipPos != null) {
         BlockState tip = level.getBlockState(tipPos);
         if (this.withDirection(tip, Direction.DOWN) && tip.getValue(THICKNESS) == DripstoneThickness.TIP && !tip.getValue(WATERLOGGED) && this.canTipGrow(tip, level, tipPos)) {
            if (random.nextBoolean()) {
               this.grow(level, tipPos, Direction.DOWN);
            } else {
               this.growBelow(level, tipPos);
            }
         }
      }
   }

   @Nullable
   private BlockPos findTip(BlockState state, LevelAccessor level, BlockPos pos, int max, boolean includeMerge) {
      if (this.isTip(state, includeMerge)) {
         return pos;
      }
      Direction dir = state.getValue(TIP_DIRECTION);
      BiPredicate<BlockPos, BlockState> path = (p, s) -> s.is(this) && s.getValue(TIP_DIRECTION) == dir;
      return findVertical(level, pos, dir.getAxisDirection(), path, s -> this.isTip(s, includeMerge), max).orElse(null);
   }

   private static Optional<BlockPos> findVertical(LevelAccessor level, BlockPos pos, Direction.AxisDirection ad, BiPredicate<BlockPos, BlockState> path, Predicate<BlockState> target, int max) {
      Direction d = Direction.get(ad, Direction.Axis.Y);
      BlockPos.MutableBlockPos m = pos.mutable();
      for (int i = 1; i < max; i++) {
         m.move(d);
         BlockState s = level.getBlockState(m);
         if (target.test(s)) {
            return Optional.of(m.immutable());
         }
         if (level.isOutsideBuildHeight(m.getY()) || !path.test(m, s)) {
            return Optional.empty();
         }
      }
      return Optional.empty();
   }

   private boolean canTipGrow(BlockState tip, ServerLevel level, BlockPos tipPos) {
      Direction g = tip.getValue(TIP_DIRECTION);
      BlockState at = level.getBlockState(tipPos.relative(g));
      if (!at.getFluidState().isEmpty()) {
         return false;
      }
      return at.isAir() || this.unmergedTip(at, g.getOpposite());
   }

   private boolean unmergedTip(BlockState s, Direction tip) {
      return this.isTip(s, false) && s.getValue(TIP_DIRECTION) == tip;
   }

   private void grow(ServerLevel level, BlockPos from, Direction to) {
      BlockPos target = from.relative(to);
      BlockState ex = level.getBlockState(target);
      if (this.unmergedTip(ex, to.getOpposite())) {
         BlockPos stalactite;
         BlockPos stalagmite;
         if (ex.getValue(TIP_DIRECTION) == Direction.UP) {
            stalagmite = target;
            stalactite = target.above();
         } else {
            stalactite = target;
            stalagmite = target.below();
         }
         this.create(level, stalactite, Direction.DOWN, DripstoneThickness.TIP_MERGE);
         this.create(level, stalagmite, Direction.UP, DripstoneThickness.TIP_MERGE);
      } else if (ex.isAir() || ex.is(net.minecraft.world.level.block.Blocks.WATER)) {
         this.create(level, target, to, DripstoneThickness.TIP);
      }
   }

   private void create(LevelAccessor level, BlockPos pos, Direction dir, DripstoneThickness t) {
      level.setBlock(pos, this.defaultBlockState().setValue(TIP_DIRECTION, dir).setValue(THICKNESS, t).setValue(WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER), 3);
   }

   private void growBelow(ServerLevel level, BlockPos above) {
      BlockPos.MutableBlockPos pos = above.mutable();
      for (int i = 0; i < 10; i++) {
         pos.move(Direction.DOWN);
         BlockState s = level.getBlockState(pos);
         if (!s.getFluidState().isEmpty()) {
            return;
         }
         if (this.unmergedTip(s, Direction.UP) && this.canTipGrow(s, level, pos)) {
            this.grow(level, pos, Direction.UP);
            return;
         }
         if (this.isValidPlacement(level, pos, Direction.UP) && !level.isWaterAt(pos.below())) {
            this.grow(level, pos.below(), Direction.UP);
            return;
         }
      }
   }
}
