package com.backport;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class MaceEnchantments {
   public static final Enchantment DENSITY = register("density", new MaceEnchantment(Enchantment.Rarity.UNCOMMON, 5, 5, 8, 25));
   public static final Enchantment BREACH = register("breach", new MaceEnchantment(Enchantment.Rarity.RARE, 4, 15, 9, 65));
   public static final Enchantment WIND_BURST = register("wind_burst", new MaceEnchantment(Enchantment.Rarity.RARE, 3, 15, 9, 65));

   private MaceEnchantments() {
   }

   public static void init() {
   }

   private static Enchantment register(String name, Enchantment e) {
      return Registry.register(BuiltInRegistries.ENCHANTMENT, Backport.id(name), e);
   }

   public static final class MaceEnchantment extends Enchantment {
      private final int maxLevel;
      private final int minBase;
      private final int perLevel;
      private final int maxBase;

      MaceEnchantment(Enchantment.Rarity rarity, int maxLevel, int minBase, int perLevel, int maxBase) {
         super(rarity, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
         this.maxLevel = maxLevel;
         this.minBase = minBase;
         this.perLevel = perLevel;
         this.maxBase = maxBase;
      }

      public int getMaxLevel() {
         return this.maxLevel;
      }

      public int getMinCost(int level) {
         return this.minBase + (level - 1) * this.perLevel;
      }

      public int getMaxCost(int level) {
         return this.maxBase + (level - 1) * this.perLevel;
      }

      public boolean canEnchant(ItemStack stack) {
         return stack.getItem() instanceof MaceItem;
      }

      protected boolean checkCompatibility(Enchantment other) {
         if (other instanceof DamageEnchantment || other instanceof MaceEnchantment) return other == this;
         return super.checkCompatibility(other);
      }
   }

   public static int level(Enchantment e, LivingEntity entity) {
      return EnchantmentHelper.getItemEnchantmentLevel(e, entity.getMainHandItem());
   }

   /** Rescales damage so that the target's armor is weakened by the Breach level. */
   public static float breach(float amount, LivingEntity target, int level) {
      if (level <= 0) return amount;
      float armor = (float) target.getArmorValue();
      float tough = (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
      float weakened = armor * Math.max(0.0F, 1.0F - 0.15F * level);
      float now = CombatRules.getDamageAfterAbsorb(amount, armor, tough);
      float then = CombatRules.getDamageAfterAbsorb(amount, weakened, tough);
      return now <= 0.0F ? amount : amount * then / now;
   }
}
