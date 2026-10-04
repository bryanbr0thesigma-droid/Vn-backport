package com.backport.mixin;

import com.backport.client.LocatorBarClient;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The locator bar takes the experience bar's slot, while the level number stays. */
@Mixin(Gui.class)
public abstract class GuiLocatorBarMixin {
   @WrapWithCondition(method = "renderExperienceBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"))
   private boolean backport$hideXpBar(GuiGraphics g, ResourceLocation tex, int x, int y, int u, int v, int w, int h) {
      return !LocatorBarClient.active();
   }

   @Inject(method = "renderExperienceBar", at = @At("RETURN"))
   private void backport$drawLocator(GuiGraphics g, int x, CallbackInfo ci) {
      if (LocatorBarClient.active()) {
         LocatorBarClient.render(g, net.minecraft.client.Minecraft.getInstance().getFrameTime());
      }
   }
}
