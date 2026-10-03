package com.backport;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;

/** Copper tools and armour from the Copper Age update. */
public final class CopperTools {
   public static final Tier TIER = new Tier() {
      public int getUses() {
         return 190;
      }

      public float getSpeed() {
         return 5.0F;
      }

      public float getAttackDamageBonus() {
         return 1.0F;
      }

      public int getLevel() {
         return 1;
      }

      public int getEnchantmentValue() {
         return 13;
      }

      public Ingredient getRepairIngredient() {
         return Ingredient.of(Items.COPPER_INGOT);
      }
   };

   public static final ArmorMaterial ARMOR = new ArmorMaterial() {
      private final int[] defense = {1, 3, 4, 2};

      public int getDurabilityForType(ArmorItem.Type type) {
         return new int[]{13, 15, 16, 11}[type.ordinal()] * 11;
      }

      public int getDefenseForType(ArmorItem.Type type) {
         return this.defense[type.ordinal()];
      }

      public int getEnchantmentValue() {
         return 8;
      }

      public SoundEvent getEquipSound() {
         return SoundEvents.ARMOR_EQUIP_IRON;
      }

      public Ingredient getRepairIngredient() {
         return Ingredient.of(Items.COPPER_INGOT);
      }

      public String getName() {
         return "copper";
      }

      public float getToughness() {
         return 0.0F;
      }

      public float getKnockbackResistance() {
         return 0.0F;
      }
   };

   public static final Item SWORD = Backport.item("copper_sword", new SwordItem(TIER, 3, -2.4F, new FabricItemSettings()));
   public static final Item SHOVEL = Backport.item("copper_shovel", new ShovelItem(TIER, 1.5F, -3.0F, new FabricItemSettings()));
   public static final Item PICKAXE = Backport.item("copper_pickaxe", new PickaxeItem(TIER, 1, -2.8F, new FabricItemSettings()));
   public static final Item AXE = Backport.item("copper_axe", new AxeItem(TIER, 7.0F, -3.2F, new FabricItemSettings()));
   public static final Item HOE = Backport.item("copper_hoe", new HoeItem(TIER, -1, -2.0F, new FabricItemSettings()));
   public static final Item HELMET = Backport.item("copper_helmet", new ArmorItem(ARMOR, ArmorItem.Type.HELMET, new FabricItemSettings()));
   public static final Item CHESTPLATE = Backport.item("copper_chestplate", new ArmorItem(ARMOR, ArmorItem.Type.CHESTPLATE, new FabricItemSettings()));
   public static final Item LEGGINGS = Backport.item("copper_leggings", new ArmorItem(ARMOR, ArmorItem.Type.LEGGINGS, new FabricItemSettings()));
   public static final Item BOOTS = Backport.item("copper_boots", new ArmorItem(ARMOR, ArmorItem.Type.BOOTS, new FabricItemSettings()));

   private CopperTools() {
   }

   public static void init() {
   }
}
