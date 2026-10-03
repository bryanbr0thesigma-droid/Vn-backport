package com.backport.client;

import com.backport.trial.TrialSpawnerBlockEntity;
import com.backport.trial.VaultBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class TrialRenderers {
   private TrialRenderers() {
   }

   public static final class Spawner implements BlockEntityRenderer<TrialSpawnerBlockEntity> {
      private final net.minecraft.client.renderer.entity.EntityRenderDispatcher dispatcher;

      public Spawner(BlockEntityRendererProvider.Context ctx) {
         this.dispatcher = ctx.getEntityRenderer();
      }

      public void render(TrialSpawnerBlockEntity be, float partial, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
         if (be.getLevel() == null) return;
         Entity entity = be.getDisplayEntity(be.getLevel());
         if (entity == null) return;
         float scale = 0.53125F;
         float size = Math.max(entity.getBbWidth(), entity.getBbHeight());
         if (size > 1.0F) scale /= size;
         pose.pushPose();
         pose.translate(0.5F, 0.4F, 0.5F);
         pose.mulPose(Axis.YP.rotationDegrees((float) Mth.lerp(partial, be.oSpin, be.spin) * 10.0F));
         pose.translate(0.0F, -0.2F, 0.0F);
         pose.mulPose(Axis.XP.rotationDegrees(-30.0F));
         pose.scale(scale, scale, scale);
         this.dispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, partial, pose, buffer, light);
         pose.popPose();
      }
   }

   public static final class Vault implements BlockEntityRenderer<VaultBlockEntity> {
      public Vault(BlockEntityRendererProvider.Context ctx) {
      }

      public void render(VaultBlockEntity be, float partial, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
         ItemStack stack = be.getDisplayItem();
         if (stack.isEmpty() || be.getLevel() == null) return;
         pose.pushPose();
         pose.translate(0.5F, 0.4F, 0.5F);
         pose.mulPose(Axis.YP.rotationDegrees(Mth.rotLerp(partial, be.oSpin, be.spin)));
         pose.scale(0.5F, 0.5F, 0.5F);
         Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buffer, be.getLevel(), 0);
         pose.popPose();
      }
   }
}
