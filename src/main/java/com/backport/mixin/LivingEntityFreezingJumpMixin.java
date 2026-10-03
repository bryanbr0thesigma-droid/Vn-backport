package com.backport.mixin;

import com.backport.ice.IceCaves;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Freezing weakens jumps as the entity freezes (down to 60% jump power when fully frozen). */
@Mixin(LivingEntity.class)
public abstract class LivingEntityFreezingJumpMixin {
   @Inject(method = "getJumpPower", at = @At("RETURN"), cancellable = true)
   private void backport$freezingJump(CallbackInfoReturnable<Float> cir) {
      LivingEntity self = (LivingEntity) (Object) this;
      if (self.hasEffect(IceCaves.FREEZING) && self.canFreeze()) {
         double pct = Math.min(1.0, self.getTicksFrozen() / (double) self.getTicksRequiredToFreeze());
         cir.setReturnValue((float) (cir.getReturnValueF() * (1.0 - 0.4 * pct)));
      }
   }
}
