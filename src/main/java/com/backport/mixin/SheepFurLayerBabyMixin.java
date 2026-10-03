package com.backport.mixin;

import com.backport.client.BabyModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.SheepFurLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SheepFurLayer.class)
public abstract class SheepFurLayerBabyMixin extends RenderLayer<Sheep, SheepModel<Sheep>> {
   private static final ResourceLocation BABY_WOOL = new ResourceLocation("backport", "textures/entity/baby/sheep/sheep_wool_baby.png");

   protected SheepFurLayerBabyMixin(RenderLayerParent<Sheep, SheepModel<Sheep>> parent) {
      super(parent);
   }

   @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/animal/Sheep;FFFFFF)V", at = @At("HEAD"), cancellable = true)
   private void backport$baby(PoseStack ps, MultiBufferSource buf, int light, Sheep sheep, float limb, float limbSwing, float pt, float age, float yaw, float pitch, CallbackInfo ci) {
      if (!BabyModels.active || !sheep.isBaby()) {
         return;
      }
      ci.cancel();
      if (sheep.isSheared() || sheep.isInvisible()) {
         return;
      }
      float r;
      float g;
      float b;
      if (sheep.hasCustomName() && "jeb_".equals(sheep.getName().getString())) {
         int n = sheep.tickCount / 25 + sheep.getId();
         int len = DyeColor.values().length;
         float[] c1 = Sheep.getColorArray(DyeColor.byId(n % len));
         float[] c2 = Sheep.getColorArray(DyeColor.byId((n + 1) % len));
         float f = ((float) (sheep.tickCount % 25) + pt) / 25.0F;
         r = c1[0] * (1.0F - f) + c2[0] * f;
         g = c1[1] * (1.0F - f) + c2[1] * f;
         b = c1[2] * (1.0F - f) + c2[2] * f;
      } else {
         float[] c = Sheep.getColorArray(sheep.getColor());
         r = c[0];
         g = c[1];
         b = c[2];
      }
      renderColoredCutoutModel(this.getParentModel(), BABY_WOOL, ps, buf, light, sheep, r, g, b);
   }
}
