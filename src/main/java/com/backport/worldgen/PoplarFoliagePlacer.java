package com.backport.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class PoplarFoliagePlacer extends FoliagePlacer {
   public static final Codec<PoplarFoliagePlacer> CODEC = RecordCodecBuilder.create(i -> foliagePlacerParts(i).and(i.group(
      IntProvider.codec(5, 16).fieldOf("height").forGetter(p -> p.height),
      Codec.floatRange(0.0F, 1.0F).fieldOf("side_hole_chance").forGetter(p -> p.sideHoleChance)
   )).apply(i, PoplarFoliagePlacer::new));
   private final IntProvider height;
   private final float sideHoleChance;

   public PoplarFoliagePlacer(IntProvider radius, IntProvider offset, IntProvider height, float sideHoleChance) {
      super(radius, offset);
      this.height = height;
      this.sideHoleChance = sideHoleChance;
   }

   protected FoliagePlacerType<?> type() {
      return BackportWorldgen.POPLAR_FOLIAGE;
   }

   protected void createFoliage(LevelSimulatedReader level, FoliageSetter setter, RandomSource random, TreeConfiguration tree, int treeHeight, FoliageAttachment attachment, int foliageHeight, int leafRadius, int offset) {
      boolean doubleTrunk = attachment.doubleTrunk();
      BlockPos foliagePos = attachment.pos().above(offset);
      int r = leafRadius + attachment.radiusOffset() - 1;
      boolean flip = random.nextBoolean();
      int h = foliageHeight;
      this.row(level, setter, random, tree, foliagePos, r - 2, h - 1, doubleTrunk, h, flip);
      this.row(level, setter, random, tree, foliagePos, r - 1, h - 2, doubleTrunk, h, flip);
      this.row(level, setter, random, tree, foliagePos, r - 1, h - 3, doubleTrunk, h, flip);
      for (int y = h - 4; y >= 1; y--) this.row(level, setter, random, tree, foliagePos, r, y, doubleTrunk, h, flip);
      this.replaceWithLog(level, setter, tree, random, foliagePos, r, h - 4, doubleTrunk, h, flip);
      this.row(level, setter, random, tree, foliagePos, r - 1, 0, doubleTrunk, h, flip);
      this.row(level, setter, random, tree, foliagePos, Mth.clamp(r - 2, 1, 2), -1, doubleTrunk, h, flip);
   }

   private void replaceWithLog(LevelSimulatedReader level, FoliageSetter setter, TreeConfiguration tree, RandomSource random, BlockPos origin, int r, int y, boolean doubleTrunk, int fh, boolean flip) {
      int off = doubleTrunk ? 1 : 0;
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
      for (int dx = -r; dx <= r + off; dx++) {
         for (int dz = -r; dz <= r + off; dz++) {
            int adz = Mth.abs(dz);
            int adx = Mth.abs(dx);
            if (within(r, adx, adz, this.cornersToCut(dx, dz, r, this.partial(fh, y), flip), 2) && (adz == 0 && r - adx >= 4 || adx == 0 && r - adz >= 4)) {
               pos.setWithOffset(origin, dx, y, dz);
               Direction.Axis axis = adz == 0 ? Direction.Axis.X : Direction.Axis.Z;
               if (level.isStateAtPosition(pos, s -> s.equals(tree.foliageProvider.getState(random, pos)))) {
                  BlockState log = tree.trunkProvider.getState(random, pos);
                  if (log.hasProperty(RotatedPillarBlock.AXIS)) log = log.setValue(RotatedPillarBlock.AXIS, axis);
                  setter.set(pos, log);
               }
            }
         }
      }
   }

   public int foliageHeight(RandomSource random, int treeHeight, TreeConfiguration tree) {
      return this.height.sample(random);
   }

   private void row(LevelSimulatedReader level, FoliageSetter setter, RandomSource random, TreeConfiguration tree, BlockPos origin, int r, int y, boolean doubleTrunk, int fh, boolean flip) {
      int off = doubleTrunk ? 1 : 0;
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
      for (int dx = -r; dx <= r + off; dx++) {
         for (int dz = -r; dz <= r + off; dz++) {
            if (!this.skip(random, dx, y, dz, r, doubleTrunk, fh, flip)) {
               pos.setWithOffset(origin, dx, y, dz);
               tryPlaceLeaf(level, setter, random, tree, pos);
            }
         }
      }
   }

   private boolean skip(RandomSource random, int dx, int y, int dz, int r, boolean doubleTrunk, int fh, boolean flip) {
      boolean partial = this.partial(fh, y);
      int corners = this.cornersToCut(dx, dz, r, partial, flip);
      int adx = Mth.abs(dx);
      int adz = Mth.abs(dz);
      boolean edge = adx == r || adz == r;
      if (partial && edge) return true;
      int extra = random.nextFloat() <= this.sideHoleChance ? 1 : 0;
      return !within(r, adx, adz, corners, extra);
   }

   protected boolean shouldSkipLocation(RandomSource random, int dx, int y, int dz, int r, boolean doubleTrunk) {
      return false;
   }

   private int cornersToCut(int dx, int dz, int r, boolean partial, boolean flip) {
      boolean small = flip ? (dx > 0 && dz > 0 || dz < 0 && dx < 0) : (dx > 0 && dz < 0 || dz > 0 && dx < 0);
      return small ? r - 1 : (partial ? r + 1 : r);
   }

   private static boolean within(int r, int adx, int adz, int corners, int extra) {
      return adx + adz <= r * 2 - (corners + extra);
   }

   private boolean partial(int fh, int y) {
      return fh - 1 == y || fh - 2 == y;
   }
}
