package com.backport;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

/** Sulfur and cinnabar stone families from the 26.x "Chaos Cubed" drop. */
public final class NewStone {
   public static final SoundType SULFUR_SOUNDS = new SoundType(1.0F, 1.0F, BackportSounds.BLOCK_SULFUR_BREAK, BackportSounds.BLOCK_SULFUR_STEP, BackportSounds.BLOCK_SULFUR_PLACE, BackportSounds.BLOCK_SULFUR_HIT, BackportSounds.BLOCK_SULFUR_FALL);
   public static final SoundType CINNABAR_SOUNDS = new SoundType(1.0F, 1.0F, BackportSounds.BLOCK_CINNABAR_BREAK, BackportSounds.BLOCK_CINNABAR_STEP, BackportSounds.BLOCK_CINNABAR_PLACE, BackportSounds.BLOCK_CINNABAR_HIT, BackportSounds.BLOCK_CINNABAR_FALL);

   public static final Block SULFUR = stone("sulfur", MapColor.COLOR_YELLOW, SULFUR_SOUNDS);
   public static final Block CINNABAR = stone("cinnabar", MapColor.COLOR_RED, CINNABAR_SOUNDS);

   public static final Block SULFUR_SPIKE = Backport.block("sulfur_spike", new com.backport.sulfur.SulfurSpikeBlock(SULFUR, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).forceSolidOn()
      .instrument(NoteBlockInstrument.BASEDRUM).noOcclusion().sound(SULFUR_SOUNDS).randomTicks().strength(1.5F, 3.0F).dynamicShape()
      .offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY).isRedstoneConductor((s, l, p) -> false)));
   public static final Block POTENT_SULFUR = Backport.block("potent_sulfur", new com.backport.sulfur.PotentSulfurBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GOLD)
      .instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SULFUR_SOUNDS)));

   private NewStone() {
   }

   public static void init() {
      com.backport.sulfur.PotentSulfurBlockEntity.register(POTENT_SULFUR);
   }

   private static BlockBehaviour.Properties props(MapColor color, SoundType sound) {
      return BlockBehaviour.Properties.of().mapColor(color).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(sound);
   }

   private static Block stone(String name, MapColor color, SoundType sound) {
      Block base = Backport.block(name, new Block(props(color, sound)));
      Backport.block(name + "_slab", new SlabBlock(props(color, sound)));
      Backport.block(name + "_stairs", new StairBlock(base.defaultBlockState(), props(color, sound)));
      Backport.block(name + "_wall", new WallBlock(props(color, sound)));
      Block polished = Backport.block("polished_" + name, new Block(props(color, sound)));
      Backport.block("polished_" + name + "_slab", new SlabBlock(props(color, sound)));
      Backport.block("polished_" + name + "_stairs", new StairBlock(polished.defaultBlockState(), props(color, sound)));
      Backport.block("polished_" + name + "_wall", new WallBlock(props(color, sound)));
      Block bricks = Backport.block(name + "_bricks", new Block(props(color, sound)));
      Backport.block(name + "_brick_slab", new SlabBlock(props(color, sound)));
      Backport.block(name + "_brick_stairs", new StairBlock(bricks.defaultBlockState(), props(color, sound)));
      Backport.block(name + "_brick_wall", new WallBlock(props(color, sound)));
      Backport.block("chiseled_" + name, new Block(props(color, sound)));
      return base;
   }
}
