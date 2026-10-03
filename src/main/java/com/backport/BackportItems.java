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
   public static final Item BREEZE_ROD = Backport.item("breeze_rod", new Item(new FabricItemSettings()));
   public static final Item MACE = Backport.item("mace", new MaceItem(new FabricItemSettings().maxDamage(500).rarity(Rarity.EPIC)));
   public static final Item WIND_CHARGE = Backport.item("wind_charge", new WindChargeItem(new FabricItemSettings()));
   public static final Item TRIAL_KEY = Backport.item("trial_key", new Item(new FabricItemSettings()));
   public static final Item OMINOUS_TRIAL_KEY = Backport.item("ominous_trial_key", new Item(new FabricItemSettings()));
   public static final Item OMINOUS_BOTTLE = Backport.item("ominous_bottle", new OminousBottleItem(new FabricItemSettings().maxCount(64).rarity(Rarity.UNCOMMON)));
   public static final Item ARMADILLO_SCUTE = Backport.item("armadillo_scute", new Item(new FabricItemSettings()));

   private BackportItems() {
   }

   public static void init() {
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
