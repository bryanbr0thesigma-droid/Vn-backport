package com.backport;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.PushReaction;

/** Items and small blocks from the Tricky Trials update. */
public final class BackportItems {
   public static final Block HEAVY_CORE = Backport.block("heavy_core", new HeavyCoreBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.METAL).instrument(NoteBlockInstrument.SNARE).strength(10.0F).explosionResistance(1200.0F).sound(SoundType.NETHERITE_BLOCK)));
   public static final Block CRAFTER = Backport.block("crafter", new com.backport.crafter.CrafterBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F, 3.5F)));
   public static final net.minecraft.world.level.block.SoundType TRIAL_SPAWNER_SOUNDS = new net.minecraft.world.level.block.SoundType(1.0F, 1.0F,
      BackportSounds.BLOCK_TRIAL_SPAWNER_BREAK, BackportSounds.BLOCK_TRIAL_SPAWNER_STEP, BackportSounds.BLOCK_TRIAL_SPAWNER_PLACE, BackportSounds.BLOCK_TRIAL_SPAWNER_HIT, BackportSounds.BLOCK_TRIAL_SPAWNER_FALL);
   public static final net.minecraft.world.level.block.SoundType VAULT_SOUNDS = new net.minecraft.world.level.block.SoundType(1.0F, 1.0F,
      BackportSounds.BLOCK_VAULT_BREAK, BackportSounds.BLOCK_VAULT_STEP, BackportSounds.BLOCK_VAULT_PLACE, BackportSounds.BLOCK_VAULT_HIT, BackportSounds.BLOCK_VAULT_FALL);
   public static final Block TRIAL_SPAWNER = Backport.block("trial_spawner", new com.backport.trial.TrialSpawnerBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().lightLevel(s -> s.getValue(com.backport.trial.TrialSpawnerBlock.STATE).lightLevel)
         .strength(50.0F).sound(TRIAL_SPAWNER_SOUNDS).noOcclusion()));
   public static final Block VAULT = Backport.block("vault", new com.backport.trial.VaultBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().lightLevel(s -> s.getValue(com.backport.trial.VaultBlock.STATE).lightLevel)
         .strength(50.0F).sound(VAULT_SOUNDS).noOcclusion()));
   public static final Item WOODEN_SPEAR = Backport.item("wooden_spear", new SpearItem(net.minecraft.world.item.Tiers.WOOD, 0.65F, 0.7F, 0.75F, 5.0F, 14.0F, 10.0F, 5.1F, 15.0F, 4.6F, new FabricItemSettings()));
   public static final Item STONE_SPEAR = Backport.item("stone_spear", new SpearItem(net.minecraft.world.item.Tiers.STONE, 0.75F, 0.82F, 0.7F, 4.5F, 13.0F, 9.0F, 5.1F, 13.75F, 4.6F, new FabricItemSettings()));
   public static final Item COPPER_SPEAR = Backport.item("copper_spear", new SpearItem(CopperTools.TIER, 0.85F, 0.82F, 0.65F, 4.0F, 12.0F, 8.25F, 5.1F, 12.5F, 4.6F, new FabricItemSettings()));
   public static final Item IRON_SPEAR = Backport.item("iron_spear", new SpearItem(net.minecraft.world.item.Tiers.IRON, 0.95F, 0.95F, 0.6F, 2.5F, 11.0F, 6.75F, 5.1F, 11.25F, 4.6F, new FabricItemSettings()));
   public static final Item GOLDEN_SPEAR = Backport.item("golden_spear", new SpearItem(net.minecraft.world.item.Tiers.GOLD, 0.95F, 0.7F, 0.7F, 3.5F, 13.0F, 8.5F, 5.1F, 13.75F, 4.6F, new FabricItemSettings()));
   public static final Item DIAMOND_SPEAR = Backport.item("diamond_spear", new SpearItem(net.minecraft.world.item.Tiers.DIAMOND, 1.05F, 1.075F, 0.5F, 3.0F, 10.0F, 6.5F, 5.1F, 10.0F, 4.6F, new FabricItemSettings()));
   public static final Item NETHERITE_SPEAR = Backport.item("netherite_spear", new SpearItem(net.minecraft.world.item.Tiers.NETHERITE, 1.15F, 1.2F, 0.4F, 2.5F, 9.0F, 5.5F, 5.1F, 8.75F, 4.6F, new FabricItemSettings().fireResistant()));
   public static final SoundType DRIED_GHAST_SOUNDS = new SoundType(1.0F, 1.0F, BackportSounds.BLOCK_DRIED_GHAST_BREAK, BackportSounds.BLOCK_DRIED_GHAST_STEP,
      BackportSounds.BLOCK_DRIED_GHAST_PLACE, BackportSounds.BLOCK_DRIED_GHAST_STEP, BackportSounds.BLOCK_DRIED_GHAST_FALL);
   public static final Block DRIED_GHAST = Backport.block("dried_ghast", new DriedGhastBlock(
      BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(0.5F).sound(DRIED_GHAST_SOUNDS).noOcclusion()));
   public static final String[] HARNESS_COLORS = {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"};
   public static final Item[] HARNESSES = new Item[16];
   static {
      for (int i = 0; i < 16; i++) HARNESSES[i] = Backport.item(HARNESS_COLORS[i] + "_harness", new HarnessItem(HARNESS_COLORS[i], new FabricItemSettings().maxCount(1)));
   }
   public static final Item NAUTILUS_SHELL = Backport.item("nautilus_shell", new Item(new FabricItemSettings()));
   public static final Item COPPER_NAUTILUS_ARMOR = Backport.item("copper_nautilus_armor", new NautilusArmorItem(4, new FabricItemSettings().maxDamage(176)));
   public static final Item IRON_NAUTILUS_ARMOR = Backport.item("iron_nautilus_armor", new NautilusArmorItem(5, new FabricItemSettings().maxDamage(240)));
   public static final Item GOLDEN_NAUTILUS_ARMOR = Backport.item("golden_nautilus_armor", new NautilusArmorItem(7, new FabricItemSettings().maxDamage(112)));
   public static final Item DIAMOND_NAUTILUS_ARMOR = Backport.item("diamond_nautilus_armor", new NautilusArmorItem(11, new FabricItemSettings().maxDamage(528)));
   public static final Item NETHERITE_NAUTILUS_ARMOR = Backport.item("netherite_nautilus_armor", new NautilusArmorItem(11, new FabricItemSettings().maxDamage(592).fireproof()));
   public static final Item BREEZE_ROD = Backport.item("breeze_rod", new Item(new FabricItemSettings()));
   public static final Item MACE = Backport.item("mace", new MaceItem(new FabricItemSettings().maxDamage(500).rarity(Rarity.EPIC)));
   public static final Item WIND_CHARGE = Backport.item("wind_charge", new WindChargeItem(new FabricItemSettings()));
   public static final Item TRIAL_KEY = Backport.item("trial_key", new Item(new FabricItemSettings()));
   public static final Item OMINOUS_TRIAL_KEY = Backport.item("ominous_trial_key", new Item(new FabricItemSettings()));
   public static final Item OMINOUS_BOTTLE = Backport.item("ominous_bottle", new OminousBottleItem(new FabricItemSettings().maxCount(64).rarity(Rarity.UNCOMMON)));
   public static final Item WOLF_ARMOR = Backport.item("wolf_armor", new WolfArmorItem(new FabricItemSettings().maxDamage(64)));
   public static final Item ARMADILLO_SCUTE = Backport.item("armadillo_scute", new Item(new FabricItemSettings()));

   private BackportItems() {
   }

   public static void init() {
      com.backport.crafter.CrafterBlockEntity.register(CRAFTER);
      com.backport.crafter.CrafterMenu.registerServer();
      com.backport.trial.TrialSpawnerBlockEntity.register(TRIAL_SPAWNER);
      com.backport.trial.VaultBlockEntity.register(VAULT);
   }

   public static final class NautilusArmorItem extends Item {
      public final int defense;

      NautilusArmorItem(int defense, Item.Properties properties) {
         super(properties);
         this.defense = defense;
      }
   }

   public static final class HarnessItem extends Item {
      public final String color;

      HarnessItem(String color, Item.Properties properties) {
         super(properties);
         this.color = color;
      }
   }

   public static final class WolfArmorItem extends Item implements net.minecraft.world.item.DyeableLeatherItem {
      WolfArmorItem(Item.Properties properties) {
         super(properties);
      }

      public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
         return repair.is(ARMADILLO_SCUTE);
      }

      public int getColor(ItemStack stack) {
         net.minecraft.nbt.CompoundTag display = stack.getTagElement("display");
         return display != null && display.contains("color", 99) ? display.getInt("color") : 0xFFFFFF;
      }
   }

   static final class WindChargeItem extends Item {
      WindChargeItem(Item.Properties properties) {
         super(properties);
      }

      public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
         ItemStack stack = player.getItemInHand(hand);
         level.playSound(null, player.getX(), player.getY(), player.getZ(), BackportSounds.ENTITY_WIND_CHARGE_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
         if (!level.isClientSide) {
            com.backport.entity.WindCharge charge = new com.backport.entity.WindCharge(player, level);
            charge.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            net.minecraft.world.phys.Vec3 look = player.getLookAngle().scale(1.5);
            charge.setDeltaMovement(look);
            charge.xPower = look.x * 0.0;
            charge.yPower = 0.0;
            charge.zPower = 0.0;
            level.addFreshEntity(charge);
         }

         player.awardStat(Stats.ITEM_USED.get(this));
         if (!player.getAbilities().instabuild) {
            stack.shrink(1);
         }

         player.getCooldowns().addCooldown(this, 10);
         return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
      }
   }

   static final class OminousBottleItem extends Item {
      OminousBottleItem(Item.Properties properties) {
         super(properties);
      }

      public UseAnim getUseAnimation(ItemStack stack) {
         return UseAnim.DRINK;
      }

      public int getUseDuration(ItemStack stack) {
         return 32;
      }

      public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
         return net.minecraft.world.item.ItemUtils.startUsingInstantly(level, player, hand);
      }

      public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
         if (!level.isClientSide) {
            int amplifier = stack.hasTag() ? stack.getTag().getInt("OminousBottleAmplifier") : 0;
            entity.removeEffect(MobEffects.BAD_OMEN);
            entity.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, 120000, amplifier, false, true, true));
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.0F, 1.0F);
         }

         if (entity instanceof Player player) {
            player.awardStat(Stats.ITEM_USED.get(this));
            if (!player.getAbilities().instabuild) {
               stack.shrink(1);
            }
         }

         return stack;
      }
   }
}
