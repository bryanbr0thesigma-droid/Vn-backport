package com.backport.mixin;

import com.backport.SpearItem;
import com.backport.spear.SpearJab;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** With a spear in hand the attack key is always a piercing jab, never a block break or a single-target hit. */
@Mixin(Minecraft.class)
public abstract class MinecraftSpearMixin {
   @Shadow
   public LocalPlayer player;
   @Shadow
   protected int missTime;

   @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
   private void backport$spearJab(CallbackInfoReturnable<Boolean> cir) {
      LocalPlayer p = this.player;
      if (p == null || this.missTime > 0 || p.isSpectator() || p.isHandsBusy() || !(p.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof SpearItem)) {
         return;
      }
      if (p.getAttackStrengthScale(0.5F) >= 1.0F) {
         ClientPlayNetworking.send(SpearJab.ID, SpearJab.packet());
         p.resetAttackStrengthTicker();
         p.swing(InteractionHand.MAIN_HAND);
      }
      cir.setReturnValue(false);
   }
}
