package com.palegarden;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class PaleParticles {
   public static final SimpleParticleType PALE_OAK_LEAVES = Registry.register(
      BuiltInRegistries.PARTICLE_TYPE, PaleGarden.id("pale_oak_leaves"), FabricParticleTypes.simple()
   );

   private PaleParticles() {
   }

   public static void init() {
   }
}
