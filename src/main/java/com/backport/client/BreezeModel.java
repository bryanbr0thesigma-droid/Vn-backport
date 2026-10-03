package com.backport.client;

import com.backport.entity.Breeze;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class BreezeModel extends HierarchicalModel<Breeze> {
   private final ModelPart root;

   public BreezeModel(ModelPart root) {
      this.root = root;
   }

   private static MeshDefinition base(boolean body, boolean wind, boolean eyes) {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      if (body || eyes) {
         PartDefinition bodyPart = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
         if (body) {
            PartDefinition rods = bodyPart.addOrReplaceChild("rods", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 0.0F));
            rods.addOrReplaceChild("rod_1", CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5981F, -3.0F, 1.5F, -2.7489F, -1.0472F, 3.1416F));
            rods.addOrReplaceChild("rod_2", CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5981F, -3.0F, 1.5F, -2.7489F, 1.0472F, 3.1416F));
            rods.addOrReplaceChild("rod_3", CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, -3.0F, 0.3927F, 0.0F, 0.0F));
         }

         CubeListBuilder headCubes = CubeListBuilder.create()
            .texOffs(4, 24).addBox(-5.0F, -5.0F, -4.2F, 10.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F));
         PartDefinition head = bodyPart.addOrReplaceChild("head", body ? headCubes : CubeListBuilder.create(), PartPose.offset(0.0F, 4.0F, 0.0F));
         if (eyes) {
            head.addOrReplaceChild("eyes", headCubes, PartPose.offset(0.0F, 0.0F, 0.0F));
         }
      }

      if (wind) {
         PartDefinition windBody = partdefinition.addOrReplaceChild("wind_body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
         PartDefinition windBottom = windBody.addOrReplaceChild("wind_bottom", CubeListBuilder.create().texOffs(1, 83).addBox(-2.5F, -7.0F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));
         PartDefinition windMid = windBottom.addOrReplaceChild("wind_mid", CubeListBuilder.create()
            .texOffs(74, 28).addBox(-6.0F, -6.0F, -6.0F, 12.0F, 6.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(78, 32).addBox(-4.0F, -6.0F, -4.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(49, 71).addBox(-2.5F, -6.0F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.0F, 0.0F));
         windMid.addOrReplaceChild("wind_top", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-9.0F, -8.0F, -9.0F, 18.0F, 8.0F, 18.0F, new CubeDeformation(0.0F))
            .texOffs(6, 6).addBox(-6.0F, -8.0F, -6.0F, 12.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(105, 57).addBox(-2.5F, -8.0F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.0F, 0.0F));
      }

      return meshdefinition;
   }

   public static LayerDefinition createBodyLayer() {
      return LayerDefinition.create(base(true, false, false), 32, 32);
   }

   public static LayerDefinition createWindLayer() {
      return LayerDefinition.create(base(false, true, false), 128, 128);
   }

   public static LayerDefinition createEyesLayer() {
      return LayerDefinition.create(base(false, false, true), 32, 32);
   }

   public ModelPart root() {
      return this.root;
   }

   public void setupAnim(Breeze breeze, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      this.root().getAllParts().forEach(ModelPart::resetPose);
      this.animate(breeze.idle, BreezeAnimation.IDLE, ageInTicks);
      this.animate(breeze.shoot, BreezeAnimation.SHOOT, ageInTicks);
      this.animate(breeze.slide, BreezeAnimation.SLIDE, ageInTicks);
      this.animate(breeze.slideBack, BreezeAnimation.SLIDE_BACK, ageInTicks);
      this.animate(breeze.inhale, BreezeAnimation.INHALE, ageInTicks);
      this.animate(breeze.longJump, BreezeAnimation.JUMP, ageInTicks);
   }
}
