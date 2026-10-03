package com.backport.worldgen;

import com.backport.PlantBlocks;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class ShelfMushroomDecorator extends TreeDecorator {
   public static final Codec<ShelfMushroomDecorator> CODEC = Codec.floatRange(0.0F, 1.0F).fieldOf("probability").xmap(ShelfMushroomDecorator::new, d -> d.probability).codec();
   private final float probability;

   public ShelfMushroomDecorator(float probability) {
      this.probability = probability;
   }

   protected TreeDecoratorType<?> type() {
      return BackportWorldgen.SHELF_MUSHROOM;
   }

   public void place(TreeDecorator.Context context) {
      RandomSource random = context.random();
      if (random.nextFloat() >= this.probability) return;
      List<BlockPos> logs = context.logs();
      if (logs.isEmpty()) return;
      if (logs.get(0).getY() == logs.get(logs.size() - 1).getY()) fallen(context, logs, random);
      else standing(context, logs, random);
   }

   private static void standing(TreeDecorator.Context ctx, List<BlockPos> logs, RandomSource random) {
      Direction first = Direction.Plane.HORIZONTAL.getRandomDirection(random);
      Direction[] dirs = {first, first.getClockWise()};
      int baseY = logs.get(0).getY();
      for (BlockPos log : logs) {
         int dy = log.getY() - baseY;
         if (dy < 1 || dy > 4) continue;
         for (Direction d : dirs) {
            if (random.nextFloat() > 0.25F) continue;
            BlockPos mp = log.offset(d.getStepX(), 0, d.getStepZ());
            if (replaceable(ctx, mp) && !has(ctx, mp.below())) {
               place(ctx, mp, d, random);
               break;
            }
         }
      }
   }

   private static void fallen(TreeDecorator.Context ctx, List<BlockPos> logs, RandomSource random) {
      BlockPos first = logs.get(0);
      BlockPos last = logs.get(logs.size() - 1);
      Direction[] dirs = first.getX() != last.getX() ? new Direction[]{Direction.NORTH, Direction.SOUTH} : new Direction[]{Direction.EAST, Direction.WEST};
      for (BlockPos log : logs) {
         for (Direction d : dirs) {
            if (random.nextFloat() > 0.25F) continue;
            BlockPos mp = log.offset(d.getStepX(), 0, d.getStepZ());
            if (replaceable(ctx, mp) && !adjacent(ctx, mp) && !adjacent(ctx, log)) place(ctx, mp, d, random);
         }
      }
   }

   private static void place(TreeDecorator.Context ctx, BlockPos pos, Direction facing, RandomSource random) {
      BlockState s = PlantBlocks.SHELF_MUSHROOM.defaultBlockState().setValue(PlantBlocks.ShelfMushroom.AGE, random.nextInt(2)).setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, facing);
      ctx.setBlock(pos, s);
   }

   private static boolean has(TreeDecorator.Context ctx, BlockPos pos) {
      return ctx.level().isStateAtPosition(pos, s -> s.is(PlantBlocks.SHELF_MUSHROOM));
   }

   private static boolean adjacent(TreeDecorator.Context ctx, BlockPos pos) {
      for (Direction d : Direction.Plane.HORIZONTAL) if (has(ctx, pos.relative(d))) return true;
      return false;
   }

   private static boolean replaceable(TreeDecorator.Context ctx, BlockPos pos) {
      return ctx.isAir(pos) && !ctx.level().isFluidAtPosition(pos, f -> f.is(net.minecraft.tags.FluidTags.WATER)) && !ctx.level().isFluidAtPosition(pos.above(), f -> f.is(net.minecraft.tags.FluidTags.WATER));
   }
}
