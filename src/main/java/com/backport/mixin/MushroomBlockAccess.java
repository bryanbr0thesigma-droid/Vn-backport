package com.backport.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.world.level.block.BushBlock;

@Mixin(BushBlock.class)
public interface MushroomBlockAccess {
   @Invoker("mayPlaceOn")
   boolean backport$mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos);
}
