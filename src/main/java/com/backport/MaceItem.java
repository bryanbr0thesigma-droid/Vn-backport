package com.backport;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MaceItem extends Item {
   private final Multimap<Attribute, AttributeModifier> modifiers;

   public MaceItem(Item.Properties properties) {
      super(properties);
      ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
      builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 5.0, AttributeModifier.Operation.ADDITION));
      builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -3.4F, AttributeModifier.Operation.ADDITION));
      this.modifiers = builder.build();
   }

   public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
      return slot == EquipmentSlot.MAINHAND ? this.modifiers : super.getDefaultAttributeModifiers(slot);
   }

   public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
      return !player.isCreative();
   }

   public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
      return repair.is(BackportItems.BREEZE_ROD);
   }

   public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      if (canSmashAttack(attacker) && attacker.level() instanceof ServerLevel level) {
         attacker.setDeltaMovement(attacker.getDeltaMovement().x, 0.01F, attacker.getDeltaMovement().z);
         if (attacker instanceof ServerPlayer player) {
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(player));
         }

         SoundEvent sound = target.onGround()
            ? (attacker.fallDistance > 5.0F ? BackportSounds.ITEM_MACE_SMASH_GROUND_HEAVY : BackportSounds.ITEM_MACE_SMASH_GROUND)
            : BackportSounds.ITEM_MACE_SMASH_AIR;
         level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), sound, attacker.getSoundSource(), 1.0F, 1.0F);
         knockback(level, attacker, target);
         attacker.resetFallDistance();
      }

      stack.hurtAndBreak(1, attacker, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
      return true;
   }

   public static boolean canSmashAttack(LivingEntity attacker) {
      return attacker.fallDistance > 1.5F && !attacker.isFallFlying();
   }

   public static float smashBonus(LivingEntity attacker) {
      double fall = attacker.fallDistance;
      double damage;
      if (fall <= 3.0) {
         damage = 4.0 * fall;
      } else if (fall <= 8.0) {
         damage = 12.0 + 2.0 * (fall - 3.0);
      } else {
         damage = 22.0 + fall - 8.0;
      }

      return (float)damage;
   }

   private static void knockback(Level level, LivingEntity attacker, LivingEntity target) {
      level.levelEvent(2001, target.getOnPos(), net.minecraft.world.level.block.Block.getId(net.minecraft.world.level.block.Blocks.STONE.defaultBlockState()));
      List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(3.5), e -> {
         boolean tamed = e instanceof TamableAnimal animal && animal.isTame() && animal.isOwnedBy(attacker);
         boolean stand = e instanceof ArmorStand armorStand && armorStand.isMarker();
         boolean flying = e instanceof Player p && p.isCreative() && p.getAbilities().flying;
         return !e.isSpectator() && e != attacker && e != target && !attacker.isAlliedTo(e) && !tamed && !stand && !flying && target.distanceToSqr(e) <= 12.25;
      });
      for (LivingEntity e : nearby) {
         Vec3 direction = e.position().subtract(target.position());
         double power = (3.5 - direction.length()) * 0.7F * (attacker.fallDistance > 5.0F ? 2 : 1) * (1.0 - e.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
         if (power > 0.0) {
            Vec3 push = direction.normalize().scale(power);
            e.push(push.x, 0.7F, push.z);
            if (e instanceof ServerPlayer other) {
               other.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(other));
            }
         }
      }
   }
}
