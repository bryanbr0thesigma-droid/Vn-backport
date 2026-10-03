package com.backport.mixin;

import com.backport.OmenEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 26.x: Bad Omen in a village becomes a 30 second Raid Omen; the raid begins when it runs out. */
@Mixin(Raids.class)
public abstract class RaidsOmenMixin {
   @Inject(method = "createOrExtendRaid", at = @At("HEAD"), cancellable = true)
   private void backport$raidOmenDelay(ServerPlayer player, CallbackInfoReturnable<Raid> cir) {
      if (OmenEffects.BYPASS_RAID_OMEN || !player.hasEffect(MobEffects.BAD_OMEN)) {
         return;
      }
      if (!player.hasEffect(OmenEffects.RAID_OMEN)) {
         MobEffectInstance bad = player.getEffect(MobEffects.BAD_OMEN);
         player.addEffect(new MobEffectInstance(OmenEffects.RAID_OMEN, 600, bad == null ? 0 : bad.getAmplifier()));
      }
      cir.setReturnValue(null);
   }
}
