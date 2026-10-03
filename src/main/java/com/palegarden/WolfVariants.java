package com.palegarden;

import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.core.Holder;

public final class WolfVariants {
   /** Index 0 is the classic (pale) wolf and keeps the vanilla textures. */
   public static final String[] NAMES = {"pale", "ashen", "black", "chestnut", "rusty", "snowy", "spotted", "striped", "woods"};
   private static final Map<String, Integer> BY_BIOME = Map.ofEntries(
      Map.entry("taiga", 0), Map.entry("snowy_taiga", 1), Map.entry("old_growth_pine_taiga", 2), Map.entry("old_growth_spruce_taiga", 3),
      Map.entry("jungle", 4), Map.entry("sparse_jungle", 4), Map.entry("bamboo_jungle", 4), Map.entry("grove", 5),
      Map.entry("savanna", 6), Map.entry("savanna_plateau", 6), Map.entry("windswept_savanna", 6),
      Map.entry("badlands", 7), Map.entry("eroded_badlands", 7), Map.entry("wooded_badlands", 7), Map.entry("forest", 8)
   );

   private WolfVariants() {
   }

   public static int forBiome(Holder<Biome> biome) {
      return biome.unwrapKey().map(key -> BY_BIOME.getOrDefault(key.location().getPath(), 0)).orElse(0);
   }

   public static ResourceLocation texture(int variant, boolean angry, boolean tame) {
      String name = NAMES[Math.floorMod(variant, NAMES.length)];
      String suffix = angry ? "_angry" : tame ? "_tame" : "";
      return PaleGarden.id("textures/entity/wolf/wolf_" + name + suffix + ".png");
   }

   public interface Holder2 {
      int palegarden$getVariant();

      void palegarden$setVariant(int variant);
   }

   public static int of(Wolf wolf) {
      return ((Holder2)wolf).palegarden$getVariant();
   }
}
