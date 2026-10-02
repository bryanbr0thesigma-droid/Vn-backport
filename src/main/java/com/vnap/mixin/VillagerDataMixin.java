package com.vnap.mixin;

import com.vnap.dialogue.ContextualDialogueController;
import com.vnap.entity.VillagerNewsData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Villager.class})
public abstract class VillagerDataMixin implements VillagerNewsData {
   @Unique
   private static final EntityDataAccessor<Boolean> VNAP_HAS_NOSE = SynchedEntityData.defineId(Villager.class, EntityDataSerializers.BOOLEAN);
   @Unique
   private static final EntityDataAccessor<Integer> VNAP_COSMETIC = SynchedEntityData.defineId(Villager.class, EntityDataSerializers.INT);
   @Unique
   private static final EntityDataAccessor<Integer> VNAP_SIGN_MESSAGE = SynchedEntityData.defineId(Villager.class, EntityDataSerializers.INT);
   @Unique
   private static final EntityDataAccessor<Integer> VNAP_SIGN_TYPE = SynchedEntityData.defineId(Villager.class, EntityDataSerializers.INT);
   @Unique
   private MobSpawnType vnap$spawnReason;
   @Unique
   private VillagerData vnap$originalVillagerData;
   @Unique
   private MerchantOffers vnap$originalVillagerOffers;

   @Inject(
      method = {"defineSynchedData"},
      at = {@At("TAIL")}
   )
   private void vnap$defineData(CallbackInfo ci) {
      SynchedEntityData data = ((Villager)(Object)this).getEntityData();
      data.define(VNAP_HAS_NOSE, true);
      data.define(VNAP_COSMETIC, 0);
      data.define(VNAP_SIGN_MESSAGE, -1);
      data.define(VNAP_SIGN_TYPE, -1);
   }

   @Inject(
      method = {"finalizeSpawn"},
      at = {@At("HEAD")}
   )
   private void vnap$captureSpawnReason(
      ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData spawnData, CompoundTag tag, CallbackInfoReturnable<SpawnGroupData> cir
   ) {
      this.vnap$spawnReason = reason;
   }

   @Override
   public MobSpawnType vnap$spawnReason() {
      return this.vnap$spawnReason;
   }

   @Inject(
      method = {"addAdditionalSaveData"},
      at = {@At("TAIL")}
   )
   private void vnap$saveData(CompoundTag output, CallbackInfo ci) {
      output.putBoolean("VillagerNewsHasNose", this.vnap$hasNose());
      output.putInt("VillagerNewsCosmetic", this.vnap$cosmetic());
      output.putInt("VillagerNewsSignMessage", this.vnap$signMessage());
      output.putInt("VillagerNewsSignType", this.vnap$signType());
      if (this.vnap$originalVillagerData != null && this.vnap$originalVillagerOffers != null) {
         VillagerData.CODEC.encodeStart(NbtOps.INSTANCE, this.vnap$originalVillagerData).result().ifPresent(tag -> output.put("VillagerNewsOriginalData", tag));
         output.put("VillagerNewsOriginalOffers", this.vnap$originalVillagerOffers.createTag());
      }
   }

   @Inject(
      method = {"readAdditionalSaveData"},
      at = {@At("TAIL")}
   )
   private void vnap$loadData(CompoundTag input, CallbackInfo ci) {
      this.vnap$setHasNose(!input.contains("VillagerNewsHasNose") || input.getBoolean("VillagerNewsHasNose"));
      this.vnap$setCosmetic(input.contains("VillagerNewsCosmetic") ? input.getInt("VillagerNewsCosmetic") : 0);
      int signMessage = input.contains("VillagerNewsSignMessage") ? input.getInt("VillagerNewsSignMessage") : -1;
      this.vnap$setSignMessage(signMessage);
      int equippedSign = ContextualDialogueController.signType(((Villager)(Object)this).getMainHandItem());
      this.vnap$setSignType(
         input.contains("VillagerNewsSignType") ? input.getInt("VillagerNewsSignType") : (equippedSign >= 0 ? equippedSign : (signMessage >= 0 ? 0 : -1))
      );
      if (equippedSign >= 0) {
         ((Villager)(Object)this).setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
      }

      this.vnap$originalVillagerData = null;
      this.vnap$originalVillagerOffers = null;
      if (input.contains("VillagerNewsOriginalData", Tag.TAG_COMPOUND) && input.contains("VillagerNewsOriginalOffers", Tag.TAG_COMPOUND)) {
         this.vnap$originalVillagerData = VillagerData.CODEC.parse(NbtOps.INSTANCE, input.get("VillagerNewsOriginalData")).result().orElse(null);
         if (this.vnap$originalVillagerData != null) {
            this.vnap$originalVillagerOffers = new MerchantOffers(input.getCompound("VillagerNewsOriginalOffers"));
         }
      }
   }

