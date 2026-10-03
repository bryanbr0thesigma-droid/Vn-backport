package com.backport.client;

import com.backport.entity.HappyGhast;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class HappyGhastHarnessModel extends EntityModel<HappyGhast> {
   private final ModelPart root;
   private final ModelPart goggles;

   public HappyGhastHarnessModel(ModelPart root) {
      this.root = root;
      this.goggles = root.getChild("goggles");
   }

   public static LayerDefinition createHarnessLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild("harness", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 16.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
      root.addOrReplaceChild("goggles", CubeListBuilder.create().texOffs(0, 32).addBox(-8.0F, -2.5F, -2.5F, 16.0F, 5.0F, 5.0F, new CubeDeformation(0.15F)), PartPose.offset(0.0F, 14.0F, -5.5F));
      return LayerDefinition.create(mesh, 64, 64);
   }

   public void setupAnim(HappyGhast entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      if (entity.isVehicle()) {
         this.goggles.xRot = 0.0F;
         this.goggles.y = 14.0F;
      } else {
         this.goggles.xRot = -0.7854F;
         this.goggles.y = 9.0F;
      }
   }

   public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, float r, float g, float b, float a) {
      this.root.render(pose, buffer, light, overlay, r, g, b, a);
   }
}
