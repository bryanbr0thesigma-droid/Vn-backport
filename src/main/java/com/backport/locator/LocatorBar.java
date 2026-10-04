package com.backport.locator;

import com.backport.Backport;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;

/** 26.x locator bar: every player appears on everyone else's bar in the same dimension, unless hidden. */
public final class LocatorBar {
   public static final ResourceLocation PACKET = Backport.id("locator");
   public static GameRules.Key<GameRules.BooleanValue> RULE;

   private LocatorBar() {
   }

   public static void init() {
      RULE = GameRuleRegistry.register("locatorBar", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         if (server.getTickCount() % 4 != 0) {
            return;
         }
         for (ServerPlayer viewer : server.getPlayerList().getPlayers()) {
            send(viewer);
         }
      });
   }

   private static boolean transmits(ServerPlayer p) {
      return !p.isSpectator() && !p.hasEffect(MobEffects.INVISIBILITY) && !p.getItemBySlot(EquipmentSlot.HEAD).is(Items.CARVED_PUMPKIN);
   }

   private static void send(ServerPlayer viewer) {
      boolean enabled = viewer.serverLevel().getGameRules().getBoolean(RULE);
      FriendlyByteBuf buf = PacketByteBufs.create();
      buf.writeBoolean(enabled);
      if (!enabled) {
         buf.writeVarInt(0);
         ServerPlayNetworking.send(viewer, PACKET, buf);
         return;
      }
      var others = viewer.serverLevel().players().stream().filter(p -> p != viewer && transmits(p)).toList();
      buf.writeVarInt(others.size());
      for (ServerPlayer p : others) {
         buf.writeUUID(p.getUUID());
         buf.writeDouble(p.getX());
         buf.writeDouble(p.getY());
         buf.writeDouble(p.getZ());
         Integer color = p.getTeam() != null ? p.getTeam().getColor().getColor() : null;
         buf.writeInt(color == null ? -1 : color);
      }
      ServerPlayNetworking.send(viewer, PACKET, buf);
   }
}
