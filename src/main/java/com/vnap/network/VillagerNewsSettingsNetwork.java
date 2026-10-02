package com.vnap.network;

import com.vnap.config.VillagerNewsSettings;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Join;
import net.minecraft.server.level.ServerPlayer;

public final class VillagerNewsSettingsNetwork {
   private VillagerNewsSettingsNetwork() {
   }

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(VillagerNewsSettingsPayload.TYPE, (payload, player, responseSender) -> {
         if (!canEdit(player)) {
            send(player);
         } else {
            VillagerNewsSettings.update(payload.chattiness(), payload.rareVoicelines(), payload.spawnSpecialVillagers());
            send(player);
         }
      });
      ServerPlayConnectionEvents.JOIN.register((Join)(listener, sender, server) -> send(listener.getPlayer()));
   }

   public static void send(ServerPlayer player) {
      if (ServerPlayNetworking.canSend(player, VillagerNewsSettingsPayload.TYPE)) {
         ServerPlayNetworking.send(
            player,
            new VillagerNewsSettingsPayload(
               VillagerNewsSettings.chattiness(), VillagerNewsSettings.rareVoicelines(), VillagerNewsSettings.spawnSpecialVillagers(), canEdit(player)
            )
         );
      }
   }

   private static boolean canEdit(ServerPlayer player) {
      return player.server.isSingleplayerOwner(player.getGameProfile()) || player.hasPermissions(2);
   }
}
