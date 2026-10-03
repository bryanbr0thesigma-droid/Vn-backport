package com.backport.trial;

import net.minecraft.util.StringRepresentable;

public enum TrialSpawnerState implements StringRepresentable {
   INACTIVE("inactive", 0, -1.0, false),
   WAITING_FOR_PLAYERS("waiting_for_players", 4, 200.0, true),
   ACTIVE("active", 8, 1000.0, true),
   WAITING_FOR_REWARD_EJECTION("waiting_for_reward_ejection", 8, -1.0, false),
   EJECTING_REWARD("ejecting_reward", 8, -1.0, false),
   COOLDOWN("cooldown", 0, -1.0, false);

   private final String name;
   public final int lightLevel;
   public final double spinSpeed;
   public final boolean capableOfSpawning;

   TrialSpawnerState(String name, int light, double spin, boolean spawning) {
      this.name = name;
      this.lightLevel = light;
      this.spinSpeed = spin;
      this.capableOfSpawning = spawning;
   }

   public boolean hasSpinningMob() {
      return this.spinSpeed >= 0.0;
   }

   public String getSerializedName() {
      return this.name;
   }
}
