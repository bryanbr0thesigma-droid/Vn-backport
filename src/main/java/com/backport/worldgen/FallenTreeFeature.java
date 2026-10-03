package com.backport.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;

public class FallenTreeFeature extends Feature<FallenTreeFeature.Config> {
   public record Config(BlockStateProvider trunkProvider, IntProvider logLength, List<TreeDecorator> stumpDecorators, List<TreeDecorator> logDecorators) implements FeatureConfiguration {
      public static final Codec<Config> CODEC = RecordCodecBuilder.create(i -> i.group(
         BlockStateProvider.CODEC.fieldOf("trunk_provider").forGetter(Config::trunkProvider),
         IntProvider.codec(0, 16).fieldOf("log_length").forGetter(Config::logLength),
         TreeDecorator.CODEC.listOf().fieldOf("stump_decorators").forGetter(Config::stumpDecorators),
         TreeDecorator.CODEC.listOf().fieldOf("log_decorators").forGetter(Config::logDecorators)
      ).apply(i, Config::new));
   }

   public FallenTreeFeature() {
      super(Config.CODEC);
   }

   public boolean place(FeaturePlaceContext<Config> ctx) {
      WorldGenLevel level = ctx.level();
      RandomSource random = ctx.random();
      Config cfg = ctx.config();
      BlockPos origin = ctx.origin();
      this.placeLog(level, random, cfg, origin.mutable(), Function.identity());
      decorate(level, random, Set.of(origin), cfg.stumpDecorators());
      Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
      int length = cfg.logLength().sample(random) - 2;
      BlockPos.MutableBlockPos start = origin.relative(direction, 2 + random.nextInt(2)).mutable();
      start.move(Direction.UP, 1);
      for (int i = 0; i < 6; i++) {
         if (mayPlaceOn(level, start)) break;
         start.move(Direction.DOWN);
      }
      int gap = 0;
      for (int i = 0; i < length; i++) {
         if (!TreeFeature.validTreePos(level, start)) return true;
         if (!isOverSolidGround(level, start)) {
            if (++gap > 2) return true;
         } else {
            gap = 0;
         }
         start.move(direction);
      }
      start.move(direction.getOpposite(), length);
      Set<BlockPos> log = new HashSet<>();
      for (int i = 0; i < length; i++) {
         log.add(this.placeLog(level, random, cfg, start, s -> s.hasProperty(RotatedPillarBlock.AXIS) ? s.setValue(RotatedPillarBlock.AXIS, direction.getAxis()) : s));
         start.move(direction);
      }
      decorate(level, random, log, cfg.logDecorators());
      return true;
   }

   private static boolean mayPlaceOn(LevelAccessor level, BlockPos pos) {
      return TreeFeature.validTreePos(level, pos) && isOverSolidGround(level, pos);
   }

   private static boolean isOverSolidGround(LevelAccessor level, BlockPos pos) {
      return level.getBlockState(pos.below()).isFaceSturdy(level, pos, Direction.UP);
   }

   private BlockPos placeLog(WorldGenLevel level, RandomSource random, Config cfg, BlockPos.MutableBlockPos pos, Function<BlockState, BlockState> mod) {
      level.setBlock(pos, mod.apply(cfg.trunkProvider().getState(random, pos)), 3);
      this.markAboveForPostProcessing(level, pos);
      return pos.immutable();
   }

   private static void decorate(WorldGenLevel level, RandomSource random, Set<BlockPos> logs, List<TreeDecorator> decorators) {
      if (decorators.isEmpty()) return;
      BiConsumer<BlockPos, BlockState> setter = (p, s) -> level.setBlock(p, s, 19);
      TreeDecorator.Context ctx = new TreeDecorator.Context(level, setter, random, logs, Set.of(), Set.of());
      decorators.forEach(d -> d.place(ctx));
   }
}
