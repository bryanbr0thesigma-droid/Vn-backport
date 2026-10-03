package com.backport.entity;

import com.backport.BackportEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class BreezeWindCharge extends WindCharge {
   public BreezeWindCharge(EntityType<? extends WindCharge> type, Level level) {
      super(type, level);
   }

   public BreezeWindCharge(LivingEntity owner, Level level) {
      super(BackportEntities.BREEZE_WIND_CHARGE, owner, level);
   }
}
