package com.backport;

import com.backport.entity.CopperGolem;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public final class CopperGolemStatues {
   private CopperGolemStatues() {
   }

   public static boolean enabled() {
      return true;
   }

   public static void turnToStatue(CopperGolem golem, ServerLevel level) {
      BlockState state = CopperBlocks.COPPER_GOLEM_STATUE[3].defaultBlockState()
         .setValue(CopperGolemStatueBlock.POSE, CopperGolemStatueBlock.Pose.values()[golem.getRandom().nextInt(CopperGolemStatueBlock.Pose.values().length)])
         .setValue(CopperGolemStatueBlock.FACING, Direction.fromYRot(golem.getYRot()));
      level.setBlockAndUpdate(golem.blockPosition(), state);
      golem.playSound(BackportSounds.ENTITY_COPPER_GOLEM_BECOME_STATUE, 1.0F, 1.0F);
      golem.discard();
   }
}
