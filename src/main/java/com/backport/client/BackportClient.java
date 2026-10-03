package com.backport.client;

import com.backport.Backport;
import com.backport.CopperBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

public class BackportClient implements ClientModInitializer {
   public void onInitializeClient() {
      ParticleFactoryRegistry.getInstance().register(CopperBlocks.COPPER_FIRE_FLAME, FlameParticle.Provider::new);
      for (Block block : BuiltInRegistries.BLOCK) {
         if (!BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(Backport.ID)) {
            continue;
         }

         String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
         if (path.contains("copper_bars") || path.contains("copper_chain") || path.contains("copper_lantern") || path.contains("copper_torch")
            || path.contains("copper_door") || path.contains("copper_trapdoor") || path.contains("copper_grate")) {
            BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout());
         }
      }
   }
}
