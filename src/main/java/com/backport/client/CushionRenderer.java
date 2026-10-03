package com.backport.client;

import com.backport.Backport;
import com.backport.entity.Cushion;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class CushionRenderer extends EntityRenderer<Cushion> {
   private final ModelPart model;

   public CushionRenderer(EntityRendererProvider.Context ctx) {
      super(ctx);
      this.model = ctx.bakeLayer(BackportClient.CUSHION_LAYER);
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      mesh.getRoot().addOrReplaceChild("cushion", CubeListBuilder.create().texOffs(0, 0).addBox(-31.0F, -4.0F, -1.0F, 16.0F, 4.0F, 16.0F, new CubeDeformation(-0.005F)), PartPose.offset(23.0F, 4.0F, -7.0F));
      return LayerDefinition.create(mesh, 64, 64);
   }

   public ResourceLocation getTextureLocation(Cushion e) {
      return Backport.id("textures/entity/cushion/" + e.getColor().getName() + "_cushion.png");
   }

   public void render(Cushion e, float yaw, float partial, PoseStack pose, MultiBufferSource buffer, int light) {
      pose.pushPose();
      pose.mulPose(Axis.YP.rotationDegrees(180.0F - Direction.fromYRot(e.getYRot()).toYRot()));
      pose.mulPose(Axis.XP.rotationDegrees(180.0F));
      pose.translate(0.0, -0.25, 0.0);
      this.model.render(pose, buffer.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(e))), light, OverlayTexture.NO_OVERLAY);
      pose.popPose();
      super.render(e, yaw, partial, pose, buffer, light);
   }
}