   @Redirect(
      method = {"customServerAiStep"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/npc/Villager;stopTrading()V"
      )
   )
   private void vnap$keepSpecialTradeOpen(Villager villager) {
      if (!ContextualDialogueController.isSpecialTrader(villager)) {
         villager.setTradingPlayer(null);
      }
   }

   @ModifyVariable(
      method = {"setVillagerData"},
      at = @At("HEAD"),
      argsOnly = true
   )
   private VillagerData vnap$preventSpecialProfession(VillagerData value) {
      Villager villager = (Villager)(Object)this;
      return ContextualDialogueController.isSpecialTrader(villager)
         ? value.setProfession(VillagerProfession.NONE).setLevel(1)
         : value;
   }

   @Override
   public boolean vnap$hasOriginalVillagerState() {
      return this.vnap$originalVillagerData != null && this.vnap$originalVillagerOffers != null;
   }

   @Override
   public void vnap$captureOriginalVillagerState() {
      if (!this.vnap$hasOriginalVillagerState()) {
         Villager villager = (Villager)(Object)this;
         this.vnap$originalVillagerData = villager.getVillagerData();
         this.vnap$originalVillagerOffers = new MerchantOffers(villager.getOffers().createTag());
      }
   }

   @Override
   public void vnap$restoreOriginalVillagerState() {
      if (this.vnap$hasOriginalVillagerState()) {
         Villager villager = (Villager)(Object)this;
         VillagerData originalData = this.vnap$originalVillagerData;
         MerchantOffers originalOffers = new MerchantOffers(this.vnap$originalVillagerOffers.createTag());
         this.vnap$originalVillagerData = null;
         this.vnap$originalVillagerOffers = null;
         villager.setVillagerData(originalData);
         villager.getOffers().clear();
         villager.getOffers().addAll(originalOffers);
      }
   }

   @Override
   public boolean vnap$hasNose() {
      return (Boolean)((Villager)(Object)this).getEntityData().get(VNAP_HAS_NOSE);
   }

   @Override
   public void vnap$setHasNose(boolean value) {
      ((Villager)(Object)this).getEntityData().set(VNAP_HAS_NOSE, value);
   }

   @Override
   public int vnap$cosmetic() {
      return (Integer)((Villager)(Object)this).getEntityData().get(VNAP_COSMETIC);
   }

   @Override
   public void vnap$setCosmetic(int value) {
      ((Villager)(Object)this).getEntityData().set(VNAP_COSMETIC, Math.max(0, Math.min(4, value)));
   }

   @Override
   public int vnap$signMessage() {
      return (Integer)((Villager)(Object)this).getEntityData().get(VNAP_SIGN_MESSAGE);
   }

   @Override
   public void vnap$setSignMessage(int value) {
      ((Villager)(Object)this).getEntityData().set(VNAP_SIGN_MESSAGE, Math.max(-1, Math.min(86, value)));
   }

   @Override
   public int vnap$signType() {
      return (Integer)((Villager)(Object)this).getEntityData().get(VNAP_SIGN_TYPE);
   }

   @Override
   public void vnap$setSignType(int value) {
      ((Villager)(Object)this).getEntityData().set(VNAP_SIGN_TYPE, Math.max(-1, Math.min(11, value)));
   }
}
