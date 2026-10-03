package com.backport.client;

import com.backport.entity.AbstractNautilus;
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
import net.minecraft.util.Mth;

public class NautilusModel extends EntityModel<AbstractNautilus> {
   protected final ModelPart root;
   protected final ModelPart nautilus;
   protected final ModelPart body;

   public NautilusModel(ModelPart root) {
      this.root = root;
      this.nautilus = root.getChild("root");
      this.body = this.nautilus.getChild("body");
   }

   public static MeshDefinition createBodyMesh() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition nautilus = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 29.0F, -6.0F));
      nautilus.addOrReplaceChild("shell", CubeListBuilder.create()
         .texOffs(0, 0).addBox(-7.0F, -10.0F, -7.0F, 14.0F, 10.0F, 16.0F)
         .texOffs(0, 26).addBox(-7.0F, 0.0F, -7.0F, 14.0F, 8.0F, 20.0F)
         .texOffs(48, 26).addBox(-7.0F, 0.0F, 6.0F, 14.0F, 8.0F, 0.0F), PartPose.offset(0.0F, -13.0F, 5.0F));
      PartDefinition body = nautilus.addOrReplaceChild("body", CubeListBuilder.create()
         .texOffs(0, 54).addBox(-5.0F, -4.51F, -3.0F, 10.0F, 8.0F, 14.0F)
         .texOffs(0, 76).addBox(-5.0F, -4.51F, 7.0F, 10.0F, 8.0F, 0.0F), PartPose.offset(0.0F, -8.5F, 12.3F));
      body.addOrReplaceChild("upper_mouth", CubeListBuilder.create().texOffs(54, 54).addBox(-5.0F, -2.0F, 0.0F, 10.0F, 4.0F, 4.0F, new CubeDeformation(-0.001F)), PartPose.offset(0.0F, -2.51F, 7.0F));
      body.addOrReplaceChild("inner_mouth", CubeListBuilder.create().texOffs(54, 70).addBox(-3.0F, -2.0F, -0.5F, 6.0F, 4.0F, 4.0F), PartPose.offset(0.0F, -0.51F, 7.5F));
      body.addOrReplaceChild("lower_mouth", CubeListBuilder.create().texOffs(54, 62).addBox(-5.0F, -1.98F, 0.0F, 10.0F, 4.0F, 4.0F, new CubeDeformation(-0.001F)), PartPose.offset(0.0F, 1.49F, 7.0F));
      return mesh;
   }

   public static LayerDefinition createBodyLayer() {
      return LayerDefinition.create(createBodyMesh(), 128, 128);
   }

   public static LayerDefinition createBabyBodyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition nautilus = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(-0.5F, 28.0F, -0.5F));
      nautilus.addOrReplaceChild("shell", CubeListBuilder.create()
         .texOffs(0, 0).addBox(-6.0F, -4.0F, -1.0F, 7.0F, 4.0F, 7.0F)
         .texOffs(0, 11).addBox(-6.0F, 0.0F, -1.0F, 7.0F, 4.0F, 9.0F)
         .texOffs(23, 11).addBox(-6.0F, 0.0F, 5.0F, 7.0F, 4.0F, 0.0F), PartPose.offset(3.0F, -8.0F, -2.0F));
      PartDefinition body = nautilus.addOrReplaceChild("body", CubeListBuilder.create()
         .texOffs(0, 24).addBox(-2.5F, -3.01F, -1.0F, 5.0F, 4.0F, 7.0F)
         .texOffs(0, 35).addBox(-2.5F, -3.01F, 4.1F, 5.0F, 4.0F, 0.0F), PartPose.offset(0.5F, -5.0F, 3.0F));
      body.addOrReplaceChild("upper_mouth", CubeListBuilder.create().texOffs(24, 24).addBox(-2.5F, -1.0F, 0.0F, 5.0F, 2.0F, 2.0F, new CubeDeformation(-0.001F)), PartPose.offset(0.0F, -2.01F, 3.9F));
      body.addOrReplaceChild("inner_mouth", CubeListBuilder.create().texOffs(24, 32).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F), PartPose.offset(0.0F, -1.01F, 4.9F));
      body.addOrReplaceChild("lower_mouth", CubeListBuilder.create().texOffs(24, 28).addBox(-2.5F, -1.0F, 0.0F, 5.0F, 2.0F, 2.0F, new CubeDeformation(-0.001F)), PartPose.offset(0.0F, -0.01F, 3.9F));
      return LayerDefinition.create(mesh, 64, 64);
   }

   public static LayerDefinition createArmorLayer(float grow, boolean shellOnly) {
      MeshDefinition mesh = createBodyMesh();
      PartDefinition nautilus = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 29.0F, -6.0F));
      CubeListBuilder b = CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -10.0F, -7.0F, 14.0F, 10.0F, 16.0F, new CubeDeformation(grow));
      if (!shellOnly) {
         b = b.texOffs(0, 26).addBox(-7.0F, 0.0F, -7.0F, 14.0F, 8.0F, 20.0F, new CubeDeformation(grow)).texOffs(48, 26).addBox(-7.0F, 0.0F, 6.0F, 14.0F, 8.0F, 0.0F);
      }
      nautilus.addOrReplaceChild("shell", b, PartPose.offset(0.0F, -13.0F, 5.0F));
      return LayerDefinition.create(mesh, 128, 128);
   }

   public void setupAnim(AbstractNautilus entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      this.body.yRot = Mth.clamp(netHeadYaw, -10.0F, 10.0F) * (float) (Math.PI / 180.0);
      this.body.xRot = Mth.clamp(headPitch, -10.0F, 10.0F) * (float) (Math.PI / 180.0);
      float t = limbSwing + ageInTicks / 5.0F;
      float amp = Math.min(2.0F, limbSwingAmount + 0.2F);
      this.nautilus.xRot = Mth.sin(t * 0.8F) * 0.05F * amp;
      this.body.zRot = Mth.sin(t * 0.4F) * 0.04F * amp;
   }

   public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, float r, float g, float b, float a) {
      this.root.render(pose, buffer, light, overlay, r, g, b, a);
   }
}
