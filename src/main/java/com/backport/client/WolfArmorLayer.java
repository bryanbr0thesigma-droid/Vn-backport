package com.backport.client;

import com.backport.Backport;
import com.backport.WolfArmor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;

public class WolfArmorLayer extends RenderLayer<Wolf, WolfModel<Wolf>> {
   private static final ResourceLocation BASE = Backport.id("textures/entity/equipment/wolf_body/armadillo_scute.png");
   private static final ResourceLocation OVERLAY = Backport.id("textures/entity/equipment/wolf_body/armadillo_scute_overlay.png");
   private static final ResourceLocation[] CRACKS = {
      Backport.id("textures/entity/wolf/wolf_armor_crackiness_low.png"),
      Backport.id("textures/entity/wolf/wolf_armor_crackiness_medium.png"),
      Backport.id("textures/entity/wolf/wolf_armor_crackiness_high.png")
   };
   private final WolfModel<Wolf> model;

   public WolfArmorLayer(RenderLayerParent<Wolf, WolfModel<Wolf>> parent, EntityModelSet models) {
      super(parent);
      this.model = new WolfModel<>(models.bakeLayer(ModelLayers.WOLF));
   }

   public void render(PoseStack pose, MultiBufferSource buffer, int light, Wolf wolf, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
      ItemStack armor = WolfArmor.get(wolf);
      if (armor.isEmpty() || wolf.isBaby()) {
         return;
      }

      coloredCutoutModelCopyLayerRender(this.getParentModel(), this.model, BASE, pose, buffer, light, wolf, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTick, 1.0F, 1.0F, 1.0F);
      if (armor.getItem() instanceof DyeableLeatherItem dyeable && dyeable.hasCustomColor(armor)) {
         int color = dyeable.getColor(armor);
         coloredCutoutModelCopyLayerRender(this.getParentModel(), this.model, OVERLAY, pose, buffer, light, wolf, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTick,
            (color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F);
      }

      float ratio = 1.0F - (float)armor.getDamageValue() / (float)armor.getMaxDamage();
      int crack = ratio <= 0.25F ? 2 : ratio <= 0.5F ? 1 : ratio <= 0.75F ? 0 : -1;
      if (crack >= 0) {
         coloredCutoutModelCopyLayerRender(this.getParentModel(), this.model, CRACKS[crack], pose, buffer, light, wolf, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTick, 1.0F, 1.0F, 1.0F);
      }
   }
}
