package com.backport.client;

import com.backport.shelf.ShelfBlock;
import com.backport.shelf.ShelfBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ShelfRenderer implements BlockEntityRenderer<ShelfBlockEntity> {
   public ShelfRenderer(BlockEntityRendererProvider.Context ctx) {
   }

   public void render(ShelfBlockEntity be, float partial, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
      if (be.getLevel() == null) return;
      float yRot = -be.getBlockState().getValue(ShelfBlock.FACING).toYRot();
      for (int slot = 0; slot < 3; slot++) {
         ItemStack stack = be.getItem(slot);
         if (stack.isEmpty()) continue;
         pose.pushPose();
         pose.translate(0.5F, 0.5F, 0.5F);
         pose.mulPose(Axis.YP.rotationDegrees(yRot));
         pose.translate((slot - 1) * 0.3125F, 0.0F, -0.25F);
         pose.scale(0.3F, 0.3F, 0.3F);
         Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buffer, be.getLevel(), (int) be.getBlockPos().asLong() + slot);
         pose.popPose();
      }
   }
}
