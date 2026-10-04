package com.backport.ai;

import com.backport.SpearItem;
import java.util.EnumSet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;

/** 26.x spear behaviour for mobs: approach, charge with the spear lowered, then back off and go again. */
public class SpearUseGoal<T extends Monster> extends Goal {
   private static final int MAX_FLEEING_TIME = reducedTickDelay(100);
   private final T mob;
   private State state;
   private final double chargeSpeed;
   private final double repositionSpeed;
   private final float approachDistanceSq;
   private final float targetInRangeRadiusSq;

   public SpearUseGoal(T mob, double chargeSpeed, double repositionSpeed, float approachDistance, float targetInRangeRadius) {
      this.mob = mob;
      this.chargeSpeed = chargeSpeed;
      this.repositionSpeed = repositionSpeed;
      this.approachDistanceSq = approachDistance * approachDistance;
      this.targetInRangeRadiusSq = targetInRangeRadius * targetInRangeRadius;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
   }

   private boolean ableToAttack() {
      return this.mob.getTarget() != null && this.mob.getMainHandItem().getItem() instanceof SpearItem;
   }

   private int useDuration() {
      return this.mob.getMainHandItem().getItem() instanceof SpearItem s ? reducedTickDelay(s.damageUseDuration()) : 0;
   }

   @Override
   public boolean canUse() {
      return this.ableToAttack() && !this.mob.isUsingItem();
   }

   @Override
   public boolean canContinueToUse() {
      return this.state != null && !this.state.done && this.ableToAttack();
   }

   @Override
   public void start() {
      super.start();
      this.mob.setAggressive(true);
      this.state = new State();
   }

   @Override
   public void stop() {
      super.stop();
      this.mob.getNavigation().stop();
      this.mob.setAggressive(false);
      this.state = null;
      this.mob.stopUsingItem();
   }

   @Override
   public boolean requiresUpdateEveryTick() {
      return true;
   }

   @Override
   public void tick() {
      if (this.state == null) {
         return;
      }
      LivingEntity target = this.mob.getTarget();
      if (target == null) {
         return;
      }
      double distSqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
      Entity mount = this.mob.getRootVehicle();
      double speed = mount instanceof net.minecraft.world.entity.Mob ? 1.2 : 1.0;
      int mountDistance = this.mob.isPassenger() ? 2 : 0;
      this.mob.lookAt(target, 30.0F, 30.0F);
      this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
      if (this.state.engageTime < 0) {
         if (distSqr > this.approachDistanceSq) {
            this.mob.getNavigation().moveTo(target, speed * this.repositionSpeed);
            return;
         }
         this.state.engageTime = this.useDuration();
         this.mob.startUsingItem(InteractionHand.MAIN_HAND);
      }
      if (this.state.engageTime > 0 && --this.state.engageTime == 0) {
         this.mob.stopUsingItem();
         double distance = Math.sqrt(distSqr);
         this.state.awayPos = LandRandomPos.getPosAway(this.mob, Math.max(1, (int) (11 + mountDistance - distance)), 7, target.position());
         this.state.fleeingTime = 1;
      }
      if (this.state.fleeingTime > 0 && ++this.state.fleeingTime > MAX_FLEEING_TIME) {
         this.state.done = true;
         return;
      }
      if (this.state.awayPos != null) {
         this.mob.getNavigation().moveTo(this.state.awayPos.x, this.state.awayPos.y, this.state.awayPos.z, speed * this.repositionSpeed);
         if (this.mob.getNavigation().isDone()) {
            if (this.state.fleeingTime > 0) {
               this.state.done = true;
               return;
            }
            this.state.awayPos = null;
         }
      } else {
         this.mob.getNavigation().moveTo(target, speed * this.chargeSpeed);
         if (distSqr < this.targetInRangeRadiusSq || this.mob.getNavigation().isDone()) {
            double distance = Math.sqrt(distSqr);
            this.state.awayPos = LandRandomPos.getPosAway(this.mob, Math.max(1, (int) (7 + mountDistance - distance)), 7, target.position());
         }
      }
   }

   private static final class State {
      int engageTime = -1;
      int fleeingTime = -1;
      Vec3 awayPos;
      boolean done;
   }
}
