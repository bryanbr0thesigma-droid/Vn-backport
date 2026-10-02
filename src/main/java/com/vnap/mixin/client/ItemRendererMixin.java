package com.vnap.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vnap.client.VillagerNewsItemModels;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemRenderer.class})
public abstract class ItemRendererMixin {
   @Unique
   private boolean vnap$swapping;

   @Inject(
      method = {"render"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void vnap$swapDisplayContextModel(
      ItemStack stack,
      ItemDisplayContext context,
      boolean leftHand,
      PoseStack poseStack,
      MultiBufferSource buffer,
      int light,
      int overlay,
      BakedModel model,
      CallbackInfo ci
   ) {
      if (!this.vnap$swapping) {
         BakedModel variant = VillagerNewsItemModels.variant(stack, context);
         if (variant != null) {
            this.vnap$swapping = true;

            try {
               ((ItemRenderer)(Object)this).render(stack, context, leftHand, poseStack, buffer, light, overlay, variant);
            } finally {
               this.vnap$swapping = false;
            }

            ci.cancel();
         }
      }
   }
}
