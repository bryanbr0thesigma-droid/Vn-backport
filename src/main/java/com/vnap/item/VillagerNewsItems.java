package com.vnap.item;

import com.vnap.VillagerNewsAddonPort;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class VillagerNewsItems {
   public static final ResourceKey<CreativeModeTab> CREATIVE_TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, VillagerNewsAddonPort.id("items"));
   public static final Item HANDBOOK = register("handbook", new Item(new Item.Properties().stacksTo(1)));
   public static final Item MAYOR_HAT = register("mayor_hat", new HeadItem(new Item.Properties().stacksTo(1)));
   public static final Item MICROPHONE = register("microphone", new Item(new Item.Properties().stacksTo(1)));
   public static final Item MOUSTACHE = register("moustache", new HeadItem(new Item.Properties().stacksTo(1)));
   public static final Item TESTIFICATE_MAN_HELMET = register("testificate_man_helmet", new HeadItem(new Item.Properties().stacksTo(1)));
   public static final Item VILLAGER_NOSE = register("villager_nose", new HeadItem(new Item.Properties().stacksTo(1)));
   public static final Item MAYOR_VILLAGER_SPAWN_EGG = registerSpawnEgg("mayor_villager_spawn_egg", EntityType.VILLAGER, "Mayor Villager", 0x562C1D, 0xB8A02C);
   public static final Item TESTIFICATE_MAN_SPAWN_EGG = registerSpawnEgg("testificate_man_spawn_egg", EntityType.VILLAGER, "Testificate Man", 0x562C1D, 0xD2A679);
   public static final Item VILLAGER_5_SPAWN_EGG = registerSpawnEgg("villager_5_spawn_egg", EntityType.VILLAGER, "Villager #5", 0x562C1D, 0x6AA84F);
   public static final Item VILLAGER_9_SPAWN_EGG = registerSpawnEgg("villager_9_spawn_egg", EntityType.VILLAGER, "Villager #9", 0x562C1D, 0x4A86E8);
   public static final Item UNTOUCHABLE_VILLAGER_SPAWN_EGG = registerSpawnEgg("untouchable_villager_spawn_egg", EntityType.VILLAGER, "Villager Unreachable", 0x562C1D, 0x999999);
   public static final Item WOOLY_SPAWN_EGG = registerSpawnEgg("wooly_spawn_egg", EntityType.SHEEP, "Wooly The Sheep", 0xE7E7E7, 0xFFC0CB);
   private static final Map<Item, Integer> COSMETICS = new LinkedHashMap<>();

   private VillagerNewsItems() {
   }

   public static void register() {
      Registry.register(
         BuiltInRegistries.CREATIVE_MODE_TAB,
         CREATIVE_TAB_KEY,
         FabricItemGroup.builder()
            .title(Component.translatable("itemGroup.villager-news-addon-port.items"))
            .icon(() -> new ItemStack(HANDBOOK))
            .displayItems((parameters, output) -> {
               output.accept(HANDBOOK);
               output.accept(MAYOR_HAT);
               output.accept(TESTIFICATE_MAN_HELMET);
               output.accept(MICROPHONE);
               output.accept(MOUSTACHE);
               output.accept(VILLAGER_NOSE);
               output.accept(MAYOR_VILLAGER_SPAWN_EGG);
               output.accept(TESTIFICATE_MAN_SPAWN_EGG);
               output.accept(VILLAGER_5_SPAWN_EGG);
               output.accept(VILLAGER_9_SPAWN_EGG);
               output.accept(UNTOUCHABLE_VILLAGER_SPAWN_EGG);
               output.accept(WOOLY_SPAWN_EGG);
            })
            .build()
      );
   }

   public static int cosmetic(Item item) {
      return COSMETICS.getOrDefault(item, 0);
   }

   public static Item cosmeticItem(int cosmetic) {
      return COSMETICS.entrySet().stream().filter(entry -> entry.getValue() == cosmetic).map(Entry::getKey).findFirst().orElse(null);
   }

   private static Item register(String path, Item item) {
      return Registry.register(BuiltInRegistries.ITEM, VillagerNewsAddonPort.id(path), item);
   }

   private static Item registerSpawnEgg(String path, EntityType<? extends Mob> type, String entityName, int primary, int secondary) {
      return register(path, new NamedSpawnEggItem(type, primary, secondary, new Item.Properties(), entityName));
   }

   /** Items that can be worn in the head slot (the 26.x port used the equippable component). */
   private static final class HeadItem extends Item implements Equipable {
      private HeadItem(Properties properties) {
         super(properties);
      }

      @Override
      public EquipmentSlot getEquipmentSlot() {
         return EquipmentSlot.HEAD;
      }

      @Override
      public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
         return this.swapWithEquipmentSlot(this, level, player, hand);
      }
   }

   /**
    * Spawn egg that applies a custom name and persistence to the spawned mob, replacing the
    * entity_data component the 26.x port attached to its eggs.
    */
   private static final class NamedSpawnEggItem extends SpawnEggItem {
      private final String entityName;

      private NamedSpawnEggItem(EntityType<? extends Mob> type, int primary, int secondary, Properties properties, String entityName) {
         super(type, primary, secondary, properties);
         this.entityName = entityName;
      }

      private void ensureEntityTag(ItemStack stack) {
         CompoundTag stackTag = stack.getOrCreateTag();
         if (!stackTag.contains("EntityTag", Tag.TAG_COMPOUND)) {
            CompoundTag entityTag = new CompoundTag();
            entityTag.putString("CustomName", Component.Serializer.toJson(Component.literal(this.entityName)));
            entityTag.putBoolean("PersistenceRequired", true);
            stackTag.put("EntityTag", entityTag);
         }
      }

      @Override
      public ItemStack getDefaultInstance() {
         ItemStack stack = super.getDefaultInstance();
         this.ensureEntityTag(stack);
         return stack;
      }

      @Override
      public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
         if (!level.isClientSide) {
            this.ensureEntityTag(stack);
         }
      }

      @Override
      public InteractionResult useOn(UseOnContext context) {
         this.ensureEntityTag(context.getItemInHand());
         return super.useOn(context);
      }

      @Override
      public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
         this.ensureEntityTag(player.getItemInHand(hand));
         return super.use(level, player, hand);
      }
   }

   static {
      COSMETICS.put(MAYOR_HAT, 1);
      COSMETICS.put(TESTIFICATE_MAN_HELMET, 2);
      COSMETICS.put(MICROPHONE, 3);
      COSMETICS.put(MOUSTACHE, 4);
   }
}
