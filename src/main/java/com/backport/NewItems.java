package com.backport;

import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.core.RegistryAccess;

/** Items from the 26.x drops that were missing: trims, sherds, banner patterns, horse armor, discs and bundles. */
public final class NewItems {
   public static final String[] COLORS = {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"};
   public static final Map<DyeColor, Item> BUNDLES = new LinkedHashMap<>();
   public static Item BUNDLE;

   public static final Item BOLT_TEMPLATE = Backport.item("bolt_armor_trim_smithing_template", SmithingTemplateItem.createArmorTrimTemplate(ResourceKey.create(Registries.TRIM_PATTERN, Backport.id("bolt"))));
   public static final Item FLOW_TEMPLATE = Backport.item("flow_armor_trim_smithing_template", SmithingTemplateItem.createArmorTrimTemplate(ResourceKey.create(Registries.TRIM_PATTERN, Backport.id("flow"))));
   public static final Item FLOW_SHERD = Backport.item("flow_pottery_sherd", new Item(new FabricItemSettings()));
   public static final Item GUSTER_SHERD = Backport.item("guster_pottery_sherd", new Item(new FabricItemSettings()));
   public static final Item SCRAPE_SHERD = Backport.item("scrape_pottery_sherd", new Item(new FabricItemSettings()));
   public static final BannerPattern FLOW_PATTERN = Registry.register(BuiltInRegistries.BANNER_PATTERN, Backport.id("flow"), new BannerPattern("flw"));
   public static final BannerPattern GUSTER_PATTERN = Registry.register(BuiltInRegistries.BANNER_PATTERN, Backport.id("guster"), new BannerPattern("gus"));
   public static final Item FLOW_BANNER_PATTERN = Backport.item("flow_banner_pattern", new BannerPatternItem(TagKey.create(Registries.BANNER_PATTERN, Backport.id("pattern_item/flow")), new FabricItemSettings().maxCount(1).rarity(Rarity.RARE)));
   public static final Item GUSTER_BANNER_PATTERN = Backport.item("guster_banner_pattern", new BannerPatternItem(TagKey.create(Registries.BANNER_PATTERN, Backport.id("pattern_item/guster")), new FabricItemSettings().maxCount(1).rarity(Rarity.RARE)));
   public static final Item COPPER_HORSE_ARMOR = Backport.item("copper_horse_armor", new CustomHorseArmor(4, "copper", new FabricItemSettings().maxCount(1)));
   public static final Item NETHERITE_HORSE_ARMOR = Backport.item("netherite_horse_armor", new CustomHorseArmor(19, "netherite", new FabricItemSettings().maxCount(1).fireproof()));
   public static RecipeSerializer<BundleDyeRecipe> BUNDLE_DYE;

   private NewItems() {
   }

   /** Horse armor whose texture lives in this mod's namespace. */
   static final class CustomHorseArmor extends HorseArmorItem {
      private final ResourceLocation tex;

      CustomHorseArmor(int protection, String name, Item.Properties props) {
         super(protection, name, props);
         this.tex = Backport.id("textures/entity/horse/armor/horse_armor_" + name + ".png");
      }

      @Override
      public ResourceLocation getTexture() {
         return this.tex;
      }
   }

   public static class BundleDyeRecipe extends CustomRecipe {
      public BundleDyeRecipe(ResourceLocation id, CraftingBookCategory category) {
         super(id, category);
      }

      @Override
      public boolean matches(CraftingContainer inv, Level level) {
         ItemStack bundle = ItemStack.EMPTY;
         boolean dye = false;
         for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty()) {
               continue;
            }
            if (s.getItem() instanceof BundleItem && bundle.isEmpty()) {
               bundle = s;
            } else if (s.getItem() instanceof DyeItem && !dye) {
               dye = true;
            } else {
               return false;
            }
         }
         return !bundle.isEmpty() && dye;
      }

      @Override
      public ItemStack assemble(CraftingContainer inv, RegistryAccess access) {
         ItemStack bundle = ItemStack.EMPTY;
         DyeColor color = null;
         for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.getItem() instanceof BundleItem) {
               bundle = s;
            } else if (s.getItem() instanceof DyeItem d) {
               color = d.getDyeColor();
            }
         }
         if (bundle.isEmpty() || color == null) {
            return ItemStack.EMPTY;
         }
         ItemStack out = new ItemStack(BUNDLES.get(color));
         if (bundle.hasTag()) {
            out.setTag(bundle.getTag().copy());
         }
         return out;
      }

      @Override
      public boolean canCraftInDimensions(int w, int h) {
         return w * h >= 2;
      }

      @Override
      public RecipeSerializer<?> getSerializer() {
         return BUNDLE_DYE;
      }
   }

   public static void init() {
      BUNDLE = Backport.item("bundle", new BundleItem(new FabricItemSettings().maxCount(1)));
      DyeColor[] dyes = DyeColor.values();
      for (DyeColor c : dyes) {
         BUNDLES.put(c, Backport.item(c.getName() + "_bundle", new BundleItem(new FabricItemSettings().maxCount(1))));
      }
      BUNDLE_DYE = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Backport.id("bundle_dye"), new SimpleCraftingRecipeSerializer<>(BundleDyeRecipe::new));
      // pottery patterns
      for (String s : new String[]{"flow", "guster", "scrape"}) {
         String name = s + "_pottery_pattern";
         ResourceKey<String> key = ResourceKey.create(Registries.DECORATED_POT_PATTERNS, Backport.id(name));
         Registry.register(BuiltInRegistries.DECORATED_POT_PATTERNS, key, name);
      }
      music("bounce", 8, 234);
      music("creator", 11, 176);
      music("creator_music_box", 11, 73);
      music("lava_chicken", 9, 134);
      music("precipice", 13, 299);
      music("tears", 10, 175);
   }

   private static void music(String name, int comparator, int seconds) {
      ResourceLocation id = Backport.id("music_disc." + name);
      SoundEvent event = Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
      Backport.item("music_disc_" + name, new RecordItem(comparator, event, new FabricItemSettings().maxCount(1).rarity(Rarity.RARE), seconds * 20));
   }

   public static ResourceKey<String> potPatternFor(Item item) {
      if (item == FLOW_SHERD) {
         return ResourceKey.create(Registries.DECORATED_POT_PATTERNS, Backport.id("flow_pottery_pattern"));
      }
      if (item == GUSTER_SHERD) {
         return ResourceKey.create(Registries.DECORATED_POT_PATTERNS, Backport.id("guster_pottery_pattern"));
      }
      if (item == SCRAPE_SHERD) {
         return ResourceKey.create(Registries.DECORATED_POT_PATTERNS, Backport.id("scrape_pottery_pattern"));
      }
      return null;
   }
}
