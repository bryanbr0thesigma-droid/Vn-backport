package com.backport.poplar;



import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class PoplarBoat extends Boat {
   public PoplarBoat(EntityType<? extends Boat> type, Level level) {
      super(type, level);
   }

   public PoplarBoat(Level level, double x, double y, double z) {
      this(PoplarBoats.BOAT, level);
      this.setPos(x, y, z);
      this.xo = x;
      this.yo = y;
      this.zo = z;
   }

   public Item getDropItem() {
      return Poplar.BOAT;
   }
}
