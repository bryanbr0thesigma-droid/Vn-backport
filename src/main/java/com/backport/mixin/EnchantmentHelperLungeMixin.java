package com.backport.mixin;

import com.backport.SpearItem;
import com.backport.spear.Lunge;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Lets the enchanting table offer Lunge on spears (the table only checks the enchantment category). */
@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperLungeMixin {
   @ModifyExpressionValue(method = "getAvailableEnchantmentResults", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentCategory;canEnchant(Lnet/minecraft/world/item/Item;)Z"))
   private static boolean backport$lungeOnSpears(boolean original, @Local(argsOnly = true) net.minecraft.world.item.ItemStack stack, @Local Enchantment enchantment) {
      if (enchantment == Lunge.INSTANCE) {
         return stack.getItem() instanceof SpearItem;
      }
      return original;
   }
}
