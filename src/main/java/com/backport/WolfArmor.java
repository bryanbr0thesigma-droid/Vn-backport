package com.backport;

import com.palegarden.WolfVariants;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.ItemStack;

/** Wolf body-armour helpers (the armour itself is stored on the wolf by WolfMixin). */
public final class WolfArmor {
   public static final float DEFENSE = 11.0F;

   private WolfArmor() {
   }

   public static ItemStack get(Wolf wolf) {
      return ((WolfVariants.Holder2)wolf).palegarden$getArmor();
   }

   public static void set(Wolf wolf, ItemStack stack) {
      ((WolfVariants.Holder2)wolf).palegarden$setArmor(stack);
   }

   public static float absorb(Wolf wolf, float amount) {
      ItemStack armor = get(wolf);
      if (armor.isEmpty()) {
         return amount;
      }

      float reduced = CombatRules.getDamageAfterAbsorb(amount, DEFENSE, 0.0F);
      ItemStack copy = armor.copy();
      int damage = Math.max(1, (int)(amount / 4.0F));
      copy.setDamageValue(copy.getDamageValue() + damage);
      if (copy.getDamageValue() >= copy.getMaxDamage()) {
         wolf.playSound(BackportSounds.ITEM_WOLF_ARMOR_BREAK, 1.0F, 1.0F);
         set(wolf, ItemStack.EMPTY);
      } else {
         wolf.playSound(BackportSounds.ITEM_WOLF_ARMOR_DAMAGE, 1.0F, 1.0F);
         set(wolf, copy);
      }

      return reduced;
   }
}
