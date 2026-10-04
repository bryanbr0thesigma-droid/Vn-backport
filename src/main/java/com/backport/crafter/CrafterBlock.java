package com.backport.crafter;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.FrontAndTop;

public class CrafterBlock extends BaseEntityBlock {
   public static final BooleanProperty CRAFTING = BooleanProperty.create("crafting");
   public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
   public static final EnumProperty<FrontAndTop> ORIENTATION = BlockStateProperties.ORIENTATION;

   public CrafterBlock(Properties properties) {
      super(properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(ORIENTATION, FrontAndTop.NORTH_UP).setValue(TRIGGERED, false).setValue(CRAFTING, false));
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   public boolean hasAnalogOutputSignal(BlockState state) {
      return true;
   }

   public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
      return level.getBlockEntity(pos) instanceof CrafterBlockEntity c ? c.getRedstoneSignal() : 0;
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos from, boolean moved) {
      boolean should = level.hasNeighborSignal(pos);
      boolean is = state.getValue(TRIGGERED);
      BlockEntity be = level.getBlockEntity(pos);
      if (should && !is) {
         level.scheduleTick(pos, this, 4);
         level.setBlock(pos, state.setValue(TRIGGERED, true), 2);
         if (be instanceof CrafterBlockEntity c) c.setTriggered(true);
      } else if (!should && is) {
         level.setBlock(pos, state.setValue(TRIGGERED, false).setValue(CRAFTING, false), 2);
         if (be instanceof CrafterBlockEntity c) c.setTriggered(false);
      }
   }

   public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      this.dispenseFrom(state, level, pos);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return level.isClientSide ? null : createTickerHelper(type, CrafterBlockEntity.TYPE, CrafterBlockEntity::serverTick);
   }

   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      CrafterBlockEntity c = new CrafterBlockEntity(pos, state);
      c.setTriggered(state.getValue(TRIGGERED));
      return c;
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Direction look = context.getNearestLookingDirection().getOpposite();
      Direction top = switch (look) {
         case DOWN -> context.getHorizontalDirection().getOpposite();
         case UP -> context.getHorizontalDirection();
         default -> Direction.UP;
      };
      return this.defaultBlockState().setValue(ORIENTATION, FrontAndTop.fromFrontAndTop(look, top)).setValue(TRIGGERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack stack) {
      if (state.getValue(TRIGGERED)) level.scheduleTick(pos, this, 4);
   }

   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.is(newState.getBlock())) {
         if (level.getBlockEntity(pos) instanceof Container c) {
            Containers.dropContents(level, pos, c);
            level.updateNeighbourForOutputSignal(pos, this);
         }
         super.onRemove(state, level, pos, newState, moved);
      }
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (!level.isClientSide && level.getBlockEntity(pos) instanceof CrafterBlockEntity c) {
         player.openMenu(c);
      }
      return InteractionResult.sidedSuccess(level.isClientSide);
   }

   public static Optional<CraftingRecipe> getPotentialResults(Level level, CraftingContainer input) {
      return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
   }

   protected void dispenseFrom(BlockState state, ServerLevel level, BlockPos pos) {
      if (level.getBlockEntity(pos) instanceof CrafterBlockEntity be) {
         Optional<CraftingRecipe> recipe = getPotentialResults(level, be);
         if (recipe.isEmpty()) {
            level.playSound(null, pos, com.backport.BackportSounds.BLOCK_CRAFTER_FAIL, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
            return;
         }
         CraftingRecipe r = recipe.get();
         ItemStack results = r.assemble(be, level.registryAccess());
         if (results.isEmpty()) {
            level.playSound(null, pos, com.backport.BackportSounds.BLOCK_CRAFTER_FAIL, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
            return;
         }
         be.setCraftingTicksRemaining(6);
         level.setBlock(pos, state.setValue(CRAFTING, true), 2);
         this.dispenseItem(level, pos, be, results, state);
         if (results.getItem() == com.backport.BackportItems.CRAFTER.asItem()) {
            for (net.minecraft.server.level.ServerPlayer near : level.getEntitiesOfClass(net.minecraft.server.level.ServerPlayer.class, new net.minecraft.world.phys.AABB(pos).inflate(8.5))) {
               com.backport.advancement.BackportEvents.fire(near, "crafter_crafted_crafter");
            }
         }
         for (ItemStack rem : r.getRemainingItems(be)) {
            if (!rem.isEmpty()) this.dispenseItem(level, pos, be, rem, state);
         }
         be.getItems().forEach(it -> {
            if (!it.isEmpty()) it.shrink(1);
         });
         be.setChanged();
      }
   }

   private void dispenseItem(ServerLevel level, BlockPos pos, CrafterBlockEntity be, ItemStack results, BlockState state) {
      Direction direction = state.getValue(ORIENTATION).front();
      Container into = HopperBlockEntity.getContainerAt(level, pos.relative(direction));
      ItemStack remaining = results.copy();
      if (into != null && (into instanceof CrafterBlockEntity || results.getCount() > into.getMaxStackSize())) {
         while (!remaining.isEmpty()) {
            ItemStack one = remaining.copy();
            one.setCount(1);
            ItemStack left = HopperBlockEntity.addItem(be, into, one, direction.getOpposite());
            if (!left.isEmpty()) break;
            remaining.shrink(1);
         }
      } else if (into != null) {
         while (!remaining.isEmpty()) {
            int old = remaining.getCount();
            remaining = HopperBlockEntity.addItem(be, into, remaining, direction.getOpposite());
            if (old == remaining.getCount()) break;
         }
      }
      if (!remaining.isEmpty()) {
         Vec3 center = Vec3.atCenterOf(pos);
         Vec3 spawn = center.relative(direction, 0.7);
         DefaultDispenseItemBehavior.spawnItem(level, remaining, 6, direction, spawn);
         level.playSound(null, pos, com.backport.BackportSounds.BLOCK_CRAFTER_CRAFT, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
         level.levelEvent(2000, pos, direction.get3DDataValue());
      }
   }

   public BlockState rotate(BlockState state, Rotation rotation) {
      return state.setValue(ORIENTATION, rotation.rotation().rotate(state.getValue(ORIENTATION)));
   }

   public BlockState mirror(BlockState state, Mirror mirror) {
      return state.setValue(ORIENTATION, mirror.rotation().rotate(state.getValue(ORIENTATION)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(ORIENTATION, TRIGGERED, CRAFTING);
   }
}
