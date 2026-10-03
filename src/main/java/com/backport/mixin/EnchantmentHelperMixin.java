package com.backport.mixin;

import com.backport.MaceEnchantments;
import com.backport.MaceItem;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
   @Inject(method = "getAvailableEnchantmentResults", at = @At("RETURN"))
   private static void backport$maceEnchants(int power, ItemStack stack, boolean treasure, CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
      if (!(stack.getItem() instanceof MaceItem)) return;
      List<EnchantmentInstance> list = cir.getReturnValue();
      for (Enchantment e : new Enchantment[]{MaceEnchantments.DENSITY, MaceEnchantments.BREACH, MaceEnchantments.WIND_BURST}) {
         for (int lvl = e.getMaxLevel(); lvl >= e.getMinLevel(); lvl--) {
            if (power >= e.getMinCost(lvl) && power <= e.getMaxCost(lvl)) {
               list.add(new EnchantmentInstance(e, lvl));
               break;
            }
         }
      }
   }
}
