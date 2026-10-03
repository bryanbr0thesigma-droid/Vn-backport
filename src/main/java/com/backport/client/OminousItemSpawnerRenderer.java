package com.backport.client;

import com.backport.entity.OminousItemSpawner;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

public class OminousItemSpawnerRenderer extends EntityRenderer<OminousItemSpawner> {
   public OminousItemSpawnerRenderer(EntityRendererProvider.Context ctx) {
      super(ctx);
   }

   @Override
   public void render(OminousItemSpawner e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int light) {
      if (e.getItem().isEmpty()) {
         return;
      }
      ps.pushPose();
      float age = e.tickCount + pt;
      ps.translate(0.0F, 0.1F + Math.sin(age * 0.1F) * 0.05F, 0.0F);
      ps.mulPose(Axis.YP.rotationDegrees(age * 4.0F));
      ps.scale(1.2F, 1.2F, 1.2F);
      Minecraft.getInstance().getItemRenderer().renderStatic(e.getItem(), ItemDisplayContext.GROUND, 15728880, OverlayTexture.NO_OVERLAY, ps, buf, e.level(), e.getId());
      ps.popPose();
   }

   @Override
   public ResourceLocation getTextureLocation(OminousItemSpawner e) {
      return TextureAtlas.LOCATION_BLOCKS;
   }
}
