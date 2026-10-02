package com.vnap.mixin;

import com.vnap.dialogue.ContextualDialogueController;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fabric API for 1.20.1 has no after-damage event, so the hurt result is observed directly. */
@Mixin({LivingEntity.class})
public abstract class LivingEntityDamageMixin {
   @Inject(
      method = {"hurt"},
      at = {@At("RETURN")}
   )
   private void vnap$afterDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
      LivingEntity entity = (LivingEntity)(Object)this;
      if (cir.getReturnValueZ() && !entity.level().isClientSide) {
         ContextualDialogueController.onDamage(entity, source, amount, amount, entity.isBlocking());
      }
   }
}
