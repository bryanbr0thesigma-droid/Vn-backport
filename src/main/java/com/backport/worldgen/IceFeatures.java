package com.backport.worldgen;

import com.backport.Backport;
import com.backport.ice.IceCaves;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

/** Ice Caves features: packed ice/calcite walls, snow + ice crystal floor patches, hanging icicles. */
public final class IceFeatures {
   public static final Feature<NoneFeatureConfiguration> PAINT = Registry.register(BuiltInRegistries.FEATURE, Backport.id("ice_cave_paint"), new PaintFeature());
   public static final Feature<NoneFeatureConfiguration> FLOOR = Registry.register(BuiltInRegistries.FEATURE, Backport.id("ice_cave_floor"), new FloorFeature());
   public static final Feature<NoneFeatureConfiguration> ICICLE = Registry.register(BuiltInRegistries.FEATURE, Backport.id("icicle"), new IcicleFeature());

   private IceFeatures() {
   }

   public static void init() {
   }

   static boolean stoneLike(BlockState s) {
      return s.is(BlockTags.BASE_STONE_OVERWORLD) || s.is(Blocks.DEEPSLATE) || s.is(Blocks.TUFF) || s.is(Blocks.CALCITE) || s.is(Blocks.PACKED_ICE) || s.is(BlockTags.DIRT);
   }

   static class PaintFeature extends Feature<NoneFeatureConfiguration> {
      private static final ResourceKey<net.minecraft.world.level.biome.Biome> BIOME = ResourceKey.create(Registries.BIOME, Backport.id("ice_caves"));

      PaintFeature() {
         super(NoneFeatureConfiguration.CODEC);
      }

      @Override
      public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
         WorldGenLevel level = ctx.level();
         BlockPos o = ctx.origin();
         int x0 = o.getX() & ~15;
         int z0 = o.getZ() & ~15;
         int minY = level.getMinBuildHeight();
         int maxY = Math.min(level.getMaxBuildHeight(), 100);
         ImprovedNoise noise = new ImprovedNoise(new LegacyRandomSource(level.getSeed() ^ 0x1CEC0DEL));
         boolean any = false;
         BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
         for (int qx = 0; qx < 4; qx++) {
            for (int qz = 0; qz < 4; qz++) {
               for (int y = minY; y < maxY; y += 4) {
                  m.set(x0 + qx * 4 + 2, y + 2, z0 + qz * 4 + 2);
                  if (!level.getBiome(m).is(BIOME)) {
                     continue;
                  }
                  for (int dx = 0; dx < 4; dx++) {
                     for (int dz = 0; dz < 4; dz++) {
                        for (int dy = 0; dy < 4; dy++) {
                           m.set(x0 + qx * 4 + dx, y + dy, z0 + qz * 4 + dz);
                           BlockState s = level.getBlockState(m);
                           if (!(s.is(BlockTags.BASE_STONE_OVERWORLD) || s.is(Blocks.DEEPSLATE) || s.is(Blocks.TUFF) || s.is(BlockTags.DIRT))) {
                              continue;
                           }
                           double n = noise.noise(m.getX() * 0.07, m.getY() * 0.1, m.getZ() * 0.07) * 1.4;
                           if (n > 0.05) {
                              level.setBlock(m, Blocks.PACKED_ICE.defaultBlockState(), 2);
                              any = true;
                           } else if (n < -0.35) {
                              level.setBlock(m, Blocks.CALCITE.defaultBlockState(), 2);
                              any = true;
                           }
                        }
                     }
                  }
               }
            }
         }
         return any;
      }
   }

   static class FloorFeature extends Feature<NoneFeatureConfiguration> {
      FloorFeature() {
         super(NoneFeatureConfiguration.CODEC);
      }

      @Override
      public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
         WorldGenLevel level = ctx.level();
         BlockPos origin = ctx.origin();
         RandomSource r = ctx.random();
         if (!level.getBlockState(origin).isAir()) {
            return false;
         }
         int radius = 3 + r.nextInt(4);
         boolean any = false;
         for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
               double dist = Math.sqrt(dx * dx + dz * dz);
               if (dist > radius || r.nextFloat() < 0.25F + dist / radius * 0.4F) {
                  continue;
               }
               BlockPos col = origin.offset(dx, 0, dz);
               BlockPos floor = null;
               for (int dy = 0; dy < 12; dy++) {
                  BlockPos p = col.below(dy);
                  BlockState s = level.getBlockState(p);
                  if (!s.isAir()) {
                     if (s.isFaceSturdy(level, p, Direction.UP) && level.getBlockState(p.above()).isAir() && !level.getFluidState(p.above()).is(net.minecraft.tags.FluidTags.WATER)) {
                        floor = p;
                     }
                     break;
                  }
               }
               if (floor == null) {
                  continue;
               }
               BlockPos above = floor.above();
               if (r.nextFloat() < 0.14F) {
                  level.setBlock(above, IceCaves.ICE_CRYSTAL.defaultBlockState(), 2);
               } else {
                  level.setBlock(above, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1 + r.nextInt(3)), 2);
               }
               any = true;
            }
         }
         return any;
      }
   }

   static class IcicleFeature extends Feature<NoneFeatureConfiguration> {
      IcicleFeature() {
         super(NoneFeatureConfiguration.CODEC);
      }

      @Override
      public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
         WorldGenLevel level = ctx.level();
         BlockPos pos = ctx.origin();
         if (!level.getBlockState(pos).isAir() && !level.getBlockState(pos).is(Blocks.WATER)) {
            return false;
         }
         BlockPos ceiling = pos.above();
         if (!stoneLike(level.getBlockState(ceiling))) {
            return false;
         }
         if (!level.getBlockState(ceiling).is(Blocks.PACKED_ICE)) {
            level.setBlock(ceiling, Blocks.PACKED_ICE.defaultBlockState(), 2);
         }
         SulfurFeatures.spike(level, pos, Direction.DOWN, 1 + ctx.random().nextInt(5), IceCaves.ICICLE);
         return true;
      }
   }
}
