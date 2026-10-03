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
   public static final EntityType<net.minecraft.world.entity.monster.Parched> PARCHED = Registry.register(
      BuiltInRegistries.ENTITY_TYPE,
      Backport.id("parched"),
      FabricEntityTypeBuilder.createMob().entityFactory(net.minecraft.world.entity.monster.Parched::new).spawnGroup(MobCategory.MONSTER)
         .spawnRestriction(SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules)
         .dimensions(EntityDimensions.fixed(0.6F, 1.99F)).trackRangeBlocks(8).build()
   );
   public static final EntityType<com.backport.entity.CamelHusk> CAMEL_HUSK = Registry.register(
      BuiltInRegistries.ENTITY_TYPE,
      Backport.id("camel_husk"),
      FabricEntityTypeBuilder.createMob().entityFactory(com.backport.entity.CamelHusk::new).spawnGroup(MobCategory.MONSTER)
         .spawnRestriction(SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.Mob::checkMobSpawnRules)
         .dimensions(EntityDimensions.fixed(1.7F, 2.375F)).trackRangeBlocks(10).build()
   );
   public static final EntityType<com.backport.entity.HappyGhast> HAPPY_GHAST = Registry.register(
      BuiltInRegistries.ENTITY_TYPE,
      Backport.id("happy_ghast"),
      FabricEntityTypeBuilder.createMob().entityFactory(com.backport.entity.HappyGhast::new).spawnGroup(MobCategory.CREATURE)
         .spawnRestriction(SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING, net.minecraft.world.entity.Mob::checkMobSpawnRules)
         .dimensions(EntityDimensions.fixed(4.0F, 4.0F)).trackRangeBlocks(10).build()
   );
   public static final EntityType<com.backport.entity.Nautilus> NAUTILUS = Registry.register(
      BuiltInRegistries.ENTITY_TYPE,
      Backport.id("nautilus"),
      FabricEntityTypeBuilder.createMob().entityFactory(com.backport.entity.Nautilus::new).spawnGroup(MobCategory.WATER_CREATURE)
         .spawnRestriction(SpawnPlacements.Type.IN_WATER, Heightmap.Types.OCEAN_FLOOR, com.backport.entity.AbstractNautilus::checkNautilusSpawnRules)
         .dimensions(EntityDimensions.fixed(0.875F, 0.95F)).trackRangeBlocks(10).build()
   );
   public static final EntityType<com.backport.entity.ZombieNautilus> ZOMBIE_NAUTILUS = Registry.register(
      BuiltInRegistries.ENTITY_TYPE,
      Backport.id("zombie_nautilus"),
      FabricEntityTypeBuilder.createMob().entityFactory(com.backport.entity.ZombieNautilus::new).spawnGroup(MobCategory.WATER_CREATURE)
         .spawnRestriction(SpawnPlacements.Type.IN_WATER, Heightmap.Types.OCEAN_FLOOR, com.backport.entity.AbstractNautilus::checkNautilusSpawnRules)
         .dimensions(EntityDimensions.fixed(0.875F, 0.95F)).trackRangeBlocks(10).build()
   );
   public static final EntityType<com.backport.entity.Cushion> CUSHION = Registry.register(
      BuiltInRegistries.ENTITY_TYPE,
      Backport.id("cushion"),
      FabricEntityTypeBuilder.<com.backport.entity.Cushion>create(MobCategory.MISC, com.backport.entity.Cushion::new).dimensions(EntityDimensions.fixed(1.0F, 0.25F)).trackRangeBlocks(10).trackedUpdateRate(Integer.MAX_VALUE).build()
   );
   public static final EntityType<com.backport.entity.Armadillo> ARMADILLO = Registry.register(
      BuiltInRegistries.ENTITY_TYPE,
      Backport.id("armadillo"),
      FabricEntityTypeBuilder.createMob().entityFactory(com.backport.entity.Armadillo::new).spawnGroup(MobCategory.CREATURE)
         .spawnRestriction(SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, (type, level, reason, pos, random) -> level.getRawBrightness(pos, 0) > 8 && level.getBlockState(pos.below()).is(net.minecraft.world.level.block.Blocks.SAND) || level.getBlockState(pos.below()).is(net.minecraft.tags.BlockTags.TERRACOTTA) || level.getBlockState(pos.below()).is(net.minecraft.world.level.block.Blocks.RED_SAND) || level.getBlockState(pos.below()).is(net.minecraft.world.level.block.Blocks.COARSE_DIRT) || level.getBlockState(pos.below()).is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK) || level.getBlockState(pos.below()).is(net.minecraft.world.level.block.Blocks.DIRT))
         .dimensions(EntityDimensions.fixed(0.7F, 0.65F)).trackRangeBlocks(10).build()
   );
   public static final EntityType<com.backport.entity.Breeze> BREEZE = Registry.register(
      BuiltInRegistries.ENTITY_TYPE,
      Backport.id("breeze"),
      FabricEntityTypeBuilder.createMob().entityFactory(com.backport.entity.Breeze::new).spawnGroup(MobCategory.MONSTER)
         .dimensions(EntityDimensions.fixed(0.6F, 1.77F)).trackRangeBlocks(10).build()
   );
   public static final EntityType<com.backport.entity.SulfurCube> SULFUR_CUBE = Registry.register(
      BuiltInRegistries.ENTITY_TYPE,
      Backport.id("sulfur_cube"),
      FabricEntityTypeBuilder.createMob().entityFactory(com.backport.entity.SulfurCube::new).spawnGroup(MobCategory.CREATURE)
         .spawnRestriction(SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, net.minecraft.world.entity.Mob::checkMobSpawnRules)
         .dimensions(EntityDimensions.fixed(1.04F, 1.04F)).trackRangeBlocks(10).build()
   );
   public static final Item SULFUR_CUBE_SPAWN_EGG = Backport.item("sulfur_cube_spawn_egg", new SpawnEggItem(SULFUR_CUBE, 0xCBD64C, 0x6F7B1F, new FabricItemSettings()));
   public static final Item BREEZE_SPAWN_EGG = Backport.item("breeze_spawn_egg", new SpawnEggItem(BREEZE, 0xAFC1E8, 0x9B9FE0, new FabricItemSettings()));
   public static final EntityType<com.backport.entity.BreezeWindCharge> BREEZE_WIND_CHARGE = Registry.register(
      BuiltInRegistries.ENTITY_TYPE,
      Backport.id("breeze_wind_charge"),
      FabricEntityTypeBuilder.<com.backport.entity.BreezeWindCharge>create(MobCategory.MISC, com.backport.entity.BreezeWindCharge::new)
         .dimensions(EntityDimensions.fixed(0.3125F, 0.3125F)).trackRangeBlocks(4).trackedUpdateRate(10).build()
   );
   public static final Item ARMADILLO_SPAWN_EGG = Backport.item("armadillo_spawn_egg", new SpawnEggItem(ARMADILLO, 0xAD716D, 0x984E4E, new FabricItemSettings()));
   public static final Item PARCHED_SPAWN_EGG = Backport.item("parched_spawn_egg", new SpawnEggItem(PARCHED, 0xE2C98B, 0xA68A52, new FabricItemSettings()));
   public static final Item CAMEL_HUSK_SPAWN_EGG = Backport.item("camel_husk_spawn_egg", new SpawnEggItem(CAMEL_HUSK, 0x8C7B4C, 0xD6BE79, new FabricItemSettings()));
   public static final Item HAPPY_GHAST_SPAWN_EGG = Backport.item("happy_ghast_spawn_egg", new SpawnEggItem(HAPPY_GHAST, 0xF2F2F2, 0xA8D7F0, new FabricItemSettings()));
   public static final Item NAUTILUS_SPAWN_EGG = Backport.item("nautilus_spawn_egg", new SpawnEggItem(NAUTILUS, 0xB6A58B, 0xE17F8B, new FabricItemSettings()));
   public static final Item ZOMBIE_NAUTILUS_SPAWN_EGG = Backport.item("zombie_nautilus_spawn_egg", new SpawnEggItem(ZOMBIE_NAUTILUS, 0x5C6E55, 0xA2B79A, new FabricItemSettings()));
   public static final Item BOGGED_SPAWN_EGG = Backport.item("bogged_spawn_egg", new SpawnEggItem(BOGGED, 0x8A9C6E, 0x7A5F43, new FabricItemSettings()));
   public static final Item COPPER_GOLEM_SPAWN_EGG = Backport.item("copper_golem_spawn_egg", new SpawnEggItem(COPPER_GOLEM, 0xB4693C, 0xE3A57A, new FabricItemSettings()));

   private BackportEntities() {
   }

   public static void init() {
      net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
         if (entity instanceof net.minecraft.world.entity.animal.Wolf wolf && !WolfArmor.get(wolf).isEmpty()) {
            wolf.spawnAtLocation(WolfArmor.get(wolf).copy());
            WolfArmor.set(wolf, net.minecraft.world.item.ItemStack.EMPTY);
         }
      });
      FabricDefaultAttributeRegistry.register(COPPER_GOLEM, CopperGolem.createAttributes());
      FabricDefaultAttributeRegistry.register(PARCHED, net.minecraft.world.entity.monster.Parched.createAttributes());
      FabricDefaultAttributeRegistry.register(CAMEL_HUSK, net.minecraft.world.entity.animal.camel.Camel.createAttributes());
      net.fabricmc.fabric.api.biome.v1.BiomeModifications.addSpawn(
         net.fabricmc.fabric.api.biome.v1.BiomeSelectors.includeByKey(net.minecraft.world.level.biome.Biomes.DESERT),
         MobCategory.MONSTER, PARCHED, 40, 2, 4);
      net.fabricmc.fabric.api.biome.v1.BiomeModifications.addSpawn(
         net.fabricmc.fabric.api.biome.v1.BiomeSelectors.includeByKey(net.minecraft.world.level.biome.Biomes.DESERT),
         MobCategory.MONSTER, CAMEL_HUSK, 5, 1, 1);
      FabricDefaultAttributeRegistry.register(HAPPY_GHAST, com.backport.entity.HappyGhast.createAttributes());
      FabricDefaultAttributeRegistry.register(NAUTILUS, com.backport.entity.AbstractNautilus.createAttributes());
      FabricDefaultAttributeRegistry.register(ZOMBIE_NAUTILUS, com.backport.entity.ZombieNautilus.createAttributes());
      net.fabricmc.fabric.api.biome.v1.BiomeModifications.addSpawn(
         net.fabricmc.fabric.api.biome.v1.BiomeSelectors.includeByKey(net.minecraft.world.level.biome.Biomes.OCEAN, net.minecraft.world.level.biome.Biomes.DEEP_OCEAN, net.minecraft.world.level.biome.Biomes.COLD_OCEAN,
            net.minecraft.world.level.biome.Biomes.DEEP_COLD_OCEAN, net.minecraft.world.level.biome.Biomes.LUKEWARM_OCEAN, net.minecraft.world.level.biome.Biomes.DEEP_LUKEWARM_OCEAN, net.minecraft.world.level.biome.Biomes.WARM_OCEAN,
            net.minecraft.world.level.biome.Biomes.FROZEN_OCEAN, net.minecraft.world.level.biome.Biomes.DEEP_FROZEN_OCEAN),
         MobCategory.WATER_CREATURE, NAUTILUS, 5, 1, 1);
      FabricDefaultAttributeRegistry.register(BOGGED, Bogged.createAttributes());
      FabricDefaultAttributeRegistry.register(SULFUR_CUBE, com.backport.entity.SulfurCube.createAttributes());
      FabricDefaultAttributeRegistry.register(BREEZE, com.backport.entity.Breeze.createAttributes());
      FabricDefaultAttributeRegistry.register(ARMADILLO, com.backport.entity.Armadillo.createAttributes());
      net.fabricmc.fabric.api.biome.v1.BiomeModifications.addSpawn(
         net.fabricmc.fabric.api.biome.v1.BiomeSelectors.includeByKey(net.minecraft.world.level.biome.Biomes.SAVANNA, net.minecraft.world.level.biome.Biomes.SAVANNA_PLATEAU,
            net.minecraft.world.level.biome.Biomes.WINDSWEPT_SAVANNA, net.minecraft.world.level.biome.Biomes.BADLANDS, net.minecraft.world.level.biome.Biomes.ERODED_BADLANDS, net.minecraft.world.level.biome.Biomes.WOODED_BADLANDS),
         MobCategory.CREATURE, ARMADILLO, 6, 2, 3);
      net.fabricmc.fabric.api.biome.v1.BiomeModifications.addSpawn(
         net.fabricmc.fabric.api.biome.v1.BiomeSelectors.includeByKey(net.minecraft.world.level.biome.Biomes.SWAMP, net.minecraft.world.level.biome.Biomes.MANGROVE_SWAMP),
         MobCategory.MONSTER, BOGGED, 30, 4, 4);
   }
}
