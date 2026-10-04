package com.backport.spear;

import com.backport.Backport;
import com.backport.BackportSounds;
import com.backport.SpearItem;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** The 26.x spear jab: the attack key pierces every entity along a 2.0 to 4.5 block line (6.5 in creative). */
public final class SpearJab {
   public static final ResourceLocation ID = Backport.id("spear_jab");
   private static final double MIN_REACH = 2.0;
   private static final double MAX_REACH = 4.5;
   private static final double MAX_REACH_CREATIVE = 6.5;
   private static final float HITBOX_MARGIN = 0.125F;

   private SpearJab() {
   }

   public static void init() {
      ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, sender) -> server.execute(() -> attack(player)));
   }

   public static FriendlyByteBuf packet() {
      return PacketByteBufs.create();
   }

   private static boolean canHit(Player jabber, Entity target) {
      if (!target.isAlive() || target.isSpectator() || !target.isAttackable() || !target.isPickable() || target.skipAttackInteraction(jabber)) {
         return false;
      }
      if (target instanceof Player other && !jabber.canHarmPlayer(other)) {
         return false;
      }
      return !jabber.isPassengerOfSameVehicle(target);
   }

   public static void attack(ServerPlayer player) {
      ItemStack stack = player.getMainHandItem();
      if (!(stack.getItem() instanceof SpearItem spear) || player.isSpectator()) {
         return;
      }
      if (player.getAttackStrengthScale(0.5F) < 1.0F) {
         return;
      }
      ServerLevel level = player.serverLevel();
      Vec3 look = player.getViewVector(1.0F);
      Vec3 eye = player.getEyePosition();
      double max = player.isCreative() ? MAX_REACH_CREATIVE : MAX_REACH;
      Vec3 motion = new Vec3(player.getX() - player.xo, player.getY() - player.yo, player.getZ() - player.zo);
      Vec3 from = eye.add(look.scale(MIN_REACH));
      Vec3 to = eye.add(look.scale(max + Math.max(0.0, motion.dot(look))));
      var block = level.clip(new ClipContext(eye, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
      boolean blocked = false;
      if (block.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
         to = block.getLocation();
         blocked = eye.distanceToSqr(to) < eye.distanceToSqr(from);
      }
      List<Entity> hits = new ArrayList<>();
      if (!blocked) {
         AABB area = new AABB(from, to).inflate(1.0);
         for (Entity e : level.getEntities(player, area, t -> canHit(player, t))) {
            AABB box = e.getBoundingBox().inflate(Math.max(HITBOX_MARGIN, e.getPickRadius()));
            if (box.clip(from, to).isPresent() || box.contains(from)) {
               hits.add(e);
            }
         }
         Vec3 origin = eye;
         hits.sort(Comparator.comparingDouble(e -> e.distanceToSqr(origin)));
      }
      float base = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
      boolean hitSomething = false;
      for (Entity target : hits) {
         hitSomething |= stab(player, stack, target, base);
      }
      player.resetAttackStrengthTicker();
      player.swing(InteractionHand.MAIN_HAND, true);
      postPiercing(player, stack);
      if (hitSomething) {
         level.playSound(null, player.getX(), player.getY(), player.getZ(), spear.isWood() ? BackportSounds.ITEM_SPEAR_WOOD_HIT : BackportSounds.ITEM_SPEAR_HIT, SoundSource.PLAYERS, 1.0F, 1.0F);
      }
      level.playSound(null, player.getX(), player.getY(), player.getZ(), spear.isWood() ? BackportSounds.ITEM_SPEAR_WOOD_ATTACK : BackportSounds.ITEM_SPEAR_ATTACK, SoundSource.PLAYERS, 1.0F, 1.0F);
   }

   private static boolean stab(ServerPlayer player, ItemStack stack, Entity target, float base) {
      MobType type = target instanceof LivingEntity lt ? lt.getMobType() : MobType.UNDEFINED;
      float damage = base + EnchantmentHelper.getDamageBonus(stack, type);
      DamageSource source = player.damageSources().playerAttack(player);
      boolean dealt = target.hurt(source, damage);
      boolean affected = dealt;
      if (target instanceof LivingEntity living) {
         float kb = 0.4F + EnchantmentHelper.getKnockbackBonus(player) * 0.5F;
         double yaw = Math.toRadians(player.getYRot());
         living.knockback(kb, Math.sin(yaw), -Math.cos(yaw));
         affected = true;
         stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));
         if (dealt) {
            EnchantmentHelper.doPostHurtEffects(living, player);
            EnchantmentHelper.doPostDamageEffects(player, living);
         }
      }
      if (affected) {
         player.setLastHurtMob(target);
      }
      return affected;
   }

   /** Lunge: after a jab, push the user forward and pay for it in hunger and durability. */
   private static void postPiercing(ServerPlayer player, ItemStack stack) {
      int level = EnchantmentHelper.getItemEnchantmentLevel(Lunge.INSTANCE, stack);
      if (level <= 0 || player.isPassenger() || player.isFallFlying() || player.isInWater()) {
         return;
      }
      if (!player.isCreative() && player.getFoodData().getFoodLevel() < 7) {
         return;
      }
      stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));
      player.causeFoodExhaustion(4.0F * level);
      Vec3 look = player.getViewVector(1.0F);
      double magnitude = 0.458 * level;
      player.setDeltaMovement(player.getDeltaMovement().add(look.x * magnitude, 0.0, look.z * magnitude));
      player.connection.send(new ClientboundSetEntityMotionPacket(player));
      player.hurtMarked = false;
      SoundEvent[] sounds = {BackportSounds.ITEM_SPEAR_LUNGE_1, BackportSounds.ITEM_SPEAR_LUNGE_2, BackportSounds.ITEM_SPEAR_LUNGE_3};
      player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sounds[player.getRandom().nextInt(3)], SoundSource.PLAYERS, 1.0F, 1.0F);
   }
}
