package com.palegarden;

import com.palegarden.entity.Creaking;
import com.palegarden.entity.PaleBoat;
import com.palegarden.entity.PaleChestBoat;
import com.palegarden.entity.CreakingHeartBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class PaleEntities {
   public static final EntityType<Creaking> CREAKING = Registry.register(
      BuiltInRegistries.ENTITY_TYPE,
      PaleGarden.id("creaking"),
      FabricEntityTypeBuilder.createMob()
         .entityFactory(Creaking::new)
         .spawnGroup(MobCategory.MONSTER)
         .dimensions(EntityDimensions.fixed(0.9F, 2.7F))
         .trackRangeBlocks(128)
         .build()
   );

   public static final EntityType<PaleBoat> PALE_OAK_BOAT = Registry.register(
      BuiltInRegistries.ENTITY_TYPE,
      PaleGarden.id("pale_oak_boat"),
      FabricEntityTypeBuilder.<PaleBoat>create(MobCategory.MISC, PaleBoat::new).dimensions(EntityDimensions.fixed(1.375F, 0.5625F)).trackRangeBlocks(10).build()
   );
   public static final EntityType<PaleChestBoat> PALE_OAK_CHEST_BOAT = Registry.register(
      BuiltInRegistries.ENTITY_TYPE,
      PaleGarden.id("pale_oak_chest_boat"),
      FabricEntityTypeBuilder.<PaleChestBoat>create(MobCategory.MISC, PaleChestBoat::new).dimensions(EntityDimensions.fixed(1.375F, 0.5625F)).trackRangeBlocks(10).build()
   );

   private PaleEntities() {
   }

   public static void init() {
      FabricDefaultAttributeRegistry.register(CREAKING, Creaking.createAttributes());
      CreakingHeartBlockEntity.init();
   }
}
