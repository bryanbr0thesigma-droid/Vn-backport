package com.backport.client;

import com.backport.entity.Mannequin;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

/** Renders a mannequin with a player model and the skin of its profile (or a default skin). */
public class MannequinRenderer extends LivingEntityRenderer<Mannequin, PlayerModel<Mannequin>> {
   private record Skin(ResourceLocation texture, boolean slim) {
   }

   private static final Map<String, GameProfile> PROFILES = new HashMap<>();
   private final PlayerModel<Mannequin> wide;
   private final PlayerModel<Mannequin> slim;

   public MannequinRenderer(EntityRendererProvider.Context ctx) {
      super(ctx, new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
      this.wide = this.model;
      this.slim = new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER_SLIM), true);
      this.addLayer(new HumanoidArmorLayer<>(this, new net.minecraft.client.model.HumanoidModel<>(ctx.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
         new net.minecraft.client.model.HumanoidModel<>(ctx.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), ctx.getModelManager()));
      this.addLayer(new ItemInHandLayer<>(this, ctx.getItemInHandRenderer()));
      this.addLayer(new CustomHeadLayer<>(this, ctx.getModelSet(), ctx.getItemInHandRenderer()));
   }

   private Skin skin(Mannequin entity) {
      String name = entity.profileName();
      UUID id = entity.profileId().orElse(null);
      if (id == null && name.isEmpty()) {
         return new Skin(DefaultPlayerSkin.getDefaultSkin(entity.getUUID()), "slim".equals(DefaultPlayerSkin.getSkinModelName(entity.getUUID())));
      }
      String key = name.isEmpty() ? id.toString() : name;
      GameProfile profile = PROFILES.get(key);
      if (profile == null) {
         GameProfile base = new GameProfile(id, name.isEmpty() ? null : name);
         PROFILES.put(key, base);
         SkullBlockEntity.updateGameprofile(base, resolved -> PROFILES.put(key, resolved));
         profile = base;
      }
      Minecraft mc = Minecraft.getInstance();
      Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = mc.getSkinManager().getInsecureSkinInformation(profile);
      MinecraftProfileTexture skin = textures.get(MinecraftProfileTexture.Type.SKIN);
      if (skin != null) {
         return new Skin(mc.getSkinManager().registerTexture(skin, MinecraftProfileTexture.Type.SKIN), "slim".equals(skin.getMetadata("model")));
      }
      UUID fallback = profile.getId() != null ? profile.getId() : UUIDUtil(name);
      return new Skin(DefaultPlayerSkin.getDefaultSkin(fallback), "slim".equals(DefaultPlayerSkin.getSkinModelName(fallback)));
   }

   private static UUID UUIDUtil(String name) {
      return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(java.nio.charset.StandardCharsets.UTF_8));
   }

   @Override
   public ResourceLocation getTextureLocation(Mannequin entity) {
      return this.skin(entity).texture;
   }

   @Override
   public void render(Mannequin entity, float yaw, float partial, PoseStack stack, MultiBufferSource buffer, int light) {
      this.model = this.skin(entity).slim ? this.slim : this.wide;
      PlayerModel<Mannequin> m = this.model;
      m.setAllVisible(true);
      m.jacket.visible = entity.layerVisible(1);
      m.leftSleeve.visible = entity.layerVisible(2);
      m.rightSleeve.visible = entity.layerVisible(3);
      m.leftPants.visible = entity.layerVisible(4);
      m.rightPants.visible = entity.layerVisible(5);
      m.hat.visible = entity.layerVisible(6);
      m.crouching = entity.isCrouching();
      super.render(entity, yaw, partial, stack, buffer, light);
   }

   @Override
   protected void setupRotations(Mannequin entity, PoseStack stack, float age, float yaw, float partial) {
      float swim = entity.getSwimAmount(partial);
      super.setupRotations(entity, stack, age, yaw, partial);
      if (entity.getPose() == Pose.SWIMMING) {
         float pitch = entity.getXRot();
         float target = entity.isInWater() ? -90.0F - pitch : -90.0F;
         stack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(Mth.lerp(swim, 0.0F, target)));
         stack.translate(0.0F, -1.0F, 0.3F);
      } else if (entity.getPose() == Pose.FALL_FLYING) {
         stack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90.0F - entity.getXRot()));
      }
   }

   @Override
   protected boolean shouldShowName(Mannequin entity) {
      return entity.descriptionText() != null && this.entityRenderDispatcher.distanceToSqr(entity) < 400.0
         || super.shouldShowName(entity);
   }

   @Override
   protected void renderNameTag(Mannequin entity, Component name, PoseStack stack, MultiBufferSource buffer, int light) {
      Component text = entity.hasCustomName() ? name : entity.descriptionText();
      super.renderNameTag(entity, text == null ? name : text, stack, buffer, light);
   }
}
