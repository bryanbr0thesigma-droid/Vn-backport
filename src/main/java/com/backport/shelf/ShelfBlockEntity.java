package com.backport.shelf;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

public class ShelfBlockEntity extends BlockEntity implements Container {
   public static BlockEntityType<ShelfBlockEntity> TYPE;
   private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);

   public static void register(Block... blocks) {
      TYPE = net.minecraft.core.Registry.register(net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE, com.backport.Backport.id("shelf"),
         FabricBlockEntityTypeBuilder.create(ShelfBlockEntity::new, blocks).build());
   }

   public ShelfBlockEntity(BlockPos pos, BlockState state) {
      super(TYPE, pos, state);
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      ContainerHelper.saveAllItems(tag, this.items, true);
   }

   public void load(CompoundTag tag) {
      super.load(tag);
      this.items.clear();
      for (int i = 0; i < 3; i++) this.items.set(i, ItemStack.EMPTY);
      ContainerHelper.loadAllItems(tag, this.items);
   }

   @Nullable
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public CompoundTag getUpdateTag() {
      CompoundTag tag = new CompoundTag();
      ContainerHelper.saveAllItems(tag, this.items, true);
      return tag;
   }

   public NonNullList<ItemStack> getItems() {
      return this.items;
   }

   public int getContainerSize() {
      return 3;
   }

   public boolean isEmpty() {
      return this.items.stream().allMatch(ItemStack::isEmpty);
   }

   public ItemStack getItem(int slot) {
      return this.items.get(slot);
   }

   public ItemStack removeItem(int slot, int amount) {
      ItemStack s = ContainerHelper.removeItem(this.items, slot, amount);
      if (!s.isEmpty()) this.setChanged();
      return s;
   }

   public ItemStack removeItemNoUpdate(int slot) {
      return ContainerHelper.takeItem(this.items, slot);
   }

   public void setItem(int slot, ItemStack stack) {
      this.items.set(slot, stack);
      this.setChanged();
   }

   public void setItemNoUpdate(int slot, ItemStack stack) {
      this.items.set(slot, stack);
   }

   public int getMaxStackSize() {
      return 1;
   }

   public boolean stillValid(Player player) {
      return Container.stillValidBlockEntity(this, player);
   }

   public void clearContent() {
      this.items.clear();
   }

   public ItemStack swapItemNoUpdate(int slot, ItemStack held) {
      ItemStack retrieved = this.removeItemNoUpdate(slot);
      this.setItemNoUpdate(slot, held.copyWithCount(Math.min(1, held.getCount())));
      return retrieved;
   }

   public void setChanged() {
      super.setChanged();
      if (this.level != null) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }
   }
}
