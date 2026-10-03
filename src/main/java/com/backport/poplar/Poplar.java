package com.backport.poplar;

import com.backport.Backport;
import com.backport.BackportParticles;
import com.backport.PlantBlocks;
import com.palegarden.block.ParticleLeavesBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.type.BlockSetTypeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.type.WoodTypeRegistry;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;

/** The poplar wood set of the dappled forest. */
public final class Poplar {
   public static final BlockSetType SET = BlockSetTypeRegistry.registerWood(Backport.id("poplar"));
   public static final WoodType WOOD = WoodTypeRegistry.register(Backport.id("poplar"), SET);

   private static BlockBehaviour.Properties planks() {
      return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava();
   }

   private static BlockBehaviour.Properties log(MapColor top, MapColor side) {
      return BlockBehaviour.Properties.of().mapColor(s -> s.getValue(RotatedPillarBlock.AXIS) == net.minecraft.core.Direction.Axis.Y ? top : side).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava();
   }

   private static BlockBehaviour.Properties leaves(MapColor color) {
      return BlockBehaviour.Properties.of().mapColor(color).strength(0.2F).randomTicks().sound(PlantBlocks.POPLAR_LEAVES_SOUNDS).noOcclusion()
         .isValidSpawn((s, l, p, t) -> t == EntityType.OCELOT || t == EntityType.PARROT).isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p) -> false).ignitedByLava()
         .pushReaction(PushReaction.DESTROY).isRedstoneConductor((s, l, p) -> false);
   }

   public static final Block PLANKS = Backport.block("poplar_planks", new Block(planks()));
   public static final Block LOG = Backport.block("poplar_log", new RotatedPillarBlock(log(MapColor.COLOR_LIGHT_GRAY, MapColor.PODZOL)));
   public static final Block WOOD_BLOCK = Backport.block("poplar_wood", new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PODZOL).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()));
   public static final Block STRIPPED_LOG = Backport.block("stripped_poplar_log", new RotatedPillarBlock(log(MapColor.COLOR_LIGHT_GRAY, MapColor.COLOR_LIGHT_GRAY)));
   public static final Block STRIPPED_WOOD = Backport.block("stripped_poplar_wood", new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()));
   public static final Block RED_LEAVES = Backport.block("red_poplar_leaves", new ParticleLeavesBlock(50, BackportParticles.RED_POPLAR_LEAVES, leaves(MapColor.COLOR_RED)));
   public static final Block ORANGE_LEAVES = Backport.block("orange_poplar_leaves", new ParticleLeavesBlock(50, BackportParticles.ORANGE_POPLAR_LEAVES, leaves(MapColor.COLOR_ORANGE)));
   public static final Block YELLOW_LEAVES = Backport.block("yellow_poplar_leaves", new ParticleLeavesBlock(50, BackportParticles.YELLOW_POPLAR_LEAVES, leaves(MapColor.COLOR_YELLOW)));
   public static final Block SAPLING = Backport.block("poplar_sapling", new SaplingBlock(new Grower(),
      BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));
   public static final Block POTTED_SAPLING = Backport.blockNoItem("potted_poplar_sapling", new FlowerPotBlock(SAPLING, BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));
   public static final Block STAIRS = Backport.block("poplar_stairs", new StairBlock(PLANKS.defaultBlockState(), BlockBehaviour.Properties.copy(PLANKS)));
   public static final Block SLAB = Backport.block("poplar_slab", new SlabBlock(planks()));
   public static final Block FENCE = Backport.block("poplar_fence", new FenceBlock(planks().forceSolidOn()));
   public static final Block FENCE_GATE = Backport.block("poplar_fence_gate", new FenceGateBlock(planks().forceSolidOn(), WOOD));
   public static final Block TRAPDOOR = Backport.block("poplar_trapdoor", new TrapDoorBlock(planks().strength(3.0F).noOcclusion().isValidSpawn((s, l, p, t) -> false), SET));
   public static final Block PRESSURE_PLATE = Backport.block("poplar_pressure_plate", new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, planks().forceSolidOn().noCollission().strength(0.5F).pushReaction(PushReaction.DESTROY), SET));
   public static final Block BUTTON = Backport.block("poplar_button", new ButtonBlock(BlockBehaviour.Properties.of().noCollission().strength(0.5F).pushReaction(PushReaction.DESTROY), SET, 30, true));
   public static final Block DOOR = Backport.blockNoItem("poplar_door", new DoorBlock(planks().strength(3.0F).noOcclusion().pushReaction(PushReaction.DESTROY), SET));
   public static final Block SIGN = Backport.blockNoItem("poplar_sign", new StandingSignBlock(planks().forceSolidOn().noCollission().strength(1.0F), WOOD));
   public static final Block WALL_SIGN = Backport.blockNoItem("poplar_wall_sign", new WallSignBlock(planks().forceSolidOn().noCollission().strength(1.0F).dropsLike(SIGN), WOOD));
   public static final Block HANGING_SIGN = Backport.blockNoItem("poplar_hanging_sign", new CeilingHangingSignBlock(planks().forceSolidOn().noCollission().strength(1.0F), WOOD));
   public static final Block WALL_HANGING_SIGN = Backport.blockNoItem("poplar_wall_hanging_sign", new WallHangingSignBlock(planks().forceSolidOn().noCollission().strength(1.0F).dropsLike(HANGING_SIGN), WOOD));
   public static final Item DOOR_ITEM = Backport.item("poplar_door", new DoubleHighBlockItem(DOOR, new FabricItemSettings()));
   public static final Item SIGN_ITEM = Backport.item("poplar_sign", new SignItem(new FabricItemSettings().maxCount(16), SIGN, WALL_SIGN));
   public static final Item HANGING_SIGN_ITEM = Backport.item("poplar_hanging_sign", new HangingSignItem(HANGING_SIGN, WALL_HANGING_SIGN, new FabricItemSettings().maxCount(16)));
   public static final Item BOAT = Backport.item("poplar_boat", new PoplarBoatItem(false, new FabricItemSettings().maxCount(1)));
   public static final Item CHEST_BOAT = Backport.item("poplar_chest_boat", new PoplarBoatItem(true, new FabricItemSettings().maxCount(1)));

   private Poplar() {
   }

   public static void init() {
      StrippableBlockRegistry.register(LOG, STRIPPED_LOG);
      StrippableBlockRegistry.register(WOOD_BLOCK, STRIPPED_WOOD);
      FlammableBlockRegistry fl = FlammableBlockRegistry.getDefaultInstance();
      for (Block b : new Block[]{LOG, WOOD_BLOCK, STRIPPED_LOG, STRIPPED_WOOD}) fl.add(b, 5, 5);
      for (Block b : new Block[]{PLANKS, SLAB, FENCE_GATE, FENCE, STAIRS}) fl.add(b, 5, 20);
      for (Block b : new Block[]{RED_LEAVES, ORANGE_LEAVES, YELLOW_LEAVES}) fl.add(b, 30, 60);
      FuelRegistry fuel = FuelRegistry.INSTANCE;
      for (net.minecraft.world.level.ItemLike i : new net.minecraft.world.level.ItemLike[]{PLANKS, LOG, WOOD_BLOCK, STRIPPED_LOG, STRIPPED_WOOD, STAIRS, FENCE, FENCE_GATE, DOOR_ITEM, TRAPDOOR, PRESSURE_PLATE, SIGN_ITEM, HANGING_SIGN_ITEM, BOAT, CHEST_BOAT}) fuel.add(i, 300);
      fuel.add(SLAB, 150);
      fuel.add(BUTTON, 100);
      fuel.add(SAPLING, 100);
      CompostingChanceRegistry c = CompostingChanceRegistry.INSTANCE;
      for (Block b : new Block[]{RED_LEAVES, ORANGE_LEAVES, YELLOW_LEAVES, SAPLING}) c.add(b, 0.3F);
   }

   public static final class Grower extends AbstractTreeGrower {
      @Nullable
      protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean flowers) {
         String[] kinds = {"red_poplar", "orange_poplar", "yellow_poplar"};
         return ResourceKey.create(Registries.CONFIGURED_FEATURE, Backport.id(kinds[random.nextInt(3)]));
      }
   }
}
