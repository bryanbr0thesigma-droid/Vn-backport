package com.backport;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CopperChestBlockEntity extends ChestBlockEntity {
   public static BlockEntityType<CopperChestBlockEntity> TYPE;

   public CopperChestBlockEntity(BlockPos pos, BlockState state) {
      super(TYPE, pos, state);
   }

   static void register(Block... blocks) {
      TYPE = net.minecraft.core.Registry.register(
         net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE, Backport.id("copper_chest"),
         FabricBlockEntityTypeBuilder.create(CopperChestBlockEntity::new, blocks).build()
      );
   }
}
