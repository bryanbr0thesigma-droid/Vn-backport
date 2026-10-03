package com.backport.mixin;

import com.backport.variant.VariantHolder;
import com.backport.variant.Variants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobVariantSpawnMixin {
   @Inject(method = "finalizeSpawn", at = @At("RETURN"))
   private void backport$variantByBiome(ServerLevelAccessor level, DifficultyInstance diff, MobSpawnType reason, SpawnGroupData data, CompoundTag tag, CallbackInfoReturnable<SpawnGroupData> cir) {
      Mob self = (Mob) (Object) this;
      if (Variants.supports(self.getType()) && self instanceof VariantHolder h) {
         h.backport$setVariant(Variants.pick(level.getBiome(self.blockPosition())));
      }
   }
}
