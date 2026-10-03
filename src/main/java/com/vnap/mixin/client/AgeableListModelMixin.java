package com.vnap.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.regex.Pattern;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_model_features.EMFAnimationApi;
import traben.entity_model_features.models.IEMFModel;
import traben.entity_model_features.utils.EMFEntity;

/**
 * The Wooly sheep model is built from a custom "root" part. 26.x draws every model from its root, but the 1.20.1
 * sheep model only draws its own head/body/leg lists, so the custom root part was never drawn (an invisible sheep).
 * For Wooly the root is drawn instead.
 */
@Mixin({AgeableListModel.class})
public abstract class AgeableListModelMixin {
   private static final Pattern VNAP$WOOLY = Pattern.compile("(?i)(Wooly|Wooly The Sheep)");

   @Inject(
      method = {"renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void vnap$renderWoolyRoot(
      PoseStack poseStack, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha, CallbackInfo ci
   ) {
      if ((Object)this instanceof SheepModel<?> && (Object)this instanceof IEMFModel emfModel && emfModel.emf$isEMFModel()) {
         EMFEntity entity = EMFAnimationApi.getCurrentEntity();
         if (entity != null && entity.etf$hasCustomName() && VNAP$WOOLY.matcher(entity.etf$getCustomName().getString()).matches()) {
            emfModel.emf$getEMFRootModel().render(poseStack, vertices, light, overlay, red, green, blue, alpha);
            ci.cancel();
         }
      }
   }
}
