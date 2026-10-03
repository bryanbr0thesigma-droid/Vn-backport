package com.palegarden;

import com.palegarden.block.BonemealableFeaturePlacerBlock;
import com.palegarden.block.CreakingHeartBlock;
import com.palegarden.block.EyeblossomBlock;
import com.palegarden.block.HangingMossBlock;
import com.palegarden.block.MossyCarpetBlock;
import com.palegarden.block.PaleOakTreeGrower;
import com.palegarden.block.ParticleLeavesBlock;
import com.palegarden.block.ResinClumpBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.core.BlockPos;

public final class PaleBlocks {
   public static final TagKey<Block> PALE_OAK_LOGS = TagKey.create(Registries.BLOCK, PaleGarden.id("pale_oak_logs"));

   public static final Block PALE_OAK_PLANKS = register("pale_oak_planks", new Block(
      BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava()));
   public static final Block PALE_OAK_WOOD = register("pale_oak_wood", new RotatedPillarBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()));
   public static final Block PALE_OAK_LOG = register("pale_oak_log", new RotatedPillarBlock(
      BlockBehaviour.Properties.of().mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == net.minecraft.core.Direction.Axis.Y ? MapColor.QUARTZ : MapColor.STONE)
         .instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()));
   public static final Block STRIPPED_PALE_OAK_LOG = register("stripped_pale_oak_log", new RotatedPillarBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()));
   public static final Block STRIPPED_PALE_OAK_WOOD = register("stripped_pale_oak_wood", new RotatedPillarBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava()));
   public static final Block PALE_OAK_LEAVES = register("pale_oak_leaves", new ParticleLeavesBlock(50, PaleParticles.PALE_OAK_LEAVES,
      BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_GREEN).strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion()
         .isValidSpawn(PaleBlocks::ocelotOrParrot).isSuffocating(PaleBlocks::never).isViewBlocking(PaleBlocks::never).ignitedByLava()
         .pushReaction(PushReaction.DESTROY).isRedstoneConductor(PaleBlocks::never)));
   public static final Block PALE_OAK_SAPLING = register("pale_oak_sapling", new SaplingBlock(new PaleOakTreeGrower(),
      BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).noCollission().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));
   public static final Block POTTED_PALE_OAK_SAPLING = register("potted_pale_oak_sapling", new FlowerPotBlock(PALE_OAK_SAPLING,
      BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));

   public static final Block PALE_OAK_STAIRS = register("pale_oak_stairs", new StairBlock(PALE_OAK_PLANKS.defaultBlockState(), BlockBehaviour.Properties.copy(PALE_OAK_PLANKS)));
   public static final Block PALE_OAK_SLAB = register("pale_oak_slab", new SlabBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava()));
   public static final Block PALE_OAK_FENCE = register("pale_oak_fence", new FenceBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).forceSolidOn().instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava()));
   public static final Block PALE_OAK_FENCE_GATE = register("pale_oak_fence_gate", new FenceGateBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).forceSolidOn().instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava(), PaleWood.PALE_OAK));
   public static final Block PALE_OAK_DOOR = register("pale_oak_door", new DoorBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava().pushReaction(PushReaction.DESTROY), PaleWood.PALE_OAK_SET));
   public static final Block PALE_OAK_TRAPDOOR = register("pale_oak_trapdoor", new TrapDoorBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn(PaleBlocks::never).ignitedByLava(), PaleWood.PALE_OAK_SET));
   public static final Block PALE_OAK_PRESSURE_PLATE = register("pale_oak_pressure_plate", new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING,
      BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(0.5F).ignitedByLava().pushReaction(PushReaction.DESTROY), PaleWood.PALE_OAK_SET));
   public static final Block PALE_OAK_BUTTON = register("pale_oak_button", new ButtonBlock(
      BlockBehaviour.Properties.of().noCollission().strength(0.5F).pushReaction(PushReaction.DESTROY), PaleWood.PALE_OAK_SET, 30, true));
   public static final Block PALE_OAK_SIGN = register("pale_oak_sign", new StandingSignBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F).ignitedByLava(), PaleWood.PALE_OAK));
   public static final Block PALE_OAK_WALL_SIGN = register("pale_oak_wall_sign", new WallSignBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F).dropsLike(PALE_OAK_SIGN).ignitedByLava(), PaleWood.PALE_OAK));
   public static final Block PALE_OAK_HANGING_SIGN = register("pale_oak_hanging_sign", new CeilingHangingSignBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F).ignitedByLava(), PaleWood.PALE_OAK));
   public static final Block PALE_OAK_WALL_HANGING_SIGN = register("pale_oak_wall_hanging_sign", new WallHangingSignBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F).dropsLike(PALE_OAK_HANGING_SIGN).ignitedByLava(), PaleWood.PALE_OAK));

   public static final Block PALE_MOSS_BLOCK = register("pale_moss_block", new BonemealableFeaturePlacerBlock(
      ResourceKey.create(Registries.CONFIGURED_FEATURE, PaleGarden.id("pale_moss_patch_bonemeal")),
      BlockBehaviour.Properties.of().ignitedByLava().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(0.1F).sound(SoundType.MOSS).pushReaction(PushReaction.DESTROY)));
   public static final Block PALE_MOSS_CARPET = register("pale_moss_carpet", new MossyCarpetBlock(
      BlockBehaviour.Properties.of().ignitedByLava().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(0.1F).sound(SoundType.MOSS_CARPET).pushReaction(PushReaction.DESTROY)));
   public static final Block PALE_HANGING_MOSS = register("pale_hanging_moss", new HangingMossBlock(
      BlockBehaviour.Properties.of().ignitedByLava().mapColor(MapColor.COLOR_LIGHT_GRAY).noCollission().sound(SoundType.MOSS_CARPET).pushReaction(PushReaction.DESTROY)));

   public static final Block CREAKING_HEART = register("creaking_heart", new CreakingHeartBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.BASEDRUM).strength(10.0F).sound(PaleSounds.CREAKING_HEART)));
   public static final Block OPEN_EYEBLOSSOM = register("open_eyeblossom", new EyeblossomBlock(EyeblossomBlock.Type.OPEN,
      BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).noCollission().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY).randomTicks()));
   public static final Block CLOSED_EYEBLOSSOM = register("closed_eyeblossom", new EyeblossomBlock(EyeblossomBlock.Type.CLOSED,
      BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_GREEN).noCollission().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY).randomTicks()));
   public static final Block POTTED_OPEN_EYEBLOSSOM = register("potted_open_eyeblossom", new FlowerPotBlock(OPEN_EYEBLOSSOM,
      BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY).randomTicks()));
   public static final Block POTTED_CLOSED_EYEBLOSSOM = register("potted_closed_eyeblossom", new FlowerPotBlock(CLOSED_EYEBLOSSOM,
      BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY).randomTicks()));

   public static final Block RESIN_CLUMP = register("resin_clump", new ResinClumpBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).replaceable().noCollission().sound(PaleSounds.RESIN).ignitedByLava().pushReaction(PushReaction.DESTROY)));
   public static final Block RESIN_BLOCK = register("resin_block", new Block(
      BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).instrument(NoteBlockInstrument.BASEDRUM).sound(PaleSounds.RESIN)));
   public static final Block RESIN_BRICKS = register("resin_bricks", new Block(resinBrickProps()));
   public static final Block RESIN_BRICK_STAIRS = register("resin_brick_stairs", new StairBlock(RESIN_BRICKS.defaultBlockState(), resinBrickProps()));
   public static final Block RESIN_BRICK_SLAB = register("resin_brick_slab", new SlabBlock(resinBrickProps()));
   public static final Block RESIN_BRICK_WALL = register("resin_brick_wall", new WallBlock(resinBrickProps()));
   public static final Block CHISELED_RESIN_BRICKS = register("chiseled_resin_bricks", new Block(resinBrickProps()));

   private PaleBlocks() {
   }

   private static BlockBehaviour.Properties resinBrickProps() {
      return BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).instrument(NoteBlockInstrument.BASEDRUM)
         .requiresCorrectToolForDrops().sound(PaleSounds.RESIN_BRICKS).strength(1.5F, 6.0F);
   }

   private static boolean never(BlockState state, BlockGetter level, BlockPos pos) {
      return false;
   }

   private static boolean never(BlockState state, BlockGetter level, BlockPos pos, EntityType<?> type) {
      return false;
   }

   private static boolean ocelotOrParrot(BlockState state, BlockGetter level, BlockPos pos, EntityType<?> type) {
      return type == EntityType.OCELOT || type == EntityType.PARROT;
   }

   private static <T extends Block> T register(String name, T block) {
      return Registry.register(BuiltInRegistries.BLOCK, PaleGarden.id(name), block);
   }

   public static void init() {
   }
}
