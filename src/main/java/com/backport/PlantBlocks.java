package com.backport;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Small decorative plants, litter, mushrooms and beds from the 26.x drops. */
public final class PlantBlocks {
   public static final SoundType LEAF_LITTER_SOUNDS = new SoundType(1.0F, 1.0F, BackportSounds.BLOCK_LEAF_LITTER_BREAK, BackportSounds.BLOCK_LEAF_LITTER_STEP, BackportSounds.BLOCK_LEAF_LITTER_PLACE, BackportSounds.BLOCK_LEAF_LITTER_HIT, BackportSounds.BLOCK_LEAF_LITTER_FALL);
   public static final SoundType RED_SHRUB_SOUNDS = new SoundType(1.0F, 1.0F, BackportSounds.BLOCK_RED_SHRUB_BREAK, SoundType.GRASS.getStepSound(), BackportSounds.BLOCK_RED_SHRUB_PLACE, SoundType.GRASS.getHitSound(), SoundType.GRASS.getFallSound());
   public static final SoundType CACTUS_FLOWER_SOUNDS = new SoundType(1.0F, 1.0F, BackportSounds.BLOCK_CACTUS_FLOWER_BREAK, SoundType.GRASS.getStepSound(), BackportSounds.BLOCK_CACTUS_FLOWER_PLACE, SoundType.GRASS.getHitSound(), SoundType.GRASS.getFallSound());
   public static final SoundType SHELF_MUSHROOM_SOUNDS = new SoundType(1.0F, 1.0F, BackportSounds.BLOCK_SHELF_MUSHROOM_BREAK, BackportSounds.BLOCK_SHELF_MUSHROOM_STEP, BackportSounds.BLOCK_SHELF_MUSHROOM_PLACE, SoundType.WOOD.getHitSound(), BackportSounds.BLOCK_SHELF_MUSHROOM_FALL);
   public static final SoundType STRAW_BED_SOUNDS = new SoundType(1.0F, 1.0F, BackportSounds.BLOCK_STRAW_BED_BREAK, BackportSounds.BLOCK_STRAW_BED_STEP, BackportSounds.BLOCK_STRAW_BED_PLACE, BackportSounds.BLOCK_STRAW_BED_HIT, BackportSounds.BLOCK_STRAW_BED_FALL);
   public static final SoundType POPLAR_LEAVES_SOUNDS = new SoundType(1.0F, 1.0F, BackportSounds.BLOCK_POPLAR_LEAVES_BREAK, BackportSounds.BLOCK_POPLAR_LEAVES_STEP, BackportSounds.BLOCK_POPLAR_LEAVES_PLACE, BackportSounds.BLOCK_POPLAR_LEAVES_HIT, BackportSounds.BLOCK_POPLAR_LEAVES_FALL);

   public static final IntegerProperty SEGMENTS = IntegerProperty.create("segment_amount", 1, 4);

