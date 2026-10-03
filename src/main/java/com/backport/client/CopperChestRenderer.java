package com.backport.client;

import com.backport.Backport;
import com.backport.CopperChestBlock;
import com.backport.CopperChestBlockEntity;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.ChestType;

public class CopperChestRenderer extends ChestRenderer<CopperChestBlockEntity> {
   private static final String[] NAME = {"copper", "copper_exposed", "copper_weathered", "copper_oxidized"};

   public CopperChestRenderer(BlockEntityRendererProvider.Context context) {
      super(context);
   }

   protected Material getMaterial(CopperChestBlockEntity chest, ChestType type) {
      int age = chest.getBlockState().getBlock() instanceof CopperChestBlock block ? block.getWeatherState().ordinal() : 0;
      String suffix = type == ChestType.LEFT ? "_left" : type == ChestType.RIGHT ? "_right" : "";
      return new Material(Sheets.CHEST_SHEET, new ResourceLocation(Backport.ID, "entity/chest/" + NAME[age] + suffix));
   }
}
