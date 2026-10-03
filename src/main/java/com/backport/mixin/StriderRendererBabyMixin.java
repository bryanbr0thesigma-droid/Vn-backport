package com.backport.mixin;

import com.backport.client.BabyModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.StriderRenderer;
import net.minecraft.world.entity.monster.Strider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The 26.x baby strider mesh is already small; skip vanilla's extra 0.5 baby scale. */
@Mixin(StriderRenderer.class)
public abstract class StriderRendererBabyMixin {
   @Inject(method = "scale(Lnet/minecraft/world/entity/monster/Strider;Lcom/mojang/blaze3d/vertex/PoseStack;F)V", at = @At("HEAD"), cancellable = true)
   private void backport$noExtraScale(Strider strider, PoseStack ps, float pt, CallbackInfo ci) {
      if (BabyModels.active && strider.isBaby()) {
         ci.cancel();
      }
   }
}
