package com.backport.client;

import com.backport.Backport;
import com.backport.CopperGolemStatueBlock;
import com.backport.CopperGolemStatueBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class CopperGolemStatueRenderer implements BlockEntityRenderer<CopperGolemStatueBlockEntity> {
   private static final String[] SUFFIX = {"", "_exposed", "_weathered", "_oxidized"};
   private final Map<CopperGolemStatueBlock.Pose, CopperGolemModel> models = new EnumMap<>(CopperGolemStatueBlock.Pose.class);

   public CopperGolemStatueRenderer(BlockEntityRendererProvider.Context context) {
      EntityModelSet set = context.getModelSet();
      this.models.put(CopperGolemStatueBlock.Pose.STANDING, new CopperGolemModel(set.bakeLayer(BackportClient.COPPER_GOLEM_LAYER)));
      this.models.put(CopperGolemStatueBlock.Pose.RUNNING, new CopperGolemModel(set.bakeLayer(BackportClient.COPPER_GOLEM_RUNNING_LAYER)));
      this.models.put(CopperGolemStatueBlock.Pose.SITTING, new CopperGolemModel(set.bakeLayer(BackportClient.COPPER_GOLEM_SITTING_LAYER)));
      this.models.put(CopperGolemStatueBlock.Pose.STAR, new CopperGolemModel(set.bakeLayer(BackportClient.COPPER_GOLEM_STAR_LAYER)));
   }

   public void render(CopperGolemStatueBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
      BlockState state = be.getBlockState();
      if (!(state.getBlock() instanceof CopperGolemStatueBlock block)) {
         return;
      }

      pose.pushPose();
      pose.translate(0.5F, 0.0F, 0.5F);
      pose.mulPose(Axis.YP.rotationDegrees(-state.getValue(CopperGolemStatueBlock.FACING).getOpposite().toYRot()));
      renderModel(this.models.get(state.getValue(CopperGolemStatueBlock.POSE)), block.getWeatheringState().ordinal(), pose, buffer, light, overlay);
      pose.popPose();
   }

   public static void renderModel(CopperGolemModel model, int age, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
      model.root().getAllParts().forEach(net.minecraft.client.model.geom.ModelPart::resetPose);
      model.root().y = 0.0F;
      model.root().zRot = (float) Math.PI;
      ResourceLocation texture = Backport.id("textures/entity/copper_golem/copper_golem" + SUFFIX[age] + ".png");
      model.renderToBuffer(pose, buffer.getBuffer(RenderType.entityCutout(texture)), light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
   }
}
