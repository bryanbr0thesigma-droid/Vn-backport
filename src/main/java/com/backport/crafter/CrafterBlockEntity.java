package com.backport.crafter;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CrafterBlockEntity extends RandomizableContainerBlockEntity implements CraftingContainer {
   public static BlockEntityType<CrafterBlockEntity> TYPE;
   private NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
   private int craftingTicksRemaining = 0;
   protected final ContainerData containerData = new ContainerData() {
      private final int[] slotStates = new int[9];
      private int triggered;

      public int get(int id) {
         return id == 9 ? this.triggered : this.slotStates[id];
      }

      public void set(int id, int value) {
         if (id == 9) this.triggered = value;
         else this.slotStates[id] = value;
      }

      public int getCount() {
         return 10;
      }
   };

   public static void register(Block block) {
      TYPE = net.minecraft.core.Registry.register(net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE, com.backport.Backport.id("crafter"), FabricBlockEntityTypeBuilder.create(CrafterBlockEntity::new, block).build());
   }

   public CrafterBlockEntity(BlockPos pos, BlockState state) {
      super(TYPE, pos, state);
   }

   protected Component getDefaultName() {
      return Component.translatable("container.crafter");
   }

   protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
      return new CrafterMenu(id, inventory, this, this.containerData);
   }

   public void setSlotState(int slot, boolean enabled) {
      if (this.slotCanBeDisabled(slot)) {
         this.containerData.set(slot, enabled ? 0 : 1);
         this.setChanged();
      }
   }

   public boolean isSlotDisabled(int slot) {
      return slot >= 0 && slot < 9 && this.containerData.get(slot) == 1;
   }

   public boolean canPlaceItem(int slot, ItemStack stack) {
      if (this.containerData.get(slot) == 1) return false;
      ItemStack cur = this.items.get(slot);
      int n = cur.getCount();
      if (n >= cur.getMaxStackSize()) return false;
      return cur.isEmpty() || !this.smallerStackExist(n, cur, slot);
   }

   private boolean smallerStackExist(int base, ItemStack item, int baseSlot) {
      for (int i = baseSlot + 1; i < 9; i++) {
         if (!this.isSlotDisabled(i)) {
            ItemStack s = this.getItem(i);
            if (s.isEmpty() || s.getCount() < base && ItemStack.isSameItemSameTags(s, item)) return true;
         }
      }
      return false;
   }

   public void load(CompoundTag tag) {
      super.load(tag);
      this.craftingTicksRemaining = tag.getInt("crafting_ticks_remaining");
      this.items = NonNullList.withSize(9, ItemStack.EMPTY);
      if (!this.tryLoadLootTable(tag)) ContainerHelper.loadAllItems(tag, this.items);
      for (int i = 0; i < 9; i++) this.containerData.set(i, 0);
      for (int i : tag.getIntArray("disabled_slots")) {
         if (this.slotCanBeDisabled(i)) this.containerData.set(i, 1);
      }
      this.containerData.set(9, tag.getInt("triggered"));
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      tag.putInt("crafting_ticks_remaining", this.craftingTicksRemaining);
      if (!this.trySaveLootTable(tag)) ContainerHelper.saveAllItems(tag, this.items);
      java.util.List<Integer> disabled = new java.util.ArrayList<>();
      for (int i = 0; i < 9; i++) if (this.isSlotDisabled(i)) disabled.add(i);
      tag.putIntArray("disabled_slots", disabled);
      tag.putInt("triggered", this.containerData.get(9));
   }

   public int getContainerSize() {
      return 9;
   }

   public boolean isEmpty() {
      for (ItemStack s : this.items) if (!s.isEmpty()) return false;
      return true;
   }

   public ItemStack getItem(int slot) {
      return this.items.get(slot);
   }

   public void setItem(int slot, ItemStack stack) {
      if (this.isSlotDisabled(slot)) this.setSlotState(slot, true);
      super.setItem(slot, stack);
   }

   public boolean stillValid(Player player) {
      return Container.stillValidBlockEntity(this, player);
   }

   public NonNullList<ItemStack> getItems() {
      return this.items;
   }

   protected void setItems(NonNullList<ItemStack> items) {
      this.items = items;
   }

   public int getWidth() {
      return 3;
   }

   public int getHeight() {
      return 3;
   }

   public void fillStackedContents(StackedContents contents) {
      for (ItemStack s : this.items) contents.accountSimpleStack(s);
   }

   public void setTriggered(boolean value) {
      this.containerData.set(9, value ? 1 : 0);
   }

   public static void serverTick(Level level, BlockPos pos, BlockState state, CrafterBlockEntity e) {
      int t = e.craftingTicksRemaining - 1;
      if (t >= 0) {
         e.craftingTicksRemaining = t;
         if (t == 0) level.setBlockAndUpdate(pos, state.setValue(CrafterBlock.CRAFTING, false));
      }
   }

   public void setCraftingTicksRemaining(int t) {
      this.craftingTicksRemaining = t;
   }

   public int getRedstoneSignal() {
      int c = 0;
      for (int i = 0; i < 9; i++) if (!this.getItem(i).isEmpty() || this.isSlotDisabled(i)) c++;
      return c;
   }

   private boolean slotCanBeDisabled(int slot) {
      return slot > -1 && slot < 9 && this.items.get(slot).isEmpty();
   }
}
