package com.backport.entity;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.NonNullList;

/** 26.x mannequin: an inert, dressable player-shaped entity with a configurable skin, pose and label. */
public class Mannequin extends LivingEntity {
   private static final EntityDataAccessor<String> PROFILE_NAME = SynchedEntityData.defineId(Mannequin.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<Optional<UUID>> PROFILE_ID = SynchedEntityData.defineId(Mannequin.class, EntityDataSerializers.OPTIONAL_UUID);
   private static final EntityDataAccessor<Boolean> IMMOVABLE = SynchedEntityData.defineId(Mannequin.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Byte> LAYERS = SynchedEntityData.defineId(Mannequin.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Boolean> LEFT_HANDED = SynchedEntityData.defineId(Mannequin.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Optional<Component>> DESCRIPTION = SynchedEntityData.defineId(Mannequin.class, EntityDataSerializers.OPTIONAL_COMPONENT);
   public static final String[] LAYER_NAMES = {"cape", "jacket", "left_sleeve", "right_sleeve", "left_pants_leg", "right_pants_leg", "hat"};
   private static final byte ALL_LAYERS = 0x7F;
   private static final Component DEFAULT_DESCRIPTION = Component.translatable("entity.backport.mannequin.label");
   private final NonNullList<ItemStack> hands = NonNullList.withSize(2, ItemStack.EMPTY);
   private final NonNullList<ItemStack> armor = NonNullList.withSize(4, ItemStack.EMPTY);
   private Component description = DEFAULT_DESCRIPTION;
   private boolean hideDescription;

   public Mannequin(EntityType<? extends Mannequin> type, Level level) {
      super(type, level);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return LivingEntity.createLivingAttributes();
   }

   @Override
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(PROFILE_NAME, "");
      this.entityData.define(PROFILE_ID, Optional.empty());
      this.entityData.define(IMMOVABLE, false);
      this.entityData.define(LAYERS, ALL_LAYERS);
      this.entityData.define(LEFT_HANDED, false);
      this.entityData.define(DESCRIPTION, Optional.of(DEFAULT_DESCRIPTION));
   }

   public String profileName() {
      return this.entityData.get(PROFILE_NAME);
   }

   public Optional<UUID> profileId() {
      return this.entityData.get(PROFILE_ID);
   }

   /** True when the given layer (see LAYER_NAMES) is shown. */
   public boolean layerVisible(int index) {
      return (this.entityData.get(LAYERS) & (1 << index)) != 0;
   }

   public Component descriptionText() {
      return this.entityData.get(DESCRIPTION).orElse(null);
   }

   @Override
   protected boolean isImmobile() {
      return this.entityData.get(IMMOVABLE) || super.isImmobile();
   }

   @Override
   public boolean isEffectiveAi() {
      return !this.entityData.get(IMMOVABLE) && super.isEffectiveAi();
   }

   @Override
   public HumanoidArm getMainArm() {
      return this.entityData.get(LEFT_HANDED) ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
   }

   @Override
   public Iterable<ItemStack> getHandSlots() {
      return this.hands;
   }

   @Override
   public Iterable<ItemStack> getArmorSlots() {
      return this.armor;
   }

   @Override
   public ItemStack getItemBySlot(EquipmentSlot slot) {
      return switch (slot.getType()) {
         case HAND -> this.hands.get(slot.getIndex());
         case ARMOR -> this.armor.get(slot.getIndex());
      };
   }

   @Override
   public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
      this.verifyEquippedItem(stack);
      switch (slot.getType()) {
         case HAND -> this.onEquipItem(slot, this.hands.set(slot.getIndex(), stack), stack);
         case ARMOR -> this.onEquipItem(slot, this.armor.set(slot.getIndex(), stack), stack);
      }
   }

   private void updateDescription() {
      this.entityData.set(DESCRIPTION, this.hideDescription ? Optional.empty() : Optional.of(this.description));
   }

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      CompoundTag profile = new CompoundTag();
      if (!this.profileName().isEmpty()) {
         profile.putString("name", this.profileName());
      }
      this.profileId().ifPresent(id -> profile.put("id", NbtUtils.createUUID(id)));
      tag.put("profile", profile);
      ListTag hidden = new ListTag();
      for (int i = 0; i < LAYER_NAMES.length; i++) {
         if (!this.layerVisible(i)) {
            hidden.add(StringTag.valueOf(LAYER_NAMES[i]));
         }
      }
      tag.put("hidden_layers", hidden);
      tag.putString("main_hand", this.getMainArm() == HumanoidArm.LEFT ? "left" : "right");
      tag.putString("pose", this.getPose().name().toLowerCase(java.util.Locale.ROOT));
      tag.putBoolean("immovable", this.entityData.get(IMMOVABLE));
      if (this.hideDescription) {
         tag.putBoolean("hide_description", true);
      } else if (!this.description.equals(DEFAULT_DESCRIPTION)) {
         tag.putString("description", Component.Serializer.toJson(this.description));
      }
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      if (tag.contains("profile")) {
         if (tag.get("profile") instanceof net.minecraft.nbt.StringTag s) {
            this.entityData.set(PROFILE_NAME, s.getAsString());
         } else {
            CompoundTag profile = tag.getCompound("profile");
            this.entityData.set(PROFILE_NAME, profile.getString("name"));
            this.entityData.set(PROFILE_ID, profile.hasUUID("id") ? Optional.of(profile.getUUID("id")) : Optional.empty());
         }
      }
      byte layers = ALL_LAYERS;
      for (net.minecraft.nbt.Tag t : tag.getList("hidden_layers", 8)) {
         for (int i = 0; i < LAYER_NAMES.length; i++) {
            if (LAYER_NAMES[i].equals(t.getAsString())) {
               layers &= (byte) ~(1 << i);
            }
         }
      }
      this.entityData.set(LAYERS, layers);
      this.entityData.set(LEFT_HANDED, "left".equals(tag.getString("main_hand")));
      if (tag.contains("pose")) {
         try {
            Pose pose = Pose.valueOf(tag.getString("pose").toUpperCase(java.util.Locale.ROOT));
            if (pose == Pose.STANDING || pose == Pose.CROUCHING || pose == Pose.SWIMMING || pose == Pose.FALL_FLYING || pose == Pose.SLEEPING) {
               this.setPose(pose);
            }
         } catch (IllegalArgumentException ignored) {
         }
      }
      this.entityData.set(IMMOVABLE, tag.getBoolean("immovable"));
      this.hideDescription = tag.getBoolean("hide_description");
      if (tag.contains("description")) {
         Component c = Component.Serializer.fromJson(tag.getString("description"));
         this.description = c == null ? DEFAULT_DESCRIPTION : c;
      }
      this.updateDescription();
   }

   @Override
   public boolean isPushable() {
      return !this.entityData.get(IMMOVABLE);
   }

   @Override
   public boolean attackable() {
      return true;
   }
}
