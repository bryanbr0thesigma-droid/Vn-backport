package com.backport.mixin;

import com.backport.BackportItems;
import com.backport.MaceItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMaceMixin {
   @ModifyVariable(method = "hurt", at = @At("HEAD"), argsOnly = true, ordinal = 0)
   private float backport$smashDamage(float amount, DamageSource source) {
      if (source.getDirectEntity() instanceof LivingEntity attacker && attacker.getMainHandItem().is(BackportItems.MACE) && MaceItem.canSmashAttack(attacker)) {
         float total = com.backport.MaceEnchantments.breach(amount + MaceItem.smashBonus(attacker), (LivingEntity) (Object) this, com.backport.MaceEnchantments.level(com.backport.MaceEnchantments.BREACH, attacker));
         if (attacker instanceof net.minecraft.server.level.ServerPlayer sp) {
            com.backport.advancement.BackportEvents.fire(sp, "overoverkill", total);
         }
         return total;
      }
      if (source.getDirectEntity() instanceof LivingEntity attacker && attacker.getMainHandItem().is(BackportItems.MACE)) {
         return com.backport.MaceEnchantments.breach(amount, (LivingEntity) (Object) this, com.backport.MaceEnchantments.level(com.backport.MaceEnchantments.BREACH, attacker));
      }

      return amount;
   }
}
