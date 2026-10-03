package com.vnap.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

public final class SleepingZParticle extends TextureSheetParticle {
   private SleepingZParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
      super(level, x, y, z, 0.0, 0.0, 0.0);
      this.pickSprite(sprites);
      this.lifetime = 40 + this.random.nextInt(10);
      this.quadSize = 0.05F + this.random.nextFloat() * 0.02F;
      this.xd = 0.01;
      this.yd = 0.025;
      this.zd = 0.0;
      this.gravity = 0.0F;
      this.hasPhysics = false;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   protected int getLightColor(float partialTick) {
      return 15728880;
   }

   public void tick() {
      super.tick();
      this.alpha = Math.max(0.0F, 1.0F - (float)this.age / (float)this.lifetime);
      this.quadSize *= 1.008F;
   }

   public static void tick(Minecraft client) {
      ClientLevel level = client.level;
      if (level == null || client.isPaused() || level.getGameTime() % 20L != 0L || client.player == null) {
         return;
      }

      AABB box = client.player.getBoundingBox().inflate(24.0);
      for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, v -> (v instanceof Villager || v instanceof WanderingTrader) && v.isSleeping())) {
         level.addParticle(
            com.vnap.VillagerNewsAddonPort.SLEEPING_Z,
            e.getX(), e.getY() + 0.9, e.getZ(), 0.0, 0.0, 0.0
         );
      }
   }

   public static final class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public Provider(SpriteSet sprites) {
         this.sprites = sprites;
      }

      public net.minecraft.client.particle.Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
         return new SleepingZParticle(level, x, y, z, this.sprites);
      }
   }
}
