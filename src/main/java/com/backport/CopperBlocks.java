package com.backport;

import java.util.function.BiFunction;
import net.fabricmc.fabric.api.object.builder.v1.block.type.BlockSetTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.type.BlockSetTypeRegistry;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.core.BlockPos;

/** The 1.21+ copper family: oxidising / waxable variants of chiseled copper, grates, bulbs, doors, trapdoors, chains, bars and lanterns. */
public final class CopperBlocks {
   public static final BlockSetType COPPER_SET = BlockSetTypeRegistry.registerWood(Backport.id("copper"));
   private static final WeatheringCopper.WeatherState[] STATES = WeatheringCopper.WeatherState.values();
   private static final String[] PREFIX = {"", "exposed_", "weathered_", "oxidized_"};
   private static final Block[] COPPER_BLOCKS = {Blocks.COPPER_BLOCK, Blocks.EXPOSED_COPPER, Blocks.WEATHERED_COPPER, Blocks.OXIDIZED_COPPER};

   public static final SimpleParticleType COPPER_FIRE_FLAME = Registry.register(BuiltInRegistries.PARTICLE_TYPE, Backport.id("copper_fire_flame"), net.fabricmc.fabric.api.particle.v1.FabricParticleTypes.simple());

   public static final Block COPPER_TORCH = Backport.blockNoItem("copper_torch", new TorchBlock(
      BlockBehaviour.Properties.of().noCollission().instabreak().lightLevel(s -> 14).sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY), COPPER_FIRE_FLAME));
   public static final Block COPPER_WALL_TORCH = Backport.blockNoItem("copper_wall_torch", new WallTorchBlock(
      BlockBehaviour.Properties.of().noCollission().instabreak().lightLevel(s -> 14).sound(SoundType.WOOD).dropsLike(COPPER_TORCH).pushReaction(PushReaction.DESTROY), COPPER_FIRE_FLAME));
   public static final Item COPPER_TORCH_ITEM = Backport.item("copper_torch", new StandingAndWallBlockItem(COPPER_TORCH, COPPER_WALL_TORCH, new FabricItemSettings(), Direction.DOWN));
   public static final Item COPPER_NUGGET = Backport.item("copper_nugget", new Item(new FabricItemSettings()));

   public static final Block[] CHISELED_COPPER = family("chiseled_copper", (s, p) -> new Block(p), WeatheringBlock::new, s -> full(s));
   public static final Block[] COPPER_GRATE = family("copper_grate", (s, p) -> new WaterloggedBlock(p), WeatheringGrateBlock::new,
      s -> BlockBehaviour.Properties.of().strength(3.0F, 6.0F).sound(SoundType.COPPER).mapColor(COPPER_BLOCKS[s.ordinal()].defaultMapColor()).noOcclusion()
         .requiresCorrectToolForDrops().isValidSpawn((a, b, c, d) -> false).isRedstoneConductor(CopperBlocks::never).isSuffocating(CopperBlocks::never).isViewBlocking(CopperBlocks::never));
   public static final Block[] COPPER_BULB = family("copper_bulb", (s, p) -> new CopperBulbBlock(p), WeatheringBulbBlock::new,
      s -> BlockBehaviour.Properties.of().mapColor(COPPER_BLOCKS[0].defaultMapColor()).strength(3.0F, 6.0F).sound(SoundType.COPPER).requiresCorrectToolForDrops()
         .isRedstoneConductor(CopperBlocks::never).lightLevel(bulbLight(new int[]{15, 12, 8, 4}[s.ordinal()])));
   public static final Block[] COPPER_DOOR = family("copper_door", (s, p) -> new DoorBlock(p, COPPER_SET), (s, p) -> new WeatheringDoorBlock(s, COPPER_SET, p),
      s -> BlockBehaviour.Properties.of().mapColor(COPPER_BLOCKS[s.ordinal()].defaultMapColor()).strength(3.0F, 6.0F).requiresCorrectToolForDrops().noOcclusion().pushReaction(PushReaction.DESTROY));
   public static final Block[] COPPER_TRAPDOOR = family("copper_trapdoor", (s, p) -> new TrapDoorBlock(p, COPPER_SET), (s, p) -> new WeatheringTrapDoorBlock(s, COPPER_SET, p),
      s -> BlockBehaviour.Properties.of().mapColor(COPPER_BLOCKS[s.ordinal()].defaultMapColor()).strength(3.0F, 6.0F).requiresCorrectToolForDrops().noOcclusion().isValidSpawn((a, b, c, d) -> false));
   public static final Block[] COPPER_CHAIN = family("copper_chain", (s, p) -> new ChainBlock(p), WeatheringChainBlock::new,
      s -> BlockBehaviour.Properties.of().forceSolidOn().requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.CHAIN).noOcclusion());
   public static final Block[] COPPER_BARS = family("copper_bars", (s, p) -> new IronBarsBlock(p), WeatheringBarsBlock::new,
      s -> BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.COPPER).noOcclusion());
   public static final Block[] COPPER_LANTERN = family("copper_lantern", (s, p) -> new LanternBlock(p), WeatheringLanternBlock::new,
      s -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).forceSolidOn().strength(3.5F).sound(SoundType.LANTERN).lightLevel(st -> 15).noOcclusion().pushReaction(PushReaction.DESTROY));

   public static final Block[] COPPER_CHEST = chestFamily();
   public static final Block[] COPPER_GOLEM_STATUE = statueFamily();
   public static final Block[] LIGHTNING_RODS = lightningRods();

   private CopperBlocks() {
   }

   private static java.util.function.ToIntFunction<BlockState> bulbLight(int level) {
      return state -> state.getValue(BlockStateProperties_LIT()) ? level : 0;
   }

   private static net.minecraft.world.level.block.state.properties.BooleanProperty BlockStateProperties_LIT() {
      return net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT;
   }

   private static boolean never(BlockState state, BlockGetter level, BlockPos pos) {
      return false;
   }

   private static BlockBehaviour.Properties full(WeatheringCopper.WeatherState state) {
      return BlockBehaviour.Properties.copy(COPPER_BLOCKS[state.ordinal()]);
   }

   /** Registers unwaxed (oxidising) and waxed variants of a block; returns the unwaxed blocks in age order. */
   private static Block[] family(
      String base,
      BiFunction<WeatheringCopper.WeatherState, BlockBehaviour.Properties, Block> waxedFactory,
      BiFunction<WeatheringCopper.WeatherState, BlockBehaviour.Properties, Block> weatheringFactory,
      java.util.function.Function<WeatheringCopper.WeatherState, BlockBehaviour.Properties> props
   ) {
      Block[] weathering = new Block[4];
      Block[] waxed = new Block[4];
      for (int i = 0; i < 4; i++) {
         weathering[i] = register(PREFIX[i] + base, weatheringFactory.apply(STATES[i], props.apply(STATES[i])), base);
         waxed[i] = register("waxed_" + PREFIX[i] + base, waxedFactory.apply(STATES[i], props.apply(STATES[i])), base);
         OxidizableBlocksRegistry.registerWaxableBlockPair(weathering[i], waxed[i]);
      }

      for (int i = 0; i < 3; i++) {
         OxidizableBlocksRegistry.registerOxidizableBlockPair(weathering[i], weathering[i + 1]);
      }

      return weathering;
   }

   private static Block register(String name, Block block, String base) {
      if (base.equals("copper_door")) {
         Backport.blockNoItem(name, block);
         Backport.item(name, new DoubleHighBlockItem(block, new FabricItemSettings()));
         return block;
      }

      return Backport.block(name, block);
   }

   private static Block[] chestFamily() {
      Block[] weathering = new Block[4];
      Block[] waxed = new Block[4];
      java.util.List<Block> all = new java.util.ArrayList<>();
      for (int i = 0; i < 4; i++) {
         BlockBehaviour.Properties props = BlockBehaviour.Properties.of().mapColor(COPPER_BLOCKS[i].defaultMapColor()).strength(3.0F, 6.0F).sound(SoundType.COPPER).requiresCorrectToolForDrops();
         weathering[i] = Backport.blockNoItem(PREFIX[i] + "copper_chest", new WeatheringChestBlock(STATES[i], props));
         waxed[i] = Backport.blockNoItem("waxed_" + PREFIX[i] + "copper_chest", new CopperChestBlock(STATES[i], props));
         all.add(weathering[i]);
         all.add(waxed[i]);
         OxidizableBlocksRegistry.registerWaxableBlockPair(weathering[i], waxed[i]);
      }

      for (int i = 0; i < 3; i++) {
         OxidizableBlocksRegistry.registerOxidizableBlockPair(weathering[i], weathering[i + 1]);
      }

      CopperChestBlockEntity.register(all.toArray(new Block[0]));
      for (Block block : all) {
         Backport.item(BuiltInRegistries.BLOCK.getKey(block).getPath(), new net.minecraft.world.item.BlockItem(block, new FabricItemSettings()));
      }

      return weathering;
   }

   private static Block[] statueFamily() {
      Block[] weathering = new Block[4];
      Block[] waxed = new Block[4];
      java.util.List<Block> all = new java.util.ArrayList<>();
      for (int i = 0; i < 4; i++) {
         BlockBehaviour.Properties props = BlockBehaviour.Properties.of().mapColor(COPPER_BLOCKS[i].defaultMapColor()).strength(3.0F, 6.0F).sound(SoundType.COPPER).noOcclusion().requiresCorrectToolForDrops();
         weathering[i] = Backport.blockNoItem(PREFIX[i] + "copper_golem_statue", new WeatheringCopperGolemStatueBlock(STATES[i], props));
         waxed[i] = Backport.blockNoItem("waxed_" + PREFIX[i] + "copper_golem_statue", new CopperGolemStatueBlock(STATES[i], props));
         all.add(weathering[i]);
         all.add(waxed[i]);
         OxidizableBlocksRegistry.registerWaxableBlockPair(weathering[i], waxed[i]);
      }

      for (int i = 0; i < 3; i++) {
         OxidizableBlocksRegistry.registerOxidizableBlockPair(weathering[i], weathering[i + 1]);
      }

      CopperGolemStatueBlockEntity.register(all.toArray(new Block[0]));
      for (Block block : all) {
         Backport.item(BuiltInRegistries.BLOCK.getKey(block).getPath(), new net.minecraft.world.item.BlockItem(block, new FabricItemSettings()));
      }

      return weathering;
   }

   private static Block[] lightningRods() {
      Block[] weathering = new Block[4];
      Block[] waxed = new Block[4];
      weathering[0] = Blocks.LIGHTNING_ROD;
      for (int i = 1; i < 4; i++) {
         weathering[i] = Backport.block(PREFIX[i] + "lightning_rod", new WeatheringLightningRodBlock(STATES[i], BlockBehaviour.Properties.copy(Blocks.LIGHTNING_ROD)));
      }

      for (int i = 0; i < 4; i++) {
         waxed[i] = Backport.block("waxed_" + PREFIX[i] + "lightning_rod", new LightningRodBlock(BlockBehaviour.Properties.copy(Blocks.LIGHTNING_ROD)));
         OxidizableBlocksRegistry.registerWaxableBlockPair(weathering[i], waxed[i]);
      }

      for (int i = 0; i < 3; i++) {
         OxidizableBlocksRegistry.registerOxidizableBlockPair(weathering[i], weathering[i + 1]);
      }

      return weathering;
   }

   public static void init() {
   }
}
