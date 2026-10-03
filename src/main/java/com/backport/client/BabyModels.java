package com.backport.client;

import com.backport.Backport;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

/** Registry of 26.x baby mob models; renderers swap them in via LivingEntityRendererBabyMixin. */
public final class BabyModels {
   public static boolean active;
   private record Def(ModelLayerLocation layer, Supplier<LayerDefinition> mesh, Function<ModelPart, EntityModel<?>> ctor) {
   }

   private static final Map<EntityType<?>, Def> DEFS = new LinkedHashMap<>();
   private static final Map<EntityType<?>, EntityModel<?>> BAKED = new HashMap<>();
   private static final Map<String, ResourceLocation> TEX = new HashMap<>();
   private static final Map<String, String> OVERRIDES = new HashMap<>();

   static {
      OVERRIDES.put("pig/pig", "pig/pig_temperate");
      OVERRIDES.put("cow/cow", "cow/cow_temperate");
      OVERRIDES.put("cow/red_mooshroom", "cow/mooshroom_red");
      OVERRIDES.put("cow/brown_mooshroom", "cow/mooshroom_brown");
      OVERRIDES.put("chicken", "chicken/chicken_temperate");
      OVERRIDES.put("sheep/sheep", "sheep/sheep");
      OVERRIDES.put("sheep/sheep_fur", "sheep/sheep_wool");
      for (String r : new String[]{"brown", "white", "black", "gold", "salt", "white_splotched", "toast", "caerbannog"}) {
         OVERRIDES.put("rabbit/" + r, "rabbit/rabbit_" + r);
      }
      for (String c : new String[]{"tabby", "black", "red", "siamese", "british_shorthair", "calico", "persian", "ragdoll", "white", "jellie", "all_black"}) {
         OVERRIDES.put("cat/" + c, "cat/cat_" + c);
      }
      OVERRIDES.put("turtle/big_sea_turtle", "turtle/turtle");
      OVERRIDES.put("fox/snow_fox", "fox/fox_snow");
      OVERRIDES.put("fox/snow_fox_sleep", "fox/fox_snow_sleep");
      for (String l : new String[]{"brown", "creamy", "gray", "white"}) {
         OVERRIDES.put("llama/" + l, "llama/llama_" + l);
      }
      OVERRIDES.put("zoglin/zoglin", "hoglin/zoglin");
      OVERRIDES.put("hoglin/hoglin", "hoglin/hoglin");
   }

   private BabyModels() {
   }

   private static void def(EntityType<?> type, String id, Supplier<LayerDefinition> mesh, Function<ModelPart, EntityModel<?>> ctor) {
      DEFS.put(type, new Def(new ModelLayerLocation(Backport.id("baby_" + id), "main"), mesh, ctor));
   }

   public static void init() {
      def(EntityType.PIG, "pig", BabyMeshes::pig, PigModel::new);
      def(EntityType.COW, "cow", BabyMeshes::cow, CowModel::new);
      def(EntityType.MOOSHROOM, "mooshroom", BabyMeshes::cow, CowModel::new);
      def(EntityType.SHEEP, "sheep", BabyMeshes::sheep, SheepModel::new);
      def(EntityType.CHICKEN, "chicken", BabyMeshes::chicken, ChickenModel::new);
      def(EntityType.WOLF, "wolf", BabyMeshes::wolf, WolfModel::new);
      def(EntityType.CAT, "cat", BabyMeshes::feline, CatModel::new);
      def(EntityType.OCELOT, "ocelot", BabyMeshes::feline, OcelotModel::new);
      def(EntityType.GOAT, "goat", BabyMeshes::goat, GoatModel::new);
      def(EntityType.FOX, "fox", BabyMeshes::fox, FoxModel::new);
      def(EntityType.RABBIT, "rabbit", BabyMeshes::rabbit, RabbitModel::new);
      def(EntityType.POLAR_BEAR, "polar_bear", BabyMeshes::polar_bear, PolarBearModel::new);
      def(EntityType.PANDA, "panda", BabyMeshes::panda, PandaModel::new);
      def(EntityType.CAMEL, "camel", BabyMeshes::camel, CamelModel::new);
      def(EntityType.AXOLOTL, "axolotl", BabyMeshes::axolotl, AxolotlModel::new);
      def(EntityType.BEE, "bee", BabyMeshes::bee, BeeModel::new);
      def(EntityType.DOLPHIN, "dolphin", BabyMeshes::dolphin, DolphinModel::new);
      def(EntityType.SQUID, "squid", BabyMeshes::squid, SquidModel::new);
      def(EntityType.GLOW_SQUID, "glow_squid", BabyMeshes::squid, SquidModel::new);
      def(EntityType.TURTLE, "turtle", BabyMeshes::turtle, TurtleModel::new);
      def(EntityType.HOGLIN, "hoglin", BabyMeshes::hoglin, HoglinModel::new);
      def(EntityType.ZOGLIN, "zoglin", BabyMeshes::hoglin, HoglinModel::new);
      def(EntityType.PIGLIN, "piglin", BabyMeshes::piglin, PiglinModel::new);
      def(EntityType.ZOMBIFIED_PIGLIN, "zombified_piglin", BabyMeshes::piglin, PiglinModel::new);
      def(EntityType.STRIDER, "strider", BabyMeshes::strider, StriderModel::new);
      def(EntityType.ZOMBIE, "zombie", BabyMeshes::zombie, ZombieModel::new);
      def(EntityType.HUSK, "husk", BabyMeshes::zombie, ZombieModel::new);
      def(EntityType.DROWNED, "drowned", BabyMeshes::zombie, DrownedModel::new);
      def(EntityType.LLAMA, "llama", BabyMeshes::llama, LlamaModel::new);
      for (Def d : DEFS.values()) {
         EntityModelLayerRegistry.registerModelLayer(d.layer(), d.mesh()::get);
      }
   }

   public static EntityModel<?> modelFor(EntityType<?> type) {
      Def d = DEFS.get(type);
      if (d == null) {
         return null;
      }
      if (BAKED.containsKey(type)) {
         return BAKED.get(type);
      }
      EntityModel<?> m = null;
      try {
         m = d.ctor().apply(Minecraft.getInstance().getEntityModels().bakeLayer(d.layer()));
      } catch (RuntimeException e) {
         Backport.LOGGER.warn("Baby model for {} could not be built; using the adult model: {}", type, e.toString());
      }
      BAKED.put(type, m);
      return m;
   }

   /** Baby texture for an adult texture, or null if there is none. */
   public static ResourceLocation textureFor(ResourceLocation adult) {
      String p = adult.getPath();
      if (!p.startsWith("textures/entity/") || !p.endsWith(".png")) {
         return null;
      }
      String rel = p.substring("textures/entity/".length(), p.length() - 4);
      return TEX.computeIfAbsent(rel, r -> {
         String baby = OVERRIDES.containsKey(r) ? OVERRIDES.get(r) + (OVERRIDES.get(r).equals("sheep/sheep") ? "_baby" : "_baby") : r + "_baby";
         ResourceLocation loc = Backport.id("textures/entity/baby/" + baby + ".png");
         return Minecraft.getInstance().getResourceManager().getResource(loc).isPresent() ? loc : null;
      });
   }
}
