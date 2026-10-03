package com.backport;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class BackportEffects {
   public static final MobEffect BREATH_OF_THE_NAUTILUS = Registry.register(BuiltInRegistries.MOB_EFFECT, Backport.id("breath_of_the_nautilus"), new MobEffect(MobEffectCategory.BENEFICIAL, 0x6FC3C7) {
      public boolean isDurationEffectTick(int duration, int amplifier) {
         return true;
      }

      public void applyEffectTick(LivingEntity entity, int amplifier) {
         entity.setAirSupply(entity.getMaxAirSupply());
      }
   });

   private BackportEffects() {
   }

   public static void init() {
   }
}
