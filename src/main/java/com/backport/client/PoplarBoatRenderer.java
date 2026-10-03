package com.backport.client;

import com.mojang.datafixers.util.Pair;
import com.backport.Backport;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;

public class PoplarBoatRenderer extends BoatRenderer {
   private final Pair<ResourceLocation, ListModel<Boat>> resources;

   public PoplarBoatRenderer(EntityRendererProvider.Context context, boolean chest) {
      super(context, chest);
      ResourceLocation texture = Backport.id("textures/entity/" + (chest ? "chest_boat" : "boat") + "/poplar.png");
      ListModel<Boat> model = chest
         ? new ChestBoatModel(context.bakeLayer(ModelLayers.createChestBoatModelName(Boat.Type.OAK)))
         : new BoatModel(context.bakeLayer(ModelLayers.createBoatModelName(Boat.Type.OAK)));
      this.resources = Pair.of(texture, model);
   }

   public Pair<ResourceLocation, ListModel<Boat>> getModelWithLocation(Boat boat) {
      return this.resources;
   }
}
