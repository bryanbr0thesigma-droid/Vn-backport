package com.backport.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class PoplarTrunkPlacer extends TrunkPlacer {
   public static final Codec<PoplarTrunkPlacer> CODEC = RecordCodecBuilder.create(i -> trunkPlacerParts(i).and(i.group(
      IntProvider.codec(0, 8).fieldOf("trunk_height_above_branches").forGetter(t -> t.trunkHeightAboveBranches),
      IntProvider.codec(1, 4).fieldOf("branch_amount").forGetter(t -> t.branchAmount)
   )).apply(i, PoplarTrunkPlacer::new));
   private final IntProvider trunkHeightAboveBranches;
   private final IntProvider branchAmount;

   public PoplarTrunkPlacer(int baseHeight, int randA, int randB, IntProvider above, IntProvider branches) {
      super(baseHeight, randA, randB);
      this.trunkHeightAboveBranches = above;
      this.branchAmount = branches;
   }

   protected TrunkPlacerType<?> type() {
      return BackportWorldgen.POPLAR_TRUNK;
   }

   public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> setter, RandomSource random, int height, BlockPos origin, TreeConfiguration cfg) {
      setDirtAt(level, setter, random, origin.below(), cfg);
      int upToBranches = height - this.trunkHeightAboveBranches.sample(random);
      for (int y = 0; y < height; y++) {
         this.placeLog(level, setter, random, origin.above(y), cfg);
         List<Direction> dirs = shuffledBranchDirections(random);
         if (upToBranches - 1 == y) {
            int branches = this.branchAmount.sample(random);
            for (int x = 0; x < branches; x++) {
               Direction d = dirs.get(x);
               this.placeLog(level, setter, random, origin.above(y).relative(d, 1), cfg, sideways(d));
            }
         }
      }
      return List.of(new FoliagePlacer.FoliageAttachment(origin.above(upToBranches), 0, false));
   }

   private static Function<BlockState, BlockState> sideways(Direction d) {
      return s -> s.hasProperty(RotatedPillarBlock.AXIS) ? s.setValue(RotatedPillarBlock.AXIS, d.getAxis()) : s;
   }

   private static List<Direction> shuffledBranchDirections(RandomSource random) {
      return Direction.Plane.HORIZONTAL.stream().sorted((a, b) -> 0).collect(Collectors.collectingAndThen(Collectors.toList(), l -> {
         java.util.Collections.shuffle(l, new java.util.Random(random.nextLong()));
         return l;
      }));
   }
}
