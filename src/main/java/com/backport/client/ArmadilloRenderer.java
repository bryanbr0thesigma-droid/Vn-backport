package com.backport.client;

import com.backport.Backport;
import com.backport.entity.Armadillo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ArmadilloRenderer extends MobRenderer<Armadillo, ArmadilloModel> {
   private static final ResourceLocation ADULT = Backport.id("textures/entity/armadillo/armadillo.png");
   private static final ResourceLocation BABY = Backport.id("textures/entity/armadillo/armadillo_baby.png");
   private final ArmadilloModel adultModel;
   private final ArmadilloModel babyModel;

   public ArmadilloRenderer(EntityRendererProvider.Context context) {
      super(context, new ArmadilloModel(context.bakeLayer(BackportClient.ARMADILLO_LAYER), false), 0.4F);
      this.adultModel = this.model;
      this.babyModel = new ArmadilloModel(context.bakeLayer(BackportClient.ARMADILLO_BABY_LAYER), true);
   }

   public void render(Armadillo armadillo, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffer, int light) {
      this.model = armadillo.isBaby() ? this.babyModel : this.adultModel;
      super.render(armadillo, yaw, partialTick, pose, buffer, light);
   }

   public ResourceLocation getTextureLocation(Armadillo armadillo) {
      return armadillo.isBaby() ? BABY : ADULT;
   }
}
