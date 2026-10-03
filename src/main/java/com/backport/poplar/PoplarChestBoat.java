package com.backport.poplar;



import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class PoplarChestBoat extends ChestBoat {
   public PoplarChestBoat(EntityType<? extends ChestBoat> type, Level level) {
      super(type, level);
   }

   public PoplarChestBoat(Level level, double x, double y, double z) {
      this(PoplarBoats.CHEST_BOAT, level);
      this.setPos(x, y, z);
      this.xo = x;
      this.yo = y;
      this.zo = z;
   }

   public Item getDropItem() {
      return Poplar.CHEST_BOAT;
   }
}
