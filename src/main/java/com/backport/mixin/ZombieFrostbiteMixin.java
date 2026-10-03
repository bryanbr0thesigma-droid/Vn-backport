package com.backport.mixin;

import com.backport.ice.Frostbite;
import com.backport.ice.IceCaves;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Zombies and husks stewing in powder snow for 30 seconds turn into frostbites. */
@Mixin(Zombie.class)
public abstract class ZombieFrostbiteMixin {
   @Unique
   private int backport$snowTicks;

   @Inject(method = "tick", at = @At("TAIL"))
   private void backport$frostbiteConversion(CallbackInfo ci) {
      Zombie self = (Zombie) (Object) this;
      if (self.level().isClientSide || self instanceof Frostbite || !self.isAlive()) {
         return;
      }
      if (self.isInPowderSnow) {
         if (++this.backport$snowTicks >= 600) {
            this.backport$snowTicks = 0;
            self.convertTo(IceCaves.FROSTBITE, true);
         }
      } else if (this.backport$snowTicks > 0) {
         this.backport$snowTicks--;
      }
   }
}
