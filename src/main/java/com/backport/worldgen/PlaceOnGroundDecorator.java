package com.backport.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class PlaceOnGroundDecorator extends TreeDecorator {
   public static final Codec<PlaceOnGroundDecorator> CODEC = RecordCodecBuilder.create(i -> i.group(
      ExtraCodecs.POSITIVE_INT.optionalFieldOf("tries", 128).forGetter(p -> p.tries),
      ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("radius", 2).forGetter(p -> p.radius),
      ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("height", 1).forGetter(p -> p.height),
      BlockStateProvider.CODEC.fieldOf("block_state_provider").forGetter(p -> p.provider)
   ).apply(i, PlaceOnGroundDecorator::new));
   private final int tries;
   private final int radius;
   private final int height;
   private final BlockStateProvider provider;

   public PlaceOnGroundDecorator(int tries, int radius, int height, BlockStateProvider provider) {
      this.tries = tries;
      this.radius = radius;
      this.height = height;
      this.provider = provider;
   }

   protected TreeDecoratorType<?> type() {
      return BackportWorldgen.PLACE_ON_GROUND;
   }

   public void place(TreeDecorator.Context context) {
      List<BlockPos> logs = context.logs();
      if (logs.isEmpty()) return;
      int minY = Integer.MAX_VALUE;
      for (BlockPos p : logs) minY = Math.min(minY, p.getY());
      int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
      for (BlockPos p : logs) {
         if (p.getY() == minY) {
            minX = Math.min(minX, p.getX());
            maxX = Math.max(maxX, p.getX());
            minZ = Math.min(minZ, p.getZ());
            maxZ = Math.max(maxZ, p.getZ());
         }
      }
      RandomSource random = context.random();
      BoundingBox bb = new BoundingBox(minX, minY, minZ, maxX, minY, maxZ).inflatedBy(this.radius).moved(0, 0, 0);
      int y0 = minY - this.height;
      int y1 = minY + this.height;
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
      for (int i = 0; i < this.tries; i++) {
         pos.set(random.nextIntBetweenInclusive(bb.minX(), bb.maxX()), random.nextIntBetweenInclusive(y0, y1), random.nextIntBetweenInclusive(bb.minZ(), bb.maxZ()));
         BlockPos above = pos.above();
         if (context.level().isStateAtPosition(above, s -> s.isAir() || s.is(Blocks.VINE))
            && context.level().isStateAtPosition(pos, s -> s.isSolidRender(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, BlockPos.ZERO))
            && (!(context.level() instanceof LevelReader lr) || lr.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos).getY() <= above.getY())) {
            context.setBlock(above, this.provider.getState(random, above));
         }
      }
   }
}
