package com.vnap.mixin;

import com.vnap.dialogue.ContextualDialogueController;
import net.minecraft.sounds.SoundEvent;
import com.vnap.sound.SupplementalSoundCatalog;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({AbstractVillager.class})
public abstract class AbstractVillagerMixin {
   @Inject(
      method = {"notifyTrade"},
      at = {@At("TAIL")}
   )
   private void vnap$onTradeCompleted(MerchantOffer offer, CallbackInfo ci) {
      AbstractVillager trader = (AbstractVillager)(Object)this;
      Player player = trader.getTradingPlayer();
      if (player != null) {
         ContextualDialogueController.onTradeCompleted(trader, player);
      }
   }

   @Inject(
      method = {"getNotifyTradeSound"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void vnap$removeVanillaTradeSound(CallbackInfoReturnable<SoundEvent> cir) {
      cir.setReturnValue(SupplementalSoundCatalog.EMPTY);
   }

   @Inject(
      method = {"getTradeUpdatedSound"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void vnap$removeVanillaTradeUpdatedSound(boolean sold, CallbackInfoReturnable<SoundEvent> cir) {
      cir.setReturnValue(SupplementalSoundCatalog.EMPTY);
   }

   @Inject(
      method = {"playCelebrateSound"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void vnap$removeVanillaCelebrateSound(CallbackInfo ci) {
      ci.cancel();
   }
}
