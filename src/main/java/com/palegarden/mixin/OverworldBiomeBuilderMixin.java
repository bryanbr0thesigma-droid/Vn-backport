package com.palegarden.mixin;

import com.palegarden.PaleGarden;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OverworldBiomeBuilder.class)
public abstract class OverworldBiomeBuilderMixin {
   private static final ResourceKey<Biome> PALE_GARDEN = ResourceKey.create(Registries.BIOME, PaleGarden.id("pale_garden"));

   private static final ResourceKey<Biome> DAPPLED_FOREST = ResourceKey.create(Registries.BIOME, new net.minecraft.resources.ResourceLocation("backport", "dappled_forest"));

   @Inject(method = "pickMiddleBiome", at = @At("RETURN"), cancellable = true)
   private void palegarden$dappledForest(int temperature, int humidity, Climate.Parameter weirdness, CallbackInfoReturnable<ResourceKey<Biome>> cir) {
      if (temperature == 1 && humidity == 0 && weirdness.max() >= 0L) {
         cir.setReturnValue(DAPPLED_FOREST);
      }
   }

   /** Pale Garden takes the dark forest's place on the plateau/"variant" climate slice, as in 1.21.4. */
   @Inject(method = "pickPlateauBiome", at = @At("RETURN"), cancellable = true)
   private void palegarden$paleGarden(int temperature, int humidity, Climate.Parameter weirdness, CallbackInfoReturnable<ResourceKey<Biome>> cir) {
      if (temperature == 2 && humidity == 4 && weirdness.max() >= 0L) {
         cir.setReturnValue(PALE_GARDEN);
      }
   }
}
