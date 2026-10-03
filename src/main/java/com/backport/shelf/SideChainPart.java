package com.backport.shelf;

import net.minecraft.util.StringRepresentable;

public enum SideChainPart implements StringRepresentable {
   UNCONNECTED("unconnected"),
   RIGHT("right"),
   CENTER("center"),
   LEFT("left");

   private final String name;

   SideChainPart(String name) {
      this.name = name;
   }

   public String getSerializedName() {
      return this.name;
   }

   public String toString() {
      return this.name;
   }

   public boolean isConnected() {
      return this != UNCONNECTED;
   }

   public boolean isConnectionTowards(SideChainPart end) {
      return this == CENTER || this == end;
   }

   public boolean isChainEnd() {
      return this != CENTER;
   }

   public SideChainPart whenConnectedToTheRight() {
      return switch (this) {
         case UNCONNECTED, LEFT -> LEFT;
         case RIGHT, CENTER -> CENTER;
      };
   }

   public SideChainPart whenConnectedToTheLeft() {
      return switch (this) {
         case UNCONNECTED, RIGHT -> RIGHT;
         case CENTER, LEFT -> CENTER;
      };
   }

   public SideChainPart whenDisconnectedFromTheRight() {
      return switch (this) {
         case UNCONNECTED, LEFT -> UNCONNECTED;
         case RIGHT, CENTER -> RIGHT;
      };
   }

   public SideChainPart whenDisconnectedFromTheLeft() {
      return switch (this) {
         case UNCONNECTED, RIGHT -> UNCONNECTED;
         case CENTER, LEFT -> LEFT;
      };
   }
}
