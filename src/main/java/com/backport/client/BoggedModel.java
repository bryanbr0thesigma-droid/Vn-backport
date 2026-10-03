package com.backport.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.monster.Bogged;

public class BoggedModel extends SkeletonModel<Bogged> {
   private final ModelPart mushrooms;

   public BoggedModel(ModelPart root) {
      super(root);
      this.mushrooms = root.getChild("head").getChild("mushrooms");
   }

   static MeshDefinition skeletonMesh(CubeDeformation deformation) {
      MeshDefinition mesh = HumanoidModel.createMesh(deformation, 0.0F);
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, deformation), PartPose.offset(-5.0F, 2.0F, 0.0F));
      root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, deformation), PartPose.offset(5.0F, 2.0F, 0.0F));
      root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F, deformation), PartPose.offset(-2.0F, 12.0F, 0.0F));
      root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F, deformation), PartPose.offset(2.0F, 12.0F, 0.0F));
      return mesh;
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition mesh = skeletonMesh(CubeDeformation.NONE);
      PartDefinition mushrooms = mesh.getRoot().getChild("head").addOrReplaceChild("mushrooms", CubeListBuilder.create(), PartPose.ZERO);
      mushrooms.addOrReplaceChild("red_mushroom_1", CubeListBuilder.create().texOffs(50, 16).addBox(-3.0F, -3.0F, 0.0F, 6.0F, 4.0F, 0.0F), PartPose.offsetAndRotation(3.0F, -8.0F, 3.0F, 0.0F, (float) (Math.PI / 4), 0.0F));
      mushrooms.addOrReplaceChild("red_mushroom_2", CubeListBuilder.create().texOffs(50, 16).addBox(-3.0F, -3.0F, 0.0F, 6.0F, 4.0F, 0.0F), PartPose.offsetAndRotation(3.0F, -8.0F, 3.0F, 0.0F, (float) (Math.PI * 3.0 / 4.0), 0.0F));
      mushrooms.addOrReplaceChild("brown_mushroom_1", CubeListBuilder.create().texOffs(50, 22).addBox(-3.0F, -3.0F, 0.0F, 6.0F, 4.0F, 0.0F), PartPose.offsetAndRotation(-3.0F, -8.0F, -3.0F, 0.0F, (float) (Math.PI / 4), 0.0F));
      mushrooms.addOrReplaceChild("brown_mushroom_2", CubeListBuilder.create().texOffs(50, 22).addBox(-3.0F, -3.0F, 0.0F, 6.0F, 4.0F, 0.0F), PartPose.offsetAndRotation(-3.0F, -8.0F, -3.0F, 0.0F, (float) (Math.PI * 3.0 / 4.0), 0.0F));
      mushrooms.addOrReplaceChild("brown_mushroom_3", CubeListBuilder.create().texOffs(50, 28).addBox(-3.0F, -4.0F, 0.0F, 6.0F, 4.0F, 0.0F), PartPose.offsetAndRotation(-2.0F, -1.0F, 4.0F, (float) (-Math.PI / 2), 0.0F, (float) (Math.PI / 4)));
      mushrooms.addOrReplaceChild("brown_mushroom_4", CubeListBuilder.create().texOffs(50, 28).addBox(-3.0F, -4.0F, 0.0F, 6.0F, 4.0F, 0.0F), PartPose.offsetAndRotation(-2.0F, -1.0F, 4.0F, (float) (-Math.PI / 2), 0.0F, (float) (Math.PI * 3.0 / 4.0)));
      return LayerDefinition.create(mesh, 64, 32);
   }

   public static LayerDefinition createOuterLayer() {
      MeshDefinition mesh = skeletonMesh(new CubeDeformation(0.2F));
      mesh.getRoot().getChild("head").addOrReplaceChild("mushrooms", CubeListBuilder.create(), PartPose.ZERO);
      return LayerDefinition.create(mesh, 64, 32);
   }

   public void setupAnim(Bogged bogged, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      super.setupAnim(bogged, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
      this.mushrooms.visible = !bogged.isSheared();
   }
}
