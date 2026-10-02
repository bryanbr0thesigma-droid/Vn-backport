package com.vnap.entity;

import net.minecraft.world.entity.MobSpawnType;

public interface VillagerNewsData {
   MobSpawnType vnap$spawnReason();

   boolean vnap$hasNose();

   void vnap$setHasNose(boolean var1);

   int vnap$cosmetic();

   void vnap$setCosmetic(int var1);

   int vnap$signMessage();

   void vnap$setSignMessage(int var1);

   int vnap$signType();

   void vnap$setSignType(int var1);

   boolean vnap$hasOriginalVillagerState();

   void vnap$captureOriginalVillagerState();

   void vnap$restoreOriginalVillagerState();
}
