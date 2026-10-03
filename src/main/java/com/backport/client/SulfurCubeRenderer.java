package com.backport.client;

import com.backport.Backport;
import com.backport.entity.SulfurCube;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class SulfurCubeRenderer extends MobRenderer<SulfurCube, SulfurCubeRenderer.CubeModel> {
   public static final ModelLayerLocation OUTER = new ModelLayerLocation(Backport.id("sulfur_cube"), "outer");
   public static final ModelLayerLocation INNER = new ModelLayerLocation(Backport.id("sulfur_cube"), "inner");
   public static final ModelLayerLocation OUTER_SMALL = new ModelLayerLocation(Backport.id("sulfur_cube"), "outer_small");
   public static final ModelLayerLocation INNER_SMALL = new ModelLayerLocation(Backport.id("sulfur_cube"), "inner_small");
   private static final ResourceLocation TEX = Backport.id("textures/entity/sulfur_cube/sulfur_cube_outer.png");
   private static final ResourceLocation TEX_SMALL = Backport.id("textures/entity/sulfur_cube/sulfur_cube_outer_small.png");
   private static final ResourceLocation INNER_TEX = Backport.id("textures/entity/sulfur_cube/sulfur_cube_inner.png");
   private static final ResourceLocation INNER_TEX_SMALL = Backport.id("textures/entity/sulfur_cube/sulfur_cube_inner_small.png");
   private final CubeModel normal;
   private final CubeModel small;

   public SulfurCubeRenderer(EntityRendererProvider.Context ctx) {
      super(ctx, new CubeModel(ctx.bakeLayer(OUTER)), 0.4F);
      this.normal = this.model;
      this.small = new CubeModel(ctx.bakeLayer(OUTER_SMALL));
      this.addLayer(new InnerLayer(this, new CubeModel(ctx.bakeLayer(INNER)), new CubeModel(ctx.bakeLayer(INNER_SMALL))));
   }

   public static LayerDefinition outer(boolean small) {
      MeshDefinition mesh = new MeshDefinition();
      int s = small ? 10 : 18;
      mesh.getRoot().addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 0).addBox(-s / 2.0F, -s / 2.0F, -s / 2.0F, s, s, s), PartPose.ZERO);
      return LayerDefinition.create(mesh, small ? 64 : 128, small ? 64 : 128);
   }

   public static LayerDefinition inner(boolean small) {
      MeshDefinition mesh = new MeshDefinition();
      int s = small ? 8 : 16;
      mesh.getRoot().addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, small ? 20 : 36).addBox(-s / 2.0F, -s / 2.0F, -s / 2.0F, s, s, s), PartPose.ZERO);
      return LayerDefinition.create(mesh, small ? 64 : 128, small ? 64 : 128);
   }

   @Override
   public void render(SulfurCube cube, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int light) {
      this.model = cube.isBaby() ? this.small : this.normal;
      super.render(cube, yaw, pt, ps, buf, light);
   }

   @Override
   protected void scale(SulfurCube cube, PoseStack ps, float pt) {
      float c = cube.isBaby() ? 0.26F : 0.52F;
      ps.scale(0.999F, 0.999F, 0.999F);
      if (cube.isPrimed()) {
         float f = cube.getFuse() - pt + 1.0F;
         if (f < 10.0F && f > 0.0F) {
            float g = 1.0F - f / 10.0F;
            g = Mth.clamp(g, 0.0F, 1.0F);
            g *= g;
            g *= g;
            float s = 1.0F + g * 0.3F;
            ps.scale(s, s, s);
         }
      }
      // model origin is the cube centre; lift it from the feet (renderer later translates by -1.501)
      ps.translate(0.0F, 1.501F - c, 0.0F);
   }

   @Override
   protected boolean isShaking(SulfurCube cube) {
      return false;
   }

   @Override
   protected RenderType getRenderType(SulfurCube cube, boolean visible, boolean translucent, boolean glowing) {
      return RenderType.entityTranslucent(this.getTextureLocation(cube));
   }

   @Override
   public ResourceLocation getTextureLocation(SulfurCube cube) {
      return cube.isBaby() ? TEX_SMALL : TEX;
   }

   public static class CubeModel extends EntityModel<SulfurCube> {
      private final ModelPart root;

      public CubeModel(ModelPart root) {
         super(RenderType::entityTranslucent);
         this.root = root;
      }

      @Override
      public void setupAnim(SulfurCube e, float a, float b, float c, float d, float f) {
      }

      @Override
      public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, float r, float g, float bl, float al) {
         this.root.render(ps, vc, light, overlay, r, g, bl, al);
      }
   }

   private static class InnerLayer extends RenderLayer<SulfurCube, CubeModel> {
      private final CubeModel normal;
      private final CubeModel small;

      InnerLayer(RenderLayerParent<SulfurCube, CubeModel> parent, CubeModel normal, CubeModel small) {
         super(parent);
         this.normal = normal;
         this.small = small;
      }

      @Override
      public void render(PoseStack ps, MultiBufferSource buf, int light, SulfurCube cube, float limb, float limbSwing, float pt, float age, float yaw, float pitch) {
         if (cube.isInvisible()) {
            return;
         }
         boolean flash = cube.isPrimed() && (int) (cube.getFuse() / 5.0F) % 2 == 0;
         int overlay = flash ? OverlayTexture.pack(OverlayTexture.u(1.0F), 10) : LivingEntityRenderer.getOverlayCoords(cube, 0.0F);
         ItemStack body = cube.getBody();
         if (!body.isEmpty()) {
            ps.pushPose();
            if (body.getItem() instanceof BlockItem bi) {
               ps.mulPose(Axis.XP.rotationDegrees(180.0F));
               float s = cube.isBaby() ? 0.5F : 1.0F;
               ps.scale(s, s, s);
               ps.translate(-0.5F, -0.518F, -0.5F);
               net.minecraft.client.Minecraft.getInstance().getBlockRenderer().renderSingleBlock(bi.getBlock().defaultBlockState(), ps, buf, light, overlay);
            } else {
               ps.mulPose(Axis.XP.rotationDegrees(180.0F));
               ps.scale(cube.isBaby() ? 0.8F : 1.6F, cube.isBaby() ? 0.8F : 1.6F, cube.isBaby() ? 0.8F : 1.6F);
               net.minecraft.client.Minecraft.getInstance().getItemRenderer().renderStatic(body, ItemDisplayContext.FIXED, light, overlay, ps, buf, cube.level(), cube.getId());
            }
            ps.popPose();
         } else {
            CubeModel m = cube.isBaby() ? this.small : this.normal;
            ResourceLocation t = cube.isBaby() ? INNER_TEX_SMALL : INNER_TEX;
            m.renderToBuffer(ps, buf.getBuffer(RenderType.entityTranslucent(t)), light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
         }
      }
   }
}
