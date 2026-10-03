package com.palegarden;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public final class PaleItems {
   public static final List<Item> TAB_ITEMS = new ArrayList<>();

   public static final Item PALE_OAK_PLANKS = block("pale_oak_planks", PaleBlocks.PALE_OAK_PLANKS);
   public static final Item PALE_OAK_LOG = block("pale_oak_log", PaleBlocks.PALE_OAK_LOG);
   public static final Item PALE_OAK_WOOD = block("pale_oak_wood", PaleBlocks.PALE_OAK_WOOD);
   public static final Item STRIPPED_PALE_OAK_LOG = block("stripped_pale_oak_log", PaleBlocks.STRIPPED_PALE_OAK_LOG);
   public static final Item STRIPPED_PALE_OAK_WOOD = block("stripped_pale_oak_wood", PaleBlocks.STRIPPED_PALE_OAK_WOOD);
   public static final Item PALE_OAK_LEAVES = block("pale_oak_leaves", PaleBlocks.PALE_OAK_LEAVES);
   public static final Item PALE_OAK_SAPLING = block("pale_oak_sapling", PaleBlocks.PALE_OAK_SAPLING);
   public static final Item PALE_OAK_STAIRS = block("pale_oak_stairs", PaleBlocks.PALE_OAK_STAIRS);
   public static final Item PALE_OAK_SLAB = block("pale_oak_slab", PaleBlocks.PALE_OAK_SLAB);
   public static final Item PALE_OAK_FENCE = block("pale_oak_fence", PaleBlocks.PALE_OAK_FENCE);
   public static final Item PALE_OAK_FENCE_GATE = block("pale_oak_fence_gate", PaleBlocks.PALE_OAK_FENCE_GATE);
   public static final Item PALE_OAK_DOOR = register("pale_oak_door", new DoubleHighBlockItem(PaleBlocks.PALE_OAK_DOOR, new FabricItemSettings()));
   public static final Item PALE_OAK_TRAPDOOR = block("pale_oak_trapdoor", PaleBlocks.PALE_OAK_TRAPDOOR);
   public static final Item PALE_OAK_PRESSURE_PLATE = block("pale_oak_pressure_plate", PaleBlocks.PALE_OAK_PRESSURE_PLATE);
   public static final Item PALE_OAK_BUTTON = block("pale_oak_button", PaleBlocks.PALE_OAK_BUTTON);
   public static final Item PALE_OAK_SIGN = register("pale_oak_sign", new SignItem(new FabricItemSettings().maxCount(16), PaleBlocks.PALE_OAK_SIGN, PaleBlocks.PALE_OAK_WALL_SIGN));
   public static final Item PALE_OAK_HANGING_SIGN = register("pale_oak_hanging_sign", new HangingSignItem(PaleBlocks.PALE_OAK_HANGING_SIGN, PaleBlocks.PALE_OAK_WALL_HANGING_SIGN, new FabricItemSettings().maxCount(16)));
   public static final Item PALE_OAK_BOAT = register("pale_oak_boat", new PaleBoatItem(false, new FabricItemSettings().maxCount(1)));
   public static final Item PALE_OAK_CHEST_BOAT = register("pale_oak_chest_boat", new PaleBoatItem(true, new FabricItemSettings().maxCount(1)));
   public static final Item PALE_MOSS_BLOCK = block("pale_moss_block", PaleBlocks.PALE_MOSS_BLOCK);
   public static final Item PALE_MOSS_CARPET = block("pale_moss_carpet", PaleBlocks.PALE_MOSS_CARPET);
   public static final Item PALE_HANGING_MOSS = block("pale_hanging_moss", PaleBlocks.PALE_HANGING_MOSS);
   public static final Item OPEN_EYEBLOSSOM = block("open_eyeblossom", PaleBlocks.OPEN_EYEBLOSSOM);
   public static final Item CLOSED_EYEBLOSSOM = block("closed_eyeblossom", PaleBlocks.CLOSED_EYEBLOSSOM);
   public static final Item CREAKING_HEART = block("creaking_heart", PaleBlocks.CREAKING_HEART);
   public static final Item RESIN_CLUMP = block("resin_clump", PaleBlocks.RESIN_CLUMP);
   public static final Item RESIN_BRICK = register("resin_brick", new Item(new FabricItemSettings()));
   public static final Item RESIN_BLOCK = block("resin_block", PaleBlocks.RESIN_BLOCK);
   public static final Item RESIN_BRICKS = block("resin_bricks", PaleBlocks.RESIN_BRICKS);
   public static final Item RESIN_BRICK_STAIRS = block("resin_brick_stairs", PaleBlocks.RESIN_BRICK_STAIRS);
   public static final Item RESIN_BRICK_SLAB = block("resin_brick_slab", PaleBlocks.RESIN_BRICK_SLAB);
   public static final Item RESIN_BRICK_WALL = block("resin_brick_wall", PaleBlocks.RESIN_BRICK_WALL);
   public static final Item CHISELED_RESIN_BRICKS = block("chiseled_resin_bricks", PaleBlocks.CHISELED_RESIN_BRICKS);
   public static final Item CREAKING_SPAWN_EGG = register("creaking_spawn_egg", new SpawnEggItem(PaleEntities.CREAKING, 0x5F5F5F, 0xFC7812, new FabricItemSettings()));

   private PaleItems() {
   }

   private static Item block(String name, Block block) {
      return register(name, new BlockItem(block, new FabricItemSettings()));
   }

   private static Item register(String name, Item item) {
      Registry.register(BuiltInRegistries.ITEM, PaleGarden.id(name), item);
      TAB_ITEMS.add(item);
      return item;
   }

   public static void init() {
      StrippableBlockRegistry.register(PaleBlocks.PALE_OAK_LOG, PaleBlocks.STRIPPED_PALE_OAK_LOG);
      StrippableBlockRegistry.register(PaleBlocks.PALE_OAK_WOOD, PaleBlocks.STRIPPED_PALE_OAK_WOOD);

      FlammableBlockRegistry flammable = FlammableBlockRegistry.getDefaultInstance();
      for (Block log : new Block[]{PaleBlocks.PALE_OAK_LOG, PaleBlocks.PALE_OAK_WOOD, PaleBlocks.STRIPPED_PALE_OAK_LOG, PaleBlocks.STRIPPED_PALE_OAK_WOOD}) {
         flammable.add(log, 5, 5);
      }
      for (Block plank : new Block[]{PaleBlocks.PALE_OAK_PLANKS, PaleBlocks.PALE_OAK_SLAB, PaleBlocks.PALE_OAK_FENCE_GATE, PaleBlocks.PALE_OAK_FENCE, PaleBlocks.PALE_OAK_STAIRS}) {
         flammable.add(plank, 5, 20);
      }
      flammable.add(PaleBlocks.PALE_OAK_LEAVES, 30, 60);
      flammable.add(PaleBlocks.PALE_MOSS_BLOCK, 5, 100);
      flammable.add(PaleBlocks.PALE_MOSS_CARPET, 5, 100);
      flammable.add(PaleBlocks.PALE_HANGING_MOSS, 5, 100);

      FuelRegistry fuel = FuelRegistry.INSTANCE;
      for (ItemLike wood : new ItemLike[]{PALE_OAK_PLANKS, PALE_OAK_LOG, PALE_OAK_WOOD, STRIPPED_PALE_OAK_LOG, STRIPPED_PALE_OAK_WOOD,
         PALE_OAK_STAIRS, PALE_OAK_FENCE, PALE_OAK_FENCE_GATE, PALE_OAK_DOOR, PALE_OAK_TRAPDOOR, PALE_OAK_PRESSURE_PLATE, PALE_OAK_SIGN, PALE_OAK_HANGING_SIGN, PALE_OAK_BOAT, PALE_OAK_CHEST_BOAT}) {
         fuel.add(wood, 300);
      }
      fuel.add(PALE_OAK_SLAB, 150);
      fuel.add(PALE_OAK_BUTTON, 100);
      fuel.add(PALE_OAK_SAPLING, 100);

      CompostingChanceRegistry compost = CompostingChanceRegistry.INSTANCE;
      compost.add(PALE_OAK_LEAVES, 0.3F);
      compost.add(PALE_OAK_SAPLING, 0.3F);
      compost.add(PALE_MOSS_BLOCK, 0.65F);
      compost.add(PALE_MOSS_CARPET, 0.3F);
      compost.add(PALE_HANGING_MOSS, 0.3F);
      compost.add(OPEN_EYEBLOSSOM, 0.65F);
      compost.add(CLOSED_EYEBLOSSOM, 0.65F);
   }
}
