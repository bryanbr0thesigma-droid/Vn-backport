package com.backport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/** Adds the 26.x plant patches and leaf litter to the matching vanilla biomes (data: backport/biome_additions.json). */
public final class BiomeAdditions {
   private BiomeAdditions() {
   }

   public static void init() {
      try (var in = BiomeAdditions.class.getResourceAsStream("/data/backport/biome_additions.json")) {
         if (in == null) return;
         JsonObject table = JsonParser.parseReader(new InputStreamReader(in)).getAsJsonObject();
         GenerationStep.Decoration[] steps = GenerationStep.Decoration.values();
         for (var e : table.entrySet()) {
            ResourceKey<Biome> biome = ResourceKey.create(Registries.BIOME, new ResourceLocation(e.getKey()));
            JsonObject o = e.getValue().getAsJsonObject();
            for (JsonElement a : o.getAsJsonArray("add")) {
               JsonArray arr = a.getAsJsonArray();
               GenerationStep.Decoration step = steps[arr.get(0).getAsInt()];
               ResourceKey<PlacedFeature> f = ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(arr.get(1).getAsString()));
               BiomeModifications.create(Backport.id("add_" + e.getKey() + "_" + f.location().getPath()))
                  .add(ModificationPhase.ADDITIONS, BiomeSelectors.includeByKey(biome), ctx -> ctx.getGenerationSettings().addFeature(step, f));
            }
            for (JsonElement a : o.getAsJsonArray("remove")) {
               JsonArray arr = a.getAsJsonArray();
               GenerationStep.Decoration step = steps[arr.get(0).getAsInt()];
               ResourceKey<PlacedFeature> f = ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(arr.get(1).getAsString()));
               BiomeModifications.create(Backport.id("remove_" + e.getKey() + "_" + f.location().getPath()))
                  .add(ModificationPhase.REMOVALS, BiomeSelectors.includeByKey(biome), ctx -> ctx.getGenerationSettings().removeFeature(step, f));
            }
         }
      } catch (Exception ex) {
         Backport.LOGGER.warn("Failed to read biome additions", ex);
      }
   }
}
