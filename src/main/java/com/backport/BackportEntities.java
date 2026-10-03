package com.backport;

import com.backport.entity.CopperGolem;
import net.minecraft.world.entity.monster.Bogged;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

public final class BackportEntities {
   public static final EntityType<CopperGolem> COPPER_GOLEM = Registry.register(
      BuiltInRegistries.ENTITY_TYPE,
      Backport.id("copper_golem"),
      FabricEntityTypeBuilder.createMob().entityFactory(CopperGolem::new).spawnGroup(MobCategory.MISC)
         .dimensions(EntityDimensions.fixed(0.49F, 0.98F)).trackRangeBlocks(10).build()
   );
   public static final EntityType<com.backport.entity.WindCharge> WIND_CHARGE = Registry.register(
      BuiltInRegistries.ENTITY_TYPE,
      Backport.id("wind_charge"),
      FabricEntityTypeBuilder.<com.backport.entity.WindCharge>create(MobCategory.MISC, com.backport.entity.WindCharge::new)
         .dimensions(EntityDimensions.fixed(0.3125F, 0.3125F)).trackRangeBlocks(4).trackedUpdateRate(10).build()
   );
   public static final EntityType<Bogged> BOGGED = Registry.register(
      BuiltInRegistries.ENTITY_TYPE,
      Backport.id("bogged"),
      FabricEntityTypeBuilder.createMob().entityFactory(Bogged::new).spawnGroup(MobCategory.MONSTER)
         .spawnRestriction(SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules)
         .dimensions(EntityDimensions.fixed(0.6F, 1.99F)).trackRangeBlocks(8).build()
   );
   public static final Item BOGGED_SPAWN_EGG = Backport.item("bogged_spawn_egg", new SpawnEggItem(BOGGED, 0x8A9C6E, 0x7A5F43, new FabricItemSettings()));
   public static final Item COPPER_GOLEM_SPAWN_EGG = Backport.item("copper_golem_spawn_egg", new SpawnEggItem(COPPER_GOLEM, 0xB4693C, 0xE3A57A, new FabricItemSettings()));

   private BackportEntities() {
   }

   public static void init() {
      FabricDefaultAttributeRegistry.register(COPPER_GOLEM, CopperGolem.createAttributes());
      FabricDefaultAttributeRegistry.register(BOGGED, Bogged.createAttributes());
      net.fabricmc.fabric.api.biome.v1.BiomeModifications.addSpawn(
         net.fabricmc.fabric.api.biome.v1.BiomeSelectors.includeByKey(net.minecraft.world.level.biome.Biomes.SWAMP, net.minecraft.world.level.biome.Biomes.MANGROVE_SWAMP),
         MobCategory.MONSTER, BOGGED, 30, 4, 4);
   }
}
