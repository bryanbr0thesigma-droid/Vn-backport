package com.backport.mixin;

import com.backport.BackportEntities;
import com.backport.entity.CopperGolem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CarvedPumpkinBlock.class)
public abstract class CarvedPumpkinBlockMixin {
   @Inject(method = "trySpawnGolem", at = @At("HEAD"), cancellable = true)
   private void backport$copperGolem(Level level, BlockPos pos, CallbackInfo ci) {
      BlockPos below = pos.below();
      BlockState state = level.getBlockState(below);
      WeatheringCopper.WeatherState weather = null;
      if (state.is(Blocks.COPPER_BLOCK) || state.is(Blocks.WAXED_COPPER_BLOCK)) {
         weather = WeatheringCopper.WeatherState.UNAFFECTED;
      } else if (state.is(Blocks.EXPOSED_COPPER) || state.is(Blocks.WAXED_EXPOSED_COPPER)) {
         weather = WeatheringCopper.WeatherState.EXPOSED;
      } else if (state.is(Blocks.WEATHERED_COPPER) || state.is(Blocks.WAXED_WEATHERED_COPPER)) {
         weather = WeatheringCopper.WeatherState.WEATHERED;
      } else if (state.is(Blocks.OXIDIZED_COPPER) || state.is(Blocks.WAXED_OXIDIZED_COPPER)) {
         weather = WeatheringCopper.WeatherState.OXIDIZED;
      }

      if (weather != null && level instanceof ServerLevel serverLevel) {
         boolean waxed = state.is(Blocks.WAXED_COPPER_BLOCK) || state.is(Blocks.WAXED_EXPOSED_COPPER) || state.is(Blocks.WAXED_WEATHERED_COPPER) || state.is(Blocks.WAXED_OXIDIZED_COPPER);
         CopperGolem golem = BackportEntities.COPPER_GOLEM.create(serverLevel);
         if (golem != null) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            level.setBlock(below, Blocks.AIR.defaultBlockState(), 2);
            golem.moveTo(below.getX() + 0.5, below.getY(), below.getZ() + 0.5, 0.0F, 0.0F);
            golem.spawn(weather);
            if (waxed) {
               golem.setWaxed();
            }

            serverLevel.addFreshEntity(golem);
            level.blockUpdated(pos, Blocks.AIR);
            level.blockUpdated(below, Blocks.AIR);
            ci.cancel();
         }
      }
   }
}
