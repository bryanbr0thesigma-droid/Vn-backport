package com.vnap.client;

import com.vnap.mixin.client.ClientLevelAccessor;
import java.util.UUID;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

/** UUID entity lookup on the client level, which 1.20.1 only exposes through a protected getter. */
public final class ClientEntities {
   private ClientEntities() {
   }

   public static Entity get(ClientLevel level, UUID id) {
      return ((ClientLevelAccessor)level).vnap$getEntities().get(id);
   }
}
