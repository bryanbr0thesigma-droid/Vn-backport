package com.backport.crafter;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public class CrafterMenu extends AbstractContainerMenu {
   public static final MenuType<CrafterMenu> TYPE = new MenuType<>(CrafterMenu::new, FeatureFlags.VANILLA_SET);
   public static final ResourceLocation TOGGLE_PACKET = com.backport.Backport.id("crafter_slot");
   private final ResultContainer resultContainer = new ResultContainer();
   private final ContainerData containerData;
   private final Player player;
   private final CraftingContainer container;

   public CrafterMenu(int id, Inventory inventory) {
      super(TYPE, id);
      this.player = inventory.player;
      this.containerData = new SimpleContainerData(10);
      this.container = new TransientCraftingContainer(this, 3, 3);
      this.addSlots(inventory);
   }

   public CrafterMenu(int id, Inventory inventory, CraftingContainer container, ContainerData data) {
      super(TYPE, id);
      this.player = inventory.player;
      this.containerData = data;
      this.container = container;
      checkContainerSize(container, 9);
      container.startOpen(inventory.player);
      this.addSlots(inventory);
   }

   public static void registerServer() {
      net.minecraft.core.Registry.register(net.minecraft.core.registries.BuiltInRegistries.MENU, com.backport.Backport.id("crafter"), TYPE);
      ServerPlayNetworking.registerGlobalReceiver(TOGGLE_PACKET, (server, player, handler, buf, sender) -> {
         int cid = buf.readInt();
         int slot = buf.readInt();
         boolean enabled = buf.readBoolean();
         server.execute(() -> {
            if (player.containerMenu.containerId == cid && player.containerMenu instanceof CrafterMenu m && slot >= 0 && slot < 9) {
               m.setSlotState(slot, enabled);
            }
         });
      });
   }

   private void addSlots(Inventory inventory) {
      for (int y = 0; y < 3; y++)
         for (int x = 0; x < 3; x++)
            this.addSlot(new CrafterSlot(this.container, x + y * 3, 26 + x * 18, 17 + y * 18, this));
      for (int y = 0; y < 3; y++)
         for (int x = 0; x < 9; x++)
            this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
      for (int x = 0; x < 9; x++)
         this.addSlot(new Slot(inventory, x, 8 + x * 18, 142));
      this.addSlot(new NonInteractiveResultSlot(this.resultContainer, 0, 134, 35));
      this.addDataSlots(this.containerData);
      this.refreshRecipeResult();
   }

   public void setSlotState(int slotId, boolean enabled) {
      CrafterSlot slot = (CrafterSlot) this.getSlot(slotId);
      this.containerData.set(slot.index, enabled ? 0 : 1);
      this.broadcastChanges();
   }

   public boolean isSlotDisabled(int slot) {
      return slot > -1 && slot < 9 && this.containerData.get(slot) == 1;
   }

   public boolean isPowered() {
      return this.containerData.get(9) == 1;
   }

   public ItemStack quickMoveStack(Player player, int index) {
      ItemStack clicked = ItemStack.EMPTY;
      Slot slot = this.slots.get(index);
      if (slot != null && slot.hasItem()) {
         ItemStack stack = slot.getItem();
         clicked = stack.copy();
         if (index < 9) {
            if (!this.moveItemStackTo(stack, 9, 45, true)) return ItemStack.EMPTY;
         } else if (!this.moveItemStackTo(stack, 0, 9, false)) {
            return ItemStack.EMPTY;
         }
         if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
         else slot.setChanged();
         if (stack.getCount() == clicked.getCount()) return ItemStack.EMPTY;
         slot.onTake(player, stack);
      }
      return clicked;
   }

   public boolean stillValid(Player player) {
      return this.container.stillValid(player);
   }

   private void refreshRecipeResult() {
      if (this.player instanceof ServerPlayer sp) {
         ItemStack result = CrafterBlock.getPotentialResults(sp.level(), this.container).map(r -> r.assemble(this.container, sp.level().registryAccess())).orElse(ItemStack.EMPTY);
         this.resultContainer.setItem(0, result);
      }
   }

   public void slotsChanged(Container c) {
      super.slotsChanged(c);
      this.refreshRecipeResult();
   }
}
