package com.backport.mixin;

import com.backport.WolfArmor;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityWolfArmorMixin {
   @ModifyVariable(method = "hurt", at = @At("HEAD"), argsOnly = true, ordinal = 0)
   private float backport$wolfArmor(float amount, DamageSource source) {
      if ((Object)this instanceof Wolf wolf && !source.is(DamageTypeTags.BYPASSES_ARMOR) && !wolf.level().isClientSide && !wolf.isInvulnerableTo(source)) {
         return WolfArmor.absorb(wolf, amount);
      }

      return amount;
   }
}
