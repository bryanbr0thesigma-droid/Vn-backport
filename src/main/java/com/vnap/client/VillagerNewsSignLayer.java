package com.vnap.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vnap.entity.VillagerNewsData;
import java.util.function.Consumer;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;
import traben.entity_model_features.models.IEMFModel;
import traben.entity_model_features.models.animation.EMFAttachment.Type;

public final class VillagerNewsSignLayer extends RenderLayer<Villager, VillagerModel<Villager>> {
   // Vanilla 1.20.1 has no pale oak sign; birch stands in for that slot.
   private static final ResourceLocation[] BOARD_TEXTURES = new ResourceLocation[]{
      sign("oak"),
      sign("spruce"),
      sign("birch"),
      sign("jungle"),
      sign("acacia"),
      sign("dark_oak"),
      sign("mangrove"),
      sign("cherry"),
      sign("birch"),
      sign("bamboo"),
      sign("crimson"),
      sign("warped")
   };
   private static final ResourceLocation TEXT_TEXTURE = new ResourceLocation("villager-news-addon-port", "textures/entity/sign_text.png");

   public VillagerNewsSignLayer(RenderLayerParent<Villager, VillagerModel<Villager>> renderer) {
      super(renderer);
   }

   @Override
   public void render(
      PoseStack poseStack,
      MultiBufferSource buffer,
      int packedLight,
      Villager villager,
      float limbSwing,
      float limbSwingAmount,
      float partialTick,
      float ageInTicks,
      float netHeadYaw,
      float headPitch
   ) {
      VillagerNewsData sign = (VillagerNewsData)villager;
      int type = sign.vnap$signType();
      int message = sign.vnap$signMessage();
      if (!villager.isInvisible() && !villager.isBaby() && type >= 0 && type < BOARD_TEXTURES.length && message >= 0 && message < 87) {
         poseStack.pushPose();
         VillagerModel<Villager> model = this.getParentModel();
         Consumer<PoseStack> positioner = model instanceof IEMFModel emfModel && emfModel.emf$isEMFModel()
            ? emfModel.emf$getEMFRootModel().getPositionerForAttachment(Type.VILLAGER)
            : null;
         if (positioner == null) {
            model.root().getChild("arms").translateAndRotate(poseStack);
         } else {
            positioner.accept(poseStack);
         }

         poseStack.translate(0.0F, 0.359375F, -0.109375F);
         poseStack.mulPose(Axis.XP.rotationDegrees(42.97F));
         drawBoard(poseStack.last(), buffer.getBuffer(RenderType.entityCutout(BOARD_TEXTURES[type])), packedLight);
         drawText(poseStack.last(), buffer.getBuffer(RenderType.entityCutout(TEXT_TEXTURE)), packedLight, message);
         poseStack.popPose();
      }
   }

   private static ResourceLocation sign(String wood) {
      return new ResourceLocation("textures/entity/signs/" + wood + ".png");
   }

   private static void drawBoard(PoseStack.Pose pose, VertexConsumer vertices, int light) {
      float left = -0.50625F;
      float right = 0.50625F;
      float top = -0.25625F;
      float bottom = 0.25625F;
      float front = -0.04792F;
      float back = 0.04792F;
      // UV regions follow the vanilla 64x32 sign texture: a 24x12x2 box with its texture origin at (0, 0).
      quad(pose, vertices, light, left, bottom, front, right, bottom, front, right, top, front, left, top, front, 2.0F / 64, 14.0F / 32, 26.0F / 64, 2.0F / 32, 0.0F, 0.0F, -1.0F);
      quad(pose, vertices, light, right, bottom, back, left, bottom, back, left, top, back, right, top, back, 28.0F / 64, 14.0F / 32, 52.0F / 64, 2.0F / 32, 0.0F, 0.0F, 1.0F);
      quad(pose, vertices, light, left, top, back, left, top, front, right, top, front, right, top, back, 2.0F / 64, 2.0F / 32, 26.0F / 64, 0.0F, 0.0F, -1.0F, 0.0F);
      quad(pose, vertices, light, left, bottom, front, left, bottom, back, right, bottom, back, right, bottom, front, 26.0F / 64, 2.0F / 32, 50.0F / 64, 0.0F, 0.0F, 1.0F, 0.0F);
      quad(pose, vertices, light, left, bottom, back, left, bottom, front, left, top, front, left, top, back, 0.0F, 14.0F / 32, 2.0F / 64, 2.0F / 32, -1.0F, 0.0F, 0.0F);
      quad(pose, vertices, light, right, bottom, front, right, bottom, back, right, top, back, right, top, front, 26.0F / 64, 14.0F / 32, 28.0F / 64, 2.0F / 32, 1.0F, 0.0F, 0.0F);
   }

   private static void drawText(PoseStack.Pose pose, VertexConsumer vertices, int light, int message) {
      float topV = message / 87.0F;
      float bottomV = (message + 1) / 87.0F;
      quad(
         pose,
         vertices,
         light,
         -0.5F,
         0.1875F,
         -0.06042F,
         0.5F,
         0.1875F,
         -0.06042F,
         0.5F,
         -0.1875F,
         -0.06042F,
         -0.5F,
         -0.1875F,
         -0.06042F,
         0.0F,
         bottomV,
         1.0F,
         topV,
         0.0F,
         0.0F,
         -1.0F
      );
   }

   private static void quad(
      PoseStack.Pose pose,
      VertexConsumer vertices,
      int light,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      float x4,
      float y4,
      float z4,
      float u1,
      float v1,
      float u2,
      float v2,
      float nx,
      float ny,
      float nz
   ) {
      vertex(pose, vertices, light, x1, y1, z1, u1, v1, nx, ny, nz);
      vertex(pose, vertices, light, x2, y2, z2, u2, v1, nx, ny, nz);
      vertex(pose, vertices, light, x3, y3, z3, u2, v2, nx, ny, nz);
      vertex(pose, vertices, light, x4, y4, z4, u1, v2, nx, ny, nz);
   }

   private static void vertex(PoseStack.Pose pose, VertexConsumer vertices, int light, float x, float y, float z, float u, float v, float nx, float ny, float nz) {
      vertices.vertex(pose.pose(), x, y, z).color(255, 255, 255, 255).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(pose.normal(), nx, ny, nz).endVertex();
   }
}
