package com.vnap.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vnap.client.DialogueAnimationState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LivingEntityRenderer.class})
public abstract class VillagerRendererMixin {
   @Inject(
      method = {"render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"},
      at = {@At("HEAD")}
   )
   private void vnap$trackVillagerBody(
      LivingEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci
   ) {
      if (entity instanceof Villager villager) {
         float bodyRot = Mth.rotLerp(partialTick, villager.yBodyRotO, villager.yBodyRot);
         DialogueAnimationState.trackBodyRotation(villager, bodyRot, villager.tickCount + partialTick);
      }
   }
}
