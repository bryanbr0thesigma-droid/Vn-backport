package com.backport.client;

import com.backport.Backport;
import com.backport.entity.Breeze;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class BreezeRenderer extends MobRenderer<Breeze, BreezeModel> {
   private static final ResourceLocation TEXTURE = Backport.id("textures/entity/breeze/breeze.png");
   private static final ResourceLocation WIND = Backport.id("textures/entity/breeze/breeze_wind.png");
   private static final ResourceLocation EYES = Backport.id("textures/entity/breeze/breeze_eyes.png");

   public BreezeRenderer(EntityRendererProvider.Context context) {
      super(context, new BreezeModel(context.bakeLayer(BackportClient.BREEZE_LAYER)), 0.5F);
      this.addLayer(new WindLayer(this, context.getModelSet()));
      this.addLayer(new EyesLayer(this, context.getModelSet()));
   }

   public ResourceLocation getTextureLocation(Breeze breeze) {
      return TEXTURE;
   }

   private static final class WindLayer extends RenderLayer<Breeze, BreezeModel> {
      private final BreezeModel model;

      WindLayer(RenderLayerParent<Breeze, BreezeModel> parent, EntityModelSet set) {
         super(parent);
         this.model = new BreezeModel(set.bakeLayer(BackportClient.BREEZE_WIND_LAYER));
      }

      public void render(PoseStack pose, MultiBufferSource buffer, int light, Breeze breeze, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
         this.model.setupAnim(breeze, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
         this.model.renderToBuffer(pose, buffer.getBuffer(RenderType.energySwirl(WIND, (ageInTicks * 0.02F) % 1.0F, 0.0F)), 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   private static final class EyesLayer extends RenderLayer<Breeze, BreezeModel> {
      private final BreezeModel model;

      EyesLayer(RenderLayerParent<Breeze, BreezeModel> parent, EntityModelSet set) {
         super(parent);
         this.model = new BreezeModel(set.bakeLayer(BackportClient.BREEZE_EYES_LAYER));
      }

      public void render(PoseStack pose, MultiBufferSource buffer, int light, Breeze breeze, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
         this.model.setupAnim(breeze, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
         this.model.renderToBuffer(pose, buffer.getBuffer(RenderType.eyes(EYES)), 15728640, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      }
   }
}
