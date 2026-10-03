package com.backport.worldgen;

import com.backport.Backport;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public final class BackportWorldgen {
   public static final TrunkPlacerType<PoplarTrunkPlacer> POPLAR_TRUNK = Registry.register(BuiltInRegistries.TRUNK_PLACER_TYPE, Backport.id("poplar_trunk_placer"), new TrunkPlacerType<>(PoplarTrunkPlacer.CODEC));
   public static final FoliagePlacerType<PoplarFoliagePlacer> POPLAR_FOLIAGE = Registry.register(BuiltInRegistries.FOLIAGE_PLACER_TYPE, Backport.id("poplar_foliage_placer"), new FoliagePlacerType<>(PoplarFoliagePlacer.CODEC));
   public static final TreeDecoratorType<ShelfMushroomDecorator> SHELF_MUSHROOM = Registry.register(BuiltInRegistries.TREE_DECORATOR_TYPE, Backport.id("shelf_mushroom"), new TreeDecoratorType<>(ShelfMushroomDecorator.CODEC));
   public static final TreeDecoratorType<AttachedToLogsDecorator> ATTACHED_TO_LOGS = Registry.register(BuiltInRegistries.TREE_DECORATOR_TYPE, Backport.id("attached_to_logs"), new TreeDecoratorType<>(AttachedToLogsDecorator.CODEC));
   public static final Feature<FallenTreeFeature.Config> FALLEN_TREE = Registry.register(BuiltInRegistries.FEATURE, Backport.id("fallen_tree"), new FallenTreeFeature());

   public static final TreeDecoratorType<PlaceOnGroundDecorator> PLACE_ON_GROUND = Registry.register(BuiltInRegistries.TREE_DECORATOR_TYPE, Backport.id("place_on_ground"), new TreeDecoratorType<>(PlaceOnGroundDecorator.CODEC));
   public static final net.minecraft.world.level.levelgen.placement.PlacementModifierType<OffsetPlacement> OFFSET = Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, Backport.id("offset"), () -> OffsetPlacement.CODEC);

   private BackportWorldgen() {
   }

   public static void init() {
      TrapezoidInt.register();
      SulfurFeatures.init();
      BelowHeightmapPredicate.init();
   }
}
