package com.backport.trial;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;

/** Data-driven trial spawner configuration, loaded from data/<ns>/trial_spawner/<path>.json. */
public final class TrialConfig {
   public static final TrialConfig DEFAULT = new TrialConfig();
   private static final Map<ResourceLocation, TrialConfig> CACHE = new HashMap<>();

   public static final class Spawn {
      public CompoundTag entity = new CompoundTag();
      public String equipmentLoot;
      public float dropChance = 0.0F;
      public int weight = 1;
   }

   public static final class Loot {
      public String table;
      public int weight = 1;
   }

   public int spawnRange = 4;
   public float totalMobs = 6.0F;
   public float simultaneousMobs = 2.0F;
   public float totalPerPlayer = 2.0F;
   public float simultaneousPerPlayer = 1.0F;
   public int ticksBetweenSpawn = 40;
   public List<Spawn> spawns = new ArrayList<>();
   public List<Loot> loot = new ArrayList<>();
   public String ominousDrops = "backport:spawners/trial_chamber/items_to_drop_when_ominous";

   public TrialConfig() {
      for (String t : new String[]{"backport:spawners/trial_chamber/consumables", "backport:spawners/trial_chamber/key"}) {
         Loot l = new Loot();
         l.table = t;
         this.loot.add(l);
      }
   }

   public int targetTotal(int extra) {
      return (int) Math.floor(this.totalMobs + this.totalPerPlayer * extra);
   }

   public int targetSimultaneous(int extra) {
      return (int) Math.floor(this.simultaneousMobs + this.simultaneousPerPlayer * extra);
   }

   public Spawn randomSpawn(RandomSource random) {
      return pick(this.spawns, random, s -> s.weight);
   }

   public Loot randomLoot(RandomSource random) {
      return pick(this.loot, random, l -> l.weight);
   }

   private static <T> T pick(List<T> list, RandomSource random, java.util.function.ToIntFunction<T> w) {
      if (list.isEmpty()) return null;
      int total = 0;
      for (T t : list) total += w.applyAsInt(t);
      int r = random.nextInt(Math.max(1, total));
      for (T t : list) {
         r -= w.applyAsInt(t);
         if (r < 0) return t;
      }
      return list.get(0);
   }

   public static void clearCache() {
      CACHE.clear();
   }

   public static TrialConfig load(MinecraftServer server, String id) {
      if (id == null || id.isEmpty()) return DEFAULT;
      ResourceLocation rl = new ResourceLocation(id);
      TrialConfig c = CACHE.get(rl);
      if (c != null) return c;
      ResourceLocation file = new ResourceLocation(rl.getNamespace(), "trial_spawner/" + rl.getPath() + ".json");
      try {
         var res = server.getResourceManager().getResource(file);
         if (res.isEmpty()) return DEFAULT;
         try (Reader r = res.get().openAsReader()) {
            c = parse(JsonParser.parseReader(r).getAsJsonObject());
         }
      } catch (Exception e) {
         com.backport.Backport.LOGGER.warn("Failed to load trial spawner config {}", id, e);
         return DEFAULT;
      }
      CACHE.put(rl, c);
      return c;
   }

   private static TrialConfig parse(JsonObject o) throws Exception {
      TrialConfig c = new TrialConfig();
      if (o.has("spawn_range")) c.spawnRange = o.get("spawn_range").getAsInt();
      if (o.has("total_mobs")) c.totalMobs = o.get("total_mobs").getAsFloat();
      if (o.has("simultaneous_mobs")) c.simultaneousMobs = o.get("simultaneous_mobs").getAsFloat();
      if (o.has("total_mobs_added_per_player")) c.totalPerPlayer = o.get("total_mobs_added_per_player").getAsFloat();
      if (o.has("simultaneous_mobs_added_per_player")) c.simultaneousPerPlayer = o.get("simultaneous_mobs_added_per_player").getAsFloat();
      if (o.has("ticks_between_spawn")) c.ticksBetweenSpawn = o.get("ticks_between_spawn").getAsInt();
      if (o.has("items_to_drop_when_ominous")) c.ominousDrops = o.get("items_to_drop_when_ominous").getAsString();
      if (o.has("loot_tables_to_eject")) {
         c.loot.clear();
         for (JsonElement e : o.getAsJsonArray("loot_tables_to_eject")) {
            Loot l = new Loot();
            l.table = e.getAsJsonObject().get("data").getAsString();
            l.weight = e.getAsJsonObject().has("weight") ? e.getAsJsonObject().get("weight").getAsInt() : 1;
            c.loot.add(l);
         }
      }
      if (o.has("spawn_potentials")) {
         for (JsonElement e : o.getAsJsonArray("spawn_potentials")) {
            JsonObject eo = e.getAsJsonObject();
            JsonObject data = eo.getAsJsonObject("data");
            Spawn s = new Spawn();
            s.weight = eo.has("weight") ? eo.get("weight").getAsInt() : 1;
            s.entity = TagParser.parseTag(data.getAsJsonObject("entity").toString());
            if (data.has("equipment")) {
               JsonObject eq = data.getAsJsonObject("equipment");
               s.equipmentLoot = eq.get("loot_table").getAsString();
               if (eq.has("slot_drop_chances") && eq.get("slot_drop_chances").isJsonPrimitive()) s.dropChance = eq.get("slot_drop_chances").getAsFloat();
            }
            c.spawns.add(s);
         }
      }
      return c;
   }
}
