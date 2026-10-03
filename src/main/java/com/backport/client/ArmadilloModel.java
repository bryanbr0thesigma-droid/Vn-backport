package com.backport.client;

import com.backport.entity.Armadillo;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class ArmadilloModel extends HierarchicalModel<Armadillo> {
   private final ModelPart root;
   private final ModelPart body;
   private final ModelPart rightHindLeg;
   private final ModelPart leftHindLeg;
   private final ModelPart cube;
   private final ModelPart head;
   private final ModelPart tail;
   private final AnimationDefinition walk;
   private final AnimationDefinition rollOut;
   private final AnimationDefinition rollUp;
   private final AnimationDefinition peek;

   public ArmadilloModel(ModelPart root, boolean baby) {
      this.root = root;
      this.body = root.getChild("body");
      this.rightHindLeg = root.getChild("right_hind_leg");
      this.leftHindLeg = root.getChild("left_hind_leg");
      this.head = this.body.getChild("head");
      this.tail = this.body.getChild("tail");
      this.cube = root.getChild("cube");
      this.walk = baby ? BabyArmadilloAnimation.ARMADILLO_BABY_WALK : ArmadilloAnimation.ARMADILLO_WALK;
      this.rollOut = baby ? BabyArmadilloAnimation.ARMADILLO_BABY_ROLL_OUT : ArmadilloAnimation.ARMADILLO_ROLL_OUT;
      this.rollUp = baby ? BabyArmadilloAnimation.ARMADILLO_BABY_ROLL_UP : ArmadilloAnimation.ARMADILLO_ROLL_UP;
      this.peek = baby ? BabyArmadilloAnimation.ARMADILLO_BABY_PEEK : ArmadilloAnimation.ARMADILLO_PEEK;
   }

   public ModelPart root() {
      return this.root;
   }

   public void setupAnim(Armadillo armadillo, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      this.root().getAllParts().forEach(ModelPart::resetPose);
      boolean hiding = armadillo.shouldHideInShell();
      if (hiding) {
         this.body.skipDraw = true;
         this.leftHindLeg.visible = false;
         this.rightHindLeg.visible = false;
         this.tail.visible = false;
         this.cube.visible = true;
      } else {
         this.body.skipDraw = false;
         this.leftHindLeg.visible = true;
         this.rightHindLeg.visible = true;
         this.tail.visible = true;
         this.cube.visible = false;
         this.head.xRot = Mth.clamp(headPitch, -22.5F, 25.0F) * (float) (Math.PI / 180.0);
         this.head.yRot = Mth.clamp(netHeadYaw, -32.5F, 32.5F) * (float) (Math.PI / 180.0);
         this.animateWalk(this.walk, limbSwing, limbSwingAmount, 16.5F, 2.5F);
      }

      this.animate(armadillo.rollOutAnimationState, this.rollOut, ageInTicks);
      this.animate(armadillo.rollUpAnimationState, this.rollUp, ageInTicks);
      this.animate(armadillo.peekAnimationState, this.peek, ageInTicks);
   }

   public static LayerDefinition createAdultLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition body = root.addOrReplaceChild(
         "body",
         CubeListBuilder.create()
            .texOffs(0, 20)
            .addBox(-4.0F, -7.0F, -10.0F, 8.0F, 8.0F, 12.0F, new CubeDeformation(0.3F))
            .texOffs(0, 40)
            .addBox(-4.0F, -7.0F, -10.0F, 8.0F, 8.0F, 12.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 21.0F, 4.0F)
      );
      body.addOrReplaceChild(
         "tail",
         CubeListBuilder.create().texOffs(44, 53).addBox(-0.5F, -0.0865F, 0.0933F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, -3.0F, 1.0F, 0.5061F, 0.0F, 0.0F)
      );
      PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, -11.0F));
      head.addOrReplaceChild(
         "head_cube",
         CubeListBuilder.create().texOffs(43, 15).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F)
      );
      PartDefinition rightEar = head.addOrReplaceChild("right_ear", CubeListBuilder.create(), PartPose.offset(-1.0F, -1.0F, 0.0F));
      rightEar.addOrReplaceChild(
         "right_ear_cube",
         CubeListBuilder.create().texOffs(43, 10).addBox(-2.0F, -3.0F, 0.0F, 2.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-0.5F, 0.0F, -0.6F, 0.1886F, -0.3864F, -0.0718F)
      );
      PartDefinition leftEar = head.addOrReplaceChild("left_ear", CubeListBuilder.create(), PartPose.offset(1.0F, -2.0F, 0.0F));
      leftEar.addOrReplaceChild(
         "left_ear_cube",
         CubeListBuilder.create().texOffs(47, 10).addBox(0.0F, -3.0F, 0.0F, 2.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.5F, 1.0F, -0.6F, 0.1886F, 0.3864F, 0.0718F)
      );
      root.addOrReplaceChild(
         "right_hind_leg",
         CubeListBuilder.create().texOffs(51, 31).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 21.0F, 4.0F)
      );
      root.addOrReplaceChild(
         "left_hind_leg",
         CubeListBuilder.create().texOffs(42, 31).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(2.0F, 21.0F, 4.0F)
      );
      root.addOrReplaceChild(
         "right_front_leg",
         CubeListBuilder.create().texOffs(51, 43).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 21.0F, -4.0F)
      );
      root.addOrReplaceChild(
         "left_front_leg",
         CubeListBuilder.create().texOffs(42, 43).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(2.0F, 21.0F, -4.0F)
      );
      root.addOrReplaceChild(
         "cube",
         CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -10.0F, -6.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 24.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }


   public static LayerDefinition createBabyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition body = root.addOrReplaceChild(
         "body",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-2.5F, -2.0F, -3.5F, 5.0F, 4.0F, 7.0F, new CubeDeformation(0.3F))
            .texOffs(0, 11)
            .addBox(-2.5F, -2.0F, -3.0F, 5.0F, 4.0F, 6.0F),
         PartPose.offset(0.0F, 20.0F, 0.5F)
      );
      PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 3.4F));
      tail.addOrReplaceChild(
         "right_ear_cube",
         CubeListBuilder.create().texOffs(22, 11).addBox(-0.5F, -0.5F, -2.0F, 1.0F, 1.0F, 4.0F),
         PartPose.offsetAndRotation(0.0F, 1.5F, 1.0F, -1.0472F, 0.0F, 0.0F)
      );
      PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -3.2F));
      PartDefinition headGroup = head.addOrReplaceChild(
         "head_cube",
         CubeListBuilder.create().texOffs(20, 17).addBox(-1.0F, -2.0F, -4.0F, 2.0F, 2.0F, 4.0F),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7417649F, 0.0F, 0.0F)
      );
      headGroup.addOrReplaceChild(
         "right_ear",
         CubeListBuilder.create().texOffs(28, 8).mirror().addBox(-1.8F, -2.0F, 0.0F, 2.0F, 3.0F, 0.0F).mirror(false),
         PartPose.offsetAndRotation(-1.0F, -2.0F, -0.3F, -0.4363F, -0.1134F, 0.0524F)
      );
      headGroup.addOrReplaceChild(
         "left_ear",
         CubeListBuilder.create().texOffs(28, 8).addBox(-0.2F, -2.0F, 0.0F, 2.0F, 3.0F, 0.0F),
         PartPose.offsetAndRotation(1.0F, -2.0F, -0.3F, -0.4363F, 0.1134F, -0.0524F)
      );
      root.addOrReplaceChild(
         "right_hind_leg",
         CubeListBuilder.create().texOffs(20, 27).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F).mirror(false),
         PartPose.offset(-1.5F, 22.0F, 2.5F)
      );
      root.addOrReplaceChild(
         "left_hind_leg", CubeListBuilder.create().texOffs(20, 27).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(1.5F, 22.0F, 2.5F)
      );
      root.addOrReplaceChild(
         "right_front_leg", CubeListBuilder.create().texOffs(20, 23).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(1.5F, 22.0F, -1.5F)
      );
      root.addOrReplaceChild(
         "left_front_leg",
         CubeListBuilder.create().texOffs(24, 0).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F).mirror(false),
         PartPose.offset(-1.5F, 22.0F, -1.5F)
      );
      root.addOrReplaceChild(
         "cube",
         CubeListBuilder.create().texOffs(0, 25).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.3F)),
         PartPose.offset(0.0F, 20.7F, 0.5F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

}
