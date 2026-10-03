package com.backport.ice;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class IceBallEntity extends ThrowableItemProjectile {
   public IceBallEntity(EntityType<? extends IceBallEntity> type, Level level) {
      super(type, level);
   }

   public IceBallEntity(Level level, LivingEntity owner) {
      super(IceCaves.ICE_BALL_ENTITY, owner, level);
   }

   @Override
   protected Item getDefaultItem() {
      return IceCaves.ICE_BALL;
   }

   private ParticleOptions particle() {
      return new ItemParticleOption(ParticleTypes.ITEM, this.getItem());
   }

   @Override
   public void handleEntityEvent(byte id) {
      if (id == 3) {
         ParticleOptions p = this.particle();
         for (int i = 0; i < 8; i++) {
            this.level().addParticle(p, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
         }
      }
   }

   @Override
   protected void onHitEntity(EntityHitResult hit) {
      super.onHitEntity(hit);
      Entity target = hit.getEntity();
      float speed = (float) this.getDeltaMovement().length();
      float damage = Mth.clamp(speed * 1.6F, 1.0F, 4.0F);
      DamageSource src = this.damageSources().thrown(this, this.getOwner());
      target.hurt(src, damage);
   }

   @Override
   protected void onHit(HitResult hit) {
      super.onHit(hit);
      if (!this.level().isClientSide) {
         this.level().broadcastEntityEvent(this, (byte) 3);
         this.discard();
      }
   }
}
