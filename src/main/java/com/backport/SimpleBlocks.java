package com.backport;

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Blocks backported from 1.21.x / 26.x (simple, data-driven ones). */
public final class SimpleBlocks {
   public static final Block WHITE_WOOL_STAIRS = block("white_wool_stairs", new StairBlock((Blocks.WHITE_WOOL).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL)));
   public static final Block WHITE_WOOL_SLAB = block("white_wool_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL)));
   public static final Block WHITE_CONCRETE_STAIRS = block("white_concrete_stairs", new StairBlock((Blocks.WHITE_CONCRETE).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE)));
   public static final Block WHITE_CONCRETE_SLAB = block("white_concrete_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE)));
   public static final Block ORANGE_WOOL_STAIRS = block("orange_wool_stairs", new StairBlock((Blocks.ORANGE_WOOL).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.ORANGE_WOOL)));
   public static final Block ORANGE_WOOL_SLAB = block("orange_wool_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.ORANGE_WOOL)));
   public static final Block ORANGE_CONCRETE_STAIRS = block("orange_concrete_stairs", new StairBlock((Blocks.ORANGE_CONCRETE).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.ORANGE_CONCRETE)));
   public static final Block ORANGE_CONCRETE_SLAB = block("orange_concrete_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.ORANGE_CONCRETE)));
   public static final Block MAGENTA_WOOL_STAIRS = block("magenta_wool_stairs", new StairBlock((Blocks.MAGENTA_WOOL).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.MAGENTA_WOOL)));
   public static final Block MAGENTA_WOOL_SLAB = block("magenta_wool_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.MAGENTA_WOOL)));
   public static final Block MAGENTA_CONCRETE_STAIRS = block("magenta_concrete_stairs", new StairBlock((Blocks.MAGENTA_CONCRETE).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.MAGENTA_CONCRETE)));
   public static final Block MAGENTA_CONCRETE_SLAB = block("magenta_concrete_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.MAGENTA_CONCRETE)));
   public static final Block LIGHT_BLUE_WOOL_STAIRS = block("light_blue_wool_stairs", new StairBlock((Blocks.LIGHT_BLUE_WOOL).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.LIGHT_BLUE_WOOL)));
   public static final Block LIGHT_BLUE_WOOL_SLAB = block("light_blue_wool_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.LIGHT_BLUE_WOOL)));
   public static final Block LIGHT_BLUE_CONCRETE_STAIRS = block("light_blue_concrete_stairs", new StairBlock((Blocks.LIGHT_BLUE_CONCRETE).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.LIGHT_BLUE_CONCRETE)));
   public static final Block LIGHT_BLUE_CONCRETE_SLAB = block("light_blue_concrete_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.LIGHT_BLUE_CONCRETE)));
   public static final Block YELLOW_WOOL_STAIRS = block("yellow_wool_stairs", new StairBlock((Blocks.YELLOW_WOOL).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.YELLOW_WOOL)));
   public static final Block YELLOW_WOOL_SLAB = block("yellow_wool_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.YELLOW_WOOL)));
   public static final Block YELLOW_CONCRETE_STAIRS = block("yellow_concrete_stairs", new StairBlock((Blocks.YELLOW_CONCRETE).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.YELLOW_CONCRETE)));
   public static final Block YELLOW_CONCRETE_SLAB = block("yellow_concrete_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.YELLOW_CONCRETE)));
   public static final Block LIME_WOOL_STAIRS = block("lime_wool_stairs", new StairBlock((Blocks.LIME_WOOL).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.LIME_WOOL)));
   public static final Block LIME_WOOL_SLAB = block("lime_wool_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.LIME_WOOL)));
   public static final Block LIME_CONCRETE_STAIRS = block("lime_concrete_stairs", new StairBlock((Blocks.LIME_CONCRETE).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.LIME_CONCRETE)));
   public static final Block LIME_CONCRETE_SLAB = block("lime_concrete_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.LIME_CONCRETE)));
   public static final Block PINK_WOOL_STAIRS = block("pink_wool_stairs", new StairBlock((Blocks.PINK_WOOL).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.PINK_WOOL)));
   public static final Block PINK_WOOL_SLAB = block("pink_wool_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.PINK_WOOL)));
   public static final Block PINK_CONCRETE_STAIRS = block("pink_concrete_stairs", new StairBlock((Blocks.PINK_CONCRETE).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.PINK_CONCRETE)));
   public static final Block PINK_CONCRETE_SLAB = block("pink_concrete_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.PINK_CONCRETE)));
   public static final Block GRAY_WOOL_STAIRS = block("gray_wool_stairs", new StairBlock((Blocks.GRAY_WOOL).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.GRAY_WOOL)));
   public static final Block GRAY_WOOL_SLAB = block("gray_wool_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.GRAY_WOOL)));
   public static final Block GRAY_CONCRETE_STAIRS = block("gray_concrete_stairs", new StairBlock((Blocks.GRAY_CONCRETE).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.GRAY_CONCRETE)));
   public static final Block GRAY_CONCRETE_SLAB = block("gray_concrete_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.GRAY_CONCRETE)));
   public static final Block LIGHT_GRAY_WOOL_STAIRS = block("light_gray_wool_stairs", new StairBlock((Blocks.LIGHT_GRAY_WOOL).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.LIGHT_GRAY_WOOL)));
   public static final Block LIGHT_GRAY_WOOL_SLAB = block("light_gray_wool_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.LIGHT_GRAY_WOOL)));
   public static final Block LIGHT_GRAY_CONCRETE_STAIRS = block("light_gray_concrete_stairs", new StairBlock((Blocks.LIGHT_GRAY_CONCRETE).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.LIGHT_GRAY_CONCRETE)));
   public static final Block LIGHT_GRAY_CONCRETE_SLAB = block("light_gray_concrete_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.LIGHT_GRAY_CONCRETE)));
   public static final Block CYAN_WOOL_STAIRS = block("cyan_wool_stairs", new StairBlock((Blocks.CYAN_WOOL).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.CYAN_WOOL)));
   public static final Block CYAN_WOOL_SLAB = block("cyan_wool_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.CYAN_WOOL)));
   public static final Block CYAN_CONCRETE_STAIRS = block("cyan_concrete_stairs", new StairBlock((Blocks.CYAN_CONCRETE).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.CYAN_CONCRETE)));
   public static final Block CYAN_CONCRETE_SLAB = block("cyan_concrete_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.CYAN_CONCRETE)));
   public static final Block PURPLE_WOOL_STAIRS = block("purple_wool_stairs", new StairBlock((Blocks.PURPLE_WOOL).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.PURPLE_WOOL)));
   public static final Block PURPLE_WOOL_SLAB = block("purple_wool_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.PURPLE_WOOL)));
   public static final Block PURPLE_CONCRETE_STAIRS = block("purple_concrete_stairs", new StairBlock((Blocks.PURPLE_CONCRETE).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.PURPLE_CONCRETE)));
   public static final Block PURPLE_CONCRETE_SLAB = block("purple_concrete_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.PURPLE_CONCRETE)));
   public static final Block BLUE_WOOL_STAIRS = block("blue_wool_stairs", new StairBlock((Blocks.BLUE_WOOL).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.BLUE_WOOL)));
   public static final Block BLUE_WOOL_SLAB = block("blue_wool_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.BLUE_WOOL)));
   public static final Block BLUE_CONCRETE_STAIRS = block("blue_concrete_stairs", new StairBlock((Blocks.BLUE_CONCRETE).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.BLUE_CONCRETE)));
   public static final Block BLUE_CONCRETE_SLAB = block("blue_concrete_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.BLUE_CONCRETE)));
   public static final Block BROWN_WOOL_STAIRS = block("brown_wool_stairs", new StairBlock((Blocks.BROWN_WOOL).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.BROWN_WOOL)));
   public static final Block BROWN_WOOL_SLAB = block("brown_wool_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.BROWN_WOOL)));
   public static final Block BROWN_CONCRETE_STAIRS = block("brown_concrete_stairs", new StairBlock((Blocks.BROWN_CONCRETE).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.BROWN_CONCRETE)));
   public static final Block BROWN_CONCRETE_SLAB = block("brown_concrete_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.BROWN_CONCRETE)));
   public static final Block GREEN_WOOL_STAIRS = block("green_wool_stairs", new StairBlock((Blocks.GREEN_WOOL).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.GREEN_WOOL)));
   public static final Block GREEN_WOOL_SLAB = block("green_wool_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.GREEN_WOOL)));
   public static final Block GREEN_CONCRETE_STAIRS = block("green_concrete_stairs", new StairBlock((Blocks.GREEN_CONCRETE).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.GREEN_CONCRETE)));
   public static final Block GREEN_CONCRETE_SLAB = block("green_concrete_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.GREEN_CONCRETE)));
   public static final Block RED_WOOL_STAIRS = block("red_wool_stairs", new StairBlock((Blocks.RED_WOOL).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.RED_WOOL)));
   public static final Block RED_WOOL_SLAB = block("red_wool_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.RED_WOOL)));
   public static final Block RED_CONCRETE_STAIRS = block("red_concrete_stairs", new StairBlock((Blocks.RED_CONCRETE).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.RED_CONCRETE)));
   public static final Block RED_CONCRETE_SLAB = block("red_concrete_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.RED_CONCRETE)));
   public static final Block BLACK_WOOL_STAIRS = block("black_wool_stairs", new StairBlock((Blocks.BLACK_WOOL).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.BLACK_WOOL)));
   public static final Block BLACK_WOOL_SLAB = block("black_wool_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.BLACK_WOOL)));
   public static final Block BLACK_CONCRETE_STAIRS = block("black_concrete_stairs", new StairBlock((Blocks.BLACK_CONCRETE).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.BLACK_CONCRETE)));
   public static final Block BLACK_CONCRETE_SLAB = block("black_concrete_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.BLACK_CONCRETE)));
   public static final Block POLISHED_TUFF = block("polished_tuff", new Block(BlockBehaviour.Properties.copy(Blocks.TUFF)));
   public static final Block TUFF_BRICKS = block("tuff_bricks", new Block(BlockBehaviour.Properties.copy(Blocks.TUFF)));
   public static final Block CHISELED_TUFF = block("chiseled_tuff", new Block(BlockBehaviour.Properties.copy(Blocks.TUFF)));
   public static final Block CHISELED_TUFF_BRICKS = block("chiseled_tuff_bricks", new Block(BlockBehaviour.Properties.copy(Blocks.TUFF)));
   public static final Block TUFF_STAIRS = block("tuff_stairs", new StairBlock((Blocks.TUFF).defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.TUFF)));
   public static final Block TUFF_SLAB = block("tuff_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.TUFF)));
   public static final Block TUFF_WALL = block("tuff_wall", new WallBlock(BlockBehaviour.Properties.copy(Blocks.TUFF)));
   public static final Block POLISHED_TUFF_STAIRS = block("polished_tuff_stairs", new StairBlock((POLISHED_TUFF).defaultBlockState(), BlockBehaviour.Properties.copy(POLISHED_TUFF)));
   public static final Block POLISHED_TUFF_SLAB = block("polished_tuff_slab", new SlabBlock(BlockBehaviour.Properties.copy(POLISHED_TUFF)));
   public static final Block POLISHED_TUFF_WALL = block("polished_tuff_wall", new WallBlock(BlockBehaviour.Properties.copy(POLISHED_TUFF)));
   public static final Block TUFF_BRICK_STAIRS = block("tuff_brick_stairs", new StairBlock((TUFF_BRICKS).defaultBlockState(), BlockBehaviour.Properties.copy(TUFF_BRICKS)));
   public static final Block TUFF_BRICK_SLAB = block("tuff_brick_slab", new SlabBlock(BlockBehaviour.Properties.copy(TUFF_BRICKS)));
   public static final Block TUFF_BRICK_WALL = block("tuff_brick_wall", new WallBlock(BlockBehaviour.Properties.copy(TUFF_BRICKS)));

   private SimpleBlocks() {
   }

   private static Block block(String name, Block block) {
      return Backport.block(name, block);
   }

   public static void init() {
   }
}
