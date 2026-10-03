package com.backport;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

/** Shared helpers for content backported from newer Minecraft versions (namespace "backport"). */
public final class Backport {
   public static final String ID = "backport";
   private static final List<Item> TAB_ITEMS = new ArrayList<>();

   private Backport() {
   }

   public static ResourceLocation id(String path) {
      return new ResourceLocation(ID, path);
   }

   public static <T extends Block> T block(String name, T block) {
      Registry.register(BuiltInRegistries.BLOCK, id(name), block);
      item(name, new BlockItem(block, new FabricItemSettings()));
      return block;
   }

   public static <T extends Block> T blockNoItem(String name, T block) {
      return Registry.register(BuiltInRegistries.BLOCK, id(name), block);
   }

   public static <T extends Item> T item(String name, T item) {
      Registry.register(BuiltInRegistries.ITEM, id(name), item);
      TAB_ITEMS.add(item);
      return item;
   }

   public static void init() {
      SimpleBlocks.init();
      Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ResourceKey.create(Registries.CREATIVE_MODE_TAB, id("main")), FabricItemGroup.builder()
         .title(Component.translatable("itemGroup.backport"))
         .icon(() -> new ItemStack(Items.TUFF))
         .displayItems((params, output) -> TAB_ITEMS.forEach(output::accept))
         .build());
   }
}
