package com.backport.worldgen;

import com.backport.NewStone;
import com.backport.Backport;
import com.backport.sulfur.PotentSulfurBlock;
import com.backport.sulfur.SulfurSpikeBlock;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.LegacyRandomSource;

/** Features for the Sulfur Caves biome (simplified ports of speleothem / speleothem_cluster / sulfur_pool / template). */
public final class SulfurFeatures {
   public static final Feature<NoneFeatureConfiguration> SPIKE = Registry.register(BuiltInRegistries.FEATURE, Backport.id("sulfur_spike"), new SpikeFeature());
   public static final Feature<NoneFeatureConfiguration> CLUSTER = Registry.register(BuiltInRegistries.FEATURE, Backport.id("sulfur_spike_cluster"), new ClusterFeature());
   public static final Feature<NoneFeatureConfiguration> PAINT = Registry.register(BuiltInRegistries.FEATURE, Backport.id("sulfur_cave_paint"), new PaintFeature());
   public static final Feature<NoneFeatureConfiguration> POOL = Registry.register(BuiltInRegistries.FEATURE, Backport.id("sulfur_pool"), new PoolFeature());
   public static final Feature<TemplateConfig> TEMPLATE = Registry.register(BuiltInRegistries.FEATURE, Backport.id("template_spring"), new TemplateFeature());

   private SulfurFeatures() {
   }

   public static void init() {
   }

   static boolean replaceable(BlockState s) {
      return s.is(BlockTags.BASE_STONE_OVERWORLD) || s.is(Blocks.DEEPSLATE) || s.is(Blocks.TUFF) || s.is(Blocks.CALCITE) || s.is(NewStone.SULFUR) || s.is(NewStone.CINNABAR) || s.is(BlockTags.DIRT);
   }

   static void spike(WorldGenLevel level, BlockPos start, Direction dir, int length) {
      Block spike = NewStone.SULFUR_SPIKE;
      BlockPos.MutableBlockPos p = start.mutable();
      for (int i = 0; i < length; i++) {
         int rem = length - 1 - i;
         DripstoneThickness t = rem == 0 ? DripstoneThickness.TIP : rem == 1 ? DripstoneThickness.FRUSTUM : i == 0 ? DripstoneThickness.BASE : DripstoneThickness.MIDDLE;
         BlockState cur = level.getBlockState(p);
         if (!cur.isAir() && !cur.is(Blocks.WATER)) {
            return;
         }
         level.setBlock(p, spike.defaultBlockState().setValue(SulfurSpikeBlock.TIP_DIRECTION, dir).setValue(SulfurSpikeBlock.THICKNESS, t)
            .setValue(SulfurSpikeBlock.WATERLOGGED, cur.is(Blocks.WATER)), 2);
         p.move(dir);
      }
   }

   static boolean solidAt(WorldGenLevel level, BlockPos p) {
      BlockState s = level.getBlockState(p);
      return s.isFaceSturdy(level, p, Direction.UP) || s.isFaceSturdy(level, p, Direction.DOWN);
   }

   static class SpikeFeature extends Feature<NoneFeatureConfiguration> {
      SpikeFeature() {
         super(NoneFeatureConfiguration.CODEC);
      }

