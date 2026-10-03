package com.palegarden.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.palegarden.PaleBlocks;
import com.palegarden.PaleGarden;
import com.palegarden.PaleWorldgen;
import com.palegarden.block.HangingMossBlock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class PaleMossDecorator extends TreeDecorator {
   public static final Codec<PaleMossDecorator> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.floatRange(0.0F, 1.0F).fieldOf("leaves_probability").forGetter(d -> d.leavesProbability),
            Codec.floatRange(0.0F, 1.0F).fieldOf("trunk_probability").forGetter(d -> d.trunkProbability),
            Codec.floatRange(0.0F, 1.0F).fieldOf("ground_probability").forGetter(d -> d.groundProbability)
         )
         .apply(instance, PaleMossDecorator::new)
   );
   private static final ResourceKey<ConfiguredFeature<?, ?>> PATCH = ResourceKey.create(Registries.CONFIGURED_FEATURE, PaleGarden.id("pale_moss_patch"));
   private final float leavesProbability;
   private final float trunkProbability;
   private final float groundProbability;

   public PaleMossDecorator(float leavesProbability, float trunkProbability, float groundProbability) {
      this.leavesProbability = leavesProbability;
      this.trunkProbability = trunkProbability;
      this.groundProbability = groundProbability;
   }

   protected TreeDecoratorType<?> type() {
      return PaleWorldgen.PALE_MOSS;
   }

   public void place(TreeDecorator.Context context) {
      RandomSource random = context.random();
      if (!(context.level() instanceof WorldGenLevel worldGenLevel)) {
         return;
      }

      List<BlockPos> logs = new ArrayList<>(context.logs());
      Collections.shuffle(logs, new java.util.Random(random.nextLong()));
      if (!logs.isEmpty()) {
         BlockPos lowest = logs.get(0);
         for (BlockPos log : logs) {
            if (log.getY() < lowest.getY()) {
               lowest = log;
            }
         }

         if (random.nextFloat() < this.groundProbability) {
            BlockPos origin = lowest.above();
            worldGenLevel.registryAccess()
               .registryOrThrow(Registries.CONFIGURED_FEATURE)
               .getHolder(PATCH)
               .ifPresent(ref -> ref.value().place(worldGenLevel, worldGenLevel.getLevel().getChunkSource().getGenerator(), random, origin));
         }

         context.logs().forEach(log -> {
            if (random.nextFloat() < this.trunkProbability) {
               BlockPos below = log.below();
               if (context.isAir(below)) {
                  addMossHanger(below, context);
               }
            }
         });
         context.leaves().forEach(leaf -> {
            if (random.nextFloat() < this.leavesProbability) {
               BlockPos below = leaf.below();
               if (context.isAir(below)) {
                  addMossHanger(below, context);
               }
            }
         });
      }
   }

   private static void addMossHanger(BlockPos pos, TreeDecorator.Context context) {
      while (context.isAir(pos.below()) && !(context.random().nextFloat() < 0.5)) {
         context.setBlock(pos, PaleBlocks.PALE_HANGING_MOSS.defaultBlockState().setValue(HangingMossBlock.TIP, false));
         pos = pos.below();
      }

      context.setBlock(pos, PaleBlocks.PALE_HANGING_MOSS.defaultBlockState().setValue(HangingMossBlock.TIP, true));
   }
}
