package com.backport.worldgen;

import com.backport.Backport;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/** 26.3 coral rework: block-specific coral trees/claws, overlay features, cuboid/random_chance placement, tag-random block provider. */
public final class CoralFeatures {
   public record One(Holder<PlacedFeature> feature) implements FeatureConfiguration {
      public static final Codec<One> CODEC = RecordCodecBuilder.create(i -> i.group(PlacedFeature.CODEC.fieldOf("feature").forGetter(One::feature)).apply(i, One::new));
   }

   public record Many(List<Holder<PlacedFeature>> features) implements FeatureConfiguration {
      public static final Codec<Many> CODEC = RecordCodecBuilder.create(i -> i.group(PlacedFeature.CODEC.listOf().fieldOf("features").forGetter(Many::features)).apply(i, Many::new));
   }

   public static final Feature<Many> OVERLAY = Registry.register(BuiltInRegistries.FEATURE, Backport.id("overlay"), new Feature<Many>(Many.CODEC) {
      @Override
      public boolean place(FeaturePlaceContext<Many> ctx) {
         boolean any = false;
         for (Holder<PlacedFeature> f : ctx.config().features()) {
            any |= f.value().place(ctx.level(), ctx.chunkGenerator(), ctx.random(), ctx.origin());
         }
         return any;
      }
   });

   public static final Feature<One> CORAL_TREE = Registry.register(BuiltInRegistries.FEATURE, Backport.id("coral_tree"), new Feature<One>(One.CODEC) {
      @Override
      public boolean place(FeaturePlaceContext<One> ctx) {
         WorldGenLevel level = ctx.level();
         ChunkGenerator gen = ctx.chunkGenerator();
         RandomSource random = ctx.random();
         PlacedFeature feature = ctx.config().feature().value();
         BlockPos.MutableBlockPos pos = ctx.origin().mutable();
         int trunk = random.nextInt(3) + 1;
         for (int i = 0; i < trunk; i++) {
            if (!feature.place(level, gen, random, pos)) {
               return true;
            }
            pos.move(Direction.UP);
         }
         BlockPos top = pos.immutable();
         int branches = random.nextInt(3) + 2;
         List<Direction> dirs = new ArrayList<>(Direction.Plane.HORIZONTAL.stream().toList());
         java.util.Collections.shuffle(dirs, new java.util.Random(random.nextLong()));
         for (Direction d : dirs.subList(0, branches)) {
            pos.set(top);
            pos.move(d);
            int height = random.nextInt(5) + 2;
            int segment = 0;
            for (int j = 0; j < height && feature.place(level, gen, random, pos); j++) {
               segment++;
               pos.move(Direction.UP);
               if (j == 0 || segment >= 2 && random.nextFloat() < 0.25F) {
                  pos.move(d);
                  segment = 0;
               }
            }
         }
         return true;
      }
   });

   public static final Feature<One> CORAL_CLAW = Registry.register(BuiltInRegistries.FEATURE, Backport.id("coral_claw"), new Feature<One>(One.CODEC) {
      @Override
      public boolean place(FeaturePlaceContext<One> ctx) {
         WorldGenLevel level = ctx.level();
         ChunkGenerator gen = ctx.chunkGenerator();
         RandomSource random = ctx.random();
         BlockPos origin = ctx.origin();
         PlacedFeature feature = ctx.config().feature().value();
         if (!feature.place(level, gen, random, origin)) {
            return false;
         }
         Direction claw = Direction.Plane.HORIZONTAL.getRandomDirection(random);
         int branches = random.nextInt(2) + 2;
         List<Direction> possible = new ArrayList<>(List.of(claw, claw.getClockWise(), claw.getCounterClockWise()));
         java.util.Collections.shuffle(possible, new java.util.Random(random.nextLong()));
         for (Direction branch : possible.subList(0, branches)) {
            BlockPos.MutableBlockPos pos = origin.mutable();
            int sideways = random.nextInt(2) + 1;
            pos.move(branch);
            int inway;
            Direction segment;
            if (branch == claw) {
               segment = claw;
               inway = random.nextInt(3) + 2;
            } else {
               pos.move(Direction.UP);
               segment = random.nextBoolean() ? branch : Direction.UP;
               inway = random.nextInt(3) + 3;
            }
            for (int i = 0; i < sideways && feature.place(level, gen, random, pos); i++) {
               pos.move(segment);
            }
            pos.move(segment.getOpposite());
            pos.move(Direction.UP);
            for (int i = 0; i < inway; i++) {
               pos.move(claw);
               if (!feature.place(level, gen, random, pos)) {
                  break;
               }
               if (random.nextFloat() < 0.25F) {
                  pos.move(Direction.UP);
               }
            }
         }
         return true;
      }
   });

