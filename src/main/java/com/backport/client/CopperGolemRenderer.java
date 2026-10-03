package com.backport.client;

import com.backport.Backport;
import com.backport.entity.CopperGolem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class CopperGolemRenderer extends MobRenderer<CopperGolem, CopperGolemModel> {
   private static final String[] SUFFIX = {"", "_exposed", "_weathered", "_oxidized"};

   public CopperGolemRenderer(EntityRendererProvider.Context context) {
      super(context, new CopperGolemModel(context.bakeLayer(BackportClient.COPPER_GOLEM_LAYER)), 0.5F);
      this.addLayer(new EyesLayer(this));
      this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
   }

   public ResourceLocation getTextureLocation(CopperGolem golem) {
      return Backport.id("textures/entity/copper_golem/copper_golem" + SUFFIX[golem.getWeatherState().ordinal()] + ".png");
   }

   private static final class EyesLayer extends RenderLayer<CopperGolem, CopperGolemModel> {
      EyesLayer(RenderLayerParent<CopperGolem, CopperGolemModel> parent) {
         super(parent);
      }

      public void render(PoseStack pose, MultiBufferSource buffer, int light, CopperGolem golem, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
         ResourceLocation eyes = Backport.id("textures/entity/copper_golem/copper_golem_eyes" + SUFFIX[golem.getWeatherState().ordinal()] + ".png");
         VertexConsumer consumer = buffer.getBuffer(RenderType.eyes(eyes));
         this.getParentModel().renderToBuffer(pose, consumer, 15728640, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      }
   }
}
