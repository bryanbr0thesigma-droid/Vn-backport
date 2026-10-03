package com.backport;

import com.backport.entity.CopperGolem;
import net.minecraft.server.level.ServerLevel;

public final class CopperGolemStatues {
   private CopperGolemStatues() {
   }

   public static boolean enabled() {
      return false;
   }

   public static void turnToStatue(CopperGolem golem, ServerLevel level) {
   }
}
