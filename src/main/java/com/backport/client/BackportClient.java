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

   public static final net.minecraft.client.model.geom.ModelLayerLocation COPPER_GOLEM_RUNNING_LAYER = new net.minecraft.client.model.geom.ModelLayerLocation(Backport.id("copper_golem_running"), "main");
   public static final net.minecraft.client.model.geom.ModelLayerLocation COPPER_GOLEM_SITTING_LAYER = new net.minecraft.client.model.geom.ModelLayerLocation(Backport.id("copper_golem_sitting"), "main");
   public static final net.minecraft.client.model.geom.ModelLayerLocation COPPER_GOLEM_STAR_LAYER = new net.minecraft.client.model.geom.ModelLayerLocation(Backport.id("copper_golem_star"), "main");

   public static final net.minecraft.client.model.geom.ModelLayerLocation WIND_CHARGE_LAYER = new net.minecraft.client.model.geom.ModelLayerLocation(Backport.id("wind_charge"), "main");

   public static final net.minecraft.client.model.geom.ModelLayerLocation BOGGED_LAYER = new net.minecraft.client.model.geom.ModelLayerLocation(Backport.id("bogged"), "main");
   public static final net.minecraft.client.model.geom.ModelLayerLocation BOGGED_OUTER_LAYER = new net.minecraft.client.model.geom.ModelLayerLocation(Backport.id("bogged"), "outer");

   public static final net.minecraft.client.model.geom.ModelLayerLocation ARMADILLO_LAYER = new net.minecraft.client.model.geom.ModelLayerLocation(Backport.id("armadillo"), "main");
   public static final net.minecraft.client.model.geom.ModelLayerLocation ARMADILLO_BABY_LAYER = new net.minecraft.client.model.geom.ModelLayerLocation(Backport.id("armadillo"), "baby");

   public static final net.minecraft.client.model.geom.ModelLayerLocation HAPPY_GHAST_LAYER = new net.minecraft.client.model.geom.ModelLayerLocation(Backport.id("happy_ghast"), "main");
   public static final net.minecraft.client.model.geom.ModelLayerLocation HAPPY_GHAST_BABY_LAYER = new net.minecraft.client.model.geom.ModelLayerLocation(Backport.id("happy_ghast"), "baby");
   public static final net.minecraft.client.model.geom.ModelLayerLocation HAPPY_GHAST_HARNESS_LAYER = new net.minecraft.client.model.geom.ModelLayerLocation(Backport.id("happy_ghast"), "harness");
   public static final net.minecraft.client.model.geom.ModelLayerLocation NAUTILUS_LAYER = new net.minecraft.client.model.geom.ModelLayerLocation(Backport.id("nautilus"), "main");
   public static final net.minecraft.client.model.geom.ModelLayerLocation NAUTILUS_BABY_LAYER = new net.minecraft.client.model.geom.ModelLayerLocation(Backport.id("nautilus"), "baby");
   public static final net.minecraft.client.model.geom.ModelLayerLocation NAUTILUS_ARMOR_LAYER = new net.minecraft.client.model.geom.ModelLayerLocation(Backport.id("nautilus"), "armor");
   public static final net.minecraft.client.model.geom.ModelLayerLocation NAUTILUS_SADDLE_LAYER = new net.minecraft.client.model.geom.ModelLayerLocation(Backport.id("nautilus"), "saddle");
   public static final net.minecraft.client.model.geom.ModelLayerLocation BREEZE_LAYER = new net.minecraft.client.model.geom.ModelLayerLocation(Backport.id("breeze"), "main");
   public static final net.minecraft.client.model.geom.ModelLayerLocation BREEZE_WIND_LAYER = new net.minecraft.client.model.geom.ModelLayerLocation(Backport.id("breeze"), "wind");
   public static final net.minecraft.client.model.geom.ModelLayerLocation BREEZE_EYES_LAYER = new net.minecraft.client.model.geom.ModelLayerLocation(Backport.id("breeze"), "eyes");

   public void onInitializeClient() {
      net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(BREEZE_LAYER, BreezeModel::createBodyLayer);
      net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(BREEZE_WIND_LAYER, BreezeModel::createWindLayer);
      net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(BREEZE_EYES_LAYER, BreezeModel::createEyesLayer);
      net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(com.backport.trial.TrialSpawnerBlockEntity.TYPE, TrialRenderers.Spawner::new);
      net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(com.backport.trial.VaultBlockEntity.TYPE, TrialRenderers.Vault::new);
      net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlock(com.backport.BackportItems.TRIAL_SPAWNER, net.minecraft.client.renderer.RenderType.cutout());
      net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlock(com.backport.BackportItems.VAULT, net.minecraft.client.renderer.RenderType.cutout());
      net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(com.backport.BackportEntities.PARCHED, ctx -> new net.minecraft.client.renderer.entity.SkeletonRenderer(ctx) {
         public net.minecraft.resources.ResourceLocation getTextureLocation(net.minecraft.world.entity.monster.AbstractSkeleton e) {
            return Backport.id("textures/entity/skeleton/parched.png");
         }
      });
      net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(com.backport.BackportEntities.CAMEL_HUSK, ctx -> new net.minecraft.client.renderer.entity.CamelRenderer(ctx, net.minecraft.client.model.geom.ModelLayers.CAMEL) {
         public net.minecraft.resources.ResourceLocation getTextureLocation(net.minecraft.world.entity.animal.camel.Camel e) {
            return Backport.id("textures/entity/camel/camel_husk.png");
         }
      });
      net.fabricmc.fabric.api.client.model.ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, out) -> {
         for (String m : new String[]{"wooden", "stone", "copper", "iron", "golden", "diamond", "netherite"}) {
            out.accept(new net.minecraft.client.resources.model.ModelResourceLocation(Backport.ID, m + "_spear_icon", "inventory"));
         }
      });
      net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(HAPPY_GHAST_LAYER, () -> HappyGhastModel.createBodyLayer(false));
      net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(HAPPY_GHAST_BABY_LAYER, () -> HappyGhastModel.createBodyLayer(true));
      net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(HAPPY_GHAST_HARNESS_LAYER, HappyGhastHarnessModel::createHarnessLayer);
      net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(com.backport.BackportEntities.HAPPY_GHAST, HappyGhastRenderer::new);
      net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlock(com.backport.BackportItems.DRIED_GHAST, net.minecraft.client.renderer.RenderType.cutout());
      net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(NAUTILUS_LAYER, NautilusModel::createBodyLayer);
      net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(NAUTILUS_BABY_LAYER, NautilusModel::createBabyBodyLayer);
      net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(NAUTILUS_ARMOR_LAYER, () -> NautilusModel.createArmorLayer(0.01F, false));
      net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(NAUTILUS_SADDLE_LAYER, () -> NautilusModel.createArmorLayer(0.2F, true));
      net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(com.backport.BackportEntities.NAUTILUS, NautilusRenderer::new);
      net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(com.backport.BackportEntities.ZOMBIE_NAUTILUS, NautilusRenderer::new);
      net.minecraft.client.gui.screens.MenuScreens.register(com.backport.crafter.CrafterMenu.TYPE, CrafterScreen::new);
      net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(com.backport.BackportEntities.BREEZE, BreezeRenderer::new);
      net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(com.backport.BackportEntities.BREEZE_WIND_CHARGE, WindChargeRenderer::new);
      net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
         if (type == net.minecraft.world.entity.EntityType.WOLF && renderer instanceof net.minecraft.client.renderer.entity.WolfRenderer wolfRenderer) {
            helper.register(new WolfArmorLayer(wolfRenderer, context.getModelSet()));
         }
      });
      net.minecraft.client.renderer.item.ItemProperties.register(com.backport.BackportItems.WOLF_ARMOR, Backport.id("dyed"),
         (stack, level, entity, seed) -> com.backport.BackportItems.WOLF_ARMOR instanceof net.minecraft.world.item.DyeableLeatherItem d && d.hasCustomColor(stack) ? 1.0F : 0.0F);
      net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry.ITEM.register(
         (stack, tint) -> tint == 1 ? ((net.minecraft.world.item.DyeableLeatherItem)stack.getItem()).getColor(stack) : -1, com.backport.BackportItems.WOLF_ARMOR);
      net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(ARMADILLO_LAYER, ArmadilloModel::createAdultLayer);
      net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(ARMADILLO_BABY_LAYER, ArmadilloModel::createBabyLayer);
      net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(com.backport.BackportEntities.ARMADILLO, ArmadilloRenderer::new);
      net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(BOGGED_LAYER, BoggedModel::createBodyLayer);
      net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(BOGGED_OUTER_LAYER, BoggedModel::createOuterLayer);
      net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(com.backport.BackportEntities.BOGGED, BoggedRenderer::new);
      net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(WIND_CHARGE_LAYER, WindChargeRenderer::createBodyLayer);
      net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(com.backport.BackportEntities.WIND_CHARGE, WindChargeRenderer::new);
      net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(COPPER_GOLEM_RUNNING_LAYER, CopperGolemModel::createRunningPoseBodyLayer);
      net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(COPPER_GOLEM_SITTING_LAYER, CopperGolemModel::createSittingPoseBodyLayer);
      net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(COPPER_GOLEM_STAR_LAYER, CopperGolemModel::createStarPoseBodyLayer);
      net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(com.backport.CopperGolemStatueBlockEntity.TYPE, CopperGolemStatueRenderer::new);
      net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(COPPER_GOLEM_LAYER, CopperGolemModel::createBodyLayer);
      net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(com.backport.BackportEntities.COPPER_GOLEM, CopperGolemRenderer::new);
      net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(com.backport.CopperChestBlockEntity.TYPE, CopperChestRenderer::new);
      for (net.minecraft.world.level.block.Block block : BuiltInRegistries.BLOCK) {
         if (BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(Backport.ID) && BuiltInRegistries.BLOCK.getKey(block).getPath().endsWith("copper_golem_statue")) {
            net.minecraft.world.level.block.state.BlockState itemState = block.defaultBlockState();
            net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.INSTANCE.register(block.asItem(), (stack, mode, matrices, vcp, light, overlay) -> {
               matrices.pushPose();
               matrices.translate(0.5F, 0.0F, 0.5F);
               CopperGolemModel model = new CopperGolemModel(net.minecraft.client.Minecraft.getInstance().getEntityModels().bakeLayer(COPPER_GOLEM_LAYER));
               CopperGolemStatueRenderer.renderModel(model, ((com.backport.CopperGolemStatueBlock)block).getWeatheringState().ordinal(), matrices, vcp, light, overlay);
               matrices.popPose();
            });
         }
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
