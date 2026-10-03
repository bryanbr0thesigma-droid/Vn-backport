package com.palegarden.block;

import com.palegarden.PaleBlocks;
import com.palegarden.PaleSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HangingMossBlock extends Block implements BonemealableBlock {
   private static final VoxelShape TIP_SHAPE = Block.box(1.0, 2.0, 1.0, 15.0, 16.0, 15.0);
   private static final VoxelShape BASE_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);
   public static final BooleanProperty TIP = BooleanProperty.create("tip");

   public HangingMossBlock(BlockBehaviour.Properties properties) {
      super(properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(TIP, true));
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return state.getValue(TIP) ? TIP_SHAPE : BASE_SHAPE;
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if (random.nextInt(500) == 0) {
         BlockState above = level.getBlockState(pos.above());
         if (above.is(PaleBlocks.PALE_OAK_LOGS) || above.is(PaleBlocks.PALE_OAK_LEAVES)) {
            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), PaleSounds.BLOCK_PALE_HANGING_MOSS_IDLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
         }
      }
   }

   public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
      return true;
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      return this.canStayAtPosition(level, pos);
   }

   private boolean canStayAtPosition(BlockGetter level, BlockPos pos) {
      BlockPos abovePos = pos.relative(Direction.UP);
      BlockState above = level.getBlockState(abovePos);
      return MultifaceBlock.canAttachTo(level, Direction.UP, abovePos, above) || above.is(PaleBlocks.PALE_HANGING_MOSS);
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      if (!this.canStayAtPosition(level, pos)) {
         level.scheduleTick(pos, this, 1);
      }

      return state.setValue(TIP, !level.getBlockState(pos.below()).is(this));
   }

   public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (!this.canStayAtPosition(level, pos)) {
         level.destroyBlock(pos, true);
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(TIP);
   }

   public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
      return level.getBlockState(this.getTip(level, pos).below()).isAir();
   }

   public BlockPos getTip(BlockGetter level, BlockPos pos) {
      BlockPos.MutableBlockPos cursor = pos.mutable();

      BlockState state;
      do {
         cursor.move(Direction.DOWN);
         state = level.getBlockState(cursor);
      } while (state.is(this));

      return cursor.relative(Direction.UP).immutable();
   }

   public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
      return true;
   }

   public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
      BlockPos target = this.getTip(level, pos).below();
      if (level.getBlockState(target).isAir()) {
         level.setBlockAndUpdate(target, state.setValue(TIP, true));
      }
   }
}
