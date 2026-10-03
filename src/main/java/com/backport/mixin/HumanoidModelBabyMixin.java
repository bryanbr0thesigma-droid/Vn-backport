package com.backport.mixin;

import com.backport.client.BabyModels;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelBabyMixin {
   @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("RETURN"))
   private void backport$keepBabyPose(LivingEntity entity, float a, float b, float c, float d, float e, CallbackInfo ci) {
      float[][] poses = BabyModels.HUMANOID_POSES.get(this);
      if (poses == null) {
         return;
      }
      HumanoidModel<?> h = (HumanoidModel<?>) (Object) this;
      ModelPart[] parts = {h.head, h.body, h.rightArm, h.leftArm, h.rightLeg, h.leftLeg};
      for (int i = 0; i < parts.length; i++) {
         parts[i].setPos(poses[i][0], poses[i][1], poses[i][2]);
      }
   }
}