      @Override
      public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
         WorldGenLevel level = ctx.level();
         BlockPos pos = ctx.origin();
         RandomSource r = ctx.random();
         BlockState here = level.getBlockState(pos);
         if (!here.isAir() && !here.is(Blocks.WATER)) {
            return false;
         }
         boolean floor = replaceable(level.getBlockState(pos.below()));
         boolean ceil = replaceable(level.getBlockState(pos.above()));
         if (floor == ceil) {
            return false;
         }
         Direction dir = floor ? Direction.UP : Direction.DOWN;
         BlockPos base = pos.relative(dir.getOpposite());
         level.setBlock(base, NewStone.SULFUR.defaultBlockState(), 2);
         spike(level, pos, dir, 1 + r.nextInt(3));
         return true;
      }
   }

   static class ClusterFeature extends Feature<NoneFeatureConfiguration> {
      ClusterFeature() {
         super(NoneFeatureConfiguration.CODEC);
      }

      @Override
      public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
         WorldGenLevel level = ctx.level();
         BlockPos origin = ctx.origin();
         RandomSource r = ctx.random();
         BlockState here = level.getBlockState(origin);
         if (!here.isAir() && !here.is(Blocks.WATER)) {
            return false;
         }
         int radius = 2 + r.nextInt(7);
         float density = 0.3F + r.nextFloat() * 0.4F;
         boolean any = false;
         for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
               double dist = Math.sqrt(dx * dx + dz * dz);
               if (dist > radius) {
                  continue;
               }
               float chance = (float) (density * (1.0 - 0.9 * dist / radius));
               BlockPos col = origin.offset(dx, 0, dz);
               // find floor
               BlockPos floorPos = null;
               for (int dy = 0; dy < 12; dy++) {
                  BlockPos p = col.below(dy);
                  if (!level.getBlockState(p).isAir() && !level.getBlockState(p).is(Blocks.WATER)) {
                     if (replaceable(level.getBlockState(p))) {
                        floorPos = p;
                     }
                     break;
                  }
               }
               BlockPos ceilPos = null;
               for (int dy = 1; dy < 12; dy++) {
                  BlockPos p = col.above(dy);
                  if (!level.getBlockState(p).isAir() && !level.getBlockState(p).is(Blocks.WATER)) {
                     if (replaceable(level.getBlockState(p))) {
                        ceilPos = p;
                     }
                     break;
                  }
               }
               if (floorPos != null) {
                  level.setBlock(floorPos, NewStone.SULFUR.defaultBlockState(), 2);
                  if (r.nextFloat() < chance) {
                     spike(level, floorPos.above(), Direction.UP, 1 + r.nextInt(4));
                     any = true;
                  }
               }
               if (ceilPos != null) {
                  level.setBlock(ceilPos, NewStone.SULFUR.defaultBlockState(), 2);
                  if (r.nextFloat() < chance) {
                     spike(level, ceilPos.below(), Direction.DOWN, 1 + r.nextInt(4));
                     any = true;
                  }
               }
            }
         }
         return any;
      }
   }

   static class PaintFeature extends Feature<NoneFeatureConfiguration> {
      private static final ResourceLocation BIOME = Backport.id("sulfur_caves");

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
         int maxY = Math.min(level.getMaxBuildHeight(), 80);
         long seed = level.getSeed();
         ImprovedNoise noise = new ImprovedNoise(new LegacyRandomSource(seed ^ 0x5E1F0AL));
         boolean any = false;
         BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
         for (int qx = 0; qx < 4; qx++) {
            for (int qz = 0; qz < 4; qz++) {
               for (int y = minY; y < maxY; y += 4) {
                  m.set(x0 + qx * 4 + 2, y + 2, z0 + qz * 4 + 2);
                  if (!level.getBiome(m).is(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BIOME, BIOME))) {
                     continue;
                  }
                  for (int dx = 0; dx < 4; dx++) {
                     for (int dz = 0; dz < 4; dz++) {
                        for (int dy = 0; dy < 4; dy++) {
                           m.set(x0 + qx * 4 + dx, y + dy, z0 + qz * 4 + dz);
                           BlockState s = level.getBlockState(m);
                           if (!(s.is(BlockTags.BASE_STONE_OVERWORLD) || s.is(Blocks.DEEPSLATE) || s.is(Blocks.TUFF))) {
                              continue;
                           }
                           double n = noise.noise(m.getX() * 0.08, m.getY() * 0.12, m.getZ() * 0.08) * 1.4;
                           BlockState to = null;
                           if (n > -0.4 && n < -0.1 || n > 0.4) {
                              to = NewStone.CINNABAR.defaultBlockState();
                           } else if (n >= 0.0 && n <= 0.4) {
                              to = NewStone.SULFUR.defaultBlockState();
                           }
                           if (to != null) {
                              level.setBlock(m, to, 2);
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

   static class PoolFeature extends Feature<NoneFeatureConfiguration> {
      PoolFeature() {
         super(NoneFeatureConfiguration.CODEC);
      }

      @Override
      public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
         WorldGenLevel level = ctx.level();
         BlockPos o = ctx.origin();
         net.minecraft.world.level.levelgen.feature.LakeFeature.Configuration cfg = new net.minecraft.world.level.levelgen.feature.LakeFeature.Configuration(
            net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider.simple(Blocks.WATER.defaultBlockState()),
            net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider.simple(NewStone.SULFUR.defaultBlockState()));
         if (!Feature.LAKE.place(cfg, level, ctx.chunkGenerator(), ctx.random(), o)) {
            return false;
         }
         // potent sulfur at the bottom of the pool
         for (int i = 1; i <= 8; i++) {
            BlockPos p = o.below(i);
            BlockState s = level.getBlockState(p);
            if (!s.is(Blocks.WATER) && !s.isAir() && level.getFluidState(p.above()).is(net.minecraft.world.level.material.Fluids.WATER)) {
               level.setBlock(p, NewStone.POTENT_SULFUR.defaultBlockState().setValue(PotentSulfurBlock.STATE, PotentSulfurBlock.State.WET), 2);
               break;
            }
         }
         return true;
      }
   }

   public record TemplateConfig(List<ResourceLocation> templates, int tuffCount, int radius) implements FeatureConfiguration {
      public static final Codec<TemplateConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
         ResourceLocation.CODEC.listOf().fieldOf("templates").forGetter(TemplateConfig::templates),
         Codec.INT.fieldOf("tuff_count").forGetter(TemplateConfig::tuffCount),
         Codec.INT.fieldOf("radius").forGetter(TemplateConfig::radius)
      ).apply(i, TemplateConfig::new));
   }

   static class TemplateFeature extends Feature<TemplateConfig> {
      TemplateFeature() {
         super(TemplateConfig.CODEC);
      }

      @Override
      public boolean place(FeaturePlaceContext<TemplateConfig> ctx) {
         WorldGenLevel level = ctx.level();
         RandomSource r = ctx.random();
         BlockPos o = ctx.origin();
         TemplateConfig cfg = ctx.config();
         var mgr = level.getLevel().getServer().getStructureManager();
         var tpl = mgr.get(cfg.templates().get(r.nextInt(cfg.templates().size())));
         if (tpl.isEmpty()) {
            return false;
         }
         // scatter some tuff around the lip
         for (int i = 0; i < cfg.tuffCount(); i++) {
            int dx = r.nextInt(cfg.radius() + 1) - r.nextInt(cfg.radius() + 1);
            int dz = r.nextInt(cfg.radius() + 1) - r.nextInt(cfg.radius() + 1);
            int dy = r.nextInt(4) - r.nextInt(4);
            BlockPos p = o.offset(dx, dy, dz);
            for (int s = 0; s < 4; s++) {
               if (solidAt(level, p.below())) {
                  break;
               }
               p = p.below();
            }
            if (level.getBlockState(p).isAir() && solidAt(level, p.below())) {
               level.setBlock(p, Blocks.TUFF.defaultBlockState(), 2);
            }
         }
         var t = tpl.get();
         StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(Rotation.getRandom(r)).setMirror(Mirror.NONE).addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR).setRandom(r);
         BlockPos at = o.offset(-t.getSize().getX() / 2, -7, -t.getSize().getZ() / 2);
         return t.placeInWorld(level, at, at, settings, r, 2);
      }
   }
}
