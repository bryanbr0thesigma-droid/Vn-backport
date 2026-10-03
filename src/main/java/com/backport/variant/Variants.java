package com.backport.variant;

import com.backport.Backport;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;

public final class Variants {
   public static final int TEMPERATE = 0;
   public static final int COLD = 1;
   public static final int WARM = 2;

   public static final TagKey<Biome> COLD_BIOMES = TagKey.create(Registries.BIOME, Backport.id("spawns_cold_variant_farm_animals"));
   public static final TagKey<Biome> WARM_BIOMES = TagKey.create(Registries.BIOME, Backport.id("spawns_warm_variant_farm_animals"));

   public static final Item BLUE_EGG = Backport.item("blue_egg", new VariantEggItem(new FabricItemSettings().maxCount(16), COLD));
   public static final Item BROWN_EGG = Backport.item("brown_egg", new VariantEggItem(new FabricItemSettings().maxCount(16), WARM));
   public static final EntityType<ThrownVariantEgg> EGG_ENTITY = Registry.register(BuiltInRegistries.ENTITY_TYPE, Backport.id("variant_egg"),
      FabricEntityTypeBuilder.<ThrownVariantEgg>create(MobCategory.MISC, ThrownVariantEgg::new).dimensions(EntityDimensions.fixed(0.25F, 0.25F)).trackRangeBlocks(4).trackedUpdateRate(10).build());

   private static final Object[][] PAINTINGS = {
      {"baroque", 2, 2}, {"humble", 2, 2}, {"meditative", 1, 1}, {"prairie_ride", 1, 2}, {"unpacked", 4, 4}, {"backyard", 3, 4}, {"bouquet", 3, 3},
      {"cavebird", 3, 3}, {"changing", 4, 2}, {"cotan", 3, 3}, {"endboss", 3, 3}, {"fern", 3, 3}, {"finding", 4, 2}, {"lowmist", 4, 2}, {"orb", 4, 4},
      {"owlemons", 3, 3}, {"passage", 4, 2}, {"pond", 3, 4}, {"sunflowers", 3, 3}, {"tides", 3, 3}, {"dennis", 3, 3}
   };

   private Variants() {
   }

   public static boolean supports(EntityType<?> type) {
      return type == EntityType.PIG || type == EntityType.COW || type == EntityType.CHICKEN;
   }

   public static int of(Entity e) {
      return supports(e.getType()) && e instanceof VariantHolder h ? h.backport$getVariant() : TEMPERATE;
   }

   public static int pick(Holder<Biome> biome) {
      if (biome.is(COLD_BIOMES)) {
         return COLD;
      }
      return biome.is(WARM_BIOMES) ? WARM : TEMPERATE;
   }

   public static void init() {
      for (Object[] p : PAINTINGS) {
         Registry.register(BuiltInRegistries.PAINTING_VARIANT, new ResourceLocation("minecraft", (String) p[0]), new PaintingVariant((int) p[1] * 16, (int) p[2] * 16));
      }
   }
}
