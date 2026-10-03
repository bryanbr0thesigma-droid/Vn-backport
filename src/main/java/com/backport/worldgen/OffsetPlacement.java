package com.backport.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class OffsetPlacement extends PlacementModifier {
   public static final Codec<OffsetPlacement> CODEC = RecordCodecBuilder.create(i -> i.group(
      IntProvider.codec(-16, 16).fieldOf("x").orElse(net.minecraft.util.valueproviders.ConstantInt.ZERO).forGetter(o -> o.x),
      IntProvider.codec(-16, 16).fieldOf("y").orElse(net.minecraft.util.valueproviders.ConstantInt.ZERO).forGetter(o -> o.y),
      IntProvider.codec(-16, 16).fieldOf("z").orElse(net.minecraft.util.valueproviders.ConstantInt.ZERO).forGetter(o -> o.z)
   ).apply(i, OffsetPlacement::new));
   private final IntProvider x;
   private final IntProvider y;
   private final IntProvider z;

   public OffsetPlacement(IntProvider x, IntProvider y, IntProvider z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public Stream<BlockPos> getPositions(PlacementContext ctx, RandomSource random, BlockPos pos) {
      return Stream.of(pos.offset(this.x.sample(random), this.y.sample(random), this.z.sample(random)));
   }

   public PlacementModifierType<?> type() {
      return BackportWorldgen.OFFSET;
   }
}
