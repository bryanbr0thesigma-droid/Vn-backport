package com.backport.poplar;

import com.backport.Backport;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class PoplarBoats {
   public static final EntityType<PoplarBoat> BOAT = Registry.register(BuiltInRegistries.ENTITY_TYPE, Backport.id("poplar_boat"),
      FabricEntityTypeBuilder.<PoplarBoat>create(MobCategory.MISC, PoplarBoat::new).dimensions(EntityDimensions.fixed(1.375F, 0.5625F)).trackRangeBlocks(10).build());
   public static final EntityType<PoplarChestBoat> CHEST_BOAT = Registry.register(BuiltInRegistries.ENTITY_TYPE, Backport.id("poplar_chest_boat"),
      FabricEntityTypeBuilder.<PoplarChestBoat>create(MobCategory.MISC, PoplarChestBoat::new).dimensions(EntityDimensions.fixed(1.375F, 0.5625F)).trackRangeBlocks(10).build());

   private PoplarBoats() {
   }

   public static void init() {
   }
}
