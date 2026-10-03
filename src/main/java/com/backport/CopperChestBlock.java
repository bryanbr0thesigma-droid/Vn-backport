package com.backport;

import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class CopperChestBlock extends ChestBlock {
   private final WeatheringCopper.WeatherState weatherState;

   public CopperChestBlock(WeatheringCopper.WeatherState weatherState, BlockBehaviour.Properties properties) {
      super(properties, () -> CopperChestBlockEntity.TYPE);
      this.weatherState = weatherState;
   }

   public WeatheringCopper.WeatherState getWeatherState() {
      return this.weatherState;
   }
}
