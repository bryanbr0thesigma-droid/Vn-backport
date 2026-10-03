package com.backport.client;

import com.backport.Backport;
import com.backport.BackportItems;
import com.backport.entity.HappyGhast;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class HappyGhastRenderer extends MobRenderer<HappyGhast, HappyGhastModel> {
   private static final ResourceLocation ADULT = Backport.id("textures/entity/ghast/happy_ghast.png");
   private static final ResourceLocation BABY = Backport.id("textures/entity/ghast/happy_ghast_baby.png");
   private final HappyGhastModel babyModel;
   private final HappyGhastModel adultModel;

   public HappyGhastRenderer(EntityRendererProvider.Context ctx) {
      super(ctx, new HappyGhastModel(ctx.bakeLayer(BackportClient.HAPPY_GHAST_LAYER)), 2.0F);
      this.adultModel = this.model;
      this.babyModel = new HappyGhastModel(ctx.bakeLayer(BackportClient.HAPPY_GHAST_BABY_LAYER));
      this.addLayer(new HarnessLayer(this, new HappyGhastHarnessModel(ctx.bakeLayer(BackportClient.HAPPY_GHAST_HARNESS_LAYER))));
   }

   public void render(HappyGhast entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffer, int light) {
      this.model = entity.isBaby() ? this.babyModel : this.adultModel;
      super.render(entity, yaw, partial, pose, buffer, light);
   }

   protected void scale(HappyGhast entity, PoseStack pose, float partial) {
      float s = entity.isBaby() ? 4.0F * HappyGhast.BABY_SCALE : 4.0F;
      pose.scale(s, s, s);
   }

   public ResourceLocation getTextureLocation(HappyGhast entity) {
      return entity.isBaby() ? BABY : ADULT;
   }

   private static final class HarnessLayer extends RenderLayer<HappyGhast, HappyGhastModel> {
      private final HappyGhastHarnessModel harness;

      HarnessLayer(RenderLayerParent<HappyGhast, HappyGhastModel> parent, HappyGhastHarnessModel harness) {
         super(parent);
         this.harness = harness;
      }

      public void render(PoseStack pose, MultiBufferSource buffer, int light, HappyGhast entity, float limbSwing, float limbSwingAmount, float partial, float age, float yaw, float pitch) {
         if (!(entity.getHarness().getItem() instanceof BackportItems.HarnessItem item)) return;
         this.harness.setupAnim(entity, limbSwing, limbSwingAmount, age, yaw, pitch);
         ResourceLocation tex = Backport.id("textures/entity/equipment/happy_ghast_body/" + item.color + "_harness.png");
         VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(tex));
         this.harness.renderToBuffer(pose, vc, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      }
   }
}
