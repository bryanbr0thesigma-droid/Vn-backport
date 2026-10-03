package com.backport.entity;

import com.backport.CopperChestBlock;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

/** Simplified copper-golem logistics: take an item from a copper chest and drop it into a matching ordinary chest. */
public class CopperGolemTransportGoal extends Goal {
   private static final int RANGE = 16;
   private final CopperGolem golem;
   private BlockPos target;
   private int cooldown = 60 + 20;
   private int interactTicks;
   private boolean deliver;

   public CopperGolemTransportGoal(CopperGolem golem) {
      this.golem = golem;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   private static boolean isSource(BlockEntity be) {
      return be instanceof ChestBlockEntity && be.getBlockState().getBlock() instanceof CopperChestBlock;
   }

   private static boolean isDestination(BlockEntity be) {
      return be instanceof ChestBlockEntity && !(be.getBlockState().getBlock() instanceof CopperChestBlock);
   }

   public boolean canUse() {
      if (this.cooldown > 0) {
         this.cooldown--;
         return false;
      }

      this.deliver = !this.golem.getMainHandItem().isEmpty();
      this.target = this.findTarget();
      return this.target != null;
   }

   public boolean canContinueToUse() {
      return this.target != null && this.golem.isAlive();
   }

   public void start() {
      this.interactTicks = 0;
      this.golem.getNavigation().moveTo(this.target.getX() + 0.5, this.target.getY(), this.target.getZ() + 0.5, 1.0);
   }

   public void stop() {
      this.golem.setState(CopperGolem.State.IDLE);
      this.golem.getNavigation().stop();
      this.target = null;
      this.cooldown = 60 + this.golem.getRandom().nextInt(40);
   }

   public void tick() {
      double dist = this.golem.distanceToSqr(this.target.getX() + 0.5, this.target.getY() + 0.5, this.target.getZ() + 0.5);
      if (dist > 6.0) {
         if (this.golem.getNavigation().isDone()) {
            this.golem.getNavigation().moveTo(this.target.getX() + 0.5, this.target.getY(), this.target.getZ() + 0.5, 1.0);
         }

         if (++this.interactTicks > 400) {
            this.target = null;
         }

         return;
      }

      this.golem.getNavigation().stop();
      this.golem.getLookControl().setLookAt(this.target.getX() + 0.5, this.target.getY() + 0.5, this.target.getZ() + 0.5);
      Level level = this.golem.level();
      BlockEntity be = level.getBlockEntity(this.target);
      if (!(be instanceof ChestBlockEntity chest)) {
         this.target = null;
         return;
      }

      if (this.interactTicks == 0) {
         this.golem.setState(this.deliver ? CopperGolem.State.DROPPING_ITEM : CopperGolem.State.GETTING_ITEM);
         ChestBlock.getContainer((ChestBlock)chest.getBlockState().getBlock(), chest.getBlockState(), level, this.target, true);
         chest.startOpen(null);
      }

      if (++this.interactTicks >= 20) {
         Container container = ChestBlock.getContainer((ChestBlock)chest.getBlockState().getBlock(), chest.getBlockState(), level, this.target, true);
         if (container == null) {
            container = chest;
         }

         if (this.deliver) {
            ItemStack held = this.golem.getMainHandItem();
            ItemStack rest = insert(container, held.copy());
            this.golem.setItemInHand(InteractionHand.MAIN_HAND, rest);
         } else {
            for (int i = 0; i < container.getContainerSize(); i++) {
               ItemStack stack = container.getItem(i);
               if (!stack.isEmpty()) {
                  this.golem.setItemInHand(InteractionHand.MAIN_HAND, container.removeItem(i, 1));
                  break;
               }
            }
         }

         chest.stopOpen(null);
         this.target = null;
      }
   }

   private static ItemStack insert(Container container, ItemStack stack) {
      for (int i = 0; i < container.getContainerSize() && !stack.isEmpty(); i++) {
         ItemStack slot = container.getItem(i);
         if (slot.isEmpty()) {
            container.setItem(i, stack.copy());
            return ItemStack.EMPTY;
         }

         if (ItemStack.isSameItemSameTags(slot, stack) && slot.getCount() < slot.getMaxStackSize()) {
            int move = Math.min(stack.getCount(), slot.getMaxStackSize() - slot.getCount());
            slot.grow(move);
            stack.shrink(move);
         }
      }

      container.setChanged();
      return stack;
   }

   private BlockPos findTarget() {
      Level level = this.golem.level();
      BlockPos origin = this.golem.blockPosition();
      BlockPos best = null;
      double bestDist = Double.MAX_VALUE;
      boolean wantMatch = this.deliver;
      ItemStack held = this.golem.getMainHandItem();
      for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-RANGE, -4, -RANGE), origin.offset(RANGE, 4, RANGE))) {
         BlockEntity be = level.getBlockEntity(pos);
         if (be == null) {
            continue;
         }

         if (this.deliver ? !isDestination(be) : !isSource(be)) {
            continue;
         }

         Container container = (Container)be;
         boolean ok;
         if (this.deliver) {
            ok = false;
            boolean hasSpace = false;
            boolean matches = false;
            for (int i = 0; i < container.getContainerSize(); i++) {
               ItemStack slot = container.getItem(i);
               if (slot.isEmpty()) {
                  hasSpace = true;
               } else if (ItemStack.isSameItemSameTags(slot, held)) {
                  matches = true;
                  if (slot.getCount() < slot.getMaxStackSize()) {
                     hasSpace = true;
                  }
               }
            }

            ok = hasSpace && matches;
         } else {
            ok = !container.isEmpty();
         }

         if (ok) {
            double d = pos.distSqr(origin);
            if (d < bestDist) {
               bestDist = d;
               best = pos.immutable();
            }
         }
      }

      return best;
   }
}
