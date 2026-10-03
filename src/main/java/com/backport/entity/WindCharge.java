package com.backport.entity;

import com.backport.BackportEntities;
import com.backport.BackportSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class WindCharge extends AbstractHurtingProjectile {
   private static final double RADIUS = 3.0;
   private int life;

   public WindCharge(EntityType<? extends WindCharge> type, Level level) {
      super(type, level);
   }

   public WindCharge(LivingEntity owner, Level level) {
      super(BackportEntities.WIND_CHARGE, owner, 0.0, 0.0, 0.0, level);
   }

   protected float getInertia() {
      return 1.0F;
   }

   protected boolean shouldBurn() {
      return false;
   }

   protected net.minecraft.core.particles.ParticleOptions getTrailParticle() {
      return ParticleTypes.CLOUD;
   }

   public boolean isPickable() {
      return false;
   }

   public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
      return false;
   }

   public void tick() {
      super.tick();
      if (++this.life > 100 && !this.level().isClientSide) {
         this.discard();
      }
   }

   protected void onHitEntity(EntityHitResult result) {
      super.onHitEntity(result);
      if (!this.level().isClientSide) {
         Entity target = result.getEntity();
         Entity owner = this.getOwner();
         if (target instanceof LivingEntity living) {
            living.hurt(this.damageSources().mobProjectile(this, owner instanceof LivingEntity l ? l : null), 1.0F);
         }

         this.burst(this.position());
      }
   }

   protected void onHitBlock(BlockHitResult result) {
      super.onHitBlock(result);
      if (!this.level().isClientSide) {
         BlockPos pos = result.getBlockPos();
         BlockState state = this.level().getBlockState(pos);
         if (state.getBlock() instanceof DoorBlock door) {
            door.setOpen(null, this.level(), state, pos, !state.getValue(DoorBlock.OPEN));
         } else if (state.getBlock() instanceof TrapDoorBlock) {
            this.level().setBlock(pos, state.cycle(TrapDoorBlock.OPEN), 3);
            this.level().playSound(null, pos, net.minecraft.sounds.SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
         } else if (state.getBlock() instanceof FenceGateBlock) {
            this.level().setBlock(pos, state.cycle(FenceGateBlock.OPEN), 3);
            this.level().playSound(null, pos, net.minecraft.sounds.SoundEvents.FENCE_GATE_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
         } else if (state.getBlock() instanceof ButtonBlock button) {
            button.press(state, this.level(), pos);
         } else if (state.getBlock() instanceof LeverBlock lever) {
            lever.pull(state, this.level(), pos);
         }

         this.burst(result.getLocation().add(Vec3.atLowerCornerOf(result.getDirection().getNormal()).scale(0.1)));
      }
   }

   protected void onHit(HitResult result) {
      super.onHit(result);
   }

   private void burst(Vec3 center) {
      if (this.level() instanceof ServerLevel level) {
         level.sendParticles(ParticleTypes.POOF, center.x, center.y, center.z, 20, 0.5, 0.5, 0.5, 0.1);
         level.sendParticles(ParticleTypes.CLOUD, center.x, center.y, center.z, 10, 0.3, 0.3, 0.3, 0.15);
         level.playSound(null, center.x, center.y, center.z, BackportSounds.ENTITY_WIND_CHARGE_WIND_BURST, SoundSource.NEUTRAL, 1.0F, 1.0F);
         for (Entity entity : level.getEntities(this, net.minecraft.world.phys.AABB.ofSize(center, RADIUS * 2, RADIUS * 2, RADIUS * 2))) {
            Vec3 offset = entity.getBoundingBox().getCenter().subtract(center);
            double distance = offset.length();
            if (distance < RADIUS && !(entity instanceof WindCharge)) {
               double power = (1.0 - distance / RADIUS) * 1.5;
               Vec3 push = distance < 1.0E-4 ? new Vec3(0.0, 1.0, 0.0) : offset.normalize();
               if (entity instanceof LivingEntity living) {
                  power *= 1.0 - living.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE);
               }

               entity.setDeltaMovement(entity.getDeltaMovement().add(push.scale(power)));
               entity.hurtMarked = true;
               entity.fallDistance = 0.0F;
            }
         }

         this.discard();
      }
   }
}
