package com.backport.worldgen;

import com.backport.Backport;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;

/** 26.4 block predicate: true when the position is below the given heightmap (i.e. covered from above). */
public record BelowHeightmapPredicate(Heightmap.Types heightmap) implements BlockPredicate {
   public static final Codec<BelowHeightmapPredicate> CODEC = RecordCodecBuilder.create(i -> i.group(
      Heightmap.Types.CODEC.fieldOf("heightmap").forGetter(BelowHeightmapPredicate::heightmap)
   ).apply(i, BelowHeightmapPredicate::new));
   public static final BlockPredicateType<BelowHeightmapPredicate> TYPE = Registry.register(BuiltInRegistries.BLOCK_PREDICATE_TYPE, Backport.id("below_heightmap"), () -> CODEC);

   @Override
   public boolean test(WorldGenLevel level, BlockPos pos) {
      return pos.getY() < level.getHeight(this.heightmap, pos.getX(), pos.getZ());
   }

   @Override
   public BlockPredicateType<?> type() {
      return TYPE;
   }

   public static void init() {
   }
}
