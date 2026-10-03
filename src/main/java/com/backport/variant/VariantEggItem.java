package com.backport.variant;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class VariantEggItem extends EggItem {
   public final int variant;

   public VariantEggItem(Properties props, int variant) {
      super(props);
      this.variant = variant;
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EGG_THROW, SoundSource.PLAYERS, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
      if (!level.isClientSide) {
         ThrownVariantEgg egg = new ThrownVariantEgg(Variants.EGG_ENTITY, level);
         egg.setOwner(player);
         egg.setPos(player.getX(), player.getEyeY() - 0.1F, player.getZ());
         egg.setItem(stack);
         egg.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
         level.addFreshEntity(egg);
      }
      player.awardStat(Stats.ITEM_USED.get(this));
      if (!player.getAbilities().instabuild) {
         stack.shrink(1);
      }
      return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
   }
}
