package com.backport.client;

import com.backport.Backport;
import com.backport.entity.WindCharge;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class WindChargeRenderer extends EntityRenderer<WindCharge> {
   private static final ResourceLocation TEXTURE = Backport.id("textures/entity/projectiles/wind_charge.png");
   private final ModelPart root;
   private final ModelPart wind;
   private final ModelPart charge;

   public WindChargeRenderer(EntityRendererProvider.Context context) {
      super(context);
      this.root = context.bakeLayer(BackportClient.WIND_CHARGE_LAYER);
      this.wind = this.root.getChild("bone").getChild("wind");
      this.charge = this.root.getChild("bone").getChild("wind_charge");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition bone = root.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      bone.addOrReplaceChild(
         "wind",
         CubeListBuilder.create().texOffs(15, 20).addBox(-4.0F, -1.0F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(0, 9).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F)
      );
      bone.addOrReplaceChild("wind_charge", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
      return LayerDefinition.create(mesh, 64, 32);
   }

   public void render(WindCharge entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffer, int light) {
      float age = entity.tickCount + partialTick;
      this.charge.yRot = -age * 16.0F * (float) (Math.PI / 180.0);
      this.wind.yRot = age * 16.0F * (float) (Math.PI / 180.0);
      pose.pushPose();
      pose.translate(0.0F, 0.15F, 0.0F);
      pose.scale(0.0625F * 16.0F * 0.0625F * 16.0F, 0.0625F * 16.0F * 0.0625F * 16.0F, 0.0625F * 16.0F * 0.0625F * 16.0F);
      this.root.render(pose, buffer.getBuffer(RenderType.energySwirl(TEXTURE, (age * 0.03F) % 1.0F, 0.0F)), 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      pose.popPose();
      super.render(entity, yaw, partialTick, pose, buffer, light);
   }

   public ResourceLocation getTextureLocation(WindCharge entity) {
      return TEXTURE;
   }
}
