package com.backport;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DriedGhastBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {
   public static final IntegerProperty HYDRATION = IntegerProperty.create("hydration", 0, 3);
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 10.0, 13.0);

   public DriedGhastBlock(Properties properties) {
      super(properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HYDRATION, 0).setValue(WATERLOGGED, false));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(FACING, HYDRATION, WATERLOGGED);
   }

   public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      if (state.getValue(WATERLOGGED)) level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
      return super.updateShape(state, dir, neighbor, level, pos, neighborPos);
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return SHAPE;
   }

   public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      int h = state.getValue(HYDRATION);
      if (state.getValue(WATERLOGGED)) {
         if (h != 3) {
            level.playSound(null, pos, BackportSounds.BLOCK_DRIED_GHAST_TRANSITION, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.setBlock(pos, state.setValue(HYDRATION, h + 1), 2);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
         } else {
            this.spawnGhastling(level, pos, state);
         }
      } else if (h > 0) {
         level.setBlock(pos, state.setValue(HYDRATION, h - 1), 2);
         level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
      }
   }

   private void spawnGhastling(ServerLevel level, BlockPos pos, BlockState state) {
      level.removeBlock(pos, false);
      com.backport.entity.HappyGhast g = BackportEntities.HAPPY_GHAST.create(level);
      if (g != null) {
         Vec3 at = Vec3.atBottomCenterOf(pos);
         g.setBaby(true);
         float rot = state.getValue(FACING).toYRot();
         g.setYHeadRot(rot);
         g.moveTo(at.x, at.y, at.z, rot, 0.0F);
         level.addFreshEntity(g);
         level.playSound(null, g, BackportSounds.ENTITY_GHASTLING_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);
      }
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource r) {
      double x = pos.getX() + 0.5, y = pos.getY() + 0.5, z = pos.getZ() + 0.5;
      if (!state.getValue(WATERLOGGED)) {
         if (r.nextInt(40) == 0 && level.getBlockState(pos.below()).is(BlockTags.SOUL_FIRE_BASE_BLOCKS)) {
            level.playLocalSound(x, y, z, BackportSounds.BLOCK_DRIED_GHAST_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F, false);
         }
         if (r.nextInt(6) == 0) level.addParticle(ParticleTypes.WHITE_ASH, x, y, z, 0.0, 0.02, 0.0);
      } else {
         if (r.nextInt(40) == 0) level.playLocalSound(x, y, z, BackportSounds.BLOCK_DRIED_GHAST_AMBIENT_WATER, SoundSource.BLOCKS, 1.0F, 1.0F, false);
         if (r.nextInt(6) == 0) level.addParticle(ParticleTypes.HAPPY_VILLAGER, x + (r.nextFloat() * 2.0F - 1.0F) / 3.0F, y + 0.4, z + (r.nextFloat() * 2.0F - 1.0F) / 3.0F, 0.0, r.nextFloat(), 0.0);
      }
   }

   public boolean isRandomlyTicking(BlockState state) {
      return true;
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if ((state.getValue(WATERLOGGED) || state.getValue(HYDRATION) > 0) && !level.getBlockTicks().hasScheduledTick(pos, this)) {
         level.scheduleTick(pos, this, 5000);
      }
   }

   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      boolean water = ctx.getLevel().getFluidState(ctx.getClickedPos()).is(Fluids.WATER);
      return this.defaultBlockState().setValue(WATERLOGGED, water).setValue(FACING, ctx.getHorizontalDirection().getOpposite());
   }

   public FluidState getFluidState(BlockState state) {
      return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
   }

   public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluid) {
      if (!state.getValue(WATERLOGGED) && fluid.getType() == Fluids.WATER) {
         if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(WATERLOGGED, true), 3);
            level.scheduleTick(pos, fluid.getType(), fluid.getType().getTickDelay(level));
            level.playSound(null, pos, BackportSounds.BLOCK_DRIED_GHAST_PLACE_IN_WATER, SoundSource.BLOCKS, 1.0F, 1.0F);
         }
         return true;
      }
      return false;
   }

   public boolean canPlaceLiquid(BlockGetter level, BlockPos pos, BlockState state, net.minecraft.world.level.material.Fluid fluid) {
      return !state.getValue(WATERLOGGED) && fluid == Fluids.WATER;
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity by, ItemStack stack) {
      super.setPlacedBy(level, pos, state, by, stack);
      level.playSound(null, pos, state.getValue(WATERLOGGED) ? BackportSounds.BLOCK_DRIED_GHAST_PLACE_IN_WATER : BackportSounds.BLOCK_DRIED_GHAST_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
   }

   public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
      return false;
   }
}
