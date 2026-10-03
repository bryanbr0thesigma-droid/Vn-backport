package com.backport.client;

import com.backport.Backport;
import java.util.HashMap;
import java.util.WeakHashMap;
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

   /** Default part positions of baby humanoid models (HumanoidModel.setupAnim overwrites them with adult values). */
   public static final Map<Object, float[][]> HUMANOID_POSES = new WeakHashMap<>();
   private record Lock(ModelPart[] parts, float[][] pos, ModelPart[] zeroXRot) {
   }

   private static final Map<EntityModel<?>, Lock> LOCKS = new java.util.WeakHashMap<>();
   private static final Map<EntityType<?>, String[][]> LOCK_NAMES = new HashMap<>();
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

   private static void lock(String[] posParts, String[] zeroRot, EntityType<?>... types) {
      for (EntityType<?> t : types) {
         LOCK_NAMES.put(t, new String[][]{posParts, zeroRot});
      }
   }

   public static void init() {
      lock(new String[]{"head", "body", "tail1", "tail2", "left_hind_leg", "right_hind_leg", "left_front_leg", "right_front_leg"}, new String[]{"body"}, EntityType.CAT, EntityType.OCELOT);
      lock(new String[]{"head", "body", "upper_body", "tail", "left_hind_leg", "right_hind_leg", "left_front_leg", "right_front_leg"}, new String[]{"body", "upper_body"}, EntityType.WOLF);
      lock(new String[]{"head", "body", "left_hind_leg", "right_hind_leg", "left_front_leg", "right_front_leg"}, new String[]{"body"}, EntityType.FOX);
      lock(new String[]{"head", "body", "left_hind_leg", "right_hind_leg", "left_front_leg", "right_front_leg"}, new String[0], EntityType.HOGLIN, EntityType.ZOGLIN);
      lock(new String[]{"body", "right_leg", "left_leg"}, new String[0], EntityType.STRIDER);
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

   /** Undo adult-pose assignments the 1.20.1 animation code makes on baby models. */
   public static void postAnim(EntityModel<?> model) {
      Lock l = LOCKS.get(model);
      if (l == null) {
         return;
      }
      for (int i = 0; i < l.parts().length; i++) {
         l.parts()[i].setPos(l.pos()[i][0], l.pos()[i][1], l.pos()[i][2]);
      }
      for (ModelPart p : l.zeroXRot()) {
         p.xRot = 0.0F;
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
         ModelPart root = Minecraft.getInstance().getEntityModels().bakeLayer(d.layer());
         m = d.ctor().apply(root);
         String[][] names = LOCK_NAMES.get(type);
         if (names != null) {
            ModelPart[] parts = new ModelPart[names[0].length];
            float[][] pos = new float[parts.length][];
            for (int i = 0; i < parts.length; i++) {
               parts[i] = root.getChild(names[0][i]);
               pos[i] = new float[]{parts[i].x, parts[i].y, parts[i].z};
            }
            ModelPart[] zero = new ModelPart[names[1].length];
            for (int i = 0; i < zero.length; i++) {
               zero[i] = root.getChild(names[1][i]);
            }
            LOCKS.put(m, new Lock(parts, pos, zero));
         }
      } catch (RuntimeException e) {
         Backport.LOGGER.warn("Baby model for {} could not be built; using the adult model: {}", type, e.toString());
      }
      if (m instanceof HumanoidModel<?> h) {
         ModelPart[] parts = {h.head, h.body, h.rightArm, h.leftArm, h.rightLeg, h.leftLeg};
         float[][] poses = new float[parts.length][];
         for (int i = 0; i < parts.length; i++) {
            poses[i] = new float[]{parts[i].x, parts[i].y, parts[i].z};
         }
         HUMANOID_POSES.put(m, poses);
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
