package com.backport;

import com.palegarden.PaleBlocks;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;

/** 26.x villager and wandering trader trades for the content this mod adds. */
public final class Trades {
   private Trades() {
   }

   private static VillagerTrades.ItemListing sell(Item item, int count, int emeralds, int maxUses) {
      return (entity, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, emeralds), new ItemStack(item, count), maxUses, 1, 0.05F);
   }

   private static Item item(String ns, String path) {
      return BuiltInRegistries.ITEM.get(new ResourceLocation(ns, path));
   }

   public static void init() {
      TradeOfferHelper.registerWanderingTraderOffers(1, trades -> {
         trades.add(sell(item("backport", "shelf_mushroom"), 3, 1, 12));
         trades.add(sell(item("backport", "firefly_bush"), 1, 3, 12));
         trades.add(sell(item("backport", "sulfur_spike"), 2, 1, 5));
         trades.add(sell(item("pale_garden", "pale_oak_sapling"), 1, 5, 8));
         trades.add(sell(item("pale_garden", "pale_hanging_moss"), 3, 1, 12));
         trades.add(sell(item("backport", "golden_dandelion"), 1, 2, 12));
         trades.add(sell(BackportItems.NAUTILUS_SHELL, 1, 5, 5));
         trades.add(sell(item("pale_garden", "pale_moss_block"), 2, 1, 5));
      });
      TradeOfferHelper.registerWanderingTraderOffers(2, trades -> trades.add(sell(item("pale_garden", "pale_oak_log"), 8, 1, 12)));
      TradeOfferHelper.registerVillagerOffers(VillagerProfession.CARTOGRAPHER, 3, trades -> trades.add(new VillagerTrades.TreasureMapForEmeralds(12,
         TagKey.create(Registries.STRUCTURE, new ResourceLocation("backport", "on_buried_trial_chambers_maps")), "filled_map.trial_chambers", MapDecoration.Type.TARGET_X, 12, 10)));
   }
}
