package com.backport.mixin;

import com.backport.BackportItems;
import com.backport.ai.PiglinSpearBehavior;
import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public final class PiglinSpearMixin {
   private PiglinSpearMixin() {
   }

   /** Piglins fight with a spear when they hold one. */
   @Mixin(PiglinAi.class)
   public abstract static class Ai {
      @Inject(method = "initFightActivity", at = @At("TAIL"))
      private static void backport$spearBehavior(Piglin piglin, Brain<Piglin> brain, CallbackInfo ci) {
         brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 5, ImmutableList.of(new PiglinSpearBehavior()), MemoryModuleType.ATTACK_TARGET);
      }
   }

   /** One in ten of the sword piglins spawn with a golden spear instead. */
   @Mixin(Piglin.class)
   public abstract static class Gear {
      @ModifyReturnValue(method = "createSpawnWeapon", at = @At("RETURN"))
      private ItemStack backport$spearChance(ItemStack original) {
         Piglin self = (Piglin) (Object) this;
         return original.is(Items.GOLDEN_SWORD) && self.getRandom().nextInt(10) == 0 ? new ItemStack(BackportItems.GOLDEN_SPEAR) : original;
      }
   }
}
