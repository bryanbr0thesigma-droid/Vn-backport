package com.palegarden.entity;

import com.palegarden.PaleEntities;
import com.palegarden.PaleItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class PaleBoat extends Boat {
   public PaleBoat(EntityType<? extends Boat> type, Level level) {
      super(type, level);
   }

   public PaleBoat(Level level, double x, double y, double z) {
      this(PaleEntities.PALE_OAK_BOAT, level);
      this.setPos(x, y, z);
      this.xo = x;
      this.yo = y;
      this.zo = z;
   }

   public Item getDropItem() {
      return PaleItems.PALE_OAK_BOAT;
   }
}
