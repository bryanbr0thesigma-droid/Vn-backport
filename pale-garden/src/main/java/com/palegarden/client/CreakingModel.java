package com.palegarden.client;

import com.palegarden.entity.Creaking;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class CreakingModel extends HierarchicalModel<Creaking> {
   private final ModelPart root;
   private final ModelPart rootBody;
   private final ModelPart upperBody;
   private final ModelPart head;
   private final ModelPart body;
   private final ModelPart rightArm;
   private final ModelPart leftArm;
   private final ModelPart leftLeg;
   private final ModelPart rightLeg;

   public CreakingModel(ModelPart modelPart) {
      this.root = modelPart;
      this.rootBody = modelPart.getChild("root");
      this.upperBody = this.rootBody.getChild("upper_body");
      this.head = this.upperBody.getChild("head");
      this.body = this.upperBody.getChild("body");
      this.rightArm = this.upperBody.getChild("right_arm");
      this.leftArm = this.upperBody.getChild("left_arm");
      this.leftLeg = this.rootBody.getChild("left_leg");
      this.rightLeg = this.rootBody.getChild("right_leg");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition rootBody = root.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition upperBody = rootBody.addOrReplaceChild("upper_body", CubeListBuilder.create(), PartPose.offset(-1.0F, -19.0F, 0.0F));
      upperBody.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(0, 0).addBox(-3.0F, -10.0F, -3.0F, 6.0F, 10.0F, 6.0F)
            .texOffs(28, 31).addBox(-3.0F, -13.0F, -3.0F, 6.0F, 3.0F, 6.0F)
            .texOffs(12, 40).addBox(3.0F, -13.0F, 0.0F, 9.0F, 14.0F, 0.0F)
            .texOffs(34, 12).addBox(-12.0F, -14.0F, 0.0F, 9.0F, 14.0F, 0.0F),
         PartPose.offset(-3.0F, -11.0F, 0.0F)
      );
      upperBody.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(0, 16).addBox(0.0F, -3.0F, -3.0F, 6.0F, 13.0F, 5.0F).texOffs(24, 0).addBox(-6.0F, -4.0F, -3.0F, 6.0F, 7.0F, 5.0F),
         PartPose.offset(0.0F, -7.0F, 1.0F)
      );
      upperBody.addOrReplaceChild(
         "right_arm",
         CubeListBuilder.create().texOffs(22, 13).addBox(-2.0F, -1.5F, -1.5F, 3.0F, 21.0F, 3.0F).texOffs(46, 0).addBox(-2.0F, 19.5F, -1.5F, 3.0F, 4.0F, 3.0F),
         PartPose.offset(-7.0F, -9.5F, 1.5F)
      );
      upperBody.addOrReplaceChild(
         "left_arm",
         CubeListBuilder.create()
            .texOffs(30, 40).addBox(0.0F, -1.0F, -1.5F, 3.0F, 16.0F, 3.0F)
            .texOffs(52, 12).addBox(0.0F, -5.0F, -1.5F, 3.0F, 4.0F, 3.0F)
            .texOffs(52, 19).addBox(0.0F, 15.0F, -1.5F, 3.0F, 4.0F, 3.0F),
         PartPose.offset(6.0F, -9.0F, 0.5F)
      );
      rootBody.addOrReplaceChild(
         "left_leg",
         CubeListBuilder.create().texOffs(42, 40).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 16.0F, 3.0F).texOffs(45, 55).addBox(-1.5F, 15.7F, -4.5F, 5.0F, 0.0F, 9.0F),
         PartPose.offset(1.5F, -16.0F, 0.5F)
      );
      rootBody.addOrReplaceChild(
         "right_leg",
         CubeListBuilder.create()
            .texOffs(0, 34).addBox(-3.0F, -1.5F, -1.5F, 3.0F, 19.0F, 3.0F)
            .texOffs(45, 46).addBox(-5.0F, 17.2F, -4.5F, 5.0F, 0.0F, 9.0F)
            .texOffs(12, 34).addBox(-3.0F, -4.5F, -1.5F, 3.0F, 3.0F, 3.0F),
         PartPose.offset(-1.0F, -17.5F, 0.5F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public ModelPart root() {
      return this.root;
   }

   public void setupAnim(Creaking creaking, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      this.root().getAllParts().forEach(ModelPart::resetPose);
      this.head.xRot = headPitch * (float) (Math.PI / 180.0);
      this.head.yRot = netHeadYaw * (float) (Math.PI / 180.0);
      if (creaking.canMove()) {
         this.animateWalk(CreakingAnimation.CREAKING_WALK, limbSwing, limbSwingAmount, 1.0F, 1.0F);
      }

      this.animate(creaking.attackAnimationState, CreakingAnimation.CREAKING_ATTACK, ageInTicks);
      this.animate(creaking.invulnerabilityAnimationState, CreakingAnimation.CREAKING_INVULNERABLE, ageInTicks);
      this.animate(creaking.deathAnimationState, CreakingAnimation.CREAKING_DEATH, ageInTicks);
   }

   /** Hides everything but the head chain so the glowing eyes can be drawn on top. */
   public void setOnlyHeadVisible(boolean onlyHead) {
      this.body.visible = !onlyHead;
      this.rightArm.visible = !onlyHead;
      this.leftArm.visible = !onlyHead;
      this.leftLeg.visible = !onlyHead;
      this.rightLeg.visible = !onlyHead;
   }
}
