package com.palegarden.mixin;

import com.palegarden.WolfVariants;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WolfRenderer.class)
public abstract class WolfRendererMixin {
   @Inject(method = "getTextureLocation(Lnet/minecraft/world/entity/animal/Wolf;)Lnet/minecraft/resources/ResourceLocation;", at = @At("HEAD"), cancellable = true)
   private void palegarden$variantTexture(Wolf wolf, CallbackInfoReturnable<ResourceLocation> cir) {
      int variant = WolfVariants.of(wolf);
      if (variant > 0) {
         cir.setReturnValue(WolfVariants.texture(variant, wolf.isAngry(), wolf.isTame()));
      }
   }
}
