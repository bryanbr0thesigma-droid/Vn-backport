package com.backport.loot;

import com.backport.Backport;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public final class BackportLootFunctions {
   public static LootItemFunctionType SET_ENCHANTMENTS;
   public static LootItemFunctionType SET_OMINOUS_AMPLIFIER;

   private BackportLootFunctions() {
   }

   public static void init() {
      SET_ENCHANTMENTS = Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, Backport.id("set_enchantments"), new LootItemFunctionType(new SetEnchantments.Ser()));
      SET_OMINOUS_AMPLIFIER = Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, Backport.id("set_ominous_bottle_amplifier"), new LootItemFunctionType(new Amplifier.Ser()));
   }

   static final class SetEnchantments extends LootItemConditionalFunction {
      final Map<ResourceLocation, Integer> map;

      SetEnchantments(LootItemCondition[] c, Map<ResourceLocation, Integer> map) {
         super(c);
         this.map = map;
      }

      public LootItemFunctionType getType() {
         return SET_ENCHANTMENTS;
      }

      protected ItemStack run(ItemStack stack, LootContext ctx) {
         for (Map.Entry<ResourceLocation, Integer> e : this.map.entrySet()) {
            Enchantment en = BuiltInRegistries.ENCHANTMENT.get(e.getKey());
            if (en == null) continue;
            if (stack.is(Items.BOOK)) {
               ItemStack book = new ItemStack(Items.ENCHANTED_BOOK, stack.getCount());
               EnchantedBookItem.addEnchantment(book, new EnchantmentInstance(en, e.getValue()));
               stack = book;
            } else if (stack.is(Items.ENCHANTED_BOOK)) {
               EnchantedBookItem.addEnchantment(stack, new EnchantmentInstance(en, e.getValue()));
            } else {
               stack.enchant(en, e.getValue());
            }
         }
         return stack;
      }

      static final class Ser extends LootItemConditionalFunction.Serializer<SetEnchantments> {
         public void serialize(JsonObject o, SetEnchantments f, JsonSerializationContext c) {
            super.serialize(o, f, c);
            JsonObject m = new JsonObject();
            f.map.forEach((k, v) -> m.addProperty(k.toString(), v));
            o.add("enchantments", m);
         }

         public SetEnchantments deserialize(JsonObject o, JsonDeserializationContext c, LootItemCondition[] conds) {
            Map<ResourceLocation, Integer> map = new LinkedHashMap<>();
            for (Map.Entry<String, com.google.gson.JsonElement> e : GsonHelper.getAsJsonObject(o, "enchantments").entrySet()) {
               map.put(new ResourceLocation(e.getKey()), e.getValue().getAsInt());
            }
            return new SetEnchantments(conds, map);
         }
      }
   }

   static final class Amplifier extends LootItemConditionalFunction {
      final NumberProvider amplifier;

      Amplifier(LootItemCondition[] c, NumberProvider amplifier) {
         super(c);
         this.amplifier = amplifier;
      }

      public LootItemFunctionType getType() {
         return SET_OMINOUS_AMPLIFIER;
      }

      protected ItemStack run(ItemStack stack, LootContext ctx) {
         stack.getOrCreateTag().putInt("OminousBottleAmplifier", this.amplifier.getInt(ctx));
         return stack;
      }

      static final class Ser extends LootItemConditionalFunction.Serializer<Amplifier> {
         public void serialize(JsonObject o, Amplifier f, JsonSerializationContext c) {
            super.serialize(o, f, c);
            o.add("amplifier", c.serialize(f.amplifier));
         }

         public Amplifier deserialize(JsonObject o, JsonDeserializationContext c, LootItemCondition[] conds) {
            return new Amplifier(conds, GsonHelper.getAsObject(o, "amplifier", c, NumberProvider.class));
         }
      }
   }
}
