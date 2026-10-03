package com.backport.client;

import com.backport.crafter.CrafterMenu;
import com.backport.crafter.CrafterSlot;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

public class CrafterScreen extends AbstractContainerScreen<CrafterMenu> {
   private static final ResourceLocation BG = new ResourceLocation("backport", "textures/gui/container/crafter.png");
   private static final ResourceLocation DISABLED = new ResourceLocation("backport", "textures/gui/crafter/disabled_slot.png");
   private static final ResourceLocation POWERED = new ResourceLocation("backport", "textures/gui/crafter/powered_redstone.png");
   private static final ResourceLocation UNPOWERED = new ResourceLocation("backport", "textures/gui/crafter/unpowered_redstone.png");

   public CrafterScreen(CrafterMenu menu, Inventory inv, Component title) {
      super(menu, inv, title);
   }

   protected void init() {
      super.init();
      this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
   }

   protected void slotClicked(Slot slot, int slotId, int button, ClickType type) {
      if (slot instanceof CrafterSlot && !slot.hasItem() && !this.minecraft.player.isSpectator() && this.menu.getCarried().isEmpty()) {
         boolean enable = this.menu.isSlotDisabled(slotId);
         FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
         buf.writeInt(this.menu.containerId);
         buf.writeInt(slotId);
         buf.writeBoolean(enable);
         ClientPlayNetworking.send(CrafterMenu.TOGGLE_PACKET, buf);
         this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, enable ? 1.0F : 0.8F));
         return;
      }
      super.slotClicked(slot, slotId, button, type);
   }

   public void render(GuiGraphics g, int mx, int my, float partial) {
      super.render(g, mx, my, partial);
      this.renderTooltip(g, mx, my);
   }

   protected void renderBg(GuiGraphics g, float partial, int mx, int my) {
      g.blit(BG, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
      for (int i = 0; i < 9; i++) {
         Slot s = this.menu.slots.get(i);
         if (this.menu.isSlotDisabled(i)) {
            g.blit(DISABLED, this.leftPos + s.x, this.topPos + s.y, 0, 0, 16, 16, 16, 16);
         }
      }
      g.blit(this.menu.isPowered() ? POWERED : UNPOWERED, this.leftPos + 97, this.topPos + 35, 0, 0, 16, 16, 16, 16);
   }
}
