package com.palegarden;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaleGarden implements ModInitializer {
   public static final String MOD_ID = "pale_garden";
   public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

   public void onInitialize() {
      com.backport.Backport.init();
      PaleSounds.init();
      PaleWood.init();
      PaleParticles.init();
      PaleBlocks.init();
      PaleItems.init();
      PaleEntities.init();
      PaleWorldgen.init();
      LOGGER.info("Pale Garden backport loaded.");
   }

   public static ResourceLocation id(String path) {
      return new ResourceLocation(MOD_ID, path);
   }
}
