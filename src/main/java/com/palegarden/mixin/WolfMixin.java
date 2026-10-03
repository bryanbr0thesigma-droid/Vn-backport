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

   @Unique
   private static final EntityDataAccessor<net.minecraft.world.item.ItemStack> PALEGARDEN_ARMOR = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.ITEM_STACK);

   @Inject(method = "defineSynchedData", at = @At("TAIL"))
   private void palegarden$define(CallbackInfo ci) {
      ((Wolf)(Object)this).getEntityData().define(PALEGARDEN_VARIANT, -1);
      ((Wolf)(Object)this).getEntityData().define(PALEGARDEN_ARMOR, net.minecraft.world.item.ItemStack.EMPTY);
   }

   @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
   private void palegarden$save(CompoundTag tag, CallbackInfo ci) {
      tag.putInt("PaleGardenVariant", this.palegarden$getVariant());
      tag.put("BodyArmorItem", this.palegarden$getArmor().save(new CompoundTag()));
   }

   @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
   private void palegarden$load(CompoundTag tag, CallbackInfo ci) {
      if (tag.contains("BodyArmorItem")) {
         this.palegarden$setArmor(net.minecraft.world.item.ItemStack.of(tag.getCompound("BodyArmorItem")));
      }

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

   public net.minecraft.world.item.ItemStack palegarden$getArmor() {
      return ((Wolf)(Object)this).getEntityData().get(PALEGARDEN_ARMOR);
   }

   public void palegarden$setArmor(net.minecraft.world.item.ItemStack stack) {
      ((Wolf)(Object)this).getEntityData().set(PALEGARDEN_ARMOR, stack);
   }

   @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
   private void palegarden$armor(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, CallbackInfoReturnable<net.minecraft.world.InteractionResult> cir) {
      Wolf wolf = (Wolf)(Object)this;
      net.minecraft.world.item.ItemStack stack = player.getItemInHand(hand);
      if (!wolf.isTame() || !wolf.isOwnedBy(player) || wolf.isBaby()) {
         return;
      }

      if (stack.is(com.backport.BackportItems.WOLF_ARMOR) && this.palegarden$getArmor().isEmpty()) {
         if (!wolf.level().isClientSide) {
            this.palegarden$setArmor(stack.copyWithCount(1));
            wolf.playSound(com.backport.BackportSounds.ITEM_ARMOR_EQUIP_WOLF, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) {
               stack.shrink(1);
            }
         }

         cir.setReturnValue(net.minecraft.world.InteractionResult.sidedSuccess(wolf.level().isClientSide));
      } else if (stack.is(net.minecraft.world.item.Items.SHEARS) && !this.palegarden$getArmor().isEmpty()) {
         if (!wolf.level().isClientSide) {
            wolf.spawnAtLocation(this.palegarden$getArmor().copy());
            this.palegarden$setArmor(net.minecraft.world.item.ItemStack.EMPTY);
            wolf.playSound(com.backport.BackportSounds.ITEM_ARMOR_UNEQUIP_WOLF, 1.0F, 1.0F);
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
         }

         cir.setReturnValue(net.minecraft.world.InteractionResult.sidedSuccess(wolf.level().isClientSide));
      }
   }

   public int palegarden$getVariant() {
      return ((Wolf)(Object)this).getEntityData().get(PALEGARDEN_VARIANT);
   }

   public void palegarden$setVariant(int variant) {
      ((Wolf)(Object)this).getEntityData().set(PALEGARDEN_VARIANT, variant);
   }
}
