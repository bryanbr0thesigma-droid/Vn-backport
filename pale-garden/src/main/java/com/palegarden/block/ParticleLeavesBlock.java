package com.palegarden.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ParticleLeavesBlock extends LeavesBlock {
   private final ParticleOptions particle;
   private final int chance;

   public ParticleLeavesBlock(int chance, ParticleOptions particle, BlockBehaviour.Properties properties) {
      super(properties);
      this.chance = chance;
      this.particle = particle;
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      super.animateTick(state, level, pos, random);
      if (random.nextInt(this.chance) == 0) {
         BlockPos below = pos.below();
         BlockState belowState = level.getBlockState(below);
         if (!isFaceFull(belowState.getCollisionShape(level, below), Direction.UP)) {
            ParticleUtils.spawnParticleBelow(level, pos, random, this.particle);
         }
      }
   }
}
