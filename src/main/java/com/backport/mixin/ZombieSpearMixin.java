package com.backport.mixin;

import com.backport.BackportItems;
import com.backport.ai.SpearUseGoal;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Zombies, husks, drowned and zombified piglins can carry spears and charge with them. */
@Mixin(Zombie.class)
public abstract class ZombieSpearMixin {
   @Inject(method = "registerGoals", at = @At("TAIL"))
   private void backport$spearGoal(CallbackInfo ci) {
      Zombie self = (Zombie) (Object) this;
      ((MobGoalAccessor) self).backport$goalSelector().addGoal(1, new SpearUseGoal<>(self, 1.0, 1.0, 10.0F, 2.0F));
   }

   @Inject(method = "populateDefaultEquipmentSlots", at = @At("TAIL"))
   private void backport$spearEquipment(RandomSource random, DifficultyInstance difficulty, CallbackInfo ci) {
      Zombie self = (Zombie) (Object) this;
      ItemStack held = self.getItemBySlot(EquipmentSlot.MAINHAND);
      if (self instanceof ZombifiedPiglin) {
         if (held.is(Items.GOLDEN_SWORD) && random.nextInt(20) == 0) {
            self.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(BackportItems.GOLDEN_SPEAR));
         }
      } else if (held.is(Items.IRON_SWORD) && random.nextBoolean()) {
         self.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(BackportItems.IRON_SPEAR));
      }
   }
}
