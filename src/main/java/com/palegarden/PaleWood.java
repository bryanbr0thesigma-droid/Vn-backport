package com.palegarden;

import net.fabricmc.fabric.api.object.builder.v1.block.type.BlockSetTypeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.type.WoodTypeRegistry;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

public final class PaleWood {
   public static final BlockSetType PALE_OAK_SET = BlockSetTypeRegistry.registerWood(PaleGarden.id("pale_oak"));
   public static final WoodType PALE_OAK = WoodTypeRegistry.register(PaleGarden.id("pale_oak"), PALE_OAK_SET);

   private PaleWood() {
   }

   public static void init() {
   }
}
