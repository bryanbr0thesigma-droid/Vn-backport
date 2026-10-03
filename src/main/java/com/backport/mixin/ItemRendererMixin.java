package com.backport.mixin;

import com.backport.SpearItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
   @Unique
   private boolean backport$swapping;

   @Inject(method = "render", at = @At("HEAD"), cancellable = true)
   private void backport$spearIcon(ItemStack stack, ItemDisplayContext ctx, boolean left, PoseStack pose, MultiBufferSource buffer, int light, int overlay, BakedModel model, CallbackInfo ci) {
      if (this.backport$swapping || !(stack.getItem() instanceof SpearItem)) return;
      if (ctx != ItemDisplayContext.GUI && ctx != ItemDisplayContext.GROUND && ctx != ItemDisplayContext.FIXED) return;
      net.minecraft.resources.ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
      BakedModel icon = Minecraft.getInstance().getModelManager().getModel(new ModelResourceLocation(id.getNamespace(), id.getPath() + "_icon", "inventory"));
      this.backport$swapping = true;
      try {
         ((ItemRenderer) (Object) this).render(stack, ctx, left, pose, buffer, light, overlay, icon);
      } finally {
         this.backport$swapping = false;
      }
      ci.cancel();
   }
}
