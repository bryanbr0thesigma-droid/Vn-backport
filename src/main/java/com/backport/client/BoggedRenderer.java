package com.backport.client;

import com.backport.Backport;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Bogged;

public class BoggedRenderer extends HumanoidMobRenderer<Bogged, BoggedModel> {
   private static final ResourceLocation TEXTURE = Backport.id("textures/entity/skeleton/bogged.png");
   private static final ResourceLocation OVERLAY = Backport.id("textures/entity/skeleton/bogged_overlay.png");

   public BoggedRenderer(EntityRendererProvider.Context context) {
      super(context, new BoggedModel(context.bakeLayer(BackportClient.BOGGED_LAYER)), 0.5F);
      this.addLayer(new HumanoidArmorLayer<>(this, new SkeletonModel<>(context.bakeLayer(ModelLayers.SKELETON_INNER_ARMOR)), new SkeletonModel<>(context.bakeLayer(ModelLayers.SKELETON_OUTER_ARMOR)), context.getModelManager()));
      this.addLayer(new OverlayLayer(this, new BoggedModel(context.bakeLayer(BackportClient.BOGGED_OUTER_LAYER))));
   }

   public ResourceLocation getTextureLocation(Bogged bogged) {
      return TEXTURE;
   }

   private static final class OverlayLayer extends RenderLayer<Bogged, BoggedModel> {
      private final BoggedModel layerModel;

      OverlayLayer(RenderLayerParent<Bogged, BoggedModel> parent, BoggedModel model) {
         super(parent);
         this.layerModel = model;
      }

      public void render(PoseStack pose, MultiBufferSource buffer, int light, Bogged bogged, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
         coloredCutoutModelCopyLayerRender(this.getParentModel(), this.layerModel, OVERLAY, pose, buffer, light, bogged, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTick, 1.0F, 1.0F, 1.0F);
      }
   }
}
