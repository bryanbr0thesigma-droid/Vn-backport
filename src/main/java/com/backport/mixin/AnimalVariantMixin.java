package com.backport.mixin;

import com.backport.variant.VariantHolder;
import com.backport.variant.Variants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Animal.class)
public abstract class AnimalVariantMixin implements VariantHolder {
   @Unique
   private static final EntityDataAccessor<Integer> BACKPORT_VARIANT = SynchedEntityData.defineId(Animal.class, EntityDataSerializers.INT);

   @Inject(method = "<init>", at = @At("TAIL"))
   private void backport$define(EntityType<? extends Animal> type, Level level, CallbackInfo ci) {
      if (Variants.supports(type)) {
         ((Animal) (Object) this).getEntityData().define(BACKPORT_VARIANT, 0);
      }
   }

   @Override
   public int backport$getVariant() {
      Animal self = (Animal) (Object) this;
      return Variants.supports(self.getType()) ? self.getEntityData().get(BACKPORT_VARIANT) : 0;
   }

   @Override
   public void backport$setVariant(int variant) {
      Animal self = (Animal) (Object) this;
      if (Variants.supports(self.getType())) {
         self.getEntityData().set(BACKPORT_VARIANT, variant);
      }
   }

   @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
   private void backport$save(CompoundTag tag, CallbackInfo ci) {
      if (Variants.supports(((Animal) (Object) this).getType())) {
         tag.putInt("BackportVariant", this.backport$getVariant());
      }
   }

   @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
   private void backport$load(CompoundTag tag, CallbackInfo ci) {
      if (Variants.supports(((Animal) (Object) this).getType()) && tag.contains("BackportVariant")) {
         this.backport$setVariant(tag.getInt("BackportVariant"));
      }
   }

   @Inject(method = "finalizeSpawnChildFromBreeding", at = @At("HEAD"))
   private void backport$inherit(ServerLevel level, Animal other, AgeableMob child, CallbackInfo ci) {
      Animal self = (Animal) (Object) this;
      if (child instanceof VariantHolder h && Variants.supports(self.getType()) && Variants.supports(child.getType())) {
         int a = this.backport$getVariant();
         int b = other instanceof VariantHolder o ? o.backport$getVariant() : a;
         h.backport$setVariant(self.getRandom().nextBoolean() ? a : b);
      }
   }
}
