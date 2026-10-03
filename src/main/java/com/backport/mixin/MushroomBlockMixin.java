package com.backport.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 26.4: mushrooms no longer uproot in bright light; only spreading still needs darkness. */
@Mixin(MushroomBlock.class)
public abstract class MushroomBlockMixin {
   @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
   private void backport$survivesInLight(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
      BlockPos belowPos = pos.below();
      BlockState below = level.getBlockState(belowPos);
      cir.setReturnValue(below.is(BlockTags.MUSHROOM_GROW_BLOCK) || ((MushroomBlockAccess) this).backport$mayPlaceOn(below, level, belowPos));
   }

   @Redirect(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;canSurvive(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z"))
   private boolean backport$spreadNeedsDark(BlockState state, LevelReader level, BlockPos pos) {
      BlockPos belowPos = pos.below();
      BlockState below = level.getBlockState(belowPos);
      return below.is(BlockTags.MUSHROOM_GROW_BLOCK) || level.getRawBrightness(pos, 0) < 13 && ((MushroomBlockAccess) this).backport$mayPlaceOn(below, level, belowPos);
   }
}
