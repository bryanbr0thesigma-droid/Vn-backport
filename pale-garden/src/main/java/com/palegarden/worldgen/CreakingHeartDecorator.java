package com.palegarden.worldgen;

import com.mojang.serialization.Codec;
import com.palegarden.PaleBlocks;
import com.palegarden.PaleWorldgen;
import com.palegarden.block.CreakingHeartBlock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class CreakingHeartDecorator extends TreeDecorator {
   public static final Codec<CreakingHeartDecorator> CODEC = Codec.floatRange(0.0F, 1.0F)
      .fieldOf("probability")
      .xmap(CreakingHeartDecorator::new, d -> d.probability)
      .codec();
   private final float probability;

   public CreakingHeartDecorator(float probability) {
      this.probability = probability;
   }

   protected TreeDecoratorType<?> type() {
      return PaleWorldgen.CREAKING_HEART;
   }

   public void place(TreeDecorator.Context context) {
      RandomSource random = context.random();
      List<BlockPos> logs = context.logs();
      if (!logs.isEmpty() && !(random.nextFloat() >= this.probability)) {
         List<BlockPos> shuffled = new ArrayList<>(logs);
         Collections.shuffle(shuffled, new java.util.Random(random.nextLong()));
         Optional<BlockPos> target = shuffled.stream().filter(pos -> {
            for (Direction direction : Direction.values()) {
               if (!context.level().isStateAtPosition(pos.relative(direction), state -> state.is(BlockTags.LOGS))) {
                  return false;
               }
            }

            return true;
         }).findFirst();
         target.ifPresent(pos -> context.setBlock(pos, PaleBlocks.CREAKING_HEART.defaultBlockState().setValue(CreakingHeartBlock.ACTIVE, true).setValue(CreakingHeartBlock.NATURAL, true)));
      }
   }
}
