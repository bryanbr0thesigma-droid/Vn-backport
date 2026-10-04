package com.backport.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Zombie horses spawn like monsters (in the dark) now that they spawn naturally. */
@Mixin(SpawnPlacements.class)
public abstract class SpawnPlacementsMixin {
   @SuppressWarnings({"unchecked", "rawtypes"})
   @Inject(method = "checkSpawnRules", at = @At("HEAD"), cancellable = true)
   private static <T extends Entity> void backport$zombieHorse(EntityType<T> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
      if (type == EntityType.ZOMBIE_HORSE) {
         cir.setReturnValue(Monster.isDarkEnoughToSpawn(level, pos, random) && Mob.checkMobSpawnRules((EntityType) type, level, reason, pos, random));
      }
   }
}
