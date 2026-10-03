package com.backport.trial;

import com.backport.BackportSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/** Server-side replacements for the 1.21 trial level events (the 1.20.1 client has no handlers for them). */
public final class TrialFx {
   private TrialFx() {
   }

   private static void burst(ServerLevel l, ParticleOptions p, double x, double y, double z, int n, double spread, double speed) {
      l.sendParticles(p, x, y, z, n, spread, spread, spread, speed);
   }

   public static void event(ServerLevel l, int event, BlockPos pos, int data) {
      RandomSource r = l.getRandom();
      double x = pos.getX() + 0.5, y = pos.getY() + 0.5, z = pos.getZ() + 0.5;
      ParticleOptions flame = data == 1 ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME;
      switch (event) {
         case 3011, 3012 -> {
            burst(l, ParticleTypes.SMOKE, x, y, z, 20, 0.9, 0.0);
            burst(l, flame, x, y, z, 20, 0.9, 0.0);
            if (event == 3011) l.playSound(null, pos, BackportSounds.BLOCK_TRIAL_SPAWNER_SPAWN_MOB, SoundSource.BLOCKS, 1.0F, (r.nextFloat() - r.nextFloat()) * 0.2F + 1.0F);
         }
         case 3013, 3019 -> {
            burst(l, event == 3019 ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMALL_FLAME, x, pos.getY() + 0.5, z, 30 + Math.min(data, 10) * 5, 0.6, 0.0);
            l.playSound(null, pos, BackportSounds.BLOCK_TRIAL_SPAWNER_DETECT_PLAYER, SoundSource.BLOCKS, 1.0F, 0.8F + 0.2F * Math.min(data, 10) / 10.0F);
         }
         case 3014 -> {
            burst(l, ParticleTypes.SMALL_FLAME, x, y, z, 20, 0.3, 0.02);
            burst(l, ParticleTypes.SMOKE, x, y, z, 20, 0.3, 0.02);
            l.playSound(null, pos, BackportSounds.BLOCK_TRIAL_SPAWNER_EJECT_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
         }
         case 3015 -> {
            burst(l, ParticleTypes.SMOKE, x, y, z, 20, 0.4, 0.0);
            burst(l, data == 1 ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMALL_FLAME, x, y, z, 20, 0.4, 0.0);
            l.playSound(null, pos, BackportSounds.BLOCK_VAULT_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
         }
         case 3016 -> {
            burst(l, data == 1 ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMALL_FLAME, x, y, z, 20, 0.1, 0.02);
            l.playSound(null, pos, BackportSounds.BLOCK_VAULT_DEACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
         }
         case 3017 -> burst(l, ParticleTypes.SMALL_FLAME, x, y + 1.2, z, 20, 0.2, 0.02);
         case 3020 -> {
            burst(l, ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 20, 0.9, 0.02);
            burst(l, ParticleTypes.ENCHANT, x, y, z, 20, 0.9, 0.02);
            l.playSound(null, pos, BackportSounds.BLOCK_TRIAL_SPAWNER_OMINOUS_ACTIVATE, SoundSource.BLOCKS, data == 0 ? 0.3F : 1.0F, 1.0F);
         }
         default -> {
         }
      }
   }
}
