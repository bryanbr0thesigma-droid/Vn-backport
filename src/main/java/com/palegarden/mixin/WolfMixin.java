package com.palegarden.mixin;

import com.palegarden.WolfVariants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Wolf.class)
public abstract class WolfMixin implements WolfVariants.Holder2 {
   @Unique
   private static final EntityDataAccessor<Integer> PALEGARDEN_VARIANT = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.INT);

   @Inject(method = "defineSynchedData", at = @At("TAIL"))
   private void palegarden$define(CallbackInfo ci) {
      ((Wolf)(Object)this).getEntityData().define(PALEGARDEN_VARIANT, -1);
   }

   @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
   private void palegarden$save(CompoundTag tag, CallbackInfo ci) {
      tag.putInt("PaleGardenVariant", this.palegarden$getVariant());
   }

   @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
   private void palegarden$load(CompoundTag tag, CallbackInfo ci) {
      if (tag.contains("PaleGardenVariant")) {
         this.palegarden$setVariant(tag.getInt("PaleGardenVariant"));
      }
   }

   @Inject(method = "getBreedOffspring(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/AgeableMob;)Lnet/minecraft/world/entity/animal/Wolf;", at = @At("RETURN"))
   private void palegarden$breed(ServerLevel level, AgeableMob partner, CallbackInfoReturnable<Wolf> cir) {
      Wolf baby = cir.getReturnValue();
      if (baby != null) {
         int mine = this.palegarden$getVariant();
         int theirs = partner instanceof Wolf other ? WolfVariants.of(other) : mine;
         ((WolfVariants.Holder2)baby).palegarden$setVariant(level.random.nextBoolean() ? mine : theirs);
      }
   }

   public int palegarden$getVariant() {
      return ((Wolf)(Object)this).getEntityData().get(PALEGARDEN_VARIANT);
   }

   public void palegarden$setVariant(int variant) {
      ((Wolf)(Object)this).getEntityData().set(PALEGARDEN_VARIANT, variant);
   }
}
