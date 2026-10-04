package com.backport.ai;

import com.backport.SpearItem;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.phys.Vec3;

/** Piglin version of the spear approach, charge and retreat cycle (26.x SpearApproach / SpearAttack / SpearRetreat). */
public class PiglinSpearBehavior extends Behavior<Piglin> {
   private static final int MAX_FLEEING_TIME = 100;
   private static final double APPROACH_DISTANCE_SQ = 100.0;
   private static final double IN_RANGE_SQ = 4.0;
   private int engageTime;
   private int fleeingTime;
   private Vec3 awayPos;
   private boolean done;

   public PiglinSpearBehavior() {
      super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT), 1200, 1200);
   }

   private static LivingEntity target(Piglin piglin) {
      return piglin.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
   }

   private static boolean able(Piglin piglin) {
      return target(piglin) != null && piglin.getMainHandItem().getItem() instanceof SpearItem;
   }

   @Override
   protected boolean checkExtraStartConditions(ServerLevel level, Piglin piglin) {
      return able(piglin) && !piglin.isUsingItem();
   }

   @Override
   protected boolean canStillUse(ServerLevel level, Piglin piglin, long time) {
      return !this.done && able(piglin);
   }

   @Override
   protected boolean timedOut(long time) {
      return false;
   }

   @Override
   protected void start(ServerLevel level, Piglin piglin, long time) {
      piglin.setAggressive(true);
      this.engageTime = -1;
      this.fleeingTime = -1;
      this.awayPos = null;
      this.done = false;
   }

   @Override
   protected void stop(ServerLevel level, Piglin piglin, long time) {
      piglin.getNavigation().stop();
      piglin.setAggressive(false);
      piglin.stopUsingItem();
      this.awayPos = null;
   }

   @Override
   protected void tick(ServerLevel level, Piglin piglin, long time) {
      LivingEntity target = target(piglin);
      if (target == null || !(piglin.getMainHandItem().getItem() instanceof SpearItem spear)) {
         return;
      }
      double distSqr = piglin.distanceToSqr(target.getX(), target.getY(), target.getZ());
      piglin.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
      piglin.lookAt(target, 30.0F, 30.0F);
      if (this.engageTime < 0) {
         if (distSqr > APPROACH_DISTANCE_SQ) {
            piglin.getNavigation().moveTo(target, 1.0);
            return;
         }
         this.engageTime = spear.damageUseDuration();
         piglin.startUsingItem(InteractionHand.MAIN_HAND);
      }
      if (this.engageTime > 0 && --this.engageTime == 0) {
         piglin.stopUsingItem();
         double distance = Math.sqrt(distSqr);
         this.awayPos = LandRandomPos.getPosAway(piglin, Math.max(1, (int) (11 - distance)), 7, target.position());
         this.fleeingTime = 1;
      }
      if (this.fleeingTime > 0 && ++this.fleeingTime > MAX_FLEEING_TIME) {
         this.done = true;
         return;
      }
      if (this.awayPos != null) {
         piglin.getNavigation().moveTo(this.awayPos.x, this.awayPos.y, this.awayPos.z, 1.0);
         if (piglin.getNavigation().isDone()) {
            if (this.fleeingTime > 0) {
               this.done = true;
               return;
            }
            this.awayPos = null;
         }
      } else {
         piglin.getNavigation().moveTo(target, 1.0);
         if (distSqr < IN_RANGE_SQ || piglin.getNavigation().isDone()) {
            double distance = Math.sqrt(distSqr);
            this.awayPos = LandRandomPos.getPosAway(piglin, Math.max(1, (int) (7 - distance)), 7, target.position());
         }
      }
   }
}
