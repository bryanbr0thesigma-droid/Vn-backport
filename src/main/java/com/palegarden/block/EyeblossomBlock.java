package com.palegarden.block;

import com.palegarden.PaleBlocks;
import com.palegarden.PaleSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class EyeblossomBlock extends FlowerBlock {
   private final EyeblossomBlock.Type type;

   public EyeblossomBlock(EyeblossomBlock.Type type, BlockBehaviour.Properties properties) {
      super(type.effect(), type.effectSeconds, properties);
      this.type = type;
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if (this.type.open && random.nextInt(700) == 0) {
         BlockState below = level.getBlockState(pos.below());
         if (below.is(PaleBlocks.PALE_MOSS_BLOCK)) {
            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), PaleSounds.BLOCK_EYEBLOSSOM_IDLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
         }
      }
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (this.tryChangingState(state, level, pos, random)) {
         level.playSound(null, pos, this.type.transform().longSwitchSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
      }

      super.randomTick(state, level, pos, random);
   }

   public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (this.tryChangingState(state, level, pos, random)) {
         level.playSound(null, pos, this.type.transform().shortSwitchSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
      }

      super.tick(state, level, pos, random);
   }

   private boolean tryChangingState(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (!level.dimensionType().natural()) {
         return false;
      } else if (level.isDay() != this.type.open) {
         return false;
      } else {
         EyeblossomBlock.Type next = this.type.transform();
         level.setBlock(pos, next.state(), 3);
         level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
         next.spawnTransformParticle(level, pos, random);
         BlockPos.betweenClosed(pos.offset(-3, -2, -3), pos.offset(3, 2, 3)).forEach(other -> {
            BlockState otherState = level.getBlockState(other);
            if (otherState == state) {
               double distance = Math.sqrt(pos.distSqr(other));
               int delay = random.nextIntBetweenInclusive((int)(distance * 5.0), (int)(distance * 10.0));
               level.scheduleTick(other.immutable(), state.getBlock(), delay);
            }
         });
         return true;
      }
   }

   public enum Type {
      OPEN(true, 11, 16545810),
      CLOSED(false, 7, 6250335);

      final boolean open;
      final int effectSeconds;
      private final int particleColor;

      Type(boolean open, int effectSeconds, int particleColor) {
         this.open = open;
         this.effectSeconds = effectSeconds;
         this.particleColor = particleColor;
      }

      public MobEffect effect() {
         return this.open ? MobEffects.BLINDNESS : MobEffects.CONFUSION;
      }

      public Block block() {
         return this.open ? PaleBlocks.OPEN_EYEBLOSSOM : PaleBlocks.CLOSED_EYEBLOSSOM;
      }

      public BlockState state() {
         return this.block().defaultBlockState();
      }

      public EyeblossomBlock.Type transform() {
         return this.open ? CLOSED : OPEN;
      }

      public SoundEvent longSwitchSound() {
         return this.open ? PaleSounds.BLOCK_EYEBLOSSOM_OPEN_LONG : PaleSounds.BLOCK_EYEBLOSSOM_CLOSE_LONG;
      }

      public SoundEvent shortSwitchSound() {
         return this.open ? PaleSounds.BLOCK_EYEBLOSSOM_OPEN : PaleSounds.BLOCK_EYEBLOSSOM_CLOSE;
      }

      public void spawnTransformParticle(ServerLevel level, BlockPos pos, RandomSource random) {
         Vec3 center = pos.getCenter();
         float r = (this.particleColor >> 16 & 255) / 255.0F;
         float g = (this.particleColor >> 8 & 255) / 255.0F;
         float b = (this.particleColor & 255) / 255.0F;
         for (int i = 0; i < 6; i++) {
            level.sendParticles(
               new DustParticleOptions(new Vector3f(r, g, b), 1.0F),
               center.x + random.nextDouble() - 0.5,
               center.y + 0.3 + random.nextDouble(),
               center.z + random.nextDouble() - 0.5,
               1, 0.0, 0.05, 0.0, 0.0
            );
         }
      }
   }
}
