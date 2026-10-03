package com.backport.client;

import com.backport.Backport;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;
import com.backport.ice.Frostbite;

public class FrostbiteRenderer extends AbstractZombieRenderer<Frostbite, ZombieModel<Frostbite>> {
   private static final ResourceLocation TEX = Backport.id("textures/entity/frostbite/frostbite.png");
   private static final ResourceLocation BABY_TEX = Backport.id("textures/entity/frostbite/frostbite_baby.png");

   public FrostbiteRenderer(EntityRendererProvider.Context ctx) {
      super(ctx, new ZombieModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)), new ZombieModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)), new ZombieModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)));
   }

   @Override
   public ResourceLocation getTextureLocation(Zombie zombie) {
      return zombie.isBaby() ? BABY_TEX : TEX;
   }
}
