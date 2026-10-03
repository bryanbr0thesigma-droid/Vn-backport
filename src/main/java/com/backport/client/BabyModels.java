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
   /** Parts whose adult animation values are applied as offsets from the adult rest pose. */
   private record Rig(ModelPart[] parts, float[][] babyPos, float[][] adultPos, ModelPart[] rotParts, float[] babyRot, float[] adultRot,
                      ModelPart[] copyFrom, ModelPart[] copyTo, float[][] copyRest, boolean lockOnly) {
   }

   private static final java.util.Set<net.minecraft.client.model.geom.ModelLayerLocation> LOCK_ONLY = new java.util.HashSet<>();

   private record RigSpec(ModelLayerLocation adult, String[] posParts, String[] rotParts, String[][] copies) {
   }

   private static final Map<EntityModel<?>, Rig> RIGS = new java.util.WeakHashMap<>();
   private static final Map<EntityType<?>, RigSpec> RIG_SPECS = new HashMap<>();
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

   private static void rig(ModelLayerLocation adult, String[] posParts, String[] rotParts, String[][] copies, EntityType<?>... types) {
      for (EntityType<?> t : types) {
         RIG_SPECS.put(t, new RigSpec(adult, posParts, rotParts, copies));
      }
   }

   private static ModelPart path(ModelPart root, String path) {
      ModelPart p = root;
      for (String n : path.split("/")) {
         p = p.getChild(n);
      }
      return p;
   }

   private static String[] concat(String[] a, String[] b) {
      String[] r = java.util.Arrays.copyOf(a, a.length + b.length);
      System.arraycopy(b, 0, r, a.length, b.length);
      return r;
   }

   public static void init() {
      LOCK_ONLY.add(net.minecraft.client.model.geom.ModelLayers.CAT);
      LOCK_ONLY.add(net.minecraft.client.model.geom.ModelLayers.OCELOT);
      String[] none = new String[0];
      String[] four = {"left_hind_leg", "right_hind_leg", "left_front_leg", "right_front_leg"};
      rig(net.minecraft.client.model.geom.ModelLayers.CAT, concat(new String[]{"head", "body", "tail1", "tail2"}, four), new String[]{"body"}, null, EntityType.CAT);
      rig(net.minecraft.client.model.geom.ModelLayers.OCELOT, concat(new String[]{"head", "body", "tail1", "tail2"}, four), new String[]{"body"}, null, EntityType.OCELOT);
      rig(net.minecraft.client.model.geom.ModelLayers.WOLF, concat(new String[]{"head", "body", "upper_body", "tail"}, four), new String[]{"body", "upper_body"}, null, EntityType.WOLF);
      rig(net.minecraft.client.model.geom.ModelLayers.FOX, concat(new String[]{"head", "body", "body/tail"}, four), new String[]{"body"}, null, EntityType.FOX);
      rig(net.minecraft.client.model.geom.ModelLayers.HOGLIN, concat(new String[]{"head", "body"}, four), none, null, EntityType.HOGLIN);
      rig(net.minecraft.client.model.geom.ModelLayers.ZOGLIN, concat(new String[]{"head", "body"}, four), none, null, EntityType.ZOGLIN);
      rig(net.minecraft.client.model.geom.ModelLayers.STRIDER, new String[]{"body", "right_leg", "left_leg"}, none, null, EntityType.STRIDER);
      rig(net.minecraft.client.model.geom.ModelLayers.AXOLOTL, new String[]{"body", "body/head"}, none, null, EntityType.AXOLOTL);
      rig(net.minecraft.client.model.geom.ModelLayers.BEE, new String[]{"bone"}, none, null, EntityType.BEE);
      rig(net.minecraft.client.model.geom.ModelLayers.SHEEP, new String[]{"head"}, none, null, EntityType.SHEEP);
      rig(net.minecraft.client.model.geom.ModelLayers.RABBIT, none, none, new String[][]{
         {"head", "body/head"}, {"left_ear", "body/head/left_ear"}, {"right_ear", "body/head/right_ear"}, {"tail", "body/tail"},
         {"left_front_leg", "body/frontlegs/left_front_leg"}, {"right_front_leg", "body/frontlegs/right_front_leg"},
         {"left_haunch", "backlegs/left_hind_leg/left_haunch"}, {"right_haunch", "backlegs/right_hind_leg/right_haunch"},
         {"left_hind_foot", "backlegs/left_hind_leg"}, {"right_hind_foot", "backlegs/right_hind_leg"}}, EntityType.RABBIT);
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

   /** Reset rigged parts to their baby rest pose before the adult animation code runs. */
   public static void preAnim(EntityModel<?> model) {
      Rig r = RIGS.get(model);
      if (r == null) {
         return;
      }
      for (int i = 0; i < r.parts().length; i++) {
         r.parts()[i].setPos(r.babyPos()[i][0], r.babyPos()[i][1], r.babyPos()[i][2]);
      }
      for (int i = 0; i < r.rotParts().length; i++) {
         r.rotParts()[i].xRot = r.babyRot()[i];
      }
      for (int i = 0; i < r.copyFrom().length; i++) {
         r.copyFrom()[i].xRot = 0.0F;
         r.copyFrom()[i].yRot = 0.0F;
         r.copyFrom()[i].zRot = 0.0F;
         r.copyTo()[i].xRot = r.copyRest()[i][0];
         r.copyTo()[i].yRot = r.copyRest()[i][1];
         r.copyTo()[i].zRot = r.copyRest()[i][2];
      }
   }

   /** Re-express what the adult animation code did as offsets from the adult rest pose, applied to the baby parts. */
   public static void postAnim(EntityModel<?> model) {
      Rig r = RIGS.get(model);
      if (r == null) {
         return;
      }
      for (int i = 0; i < r.parts().length; i++) {
         ModelPart p = r.parts()[i];
         float[] b = r.babyPos()[i];
         float[] a = r.adultPos()[i];
         if (r.lockOnly()) {
            p.setPos(b[0], b[1], b[2]);
         } else if (p.x != b[0] || p.y != b[1] || p.z != b[2]) {
            p.setPos(b[0] + (p.x - a[0]), b[1] + (p.y - a[1]), b[2] + (p.z - a[2]));
         }
      }
      for (int i = 0; i < r.rotParts().length; i++) {
         ModelPart p = r.rotParts()[i];
         if (r.lockOnly()) {
            p.xRot = r.babyRot()[i];
         } else if (p.xRot != r.babyRot()[i]) {
            p.xRot = r.babyRot()[i] + (p.xRot - r.adultRot()[i]);
         }
      }
      for (int i = 0; i < r.copyFrom().length; i++) {
         r.copyTo()[i].xRot = r.copyRest()[i][0] + r.copyFrom()[i].xRot;
         r.copyTo()[i].yRot = r.copyRest()[i][1] + r.copyFrom()[i].yRot;
         r.copyTo()[i].zRot = r.copyRest()[i][2] + r.copyFrom()[i].zRot;
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
         RigSpec spec = RIG_SPECS.get(type);
         if (spec != null) {
            ModelPart adult = Minecraft.getInstance().getEntityModels().bakeLayer(spec.adult());
            ModelPart[] parts = new ModelPart[spec.posParts().length];
            float[][] bp = new float[parts.length][];
            float[][] ap = new float[parts.length][];
            for (int i = 0; i < parts.length; i++) {
               parts[i] = path(root, spec.posParts()[i]);
               ModelPart ad = path(adult, spec.posParts()[i]);
               bp[i] = new float[]{parts[i].x, parts[i].y, parts[i].z};
               ap[i] = new float[]{ad.x, ad.y, ad.z};
            }
            ModelPart[] rp = new ModelPart[spec.rotParts().length];
            float[] br = new float[rp.length];
            float[] ar = new float[rp.length];
            for (int i = 0; i < rp.length; i++) {
               rp[i] = path(root, spec.rotParts()[i]);
               br[i] = rp[i].xRot;
               ar[i] = path(adult, spec.rotParts()[i]).xRot;
            }
            int n = spec.copies() == null ? 0 : spec.copies().length;
            ModelPart[] cf = new ModelPart[n];
            ModelPart[] ct = new ModelPart[n];
            float[][] cr = new float[n][];
            for (int i = 0; i < n; i++) {
               cf[i] = path(root, spec.copies()[i][0]);
               ct[i] = path(root, spec.copies()[i][1]);
               cr[i] = new float[]{ct[i].xRot, ct[i].yRot, ct[i].zRot};
            }
            RIGS.put(m, new Rig(parts, bp, ap, rp, br, ar, cf, ct, cr, LOCK_ONLY.contains(spec.adult())));
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
