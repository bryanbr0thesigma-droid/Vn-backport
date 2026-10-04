package com.backport.advancement;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerPlayer;

/** Remembers where a wind charge launched a player so landing far below fires "Who Needs Rockets?". */
public final class ExplosionFall {
   private static final Map<ServerPlayer, double[]> LAUNCHES = new WeakHashMap<>();

   private ExplosionFall() {
   }

   public static void record(ServerPlayer player) {
      LAUNCHES.put(player, new double[]{player.getY(), player.level().getGameTime()});
   }

   public static void landed(ServerPlayer player) {
      double[] launch = LAUNCHES.remove(player);
      if (launch != null && player.level().getGameTime() - launch[1] < 400) {
         double fell = launch[0] - player.getY();
         if (fell >= 7.0) {
            BackportEvents.fire(player, "fall_after_explosion", fell);
         }
      }
   }

   public static void forget(ServerPlayer player) {
      LAUNCHES.remove(player);
   }
}
