package com.backport.client;

import com.backport.entity.HappyGhast;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.ModelPart;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;

public class HappyGhastModel extends EntityModel<HappyGhast> {
   private final ModelPart root;
   private final ModelPart[] tentacles = new ModelPart[9];

   public HappyGhastModel(ModelPart root) {
      this.root = root;
      ModelPart body = root.getChild("body");
      for (int i = 0; i < 9; i++) this.tentacles[i] = body.getChild("tentacle" + i);
   }

   public static LayerDefinition createBodyLayer(boolean baby) {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F), PartPose.offset(0.0F, 16.0F, 0.0F));
      if (baby) {
         body.addOrReplaceChild("inner_body", CubeListBuilder.create().texOffs(0, 32).addBox(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(-0.5F)), PartPose.offset(0.0F, 8.0F, 0.0F));
      }
      float[][] t = {{-3.75F, -5.0F, 5}, {1.25F, -5.0F, 7}, {6.25F, -5.0F, 4}, {-6.25F, 0.0F, 5}, {-1.25F, 0.0F, 5}, {3.75F, 0.0F, 7}, {-3.75F, 5.0F, 8}, {1.25F, 5.0F, 8}, {6.25F, 5.0F, 5}};
      for (int i = 0; i < 9; i++) {
         body.addOrReplaceChild("tentacle" + i, CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, t[i][2], 2.0F), PartPose.offset(t[i][0], 7.0F, t[i][1]));
      }
      return LayerDefinition.create(mesh, 64, 64);
   }

   public void setupAnim(HappyGhast entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      for (int i = 0; i < this.tentacles.length; i++) {
         this.tentacles[i].xRot = 0.2F * Mth.sin(ageInTicks * 0.3F + i) + 0.4F;
      }
   }

   public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, float r, float g, float b, float a) {
      this.root.render(pose, buffer, light, overlay, r, g, b, a);
   }
}
