package com.palegarden.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class FallingLeavesParticle extends TextureSheetParticle {
   private float rotSpeed;
   private final float particleRandom;
   private final float spinAcceleration;
   private final float windBig;
   private final boolean swirl;
   private final boolean flowAway;
   private final double xaFlowScale;
   private final double zaFlowScale;
   private final double swirlPeriod;

   protected FallingLeavesParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites, float gravity, float wind, boolean swirl, boolean flowAway, float size, float startFall) {
      super(level, x, y, z);
      this.setSprite(sprites.get(this.random.nextInt(12), 12));
      this.rotSpeed = (float)Math.toRadians(this.random.nextBoolean() ? -30.0 : 30.0);
      this.particleRandom = this.random.nextFloat();
      this.spinAcceleration = (float)Math.toRadians(this.random.nextBoolean() ? -5.0 : 5.0);
      this.windBig = wind;
      this.swirl = swirl;
      this.flowAway = flowAway;
      this.lifetime = 300;
      this.gravity = gravity * 1.2F * 0.0025F;
      float scale = size * (this.random.nextBoolean() ? 0.05F : 0.075F);
      this.quadSize = scale;
      this.setSize(scale, scale);
      this.friction = 1.0F;
      this.yd = -startFall;
      this.xaFlowScale = Math.cos(Math.toRadians(this.particleRandom * 60.0F)) * this.windBig;
      this.zaFlowScale = Math.sin(Math.toRadians(this.particleRandom * 60.0F)) * this.windBig;
      this.swirlPeriod = Math.toRadians(1000.0F + this.particleRandom * 3000.0F);
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.lifetime-- <= 0) {
         this.remove();
      }

      if (!this.removed) {
         float elapsed = 300 - this.lifetime;
         float progress = Math.min(elapsed / 300.0F, 1.0F);
         double dx = 0.0;
         double dz = 0.0;
         if (this.flowAway) {
            dx += this.xaFlowScale * Math.pow(progress, 1.25);
            dz += this.zaFlowScale * Math.pow(progress, 1.25);
         }

         if (this.swirl) {
            dx += progress * Math.cos(progress * this.swirlPeriod) * this.windBig;
            dz += progress * Math.sin(progress * this.swirlPeriod) * this.windBig;
         }

         this.xd += dx * 0.0025F;
         this.zd += dz * 0.0025F;
         this.yd = this.yd - this.gravity;
         this.rotSpeed = this.rotSpeed + this.spinAcceleration / 20.0F;
         this.oRoll = this.roll;
         this.roll = this.roll + this.rotSpeed / 20.0F;
         this.move(this.xd, this.yd, this.zd);
         if (this.onGround || this.lifetime < 299 && (this.xd == 0.0 || this.zd == 0.0)) {
            this.remove();
         }

         if (!this.removed) {
            this.xd = this.xd * this.friction;
            this.yd = this.yd * this.friction;
            this.zd = this.zd * this.friction;
         }
      }
   }

   public static class PaleOakProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public PaleOakProvider(SpriteSet sprites) {
         this.sprites = sprites;
      }

      public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
         return new FallingLeavesParticle(level, x, y, z, this.sprites, 0.07F, 10.0F, true, false, 2.0F, 0.021F);
      }
   }
}
