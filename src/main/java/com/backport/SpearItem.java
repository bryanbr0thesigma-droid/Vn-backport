package com.backport;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Backport of the 1.21.11 spear: jab with the attack key, lunge by holding use. */
public class SpearItem extends Item {
   private static final Map<LivingEntity, Map<UUID, Long>> STABBED = new WeakHashMap<>();
   private static final Map<LivingEntity, java.util.Set<UUID>> LIVING_STABBED = new WeakHashMap<>();
   private final Tier tier;
   private final Multimap<Attribute, AttributeModifier> modifiers;
   private final int delayTicks;
   private final float damageMultiplier;
   private final Cond dismount;
   private final Cond knockback;
   private final Cond damage;
   private final boolean wood;

   private record Cond(int maxTicks, float minSpeed, float minRelative) {
      boolean test(int ticksUsed, double speed, double relative, double factor) {
         return ticksUsed <= this.maxTicks && speed >= this.minSpeed * factor && relative >= this.minRelative * factor;
      }
   }

   public SpearItem(Tier tier, float attackDuration, float damageMultiplier, float delay, float dismountTime, float dismountThreshold,
                    float knockbackTime, float knockbackThreshold, float damageTime, float damageThreshold, Item.Properties properties) {
      super(properties.durability(tier.getUses()));
      this.tier = tier;
      this.wood = tier == net.minecraft.world.item.Tiers.WOOD;
      this.delayTicks = (int) (delay * 20.0F);
      this.damageMultiplier = damageMultiplier;
      this.dismount = new Cond((int) (dismountTime * 20.0F), dismountThreshold, 0.0F);
      this.knockback = new Cond((int) (knockbackTime * 20.0F), knockbackThreshold, 0.0F);
      this.damage = new Cond((int) (damageTime * 20.0F), 0.0F, damageThreshold);
      ImmutableMultimap.Builder<Attribute, AttributeModifier> b = ImmutableMultimap.builder();
      b.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", tier.getAttackDamageBonus(), AttributeModifier.Operation.ADDITION));
      b.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", 1.0F / attackDuration - 4.0, AttributeModifier.Operation.ADDITION));
      this.modifiers = b.build();
   }

   public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
      return slot == EquipmentSlot.MAINHAND ? this.modifiers : super.getDefaultAttributeModifiers(slot);
   }

   /** Ticks of charging until the weapon can no longer deal damage (delay plus the damage window). */
   public int damageUseDuration() {
      return this.delayTicks + this.damage.maxTicks;
   }

   public boolean isWood() {
      return this.wood;
   }

   public int getEnchantmentValue() {
      return this.tier.getEnchantmentValue();
   }

   public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
      return this.tier.getRepairIngredient().test(repair) || super.isValidRepairItem(stack, repair);
   }

   public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
      return !player.isCreative();
   }

   public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      stack.hurtAndBreak(1, attacker, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
      return true;
   }

   public UseAnim getUseAnimation(ItemStack stack) {
      return UseAnim.SPEAR;
   }

   public int getUseDuration(ItemStack stack) {
      return 72000;
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      player.startUsingItem(hand);
      if (!level.isClientSide) {
         STABBED.remove(player);
         LIVING_STABBED.remove(player);
         level.playSound(null, player.getX(), player.getY(), player.getZ(), this.wood ? BackportSounds.ITEM_SPEAR_WOOD_USE : BackportSounds.ITEM_SPEAR_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
      }
      return InteractionResultHolder.consume(stack);
   }

   private static Vec3 motion(Entity e) {
      return new Vec3(e.getX() - e.xo, e.getY() - e.yo, e.getZ() - e.zo).scale(20.0);
   }

   public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remaining) {
      if (!(level instanceof ServerLevel sl)) return;
      int used = this.getUseDuration(stack) - remaining;
      if (used < this.delayTicks) return;
      int ticksUsed = used - this.delayTicks;
      if (ticksUsed > Math.max(this.damage.maxTicks, Math.max(this.knockback.maxTicks, this.dismount.maxTicks)) + 20) return;
      Vec3 look = user.getLookAngle();
      double attackerSpeed = look.dot(motion(user));
      double reach = user instanceof Player p && p.isCreative() ? 6.5 : 4.5;
      Vec3 eye = user.getEyePosition();
      Vec3 end = eye.add(look.scale(reach));
      double blockDist = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, user)).getLocation().distanceTo(eye);
      boolean affected = false;
      Map<UUID, Long> hits = STABBED.computeIfAbsent(user, k -> new java.util.HashMap<>());
      for (Entity target : level.getEntities(user, new AABB(eye, end).inflate(1.0), e -> e.isAttackable() && !e.isSpectator() && e != user && !e.isPassengerOfSameVehicle(user))) {
         AABB box = target.getBoundingBox().inflate(Math.max(0.125, target.getPickRadius()));
         java.util.Optional<Vec3> hit = box.clip(eye, end);
         if (hit.isEmpty() || hit.get().distanceTo(eye) > blockDist + 0.5) continue;
         Long last = hits.get(target.getUUID());
         if (last != null && level.getGameTime() - last < 10) continue;
         hits.put(target.getUUID(), level.getGameTime());
         double rel = Math.max(0.0, attackerSpeed - look.dot(motion(target)));
         double factor = user instanceof Player ? 1.0 : 0.2;
         boolean dDis = this.dismount.test(ticksUsed, attackerSpeed, rel, factor);
         boolean dKb = this.knockback.test(ticksUsed, attackerSpeed, rel, factor);
         boolean dDmg = this.damage.test(ticksUsed, attackerSpeed, rel, factor);
         if (!dDis && !dKb && !dDmg) continue;
         float dmg = (float) user.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) + Mth.floor(rel * this.damageMultiplier);
         DamageSource src = user instanceof Player p ? sl.damageSources().playerAttack(p) : sl.damageSources().mobAttack(user);
         boolean dealt = dDmg && target.hurt(src, dmg);
         if (dKb && target instanceof LivingEntity lt) {
            lt.knockback(0.4F + 0.5F, -look.x, -look.z);
         }
         if (dDis && target.isPassenger()) {
            target.stopRiding();
            dealt = true;
         }
         if (target instanceof LivingEntity lt) stack.hurtAndBreak(1, user, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
         if (target instanceof LivingEntity && (dealt || dKb) && user instanceof net.minecraft.server.level.ServerPlayer sp) {
            java.util.Set<UUID> set = LIVING_STABBED.computeIfAbsent(user, k -> new java.util.HashSet<>());
            set.add(target.getUUID());
            com.backport.advancement.BackportEvents.fire(sp, "spear_mobs", set.size());
         }
         affected |= dealt || dKb;
      }
      if (affected) {
         level.playSound(null, user.getX(), user.getY(), user.getZ(), this.wood ? BackportSounds.ITEM_SPEAR_WOOD_HIT : BackportSounds.ITEM_SPEAR_HIT, SoundSource.PLAYERS, 1.0F, 1.0F);
      }
   }

   public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int remaining) {
      STABBED.remove(user);
      LIVING_STABBED.remove(user);
   }
}
