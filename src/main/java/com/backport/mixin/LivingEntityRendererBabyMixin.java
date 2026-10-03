package com.backport.mixin;

import com.backport.client.BabyModels;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.SheepFurLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings({"unchecked", "rawtypes"})
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererBabyMixin<T extends LivingEntity, M extends EntityModel<T>> {
   @Shadow
   protected M model;

   @Unique
   private M backport$adult;
   @Unique
   private ResourceLocation backport$tex;
   @Unique
   private boolean backport$variantAdult;

   @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
   private void backport$swapIn(T entity, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int light, CallbackInfo ci) {
      this.backport$adult = null;
      this.backport$tex = null;
      this.backport$variantAdult = false;
      int variant = com.backport.variant.Variants.of(entity);
      if (!entity.isBaby()) {
         if (variant != 0) {
            EntityModel<?> vm = com.backport.client.VariantModels.model(entity.getType(), variant);
            if (vm != null) {
               this.backport$adult = this.model;
               this.model = (M) vm;
            }
            this.backport$tex = com.backport.client.VariantModels.texture(entity.getType(), variant);
            this.backport$variantAdult = true;
         }
         return;
      }
      EntityModel<?> baby = BabyModels.modelFor(entity.getType());
      if (baby == null) {
         return;
      }
      ResourceLocation tex = BabyModels.textureFor(variant != 0 ? com.backport.client.VariantModels.texture(entity.getType(), variant)
         : ((net.minecraft.client.renderer.entity.EntityRenderer<T>) (Object) this).getTextureLocation(entity));
      if (tex == null) {
         return;
      }
      this.backport$adult = this.model;
      this.model = (M) baby;
      this.backport$tex = tex;
      BabyModels.active = true;
      BabyModels.preAnim(baby);
   }

   @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("RETURN"))
   private void backport$swapOut(T entity, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int light, CallbackInfo ci) {
      if (this.backport$adult != null) {
         this.model = this.backport$adult;
         this.backport$adult = null;
         BabyModels.active = false;
      }
      this.backport$tex = null;
      this.backport$variantAdult = false;
   }

   @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
   private void backport$babyRenderType(T entity, boolean visible, boolean translucent, boolean glowing, CallbackInfoReturnable<RenderType> cir) {
      if (this.backport$tex != null) {
         if (!this.backport$variantAdult) {
            this.model.young = false;
            BabyModels.postAnim(this.model);
         }
         if (translucent) {
            cir.setReturnValue(RenderType.itemEntityTranslucentCull(this.backport$tex));
         } else if (visible) {
            cir.setReturnValue(this.model.renderType(this.backport$tex));
         } else {
            cir.setReturnValue(glowing ? RenderType.outline(this.backport$tex) : null);
         }
      }
   }

   @WrapWithCondition(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/Entity;FFFFFF)V"))
   private boolean backport$babyLayers(RenderLayer layer, PoseStack ps, MultiBufferSource buf, int light, net.minecraft.world.entity.Entity entity, float a, float b, float c, float d, float e, float f) {
      return this.backport$tex == null || this.backport$variantAdult || layer instanceof ItemInHandLayer || layer instanceof CustomHeadLayer || layer instanceof SheepFurLayer;
   }
}
