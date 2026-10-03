package com.backport.entity;

import com.backport.BackportSounds;
import com.backport.CopperChestBlock;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Port of the 26.x TransportItemsBetweenContainers behaviour for the copper golem (copper chests -> ordinary chests). */
public class CopperGolemTransportGoal extends Goal {
   private static final int HORIZONTAL = 32;
   private static final int VERTICAL = 8;
   private static final int INTERACT_TICKS = 60;
   private static final int IDLE_COOLDOWN = 140;
   private static final int MAX_STACK = 16;
   private final CopperGolem golem;
   private final Set<BlockPos> visited = new HashSet<>();
   private final Set<BlockPos> unreachable = new HashSet<>();
   private BlockPos target;
   private Phase phase = Phase.TRAVELLING;
   private int ticksAtTarget;
   private int cooldown = 60 + 20;
   private long visitedExpiry;
   private boolean opened;

   private enum Phase {
      TRAVELLING,
      INTERACTING
   }

   public CopperGolemTransportGoal(CopperGolem golem) {
      this.golem = golem;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   private static boolean isSource(BlockState state) {
      return state.getBlock() instanceof CopperChestBlock;
   }

   private static boolean isDestination(BlockState state) {
      return state.is(net.minecraft.world.level.block.Blocks.CHEST) || state.is(net.minecraft.world.level.block.Blocks.TRAPPED_CHEST);
   }

   private boolean picking() {
      return this.golem.getMainHandItem().isEmpty();
   }

   private boolean wanted(BlockState state) {
      return this.picking() ? isSource(state) : isDestination(state);
   }

   public boolean canUse() {
      if (this.cooldown > 0) {
         this.cooldown--;
         return false;
      }

      if (this.golem.isLeashed()) {
         return false;
      }

      if (this.golem.level().getGameTime() > this.visitedExpiry) {
         this.visited.clear();
         this.unreachable.clear();
      }

      this.target = this.findTarget();
      if (this.target == null) {
         this.cooldown = IDLE_COOLDOWN;
         this.visited.clear();
         this.unreachable.clear();
         return false;
      }

      return true;
   }

   public boolean canContinueToUse() {
      return this.target != null && this.golem.isAlive() && !this.golem.isLeashed();
   }

   public void start() {
      this.phase = Phase.TRAVELLING;
      this.ticksAtTarget = 0;
      this.visited.add(this.target);
      this.visitedExpiry = this.golem.level().getGameTime() + 6000L;
      this.walk();
   }

   public void stop() {
      this.closeChest();
      this.golem.setState(CopperGolem.State.IDLE);
      this.golem.getNavigation().stop();
      this.target = null;
      this.ticksAtTarget = 0;
   }

   private void walk() {
      if (this.target != null) {
         this.golem.getNavigation().moveTo(this.target.getX() + 0.5, this.target.getY(), this.target.getZ() + 0.5, 1.0);
      }
   }

   public void tick() {
      Level level = this.golem.level();
      if (this.target == null) {
         return;
      }

      BlockEntity be = level.getBlockEntity(this.target);
      BlockState state = level.getBlockState(this.target);
      if (!(be instanceof ChestBlockEntity) || !this.wanted(state) || ChestBlock.isChestBlockedAt(level, this.target)) {
         this.drop();
         return;
      }

      if (this.phase == Phase.TRAVELLING) {
         if (this.withinReach(state, 0.5) || (this.golem.getNavigation().isDone() && this.withinReach(state, 1.5))) {
            if (this.canSee(state)) {
               this.phase = Phase.INTERACTING;
               this.ticksAtTarget = 0;
               this.golem.getNavigation().stop();
               this.startInteraction(be);
            } else {
               this.unreachable.add(this.target);
               this.drop();
            }
         } else if (this.golem.getNavigation().isDone()) {
            if (this.golem.getNavigation().createPath(this.target, 0) == null) {
               this.unreachable.add(this.target);
               this.visited.remove(this.target);
               this.drop();
            } else {
               this.walk();
            }
         }

         return;
      }

      if (!this.withinReach(state, 2.0)) {
         this.phase = Phase.TRAVELLING;
         this.closeChest();
         this.walk();
         return;
      }

      this.golem.getNavigation().stop();
      this.golem.setDeltaMovement(0.0, this.golem.getDeltaMovement().y, 0.0);
      this.golem.getLookControl().setLookAt(Vec3.atCenterOf(this.target));
      this.ticksAtTarget++;
      if (this.ticksAtTarget == 9) {
         this.playInteractionSound();
      }

      if (this.ticksAtTarget >= INTERACT_TICKS) {
         this.finishInteraction((ChestBlockEntity)be, state);
      }
   }

   private Container container(ChestBlockEntity chest, BlockState state) {
      Container container = ChestBlock.getContainer((ChestBlock)state.getBlock(), state, this.golem.level(), this.target, false);
      return container != null ? container : chest;
   }

   private boolean matchesLeaving(Container container) {
      if (container.isEmpty()) {
         return true;
      }

      ItemStack held = this.golem.getMainHandItem();
      for (int i = 0; i < container.getContainerSize(); i++) {
         if (ItemStack.isSameItem(container.getItem(i), held)) {
            return true;
         }
      }

      return false;
   }

   private void startInteraction(BlockEntity be) {
      ChestBlockEntity chest = (ChestBlockEntity)be;
      BlockState state = chest.getBlockState();
      Container container = this.container(chest, state);
      boolean picking = this.picking();
      CopperGolem.State anim;
      if (picking) {
         anim = container.isEmpty() ? CopperGolem.State.GETTING_NO_ITEM : CopperGolem.State.GETTING_ITEM;
      } else {
         anim = this.matchesLeaving(container) ? CopperGolem.State.DROPPING_ITEM : CopperGolem.State.DROPPING_NO_ITEM;
      }

      this.golem.setState(anim);
      this.openChest(chest, state);
   }

   private void playInteractionSound() {
      SoundEvent sound = switch (this.golem.getState()) {
         case GETTING_ITEM -> BackportSounds.ENTITY_COPPER_GOLEM_NO_ITEM_GET;
         case GETTING_NO_ITEM -> BackportSounds.ENTITY_COPPER_GOLEM_NO_ITEM_NO_GET;
         case DROPPING_ITEM -> BackportSounds.ENTITY_COPPER_GOLEM_ITEM_DROP;
         case DROPPING_NO_ITEM -> BackportSounds.ENTITY_COPPER_GOLEM_ITEM_NO_DROP;
         default -> null;
      };
      if (sound != null) {
         this.golem.playSound(sound, 1.0F, 1.0F);
      }
   }

   private void finishInteraction(ChestBlockEntity chest, BlockState state) {
      Container container = this.container(chest, state);
      if (this.picking()) {
         if (!container.isEmpty()) {
            for (int i = 0; i < container.getContainerSize(); i++) {
               ItemStack stack = container.getItem(i);
               if (!stack.isEmpty()) {
                  this.golem.setItemSlot(EquipmentSlot.MAINHAND, container.removeItem(i, Math.min(stack.getCount(), MAX_STACK)));
                  break;
               }
            }

            container.setChanged();
            this.visited.clear();
            this.unreachable.clear();
         }
      } else if (this.matchesLeaving(container)) {
         ItemStack left = this.addItems(container, this.golem.getMainHandItem());
         container.setChanged();
         this.golem.setItemSlot(EquipmentSlot.MAINHAND, left);
         if (left.isEmpty()) {
            this.visited.clear();
            this.unreachable.clear();
         }
      }

      this.drop();
   }

   private ItemStack addItems(Container container, ItemStack stack) {
      ItemStack remaining = stack.copy();
      for (int i = 0; i < container.getContainerSize() && !remaining.isEmpty(); i++) {
         ItemStack slot = container.getItem(i);
         if (slot.isEmpty()) {
            container.setItem(i, remaining);
            return ItemStack.EMPTY;
         }

         if (ItemStack.isSameItemSameTags(slot, remaining) && slot.getCount() < slot.getMaxStackSize()) {
            int move = Math.min(slot.getMaxStackSize() - slot.getCount(), remaining.getCount());
            slot.grow(move);
            remaining.shrink(move);
         }
      }

      return remaining;
   }

   private void drop() {
      this.closeChest();
      this.golem.getNavigation().stop();
      this.target = null;
      this.ticksAtTarget = 0;
      this.golem.setState(CopperGolem.State.IDLE);
   }

   private void openChest(ChestBlockEntity chest, BlockState state) {
      if (!this.opened) {
         this.opened = true;
         this.signal(1);
      }
   }

   private void closeChest() {
      if (this.opened && this.target != null) {
         this.signal(0);
      }

      this.opened = false;
   }

   private void signal(int open) {
      Level level = this.golem.level();
      BlockState state = level.getBlockState(this.target);
      if (state.getBlock() instanceof ChestBlock) {
         level.blockEvent(this.target, state.getBlock(), 1, open);
         level.playSound(null, this.target, open > 0 ? net.minecraft.sounds.SoundEvents.CHEST_OPEN : net.minecraft.sounds.SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, 1.0F);
         if (state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            BlockPos other = this.target.relative(ChestBlock.getConnectedDirection(state));
            level.blockEvent(other, state.getBlock(), 1, open);
         }
      }
   }

   private boolean withinReach(BlockState state, double dist) {
      AABB box = this.golem.getBoundingBox();
      AABB moved = AABB.ofSize(this.golem.position().add(0.0, box.getYsize() / 2.0, 0.0), box.getXsize(), box.getYsize(), box.getZsize());
      return state.getCollisionShape(this.golem.level(), this.target).bounds().inflate(dist, 0.5, dist).move(this.target).intersects(moved);
   }

   private boolean canSee(BlockState state) {
      Level level = this.golem.level();
      Vec3 eye = this.golem.position().add(0.0, this.golem.getBoundingBox().getYsize() / 2.0, 0.0);
      Vec3 center = Vec3.atCenterOf(this.target);
      for (Direction direction : Direction.values()) {
         Vec3 hit = center.add(0.5 * direction.getStepX(), 0.5 * direction.getStepY(), 0.5 * direction.getStepZ());
         var result = level.clip(new ClipContext(eye, hit, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.golem));
         if (result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(this.target)) {
            return true;
         }
      }

      return false;
   }

   private BlockPos findTarget() {
      Level level = this.golem.level();
      BlockPos origin = this.golem.blockPosition();
      BlockPos best = null;
      double bestDist = Double.MAX_VALUE;
      for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-HORIZONTAL, -VERTICAL, -HORIZONTAL), origin.offset(HORIZONTAL, VERTICAL, HORIZONTAL))) {
         BlockState state = level.getBlockState(pos);
         if (!(state.getBlock() instanceof ChestBlock) || !this.wanted(state)) {
            continue;
         }

         BlockEntity be = level.getBlockEntity(pos);
         if (!(be instanceof ChestBlockEntity chest) || this.visited.contains(pos) || this.unreachable.contains(pos)) {
            continue;
         }

         if (ChestBlock.isChestBlockedAt(level, pos)) {
            continue;
         }

         if (state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            BlockPos other = pos.relative(ChestBlock.getConnectedDirection(state));
            if (this.visited.contains(other) || this.unreachable.contains(other)) {
               continue;
            }
         }

         double d = pos.distToCenterSqr(this.golem.position());
         if (d < bestDist) {
            bestDist = d;
            best = pos.immutable();
         }
      }

      return best;
   }
}
