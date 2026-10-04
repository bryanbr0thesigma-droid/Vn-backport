package com.backport.mixin;

import com.vnap.client.HandbookScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A Guidebook button on Minecraft's Options screen, beside the Done button. */
@Mixin(OptionsScreen.class)
public abstract class OptionsScreenGuidebookMixin {
   @Inject(method = "init", at = @At("TAIL"))
   private void backport$guidebookButton(CallbackInfo ci) {
      Screen self = (Screen) (Object) this;
      AbstractWidget done = null;
      for (GuiEventListener child : self.children()) {
         if (child instanceof AbstractWidget widget && (done == null || widget.getY() >= done.getY())) {
            done = widget;
         }
      }
      if (done == null) {
         return;
      }
      Button button = Button.builder(Component.translatable("vanilla_extended.guidebook"), b -> Minecraft.getInstance().setScreen(HandbookScreen.settingsScreen(self)))
         .bounds(done.getX() + done.getWidth() + 6, done.getY(), 100, 20).build();
      ((ScreenInvoker) self).backport$addRenderableWidget(button);
   }
}
