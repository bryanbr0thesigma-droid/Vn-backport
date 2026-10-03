package com.backport.crafter;

import java.util.Optional;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class NonInteractiveResultSlot extends Slot {
   public NonInteractiveResultSlot(Container c, int id, int x, int y) {
      super(c, id, x, y);
   }

   public void onQuickCraft(ItemStack a, ItemStack b) {
   }

   public boolean mayPickup(Player p) {
      return false;
   }

   public Optional<ItemStack> tryRemove(int a, int b, Player p) {
      return Optional.empty();
   }

   public ItemStack safeTake(int a, int b, Player p) {
      return ItemStack.EMPTY;
   }

   public ItemStack safeInsert(ItemStack s) {
      return s;
   }

   public ItemStack safeInsert(ItemStack s, int n) {
      return s;
   }

   public boolean allowModification(Player p) {
      return false;
   }

   public boolean mayPlace(ItemStack s) {
      return false;
   }

   public ItemStack remove(int n) {
      return ItemStack.EMPTY;
   }

   public void onTake(Player p, ItemStack s) {
   }
}
