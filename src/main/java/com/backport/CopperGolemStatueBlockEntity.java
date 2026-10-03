package com.backport;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CopperGolemStatueBlockEntity extends BlockEntity {
   public static BlockEntityType<CopperGolemStatueBlockEntity> TYPE;

   public CopperGolemStatueBlockEntity(BlockPos pos, BlockState state) {
      super(TYPE, pos, state);
   }

   static void register(Block... blocks) {
      TYPE = net.minecraft.core.Registry.register(
         net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE, Backport.id("copper_golem_statue"),
         FabricBlockEntityTypeBuilder.create(CopperGolemStatueBlockEntity::new, blocks).build()
      );
   }
}
