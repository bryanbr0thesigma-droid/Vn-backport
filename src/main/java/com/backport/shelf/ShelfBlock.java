package com.backport.shelf;

import com.backport.BackportSounds;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ShelfBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final EnumProperty<SideChainPart> SIDE_CHAIN = EnumProperty.create("side_chain", SideChainPart.class);
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private static final Map<Direction, VoxelShape> SHAPES = rotate(Shapes.or(Block.box(0.0, 12.0, 11.0, 16.0, 16.0, 13.0), Block.box(0.0, 0.0, 13.0, 16.0, 16.0, 16.0), Block.box(0.0, 0.0, 11.0, 16.0, 4.0, 13.0)));

   private static Map<Direction, VoxelShape> rotate(VoxelShape southShape) {
      // authored on the south wall; builds the four horizontal rotations
      java.util.EnumMap<Direction, VoxelShape> map = new java.util.EnumMap<>(Direction.class);
      map.put(Direction.SOUTH, southShape);
      map.put(Direction.NORTH, rotateY(southShape, 2));
      map.put(Direction.WEST, rotateY(southShape, 1));
      map.put(Direction.EAST, rotateY(southShape, 3));
      return map;
   }

   private static VoxelShape rotateY(VoxelShape shape, int times) {
      VoxelShape[] buffer = {shape, Shapes.empty()};
      for (int i = 0; i < times; i++) {
         buffer[0].forAllBoxes((x1, y1, z1, x2, y2, z2) -> buffer[1] = Shapes.or(buffer[1], Shapes.box(1.0 - z2, y1, x1, 1.0 - z1, y2, x2)));
         buffer[0] = buffer[1];
         buffer[1] = Shapes.empty();
      }
      return buffer[0];
   }

   public ShelfBlock(BlockBehaviour.Properties props) {
      super(props);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false).setValue(SIDE_CHAIN, SideChainPart.UNCONNECTED).setValue(WATERLOGGED, false));
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return SHAPES.get(state.getValue(FACING).getOpposite());
   }

   public boolean useShapeForLightOcclusion(BlockState state) {
      return true;
   }

   public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
      return type == PathComputationType.WATER && state.getFluidState().is(net.minecraft.tags.FluidTags.WATER);
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new ShelfBlockEntity(pos, state);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(FACING, POWERED, SIDE_CHAIN, WATERLOGGED);
   }

   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.is(newState.getBlock())) {
         if (level.getBlockEntity(pos) instanceof ShelfBlockEntity be) {
            Containers.dropContents(level, pos, be);
            level.updateNeighbourForOutputSignal(pos, this);
         }
         super.onRemove(state, level, pos, newState, moved);
         if (!level.isClientSide) this.updateNeighborsAfterPoweringDown(level, pos, state);
      }
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos from, boolean moved) {
      if (!level.isClientSide) {
         boolean signal = level.hasNeighborSignal(pos);
         if (state.getValue(POWERED) != signal) {
            BlockState ns = state.setValue(POWERED, signal);
            if (!signal) ns = ns.setValue(SIDE_CHAIN, SideChainPart.UNCONNECTED);
            level.setBlockAndUpdate(pos, ns);
            level.playSound(null, pos, signal ? BackportSounds.BLOCK_SHELF_ACTIVATE : BackportSounds.BLOCK_SHELF_DEACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, signal ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos);
         }
      }
   }

   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      FluidState fluid = ctx.getLevel().getFluidState(ctx.getClickedPos());
      return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite()).setValue(POWERED, ctx.getLevel().hasNeighborSignal(ctx.getClickedPos())).setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
   }

   public BlockState rotate(BlockState state, Rotation rotation) {
      return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
   }

   public BlockState mirror(BlockState state, Mirror mirror) {
      return state.rotate(mirror.getRotation(state.getValue(FACING)));
   }

   public FluidState getFluidState(BlockState state) {
      return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
   }

   public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      if (state.getValue(WATERLOGGED)) level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
      return super.updateShape(state, dir, neighbor, level, pos, neighborPos);
   }

   public boolean hasAnalogOutputSignal(BlockState state) {
      return true;
   }

   public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
      if (level.isClientSide) return 0;
      if (level.getBlockEntity(pos) instanceof ShelfBlockEntity be) {
         return (be.getItem(0).isEmpty() ? 0 : 1) | (be.getItem(1).isEmpty() ? 0 : 1) << 1 | (be.getItem(2).isEmpty() ? 0 : 1) << 2;
      }
      return 0;
   }

   private OptionalInt getHitSlot(BlockHitResult hit, Direction facing) {
      if (hit.getDirection() != facing) return OptionalInt.empty();
      Vec3 rel = hit.getLocation().subtract(hit.getBlockPos().getX(), hit.getBlockPos().getY(), hit.getBlockPos().getZ());
      double x = switch (facing) {
         case NORTH -> 1.0 - rel.x;
         case SOUTH -> rel.x;
         case WEST -> rel.z;
         case EAST -> 1.0 - rel.z;
         default -> -1;
      };
      if (x < 0) return OptionalInt.empty();
      return OptionalInt.of(Mth.clamp(Mth.floor(x * 16.0 / (16.0 / 3.0)), 0, 2));
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (!(level.getBlockEntity(pos) instanceof ShelfBlockEntity shelf) || hand == InteractionHand.OFF_HAND) return InteractionResult.PASS;
      OptionalInt slot = this.getHitSlot(hit, state.getValue(FACING));
      if (slot.isEmpty()) return InteractionResult.PASS;
      ItemStack held = player.getItemInHand(hand);
      Inventory inv = player.getInventory();
      if (level.isClientSide) return held.isEmpty() ? InteractionResult.PASS : InteractionResult.CONSUME;
      if (!state.getValue(POWERED)) {
         ItemStack removed = shelf.swapItemNoUpdate(slot.getAsInt(), held);
         if (!removed.isEmpty() || !held.isEmpty()) {
            ItemStack newHeld = removed;
            if (player.getAbilities().instabuild && removed.isEmpty()) newHeld = held.copy();
            inv.setItem(inv.selected, newHeld);
            inv.setChanged();
            shelf.setChanged();
            level.gameEvent(player, GameEvent.ENTITY_INTERACT, pos);
            level.playSound(null, pos, removed.isEmpty() ? BackportSounds.BLOCK_SHELF_PLACE_ITEM : (held.isEmpty() ? BackportSounds.BLOCK_SHELF_TAKE_ITEM : BackportSounds.BLOCK_SHELF_SINGLE_SWAP), SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
         }
         return InteractionResult.PASS;
      }
      boolean any = this.swapHotbar(level, pos, inv);
      if (!any) return InteractionResult.CONSUME;
      level.playSound(null, pos, BackportSounds.BLOCK_SHELF_MULTI_SWAP, SoundSource.BLOCKS, 1.0F, 1.0F);
      return InteractionResult.SUCCESS;
   }

   private boolean swapHotbar(Level level, BlockPos pos, Inventory inv) {
      List<BlockPos> connected = this.allConnected(level, pos);
      if (connected.isEmpty()) return false;
      boolean any = false;
      for (int i = 0; i < connected.size(); i++) {
         if (!(level.getBlockEntity(connected.get(i)) instanceof ShelfBlockEntity part)) continue;
         for (int slot = 0; slot < part.getContainerSize(); slot++) {
            int invSlot = 9 - (connected.size() - i) * part.getContainerSize() + slot;
            if (invSlot >= 0 && invSlot < 9) {
               ItemStack placed = inv.removeItemNoUpdate(invSlot);
               ItemStack removed = part.swapItemNoUpdate(slot, placed);
               if (!placed.isEmpty() || !removed.isEmpty()) {
                  inv.setItem(invSlot, removed);
                  any = true;
               }
            }
         }
         inv.setChanged();
         part.setChanged();
      }
      return any;
   }

   // ---- side chain ----
   private boolean connectable(BlockState s) {
      return s.getBlock() instanceof ShelfBlock && s.getValue(POWERED);
   }

   private BlockState neighborAt(LevelAccessor level, BlockPos pos, Direction facing) {
      BlockState s = level.getBlockState(pos);
      return connectable(s) && s.getValue(FACING) == facing ? s : null;
   }

   private List<BlockPos> allConnected(LevelAccessor level, BlockPos pos) {
      BlockState state = level.getBlockState(pos);
      if (!connectable(state)) return List.of();
      Direction facing = state.getValue(FACING);
      LinkedList<BlockPos> results = new LinkedList<>();
      results.add(pos);
      for (int dirIdx = 0; dirIdx < 2; dirIdx++) {
         Direction d = dirIdx == 0 ? facing.getClockWise() : facing.getCounterClockWise();
         SideChainPart end = dirIdx == 0 ? SideChainPart.LEFT : SideChainPart.RIGHT;
         for (int steps = 1; steps < 3; steps++) {
            BlockPos p = pos.relative(d, steps);
            BlockState n = neighborAt(level, p, facing);
            if (n == null) break;
            SideChainPart part = n.getValue(SIDE_CHAIN);
            if (part.isConnectionTowards(end)) {
               if (dirIdx == 0) results.addFirst(p);
               else results.addLast(p);
            }
            if (part.isChainEnd()) break;
         }
      }
      return results;
   }

   private void setPart(LevelAccessor level, BlockPos pos, SideChainPart part) {
      BlockState s = level.getBlockState(pos);
      if (s.getBlock() instanceof ShelfBlock && s.getValue(SIDE_CHAIN) != part) level.setBlock(pos, s.setValue(SIDE_CHAIN, part), 3);
   }

   private void updateNeighborsAfterPoweringDown(LevelAccessor level, BlockPos pos, BlockState state) {
      Direction facing = state.getValue(FACING);
      BlockPos left = pos.relative(facing.getClockWise());
      BlockPos right = pos.relative(facing.getCounterClockWise());
      BlockState l = neighborAt(level, left, facing);
      BlockState r = neighborAt(level, right, facing);
      if (l != null) this.setPart(level, left, l.getValue(SIDE_CHAIN).whenDisconnectedFromTheRight());
      if (r != null) this.setPart(level, right, r.getValue(SIDE_CHAIN).whenDisconnectedFromTheLeft());
   }

   public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
      if (level.isClientSide) return;
      if (state.getValue(POWERED)) {
         if (old.getBlock() instanceof ShelfBlock && old.getValue(POWERED) && old.getValue(SIDE_CHAIN).isConnected() || state.getValue(SIDE_CHAIN).isConnected()) return;
         Direction facing = state.getValue(FACING);
         BlockPos leftPos = pos.relative(facing.getClockWise());
         BlockPos rightPos = pos.relative(facing.getCounterClockWise());
         BlockState l = neighborAt(level, leftPos, facing);
         BlockState r = neighborAt(level, rightPos, facing);
         SideChainPart self = SideChainPart.UNCONNECTED;
         int leftChain = l != null ? allConnected(level, leftPos).size() : 0;
         int rightChain = r != null ? allConnected(level, rightPos).size() : 0;
         int cur = 1;
         if (leftChain > 0 && cur + leftChain <= 3) {
            self = self.whenConnectedToTheLeft();
            this.setPart(level, leftPos, l.getValue(SIDE_CHAIN).whenConnectedToTheRight());
            cur += leftChain;
         }
         if (rightChain > 0 && cur + rightChain <= 3) {
            self = self.whenConnectedToTheRight();
            this.setPart(level, rightPos, r.getValue(SIDE_CHAIN).whenConnectedToTheLeft());
         }
         this.setPart(level, pos, self);
      } else {
         this.updateNeighborsAfterPoweringDown(level, pos, state);
      }
   }
}
