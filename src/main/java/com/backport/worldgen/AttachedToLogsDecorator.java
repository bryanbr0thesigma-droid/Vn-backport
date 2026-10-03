package com.backport.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class AttachedToLogsDecorator extends TreeDecorator {
   public static final Codec<AttachedToLogsDecorator> CODEC = RecordCodecBuilder.create(i -> i.group(
      Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(d -> d.probability),
      BlockStateProvider.CODEC.fieldOf("block_provider").forGetter(d -> d.provider),
      Direction.CODEC.listOf().fieldOf("directions").forGetter(d -> d.directions)
   ).apply(i, AttachedToLogsDecorator::new));
   private final float probability;
   private final BlockStateProvider provider;
   private final List<Direction> directions;

   public AttachedToLogsDecorator(float probability, BlockStateProvider provider, List<Direction> directions) {
      this.probability = probability;
      this.provider = provider;
      this.directions = directions;
   }

   protected TreeDecoratorType<?> type() {
      return BackportWorldgen.ATTACHED_TO_LOGS;
   }

   public void place(TreeDecorator.Context context) {
      RandomSource random = context.random();
      for (BlockPos log : context.logs()) {
         Direction d = this.directions.get(random.nextInt(this.directions.size()));
         BlockPos p = log.relative(d);
         if (random.nextFloat() <= this.probability && context.isAir(p)) {
            context.setBlock(p, this.provider.getState(random, p));
         }
      }
   }
}
