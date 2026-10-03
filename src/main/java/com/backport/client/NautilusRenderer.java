package com.backport.client;

import com.backport.Backport;
import com.backport.BackportItems;
import com.backport.entity.AbstractNautilus;
import com.backport.entity.ZombieNautilus;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class NautilusRenderer extends MobRenderer<AbstractNautilus, NautilusModel> {
   private final NautilusModel adult;
   private final NautilusModel baby;
   private final NautilusModel coral;

   public NautilusRenderer(EntityRendererProvider.Context ctx) {
      super(ctx, new NautilusModel(ctx.bakeLayer(BackportClient.NAUTILUS_LAYER)), 0.7F);
      this.adult = this.model;
      this.coral = new NautilusModel(ctx.bakeLayer(BackportClient.NAUTILUS_CORAL_LAYER));
      this.baby = new NautilusModel(ctx.bakeLayer(BackportClient.NAUTILUS_BABY_LAYER));
      this.addLayer(new Equip(this, new NautilusModel(ctx.bakeLayer(BackportClient.NAUTILUS_ARMOR_LAYER)), true));
      this.addLayer(new Equip(this, new NautilusModel(ctx.bakeLayer(BackportClient.NAUTILUS_SADDLE_LAYER)), false));
   }

   public void render(AbstractNautilus entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffer, int light) {
      this.model = entity.isBaby() ? this.baby : entity instanceof ZombieNautilus z && z.isCoral() ? this.coral : this.adult;
      super.render(entity, yaw, partial, pose, buffer, light);
   }

   public ResourceLocation getTextureLocation(AbstractNautilus entity) {
      if (entity instanceof ZombieNautilus z) {
         return Backport.id(z.isCoral() ? "textures/entity/nautilus/zombie_nautilus_coral.png" : "textures/entity/nautilus/zombie_nautilus.png");
      }
      return Backport.id(entity.isBaby() ? "textures/entity/nautilus/nautilus_baby.png" : "textures/entity/nautilus/nautilus.png");
   }

   private static final class Equip extends RenderLayer<AbstractNautilus, NautilusModel> {
      private final NautilusModel layer;
      private final boolean armor;

      Equip(RenderLayerParent<AbstractNautilus, NautilusModel> parent, NautilusModel layer, boolean armor) {
         super(parent);
         this.layer = layer;
         this.armor = armor;
      }

      public void render(PoseStack pose, MultiBufferSource buffer, int light, AbstractNautilus entity, float limbSwing, float limbSwingAmount, float partial, float age, float yaw, float pitch) {
         if (entity.isBaby()) return;
         ItemStack stack = this.armor ? entity.getArmorItem() : entity.getSaddleItem();
         if (stack.isEmpty()) return;
         String tex;
         if (this.armor) {
            Item it = stack.getItem();
            String name = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(it).getPath().replace("_nautilus_armor", "");
            tex = "textures/entity/equipment/nautilus_body/" + (name.equals("golden") ? "gold" : name) + ".png";
         } else {
            tex = "textures/entity/equipment/nautilus_saddle/saddle.png";
         }
         this.getParentModel().copyPropertiesTo(this.layer);
         this.layer.setupAnim(entity, limbSwing, limbSwingAmount, age, yaw, pitch);
         VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(Backport.id(tex)));
         this.layer.renderToBuffer(pose, vc, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      }
   }
}
