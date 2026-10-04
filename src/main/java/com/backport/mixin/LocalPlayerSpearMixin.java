package com.backport.mixin;

import com.backport.SpearItem;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Charging a spear does not slow the player or cancel sprinting (26.x use_effects). */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerSpearMixin {
   @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
   private boolean backport$noSlowdown(boolean using) {
      return using && !(((LocalPlayer) (Object) this).getUseItem().getItem() instanceof SpearItem);
   }

   @ModifyExpressionValue(method = "canStartSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
   private boolean backport$canSprint(boolean using) {
      return using && !(((LocalPlayer) (Object) this).getUseItem().getItem() instanceof SpearItem);
   }
}
