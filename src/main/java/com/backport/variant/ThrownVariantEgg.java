package com.backport.variant;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class ThrownVariantEgg extends ThrownEgg {
   public ThrownVariantEgg(EntityType<? extends ThrownEgg> type, Level level) {
      super(type, level);
   }

   @Override
   protected Item getDefaultItem() {
      return Variants.BLUE_EGG;
   }

   @Override
   protected void onHit(HitResult hit) {
      if (this.level().isClientSide) {
         return;
      }
      if (this.random.nextInt(8) == 0) {
         int n = this.random.nextInt(32) == 0 ? 4 : 1;
         int variant = this.getItem().is(Variants.BROWN_EGG) ? Variants.WARM : Variants.COLD;
         for (int i = 0; i < n; i++) {
            Chicken chicken = EntityType.CHICKEN.create(this.level());
            if (chicken != null) {
               chicken.setAge(-24000);
               ((VariantHolder) chicken).backport$setVariant(variant);
               chicken.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
               this.level().addFreshEntity(chicken);
            }
         }
      }
      this.level().broadcastEntityEvent(this, (byte) 3);
      this.discard();
   }
}
