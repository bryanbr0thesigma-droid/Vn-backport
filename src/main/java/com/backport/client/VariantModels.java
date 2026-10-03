package com.backport.client;

import com.backport.Backport;
import com.backport.variant.Variants;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/** Adult cold/warm pig, cow and chicken models and textures (26.x). */
public final class VariantModels {
   private static final Map<String, EntityModel<?>> MODELS = new HashMap<>();

   private VariantModels() {
   }

   public static ResourceLocation texture(EntityType<?> type, int variant) {
      String n = type == EntityType.PIG ? "pig" : type == EntityType.COW ? "cow" : "chicken";
      return Backport.id("textures/entity/variant/" + n + (variant == Variants.COLD ? "_cold" : "_warm") + ".png");
   }

   /** The replacement adult model, or null when the vanilla model is kept and only the texture changes. */
   public static EntityModel<? extends Entity> model(EntityType<?> type, int variant) {
      if (type == EntityType.CHICKEN && variant != Variants.COLD) {
         return null;
      }
      return MODELS.computeIfAbsent(type + "/" + variant, k -> {
         boolean cold = variant == Variants.COLD;
         if (type == EntityType.PIG) {
            return new PigModel<Entity>(pig(cold).bakeRoot());
         } else if (type == EntityType.COW) {
            return new CowModel<Entity>(cow(cold).bakeRoot());
         }
         return new ChickenModel<Entity>(coldChicken().bakeRoot());
      });
   }

   private static LayerDefinition pig(boolean cold) {
      MeshDefinition mesh = QuadrupedModel.createBodyMesh(6, CubeDeformation.NONE);
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F).texOffs(16, 16).addBox(-2.0F, 0.0F, -9.0F, 4.0F, 3.0F, 1.0F),
         PartPose.offset(0.0F, 12.0F, -6.0F));
      if (cold) {
         root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(28, 8).addBox(-5.0F, -10.0F, -7.0F, 10.0F, 16.0F, 8.0F)
            .texOffs(28, 32).addBox(-5.0F, -10.0F, -7.0F, 10.0F, 16.0F, 8.0F, new CubeDeformation(0.5F)),
            PartPose.offsetAndRotation(0.0F, 11.0F, 2.0F, (float) (Math.PI / 2), 0.0F, 0.0F));
      }
      return LayerDefinition.create(mesh, 64, 64);
   }

   private static LayerDefinition cow(boolean cold) {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F);
      root.addOrReplaceChild("right_hind_leg", leg, PartPose.offset(-4.0F, 12.0F, 7.0F));
      root.addOrReplaceChild("left_hind_leg", leg, PartPose.offset(4.0F, 12.0F, 7.0F));
      root.addOrReplaceChild("right_front_leg", leg, PartPose.offset(-4.0F, 12.0F, -6.0F));
      root.addOrReplaceChild("left_front_leg", leg, PartPose.offset(4.0F, 12.0F, -6.0F));
      PartPose bodyPose = PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, (float) (Math.PI / 2), 0.0F, 0.0F);
      if (cold) {
         root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(20, 32).addBox(-6.0F, -10.0F, -7.0F, 12.0F, 18.0F, 10.0F, new CubeDeformation(0.5F))
            .texOffs(18, 4).addBox(-6.0F, -10.0F, -7.0F, 12.0F, 18.0F, 10.0F).texOffs(52, 0).addBox(-2.0F, 2.0F, -8.0F, 4.0F, 6.0F, 1.0F), bodyPose);
         PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -6.0F, 8.0F, 8.0F, 6.0F)
            .texOffs(9, 33).addBox(-3.0F, 1.0F, -7.0F, 6.0F, 3.0F, 1.0F), PartPose.offset(0.0F, 4.0F, -8.0F));
         head.addOrReplaceChild("right_horn", CubeListBuilder.create().texOffs(0, 40).addBox(-1.5F, -4.5F, -0.5F, 2.0F, 6.0F, 2.0F),
            PartPose.offsetAndRotation(-4.5F, -2.5F, -3.5F, 1.5708F, 0.0F, 0.0F));
         head.addOrReplaceChild("left_horn", CubeListBuilder.create().texOffs(0, 32).addBox(-1.5F, -3.0F, -0.5F, 2.0F, 6.0F, 2.0F),
            PartPose.offsetAndRotation(5.5F, -2.5F, -5.0F, 1.5708F, 0.0F, 0.0F));
      } else {
         root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(18, 4).addBox(-6.0F, -10.0F, -7.0F, 12.0F, 18.0F, 10.0F).texOffs(52, 0).addBox(-2.0F, 2.0F, -8.0F, 4.0F, 6.0F, 1.0F), bodyPose);
         root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -6.0F, 8.0F, 8.0F, 6.0F).texOffs(1, 33).addBox(-3.0F, 1.0F, -7.0F, 6.0F, 3.0F, 1.0F)
            .texOffs(27, 0).addBox(-8.0F, -3.0F, -5.0F, 4.0F, 2.0F, 2.0F).texOffs(39, 0).addBox(-8.0F, -5.0F, -5.0F, 2.0F, 2.0F, 2.0F)
            .texOffs(27, 0).mirror().addBox(4.0F, -3.0F, -5.0F, 4.0F, 2.0F, 2.0F).mirror(false)
            .texOffs(39, 0).mirror().addBox(6.0F, -5.0F, -5.0F, 2.0F, 2.0F, 2.0F).mirror(false), PartPose.offset(0.0F, 4.0F, -8.0F));
      }
      return LayerDefinition.create(mesh, 64, 64);
   }

   private static LayerDefinition coldChicken() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 6.0F, 3.0F).texOffs(44, 0).addBox(-3.0F, -7.0F, -2.015F, 6.0F, 3.0F, 4.0F),
         PartPose.offset(0.0F, 15.0F, -4.0F));
      root.addOrReplaceChild("beak", CubeListBuilder.create().texOffs(14, 0).addBox(-2.0F, -4.0F, -4.0F, 4.0F, 2.0F, 2.0F), PartPose.offset(0.0F, 15.0F, -4.0F));
      root.addOrReplaceChild("red_thing", CubeListBuilder.create().texOffs(14, 4).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(0.0F, 15.0F, -4.0F));
      root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 9).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 8.0F, 6.0F).texOffs(38, 9).addBox(0.0F, 3.0F, -1.0F, 0.0F, 3.0F, 5.0F),
         PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, (float) (Math.PI / 2), 0.0F, 0.0F));
      CubeListBuilder leg = CubeListBuilder.create().texOffs(26, 0).addBox(-1.0F, 0.0F, -3.0F, 3.0F, 5.0F, 3.0F);
      root.addOrReplaceChild("right_leg", leg, PartPose.offset(-2.0F, 19.0F, 1.0F));
      root.addOrReplaceChild("left_leg", leg, PartPose.offset(1.0F, 19.0F, 1.0F));
      root.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(24, 13).addBox(0.0F, 0.0F, -3.0F, 1.0F, 4.0F, 6.0F), PartPose.offset(-4.0F, 13.0F, 0.0F));
      root.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(24, 13).addBox(-1.0F, 0.0F, -3.0F, 1.0F, 4.0F, 6.0F), PartPose.offset(4.0F, 13.0F, 0.0F));
      return LayerDefinition.create(mesh, 64, 32);
   }
}
