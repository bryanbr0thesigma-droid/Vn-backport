package com.backport.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;

public class TrapezoidInt extends IntProvider {
   public static final Codec<TrapezoidInt> CODEC = RecordCodecBuilder.create(i -> i.group(
      Codec.INT.fieldOf("min").forGetter(t -> t.min),
      Codec.INT.fieldOf("max").forGetter(t -> t.max),
      Codec.INT.fieldOf("plateau").forGetter(t -> t.plateau)
   ).apply(i, TrapezoidInt::new));
   public static IntProviderType<TrapezoidInt> TYPE;
   private final int min;
   private final int max;
   private final int plateau;

   public TrapezoidInt(int min, int max, int plateau) {
      this.min = min;
      this.max = max;
      this.plateau = plateau;
   }

   public static void register() {
      TYPE = Registry.register(BuiltInRegistries.INT_PROVIDER_TYPE, new ResourceLocation("minecraft", "trapezoid"), () -> CODEC);
   }

   public int sample(RandomSource random) {
      if (this.plateau == 0 && this.max == -this.min) {
         return random.nextInt(this.max + 1) - random.nextInt(this.max + 1);
      }
      int range = this.max - this.min;
      if (this.plateau == range) return Mth.randomBetweenInclusive(random, this.min, this.max);
      int start = (range - this.plateau) / 2;
      int end = range - start;
      return this.min + Mth.randomBetweenInclusive(random, 0, end) + Mth.randomBetweenInclusive(random, 0, start);
   }

   public int getMinValue() {
      return this.min;
   }

   public int getMaxValue() {
      return this.max;
   }

   public IntProviderType<?> getType() {
      return TYPE;
   }
}
