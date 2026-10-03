package com.palegarden.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.palegarden.PaleGarden;
import com.palegarden.entity.Creaking;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class CreakingRenderer extends MobRenderer<Creaking, CreakingModel> {
   private static final ResourceLocation TEXTURE = PaleGarden.id("textures/entity/creaking/creaking.png");
   private static final ResourceLocation EYES = PaleGarden.id("textures/entity/creaking/creaking_eyes.png");

   public CreakingRenderer(EntityRendererProvider.Context context) {
      super(context, new CreakingModel(context.bakeLayer(PaleGardenClient.CREAKING_LAYER)), 0.6F);
      this.addLayer(new EyesLayer(this));
   }

   public ResourceLocation getTextureLocation(Creaking creaking) {
      return TEXTURE;
   }

   protected boolean isShaking(Creaking creaking) {
      return false;
   }

   private static final class EyesLayer extends RenderLayer<Creaking, CreakingModel> {
      EyesLayer(RenderLayerParent<Creaking, CreakingModel> parent) {
         super(parent);
      }

      public void render(PoseStack pose, MultiBufferSource buffer, int light, Creaking creaking, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
         boolean glowing = creaking.isTearingDown() ? creaking.hasGlowingEyes() : creaking.isActive();
         if (glowing) {
            CreakingModel model = this.getParentModel();
            VertexConsumer consumer = buffer.getBuffer(RenderType.eyes(EYES));
            model.setOnlyHeadVisible(true);
            model.renderToBuffer(pose, consumer, 15728640, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            model.setOnlyHeadVisible(false);
         }
      }
   }
}
