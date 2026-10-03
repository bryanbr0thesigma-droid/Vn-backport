package com.backport.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Lets the vanilla (unaffected) lightning rod oxidise like the other copper blocks. */
@Mixin(LightningRodBlock.class)
public abstract class LightningRodBlockMixin extends Block {
   protected LightningRodBlockMixin(Properties properties) {
      super(properties);
   }

   @Override
   public boolean isRandomlyTicking(BlockState state) {
      return state.is(Blocks.LIGHTNING_ROD) ? WeatheringCopper.getNext(state.getBlock()).isPresent() : super.isRandomlyTicking(state);
   }

   @Inject(method = "randomTick", at = @At("HEAD"), require = 0)
   private void backport$oxidise(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
      if (!state.is(Blocks.LIGHTNING_ROD)) {
         return;
      }

      if (random.nextFloat() >= 0.05688889F) {
         return;
      }

      int higher = 0;
      int same = 0;
      for (BlockPos other : BlockPos.withinManhattan(pos, 4, 4, 4)) {
         int distance = other.distManhattan(pos);
         if (distance > 4) {
            break;
         }

         if (!other.equals(pos)) {
            BlockState neighbor = level.getBlockState(other);
            if (neighbor.getBlock() instanceof WeatheringCopper copper) {
               WeatheringCopper.WeatherState age = copper.getAge();
               if (age.ordinal() < WeatheringCopper.WeatherState.UNAFFECTED.ordinal()) {
                  return;
               }

               if (age.ordinal() > WeatheringCopper.WeatherState.UNAFFECTED.ordinal()) {
                  higher++;
               } else {
                  same++;
               }
            }
         }
      }

      float chance = (float)(higher + 1) / (float)(higher + same + 1);
      if (random.nextFloat() < chance * chance * 0.75F) {
         WeatheringCopper.getNext(state.getBlock()).ifPresent(next -> level.setBlockAndUpdate(pos, copyRodState(state, next.defaultBlockState())));
      }
   }

   private static BlockState copyRodState(BlockState old, BlockState next) {
      for (var property : old.getProperties()) {
         if (next.hasProperty(property)) {
            next = copyValue(old, next, property);
         }
      }

      return next;
   }

   private static <T extends Comparable<T>> BlockState copyValue(BlockState from, BlockState to, net.minecraft.world.level.block.state.properties.Property<T> property) {
      return to.setValue(property, from.getValue(property));
   }
}
