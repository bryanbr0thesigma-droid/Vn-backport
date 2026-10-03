package com.backport.mixin;

import com.backport.variant.VariantHolder;
import com.backport.variant.Variants;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Chicken.class)
public abstract class ChickenEggMixin {
   @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Chicken;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"))
   private ItemEntity backport$layVariantEgg(Chicken self, ItemLike egg) {
      int v = ((VariantHolder) self).backport$getVariant();
      return self.spawnAtLocation(v == Variants.COLD ? Variants.BLUE_EGG : v == Variants.WARM ? Variants.BROWN_EGG : egg);
   }
}