   public static final PlacementModifierType<CuboidPlacement> CUBOID = Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, Backport.id("cuboid"), () -> CuboidPlacement.CODEC);
   public static final PlacementModifierType<RandomChancePlacement> RANDOM_CHANCE = Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, Backport.id("random_chance"), () -> RandomChancePlacement.CODEC);
   public static final BlockStateProviderType<RandomBlockProvider> RANDOM_BLOCK = Registry.register(BuiltInRegistries.BLOCKSTATE_PROVIDER_TYPE, Backport.id("random_block"), new BlockStateProviderType<>(RandomBlockProvider.CODEC));

   private CoralFeatures() {
   }

   public static void init() {
   }

   public static class CuboidPlacement extends PlacementModifier {
      public static final Codec<CuboidPlacement> CODEC = RecordCodecBuilder.create(i -> i.group(
         IntProvider.codec(1, 16).fieldOf("xz_size").forGetter(c -> c.xz),
         IntProvider.codec(1, 16).fieldOf("y_size").forGetter(c -> c.y),
         Codec.BOOL.optionalFieldOf("include_edges", true).forGetter(c -> c.edges),
         Codec.BOOL.optionalFieldOf("include_interior", true).forGetter(c -> c.interior)
      ).apply(i, CuboidPlacement::new));
      private final IntProvider xz;
      private final IntProvider y;
      private final boolean edges;
      private final boolean interior;

      public CuboidPlacement(IntProvider xz, IntProvider y, boolean edges, boolean interior) {
         this.xz = xz;
         this.y = y;
         this.edges = edges;
         this.interior = interior;
      }

      @Override
      public Stream<BlockPos> getPositions(PlacementContext ctx, RandomSource random, BlockPos origin) {
         int height = this.y.sample(random);
         int width = this.xz.sample(random);
         int length = this.xz.sample(random);
         List<BlockPos> out = new ArrayList<>();
         for (int x = 0; x <= width; x++) {
            for (int yy = 0; yy <= height; yy++) {
               for (int z = 0; z <= length; z++) {
                  if ((this.edges || x != 0 && x != width || yy != 0 && yy != height)
                     && (this.edges || z != 0 && z != length || yy != 0 && yy != height)
                     && (this.edges || x != 0 && x != width || z != 0 && z != length)
                     && (this.interior || x == 0 || x == width || yy == 0 || yy == height || z == 0 || z == length)) {
                     out.add(origin.offset(x, yy, z));
                  }
               }
            }
         }
         return out.stream();
      }

      @Override
      public PlacementModifierType<?> type() {
         return CUBOID;
      }
   }

   public static class RandomChancePlacement extends PlacementModifier {
      public static final Codec<RandomChancePlacement> CODEC = RecordCodecBuilder.create(i -> i.group(Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(c -> c.chance)).apply(i, RandomChancePlacement::new));
      private final float chance;

      public RandomChancePlacement(float chance) {
         this.chance = chance;
      }

      @Override
      public Stream<BlockPos> getPositions(PlacementContext ctx, RandomSource random, BlockPos pos) {
         return random.nextFloat() < this.chance ? Stream.of(pos) : Stream.empty();
      }

      @Override
      public PlacementModifierType<?> type() {
         return RANDOM_CHANCE;
      }
   }

   public static class RandomBlockProvider extends BlockStateProvider {
      public static final Codec<RandomBlockProvider> CODEC = RecordCodecBuilder.create(i -> i.group(
         TagKey.hashedCodec(Registries.BLOCK).fieldOf("blocks").forGetter(p -> p.tag),
         Direction.CODEC.optionalFieldOf("direction").forGetter(p -> p.direction)
      ).apply(i, RandomBlockProvider::new));
      private final TagKey<Block> tag;
      private final Optional<Direction> direction;

      public RandomBlockProvider(TagKey<Block> tag, Optional<Direction> direction) {
         this.tag = tag;
         this.direction = direction;
      }

      @Override
      protected BlockStateProviderType<?> type() {
         return RANDOM_BLOCK;
      }

      @Override
      public BlockState getState(RandomSource random, BlockPos pos) {
         Optional<HolderSet.Named<Block>> set = BuiltInRegistries.BLOCK.getTag(this.tag);
         if (set.isEmpty()) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
         }
         Optional<Holder<Block>> pick = set.get().getRandomElement(random);
         BlockState state = pick.map(h -> h.value().defaultBlockState()).orElse(net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
         if (this.direction.isPresent() && state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            state = state.setValue(HorizontalDirectionalBlock.FACING, this.direction.get());
         }
         return state;
      }
   }
}
