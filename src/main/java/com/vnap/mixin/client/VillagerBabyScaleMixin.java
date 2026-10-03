package com.vnap.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The 26.3 baby villager is its own full-size baby model (villager_baby.jem), but 1.20.1 renders
 * babies as the adult model at half scale. This undoes that halving so the baby model keeps its proportions.
 */
@Mixin({VillagerRenderer.class})
public abstract class VillagerBabyScaleMixin {
   @Inject(
      method = {"scale(Lnet/minecraft/world/entity/npc/Villager;Lcom/mojang/blaze3d/vertex/PoseStack;F)V"},
      at = {@At("RETURN")}
   )
   private void vnap$keepBabyModelProportions(Villager villager, PoseStack poseStack, float partialTick, CallbackInfo ci) {
      if (villager.isBaby()) {
         poseStack.scale(2.0F, 2.0F, 2.0F);
      }
   }
}
