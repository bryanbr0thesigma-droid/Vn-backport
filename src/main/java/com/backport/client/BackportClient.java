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
   public static final net.minecraft.client.model.geom.ModelLayerLocation COPPER_GOLEM_LAYER = new net.minecraft.client.model.geom.ModelLayerLocation(Backport.id("copper_golem"), "main");

   public void onInitializeClient() {
      net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(COPPER_GOLEM_LAYER, CopperGolemModel::createBodyLayer);
      net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(com.backport.BackportEntities.COPPER_GOLEM, CopperGolemRenderer::new);
      net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(com.backport.CopperChestBlockEntity.TYPE, CopperChestRenderer::new);
      for (net.minecraft.world.level.block.Block block : BuiltInRegistries.BLOCK) {
         if (BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(Backport.ID) && BuiltInRegistries.BLOCK.getKey(block).getPath().endsWith("copper_chest")) {
            net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.INSTANCE.register(block.asItem(), (stack, mode, matrices, vcp, light, overlay) ->
               net.minecraft.client.Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(
                  new com.backport.CopperChestBlockEntity(net.minecraft.core.BlockPos.ZERO, block.defaultBlockState()), matrices, vcp, light, overlay));
         }
      }

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
