package com.palegarden.client;

import com.palegarden.PaleBlocks;
import com.palegarden.PaleEntities;
import com.palegarden.PaleGarden;
import com.palegarden.PaleParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;

public class PaleGardenClient implements ClientModInitializer {
   public static final ModelLayerLocation CREAKING_LAYER = new ModelLayerLocation(PaleGarden.id("creaking"), "main");

   public void onInitializeClient() {
      EntityModelLayerRegistry.registerModelLayer(CREAKING_LAYER, CreakingModel::createBodyLayer);
      EntityRendererRegistry.register(PaleEntities.CREAKING, CreakingRenderer::new);
      EntityRendererRegistry.register(PaleEntities.PALE_OAK_BOAT, ctx -> new PaleBoatRenderer(ctx, false));
      EntityRendererRegistry.register(PaleEntities.PALE_OAK_CHEST_BOAT, ctx -> new PaleBoatRenderer(ctx, true));
      ParticleFactoryRegistry.getInstance().register(PaleParticles.PALE_OAK_LEAVES, FallingLeavesParticle.PaleOakProvider::new);

      BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutoutMipped(), PaleBlocks.PALE_OAK_LEAVES);
      BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(),
         PaleBlocks.PALE_OAK_SAPLING, PaleBlocks.POTTED_PALE_OAK_SAPLING, PaleBlocks.PALE_OAK_DOOR, PaleBlocks.PALE_OAK_TRAPDOOR,
         PaleBlocks.PALE_MOSS_CARPET, PaleBlocks.PALE_HANGING_MOSS, PaleBlocks.OPEN_EYEBLOSSOM, PaleBlocks.CLOSED_EYEBLOSSOM,
         PaleBlocks.POTTED_OPEN_EYEBLOSSOM, PaleBlocks.POTTED_CLOSED_EYEBLOSSOM, PaleBlocks.RESIN_CLUMP);
   }
}
