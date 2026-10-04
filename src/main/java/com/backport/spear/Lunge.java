package com.backport.spear;

import com.backport.Backport;
import com.backport.SpearItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class Lunge extends Enchantment {
   public static final Lunge INSTANCE = new Lunge();

   private Lunge() {
      super(Rarity.UNCOMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
   }

   public static void init() {
      Registry.register(BuiltInRegistries.ENCHANTMENT, Backport.id("lunge"), INSTANCE);
   }

   @Override
   public int getMinCost(int level) {
      return 5 + (level - 1) * 8;
   }

   @Override
   public int getMaxCost(int level) {
      return 25 + (level - 1) * 8;
   }

   @Override
   public int getMaxLevel() {
      return 3;
   }

   @Override
   public boolean canEnchant(ItemStack stack) {
      return stack.getItem() instanceof SpearItem;
   }
}
