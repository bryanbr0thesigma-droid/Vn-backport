package com.palegarden.entity;

import com.palegarden.PaleEntities;
import com.palegarden.PaleItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class PaleChestBoat extends ChestBoat {
   public PaleChestBoat(EntityType<? extends ChestBoat> type, Level level) {
      super(type, level);
   }

   public PaleChestBoat(Level level, double x, double y, double z) {
      this(PaleEntities.PALE_OAK_CHEST_BOAT, level);
      this.setPos(x, y, z);
      this.xo = x;
      this.yo = y;
      this.zo = z;
   }

   public Item getDropItem() {
      return PaleItems.PALE_OAK_CHEST_BOAT;
   }
}
