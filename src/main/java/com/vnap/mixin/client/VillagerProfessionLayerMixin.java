package com.vnap.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Baby villagers use their own full baby model (villager_baby.jem). The vanilla profession/biome clothing
 * layer would draw that model a second time with adult-layout clothing textures, so it is skipped for babies.
 */
@Mixin({VillagerProfessionLayer.class})
public abstract class VillagerProfessionLayerMixin {
   @Inject(
      method = {"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void vnap$skipBabyClothing(
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
      if (entity instanceof Villager villager && villager.isBaby()) {
         ci.cancel();
      }
   }
}
