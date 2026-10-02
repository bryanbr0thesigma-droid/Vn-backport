package com.vnap.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vnap.client.RainbowNoseRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_model_features.models.parts.EMFModelPart;
import traben.entity_model_features.models.parts.EMFModelPartCustom;

@Mixin({EMFModelPart.class})
public abstract class EMFModelPartMixin {
   @Shadow
   public ResourceLocation textureOverride;
   @Unique
   private static final ResourceLocation VNAP$RAINBOW_TEXTURE = new ResourceLocation("villager-news-addon-port", "textures/entity/rainbow_nose.png");
   @Unique
   private ResourceLocation vnap$previousTexture;
   @Unique
   private boolean vnap$rainbowTextureActive;

   @Inject(
      method = {"render"},
      at = {@At("HEAD")}
   )
   private void vnap$beginRainbowTexture(PoseStack poseStack, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha, CallbackInfo ci) {
      if (this.vnap$isRainbowNose()) {
         this.vnap$previousTexture = this.textureOverride;
         this.textureOverride = VNAP$RAINBOW_TEXTURE;
         this.vnap$rainbowTextureActive = true;
      }
   }

   @Inject(
      method = {"render"},
      at = {@At("RETURN")}
   )
   private void vnap$endRainbowTexture(PoseStack poseStack, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha, CallbackInfo ci) {
      if (this.vnap$rainbowTextureActive) {
         this.textureOverride = this.vnap$previousTexture;
         this.vnap$previousTexture = null;
         this.vnap$rainbowTextureActive = false;
      }
   }

   @ModifyVariable(
      method = {"compile", "method_22702"},
      at = @At("HEAD"),
      argsOnly = true,
      remap = false,
      ordinal = 0
   )
   private int vnap$rainbowLight(int light) {
      return this.vnap$isRainbowNose() ? 15728880 : light;
   }

   @ModifyVariable(
      method = {"compile", "method_22702"},
      at = @At("HEAD"),
      argsOnly = true,
      remap = false,
      ordinal = 0
   )
   private float vnap$rainbowRed(float red) {
      return this.vnap$isRainbowNose() ? red * vnap$rainbow()[0] : red;
   }

   @ModifyVariable(
      method = {"compile", "method_22702"},
      at = @At("HEAD"),
      argsOnly = true,
      remap = false,
      ordinal = 1
   )
   private float vnap$rainbowGreen(float green) {
      return this.vnap$isRainbowNose() ? green * vnap$rainbow()[1] : green;
   }

   @ModifyVariable(
      method = {"compile", "method_22702"},
      at = @At("HEAD"),
      argsOnly = true,
      remap = false,
      ordinal = 2
   )
   private float vnap$rainbowBlue(float blue) {
      return this.vnap$isRainbowNose() ? blue * vnap$rainbow()[2] : blue;
   }

   @Unique
   private static float[] vnap$rainbow() {
      float phase = Mth.positiveModulo(RainbowNoseRenderState.cycleSeconds(), 6.0F);
      float x = 1.0F - Math.abs(phase % 2.0F - 1.0F);
      float red = phase < 1.0F ? 1.0F : (phase < 2.0F ? x : (phase < 4.0F ? 0.0F : (phase < 5.0F ? x : 1.0F)));
      float green = phase < 1.0F ? x : (phase < 3.0F ? 1.0F : (phase < 4.0F ? x : 0.0F));
      float blue = phase < 2.0F ? 0.0F : (phase < 3.0F ? x : (phase < 5.0F ? 1.0F : x));
      return new float[]{red, green, blue};
   }

   @Unique
   private boolean vnap$isRainbowNose() {
      return (Object)this instanceof EMFModelPartCustom part && part.id.contains("villager_news_base_fgk6") && RainbowNoseRenderState.active();
   }
}