   public static final Block BUSH = Backport.block("bush", new SmallBush(
      BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).replaceable().noCollission().instabreak().sound(SoundType.GRASS).ignitedByLava().offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY), 13));
   public static final Block RED_SHRUB = Backport.block("red_shrub", new SmallBush(
      BlockBehaviour.Properties.of().mapColor(MapColor.CRIMSON_NYLIUM).replaceable().noCollission().instabreak().sound(RED_SHRUB_SOUNDS).ignitedByLava().pushReaction(PushReaction.DESTROY), 13));
   public static final Block CACTUS_FLOWER = Backport.block("cactus_flower", new CactusFlower(
      BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).noCollission().instabreak().ignitedByLava().sound(CACTUS_FLOWER_SOUNDS).pushReaction(PushReaction.DESTROY)));
   public static final Block FIREFLY_BUSH = Backport.block("firefly_bush", new FireflyBush(
      BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).ignitedByLava().lightLevel(s -> 2).noCollission().instabreak().sound(SoundType.SWEET_BERRY_BUSH).pushReaction(PushReaction.DESTROY)));
   public static final Block GOLDEN_DANDELION = Backport.block("golden_dandelion", new FlowerBlock(MobEffects.SATURATION, 1,
      BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));
   public static final Block POTTED_GOLDEN_DANDELION = Backport.blockNoItem("potted_golden_dandelion", new FlowerPotBlock(GOLDEN_DANDELION,
      BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));
   public static final Block LEAF_LITTER = Backport.block("leaf_litter", new LeafLitter(
      BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).replaceable().noCollission().sound(LEAF_LITTER_SOUNDS).pushReaction(PushReaction.DESTROY)));
   public static final Block WILDFLOWERS = Backport.block("wildflowers", new PinkPetalsBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().sound(SoundType.PINK_PETALS).pushReaction(PushReaction.DESTROY)));
   public static final Block SHORT_DRY_GRASS = Backport.block("short_dry_grass", new DryGrass(false,
      BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).replaceable().noCollission().instabreak().sound(SoundType.GRASS).ignitedByLava().offsetType(BlockBehaviour.OffsetType.XYZ).pushReaction(PushReaction.DESTROY)));
   public static final Block TALL_DRY_GRASS = Backport.block("tall_dry_grass", new DryGrass(true,
      BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).replaceable().noCollission().instabreak().sound(SoundType.GRASS).ignitedByLava().offsetType(BlockBehaviour.OffsetType.XYZ).pushReaction(PushReaction.DESTROY)));
   public static final Block SHELF_MUSHROOM = Backport.block("shelf_mushroom", new ShelfMushroom(
      BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_YELLOW).sound(SHELF_MUSHROOM_SOUNDS).noOcclusion().strength(0.4F).pushReaction(PushReaction.DESTROY)));
   public static final Block STRAW_BED = Backport.block("straw_bed", new StrawBed(
      BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).sound(STRAW_BED_SOUNDS).strength(0.2F).noOcclusion().ignitedByLava().pushReaction(PushReaction.DESTROY)));

   private PlantBlocks() {
   }

   public static void init() {
      FlowerPotBlock pot = (FlowerPotBlock) Blocks.FLOWER_POT;
      net.fabricmc.fabric.api.registry.FlammableBlockRegistry fl = net.fabricmc.fabric.api.registry.FlammableBlockRegistry.getDefaultInstance();
      fl.add(BUSH, 60, 100);
      fl.add(LEAF_LITTER, 60, 100);
      fl.add(SHORT_DRY_GRASS, 60, 100);
      fl.add(TALL_DRY_GRASS, 60, 100);
      fl.add(FIREFLY_BUSH, 60, 100);
      fl.add(STRAW_BED, 60, 20);
      net.fabricmc.fabric.api.registry.CompostingChanceRegistry c = net.fabricmc.fabric.api.registry.CompostingChanceRegistry.INSTANCE;
      for (Block b : new Block[]{BUSH, RED_SHRUB, CACTUS_FLOWER, FIREFLY_BUSH, GOLDEN_DANDELION, LEAF_LITTER, WILDFLOWERS, SHORT_DRY_GRASS, TALL_DRY_GRASS}) c.add(b, 0.3F);
      c.add(SHELF_MUSHROOM, 0.65F);
      // wildflowers heal like pink petals on bonemeal, same behaviour from vanilla class
      net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, level, hand, hit) -> InteractionResult.PASS);
      net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents.ALLOW_SETTING_SPAWN.register((entity, pos) -> !entity.level().getBlockState(pos).is(STRAW_BED));
      net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents.STOP_SLEEPING.register((entity, pos) -> {
         Level level = entity.level();
         BlockState state = level.getBlockState(pos);
         if (state.is(STRAW_BED) && !level.isClientSide) {
            level.playSound(null, pos, BackportSounds.BLOCK_STRAW_BED_BREAK_LEAVE, SoundSource.BLOCKS, 1.0F, 1.0F);
            BlockPos other = pos.relative(BedBlock.getConnectedDirection(state));
            level.removeBlock(pos, false);
            if (level.getBlockState(other).is(STRAW_BED)) level.removeBlock(other, false);
         }
      });
   }

   private static Optional<BlockPos> spreadTarget(LevelReader level, BlockPos pos, BlockState state, RandomSource random) {
      java.util.List<BlockPos> candidates = new java.util.ArrayList<>();
      for (int dx = -3; dx <= 3; dx++) {
         for (int dz = -3; dz <= 3; dz++) {
            for (int dy = -1; dy <= 1; dy++) {
               BlockPos p = pos.offset(dx, dy, dz);
               if ((dx != 0 || dz != 0) && level.getBlockState(p).isAir() && state.canSurvive(level, p)) candidates.add(p);
            }
         }
      }
      return candidates.isEmpty() ? Optional.empty() : Optional.of(candidates.get(random.nextInt(candidates.size())));
   }

   public static class SmallBush extends BushBlock implements BonemealableBlock {
      private final VoxelShape shape;

      SmallBush(BlockBehaviour.Properties props, int height) {
         super(props);
         this.shape = Block.box(0.0, 0.0, 0.0, 16.0, height, 16.0);
      }

      public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
         return this.shape;
      }

      public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean client) {
         return spreadTarget(level, pos, state, RandomSource.create()).isPresent();
      }

      public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
         return true;
      }

      public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
         spreadTarget(level, pos, state, random).ifPresent(p -> level.setBlockAndUpdate(p, this.defaultBlockState()));
      }
   }

   public static class CactusFlower extends BushBlock {
      private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 12.0, 15.0);

      CactusFlower(BlockBehaviour.Properties props) {
         super(props);
      }

      public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
         return SHAPE;
      }

      protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
         return state.is(Blocks.CACTUS) || state.isFaceSturdy(level, pos, Direction.UP, net.minecraft.world.level.block.SupportType.CENTER);
      }
   }

   public static class FireflyBush extends BushBlock implements BonemealableBlock {
      FireflyBush(BlockBehaviour.Properties props) {
         super(props);
      }

      public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
         if (random.nextInt(30) == 0 && level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ()) <= pos.getY()
            && level.getBrightness(net.minecraft.world.level.LightLayer.SKY, pos) < 12) {
            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), BackportSounds.BLOCK_FIREFLY_BUSH_IDLE, SoundSource.AMBIENT, 1.0F, 1.0F, false);
         }
         if (level.getMaxLocalRawBrightness(pos) <= 13 && random.nextDouble() <= 0.7) {
            double x = pos.getX() + random.nextDouble() * 10.0 - 5.0;
            double y = pos.getY() + random.nextDouble() * 5.0;
            double z = pos.getZ() + random.nextDouble() * 10.0 - 5.0;
            level.addParticle(BackportParticles.FIREFLY, x, y, z, 0.0, 0.0, 0.0);
         }
      }

      public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean client) {
         return spreadTarget(level, pos, state, RandomSource.create()).isPresent();
      }

      public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
         return true;
      }

      public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
         spreadTarget(level, pos, state, random).ifPresent(p -> level.setBlockAndUpdate(p, this.defaultBlockState()));
      }
   }

   public static class LeafLitter extends BushBlock {
      public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

      LeafLitter(BlockBehaviour.Properties props) {
         super(props);
         this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(SEGMENTS, 1));
      }

      public BlockState rotate(BlockState state, Rotation rotation) {
         return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
      }

      public BlockState mirror(BlockState state, Mirror mirror) {
         return state.rotate(mirror.getRotation(state.getValue(FACING)));
      }

      public boolean canBeReplaced(BlockState state, BlockPlaceContext ctx) {
         return !ctx.isSecondaryUseActive() && ctx.getItemInHand().is(this.asItem()) && state.getValue(SEGMENTS) < 4 ? true : super.canBeReplaced(state, ctx);
      }

      public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
         BlockPos below = pos.below();
         return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
      }

      protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
         return true;
      }

      public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
         return Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
      }

      public BlockState getStateForPlacement(BlockPlaceContext ctx) {
         BlockState existing = ctx.getLevel().getBlockState(ctx.getClickedPos());
         return existing.is(this) ? existing.setValue(SEGMENTS, Math.min(4, existing.getValue(SEGMENTS) + 1)) : this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
      }

      protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
         builder.add(FACING, SEGMENTS);
      }
   }

   public static class DryGrass extends BushBlock implements BonemealableBlock {
      private final boolean tall;

      DryGrass(boolean tall, BlockBehaviour.Properties props) {
         super(props);
         this.tall = tall;
      }

      public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
         return this.tall ? Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0) : Block.box(2.0, 0.0, 2.0, 14.0, 10.0, 14.0);
      }

      protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
         return state.is(BlockTags.SAND) || state.is(BlockTags.TERRACOTTA) || state.is(BlockTags.DIRT);
      }

      public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
         if (random.nextInt(150) == 0 && level.getBlockState(pos.below()).is(BlockTags.SAND)) {
            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), BackportSounds.BLOCK_DRY_GRASS_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F, false);
         }
      }

      public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean client) {
         return !this.tall || spreadTarget(level, pos, SHORT_DRY_GRASS.defaultBlockState(), RandomSource.create()).isPresent();
      }

      public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
         return true;
      }

      public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
         if (!this.tall) {
            level.setBlockAndUpdate(pos, TALL_DRY_GRASS.defaultBlockState());
         } else {
            BlockState s = SHORT_DRY_GRASS.defaultBlockState();
            spreadTarget(level, pos, s, random).ifPresent(p -> level.setBlockAndUpdate(p, s));
         }
      }
   }

   public static class ShelfMushroom extends HorizontalDirectionalBlock implements BonemealableBlock {
      public static final IntegerProperty AGE = BlockStateProperties.AGE_1;

      ShelfMushroom(BlockBehaviour.Properties props) {
         super(props);
         this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(AGE, 0));
      }

      public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
         Direction facing = state.getValue(FACING);
         BlockPos support = pos.relative(facing.getOpposite());
         return level.getBlockState(support).isFaceSturdy(level, support, facing);
      }

      public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
         int age = state.getValue(AGE);
         double d = 1.0 + age;
         VoxelShape base = Block.box(8.0 - (10 + age * 4) / 2.0, 8.0 - age * 2, 0.0, 8.0 + (10 + age * 4) / 2.0, 8.0 - age * 2 + 7 + age * 3, 3 + age * 2);
         return rotate(base, state.getValue(FACING));
      }

      private static VoxelShape rotate(VoxelShape north, Direction facing) {
         // shapes are authored on the south wall (z from 0), then turned to face the requested direction
         return switch (facing) {
            case SOUTH -> north;
            case NORTH -> Shapes.create(1.0 - north.max(net.minecraft.core.Direction.Axis.X), north.min(net.minecraft.core.Direction.Axis.Y), 1.0 - north.max(net.minecraft.core.Direction.Axis.Z), 1.0 - north.min(net.minecraft.core.Direction.Axis.X), north.max(net.minecraft.core.Direction.Axis.Y), 1.0 - north.min(net.minecraft.core.Direction.Axis.Z));
            case EAST -> Shapes.create(north.min(net.minecraft.core.Direction.Axis.Z), north.min(net.minecraft.core.Direction.Axis.Y), north.min(net.minecraft.core.Direction.Axis.X), north.max(net.minecraft.core.Direction.Axis.Z), north.max(net.minecraft.core.Direction.Axis.Y), north.max(net.minecraft.core.Direction.Axis.X));
            default -> Shapes.create(1.0 - north.max(net.minecraft.core.Direction.Axis.Z), north.min(net.minecraft.core.Direction.Axis.Y), north.min(net.minecraft.core.Direction.Axis.X), 1.0 - north.min(net.minecraft.core.Direction.Axis.Z), north.max(net.minecraft.core.Direction.Axis.Y), north.max(net.minecraft.core.Direction.Axis.X));
         };
      }

      @Nullable
      public BlockState getStateForPlacement(BlockPlaceContext ctx) {
         BlockState state = this.defaultBlockState();
         LevelReader level = ctx.getLevel();
         BlockPos pos = ctx.getClickedPos();
         for (Direction dir : ctx.getNearestLookingDirections()) {
            if (dir.getAxis().isHorizontal()) {
               state = state.setValue(FACING, dir.getOpposite());
               if (state.canSurvive(level, pos)) return state;
            }
         }
         return null;
      }

      public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fall) {
         if (entity.isSuppressingBounce()) {
            super.fallOn(level, state, pos, entity, fall);
         } else {
            entity.causeFallDamage(fall, 0.0F, level.damageSources().fall());
         }
      }

      public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
         if (entity.isSuppressingBounce()) {
            super.updateEntityAfterFallOn(level, entity);
         } else {
            net.minecraft.world.phys.Vec3 v = entity.getDeltaMovement();
            if (v.y < 0.0) {
               double d = entity instanceof net.minecraft.world.entity.LivingEntity ? 0.75 : 0.5;
               entity.setDeltaMovement(v.x, -v.y * d, v.z);
               if (!entity.level().isClientSide && !(entity instanceof ItemEntity) && !(entity instanceof PrimedTnt)) {
                  entity.level().playSound(null, entity.blockPosition(), BackportSounds.BLOCK_SHELF_MUSHROOM_BOUNCE, SoundSource.BLOCKS, 1.0F, 1.0F);
               }
            }
         }
      }

      public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
         return dir == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, dir, neighbor, level, pos, neighborPos);
      }

      public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean client) {
         return state.getValue(AGE) < 1;
      }

      public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
         return true;
      }

      public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
         level.setBlock(pos, state.setValue(AGE, state.getValue(AGE) + 1), 2);
      }

      protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
         builder.add(FACING, AGE);
      }

      public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
         return false;
      }
   }

   public static class StrawBed extends BedBlock {
      private static final VoxelShape BASE = Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0);
      private static final VoxelShape HEAD = Shapes.or(BASE, Block.box(0.0, 0.0, 0.0, 16.0, 5.0, 8.0));

      StrawBed(BlockBehaviour.Properties props) {
         super(DyeColor.YELLOW, props);
      }

      public RenderShape getRenderShape(BlockState state) {
         return RenderShape.MODEL;
      }

      @Nullable
      public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
         return null;
      }

      public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
         if (state.getValue(PART) != BedPart.HEAD) return BASE;
         Direction toward = getConnectedDirection(state).getOpposite();
         VoxelShape pillow = Block.box(0.0, 0.0, 0.0, 16.0, 5.0, 8.0);
         return switch (toward) {
            case SOUTH -> Shapes.or(BASE, Block.box(0.0, 0.0, 8.0, 16.0, 5.0, 16.0));
            case EAST -> Shapes.or(BASE, Block.box(8.0, 0.0, 0.0, 16.0, 5.0, 16.0));
            case WEST -> Shapes.or(BASE, Block.box(0.0, 0.0, 0.0, 8.0, 5.0, 16.0));
            default -> Shapes.or(BASE, pillow);
         };
      }

      public net.minecraft.world.level.block.entity.BlockEntityType<?> unused() {
         return null;
      }
   }
}
