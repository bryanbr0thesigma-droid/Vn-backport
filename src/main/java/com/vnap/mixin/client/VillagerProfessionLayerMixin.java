package com.vnap.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The baby villager model uses its own texture layout, so the biome clothing (the brown robe) cannot be taken
 * from the adult biome textures. Babies read baby-layout copies from textures/entity/villager/type_baby instead.
 */
@Mixin({VillagerProfessionLayer.class})
public abstract class VillagerProfessionLayerMixin {
   private static final ThreadLocal<Boolean> VNAP$BABY = ThreadLocal.withInitial(() -> false);

   @Inject(
      method = {"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V"},
      at = {@At("HEAD")}
   )
   private void vnap$beginClothing(
      PoseStack poseStack,
      MultiBufferSource buffer,
      int packedLight,
      LivingEntity entity,
      float limbSwing,
      float limbSwingAmount,
      float partialTick,
      float ageInTicks,
      float netHeadYaw,
      float headPitch,
      CallbackInfo ci
   ) {
      VNAP$BABY.set(entity instanceof Villager villager && villager.isBaby());
   }

   @Inject(
      method = {"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V"},
      at = {@At("RETURN")}
   )
   private void vnap$endClothing(
      PoseStack poseStack,
      MultiBufferSource buffer,
      int packedLight,
      LivingEntity entity,
      float limbSwing,
      float limbSwingAmount,
      float partialTick,
      float ageInTicks,
      float netHeadYaw,
      float headPitch,
      CallbackInfo ci
   ) {
      VNAP$BABY.set(false);
   }

   @Inject(
      method = {"getResourceLocation(Ljava/lang/String;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/resources/ResourceLocation;"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void vnap$babyClothingTexture(String folder, ResourceLocation id, CallbackInfoReturnable<ResourceLocation> cir) {
      if (VNAP$BABY.get() && folder.equals("type")) {
         cir.setReturnValue(new ResourceLocation(id.getNamespace(), "textures/entity/villager/type_baby/" + id.getPath() + ".png"));
      }
   }
}
