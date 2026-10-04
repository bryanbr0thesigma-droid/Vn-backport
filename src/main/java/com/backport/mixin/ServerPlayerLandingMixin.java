package com.backport.mixin;

import com.backport.advancement.ExplosionFall;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerLandingMixin {
   @Inject(method = "doCheckFallDamage", at = @At("HEAD"))
   private void backport$landed(double dx, double dy, double dz, boolean onGround, CallbackInfo ci) {
      ServerPlayer self = (ServerPlayer) (Object) this;
      if (onGround) {
         ExplosionFall.landed(self);
      } else if (self.isInWater() || self.getAbilities().flying) {
         ExplosionFall.forget(self);
      }
   }
}
