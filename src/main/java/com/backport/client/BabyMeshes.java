package com.backport.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Baby mob meshes from the 26.x baby overhaul (generated from the official models). */
public final class BabyMeshes {
   private BabyMeshes() {
   }

   public static LayerDefinition pig() {
      CubeDeformation g = CubeDeformation.NONE;

      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -3.0F, -4.5F, 7.0F, 6.0F, 9.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 19.0F, 0.5F)
      );
      root.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(0, 15)
            .addBox(-3.5F, -5.0F, -5.0F, 7.0F, 6.0F, 6.0F, new CubeDeformation(0.025F))
            .texOffs(6, 27)
            .addBox(-1.5F, -1.975F, -6.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.015F)),
         PartPose.offset(0.0F, 19.0F, -2.0F)
      );
      root.addOrReplaceChild(
         "left_front_leg",
         CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(2.5F, 22.0F, -3.0F)
      );
      root.addOrReplaceChild(
         "right_front_leg",
         CubeListBuilder.create().texOffs(23, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.5F, 22.0F, -3.0F)
      );
      root.addOrReplaceChild(
         "left_hind_leg",
         CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(2.5F, 22.0F, 4.0F)
      );
      root.addOrReplaceChild(
         "right_hind_leg",
         CubeListBuilder.create().texOffs(23, 4).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.5F, 22.0F, 4.0F)
      );
      return LayerDefinition.create(mesh, 32, 32);
      }

   public static LayerDefinition cow() {

      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(0, 18)
            .addBox(-3.0F, -4.569F, -4.8333F, 6.0F, 6.0F, 5.0F)
            .texOffs(8, 29)
            .addBox(3.0F, -5.569F, -3.8333F, 1.0F, 2.0F, 1.0F)
            .texOffs(4, 29)
            .mirror()
            .addBox(-4.0F, -5.569F, -3.8333F, 1.0F, 2.0F, 1.0F)
            .mirror(false)
            .texOffs(12, 29)
            .addBox(-2.0F, -1.569F, -5.8333F, 4.0F, 3.0F, 1.0F),
         PartPose.offset(0.0F, 13.569F, -5.1667F)
      );
      root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -7.0F, -1.0F, 8.0F, 6.0F, 12.0F), PartPose.offset(3.0F, 19.0F, -5.0F));
      root.addOrReplaceChild(
         "right_front_leg", CubeListBuilder.create().texOffs(22, 18).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), PartPose.offset(-2.5F, 18.0F, -3.5F)
      );
      root.addOrReplaceChild(
         "left_front_leg", CubeListBuilder.create().texOffs(34, 18).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), PartPose.offset(2.5F, 18.0F, -3.5F)
      );
      root.addOrReplaceChild(
         "right_hind_leg", CubeListBuilder.create().texOffs(22, 27).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), PartPose.offset(-2.5F, 18.0F, 3.5F)
      );
      root.addOrReplaceChild(
         "left_hind_leg", CubeListBuilder.create().texOffs(34, 27).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), PartPose.offset(2.5F, 18.0F, 3.5F)
      );
      return LayerDefinition.create(mesh, 64, 64);
      }

   public static LayerDefinition sheep() {

      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 10).addBox(-3.0F, -2.0F, -4.5F, 6.0F, 4.0F, 9.0F), PartPose.offset(0.0F, 17.0F, 0.5F));
      root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -4.5F, -3.5F, 5.0F, 5.0F, 5.0F), PartPose.offset(0.0F, 15.5F, -2.5F));
      root.addOrReplaceChild(
         "right_hind_leg", CubeListBuilder.create().texOffs(0, 23).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F), PartPose.offset(-2.0F, 19.0F, 3.0F)
      );
      root.addOrReplaceChild(
         "left_hind_leg", CubeListBuilder.create().texOffs(24, 12).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F), PartPose.offset(2.0F, 19.0F, 3.0F)
      );
      root.addOrReplaceChild(
         "right_front_leg", CubeListBuilder.create().texOffs(8, 23).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F), PartPose.offset(-2.0F, 19.0F, -2.0F)
      );
      root.addOrReplaceChild(
         "left_front_leg", CubeListBuilder.create().texOffs(24, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F), PartPose.offset(2.0F, 19.0F, -2.0F)
      );
      return LayerDefinition.create(mesh, 64, 32);
      }

   public static LayerDefinition chicken() {

      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.25F, -0.75F, 4.0F, 4.0F, 4.0F).texOffs(10, 8).addBox(-1.0F, -0.25F, -1.75F, 2.0F, 1.0F, 1.0F),
         PartPose.offset(0.0F, 20.25F, -1.25F)
      );
      root.addOrReplaceChild(
         "left_leg",
         CubeListBuilder.create().texOffs(2, 2).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 2.0F, 0.0F).texOffs(0, 1).addBox(-0.5F, 2.0F, -1.0F, 1.0F, 0.0F, 1.0F),
         PartPose.offset(1.0F, 22.0F, 0.5F)
      );
      root.addOrReplaceChild(
         "right_leg",
         CubeListBuilder.create().texOffs(0, 2).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 2.0F, 0.0F).texOffs(0, 0).addBox(-0.5F, 2.0F, -1.0F, 1.0F, 0.0F, 1.0F),
         PartPose.offset(-1.0F, 22.0F, 0.5F)
      );
      root.addOrReplaceChild(
         "right_wing", CubeListBuilder.create().texOffs(6, 8).addBox(0.0F, 0.0F, -1.0F, 1.0F, 0.0F, 2.0F), PartPose.offset(2.0F, 20.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "left_wing", CubeListBuilder.create().texOffs(4, 8).addBox(-1.0F, 0.0F, -1.0F, 1.0F, 0.0F, 2.0F), PartPose.offset(-2.0F, 20.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 16, 16);
      }

   public static LayerDefinition wolf() {

      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition head = root.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(0, 12)
            .addBox(-2.99F, -3.25F, -3.0F, 6.0F, 5.0F, 5.0F, new CubeDeformation(0.025F))
            .texOffs(17, 12)
            .addBox(-1.5F, -0.24F, -5.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 18.25F, -4.0F)
      );
      head.addOrReplaceChild(
         "right_ear", CubeListBuilder.create().texOffs(0, 5).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offset(-2.0F, -4.25F, -0.5F)
      );
      head.addOrReplaceChild(
         "left_ear", CubeListBuilder.create().texOffs(20, 5).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offset(2.0F, -4.25F, -0.5F)
      );
      root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -2.0F, -4.0F, 6.0F, 4.0F, 8.0F), PartPose.offset(0.0F, 19.0F, 0.0F));
      root.addOrReplaceChild(
         "right_hind_leg", CubeListBuilder.create().texOffs(0, 22).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(-1.5F, 21.0F, 3.0F)
      );
      root.addOrReplaceChild(
         "left_hind_leg", CubeListBuilder.create().texOffs(8, 22).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(1.5F, 21.0F, 3.0F)
      );
      root.addOrReplaceChild(
         "right_front_leg", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(-1.5F, 21.0F, -3.0F)
      );
      root.addOrReplaceChild(
         "left_front_leg", CubeListBuilder.create().texOffs(20, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(1.5F, 21.0F, -3.0F)
      );
      PartDefinition tail = root.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 19.0F, 3.0F, -0.5236F, 0.0F, 0.0F));
      tail.addOrReplaceChild(
         "tail_r1",
         CubeListBuilder.create().texOffs(22, 16).addBox(-1.0F, -5.7F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, -0.6F, 0.2F, -3.1F, 0.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 32, 32);
      }

   public static LayerDefinition feline() {

      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-2.5F, -3.0F, -2.875F, 5.0F, 4.0F, 4.0F)
            .texOffs(18, 0)
            .addBox(-2.0F, -4.0F, -0.875F, 1.0F, 1.0F, 2.0F)
            .texOffs(24, 0)
            .addBox(1.0F, -4.0F, -0.875F, 1.0F, 1.0F, 2.0F)
            .texOffs(18, 3)
            .addBox(-1.5F, -1.0F, -3.875F, 3.0F, 2.0F, 1.0F),
         PartPose.offset(0.0F, 20.0F, -3.125F)
      );
      partdefinition.addOrReplaceChild(
         "left_front_leg", CubeListBuilder.create().texOffs(18, 18).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F), PartPose.offset(1.0F, 22.0F, -1.5F)
      );
      partdefinition.addOrReplaceChild(
         "right_front_leg", CubeListBuilder.create().texOffs(12, 18).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F), PartPose.offset(-1.0F, 22.0F, -1.5F)
      );
      partdefinition.addOrReplaceChild(
         "left_hind_leg", CubeListBuilder.create().texOffs(18, 22).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F), PartPose.offset(1.0F, 22.0F, 2.5F)
      );
      partdefinition.addOrReplaceChild(
         "body", CubeListBuilder.create().texOffs(0, 8).addBox(-2.0F, -1.5F, -3.5F, 4.0F, 3.0F, 7.0F), PartPose.offset(0.0F, 20.5F, 0.5F)
      );
      partdefinition.addOrReplaceChild(
         "right_hind_leg", CubeListBuilder.create().texOffs(12, 22).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F), PartPose.offset(-1.0F, 22.0F, 2.5F)
      );
      partdefinition.addOrReplaceChild(
         "tail1",
         CubeListBuilder.create().texOffs(0, 18).addBox(-0.5F, -0.107F, 0.0849F, 1.0F, 1.0F, 5.0F),
         PartPose.offsetAndRotation(0.0F, 19.107F, 3.9151F, -0.567232F, 0.0F, 0.0F)
      );
      partdefinition.addOrReplaceChild("tail2", CubeListBuilder.create(), PartPose.ZERO);
      return LayerDefinition.create(meshdefinition, 32, 32);
      }

   public static LayerDefinition goat() {

      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild(
         "left_hind_leg", CubeListBuilder.create().texOffs(29, 12).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F), PartPose.offset(1.5F, 19.5F, 3.0F)
      );
      root.addOrReplaceChild(
         "right_hind_leg", CubeListBuilder.create().texOffs(21, 12).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F), PartPose.offset(-1.5F, 19.5F, 3.0F)
      );
      root.addOrReplaceChild(
         "right_front_leg", CubeListBuilder.create().texOffs(21, 5).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F), PartPose.offset(-1.5F, 19.5F, -2.0F)
      );
      root.addOrReplaceChild(
         "left_front_leg", CubeListBuilder.create().texOffs(29, 5).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F), PartPose.offset(1.5F, 19.5F, -2.0F)
      );
      root.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(0, 10).addBox(-3.0F, -2.3F, -4.5F, 6.0F, 5.0F, 9.0F).texOffs(0, 24).addBox(-2.5F, -2.2F, -4.0F, 5.0F, 4.0F, 8.0F),
         PartPose.offset(0.0F, 17.8F, 0.0F)
      );
      PartDefinition head = root.addOrReplaceChild(
         "head",
         CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -3.8126F, -5.1548F, 4.0F, 4.0F, 6.0F),
         PartPose.offsetAndRotation(0.0F, 15.5F, -3.0F, 0.4363F, 0.0F, 0.0F)
      );
      head.addOrReplaceChild(
         "right_horn",
         CubeListBuilder.create().texOffs(24, 0).mirror().addBox(0.0F, -4.5F, 0.0F, 1.0F, 2.0F, 1.0F).mirror(false),
         PartPose.offsetAndRotation(-1.5F, -1.5F, -1.0F, (float) (-Math.PI / 8), 0.0F, 0.0F)
      );
      head.addOrReplaceChild(
         "left_horn",
         CubeListBuilder.create().texOffs(24, 0).mirror().addBox(2.0F, -4.5F, 0.0F, 1.0F, 2.0F, 1.0F).mirror(false),
         PartPose.offsetAndRotation(-1.5F, -1.5F, -1.0F, (float) (-Math.PI / 8), 0.0F, 0.0F)
      );
      head.addOrReplaceChild(
         "right_ear",
         CubeListBuilder.create().texOffs(0, 12).mirror().addBox(-2.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F).mirror(false),
         PartPose.offsetAndRotation(-1.7F, -2.3126F, 0.1452F, 0.0F, -0.5236F, 0.0F)
      );
      head.addOrReplaceChild(
         "left_ear",
         CubeListBuilder.create().texOffs(0, 12).addBox(0.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F),
         PartPose.offsetAndRotation(1.7F, -2.3126F, 0.1452F, 0.0F, 0.5236F, 0.0F)
      );
      head.addOrReplaceChild(
         "HeadMain", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.5F, -4.0F, 4.0F, 4.0F, 6.0F), PartPose.offset(0.0F, -1.3126F, -1.1548F)
      );
      return LayerDefinition.create(mesh, 64, 64);
      }

   public static LayerDefinition fox() {

      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition root = meshdefinition.getRoot();
      root.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-3.0F, -2.125F, -5.125F, 6.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
            .texOffs(18, 20)
            .addBox(-1.0F, 0.875F, -7.125F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(22, 8)
            .addBox(-3.0F, -4.125F, -4.125F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(22, 11)
            .addBox(1.0F, -4.125F, -4.125F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 18.125F, 0.125F)
      );
      root.addOrReplaceChild(
         "right_hind_leg",
         CubeListBuilder.create().texOffs(22, 4).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-1.5F, 22.0F, 4.0F)
      );
      root.addOrReplaceChild(
         "left_hind_leg",
         CubeListBuilder.create().texOffs(22, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(1.5F, 22.0F, 4.0F)
      );
      root.addOrReplaceChild(
         "right_front_leg",
         CubeListBuilder.create().texOffs(22, 4).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-1.5F, 22.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "left_front_leg",
         CubeListBuilder.create().texOffs(22, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(1.5F, 22.0F, 0.0F)
      );
      PartDefinition body = root.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(0, 10).addBox(-2.5F, -2.0F, -3.0F, 5.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 20.0F, 2.0F)
      );
      body.addOrReplaceChild(
         "tail",
         CubeListBuilder.create().texOffs(0, 20).addBox(-1.5F, -1.48F, -1.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, -0.5F, 3.0F)
      );
      return LayerDefinition.create(meshdefinition, 32, 32);
      }

   public static LayerDefinition rabbit() {

      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 23.0F, 1.6F));
      body.addOrReplaceChild(
         "body_r1",
         CubeListBuilder.create().texOffs(0, 8).addBox(-2.0F, -2.0F, -3.0F, 4.0F, 3.0F, 6.0F),
         PartPose.offsetAndRotation(0.0F, -2.0F, -1.6F, -0.5236F, 0.0F, 0.0F)
      );
      PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, -2.2F, 2.0F));
      tail.addOrReplaceChild(
         "tail_r1",
         CubeListBuilder.create().texOffs(0, 21).addBox(-1.4F, -2.0268F, -1.0177F, 3.0F, 3.0F, 3.0F),
         PartPose.offsetAndRotation(-0.1F, 0.0F, 0.0F, -0.5236F, 0.0F, 0.0F)
      );
      PartDefinition head = body.addOrReplaceChild(
         "head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -3.0F, -3.0F, 5.0F, 4.0F, 4.0F), PartPose.offset(0.0F, -5.0F, -2.6F)
      );
      head.addOrReplaceChild(
         "right_ear", CubeListBuilder.create().texOffs(18, 0).addBox(-1.0F, -3.5F, -0.5F, 2.0F, 4.0F, 1.0F), PartPose.offset(-1.5F, -3.5F, -0.5F)
      );
      head.addOrReplaceChild(
         "left_ear", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -3.5F, -0.5F, 2.0F, 4.0F, 1.0F), PartPose.offset(1.5F, -3.5F, -0.5F)
      );
      PartDefinition frontLegs = body.addOrReplaceChild("frontlegs", CubeListBuilder.create(), PartPose.offset(0.0F, -2.5F, -2.6F));
      PartDefinition leftFrontLeg = frontLegs.addOrReplaceChild(
         "left_front_leg", CubeListBuilder.create(), PartPose.offsetAndRotation(1.0F, 1.0F, -0.5F, 0.3927F, 0.0F, 0.0F)
      );
      leftFrontLeg.addOrReplaceChild(
         "left_front_leg_r1",
         CubeListBuilder.create().texOffs(18, 8).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 3.0F, 1.0F),
         PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.3927F, 0.0F, 0.0F)
      );
      PartDefinition rightFrontLeg = frontLegs.addOrReplaceChild(
         "right_front_leg", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.0F, 1.0F, -0.5F, 0.3927F, 0.0F, 0.0F)
      );
      rightFrontLeg.addOrReplaceChild(
         "right_front_leg_r1",
         CubeListBuilder.create().texOffs(14, 8).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 3.0F, 1.0F),
         PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.3927F, 0.0F, 0.0F)
      );
      PartDefinition backLegs = root.addOrReplaceChild("backlegs", CubeListBuilder.create(), PartPose.offset(0.0F, 23.0F, 2.0F));
      PartDefinition leftBackLeg = backLegs.addOrReplaceChild(
         "left_hind_leg", CubeListBuilder.create(), PartPose.offsetAndRotation(1.5F, 0.5F, 0.5F, 0.0F, 3.1416F, 0.0F)
      );
      leftBackLeg.addOrReplaceChild(
         "left_haunch",
         CubeListBuilder.create().texOffs(10, 17).addBox(-2.0F, -0.5F, 0.0F, 2.0F, 1.0F, 3.0F),
         PartPose.offsetAndRotation(1.0F, 0.0F, 0.5F, 0.0F, -0.7854F, 0.0F)
      );
      PartDefinition rightBackLeg = backLegs.addOrReplaceChild(
         "right_hind_leg", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.5F, 0.5F, 0.5F, 0.0F, 3.1416F, 0.0F)
      );
      rightBackLeg.addOrReplaceChild(
         "right_haunch",
         CubeListBuilder.create().texOffs(0, 17).addBox(-2.0F, -0.5F, 0.0F, 2.0F, 1.0F, 3.0F),
         PartPose.offsetAndRotation(0.5F, 0.0F, -0.9F, 0.0F, 0.7854F, 0.0F)
      );
      return LayerDefinition.create(mesh, 32, 32);
      }

   public static LayerDefinition polar_bear() {

      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 9).addBox(-4.0F, -3.5F, -6.0F, 8.0F, 7.0F, 12.0F), PartPose.offset(0.0F, 17.5F, 0.0F));
      root.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-3.0F, -2.625F, -4.25F, 6.0F, 5.0F, 4.0F)
            .texOffs(20, 3)
            .addBox(-2.0F, 0.375F, -6.25F, 4.0F, 2.0F, 2.0F)
            .texOffs(20, 0)
            .addBox(-4.0F, -3.625F, -2.75F, 2.0F, 2.0F, 1.0F)
            .texOffs(26, 0)
            .addBox(2.0F, -3.625F, -2.75F, 2.0F, 2.0F, 1.0F),
         PartPose.offset(0.0F, 18.625F, -5.75F)
      );
      root.addOrReplaceChild(
         "right_hind_leg", CubeListBuilder.create().texOffs(0, 34).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 3.0F, 3.0F), PartPose.offset(-2.5F, 21.5F, 4.5F)
      );
      root.addOrReplaceChild(
         "left_hind_leg", CubeListBuilder.create().texOffs(12, 34).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 3.0F, 3.0F), PartPose.offset(2.5F, 21.5F, 4.5F)
      );
      root.addOrReplaceChild(
         "right_front_leg", CubeListBuilder.create().texOffs(0, 28).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 3.0F, 3.0F), PartPose.offset(-2.5F, 21.5F, -4.5F)
      );
      root.addOrReplaceChild(
         "left_front_leg", CubeListBuilder.create().texOffs(12, 28).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 3.0F, 3.0F), PartPose.offset(2.5F, 21.5F, -4.5F)
      );
      return LayerDefinition.create(mesh, 64, 64);
      }

   public static LayerDefinition panda() {

      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 11).addBox(-4.5F, -3.5F, -5.5F, 9.0F, 7.0F, 11.0F), PartPose.offset(0.0F, 18.5F, 2.5F));
      root.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-3.5F, -3.0F, -5.0F, 7.0F, 6.0F, 5.0F)
            .texOffs(24, 6)
            .addBox(-2.0F, 1.0F, -6.0F, 4.0F, 2.0F, 1.0F)
            .texOffs(24, 0)
            .addBox(-4.5F, -4.0F, -3.5F, 3.0F, 3.0F, 1.0F)
            .texOffs(33, 0)
            .addBox(1.5F, -4.0F, -3.5F, 3.0F, 3.0F, 1.0F),
         PartPose.offset(0.0F, 19.0F, -3.0F)
      );
      root.addOrReplaceChild(
         "right_hind_leg", CubeListBuilder.create().texOffs(0, 34).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offset(-3.0F, 22.0F, 6.5F)
      );
      root.addOrReplaceChild(
         "left_hind_leg", CubeListBuilder.create().texOffs(12, 34).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offset(3.0F, 22.0F, 6.5F)
      );
      root.addOrReplaceChild(
         "right_front_leg", CubeListBuilder.create().texOffs(0, 29).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offset(-3.0F, 22.0F, -1.5F)
      );
      root.addOrReplaceChild(
         "left_front_leg", CubeListBuilder.create().texOffs(12, 29).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offset(3.0F, 22.0F, -1.5F)
      );
      return LayerDefinition.create(mesh, 64, 64);
      }

   public static LayerDefinition camel() {

      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition body = root.addOrReplaceChild(
         "body", CubeListBuilder.create().texOffs(0, 14).addBox(-4.5F, -4.0F, -8.0F, 9.0F, 8.0F, 16.0F), PartPose.offset(0.0F, 7.0F, 0.0F)
      );
      body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(50, 38).addBox(-1.5F, -0.5F, 0.0F, 3.0F, 9.0F, 0.0F), PartPose.offset(0.0F, -1.5F, 8.05F));
      PartDefinition head = body.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(20, 0)
            .addBox(-2.5F, -3.0F, -7.5F, 5.0F, 5.0F, 7.0F)
            .texOffs(0, 0)
            .addBox(-2.5F, -12.0F, -7.5F, 5.0F, 9.0F, 5.0F)
            .texOffs(0, 14)
            .addBox(-2.5F, -12.0F, -10.5F, 5.0F, 4.0F, 3.0F),
         PartPose.offset(0.0F, 1.0F, -7.5F)
      );
      head.addOrReplaceChild(
         "right_ear", CubeListBuilder.create().texOffs(37, 0).addBox(-3.0F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F), PartPose.offset(-2.5F, -11.0F, -4.0F)
      );
      head.addOrReplaceChild(
         "left_ear", CubeListBuilder.create().texOffs(47, 0).addBox(0.0F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F), PartPose.offset(2.5F, -11.0F, -4.0F)
      );
      root.addOrReplaceChild(
         "right_front_leg", CubeListBuilder.create().texOffs(36, 14).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 13.0F, 3.0F), PartPose.offset(-3.0F, 11.5F, -5.5F)
      );
      root.addOrReplaceChild(
         "left_front_leg", CubeListBuilder.create().texOffs(48, 14).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 13.0F, 3.0F), PartPose.offset(3.0F, 11.5F, -5.5F)
      );
      root.addOrReplaceChild(
         "left_hind_leg", CubeListBuilder.create().texOffs(12, 38).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 13.0F, 3.0F), PartPose.offset(3.0F, 11.5F, 5.5F)
      );
      root.addOrReplaceChild(
         "right_hind_leg", CubeListBuilder.create().texOffs(0, 38).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 13.0F, 3.0F), PartPose.offset(-3.0F, 11.5F, 5.5F)
      );
      return LayerDefinition.create(mesh, 64, 64);
      }

   public static LayerDefinition axolotl() {

      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition body = root.addOrReplaceChild(
         "body",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-2.0F, -0.75F, -2.75F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(0, 12)
            .addBox(0.0F, -1.75F, -2.75F, 0.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, -1.25F, 1.75F)
      );
      body.addOrReplaceChild(
         "right_front_leg",
         CubeListBuilder.create().texOffs(20, 16).addBox(-3.0F, 0.0F, -0.5F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 0.25F, -1.25F)
      );
      PartDefinition right_leg = body.addOrReplaceChild(
         "right_hind_leg", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.0F, 0.25F, 1.75F, 0.0F, 1.5708F, 1.5708F)
      );
      right_leg.addOrReplaceChild(
         "right_leg_r1",
         CubeListBuilder.create().texOffs(20, 14).addBox(0.0F, 0.0F, -0.5F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 1.5708F)
      );
      body.addOrReplaceChild(
         "left_front_leg",
         CubeListBuilder.create().texOffs(20, 13).addBox(0.0F, 0.0F, -0.5F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offset(2.0F, 0.25F, -1.25F)
      );
      body.addOrReplaceChild(
         "left_hind_leg",
         CubeListBuilder.create().texOffs(20, 14).addBox(0.0F, 0.0F, -0.5F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offset(2.0F, 0.25F, 1.75F)
      );
      body.addOrReplaceChild(
         "tail",
         CubeListBuilder.create().texOffs(10, 9).addBox(0.0F, -1.5F, -1.0F, 0.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, -0.25F, 3.25F)
      );
      PartDefinition head = body.addOrReplaceChild(
         "head",
         CubeListBuilder.create().texOffs(0, 8).addBox(-3.0F, -2.0F, -4.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 0.25F, -2.75F)
      );
      head.addOrReplaceChild(
         "left_gills",
         CubeListBuilder.create().texOffs(20, 8).addBox(0.0F, -3.5F, 0.0F, 3.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)),
         PartPose.offset(3.0F, -0.5F, -2.0F)
      );
      head.addOrReplaceChild(
         "right_gills",
         CubeListBuilder.create().texOffs(20, 3).addBox(-3.0F, -3.5F, 0.0F, 3.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-3.0F, -0.5F, -2.0F)
      );
      head.addOrReplaceChild(
         "top_gills",
         CubeListBuilder.create().texOffs(20, 0).addBox(-3.0F, -3.0F, 0.0F, 6.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, -2.0F, -2.0F)
      );
      return LayerDefinition.create(meshdefinition, 32, 32);
      }

   public static LayerDefinition bee() {

      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition bone = root.addOrReplaceChild(
         "bone",
         CubeListBuilder.create()
            .texOffs(6, 12)
            .addBox(1.0F, -1.6667F, -2.1633F, 1.0F, 2.0F, 2.0F)
            .texOffs(0, 12)
            .addBox(-2.0F, -1.6667F, -2.1933F, 1.0F, 2.0F, 2.0F),
         PartPose.offset(0.0F, 19.6667F, -1.8567F)
      );
      PartDefinition body = bone.addOrReplaceChild(
         "body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -2.5F, 4.0F, 4.0F, 5.0F), PartPose.offset(0.0F, 1.3333F, 2.3567F)
      );
      body.addOrReplaceChild("stinger", CubeListBuilder.create().texOffs(13, 2).addBox(0.0F, -0.5F, 0.0F, 0.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 0.5F, 2.5F));
      bone.addOrReplaceChild(
         "right_wing",
         CubeListBuilder.create().texOffs(3, 9).addBox(-3.0F, 0.0F, 0.0F, 3.0F, 0.0F, 3.0F),
         PartPose.offsetAndRotation(-1.0F, -0.6667F, 0.8567F, 0.2182F, 0.3491F, 0.0F)
      );
      bone.addOrReplaceChild(
         "left_wing",
         CubeListBuilder.create().texOffs(-3, 9).mirror().addBox(0.0F, 0.0F, 0.0F, 3.0F, 0.0F, 3.0F).mirror(false),
         PartPose.offsetAndRotation(1.0F, -0.6667F, 0.8567F, 0.2182F, -0.3491F, 0.0F)
      );
      bone.addOrReplaceChild(
         "front_legs", CubeListBuilder.create().texOffs(13, 0).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 1.0F, 0.0F), PartPose.offset(0.0F, 3.3333F, 1.8567F)
      );
      bone.addOrReplaceChild(
         "middle_legs", CubeListBuilder.create().texOffs(13, 1).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 1.0F, 0.0F), PartPose.offset(0.0F, 3.3333F, 2.8567F)
      );
      bone.addOrReplaceChild(
         "back_legs", CubeListBuilder.create().texOffs(13, 2).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 1.0F, 0.0F), PartPose.offset(0.0F, 3.3333F, 3.8567F)
      );
      return LayerDefinition.create(mesh, 32, 32);
      }

   public static LayerDefinition dolphin() {

      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition body = root.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(20, 0).addBox(-3.0F, -2.5F, -4.0F, 6.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 21.5F, 0.0F)
      );
      PartDefinition head = body.addOrReplaceChild(
         "head",
         CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.5F, -4.0F, 6.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 1.0F, -4.0F)
      );
      head.addOrReplaceChild(
         "nose",
         CubeListBuilder.create().texOffs(0, 9).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 0.5F, -4.0F)
      );
      body.addOrReplaceChild(
         "left_fin",
         CubeListBuilder.create().texOffs(34, 18).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(1.8F, 0.85F, -2.6F, 0.8727F, 0.0F, 1.7017F)
      );
      body.addOrReplaceChild(
         "right_fin",
         CubeListBuilder.create().texOffs(48, 18).mirror().addBox(-0.5F, -1.5F, -0.5F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offsetAndRotation(-1.8F, 0.85F, -2.6F, 0.8727F, 0.0F, -1.7017F)
      );
      PartDefinition tail = body.addOrReplaceChild(
         "tail",
         CubeListBuilder.create().texOffs(0, 13).addBox(-2.0F, -1.5F, 0.0F, 4.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 1.0F, 4.0F)
      );
      tail.addOrReplaceChild(
         "tail_fin",
         CubeListBuilder.create().texOffs(22, 13).addBox(-4.0F, -0.5F, -1.0F, 8.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 0.0F, 6.0F)
      );
      body.addOrReplaceChild(
         "back_fin",
         CubeListBuilder.create().texOffs(42, 0).addBox(-0.5F, -1.0F, 1.0F, 1.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, -1.0F, -2.7F, 0.8727F, 0.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 64, 64);
      }

   public static LayerDefinition squid() {

      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition root = meshdefinition.getRoot();
      PartDefinition body = root.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -5.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 13.0F, 0.0F)
      );
      int tentacleCount = 8;
      CubeListBuilder tentacle = CubeListBuilder.create().texOffs(0, 18).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 6.0F, 2.0F);

      for (int i = 0; i < 8; i++) {
         double angle = i * Math.PI * 2.0 / 8.0;
         float x = (float)Math.cos(angle) * 3.0F;
         float y = 18.5F;
         float z = (float)Math.sin(angle) * 3.0F;
         angle = i * Math.PI * -2.0 / 8.0 + (Math.PI / 2);
         float yRot = (float)angle;
         root.addOrReplaceChild("tentacle" + i, tentacle, PartPose.offsetAndRotation(x, 18.5F, z, 0.0F, yRot, 0.0F));
      }

      return LayerDefinition.create(meshdefinition, 32, 32);
      }

   public static LayerDefinition turtle() {

      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F), PartPose.offset(0.0F, 22.9F, 1.0F));
      root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 6).addBox(-1.5F, -2.0F, -3.0F, 3.0F, 3.0F, 3.0F), PartPose.offset(0.0F, 22.9F, -1.0F));
      root.addOrReplaceChild(
         "right_hind_leg", CubeListBuilder.create().texOffs(-1, 0).addBox(-2.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F), PartPose.offset(-2.0F, 23.9F, 2.5F)
      );
      root.addOrReplaceChild(
         "left_hind_leg", CubeListBuilder.create().texOffs(-1, 1).addBox(0.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F), PartPose.offset(2.0F, 23.9F, 2.5F)
      );
      root.addOrReplaceChild(
         "right_front_leg", CubeListBuilder.create().texOffs(8, 6).addBox(-2.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F), PartPose.offset(-2.0F, 23.9F, -0.5F)
      );
      root.addOrReplaceChild(
         "left_front_leg", CubeListBuilder.create().texOffs(8, 7).addBox(0.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F), PartPose.offset(2.0F, 23.9F, -0.5F)
      );
      return LayerDefinition.create(mesh, 16, 16);
      }

   public static LayerDefinition hoglin() {

      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition head = root.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-5.0F, -2.2605F, -10.547F, 10.0F, 4.0F, 12.0F)
            .texOffs(44, 29)
            .addBox(-7.0F, -4.0981F, -8.4879F, 2.0F, 5.0F, 2.0F)
            .texOffs(52, 29)
            .addBox(5.0F, -4.0981F, -8.4879F, 2.0F, 5.0F, 2.0F),
         PartPose.offsetAndRotation(0.0F, 13.0F, -7.0F, 0.8727F, 0.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "body",
         CubeListBuilder.create()
            .texOffs(0, 16)
            .addBox(-4.0F, -14.0F, -7.0F, 8.0F, 8.0F, 14.0F, new CubeDeformation(0.02F))
            .texOffs(24, 39)
            .addBox(0.0F, -18.0F, -8.0F, 0.0F, 6.0F, 11.0F, new CubeDeformation(0.02F)),
         PartPose.offset(0.0F, 24.0F, 0.0F)
      );
      head.addOrReplaceChild(
         "right_ear",
         CubeListBuilder.create().texOffs(32, 5).addBox(-5.1F, -0.5F, -2.0F, 6.0F, 1.0F, 4.0F),
         PartPose.offsetAndRotation(-5.0F, -1.0F, -1.5F, 0.0F, 0.0F, -0.8727F)
      );
      head.addOrReplaceChild(
         "left_ear",
         CubeListBuilder.create().texOffs(32, 0).mirror().addBox(-0.9F, -0.5F, -2.0F, 6.0F, 1.0F, 4.0F).mirror(false),
         PartPose.offsetAndRotation(5.0F, -1.0F, -1.5F, 0.0F, 0.0F, 0.8727F)
      );
      root.addOrReplaceChild(
         "right_hind_leg", CubeListBuilder.create().texOffs(0, 47).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), PartPose.offset(-2.5F, 18.0F, 4.5F)
      );
      root.addOrReplaceChild(
         "left_hind_leg", CubeListBuilder.create().texOffs(12, 47).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), PartPose.offset(2.5F, 18.0F, 4.5F)
      );
      root.addOrReplaceChild(
         "right_front_leg", CubeListBuilder.create().texOffs(0, 38).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), PartPose.offset(-2.5F, 18.0F, -4.5F)
      );
      root.addOrReplaceChild(
         "left_front_leg", CubeListBuilder.create().texOffs(12, 38).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), PartPose.offset(2.5F, 18.0F, -4.5F)
      );
      return LayerDefinition.create(mesh, 64, 64);
      }

   public static LayerDefinition piglin() {

      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 13).addBox(-3.0F, -3.0F, -1.0F, 6.0F, 5.0F, 3.0F), PartPose.offset(0.0F, 18.0F, -0.5F));
      PartDefinition head = root.addOrReplaceChild(
         "head",
         CubeListBuilder.create().texOffs(21, 30).addBox(-1.5F, -3.0F, -4.5F, 3.0F, 3.0F, 1.0F).texOffs(0, 0).addBox(-4.5F, -6.0F, -3.5F, 9.0F, 6.0F, 7.0F),
         PartPose.offset(0.0F, 15.0F, 0.0F)
      );
      head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition leftear = head.addOrReplaceChild("left_ear", CubeListBuilder.create(), PartPose.offset(4.2F, -4.0F, 0.0F));
      leftear.addOrReplaceChild(
         "left_ear_r1",
         CubeListBuilder.create().texOffs(0, 21).addBox(-0.5F, -3.0F, -2.0F, 1.0F, 6.0F, 4.0F),
         PartPose.offsetAndRotation(1.0F, 1.75F, 0.0F, 0.0F, 0.0F, -0.6109F)
      );
      PartDefinition rightear = head.addOrReplaceChild("right_ear", CubeListBuilder.create(), PartPose.offset(-4.2F, -4.0F, 0.0F));
      rightear.addOrReplaceChild(
         "right_ear_r1",
         CubeListBuilder.create().texOffs(18, 13).addBox(-0.5F, -3.0F, -2.0F, 1.0F, 6.0F, 4.0F),
         PartPose.offsetAndRotation(-1.0F, 1.75F, 0.0F, 0.0F, 0.0F, 0.6109F)
      );
      root.addOrReplaceChild(
         "left_arm", CubeListBuilder.create().texOffs(28, 13).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 5.0F, 3.0F), PartPose.offset(4.0F, 15.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "right_arm", CubeListBuilder.create().texOffs(10, 30).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 5.0F, 3.0F), PartPose.offset(-4.0F, 15.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "right_leg", CubeListBuilder.create().texOffs(22, 23).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F), PartPose.offset(-1.5F, 20.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "left_leg", CubeListBuilder.create().texOffs(10, 23).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F), PartPose.offset(1.5F, 20.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 64, 64);
      }

   public static LayerDefinition strider() {

      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition body = root.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -3.75F, -4.0F, 7.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 16.75F, 0.0F)
      );
      root.addOrReplaceChild(
         "right_leg",
         CubeListBuilder.create().texOffs(0, 24).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-1.5F, 20.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "left_leg",
         CubeListBuilder.create().texOffs(8, 24).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(1.5F, 20.0F, 0.0F)
      );
      body.addOrReplaceChild(
         "bristle0",
         CubeListBuilder.create().texOffs(0, 21).addBox(-3.5F, -2.5F, 0.0F, 7.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, -4.25F, 2.0F)
      );
      body.addOrReplaceChild(
         "bristle1",
         CubeListBuilder.create().texOffs(0, 18).addBox(-3.5F, -2.5F, 0.0F, 7.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, -4.25F, 0.0F)
      );
      body.addOrReplaceChild(
         "bristle2",
         CubeListBuilder.create().texOffs(0, 15).addBox(-3.5F, -2.5F, 0.0F, 7.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, -4.25F, -2.0F)
      );
      return LayerDefinition.create(mesh, 32, 32);
      }

   public static LayerDefinition zombie() {
      CubeDeformation g = CubeDeformation.NONE;

      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild(
         "body", CubeListBuilder.create().texOffs(16, 16).addBox(-2.0F, -2.5F, -1.0F, 4.0F, 5.0F, 2.0F, g), PartPose.offset(0.0F, 17.5F, 0.0F)
      );
      PartDefinition head = root.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(3, 3)
            .addBox(-3.0F, -6.25F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(35, 3)
            .addBox(-3.0F, -6.15F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.25F)),
         PartPose.offset(0.0F, 15.25F, 0.0F)
      );
      head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
      root.addOrReplaceChild(
         "right_arm", CubeListBuilder.create().texOffs(36, 16).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F, g), PartPose.offset(-3.0F, 15.5F, 0.0F)
      );
      root.addOrReplaceChild(
         "left_arm", CubeListBuilder.create().texOffs(28, 16).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F, g), PartPose.offset(3.0F, 15.5F, 0.0F)
      );
      root.addOrReplaceChild(
         "right_leg", CubeListBuilder.create().texOffs(8, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, g), PartPose.offset(-1.0F, 20.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "left_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, g), PartPose.offset(1.0F, 20.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 64, 64);
      }

   public static LayerDefinition llama() {
      CubeDeformation g = CubeDeformation.NONE;

      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-3.0F, -9.0F, -4.0F, 6.0F, 11.0F, 4.0F, g)
            .texOffs(0, 15)
            .addBox(-1.5F, -7.0F, -7.0F, 3.0F, 3.0F, 3.0F, g)
            .texOffs(20, 4)
            .addBox(0.5F, -11.0F, -3.0F, 2.0F, 2.0F, 2.0F, g)
            .texOffs(20, 0)
            .addBox(-2.5F, -11.0F, -3.0F, 2.0F, 2.0F, 2.0F, g),
         PartPose.offset(0.0F, 12.0F, -4.0F)
      );
      root.addOrReplaceChild(
         "right_hind_leg", CubeListBuilder.create().texOffs(0, 45).addBox(-1.4F, -0.5F, -1.5F, 3.0F, 8.0F, 3.0F, g), PartPose.offset(-2.5F, 16.5F, 4.5F)
      );
      root.addOrReplaceChild(
         "left_hind_leg", CubeListBuilder.create().texOffs(12, 45).addBox(-1.6F, -0.5F, -1.5F, 3.0F, 8.0F, 3.0F, g), PartPose.offset(2.5F, 16.5F, 4.5F)
      );
      root.addOrReplaceChild(
         "right_front_leg", CubeListBuilder.create().texOffs(0, 34).addBox(-1.4F, -0.5F, -1.5F, 3.0F, 8.0F, 3.0F, g), PartPose.offset(-2.5F, 16.5F, -3.5F)
      );
      root.addOrReplaceChild(
         "left_front_leg", CubeListBuilder.create().texOffs(12, 34).addBox(-1.6F, -0.5F, -1.5F, 3.0F, 8.0F, 3.0F, g), PartPose.offset(2.5F, 16.5F, -3.5F)
      );
      root.addOrReplaceChild(
         "body", CubeListBuilder.create().texOffs(0, 15).addBox(-4.0F, -3.0F, -8.5F, 8.0F, 6.0F, 13.0F, g), PartPose.offset(0.0F, 14.0F, 2.5F)
      );
      root.addOrReplaceChild(
         "right_chest",
         CubeListBuilder.create().texOffs(45, 28).addBox(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F, g),
         PartPose.offsetAndRotation(-8.5F, 4.0F, 3.0F, 0.0F, (float) (Math.PI / 2), 0.0F)
      );
      root.addOrReplaceChild(
         "left_chest",
         CubeListBuilder.create().texOffs(45, 41).addBox(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F, g),
         PartPose.offsetAndRotation(5.5F, 4.0F, 3.0F, 0.0F, (float) (Math.PI / 2), 0.0F)
      );
      return LayerDefinition.create(mesh, 64, 64);
      }
}
