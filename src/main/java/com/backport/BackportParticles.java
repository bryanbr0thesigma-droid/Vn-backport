package com.backport;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class BackportParticles {
   public static final SimpleParticleType FIREFLY = Registry.register(BuiltInRegistries.PARTICLE_TYPE, Backport.id("firefly"), FabricParticleTypes.simple());
   public static final SimpleParticleType RED_POPLAR_LEAVES = Registry.register(BuiltInRegistries.PARTICLE_TYPE, Backport.id("red_poplar_leaves"), FabricParticleTypes.simple());
   public static final SimpleParticleType ORANGE_POPLAR_LEAVES = Registry.register(BuiltInRegistries.PARTICLE_TYPE, Backport.id("orange_poplar_leaves"), FabricParticleTypes.simple());
   public static final SimpleParticleType YELLOW_POPLAR_LEAVES = Registry.register(BuiltInRegistries.PARTICLE_TYPE, Backport.id("yellow_poplar_leaves"), FabricParticleTypes.simple());

   public static final SimpleParticleType SULFUR_CUBE_GOO = Registry.register(BuiltInRegistries.PARTICLE_TYPE, Backport.id("sulfur_cube_goo"), FabricParticleTypes.simple());

   private BackportParticles() {
   }

   public static void init() {
   }
}
