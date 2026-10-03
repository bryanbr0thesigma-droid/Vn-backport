package com.backport.mixin;

import com.backport.NewItems;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DecoratedPotPatterns.class)
public abstract class DecoratedPotPatternsMixin {
   @Inject(method = "getResourceKey", at = @At("HEAD"), cancellable = true)
   private static void backport$sherds(Item item, CallbackInfoReturnable<ResourceKey<String>> cir) {
      ResourceKey<String> key = NewItems.potPatternFor(item);
      if (key != null) {
         cir.setReturnValue(key);
      }
   }
}
