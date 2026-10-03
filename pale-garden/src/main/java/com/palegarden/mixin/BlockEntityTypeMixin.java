package com.palegarden.mixin;

import com.palegarden.PaleBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityType.class)
public abstract class BlockEntityTypeMixin {
   @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
   private void palegarden$validSigns(BlockState state, CallbackInfoReturnable<Boolean> cir) {
      Object self = this;
      Block block = state.getBlock();
      if (self == BlockEntityType.SIGN) {
         if (block == PaleBlocks.PALE_OAK_SIGN || block == PaleBlocks.PALE_OAK_WALL_SIGN) {
            cir.setReturnValue(true);
         }
      } else if (self == BlockEntityType.HANGING_SIGN) {
         if (block == PaleBlocks.PALE_OAK_HANGING_SIGN || block == PaleBlocks.PALE_OAK_WALL_HANGING_SIGN) {
            cir.setReturnValue(true);
         }
      }
   }
}
