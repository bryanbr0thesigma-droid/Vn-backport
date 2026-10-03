package com.backport.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class FireflyParticle extends TextureSheetParticle {
   private final float baseSize;
   private final float phase;

   FireflyParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
      super(level, x, y, z);
      this.pickSprite(sprites);
      this.lifetime = (int) (this.random.nextFloat() * 40.0F + 80.0F);
      this.baseSize = 0.04F + this.random.nextFloat() * 0.03F;
      this.quadSize = this.baseSize;
      this.phase = this.random.nextFloat() * 6.28F;
      this.xd = (this.random.nextFloat() - 0.5F) * 0.02;
      this.yd = (this.random.nextFloat() - 0.5F) * 0.01;
      this.zd = (this.random.nextFloat() - 0.5F) * 0.02;
      this.friction = 0.99F;
      this.hasPhysics = false;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   public int getLightColor(float partial) {
      return 15728880;
   }

   public void tick() {
      super.tick();
      float t = (float) this.age / this.lifetime;
      float fade = t < 0.2F ? t / 0.2F : (t > 0.8F ? (1.0F - t) / 0.2F : 1.0F);
      this.alpha = Mth.clamp(fade * (0.6F + 0.4F * Mth.sin(this.age * 0.35F + this.phase)), 0.0F, 1.0F);
      if (this.random.nextInt(8) == 0) {
         this.xd += (this.random.nextFloat() - 0.5F) * 0.006;
         this.zd += (this.random.nextFloat() - 0.5F) * 0.006;
         this.yd += (this.random.nextFloat() - 0.5F) * 0.004;
      }
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public Provider(SpriteSet sprites) {
         this.sprites = sprites;
      }

      public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
         return new FireflyParticle(level, x, y, z, this.sprites);
      }
   }
}
