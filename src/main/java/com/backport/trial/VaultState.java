package com.backport.trial;

import net.minecraft.util.StringRepresentable;

public enum VaultState implements StringRepresentable {
   INACTIVE("inactive", 6),
   ACTIVE("active", 12),
   UNLOCKING("unlocking", 12),
   EJECTING("ejecting", 12);

   private final String name;
   public final int lightLevel;

   VaultState(String name, int light) {
      this.name = name;
      this.lightLevel = light;
   }

   public String getSerializedName() {
      return this.name;
   }
}
