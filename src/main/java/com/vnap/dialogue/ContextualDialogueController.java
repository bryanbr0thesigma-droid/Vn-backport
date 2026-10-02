package com.vnap.dialogue;

import com.vnap.config.VillagerNewsSettings;
import com.vnap.entity.VillagerNewsData;
import com.vnap.item.VillagerNewsItems;
import com.vnap.network.DialogueAnimationNetwork;
import com.vnap.network.HurtEffectNetwork;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents.StopSleeping;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.Unload;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopping;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.After;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public final class ContextualDialogueController {
   public static final String DIALOGUE_TEST_TAG = "vnap_dialogue_test";
   private static final double OBSERVER_RANGE = 16.0;
   private static final double NEARBY_SUBJECT_RANGE = 8.0;
   private static final long SHORT_COOLDOWN = 900L;
   private static final long LONG_COOLDOWN = 3000L;
   private static final Map<String, Long> COOLDOWNS = new HashMap<>();
   private static final Map<UUID, Long> BUSY_UNTIL = new HashMap<>();
   private static final Map<UUID, ContextualDialogueController.ActiveSound> ACTIVE_SOUNDS = new HashMap<>();
   private static final Map<UUID, ContextualDialogueController.PlayerObservation> PLAYER_OBSERVATIONS = new HashMap<>();
   private static final Map<UUID, Integer> PLAYER_DEATHS = new HashMap<>();
   private static final Map<UUID, Boolean> LAST_SLEEPING = new HashMap<>();
   private static final Map<UUID, Boolean> LAST_TRADER_INVISIBLE = new HashMap<>();
   private static final Map<UUID, ContextualDialogueController.VillagerSnapshot> VILLAGER_STATES = new HashMap<>();
   private static final Map<UUID, Set<String>> CONDITION_HISTORY = new HashMap<>();
   private static final Map<UUID, Integer> CONDITION_CURSORS = new HashMap<>();
   private static final Map<UUID, ContextualDialogueController.PendingConditionRelief> PENDING_CONDITION_RELIEF = new HashMap<>();
   private static final Map<UUID, Map<String, Integer>> VILLAGER_INVENTORIES = new HashMap<>();
   private static final Map<UUID, ContextualDialogueController.SpeechTarget> SPEECH_TARGETS = new HashMap<>();
   private static final Map<String, List<Integer>> SHARED_RECENT_VARIANTS = new HashMap<>();
   private static final Map<UUID, Long> NO_WORKSTATION_SINCE = new HashMap<>();
   private static final Map<UUID, Long> LAST_DANGER = new HashMap<>();
   private static final Map<UUID, Long> NO_BELL_SINCE = new HashMap<>();
   private static final Map<UUID, ContextualDialogueController.TradeSession> ACTIVE_TRADES = new HashMap<>();
   private static final Map<UUID, LastInteraction> LAST_INTERACTIONS = new HashMap<>();
   private static final Map<UUID, ContextualDialogueController.UnreachableState> UNREACHABLE_STATES = new HashMap<>();
   private static final Set<UUID> ACTIVE_PLAYER_ENCOUNTERS = new HashSet<>();
   private static final Map<UUID, ContextualDialogueController.PendingSleep> PENDING_SLEEP = new HashMap<>();
   private static final Map<UUID, ContextualDialogueController.PendingWake> PENDING_WAKE = new HashMap<>();
   private static final List<ContextualDialogueController.PendingBell> PENDING_BELLS = new ArrayList<>();
   private static final List<ContextualDialogueController.PendingBellReaction> PENDING_BELL_REACTIONS = new ArrayList<>();
   private static final Map<UUID, UUID> WAKE_SOURCES = new HashMap<>();
   private static final Set<UUID> SLEEP_BYPASS = new HashSet<>();
   private static final Set<UUID> INTERRUPTED_SLEEP = new HashSet<>();
   private static final List<ContextualDialogueController.PendingSpeech> PENDING_SPEECH = new ArrayList<>();
   private static final Map<String, Long> LAST_LEVEL_TIME = new HashMap<>();
   private static final Map<String, Difficulty> LAST_DIFFICULTY = new HashMap<>();
   private static final Map<String, Integer> PAIR_TICKS = new HashMap<>();
   private static final String NATURAL_SPECIAL_TAG = "vnap_natural_special";
   private static final String SPECIAL_OBJECTIVE = "vnap_special";
   private static final String SPECIAL_X_OBJECTIVE = "vnap_special_x";
   private static final String SPECIAL_Z_OBJECTIVE = "vnap_special_z";
   private static final Map<String, String> NATURAL_SPECIAL_NAMES = Map.of(
      "mayor",
      "The Mayor",
      "testificate",
      "Testificate Man",
      "number_5",
      "Villager #5",
      "number_9",
      "Villager #9",
      "unreachable",
      "Can't Catch Me!",
      "wooly",
      "Wooly The Sheep"
   );
   private static final Set<String> SPECIAL_NAMES = Set.of(
      "Mayor Villager", "The Mayor", "Testificate Man", "Villager #5", "Villager #9", "Villager Unreachable", "Can't Catch Me!", "Wooly The Sheep"
   );
   private static final Set<String> MOBILE_DIALOGUES = Set.of(
      "huhcbd",
      "gesjov",
      "gacgtq",
      "hmadgp",
      "nkcoqb",
      "qhpyaw",
      "uveohs",
      "caykki",
      "swomdw",
      "rtikom",
      "hfmwvf",
      "mytmrk",
      "ikrwzy",
      "fcbygh",
      "etkxko",
      "elryje",
      "rogpvp",
      "igebly",
      "vnaodx",
      "yzqpvi",
      "nsxmkr",
      "cifbit",
      "wyvzhk",
      "rueszy",
      "yjctyw",
      "qqyjjg",
      "hpnsfu",
      "vevdkl",
      "ahcvzd",
      "ecslqo",
      "ssbhiv",
      "ltdnvy",
      "fzoqwd",
      "behifz",
      "wrbvvp",
      "asuufu",
      "eyiraw",
      "ncyeaw",
      "uzdxum",
      "lpuocy",
      "slbqfwbayahw"
   );
   private static final Set<String> BABY_DIALOGUES = Set.of(
      "abfwiv",
      "aezdiy",
      "ahcvzd",
      "cmrqhw",
      "durjjd",
      "ecslqo",
      "fzyrfm",
      "ggitzq",
      "gotjxf",
      "gzsztp",
      "hbalps",
      "hcdvqm",
      "jfuftm",
      "lgjtnf",
      "mqnapy",
      "msemoe",
      "nxalcz",
      "qrdzmt",
      "rfnirh",
      "saxuwk",
      "svdjdk",
      "vbclem",
      "vhwksn",
      "wkwcrf",
      "wsxfok",
      "wtuguc",
      "zeykfp",
      "cxeziv",
      "riezum",
      "rlkdqd"
   );
   private static final Set<String> COSMETIC_RECIPIENT_DIALOGUES = Set.of("wurmgu", "inirxg", "ozxzla", "cxeziv", "riezum", "rlkdqd");
   private static final Set<String> ONGOING_DAMAGE_DIALOGUES = Set.of("elryje", "rogpvp", "etkxko", "igebly", "vnaodx");
   private static final Set<String> DAMAGE_LOCK_DIALOGUES = Set.of(
      "huhcbd",
      "gesjov",
      "gacgtq",
      "hmadgp",
      "nkcoqb",
      "qhpyaw",
      "uveohs",
      "caykki",
      "swomdw",
      "rtikom",
      "hfmwvf",
      "mytmrk",
      "ikrwzy",
      "fcbygh",
      "elryje",
      "rogpvp",
      "etkxko",
      "igebly",
      "vnaodx",
      "yzqpvi",
      "nsxmkr",
      "cifbit",
      "wyvzhk",
      "rueszy",
      "yjctyw",
      "qqyjjg",
      "hpnsfu",
      "vevdkl",
      "ahcvzd",
      "ecslqo",
      "ssbhiv",
      "ltdnvy",
      "fzoqwd",
      "behifz",
      "wrbvvp",
      "asuufu",
      "eyiraw",
      "ncyeaw",
      "onindz",
      "xemyaj",
      "yebifs"
   );
   private static final List<List<String>> WANDERING_CONVERSATIONS = List.of(
      List.of("gmrypkswxeva", "gmrypkbayahw", "gmrypkmudlec"),
      List.of("gmrypkoallbt", "gmrypkfobzlt", "gmrypkcljvls"),
      List.of("gmrypkhiqnpi", "gmrypkvkuidc", "gmrypkhnvsiu", "gmrypkvswnrg"),
      List.of("gmrypkmwtiaf", "gmrypkgougka")
   );
   private static final List<String> CAMPFIRE_CONVERSATION = List.of(
      "wrswgiswxeva", "wrswgibayahw", "wrswgimudlec", "wrswgitvewwu", "wrswgisrlwzw", "wrswgicsmkgk"
   );
   private static final List<String> GOSSIP_CONVERSATION = List.of("wrjbddswxeva", "wrjbddbayahw", "wrjbddmudlec", "wrjbddtvewwu", "wrjbddsrlwzw");
   private static final List<List<String>> ONE_MISSING_NOSE_CONVERSATIONS = List.of(
      List.of("bygaxwswxeva", "bygaxwbayahw"), List.of("bygaxwoallbt", "bygaxwfobzlt", "bygaxwcljvls"), List.of("bygaxwhiqnpi"), List.of("bygaxwmwtiaf")
   );
   private static final List<List<String>> TWO_MISSING_NOSES_CONVERSATIONS = List.of(
      List.of("loicswswxeva", "loicswbayahw"), List.of("loicswrotbcq"), List.of("loicswhiqnpi", "loicswvkuidc", "loicswhnvsiu")
   );
   private static final Map<String, String> NEARBY_ENTITY_DIALOGUES = Map.ofEntries(
      Map.entry("allay", "rnlher"),
      Map.entry("armor_stand", "ckniqq"),
      Map.entry("bat", "ozmthf"),
      Map.entry("bee", "rbkjsr"),
      Map.entry("cave_spider", "gtmfpl"),
      Map.entry("bogged", "nsosix"),
      Map.entry("camel", "turlrl"),
      Map.entry("cat", "ynxhfb"),
      Map.entry("chicken", "hggexx"),
      Map.entry("cow", "lvzfcv"),
      Map.entry("creaking", "nwlcij"),
      Map.entry("creeper", "odwhzm"),
      Map.entry("dolphin", "aqtshb"),
      Map.entry("drowned", "atwycp"),
      Map.entry("enderman", "yeqxvm"),
      Map.entry("frog", "pguaqp"),
      Map.entry("horse", "yazvzs"),
      Map.entry("husk", "gcoysc"),
      Map.entry("llama", "ysbfqu"),
      Map.entry("trader_llama", "ysbfqu"),
      Map.entry("panda", "swewsr"),
      Map.entry("parrot", "vapupl"),
      Map.entry("phantom", "nwzvkb"),
      Map.entry("pig", "jqdeef"),
      Map.entry("rabbit", "spfefr"),
      Map.entry("sheep", "vxycol"),
      Map.entry("skeleton", "lqzdqk"),
      Map.entry("slime", "rzvitn"),
      Map.entry("sniffer", "tqishj"),
      Map.entry("spider", "gtmfpl"),
      Map.entry("stray", "bxbibd"),
      Map.entry("turtle", "neoxpu"),
      Map.entry("warden", "jicosq"),
      Map.entry("witch", "lwcrnt"),
      Map.entry("wither", "satsrf"),
      Map.entry("wolf", "vvntcf"),
      Map.entry("zombie", "dortcb"),
      Map.entry("zombie_villager", "xtooxu"),
      Map.entry("zombified_piglin", "wboncy"),
      Map.entry("copper_golem", "ktdshy"),
      Map.entry("snow_golem", "kxjegd"),
      Map.entry("iron_golem", "cuchwi"),
      Map.entry("ender_dragon", "xxjkmo"),
      Map.entry("happy_ghast", "lxvofx"),
      Map.entry("polar_bear", "toolzx"),
      Map.entry("sulfur_cube", "dmcjmd"),
      Map.entry("cod", "trkugw"),
      Map.entry("salmon", "trkugw"),
      Map.entry("pufferfish", "trkugw"),
      Map.entry("tropical_fish", "trkugw")
   );
   private static final Map<String, String> BABY_ENTITY_DIALOGUES = Map.ofEntries(
      Map.entry("bee", "qqtnlm"),
      Map.entry("cat", "knjdbi"),
      Map.entry("chicken", "pjcwec"),
      Map.entry("cow", "hzahog"),
      Map.entry("drowned", "vakwgb"),
      Map.entry("horse", "ualabt"),
      Map.entry("husk", "hwltxk"),
      Map.entry("panda", "gggzar"),
      Map.entry("pig", "htibul"),
      Map.entry("sheep", "eccdga"),
      Map.entry("wolf", "hyzwpr"),
      Map.entry("zombie", "zvwapr"),
      Map.entry("zombified_piglin", "qltnkz"),
      Map.entry("zombie_villager", "nstwos")
   );
   private static long ticks;

   private ContextualDialogueController() {
   }

   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(ContextualDialogueController::tick);
      ServerEntityEvents.ENTITY_LOAD.register(ContextualDialogueController::onEntityLoad);
      ServerEntityEvents.ENTITY_UNLOAD.register((Unload)(entity, level) -> {
         UUID id = entity.getUUID();
         String encodedId = id.toString();
         BUSY_UNTIL.remove(id);
         ACTIVE_SOUNDS.remove(id);
         SPEECH_TARGETS.remove(id);
         LAST_SLEEPING.remove(id);
         LAST_TRADER_INVISIBLE.remove(id);
         VILLAGER_STATES.remove(id);
         CONDITION_HISTORY.remove(id);
         CONDITION_CURSORS.remove(id);
         PENDING_CONDITION_RELIEF.remove(id);
         VILLAGER_INVENTORIES.remove(id);
         NO_WORKSTATION_SINCE.remove(id);
         LAST_DANGER.remove(id);
         NO_BELL_SINCE.remove(id);
         PLAYER_OBSERVATIONS.remove(id);
         PLAYER_DEATHS.remove(id);
         ACTIVE_TRADES.remove(id);
         ACTIVE_PLAYER_ENCOUNTERS.remove(id);
         ACTIVE_TRADES.entrySet().removeIf(entry -> entry.getValue().traderId.equals(id));
         UNREACHABLE_STATES.remove(id);
         PENDING_SLEEP.remove(id);
         PENDING_WAKE.remove(id);
         WAKE_SOURCES.remove(id);
         SLEEP_BYPASS.remove(id);
         INTERRUPTED_SLEEP.remove(id);
         PENDING_SPEECH.removeIf(pending -> pending.speakerId.equals(id) || id.equals(pending.targetId));
         PAIR_TICKS.keySet().removeIf(pair -> pair.contains(encodedId));
      });
      PlayerBlockBreakEvents.AFTER
         .register(
            (After)(level, player, pos, state, blockEntity) -> {
               if (level instanceof ServerLevel serverLevel) {
                  if (queueFreedSuffocationRelief(serverLevel, pos)) {
                     return;
                  }

                  ContextualDialogueController.PlayerObservation observation = PLAYER_OBSERVATIONS.computeIfAbsent(
                     player.getUUID(), ignored -> new ContextualDialogueController.PlayerObservation()
                  );
                  String title = selectBreakContext(state, observation);
                  if (title.equals("Harvest Crops")
                     && nearbyVillagers(serverLevel, Vec3.atCenterOf(pos), 16.0).stream().anyMatch(villager -> profession(villager).equals("farmer"))) {
                     title = "Harvest Crops Near a Farmer";
                  }

                  playObserved(serverLevel, player, Vec3.atCenterOf(pos), title, 900L);
               }
            }
         );
      UseBlockCallback.EVENT
         .register(
            (UseBlockCallback)(player, level, hand, hitResult) -> {
               if (level instanceof ServerLevel serverLevel) {
                  ItemStack held = player.getItemInHand(hand);
                  BlockState clicked = level.getBlockState(hitResult.getBlockPos());
                  String clickedPath = BuiltInRegistries.BLOCK.getKey(clicked.getBlock()).getPath();
                  if (clickedPath.equals("bell")) {
                     queueBell(serverLevel, hitResult.getLocation());
                     return InteractionResult.PASS;
                  }

                  String title = selectHeldBlockContext(held, clicked);
                  if (title == null && held.getItem() instanceof BlockItem blockItem) {
                     title = selectPlaceContext(blockItem.getBlock(), serverLevel, hitResult.getBlockPos());
                  } else if (title == null) {
                     title = selectUseBlockContext(clicked);
                  }

                  if (clickedPath.endsWith("_door")
                     && clicked.hasProperty(BlockStateProperties.OPEN)
                     && (Boolean)clicked.getValue(BlockStateProperties.OPEN)
                     && !nearbyVillagers(serverLevel, hitResult.getLocation(), 3.0).isEmpty()) {
                     title = "Close a Door in a Villager's Face";
                  }

                  String heldPath = BuiltInRegistries.ITEM.getKey(held.getItem()).getPath();
                  if ((heldPath.equals("pumpkin") || heldPath.equals("carved_pumpkin")) && nearBlock(serverLevel, hitResult.getBlockPos(), "iron_block", 3)) {
                     title = "Build an Iron Golem Frame";
                  }

                  boolean played = (clickedPath.equals("chest") || clickedPath.equals("trapped_chest"))
                     && playHomeChestReaction(serverLevel, player, hitResult.getBlockPos());
                  if (!played) {
                     if (title != null && playObserved(serverLevel, player, hitResult.getLocation(), title, 900L)) {
                        boolean var13 = true;
                     } else {
                        boolean var10000 = false;
                     }
                  }
               }

               return InteractionResult.PASS;
            }
         );
      UseItemCallback.EVENT.register((UseItemCallback)(player, level, hand) -> {
         if (level instanceof ServerLevel serverLevel) {
            String title = selectUseItemContext(player.getItemInHand(hand));
            if (title != null) {
               playObserved(serverLevel, player, player.position(), title, 900L);
            }
         }

         return InteractionResultHolder.pass(player.getItemInHand(hand));
      });
      UseEntityCallback.EVENT
         .register(
            (UseEntityCallback)(player, level, hand, entity, hitResult) -> (InteractionResult)(level instanceof ServerLevel
               ? onUseEntityOnce(player, entity, hand)
               : InteractionResult.PASS)
         );
      AttackEntityCallback.EVENT
         .register(
            (AttackEntityCallback)(player, level, hand, entity, hitResult) -> {
               if (entity.getTags().contains("vnap_dialogue_test")) {
                  return InteractionResult.SUCCESS;
               } else if (level instanceof ServerLevel
                  && entity instanceof Villager villager
                  && cast(villager) == ContextualDialogueController.CastProfile.UNREACHABLE) {
                  repelPlayer(villager, player);
                  return InteractionResult.SUCCESS;
               } else {
                  if (level instanceof ServerLevel) {
                     onAttackEntity(player, entity);
                  }

                  return InteractionResult.PASS;
               }
            }
         );
      EntitySleepEvents.STOP_SLEEPING
         .register(
            (StopSleeping)(entity, sleepingPos) -> {
               if (entity instanceof ServerPlayer player) {
                  boolean armored = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)
                     .stream()
                     .anyMatch(slot -> !player.getItemBySlot(slot).isEmpty());
                  playObserved((ServerLevel)player.level(), player, player.position(), armored ? "Wake Up in Armor" : "Player Wakes Up", 3000L);
               } else if (entity instanceof Villager villager) {
                  UUID id = villager.getUUID();
                  boolean explicitlyInterrupted = INTERRUPTED_SLEEP.remove(id);
                  PENDING_SLEEP.remove(id);
                  SLEEP_BYPASS.remove(id);
                  interrupt(villager);
                  if (entity.level() instanceof ServerLevel level) {
                     boolean interrupted = explicitlyInterrupted || isVillagerSleepTime(villager, level);
                     String dialogue = interrupted ? "viwaal" : "ctptjt";
                     UUID targetId = interrupted ? WAKE_SOURCES.remove(id) : null;
                     PENDING_WAKE.put(id, new ContextualDialogueController.PendingWake(level, id, dialogue, targetId, ticks + 4L));
                     LAST_SLEEPING.put(id, false);
                  } else {
                     PENDING_WAKE.remove(id);
                     WAKE_SOURCES.remove(id);
                  }
               }
            }
         );
      ServerLivingEntityEvents.AFTER_DEATH.register(ContextualDialogueController::onDeath);
      ServerLifecycleEvents.SERVER_STOPPING.register((ServerStopping)server -> clearState());
   }

   private static void onEntityLoad(Entity entity, ServerLevel level) {
      if (!entity.getTags().contains("vnap_dialogue_test")) {
         normalizeSpecialEntity(entity);
         String entityPath = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
         if (entityPath.equals("firework_rocket")) {
            playFireworkReactions(level, entity);
         } else if (entityPath.equals("lightning_bolt")) {
            playLightningReaction(level, entity);
         } else if (entity instanceof Sheep sheep) {
            if (isWooly(sheep) && sheep.getColor() == DyeColor.RED) {
               sheep.setColor(DyeColor.WHITE);
            }
         } else if (entity instanceof Villager villager) {
            if (!tryCreateNaturalSpecial(villager, level)) {
               ensureSpecialTrade(villager);
               if (cast(villager) == ContextualDialogueController.CastProfile.UNREACHABLE) {
                  UNREACHABLE_STATES.putIfAbsent(villager.getUUID(), new ContextualDialogueController.UnreachableState(ticks));
               }

               VILLAGER_STATES.put(villager.getUUID(), snapshot(villager, false));
               VILLAGER_INVENTORIES.put(villager.getUUID(), inventoryCounts(villager));
               MobSpawnType reason = data(villager).vnap$spawnReason();
               if (reason == MobSpawnType.SPAWN_EGG || reason == MobSpawnType.DISPENSER) {
                  ServerPlayer player = nearestPlayer(level, villager.position(), 12.0);
                  PENDING_SPEECH.add(
                     new ContextualDialogueController.PendingSpeech(
                        level, villager.getUUID(), villager.isBaby() ? "abfwiv" : "vskjkl", player == null ? null : player.getUUID(), ticks + 2L
                     )
                  );
               } else if (reason == MobSpawnType.BREEDING) {
                  Villager parent = nearbyVillagers(level, villager.position(), 12.0)
                     .stream()
                     .filter(other -> other != villager && !other.isBaby())
                     .min(Comparator.comparingDouble(other -> other.distanceToSqr(villager)))
                     .orElse(null);
                  if (parent != null) {
                     PENDING_SPEECH.add(new ContextualDialogueController.PendingSpeech(level, parent.getUUID(), "fbuabj", villager.getUUID(), ticks + 2L, true));
                     PENDING_SPEECH.add(
                        new ContextualDialogueController.PendingSpeech(
                           level, villager.getUUID(), "lgjtnf", parent.getUUID(), ticks + DialogueCatalog.byId("fbuabj").durationTicks() + 4L
                        )
                     );
                  } else {
                     PENDING_SPEECH.add(new ContextualDialogueController.PendingSpeech(level, villager.getUUID(), "lgjtnf", null, ticks + 2L));
                  }
               } else if (reason == MobSpawnType.CONVERSION) {
                  PENDING_SPEECH.add(
                     new ContextualDialogueController.PendingSpeech(level, villager.getUUID(), villager.isBaby() ? "ggitzq" : "ivumgm", null, ticks + 2L)
                  );
               }
            }
         }
      }
   }

   public static void onBabySpawnedFromEgg(Villager villager, Player player) {
      if (villager.level() instanceof ServerLevel level && villager.isBaby() && !villager.getTags().contains("vnap_dialogue_test")) {
         UUID id = villager.getUUID();
         PENDING_SPEECH.removeIf(pending -> pending.speakerId.equals(id) || id.equals(pending.targetId));
         PENDING_SPEECH.add(new ContextualDialogueController.PendingSpeech(level, id, "abfwiv", player.getUUID(), ticks + 2L));
      }
   }

   private static void processPendingSpeech() {
      PENDING_SPEECH.removeIf(
         pending -> {
            if (pending.dueTick > ticks) {
               return false;
            } else {
               Entity speaker = pending.level.getEntity(pending.speakerId);
               Entity target = pending.targetId == null ? null : pending.level.getEntity(pending.targetId);
               if (speaker instanceof LivingEntity living && living.isAlive()) {
                  DialogueCatalog.DialogueGroup group = DialogueCatalog.byId(pending.dialogueId);
                  boolean played = group != null
                     && (
                        pending.sharedAdult
                           ? playSharedId(living, pending.dialogueId, "queued:" + living.getUUID() + ":" + pending.dialogueId, 1L, target)
                           : playId(living, pending.dialogueId, "queued:" + living.getUUID() + ":" + pending.dialogueId, 1L, target)
                     );
                  if (played && target instanceof LivingEntity listener) {
                     holdListener(listener, living, group.durationTicks());
                  }
               }

               return true;
            }
         }
      );
   }

   private static void maintainSpeechTargets(MinecraftServer server) {
      SPEECH_TARGETS.entrySet().removeIf(entry -> {
         ContextualDialogueController.SpeechTarget speech = entry.getValue();
         if (speech.untilTick <= ticks) {
            return true;
         } else {
            for (ServerLevel level : server.getAllLevels()) {
               if (level.getEntity(entry.getKey()) instanceof Mob mob && mob.isAlive()) {
                  Entity target = speech.targetId == null ? null : level.getEntity(speech.targetId);
                  Vec3 position = target != null && target.isAlive() ? target.getEyePosition() : speech.position;
                  if (speech.lockMovement) {
                     holdMob(mob, position);
                  } else {
                     faceMob(mob, position);
                  }

                  return false;
               }
            }

            return true;
         }
      });
   }

   private static void processTradeSessions(MinecraftServer server) {
      ACTIVE_TRADES.entrySet()
         .removeIf(
            entry -> {
               ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
               ContextualDialogueController.TradeSession session = entry.getValue();
               if (player != null && player.containerMenu instanceof MerchantMenu) {
                  if (!session.opened) {
                     session.opened = true;
                     Entity trader = session.level.getEntity(session.traderId);
                     if (trader instanceof Villager villager) {
                        String id = tradeOpeningId(villager, player);
                        playId(villager, id, "trade_open:" + villager.getUUID() + ":" + id, 900L, player);
                     } else if (trader instanceof WanderingTrader wanderingTrader) {
                        playId(wanderingTrader, "yubpbb", "trade_open:" + wanderingTrader.getUUID(), 900L, player);
                     }
                  }

                  return false;
               } else if (!session.opened && ticks - session.createdTick <= 20L) {
                  return false;
               } else {
                  if (!session.opened && player != null && session.level.getEntity(session.traderId) instanceof Villager villager) {
                     String id = unavailableTradeId(villager, player);
                     if (id != null) {
                        playId(villager, id, "trade_unavailable:" + villager.getUUID() + ":" + id, 900L, player);
                     }
                  }

                  if (session.opened) {
                     Entity trader = session.level.getEntity(session.traderId);
                     if (trader instanceof Villager villagerx) {
                        ContextualDialogueController.CastProfile profile = cast(villagerx);

                        String id = switch (profile) {
                           case MAYOR -> session.completed ? "shrrya" : "bgzmea";
                           case TESTIFICATE_MAN -> session.completed ? "xcjort" : "rdugrl";
                           case NUMBER_5 -> session.completed ? "msofrj" : "lilimm";
                           case NUMBER_9 -> session.completed ? "czvvwy" : "lilimm";
                           default -> session.completed ? "czvvwy" : (ticks % 2L == 0L ? "lilimm" : "laztau");
                        };
                        boolean played = playId(villagerx, id, "trade_close:" + villagerx.getUUID(), 10L, player);
                        if (!played) {
                           String fallback = profile == ContextualDialogueController.CastProfile.TESTIFICATE_MAN
                              ? "ctzfzj"
                              : (
                                 profile == ContextualDialogueController.CastProfile.NUMBER_5
                                    ? "nfdery"
                                    : (profile == ContextualDialogueController.CastProfile.NUMBER_9 ? "hvjfnk" : null)
                              );
                           if (fallback != null) {
                              playId(villagerx, fallback, "trade_close_fallback:" + villagerx.getUUID(), 10L, player);
                           }
                        }
                     } else if (trader instanceof WanderingTrader wanderingTrader) {
                        playId(wanderingTrader, session.completed ? "uzdvsi" : "erbcfn", "trade_close:" + wanderingTrader.getUUID(), 10L, player);
                     }
                  }

                  return true;
               }
            }
         );
   }

   private static String tradeOpeningId(Villager villager, Player player) {
      ContextualDialogueController.CastProfile profile = cast(villager);
      if (profile != ContextualDialogueController.CastProfile.VILLAGER) {
         return profile.trade;
      } else {
         String unavailable = unavailableTradeId(villager, player);
         if (unavailable != null) {
            return unavailable;
         } else {
            int reputation = villager.getPlayerReputation(player);
            if (reputation < -225) {
               return "xduuwm";
            } else if (reputation < -75) {
               return "qmdvft";
            } else if (reputation >= 75) {
               return "vlrsrn";
            } else {
               return reputation >= 25 ? "kuhvdv" : profile.trade;
            }
         }
      }
   }

   private static String unavailableTradeId(Villager villager, Player player) {
      if (cast(villager) != ContextualDialogueController.CastProfile.VILLAGER) {
         return null;
      } else {
         String profession = profession(villager);
         if (profession.equals("nitwit")) {
            return "nukxsf";
         } else if (profession.equals("none")) {
            return "nlbhku";
         } else if (villager.level() instanceof ServerLevel level && level.isRaided(villager.blockPosition())) {
            return "klabhl";
         } else if (villager.getOffers().isEmpty()) {
            return "zalmof";
         } else {
            return villager.getPlayerReputation(player) <= -150 ? "lhdgsy" : null;
         }
      }
   }

   private static ServerPlayer nearestPlayer(ServerLevel level, Vec3 position, double range) {
      return level.players()
         .stream()
         .filter(LivingEntity::isAlive)
         .filter(player -> player.distanceToSqr(position) <= range * range)
         .min(Comparator.comparingDouble(player -> player.distanceToSqr(position)))
         .orElse(null);
   }

   private static void processTimeChange(ServerLevel level) {
      String key = level.dimension().location().toString();
      long now = level.getDayTime();
      Long before = LAST_LEVEL_TIME.put(key, now);
      if (before != null && Math.abs(now - before) > 40L && !level.players().isEmpty()) {
         ServerPlayer player = (ServerPlayer)level.players().get(0);
         Villager speaker = nearbyVillagers(level, player.position(), 32.0)
            .stream()
            .filter(villager -> !villager.isSleeping())
            .min(Comparator.comparingDouble(villager -> villager.distanceToSqr(player)))
            .orElse(null);
         if (speaker != null) {
            boolean wasDay = Math.floorMod(before, 24000L) < 12000L;
            boolean isDay = Math.floorMod(now, 24000L) < 12000L;
            String id = wasDay == isDay
               ? (speaker.isBaby() ? "durjjd" : "uqwdqn")
               : (isDay ? (speaker.isBaby() ? "wkwcrf" : "mgmzeh") : (speaker.isBaby() ? "msemoe" : "ohdwnz"));
            playSharedId(speaker, id, "time_skip:" + key, 900L, player);
         }
      }
   }

   private static void processDifficultyChange(ServerLevel level) {
      String key = level.dimension().location().toString();
      Difficulty difficulty = level.getDifficulty();
      Difficulty previous = LAST_DIFFICULTY.put(key, difficulty);
      if (previous != null && previous != difficulty && !level.players().isEmpty()) {
         ServerPlayer player = (ServerPlayer)level.players().get(0);
         Villager speaker = nearbyVillagers(level, player.position(), 32.0)
            .stream()
            .filter(villager -> !villager.isBaby() && !villager.isSleeping())
            .min(Comparator.comparingDouble(villager -> villager.distanceToSqr(player)))
            .orElse(null);
         if (speaker != null) {
            String id = difficulty == Difficulty.HARD ? "arzojk" : (difficulty == Difficulty.PEACEFUL ? "xuyypm" : "ibcrvx");
            playSharedId(speaker, id, "difficulty:" + key + ":" + difficulty.name(), 900L, player);
         }
      }
   }

   private static void tick(MinecraftServer server) {
      if (false) {
         stopActiveDialogue(server);
      } else {
         ticks++;
         processUnreachableVillagers(server);
         if (!VillagerNewsSettings.dialogueEnabled()) {
            stopActiveDialogue(server);
            ACTIVE_PLAYER_ENCOUNTERS.clear();
         } else {
            ACTIVE_SOUNDS.entrySet().removeIf(entry -> entry.getValue().endTick <= ticks);
            maintainSpeechTargets(server);
            processPendingSleep();
            processPendingConditionRelief();
            processPendingWake();
            processPendingSpeech();
            processPendingBells();
            processTradeSessions(server);
            if (ticks % 10L == 0L) {
               for (ServerLevel level : server.getAllLevels()) {
                  processTimeChange(level);
                  processDifficultyChange(level);

                  for (ServerPlayer player : level.players()) {
                     if (player.gameMode.getGameModeForPlayer().getName().equals("spectator")) {
                        ACTIVE_PLAYER_ENCOUNTERS.remove(player.getUUID());
                        ContextualDialogueController.PlayerObservation observation = PLAYER_OBSERVATIONS.computeIfAbsent(
                           player.getUUID(), ignored -> new ContextualDialogueController.PlayerObservation()
                        );
                        observation.lastPosition = player.position();
                        observation.lastGameMode = "spectator";
                        observation.lastPlayerContext = null;
                        observation.stillTicks = 0;
                        observation.stareTicks = 0;
                     } else {
                        processPlayer(level, player);
                        processWanderingTrader(level, player);
                        processWooly(level, player);
                     }
                  }

                  if (ticks % 100L == 0L) {
                     processConversations(level);
                  }
               }

               if (ticks % 1200L == 0L) {
                  COOLDOWNS.entrySet().removeIf(entry -> entry.getValue() + 12000L < ticks);
                  BUSY_UNTIL.entrySet().removeIf(entry -> entry.getValue() < ticks);
               }
            }
         }
      }
   }

   private static void clearState() {
      COOLDOWNS.clear();
      BUSY_UNTIL.clear();
      ACTIVE_SOUNDS.clear();
      PLAYER_OBSERVATIONS.clear();
      PLAYER_DEATHS.clear();
      LAST_SLEEPING.clear();
      LAST_TRADER_INVISIBLE.clear();
      VILLAGER_STATES.clear();
      CONDITION_HISTORY.clear();
      CONDITION_CURSORS.clear();
      PENDING_CONDITION_RELIEF.clear();
      VILLAGER_INVENTORIES.clear();
      SPEECH_TARGETS.clear();
      SHARED_RECENT_VARIANTS.clear();
      NO_WORKSTATION_SINCE.clear();
      LAST_DANGER.clear();
      NO_BELL_SINCE.clear();
      ACTIVE_TRADES.clear();
      LAST_INTERACTIONS.clear();
      UNREACHABLE_STATES.clear();
      ACTIVE_PLAYER_ENCOUNTERS.clear();
      PENDING_SLEEP.clear();
      PENDING_WAKE.clear();
      PENDING_BELLS.clear();
      PENDING_BELL_REACTIONS.clear();
      WAKE_SOURCES.clear();
      SLEEP_BYPASS.clear();
      INTERRUPTED_SLEEP.clear();
      PENDING_SPEECH.clear();
      LAST_LEVEL_TIME.clear();
      LAST_DIFFICULTY.clear();
      PAIR_TICKS.clear();
      ticks = 0L;
   }

   private static void processPendingWake() {
      PENDING_WAKE.entrySet().removeIf(entry -> {
         ContextualDialogueController.PendingWake pending = entry.getValue();
         if (pending.dueTick > ticks) {
            return false;
         } else if (!(pending.level.getEntity(pending.villagerId) instanceof Villager villager && villager.isAlive() && !villager.isSleeping())) {
            return true;
         } else if (isBusy(villager)) {
            return false;
         } else {
            Entity target = pending.targetId == null ? null : pending.level.getEntity(pending.targetId);
            return playSharedId(villager, pending.dialogueId, "wake:" + pending.villagerId + ":" + pending.dialogueId, 1L, target);
         }
      });
   }

   private static void queueBell(ServerLevel level, Vec3 position) {
      boolean queued = PENDING_BELLS.stream()
         .anyMatch(pending -> pending.level == level && pending.position.distanceToSqr(position) < 0.25 && pending.dueTick > ticks);
      if (!queued) {
         PENDING_BELLS.add(new ContextualDialogueController.PendingBell(level, position, ticks + 15L));
      }
   }

   private static void processPendingBells() {
      PENDING_BELLS.removeIf(
         pending -> {
            if (pending.dueTick > ticks) {
               return false;
            } else {
               for (Villager villager : nearbyVillagers(pending.level, pending.position, 50.0)) {
                  if (!villager.isSleeping() && cast(villager) == ContextualDialogueController.CastProfile.VILLAGER) {
                     long dueTick = ticks + ThreadLocalRandom.current().nextInt(5);
                     PENDING_BELL_REACTIONS.add(
                        new ContextualDialogueController.PendingBellReaction(pending.level, villager.getUUID(), pending.position, dueTick, dueTick + 10L)
                     );
                  }
               }

               return true;
            }
         }
      );
      PENDING_BELL_REACTIONS.removeIf(
         pending -> {
            if (pending.dueTick > ticks) {
               return false;
            } else if (!(
               pending.level.getEntity(pending.villagerId) instanceof Villager villager
                  && villager.isAlive()
                  && !villager.isSleeping()
                  && cast(villager) == ContextualDialogueController.CastProfile.VILLAGER
            )) {
               return true;
            } else if (isBusy(villager)) {
               return ticks >= pending.expireTick;
            } else {
               String id = villager.isBaby() ? "nxalcz" : "kljgyu";
               return playId(villager, id, "bell:" + pending.dueTick + ":" + villager.getUUID(), 1L, pending.position) || ticks >= pending.expireTick;
            }
         }
      );
   }

   private static boolean isVillagerSleepTime(Villager villager, ServerLevel level) {
      long time = Math.floorMod(level.getDayTime(), 24000L);
      return profession(villager).equals("nitwit") ? time >= 14000L || time < 2000L : time >= 12000L;
   }

   private static void processPendingSleep() {
      PENDING_SLEEP.entrySet()
         .removeIf(
            entry -> {
               ContextualDialogueController.PendingSleep pending = entry.getValue();
               ContextualDialogueController.ActiveSound active = ACTIVE_SOUNDS.get(entry.getKey());
               if (active != null && active.endTick >= pending.dueTick) {
                  pending.dueTick = active.endTick + 1L;
               }

               if (pending.dueTick > ticks) {
                  return false;
               } else {
                  if (pending.level.getEntity(entry.getKey()) instanceof Villager villager
                     && villager.isAlive()
                     && !villager.isSleeping()
                     && villager.distanceToSqr(Vec3.atCenterOf(pending.bedPos)) <= 16.0
                     && BuiltInRegistries.BLOCK.getKey(pending.level.getBlockState(pending.bedPos).getBlock()).getPath().endsWith("_bed")) {
                     if (!pending.bedtimeStarted) {
                        DialogueCatalog.DialogueGroup group = DialogueCatalog.byId("ioxtmt");
                        if (group != null && play(villager, group, "bed:" + villager.getUUID(), 3000L, null, Vec3.atCenterOf(pending.bedPos))) {
                           ContextualDialogueController.ActiveSound bedtime = ACTIVE_SOUNDS.get(villager.getUUID());
                           pending.bedtimeStarted = true;
                           pending.dueTick = bedtime == null ? ticks + group.durationTicks() : bedtime.endTick + 1L;
                           return false;
                        }
                     }

                     SLEEP_BYPASS.add(villager.getUUID());

                     try {
                        villager.startSleeping(pending.bedPos);
                     } finally {
                        SLEEP_BYPASS.remove(villager.getUUID());
                     }
                  }

                  return true;
               }
            }
         );
   }

   public static boolean delayVillagerSleep(Villager villager, BlockPos bedPos) {
      UUID id = villager.getUUID();
      if (SLEEP_BYPASS.remove(id)) {
         return false;
      } else if (!(villager.level() instanceof ServerLevel level && !villager.isSleeping())) {
         return false;
      } else if (PENDING_SLEEP.containsKey(id)) {
         return true;
      } else {
         ContextualDialogueController.ActiveSound active = ACTIVE_SOUNDS.get(id);
         if (active != null && active.endTick > ticks) {
            PENDING_SLEEP.put(id, new ContextualDialogueController.PendingSleep(level, bedPos.immutable(), active.endTick + 1L, false));
            LAST_SLEEPING.put(id, false);
            return true;
         } else {
            DialogueCatalog.DialogueGroup group = DialogueCatalog.byId("ioxtmt");
            if (group != null && play(villager, group, "bed:" + id, 3000L, null, Vec3.atCenterOf(bedPos))) {
               ContextualDialogueController.ActiveSound bedtime = ACTIVE_SOUNDS.get(id);
               long dueTick = bedtime == null ? ticks + group.durationTicks() : bedtime.endTick + 1L;
               PENDING_SLEEP.put(id, new ContextualDialogueController.PendingSleep(level, bedPos.immutable(), dueTick, true));
               LAST_SLEEPING.put(id, false);
               return true;
            } else {
               return false;
            }
         }
      }
   }

   private static void stopActiveDialogue(MinecraftServer server) {
      for (UUID id : List.copyOf(ACTIVE_SOUNDS.keySet())) {
         LivingEntity speaker = null;

         for (ServerLevel level : server.getAllLevels()) {
            if (level.getEntity(id) instanceof LivingEntity living) {
               speaker = living;
               break;
            }
         }

         if (speaker != null) {
            interrupt(speaker);
         } else {
            ACTIVE_SOUNDS.remove(id);
            BUSY_UNTIL.remove(id);
            SPEECH_TARGETS.remove(id);
         }
      }
   }

   private static void processWanderingTrader(ServerLevel level, ServerPlayer player) {
      AABB area = AABB.ofSize(player.position(), 32.0, 16.0, 32.0);
      WanderingTrader trader = level.getEntitiesOfClass(WanderingTrader.class, area, Entity::isAlive)
         .stream()
         .filter(candidate -> candidate.hasLineOfSight(player))
         .min(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(player)))
         .orElse(null);
      if (trader != null) {
         String pair = player.getUUID() + ":" + trader.getUUID();
         if (!playId(trader, "hxlyuc", "approach:" + pair, 3000L, player)) {
            boolean invisible = trader.hasEffect(MobEffects.INVISIBILITY);
            boolean wasInvisible = LAST_TRADER_INVISIBLE.put(trader.getUUID(), invisible) == Boolean.TRUE;
            if (invisible) {
               long llamas = level.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(trader.position(), 24.0, 12.0, 24.0), Entity::isAlive)
                  .stream()
                  .filter(entity -> BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath().equals("trader_llama"))
                  .count();
               String id = wasInvisible ? "dbzjqi" : (llamas >= 2L ? "myajyt" : (llamas == 1L ? "jkeahu" : "vggdrt"));
               if (playId(trader, id, "invisible:" + trader.getUUID() + ":" + id, 3000L, player)) {
                  return;
               }
            }

            if (!level.isRainingAt(trader.blockPosition()) || !playId(trader, "kxoqky", "rain:" + trader.getUUID(), 3000L)) {
               if (ticks % 100L == 0L && trader.getDeltaMovement().horizontalDistanceSqr() > 4.0E-4) {
                  playId(trader, "stqafd", "idle:" + trader.getUUID(), 3000L);
               }
            }
         }
      }
   }

   private static void processWooly(ServerLevel level, ServerPlayer player) {
      AABB area = AABB.ofSize(player.position(), 32.0, 16.0, 32.0);
      Sheep wooly = level.getEntitiesOfClass(
            Sheep.class, area, sheep -> sheep.isAlive() && isWooly(sheep) && !sheep.getTags().contains("vnap_dialogue_test")
         )
         .stream()
         .filter(candidate -> candidate.hasLineOfSight(player))
         .min(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(player)))
         .orElse(null);
      if (wooly != null) {
         String pair = player.getUUID() + ":" + wooly.getUUID();
         if (!playId(wooly, "uvtocs", "approach:" + pair, 3000L, player)) {
            if (ticks % 100L == 0L && wooly.getDeltaMovement().horizontalDistanceSqr() > 4.0E-4) {
               playId(wooly, "vmohcm", "idle:" + wooly.getUUID(), 3000L);
            }
         }
      }
   }

   private static void processUnreachableVillagers(MinecraftServer server) {
      Set<UUID> processed = new HashSet<>();

      for (ServerLevel level : server.getAllLevels()) {
         for (ServerPlayer player : level.players()) {
            AABB area = player.getBoundingBox().inflate(16.0);

            for (Villager villager : level.getEntitiesOfClass(
               Villager.class, area, candidate -> candidate.isAlive() && cast(candidate) == ContextualDialogueController.CastProfile.UNREACHABLE
            )) {
               if (!processed.contains(villager.getUUID())) {
                  ServerPlayer nearest = nearestPlayer(level, villager.position(), 16.0);
                  if (nearest != null) {
                     processed.add(villager.getUUID());
                     updateUnreachable(villager, nearest);
                  }
               }
            }
         }
      }

      UNREACHABLE_STATES.entrySet()
         .removeIf(
            entry -> {
               if (processed.contains(entry.getKey())) {
                  return false;
               } else {
                  for (ServerLevel levelx : server.getAllLevels()) {
                     if (levelx.getEntity(entry.getKey()) instanceof Villager villagerx
                        && villagerx.isAlive()
                        && cast(villagerx) == ContextualDialogueController.CastProfile.UNREACHABLE) {
                        villagerx.getNavigation().stop();
                        entry.getValue().stopFleeing(ticks);
                        return false;
                     }
                  }

                  return true;
               }
            }
         );
   }

   private static void updateUnreachable(Villager villager, ServerPlayer player) {
      ContextualDialogueController.UnreachableState state = UNREACHABLE_STATES.computeIfAbsent(
         villager.getUUID(), ignored -> new ContextualDialogueController.UnreachableState(ticks)
      );
      state.updateFleeing(ticks);
      double distance = villager.distanceTo(player);
      if (distance <= 4.0) {
         teleportUnreachable(villager, player);
      }

      villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      if (ticks >= state.nextPathTick || villager.getNavigation().isDone()) {
         Vec3 destination = DefaultRandomPos.getPosAway(villager, 16, 7, player.position());
         if (destination == null || destination.distanceToSqr(player.position()) <= villager.distanceToSqr(player)) {
            Vec3 away = villager.position().subtract(player.position());
            if (away.horizontalDistanceSqr() < 1.0E-4) {
               double angle = villager.getRandom().nextDouble() * Math.PI * 2.0;
               away = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
            } else {
               away = new Vec3(away.x, 0.0, away.z).normalize();
            }

            destination = villager.position().add(away.scale(16.0));
         }

         villager.getNavigation().moveTo(destination.x, destination.y, destination.z, 0.9);
         state.nextPathTick = ticks + 10L;
      }

      if (distance <= 12.0) {
         faceMob(villager, player.getEyePosition());
      }

      if (state.canTaunt(ticks) && playId(villager, "eltxge", "unreachable_taunt:" + villager.getUUID(), 1L, player)) {
         state.taunted(ticks);
      }
   }

   private static void repelPlayer(Villager villager, Player player) {
      if (villager.distanceToSqr(player) <= 16.0) {
         teleportUnreachable(villager, player);
      }

      if (player instanceof ServerPlayer serverPlayer && villager.distanceTo(player) <= 16.0) {
         updateUnreachable(villager, serverPlayer);
      }
   }

   private static boolean teleportUnreachable(Villager villager, Player player) {
      Vec3 away = villager.position().subtract(player.position());
      double baseAngle = away.horizontalDistanceSqr() < 1.0E-4 ? villager.getRandom().nextDouble() * Math.PI * 2.0 : Math.atan2(away.z, away.x);

      for (int attempt = 0; attempt < 24; attempt++) {
         double angle = baseAngle + (villager.getRandom().nextDouble() - 0.5) * Math.PI * 0.75;
         double distance = 14.0 + villager.getRandom().nextDouble() * 4.0;
         double x = villager.getX() + Math.cos(angle) * distance;
         double y = villager.getY() + villager.getRandom().nextInt(11) - 5.0;
         double z = villager.getZ() + Math.sin(angle) * distance;
         if (villager.randomTeleport(x, y, z, true)) {
            villager.getNavigation().stop();
            return true;
         }
      }

      return false;
   }

   private static void processPlayer(ServerLevel level, ServerPlayer player) {
      ContextualDialogueController.PlayerObservation observation = PLAYER_OBSERVATIONS.computeIfAbsent(
         player.getUUID(), ignored -> new ContextualDialogueController.PlayerObservation()
      );
      String gameMode = player.gameMode.getGameModeForPlayer().getName();
      boolean changedGameMode = observation.lastGameMode != null && !observation.lastGameMode.equals(gameMode);
      observation.lastGameMode = gameMode;
      Vec3 movement = player.position().subtract(observation.lastPosition);
      if (movement.horizontalDistanceSqr() < 4.0E-4 && Math.abs(movement.y) < 0.01) {
         observation.stillTicks += 10;
      } else {
         observation.stillTicks = 0;
      }

      observation.lastPosition = player.position();
      BlockPos ground = player.blockPosition().below();
      String groundBlock = BuiltInRegistries.BLOCK.getKey(level.getBlockState(ground).getBlock()).getPath();
      if (ground.equals(observation.lastGroundPos) && observation.lastGroundBlock.equals("farmland") && groundBlock.equals("dirt")) {
         playObserved(level, player, player.position(), "Trample Crops", 900L);
      }

      observation.lastGroundPos = ground;
      observation.lastGroundBlock = groundBlock;
      List<Villager> nearby = nearbyVillagers(level, player.position(), 16.0).stream().filter(villager -> !villager.isSleeping()).toList();

      for (Villager villager : nearby) {
         processConditionDialogues(villager);
      }

      Villager adult = nearby.stream()
         .filter(villager -> !villager.isBaby())
         .filter(villager -> cast(villager) != ContextualDialogueController.CastProfile.UNREACHABLE)
         .filter(villager -> villager.hasLineOfSight(player))
         .min(Comparator.comparingDouble(villager -> villager.distanceToSqr(player)))
         .orElse(null);
      if (adult == null) {
         Villager baby = nearby.stream()
            .filter(AgeableMob::isBaby)
            .filter(villager -> cast(villager) != ContextualDialogueController.CastProfile.UNREACHABLE)
            .filter(villager -> villager.hasLineOfSight(player))
            .min(Comparator.comparingDouble(villager -> villager.distanceToSqr(player)))
            .orElse(null);
         if (baby != null) {
            boolean firstNotice = ACTIVE_PLAYER_ENCOUNTERS.add(player.getUUID());
            if (ticks % 40L == 0L && playNearbyEntityContext(level, baby)) {
               return;
            }

            String id = player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) ? "fzyrfm" : (isNegativeReputation(baby, player) ? "jfuftm" : "wtuguc");
            if (firstNotice) {
               playId(baby, id, "player_greeting:" + player.getUUID(), 3000L, player);
            }
         } else {
            ACTIVE_PLAYER_ENCOUNTERS.remove(player.getUUID());
         }
      } else {
         boolean firstNoticex = ACTIVE_PLAYER_ENCOUNTERS.add(player.getUUID());
         String pair = player.getUUID() + ":" + adult.getUUID();
         if (!playCosmeticObservation(player, adult)) {
            if (player.getBoundingBox().inflate(0.15).intersects(adult.getBoundingBox()) && movement.horizontalDistanceSqr() > 0.002) {
               String id = adult.getVehicle() != null && BuiltInRegistries.ENTITY_TYPE.getKey(adult.getVehicle().getType()).getPath().contains("boat")
                  ? "zvbnea"
                  : "ajexrq";
               if (playId(adult, id, "nudge:" + adult.getUUID(), 900L, player)) {
                  return;
               }
            }

            if (!changedGameMode
               || !playSharedId(adult, gameMode.equals("creative") ? "ohtblt" : "fhhqxg", "gamemode:" + player.getUUID() + ":" + gameMode, 900L, player)) {
               ContextualDialogueController.CastProfile adultProfile = cast(adult);
               if (!firstNoticex
                  || adultProfile == ContextualDialogueController.CastProfile.VILLAGER
                  || !player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE)
                  || !playSharedId(adult, "gnetsk", "player_greeting:" + player.getUUID(), 3000L, player)) {
                  String approach = adultProfile == ContextualDialogueController.CastProfile.VILLAGER
                     ? reputationApproach(adult, player)
                     : adultProfile.approach;
                  if (!firstNoticex || !playId(adult, approach, "player_greeting:" + player.getUUID(), 3000L, player)) {
                     Vec3 toVillager = adult.getEyePosition().subtract(player.getEyePosition()).normalize();
                     double lookDot = player.getLookAngle().dot(toVillager);
                     if (lookDot > 0.985) {
                        observation.stareTicks += 10;
                     } else {
                        observation.stareTicks = 0;
                     }

                     if (observation.stareTicks >= 60 && playSharedTitle(adult, "Stare at a Villager", "stare:" + pair, 3000L, player)) {
                        observation.stareTicks = 0;
                     } else if (ticks % 400L == 0L
                        && observation.stillTicks >= 2400
                        && playSharedTitle(adult, "Stand Completely Still", "still:" + player.getUUID(), 3000L, player)) {
                        observation.stillTicks = 0;
                     } else {
                        String playerContext = playerContext(player, adult);
                        boolean changedPlayerContext = playerContext != null && !playerContext.equals(observation.lastPlayerContext);
                        observation.lastPlayerContext = playerContext;
                        if (!changedPlayerContext
                           || !playSharedTitle(adult, playerContext, "player_context:" + player.getUUID() + ":" + playerContext, 3000L, player)) {
                           long nearbyPlayers = level.players().stream().filter(other -> other.distanceToSqr(adult) <= 64.0).count();
                           if (nearbyPlayers < 2L || !playSharedId(adult, "cstyvg", "player_crowd:" + adult.getUUID(), 3000L, player)) {
                              String environment = environmentContext(level, adult);
                              if (environment == null || !playTitle(adult, environment, "environment:" + adult.getUUID() + ":" + environment, 3000L)) {
                                 if (ticks % 40L != 0L || !playNearbyEntityContext(level, adult)) {
                                    if (ticks % 200L == 0L) {
                                       String time = timeContext(level, adult);
                                       if (playTitle(adult, time, "time:" + adult.getUUID() + ":" + time, 3000L)) {
                                          return;
                                       }
                                    }

                                    if (ticks % 100L == 0L && adult.getDeltaMovement().horizontalDistanceSqr() > 4.0E-4) {
                                       ContextualDialogueController.CastProfile profile = cast(adult);
                                       if (profile != ContextualDialogueController.CastProfile.VILLAGER) {
                                          playId(adult, profile.idle, "idle:" + adult.getUUID(), 3000L);
                                       } else {
                                          String ambient = ambientDialogue(adult);
                                          playId(adult, ambient, "idle:" + adult.getUUID() + ":" + ambient, 3000L);
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static String environmentContext(ServerLevel level, Villager villager) {
      Entity vehicle = villager.getVehicle();
      if (vehicle != null) {
         String vehiclePath = BuiltInRegistries.ENTITY_TYPE.getKey(vehicle.getType()).getPath();
         if (vehiclePath.contains("minecart")) {
            return vehicle.getDeltaMovement().horizontalDistanceSqr() > 0.001 ? "Ride in a Moving Minecart" : "Sit in a Minecart";
         }

         if (vehiclePath.contains("boat")) {
            if (ticks / 3000L % 3L == 0L) {
               return "Sit in a Boat";
            }

            return vehicle.isInWater() ? "Boat on Water" : "Boat on Land";
         }
      }

      if (level.dimension() == Level.NETHER) {
         return "Wander in the Nether";
      } else if (level.dimension() == Level.END) {
         return "Wander in the End";
      } else if (level.dimension() != Level.OVERWORLD) {
         return "Wander in Another Dimension";
      } else if (level.isRainingAt(villager.blockPosition())) {
         return "Caught in the Rain";
      } else if (villager.isInWater()) {
         return "Stand in Shallow Water";
      } else {
         String biome = level.getBiome(villager.blockPosition()).unwrapKey().map(key -> key.location().getPath()).orElse("");
         if (biome.contains("desert") || biome.contains("badlands") || biome.contains("savanna")) {
            return "Wander Somewhere Hot";
         } else if (!biome.contains("snow") && !biome.contains("frozen") && !biome.contains("ice") && !biome.contains("cold")) {
            BlockState below = level.getBlockState(villager.blockPosition().below());
            if (below.is(Blocks.ICE) || below.is(Blocks.PACKED_ICE) || below.is(Blocks.BLUE_ICE)) {
               return "Stand on Ice";
            } else if (below.is(Blocks.SNOW_BLOCK) || below.is(Blocks.POWDER_SNOW)) {
               return "Stand on Snow";
            } else if (below.is(Blocks.MAGMA_BLOCK)) {
               return "Stand on Magma";
            } else {
               for (int x = -3; x <= 3; x++) {
                  for (int y = -2; y <= 2; y++) {
                     for (int z = -3; z <= 3; z++) {
                        String block = BuiltInRegistries.BLOCK.getKey(level.getBlockState(villager.blockPosition().offset(x, y, z)).getBlock()).getPath();
                        if (block.contains("campfire")) {
                           return "See a Campfire";
                        }

                        if (block.equals("fire") || block.equals("soul_fire")) {
                           return "Stand Near Fire";
                        }

                        if (block.equals("bookshelf") && villager.getDeltaMovement().horizontalDistanceSqr() < 4.0E-4) {
                           return "Inspect Bookshelves";
                        }
                     }
                  }
               }

               if (villager.getY() < level.getSeaLevel() - 30) {
                  return "Wander Deep Underground";
               } else {
                  return villager.getY() > level.getSeaLevel() + 75 ? "Wander High Above the Ground" : null;
               }
            }
         } else {
            return "Wander Somewhere Cold";
         }
      }
   }

   private static String timeContext(ServerLevel level, Villager villager) {
      float sunAngle = (float)Math.floorMod(level.getDayTime(), 24000L) / 24000.0F;
      if (sunAngle < 0.125F || sunAngle >= 0.875F) {
         return "Morning";
      } else if (sunAngle < 0.45F) {
         return "Afternoon";
      } else {
         return sunAngle < 0.625F ? "Evening" : "Night";
      }
   }

   private static String ambientDialogue(Villager villager) {
      String profession = profession(villager);
      if (profession.equals("nitwit")) {
         return "uookqp";
      } else if (profession.equals("none")) {
         return "gbxzxv";
      } else {
         LocalDate date = LocalDate.now();
         List<String> choices = new ArrayList<>();
         if (date.getMonthValue() == 10) {
            choices.add("mltyge");
         }

         if (date.getMonthValue() == 12) {
            choices.add("tkkegl");
         }

         if (date.getMonthValue() == 1 && date.getDayOfMonth() == 1) {
            choices.add("uyqiwv");
         }

         if (date.getMonthValue() == 2 && date.getDayOfMonth() == 14) {
            choices.add("fabiyx");
         }

         if (date.getMonthValue() == 4 && date.getDayOfMonth() == 1) {
            choices.add("obitls");
         }

         if (date.getMonthValue() == 5 && date.getDayOfMonth() == 17) {
            choices.add("iriuqa");
         }

         if (date.getMonthValue() == 10 && date.getDayOfMonth() == 31) {
            choices.add("adhxce");
         }

         if (date.getMonthValue() == 12 && date.getDayOfMonth() == 24) {
            choices.add("zoqxvy");
         }

         if (date.getMonthValue() == 12 && date.getDayOfMonth() == 25) {
            choices.add("rclyrl");
         }

         if (date.getMonthValue() == 12 && date.getDayOfMonth() == 31) {
            choices.add("xljknt");
         }

         if (date.getDayOfMonth() == 13 && date.getDayOfWeek() == DayOfWeek.FRIDAY) {
            choices.add("qfcwvz");
         }

         if (LocalDateTime.now().getMinute() == 0) {
            choices.add("jqgkhy");
         }

         if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            choices.add("bkyidl");
            choices.add("ckngck");
         }
         choices.add(switch (date.getDayOfWeek()) {
            case MONDAY -> ticks / 3000L % 2L == 0L ? "gkvlqc" : "jpucos";
            case TUESDAY -> ticks / 3000L % 2L == 0L ? "dkpihl" : "lgeeem";
            case WEDNESDAY -> ticks / 3000L % 2L == 0L ? "gwakiz" : "qiqiez";
            case THURSDAY -> ticks / 3000L % 2L == 0L ? "zglkgp" : "caiyte";
            case FRIDAY -> ticks / 3000L % 2L == 0L ? "ypyumu" : "cxtvsx";
            case SATURDAY -> ticks / 3000L % 2L == 0L ? "ildosa" : "lfhnxz";
            case SUNDAY -> ticks / 3000L % 2L == 0L ? "uzvatl" : "zckxrc";
         });
         if (choices.size() == 1) {
            choices.add("lvigit");
         }

         return choices.get(Math.floorMod((int)(ticks / 3000L), choices.size()));
      }
   }

   private static String playerContext(ServerPlayer player, Villager villager) {
      if (player.isFallFlying()) {
         return "Glide with Elytra";
      } else if (player.gameMode.getGameModeForPlayer().getName().equals("creative") && player.getAbilities().flying) {
         return "Fly in Creative Mode";
      } else if (player.isShiftKeyDown() && player.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4) {
         return "Crouch-Walk";
      } else if (player.getHealth() <= player.getMaxHealth() * 0.3F) {
         return "Low Health";
      } else if (player.hasEffect(MobEffects.INVISIBILITY)) {
         return "Invisibility";
      } else if (player.hasEffect(MobEffects.DARKNESS)) {
         return "Darkness";
      } else if (player.hasEffect(MobEffects.NIGHT_VISION)) {
         return "Night Vision";
      } else if (player.hasEffect(MobEffects.WATER_BREATHING)) {
         return "Water Breathing";
      } else if (player.hasEffect(MobEffects.MOVEMENT_SPEED)) {
         return "Swiftness";
      } else if (player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
         return "Slowness";
      } else if (player.hasEffect(MobEffects.DAMAGE_BOOST)) {
         return "Strength";
      } else if (player.hasEffect(MobEffects.WEAKNESS)) {
         return "Weakness";
      } else if (player.hasEffect(MobEffects.HUNGER)) {
         return "Hunger";
      } else if (player.hasEffect(MobEffects.CONFUSION)) {
         return "Nausea";
      } else if (player.hasEffect(MobEffects.BAD_OMEN)) {
         return "Bad Omen";
      } else if (player.getActiveEffects().size() >= 2) {
         return "Multiple Status Effects";
      } else if (BuiltInRegistries.BLOCK.getKey(player.level().getBlockState(player.blockPosition().below()).getBlock()).getPath().endsWith("_bed")) {
         return "Stand on a Villager's Bed";
      } else {
         ItemStack held = player.getMainHandItem();
         if (held.isDamageableItem() && held.getDamageValue() >= held.getMaxDamage() * 0.85F) {
            return "Hold a Nearly Broken Item";
         } else {
            int armor = 0;
            Set<String> armorMaterials = new HashSet<>();

            for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
               ItemStack stack = player.getItemBySlot(slot);
               if (!stack.isEmpty()) {
                  armor++;
                  String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
                  armorMaterials.add(path.substring(0, path.indexOf(95) > 0 ? path.indexOf(95) : path.length()));
               }
            }

            if (armor == 4 && armorMaterials.size() == 1 && armorMaterials.contains("iron")) {
               return "Wear Full Iron Armor";
            } else if (armor == 4 && armorMaterials.size() > 1) {
               return "Wear Mixed Armor";
            } else if (armor != 4 || !armorMaterials.contains("diamond") && !armorMaterials.contains("netherite")) {
               return armor > 0 ? "Wear Armor" : null;
            } else {
               return "Wear High-Level Armor";
            }
         }
      }
   }

   private static String reputationApproach(Villager villager, ServerPlayer player) {
      if (player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE)) {
         return "gnetsk";
      } else {
         int reputation = villager.getPlayerReputation(player);
         if (reputation < -225) {
            return BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).getPath().endsWith("_sword") ? "stuirs" : "zstdjn";
         } else if (reputation < -75) {
            return "tfzlsw";
         } else if (reputation >= 75) {
            return "kcbenk";
         } else {
            return reputation >= 25 ? "omgcte" : "xfpjxq";
         }
      }
   }

   private static boolean isNegativeReputation(Villager villager, ServerPlayer player) {
      return villager.getPlayerReputation(player) < -75;
   }

   private static boolean playCosmeticObservation(ServerPlayer player, Villager villager) {
      ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
      ItemStack held = player.getMainHandItem();
      ContextualDialogueController.CastProfile profile = cast(villager);
      String id = null;
      if (head.getItem() == VillagerNewsItems.VILLAGER_NOSE) {
         id = "kejscw";
      } else if (head.getItem() == VillagerNewsItems.MAYOR_HAT && profile == ContextualDialogueController.CastProfile.MAYOR) {
         id = "cmkesu";
      } else if (head.getItem() == VillagerNewsItems.TESTIFICATE_MAN_HELMET && profile == ContextualDialogueController.CastProfile.TESTIFICATE_MAN) {
         id = "rooiup";
      } else if (head.getItem() == VillagerNewsItems.MOUSTACHE && profile == ContextualDialogueController.CastProfile.NUMBER_5) {
         id = "mjyhgw";
      } else if (held.getItem() == VillagerNewsItems.MICROPHONE && profile == ContextualDialogueController.CastProfile.NUMBER_9) {
         id = "adhvqz";
      }

      if (id == null) {
         return false;
      } else {
         String key = "player_cosmetic:" + villager.getUUID() + ":" + player.getUUID() + ":" + id;
         return id.equals("kejscw") ? playSharedId(villager, id, key, 3000L, player) : playId(villager, id, key, 3000L, player);
      }
   }

   private static void processConversations(ServerLevel level) {
      Set<UUID> updatedVillagers = new HashSet<>();

      for (ServerPlayer player : level.players()) {
         for (Villager villager : nearbyVillagers(level, player.position(), 24.0)) {
            if (cast(villager) != ContextualDialogueController.CastProfile.UNREACHABLE && updatedVillagers.add(villager.getUUID())) {
               processVillagerState(villager);
            }
         }
      }

      Set<String> checkedPairs = new HashSet<>();

      for (ServerPlayer player : level.players()) {
         List<Villager> villagers = nearbyVillagers(level, player.position(), 24.0)
            .stream()
            .filter(villagerx -> !villagerx.isBaby() && !villagerx.isSleeping())
            .filter(villagerx -> cast(villagerx) != ContextualDialogueController.CastProfile.UNREACHABLE)
            .filter(villagerx -> activeConditionDialogues(villagerx).isEmpty())
            .toList();

         for (Villager subject : villagers) {
            if (data(subject).vnap$cosmetic() != 0) {
               Villager witness = villagers.stream()
                  .filter(other -> other != subject && other.hasLineOfSight(subject))
                  .min(Comparator.comparingDouble(other -> other.distanceToSqr(subject)))
                  .orElse(null);
               if (witness != null) {
                  String id = cast(witness) == ContextualDialogueController.CastProfile.TESTIFICATE_MAN && data(subject).vnap$cosmetic() == 2
                     ? "pbbywc"
                     : "anrhns";
                  if (playSharedId(witness, id, "cosmetic_witness:" + witness.getUUID() + ":" + subject.getUUID() + ":" + id, 3000L, subject)) {
                     return;
                  }
               }
            }
         }

         if (villagers.size() >= 8 && playSharedId((LivingEntity)villagers.get(0), "kzemrz", "villager_crowd:" + player.getUUID(), 3000L, player)) {
            return;
         }

         Villager gatheringSpeaker = villagers.stream()
            .filter(villagerx -> cast(villagerx) == ContextualDialogueController.CastProfile.VILLAGER)
            .findFirst()
            .orElse(null);
         Villager gatheringTarget = gatheringSpeaker == null ? null : nearestConversationPartner(gatheringSpeaker, villagers);
         if (gatheringTarget != null && villagers.size() >= 3 && playId(gatheringSpeaker, "ebfifz", "gathering:" + player.getUUID(), 3000L, gatheringTarget)) {
            return;
         }

         for (Villager candidate : villagers) {
            Villager partner = nearestConversationPartner(candidate, villagers);
            if (partner != null) {
               String pair = orderedPair(candidate.getUUID(), partner.getUUID());
               if (checkedPairs.add(pair)) {
                  Villager first = candidate.getUUID().compareTo(partner.getUUID()) <= 0 ? candidate : partner;
                  Villager second = first == candidate ? partner : candidate;
                  if (!isBusy(first) && !isBusy(second)) {
                     int togetherTicks = PAIR_TICKS.merge(pair, 100, Integer::sum);
                     if (togetherTicks >= 200) {
                        boolean firstHasNose = data(first).vnap$hasNose();
                        boolean secondHasNose = data(second).vnap$hasNose();
                        if (firstHasNose && secondHasNose) {
                           ContextualDialogueController.CastProfile firstCast = cast(first);
                           ContextualDialogueController.CastProfile secondCast = cast(second);
                           String meetId = meetDialogue(firstCast == ContextualDialogueController.CastProfile.VILLAGER ? secondCast : firstCast);
                           if (meetId == null
                              || firstCast != ContextualDialogueController.CastProfile.VILLAGER
                                 && secondCast != ContextualDialogueController.CastProfile.VILLAGER) {
                              boolean atCampfire = nearBlock(level, first.blockPosition(), "campfire", 4)
                                 && nearBlock(level, second.blockPosition(), "campfire", 4);
                              boolean negativeGossip = isNegativeReputation(first, player) || isNegativeReputation(second, player);
                              String conversation = atCampfire
                                 ? CAMPFIRE_CONVERSATION.get(0)
                                 : (
                                    first.getVehicle() != null && first.getVehicle() == second.getVehicle()
                                       ? "zqfvby"
                                       : (
                                          negativeGossip
                                                && first.getDeltaMovement().horizontalDistanceSqr() + second.getDeltaMovement().horizontalDistanceSqr()
                                                   < 4.0E-4
                                             ? (Math.floorMod(pair.hashCode() + (int)(ticks / 3000L), 2) == 0 ? "wrjbdd" : GOSSIP_CONVERSATION.get(0))
                                             : wanderingConversation(pair).get(0)
                                       )
                                 );
                              if (playId(first, conversation, "conversation:" + pair + ":" + conversation, 3000L, second)) {
                                 holdListener(second, first, DialogueCatalog.byId(conversation).durationTicks());
                                 if (conversation.startsWith("gmrypk")) {
                                    queueWanderingConversation(level, first, second, wanderingConversation(pair));
                                 } else if (atCampfire) {
                                    queueConversation(level, first, second, CAMPFIRE_CONVERSATION);
                                 } else if (conversation.equals(GOSSIP_CONVERSATION.get(0))) {
                                    queueConversation(level, first, second, GOSSIP_CONVERSATION);
                                 }

                                 PAIR_TICKS.put(pair, 0);
                                 return;
                              }
                              continue;
                           }

                           Villager speaker = firstCast == ContextualDialogueController.CastProfile.VILLAGER ? first : second;
                           Villager subjectx = speaker == first ? second : first;
                           if (playId(speaker, meetId, "meet:" + pair, 3000L, subjectx)) {
                              holdListener(subjectx, speaker, 80L);
                              PAIR_TICKS.put(pair, 0);
                           }

                           return;
                        }

                        boolean bothMissing = !firstHasNose && !secondHasNose;
                        Villager conversationSpeaker = !bothMissing && !firstHasNose ? second : first;
                        Villager conversationSubject = conversationSpeaker == first ? second : first;
                        List<List<String>> choices = bothMissing ? TWO_MISSING_NOSES_CONVERSATIONS : ONE_MISSING_NOSE_CONVERSATIONS;
                        List<String> sequence = choices.get(Math.floorMod(pair.hashCode() + (int)(ticks / 3000L), choices.size()));
                        if (playId(
                           conversationSpeaker, sequence.get(0), "nose_conversation:" + pair + ":" + sequence.get(0), 3000L, conversationSubject
                        )) {
                           holdListener(conversationSubject, conversationSpeaker, DialogueCatalog.byId(sequence.get(0)).durationTicks());
                           queueConversation(level, conversationSpeaker, conversationSubject, sequence);
                           PAIR_TICKS.put(pair, 0);
                        }

                        return;
                     }
                  }
               }
            }
         }

         Villager adult = villagers.stream().findFirst().orElse(null);
         List<Villager> babies = nearbyVillagers(level, player.position(), 24.0)
            .stream()
            .filter(villagerx -> villagerx.isBaby() && !villagerx.isSleeping())
            .toList();
         Villager baby = babies.stream().findFirst().orElse(null);
         if (babies.size() >= 2
            && babies.get(0).distanceToSqr((Entity)babies.get(1)) <= 64.0
            && babies.get(0).getDeltaMovement().horizontalDistanceSqr() + babies.get(1).getDeltaMovement().horizontalDistanceSqr() > 0.01) {
            playId(
               (LivingEntity)babies.get(0),
               "rfnirh",
               "baby_chase:" + orderedPair(babies.get(0).getUUID(), babies.get(1).getUUID()),
               3000L,
               (Entity)babies.get(1)
            );
         }

         if (adult != null && baby != null && adult.distanceToSqr(baby) <= 64.0) {
            playSharedId(adult, "pbmrxx", "see_baby:" + adult.getUUID() + ":" + baby.getUUID(), 3000L, baby);
         }
      }
   }

   private static List<String> wanderingConversation(String pair) {
      return WANDERING_CONVERSATIONS.get(Math.floorMod(pair.hashCode() + (int)(ticks / 3000L), WANDERING_CONVERSATIONS.size()));
   }

   private static void queueWanderingConversation(ServerLevel level, Villager first, Villager second, List<String> sequence) {
      queueConversation(level, first, second, sequence);
   }

   private static void queueConversation(ServerLevel level, Villager first, Villager second, List<String> sequence) {
      long due = ticks + DialogueCatalog.byId(sequence.get(0)).durationTicks() + 2L;

      for (int index = 1; index < sequence.size(); index++) {
         Villager speaker = index % 2 == 1 ? second : first;
         Villager target = speaker == first ? second : first;
         String id = sequence.get(index);
         PENDING_SPEECH.add(new ContextualDialogueController.PendingSpeech(level, speaker.getUUID(), id, target.getUUID(), due));
         due += DialogueCatalog.byId(id).durationTicks() + 2L;
      }
   }

   private static Villager nearestConversationPartner(Villager villager, List<Villager> candidates) {
      return candidates.stream()
         .filter(candidate -> candidate != villager && !isBusy(candidate))
         .filter(candidate -> villager.distanceToSqr(candidate) <= 6.25 && villager.hasLineOfSight(candidate))
         .min(Comparator.comparingDouble(villager::distanceToSqr))
         .orElse(null);
   }

   private static boolean nearBlock(ServerLevel level, BlockPos origin, String pathPart, int range) {
      for (int x = -range; x <= range; x++) {
         for (int y = -2; y <= 2; y++) {
            for (int z = -range; z <= range; z++) {
               String path = BuiltInRegistries.BLOCK.getKey(level.getBlockState(origin.offset(x, y, z)).getBlock()).getPath();
               if (path.contains(pathPart)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private static boolean playHomeChestReaction(ServerLevel level, Player player, BlockPos chestPos) {
      boolean bedNearby = nearBlock(level, chestPos, "bed", 6);
      return nearbyVillagers(level, Vec3.atCenterOf(chestPos), 16.0)
         .stream()
         .filter(villager -> !villager.isBaby() && !villager.isSleeping() && cast(villager) == ContextualDialogueController.CastProfile.VILLAGER)
         .filter(villager -> bedNearby || homeMatches(villager, chestPos, 12))
         .filter(villager -> villager.hasLineOfSight(player))
         .min(Comparator.comparingDouble(villager -> villager.distanceToSqr(player)))
         .map(villager -> playId(villager, "qfhrlh", "home_chest:" + villager.getUUID(), 900L, player))
         .orElse(false);
   }

   private static boolean homeMatches(Villager villager, BlockPos pos, int range) {
      return villager.getBrain().getMemory(MemoryModuleType.HOME).map(home -> home.dimension() == villager.level().dimension() && home.pos().closerThan(pos, range)).orElse(false);
   }

   private static void playFireworkReactions(ServerLevel level, Entity firework) {
      List<Villager> witnesses = nearbyVillagers(level, firework.position(), 16.0)
         .stream()
         .filter(villager -> !villager.isSleeping())
         .filter(villager -> villager.hasLineOfSight(firework))
         .toList();
      witnesses.stream()
         .filter(villager -> !villager.isBaby())
         .min(Comparator.comparingDouble(villager -> villager.distanceToSqr(firework)))
         .ifPresent(villager -> playSharedId(villager, "dfdkli", "firework_spawn:" + villager.getUUID(), 900L, firework));
      witnesses.stream()
         .filter(AgeableMob::isBaby)
         .min(Comparator.comparingDouble(villager -> villager.distanceToSqr(firework)))
         .ifPresent(villager -> playId(villager, "zeykfp", "firework_seen:" + villager.getUUID(), 900L, firework));
   }

   private static void playLightningReaction(ServerLevel level, Entity lightning) {
      nearbyVillagers(level, lightning.position(), 128.0)
         .stream()
         .filter(villager -> !villager.isBaby() && !villager.isSleeping() && villager.hasLineOfSight(lightning))
         .min(Comparator.comparingDouble(villager -> villager.distanceToSqr(lightning)))
         .ifPresent(villager -> playSharedId(villager, "ikrwzy", "lightning:" + villager.getUUID(), 900L, lightning));
   }

   private static void processVillagerState(Villager villager) {
      ensureSpecialTrade(villager);
      ContextualDialogueController.VillagerSnapshot previous = VILLAGER_STATES.get(villager.getUUID());
      BlockPos workstation = findWorkstation(villager);
      ContextualDialogueController.VillagerSnapshot current = snapshot(villager, workstation != null);
      Map<String, Integer> oldInventory = VILLAGER_INVENTORIES.get(villager.getUUID());
      boolean sleeping = villager.isSleeping();
      boolean wasSleeping = LAST_SLEEPING.getOrDefault(villager.getUUID(), sleeping);
      if (sleeping) {
         if (wasSleeping) {
            playTitle(villager, "Sleeping", "sleeping:" + villager.getUUID(), 3000L);
         }

         LAST_SLEEPING.put(villager.getUUID(), true);
         VILLAGER_INVENTORIES.put(villager.getUUID(), inventoryCounts(villager));
         VILLAGER_STATES.put(villager.getUUID(), current);
      } else {
         if (wasSleeping) {
            playSharedTitle(villager, "Wake Up Naturally", "wake:" + villager.getUUID(), 3000L, null);
         }

         LAST_SLEEPING.put(villager.getUUID(), false);
         ItemStack pickedUp = findNewItem(villager, oldInventory);
         if (pickedUp != null) {
            String path = BuiltInRegistries.ITEM.getKey(pickedUp.getItem()).getPath();
            boolean armor = path.endsWith("_helmet") || path.endsWith("_chestplate") || path.endsWith("_leggings") || path.endsWith("_boots");
            String id = armor ? (pickedUp.isEnchanted() ? "habfnx" : "zjwpzi") : "dxmmiu";
            boolean food = path.equals("beetroot") || path.equals("bread") || path.equals("carrot") || path.equals("potato");
            Villager donor = villager.level() instanceof ServerLevel level
               ? nearbyVillagers(level, villager.position(), 4.0)
                  .stream()
                  .filter(other -> other != villager)
                  .min(Comparator.comparingDouble(other -> other.distanceToSqr(villager)))
                  .orElse(null)
               : null;
            ServerPlayer nearbyPlayer = villager.level() instanceof ServerLevel levelx ? nearestPlayer(levelx, villager.position(), 6.0) : null;
            if (villager.isBaby() && donor != null && food && playId(donor, "locuih", "share_food:" + donor.getUUID(), 900L, villager)) {
               long due = ticks + DialogueCatalog.byId("locuih").durationTicks() + 2L;
               PENDING_SPEECH.add(
                  new ContextualDialogueController.PendingSpeech((ServerLevel)villager.level(), villager.getUUID(), "saxuwk", donor.getUUID(), due)
               );
            } else if (donor != null && !armor) {
               playId(villager, "ujyxfg", "pickup_villager:" + villager.getUUID() + ":" + path, 900L, donor);
            } else if (nearbyPlayer != null) {
               playId(villager, armor ? id : "vkhrme", "pickup_player:" + villager.getUUID() + ":" + path, 900L, nearbyPlayer);
            } else {
               playId(villager, id, "pickup:" + villager.getUUID() + ":" + path, 900L);
            }
         }

         VILLAGER_INVENTORIES.put(villager.getUUID(), inventoryCounts(villager));
         if (previous != null) {
            if (previous.baby && !current.baby) {
               playId(villager, "smvnbj", "grow:" + villager.getUUID(), 1L);
            } else if ((previous.profession.equals("none") || previous.profession.equals("nitwit"))
               && !current.profession.equals("none")
               && !current.profession.equals("nitwit")) {
               playId(villager, "zndzjx", "job:" + villager.getUUID(), 1L);
            } else if (current.level > previous.level) {
               playId(villager, current.level >= 5 ? "pnvkfy" : "fltegg", "level:" + villager.getUUID() + ":" + current.level, 1L);
            } else if (!current.name.isEmpty() && !current.name.equals(previous.name)) {
               String lower = current.name.toLowerCase(Locale.ROOT);
               String id = current.baby
                  ? (lower.equals("dragon") ? "cmrqhw" : "gzsztp")
                  : (lower.equals("dinnerbone") ? "qmpcxi" : (!lower.equals("jeb") && !lower.equals("jeb_") ? "spfsrr" : "armupg"));
               playId(villager, id, "name:" + villager.getUUID() + ":" + current.name, 1L);
            }

            if (!previous.working && current.working) {
               playId(villager, "qawras", "work_start:" + villager.getUUID(), 3000L, workstation == null ? null : Vec3.atCenterOf(workstation));
            } else if (current.working) {
               String work = ticks / 3000L % 3L == 0L ? "sdhkke" : professionWorkDialogue(current.profession);
               if (work != null) {
                  playId(villager, work, "work:" + villager.getUUID() + ":" + work, 3000L, workstation == null ? null : Vec3.atCenterOf(workstation));
               }
            }
         }

         processConditionDialogues(villager);
         if (!current.profession.equals("none") && !current.profession.equals("nitwit") && workstation == null) {
            long since = NO_WORKSTATION_SINCE.computeIfAbsent(villager.getUUID(), ignored -> ticks);
            if (ticks - since >= 600L) {
               playId(villager, "ywzhwz", "missing_workstation:" + villager.getUUID(), 3000L);
               NO_WORKSTATION_SINCE.put(villager.getUUID(), ticks);
            }
         } else {
            NO_WORKSTATION_SINCE.remove(villager.getUUID());
         }

         if (!villager.isBaby() && !villager.getBrain().hasMemoryValue(MemoryModuleType.MEETING_POINT)) {
            long since = NO_BELL_SINCE.computeIfAbsent(villager.getUUID(), ignored -> ticks);
            if (ticks - since >= 1200L) {
               playId(villager, "trphsn", "missing_bell:" + villager.getUUID(), 3000L);
               NO_BELL_SINCE.put(villager.getUUID(), ticks);
            }
         } else {
            NO_BELL_SINCE.remove(villager.getUUID());
         }

         if (profession(villager).equals("farmer") && nearCrops(villager.level(), villager.blockPosition(), 4)) {
            playId(villager, "aobqjt", "farming:" + villager.getUUID(), 3000L);
         }

         if (!villager.isSleeping() && villager.getDeltaMovement().horizontalDistanceSqr() > 4.0E-4 && villager.level() instanceof ServerLevel level) {
            if (!data(villager).vnap$hasNose()) {
               playId(villager, "dcvgnm", "no_nose_wander:" + villager.getUUID(), 3000L);
            }

            float sunAngle = (float)Math.floorMod(level.getDayTime(), 24000L) / 24000.0F;
            if (sunAngle >= 0.5F && sunAngle < 0.85F) {
               String id = level.dimension() == Level.END
                  ? "iubjul"
                  : (
                     level.dimension() == Level.NETHER
                        ? "bvtmmz"
                        : (level.dimension() != Level.OVERWORLD ? "uhbigm" : (villager.getBrain().hasMemoryValue(MemoryModuleType.HOME) ? "wkfbuv" : "uqguqj"))
                  );
               playId(villager, id, "return_home:" + villager.getUUID() + ":" + id, 3000L);
            }
         }

         long danger = LAST_DANGER.getOrDefault(villager.getUUID(), -4611686018427387904L);
         if (ticks - danger >= 100L && ticks - danger <= 200L) {
            playId(villager, villager.isBaby() ? "wsxfok" : "wbbxpo", "calm:" + villager.getUUID(), 3000L);
         }

         if (villager.isBaby() && villager.getDeltaMovement().horizontalDistanceSqr() > 0.02) {
            boolean weekend = LocalDate.now().getDayOfWeek() == DayOfWeek.SATURDAY || LocalDate.now().getDayOfWeek() == DayOfWeek.SUNDAY;
            playId(villager, weekend ? "vbclem" : "vhwksn", "baby_sprint:" + villager.getUUID(), 3000L);
         }

         VILLAGER_STATES.put(villager.getUUID(), current);
      }
   }

   private static Map<String, Integer> inventoryCounts(Villager villager) {
      Map<String, Integer> counts = new HashMap<>();

      for (ItemStack stack : inventoryStacks(villager)) {
         if (!stack.isEmpty()) {
            counts.merge(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath(), stack.getCount(), Integer::sum);
         }
      }

      return counts;
   }

   private static ItemStack findNewItem(Villager villager, Map<String, Integer> previous) {
      if (previous == null) {
         return null;
      } else {
         Map<String, Integer> current = inventoryCounts(villager);

         for (ItemStack stack : inventoryStacks(villager)) {
            if (!stack.isEmpty()) {
               String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
               if (current.getOrDefault(path, 0) > previous.getOrDefault(path, 0)) {
                  return stack;
               }
            }
         }

         return null;
      }
   }

   private static ContextualDialogueController.VillagerSnapshot snapshot(Villager villager, boolean working) {
      String name = villager.hasCustomName() && villager.getCustomName() != null ? villager.getCustomName().getString() : "";
      return new ContextualDialogueController.VillagerSnapshot(
         villager.isBaby(),
         profession(villager),
         villager.getVillagerData().getLevel(),
         working,
         name,
         villager.hasEffect(MobEffects.POISON),
         villager.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
         villager.hasEffect(MobEffects.WEAKNESS),
         villager.isInWall()
      );
   }

   private static List<String> activeConditionDialogues(Villager villager) {
      List<String> active = new ArrayList<>();
      if (villager.hasEffect(MobEffects.POISON)) {
         active.add("onindz");
      }

      if (villager.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
         active.add("xemyaj");
      }

      if (villager.hasEffect(MobEffects.WEAKNESS)) {
         active.add("yebifs");
      }

      if (villager.isInLava()) {
         active.add("elryje");
      } else if (villager.isOnFire()) {
         active.add("etkxko");
      }

      if (villager.isFullyFrozen()) {
         active.add("igebly");
      }

      if (villager.isInWall()) {
         active.add("vnaodx");
      }

      return active;
   }

   private static boolean canSpeakDuringCondition(Villager villager, String dialogue) {
      ContextualDialogueController.PendingConditionRelief relief = PENDING_CONDITION_RELIEF.get(villager.getUUID());
      List<String> active = activeConditionDialogues(villager);
      if (active.isEmpty()) {
         return relief == null || relief.dialogueId.equals(dialogue);
      } else if (active.contains(dialogue)) {
         return true;
      } else if (villager.isBaby()) {
         return dialogue.equals("ahcvzd") || dialogue.equals("ecslqo");
      } else {
         ContextualDialogueController.CastProfile profile = cast(villager);
         return profile != ContextualDialogueController.CastProfile.VILLAGER && (dialogue.equals(profile.hurt) || dialogue.equals(profile.attack));
      }
   }

   private static void processConditionDialogues(Villager villager) {
      UUID id = villager.getUUID();
      List<String> active = activeConditionDialogues(villager);
      if (active.isEmpty()) {
         Set<String> history = CONDITION_HISTORY.remove(id);
         CONDITION_CURSORS.remove(id);
         if (history != null && !history.isEmpty()) {
            queueConditionRelief(villager, history, ticks + 1L);
         }
      } else {
         PENDING_CONDITION_RELIEF.remove(id);
         CONDITION_HISTORY.computeIfAbsent(id, ignored -> new HashSet<>()).addAll(active);
         PENDING_SPEECH.removeIf(pending -> pending.speakerId.equals(id) || id.equals(pending.targetId));
         List<String> reactions;
         if (villager.isBaby()) {
            reactions = List.of("ecslqo");
         } else {
            ContextualDialogueController.CastProfile profile = cast(villager);
            reactions = profile == ContextualDialogueController.CastProfile.VILLAGER ? active : List.of(profile.hurt);
         }

         ContextualDialogueController.ActiveSound sound = ACTIVE_SOUNDS.get(id);
         if (sound == null || sound.endTick <= ticks || !reactions.contains(sound.groupId)) {
            if (isBusy(villager)) {
               interrupt(villager);
            }

            int cursor = Math.floorMod(CONDITION_CURSORS.getOrDefault(id, 0), reactions.size());

            for (int offset = 0; offset < reactions.size(); offset++) {
               int index = (cursor + offset) % reactions.size();
               String dialogue = reactions.get(index);
               String key = "condition:" + id + ":" + dialogue;
               if (playId(villager, dialogue, key, 1L)) {
                  CONDITION_CURSORS.put(id, index + 1);
                  break;
               }
            }
         }
      }
   }

   private static boolean queueConditionRelief(Villager villager, Set<String> conditions, long dueTick) {
      String dialogue;
      if (villager.isBaby()) {
         dialogue = "wsxfok";
      } else {
         if (cast(villager) != ContextualDialogueController.CastProfile.VILLAGER) {
            return false;
         }

         dialogue = conditions.contains("vnaodx") ? "fxbysi" : "wbbxpo";
      }

      LAST_DANGER.remove(villager.getUUID());
      PENDING_CONDITION_RELIEF.put(
         villager.getUUID(), new ContextualDialogueController.PendingConditionRelief((ServerLevel)villager.level(), villager.getUUID(), dialogue, dueTick)
      );
      return true;
   }

   private static void processPendingConditionRelief() {
      PENDING_CONDITION_RELIEF.entrySet().removeIf(entry -> {
         ContextualDialogueController.PendingConditionRelief pending = entry.getValue();
         if (pending.dueTick > ticks) {
            return false;
         } else if (!(pending.level.getEntity(pending.villagerId) instanceof Villager villager && villager.isAlive())) {
            return true;
         } else if (!activeConditionDialogues(villager).isEmpty()) {
            return false;
         } else {
            ContextualDialogueController.ActiveSound sound = ACTIVE_SOUNDS.get(villager.getUUID());
            if (sound != null && sound.endTick > ticks && DAMAGE_LOCK_DIALOGUES.contains(sound.groupId)) {
               return false;
            } else {
               if (isBusy(villager)) {
                  interrupt(villager);
               }

               return playId(villager, pending.dialogueId, "condition_relief:" + villager.getUUID() + ":" + pending.dialogueId, 1L);
            }
         }
      });
   }

   private static boolean queueFreedSuffocationRelief(ServerLevel level, BlockPos pos) {
      AABB block = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0);
      boolean queued = false;

      for (Villager villager : nearbyVillagers(level, Vec3.atCenterOf(pos), 4.0)) {
         Set<String> history = CONDITION_HISTORY.get(villager.getUUID());
         ContextualDialogueController.VillagerSnapshot snapshot = VILLAGER_STATES.get(villager.getUUID());
         boolean wasSuffocating = history != null && history.contains("vnaodx") || snapshot != null && snapshot.suffocating;
         if (wasSuffocating && villager.getBoundingBox().inflate(0.25).intersects(block)) {
            CONDITION_HISTORY.remove(villager.getUUID());
            CONDITION_CURSORS.remove(villager.getUUID());
            queued |= queueConditionRelief(villager, Set.of("vnaodx"), ticks + 1L);
         }
      }

      return queued;
   }

   private static String profession(Villager villager) {
      return villager.getVillagerData().getProfession().name();
   }

   private static BlockPos findWorkstation(Villager villager) {
      String professionName = profession(villager);

      String block = switch (professionName) {
         case "armorer" -> "blast_furnace";
         case "butcher" -> "smoker";
         case "cartographer" -> "cartography_table";
         case "cleric" -> "brewing_stand";
         case "farmer" -> "composter";
         case "fisherman" -> "barrel";
         case "fletcher" -> "fletching_table";
         case "leatherworker" -> "cauldron";
         case "librarian" -> "lectern";
         case "mason" -> "stonecutter";
         case "shepherd" -> "loom";
         case "toolsmith" -> "smithing_table";
         case "weaponsmith" -> "grindstone";
         default -> null;
      };
      if (block == null) {
         return null;
      } else {
         BlockPos origin = villager.blockPosition();

         for (int x = -3; x <= 3; x++) {
            for (int y = -2; y <= 2; y++) {
               for (int z = -3; z <= 3; z++) {
                  BlockPos pos = origin.offset(x, y, z);
                  if (BuiltInRegistries.BLOCK.getKey(villager.level().getBlockState(pos).getBlock()).getPath().equals(block)) {
                     return pos;
                  }
               }
            }
         }

         return null;
      }
   }

   private static boolean nearCrops(Level level, BlockPos origin, int range) {
      for (int x = -range; x <= range; x++) {
         for (int y = -2; y <= 2; y++) {
            for (int z = -range; z <= range; z++) {
               String path = BuiltInRegistries.BLOCK.getKey(level.getBlockState(origin.offset(x, y, z)).getBlock()).getPath();
               if (path.equals("wheat")
                  || path.equals("carrots")
                  || path.equals("potatoes")
                  || path.equals("beetroots")
                  || path.equals("torchflower_crop")
                  || path.equals("pitcher_crop")) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private static String professionWorkDialogue(String profession) {
      return switch (profession) {
         case "armorer" -> "djpksc";
         case "butcher" -> "ueczyh";
         case "cartographer" -> "wuoloh";
         case "cleric" -> "hkowex";
         case "farmer" -> "umdvtb";
         case "fisherman" -> "tgggoh";
         case "fletcher" -> "fzjope";
         case "leatherworker" -> "ljewqf";
         case "librarian" -> "fezzjw";
         case "mason" -> "yldlzt";
         case "shepherd" -> "opxfuo";
         case "toolsmith" -> "ivktls";
         case "weaponsmith" -> "ccpvqj";
         default -> null;
      };
   }

   private static boolean playNearbyEntityContext(ServerLevel level, Villager speaker) {
      AABB area = speaker.getBoundingBox().inflate(8.0, 6.0, 8.0);
      List<Entity> visibleEntities = level.getEntities(speaker, area, Entity::isAlive)
         .stream()
         .filter(entityx -> speaker.distanceToSqr(entityx) <= 64.0)
         .filter(speaker::hasLineOfSight)
         .sorted(Comparator.comparingDouble(speaker::distanceToSqr))
         .toList();

      for (Entity entity : visibleEntities) {
         String path = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();

         String id = switch (path) {
            case "falling_block" -> "bodvsv";
            case "experience_orb" -> "cvltyw";
            case "tnt" -> "pmaqgq";
            case "firework_rocket" -> speaker.isBaby() ? "zeykfp" : null;
            default -> null;
         };
         if (id != null && playSharedId(speaker, id, "nearby_misc:" + speaker.getUUID() + ":" + entity.getUUID() + ":" + id, 3000L, entity)) {
            return true;
         }
      }

      List<ItemEntity> droppedItems = level.getEntitiesOfClass(ItemEntity.class, AABB.ofSize(speaker.position(), 10.0, 10.0, 10.0), Entity::isAlive)
         .stream()
         .filter(speaker::hasLineOfSight)
         .sorted(Comparator.comparingDouble(speaker::distanceToSqr))
         .toList();
      return droppedItems.size() >= 5 && playSharedId(speaker, "zywcju", "item_pile:" + speaker.getUUID(), 3000L, (Entity)droppedItems.get(0))
         ? true
         : visibleEntities.stream()
            .filter(LivingEntity.class::isInstance)
            .map(LivingEntity.class::cast)
            .filter(entityx -> entityx != speaker && !(entityx instanceof Player) && !(entityx instanceof Villager))
            .anyMatch(
               entityx -> {
                  String pathx = BuiltInRegistries.ENTITY_TYPE.getKey(entityx.getType()).getPath();
                  Entity target = entityx;
                  String dialogue = speaker.isBaby() && pathx.equals("iron_golem") ? "mqnapy" : null;
                  if (dialogue == null && entityx instanceof Sheep sheep && sheep.isSheared()) {
                     dialogue = "afxbav";
                  }

                  if (dialogue == null && entityx instanceof TamableAnimal tame && tame.isTame()) {
                     dialogue = entityx.isBaby() ? "sxikgq" : "aqxgxh";
                  }

                  if (dialogue == null && entityx instanceof Mob mob && mob.getTarget() != null) {
                     if (pathx.equals("bee")) {
                        dialogue = "bmimxe";
                     } else if (pathx.equals("iron_golem") && mob.getTarget() instanceof Player player) {
                        dialogue = "qffeco";
                        target = player;
                     }
                  }

                  if (dialogue == null && (entityx.isPassenger() || !entityx.getPassengers().isEmpty())) {
                     dialogue = "dxeaal";
                  }

                  if (dialogue == null && entityx.isBaby()) {
                     dialogue = BABY_ENTITY_DIALOGUES.get(pathx);
                  }

                  if (dialogue == null) {
                     dialogue = NEARBY_ENTITY_DIALOGUES.get(pathx);
                  }

                  if (dialogue == null && !(entityx instanceof WanderingTrader) && !(entityx instanceof Sheep sheep && isWooly(sheep))) {
                     dialogue = "gjtuqd";
                  }

                  return dialogue != null
                     && speaker.hasLineOfSight(target)
                     && playSharedId(speaker, dialogue, "nearby:" + speaker.getUUID() + ":" + entityx.getUUID() + ":" + dialogue, 3000L, target);
               }
            );
   }

   public static void onDamage(LivingEntity entity, DamageSource source, float baseDamageTaken, float damageTaken, boolean blocked) {
      if (!entity.getTags().contains("vnap_dialogue_test")) {
         if (!blocked && !(damageTaken <= 0.0F)) {
            playIronGolemAttackWitness(entity, source);
            playHurtWitness(entity);
            if (!hasDamageLock(entity)) {
               if (entity.isAlive() && entity.level() instanceof ServerLevel level && (entity instanceof Villager || entity instanceof WanderingTrader)) {
                  HurtEffectNetwork.send(level, entity, entity instanceof Villager villager && villager.isBaby());
               }

               if (entity instanceof Sheep sheep && isWooly(sheep)) {
                  Entity attacker = source.getEntity();
                  String id = attacker instanceof Player ? "ncyeaw" : "eyiraw";
                  playDamageDialogue(sheep, id, "hurt:" + sheep.getUUID(), source, attacker);
               } else if (entity instanceof WanderingTrader trader) {
                  Entity attacker = source.getEntity();
                  String id = attacker instanceof Player ? "vevdkl" : "wyvzhk";
                  playDamageDialogue(trader, id, "hurt:" + trader.getUUID(), source, attacker);
               } else if (entity instanceof Villager villager) {
                  LAST_DANGER.put(villager.getUUID(), ticks);
                  if (!villager.isBaby()) {
                     ContextualDialogueController.CastProfile profile = cast(villager);
                     if (profile != ContextualDialogueController.CastProfile.VILLAGER) {
                        String id = source.getEntity() instanceof Player ? profile.attack : profile.hurt;
                        playDamageDialogue(villager, id, "hurt:" + villager.getUUID(), source, source.getEntity());
                     } else if (!(source.getEntity() instanceof Player player && homeMatches(villager, villager.blockPosition(), 4))) {
                        String dialogue = damageDialogue(source);
                        if (playDamageDialogue(villager, dialogue, "hurt:" + villager.getUUID() + ":" + dialogue, source, source.getEntity())
                           && ready("panic:" + villager.getUUID(), 900L)
                           && villager.level() instanceof ServerLevel level) {
                           COOLDOWNS.put("panic:" + villager.getUUID(), ticks);
                           PENDING_SPEECH.add(
                              new ContextualDialogueController.PendingSpeech(
                                 level,
                                 villager.getUUID(),
                                 "uzdxum",
                                 source.getEntity() == null ? null : source.getEntity().getUUID(),
                                 ticks + DialogueCatalog.byId(dialogue).durationTicks() + 2L
                              )
                           );
                        }
                     } else if (!isPlaying(villager, "lpuocy") && !isPlaying(villager, "slbqfwbayahw")) {
                        interrupt(villager);
                        playHomeAttackReaction(villager, player);
                     }
                  } else {
                     String id = source.getEntity() instanceof Player player && source.getDirectEntity() == player ? "ahcvzd" : "ecslqo";
                     playDamageDialogue(villager, id, "hurt:" + villager.getUUID(), source, source.getEntity());
                  }
               }
            }
         }
      }
   }

   private static boolean playDamageDialogue(LivingEntity speaker, String id, String cooldownKey, DamageSource source, Entity target) {
      DialogueCatalog.DialogueGroup group = DialogueCatalog.byId(id);
      long cooldown = ONGOING_DAMAGE_DIALOGUES.contains(id) ? 1L : (source.getEntity() == null ? 900L : (group == null ? 20L : group.durationTicks() + 20L));
      if (!isPlaying(speaker, id) && ready(cooldownKey, cooldown)) {
         interrupt(speaker);
         boolean sharedAdult = speaker instanceof WanderingTrader
            || speaker instanceof Villager villager && cast(villager) == ContextualDialogueController.CastProfile.UNREACHABLE;
         return sharedAdult ? playSharedId(speaker, id, cooldownKey, cooldown, target) : playId(speaker, id, cooldownKey, cooldown, target);
      } else {
         return false;
      }
   }

   private static void playHurtWitness(LivingEntity entity) {
      if (entity.level() instanceof ServerLevel level) {
         nearbyVillagers(level, entity.position(), 16.0)
            .stream()
            .filter(witness -> witness != entity && !witness.isBaby() && !witness.isSleeping() && !isBusy(witness) && witness.hasLineOfSight(entity))
            .min(Comparator.comparingDouble(witness -> witness.distanceToSqr(entity)))
            .ifPresent(witness -> playSharedId(witness, "pkvhpv", "witness_hurt:" + witness.getUUID(), 900L, entity));
      }
   }

   private static void playIronGolemAttackWitness(LivingEntity entity, DamageSource source) {
      if (entity.level() instanceof ServerLevel level) {
         ServerPlayer var9 = null;
         String hurtType = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
         Entity attacker = source.getEntity();
         String attackerType = attacker == null ? "" : BuiltInRegistries.ENTITY_TYPE.getKey(attacker.getType()).getPath();
         if (hurtType.equals("iron_golem") && attacker instanceof ServerPlayer serverPlayer) {
            var9 = serverPlayer;
         } else if (entity instanceof ServerPlayer serverPlayer && attackerType.equals("iron_golem")) {
            var9 = serverPlayer;
         }

         if (var9 != null && !var9.isCreative()) {
            ServerPlayer subject = var9;
            nearbyVillagers(level, entity.position(), 16.0)
               .stream()
               .filter(witness -> !witness.isBaby() && !witness.isSleeping() && !isBusy(witness) && witness.hasLineOfSight(subject))
               .min(Comparator.comparingDouble(witness -> witness.distanceToSqr(entity)))
               .ifPresent(witness -> playSharedId(witness, "qffeco", "golem_attack:" + witness.getUUID(), 900L, subject));
         }
      }
   }

   private static String damageDialogue(DamageSource source) {
      String attacker = source.getEntity() == null ? "" : BuiltInRegistries.ENTITY_TYPE.getKey(source.getEntity().getType()).getPath();
      String direct = source.getDirectEntity() == null ? "" : BuiltInRegistries.ENTITY_TYPE.getKey(source.getDirectEntity().getType()).getPath();
      if (direct.equals("arrow")) {
         return "huhcbd";
      } else if (direct.equals("snowball")) {
         return "dlrxes";
      } else if (source.is(DamageTypes.FALLING_ANVIL)) {
         return "yzqpvi";
      } else if (source.is(DamageTypes.STALAGMITE) || source.is(DamageTypes.FALLING_STALACTITE)) {
         return "nsxmkr";
      } else if (direct.equals("firework_rocket") || source.is(DamageTypes.FIREWORKS)) {
         return "gesjov";
      } else if (!direct.equals("potion") && !direct.equals("lingering_potion")) {
         if (source.getEntity() instanceof Player player && source.getDirectEntity() == player) {
            return weaponAttackDialogue(player.getMainHandItem());
         } else if (attacker.equals("evoker")) {
            return "nkcoqb";
         } else if (attacker.equals("pillager")) {
            return "qhpyaw";
         } else if (attacker.equals("ravager")) {
            return "uveohs";
         } else if (attacker.equals("vex")) {
            return "caykki";
         } else if (attacker.equals("vindicator")) {
            return "swomdw";
         } else if (attacker.equals("witch")) {
            return "rtikom";
         } else if (attacker.equals("zoglin")) {
            return "hfmwvf";
         } else if (attacker.contains("zombie") || attacker.equals("drowned") || attacker.equals("husk")) {
            return "mytmrk";
         } else if (source.is(DamageTypes.LIGHTNING_BOLT)) {
            return "ikrwzy";
         } else if (source.is(DamageTypes.EXPLOSION) || source.is(DamageTypes.PLAYER_EXPLOSION)) {
            return "fcbygh";
         } else if (source.is(DamageTypes.LAVA)) {
            return "elryje";
         } else if (source.is(DamageTypes.IN_FIRE)
            || source.is(DamageTypes.ON_FIRE)
            || source.is(DamageTypes.HOT_FLOOR)) {
            return "etkxko";
         } else if (source.is(DamageTypes.CACTUS) || source.is(DamageTypes.SWEET_BERRY_BUSH)) {
            return "rogpvp";
         } else if (source.is(DamageTypes.FREEZE)) {
            return "igebly";
         } else if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CRAMMING)) {
            return "vnaodx";
         } else {
            return source.is(DamageTypes.FALL) ? "cifbit" : "wyvzhk";
         }
      } else {
         return !source.is(DamageTypes.MAGIC) && !source.is(DamageTypes.INDIRECT_MAGIC) ? "hmadgp" : "gacgtq";
      }
   }

   private static void onDeath(LivingEntity entity, DamageSource source) {
      if (!entity.getTags().contains("vnap_dialogue_test")) {
         interrupt(entity);
         if (entity.level() instanceof ServerLevel level) {
            if (entity.getTags().contains("vnap_natural_special")) {
               clearNaturalSpecial(level, entity);
            }

            if (entity instanceof Villager villager) {
               BUSY_UNTIL.remove(villager.getUUID());
               if (villager.isBaby()) {
                  playId(villager, "ecslqo", "death:" + villager.getUUID(), 1L);
               } else {
                  playSharedId(villager, "hivgme", "death:" + villager.getUUID(), 1L, null);
               }
            }

            if (entity instanceof Villager || entity instanceof WanderingTrader || entity instanceof Sheep sheep && isWooly(sheep)) {
               nearbyVillagers(level, entity.position(), 16.0)
                  .stream()
                  .filter(witness -> witness != entity && !witness.isBaby() && !witness.isSleeping())
                  .min(Comparator.comparingDouble(witness -> witness.distanceToSqr(entity)))
                  .ifPresent(witness -> playSharedId(witness, "pmqrpb", "witness_death:" + witness.getUUID(), 900L, entity));
            } else if (entity instanceof ServerPlayer player) {
               int deaths = PLAYER_DEATHS.merge(player.getUUID(), 1, Integer::sum);
               String id = level.getLevelData().isHardcore() ? "elcjbb" : (deaths > 1 ? "dxcjqn" : "hzjycq");
               nearbyVillagers(level, player.position(), 16.0)
                  .stream()
                  .filter(witness -> !witness.isBaby() && !witness.isSleeping() && witness.hasLineOfSight(player))
                  .min(Comparator.comparingDouble(witness -> witness.distanceToSqr(player)))
                  .ifPresent(witness -> playSharedId(witness, id, "player_death:" + witness.getUUID() + ":" + deaths, 900L, player));
            }
         }
      }
   }

   /**
    * Fabric API 1.20.1 fires the entity-use callback for both the interact-at and the plain interact
    * packet of a single right-click. The second call would repeat every side effect (a sheared nose
    * would immediately open the trade screen), so a repeat within the same click replays the first result.
    */
   private static InteractionResult onUseEntityOnce(Player player, Entity entity, InteractionHand hand) {
      LastInteraction last = LAST_INTERACTIONS.get(player.getUUID());
      if (last != null && last.entityId() == entity.getId() && last.hand() == hand && ticks - last.tick() <= 1L) {
         return last.result();
      }

      InteractionResult result = onUseEntity(player, entity, hand);
      LAST_INTERACTIONS.put(player.getUUID(), new LastInteraction(entity.getId(), hand, ticks, result));
      return result;
   }

   private static InteractionResult onUseEntity(Player player, Entity entity, InteractionHand hand) {
      if (entity.getTags().contains("vnap_dialogue_test")) {
         return InteractionResult.SUCCESS;
      } else {
         if (entity instanceof Villager villager) {
            if (cast(villager) == ContextualDialogueController.CastProfile.UNREACHABLE) {
               repelPlayer(villager, player);
               return InteractionResult.SUCCESS;
            }

            ItemStack heldStack = player.getItemInHand(hand);
            String heldItem = BuiltInRegistries.ITEM.getKey(heldStack.getItem()).getPath();
            InteractionResult cosmeticResult = interactWithCosmetic(player, villager, heldStack);
            if (cosmeticResult != InteractionResult.PASS) {
               return cosmeticResult;
            }

            String gift = foodGiftDialogue(villager.isBaby(), heldItem);
            if (gift != null) {
               playId(villager, gift, "food_gift:" + villager.getUUID() + ":" + heldItem, 900L, player);
               return InteractionResult.PASS;
            }

            int offeredSign = signType(heldStack);
            if (offeredSign >= 0 && !villager.isBaby()) {
               VillagerNewsData state = data(villager);
               int previousSign = state.vnap$signType();
               if (previousSign == offeredSign) {
                  return InteractionResult.SUCCESS;
               }

               if (previousSign >= 0) {
                  villager.spawnAtLocation(signItem(previousSign));
               }

               state.vnap$setSignType(offeredSign);
               consume(player, heldStack);
               villager.level().playSound(null, villager.blockPosition(), SoundEvents.HORSE_STEP_WOOD, SoundSource.NEUTRAL, 1.0F, 1.0F);
               if (previousSign < 0) {
                  state.vnap$setSignMessage(villager.getRandom().nextInt(87));
                  playId(villager, "vqlrqf", "sign_gift:" + villager.getUUID(), 900L, player);
               }

               return InteractionResult.SUCCESS;
            }

            if (villager.isSleeping()) {
               UUID id = villager.getUUID();
               WAKE_SOURCES.put(id, player.getUUID());
               INTERRUPTED_SLEEP.add(id);
               villager.stopSleeping();
               return InteractionResult.PASS;
            }

            ensureSpecialTrade(villager);
            if (!villager.isBaby() && hand == InteractionHand.MAIN_HAND && player.level() instanceof ServerLevel level) {
               ACTIVE_TRADES.put(player.getUUID(), new ContextualDialogueController.TradeSession(level, villager.getUUID(), ticks));
            }

            if (villager.isBaby()) {
               playId(villager, "aezdiy", "baby_trade:" + villager.getUUID(), 900L, player);
            }
         } else if (entity instanceof WanderingTrader trader) {
            if (hand == InteractionHand.MAIN_HAND && player.level() instanceof ServerLevel level) {
               ACTIVE_TRADES.put(player.getUUID(), new ContextualDialogueController.TradeSession(level, trader.getUUID(), ticks));
            }
         } else if (entity instanceof Sheep sheep && isWooly(sheep)) {
            String held = BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).getPath();
            playId(sheep, held.equals("shears") ? "jqaekk" : "fskcce", "interact:" + sheep.getUUID(), 900L, player);
         } else if (BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).getPath().equals("lead") && player.level() instanceof ServerLevel level) {
            playObserved(level, player, entity.position(), "Use a Lead", 900L);
         }

         if (entity instanceof Sheep
            && BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).getPath().equals("shears")
            && player.level() instanceof ServerLevel level) {
            playObserved(level, player, entity.position(), "Shear a Sheep", 900L);
         }

         return InteractionResult.PASS;
      }
   }

   private static InteractionResult interactWithCosmetic(Player player, Villager villager, ItemStack stack) {
      VillagerNewsData state = data(villager);
      if (stack.getItem() == Items.SHEARS && state.vnap$signType() >= 0) {
         if (villager.level() instanceof ServerLevel level) {
            villager.spawnAtLocation(signItem(state.vnap$signType()));
            level.playSound(null, villager.blockPosition(), SoundEvents.SHEEP_SHEAR, SoundSource.NEUTRAL, 1.0F, 1.0F);
         }

         state.vnap$setSignMessage(-1);
         state.vnap$setSignType(-1);
         damageShears(player, stack);
         return InteractionResult.SUCCESS;
      } else if (isAxe(stack) && state.vnap$signType() >= 0) {
         int direction = player.isShiftKeyDown() ? -1 : 1;
         int message = Math.floorMod(state.vnap$signMessage() + direction, 87);
         state.vnap$setSignMessage(message);
         if (villager.level() instanceof ServerLevel level) {
            level.playSound(null, villager.blockPosition(), SoundEvents.HORSE_STEP_WOOD, SoundSource.NEUTRAL, 1.0F, 1.0F);
         }

         if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(Component.literal("Sign message " + (message + 1) + " / 87"), true);
         }

         return InteractionResult.SUCCESS;
      } else {
         if (stack.getItem() == Items.SHEARS && !villager.isBaby()) {
            if (state.vnap$cosmetic() != 0) {
               if (villager.level() instanceof ServerLevel level) {
                  Item item = VillagerNewsItems.cosmeticItem(state.vnap$cosmetic());
                  if (item != null) {
                     villager.spawnAtLocation(new ItemStack(item));
                  }

                  level.playSound(null, villager.blockPosition(), SoundEvents.SHEEP_SHEAR, SoundSource.NEUTRAL, 1.0F, 1.0F);
               }

               state.vnap$setCosmetic(0);
               damageShears(player, stack);
               playId(villager, "ckjbyd", "remove_cosmetic:" + villager.getUUID(), 1L, player);
               return InteractionResult.SUCCESS;
            }

            if (state.vnap$hasNose()) {
               if (villager.level() instanceof ServerLevel level) {
                  villager.spawnAtLocation(new ItemStack(VillagerNewsItems.VILLAGER_NOSE));
                  level.playSound(null, villager.blockPosition(), SoundEvents.SHEEP_SHEAR, SoundSource.NEUTRAL, 1.0F, 1.0F);
               }

               state.vnap$setHasNose(false);
               damageShears(player, stack);
               playId(villager, "jktrnd", "shear_nose:" + villager.getUUID(), 1L, player);
               return InteractionResult.SUCCESS;
            }
         }

         if (stack.getItem() == VillagerNewsItems.VILLAGER_NOSE) {
            if (state.vnap$hasNose()) {
               playId(villager, "akfekx", "second_nose:" + villager.getUUID(), 900L, player);
               return InteractionResult.SUCCESS;
            } else {
               consume(player, stack);
               state.vnap$setHasNose(true);
               playId(villager, "kxrhxt", "return_nose:" + villager.getUUID(), 1L, player);
               return InteractionResult.SUCCESS;
            }
         } else {
            int cosmetic = VillagerNewsItems.cosmetic(stack.getItem());
            if (cosmetic == 0) {
               return InteractionResult.PASS;
            } else if (state.vnap$cosmetic() != 0) {
               return InteractionResult.SUCCESS;
            } else {
               consume(player, stack);
               state.vnap$setCosmetic(cosmetic);
               playGivenCosmetic(villager, player, cosmetic);
               return InteractionResult.SUCCESS;
            }
         }
      }
   }

   private static void playGivenCosmetic(Villager villager, Player player, int cosmetic) {
      String id;
      if (villager.isBaby()) {
         id = switch (cosmetic) {
            case 1 -> "svdjdk";
            case 2 -> "cxeziv";
            case 3 -> "riezum";
            case 4 -> "rlkdqd";
            default -> "orogba";
         };
      } else {
         String special = switch (cosmetic) {
            case 2 -> "wurmgu";
            case 3 -> "inirxg";
            case 4 -> "ozxzla";
            default -> null;
         };
         id = special == null || cosmetic != 3 && ThreadLocalRandom.current().nextInt(3) == 0 ? "orogba" : special;
      }

      playId(villager, id, "give_cosmetic:" + villager.getUUID() + ":" + cosmetic, 1L, player);
   }

   private static void damageShears(Player player, ItemStack stack) {
      if (!player.isCreative() && player instanceof ServerPlayer serverPlayer) {
         stack.hurtAndBreak(1, serverPlayer, ignored -> {});
      }
   }

   private static void consume(Player player, ItemStack stack) {
      if (!player.isCreative()) {
         stack.shrink(1);
      }
   }

   private static boolean isStandingSign(ItemStack stack) {
      String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
      return path.endsWith("_sign") && !path.endsWith("_hanging_sign");
   }

   public static int signType(ItemStack stack) {
      if (!isStandingSign(stack)) {
         return -1;
      } else {
         String var1 = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();

         return switch (var1) {
            case "oak_sign" -> 0;
            case "spruce_sign" -> 1;
            case "birch_sign" -> 2;
            case "jungle_sign" -> 3;
            case "acacia_sign" -> 4;
            case "dark_oak_sign" -> 5;
            case "mangrove_sign" -> 6;
            case "cherry_sign" -> 7;
            case "pale_oak_sign" -> 8;
            case "bamboo_sign" -> 9;
            case "crimson_sign" -> 10;
            case "warped_sign" -> 11;
            default -> -1;
         };
      }
   }

   public static ItemStack signItem(int type) {
      return new ItemStack(switch (type) {
         case 1 -> Items.SPRUCE_SIGN;
         case 2 -> Items.BIRCH_SIGN;
         case 3 -> Items.JUNGLE_SIGN;
         case 4 -> Items.ACACIA_SIGN;
         case 5 -> Items.DARK_OAK_SIGN;
         case 6 -> Items.MANGROVE_SIGN;
         case 7 -> Items.CHERRY_SIGN;
         case 8 -> Items.BIRCH_SIGN;
         case 9 -> Items.BAMBOO_SIGN;
         case 10 -> Items.CRIMSON_SIGN;
         case 11 -> Items.WARPED_SIGN;
         default -> Items.OAK_SIGN;
      });
   }

   private static boolean isAxe(ItemStack stack) {
      return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().endsWith("_axe");
   }

   private static String foodGiftDialogue(boolean baby, String item) {
      if (baby) {
         return switch (item) {
            case "beetroot" -> "qrdzmt";
            case "bread" -> "hbalps";
            case "carrot" -> "hcdvqm";
            case "potato" -> "gotjxf";
            default -> null;
         };
      } else {
         return switch (item) {
            case "beetroot" -> "rlfjux";
            case "bread" -> "bbjsik";
            case "carrot" -> "nqktml";
            case "potato" -> "ytydjc";
            case "wheat" -> "ebyrtk";
            default -> null;
         };
      }
   }

   private static void onAttackEntity(Player player, Entity entity) {
      if (entity instanceof Villager villager && villager.isSleeping()) {
         UUID id = villager.getUUID();
         WAKE_SOURCES.put(id, player.getUUID());
         INTERRUPTED_SLEEP.add(id);
         villager.stopSleeping();
      }
   }

   private static void playHomeAttackReaction(Villager villager, Player player) {
      if (villager.level() instanceof ServerLevel level) {
         Villager witness = nearbyVillagers(level, villager.position(), 10.0)
            .stream()
            .filter(other -> other != villager && !other.isBaby() && !other.isSleeping() && other.hasLineOfSight(villager))
            .min(Comparator.comparingDouble(other -> other.distanceToSqr(villager)))
            .orElse(null);
         if (witness != null && playSharedId(witness, "slbqfwswxeva", "home_witness:" + witness.getUUID(), 20L, villager)) {
            long due = ticks + DialogueCatalog.byId("slbqfwswxeva").durationTicks() + 2L;
            PENDING_SPEECH.add(new ContextualDialogueController.PendingSpeech(level, villager.getUUID(), "slbqfwbayahw", witness.getUUID(), due));
         } else {
            playId(villager, "lpuocy", "attacked_home:" + villager.getUUID(), 20L, player);
         }
      }
   }

   private static String weaponAttackDialogue(ItemStack stack) {
      String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
      if (path.endsWith("_sword")) {
         return "rueszy";
      } else if (path.endsWith("_axe")) {
         return "yjctyw";
      } else if (path.endsWith("_hoe")) {
         return "qqyjjg";
      } else {
         return path.endsWith("_shovel") ? "hpnsfu" : "vevdkl";
      }
   }

   private static String selectBreakContext(BlockState state, ContextualDialogueController.PlayerObservation observation) {
      if (ticks - observation.lastBreakTick <= 30L) {
         observation.breakStreak++;
      } else {
         observation.breakStreak = 1;
      }

      observation.lastBreakTick = ticks;
      if (observation.breakStreak >= 4) {
         return "Break Multiple Blocks";
      } else {
         String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
         if (path.equals("wheat")
            || path.equals("carrots")
            || path.equals("potatoes")
            || path.equals("beetroots")
            || path.equals("torchflower_crop")
            || path.equals("pitcher_crop")) {
            return "Harvest Crops";
         } else if (path.contains("flower") || path.contains("candle") || path.contains("coral") || path.contains("banner") || path.contains("decorated_pot")) {
            return "Break a Decorative Block";
         } else if (path.endsWith("_door")) {
            return "Break a Door";
         } else if (path.endsWith("_bed")) {
            return "Break a Bed";
         } else if (path.equals("bell")) {
            return "Break a Bell";
         } else if (isWorkstation(path)) {
            return "Break a Workstation";
         } else if (path.contains("log") || path.contains("wood") || path.contains("stem") || path.contains("hyphae")) {
            return "Break Wood";
         } else {
            return !path.contains("stone") && !path.contains("deepslate") && !path.contains("cobblestone") ? "Break a Block" : "Break Stone";
         }
      }
   }

   private static String selectPlaceContext(Block block, ServerLevel level, BlockPos position) {
      String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
      if (level.dimension() == Level.END) {
         return "Place a Block from the End";
      } else if (level.dimension() == Level.NETHER) {
         return "Place a Block from the Nether";
      } else {
         String biome = level.getBiome(position).unwrapKey().map(key -> key.location().getPath()).orElse("");
         if (biome.contains("ocean")) {
            return "Place a Block from the Ocean";
         } else if (path.equals("daylight_detector")) {
            return "Place a Daylight Detector";
         } else if (path.equals("detector_rail")) {
            return "Place a Detector Rail";
         } else if (path.equals("lightning_rod")) {
            return "Place a Lightning Rod";
         } else if (path.equals("melon")) {
            return "Place a Melon";
         } else if (path.equals("observer")) {
            return "Place an Observer";
         } else if (path.endsWith("pressure_plate")) {
            return "Place a Pressure Plate";
         } else if (path.equals("pumpkin")) {
            return "Place a Pumpkin";
         } else if (path.equals("redstone_lamp")) {
            return "Place a Redstone Lamp";
         } else if (path.equals("repeater")) {
            return "Place a Redstone Repeater";
         } else if (path.equals("redstone_torch") || path.equals("redstone_wall_torch")) {
            return "Place a Redstone Torch";
         } else if (path.contains("sculk_sensor")) {
            return "Place a Sculk Sensor";
         } else if (path.equals("tripwire_hook")) {
            return "Place a Tripwire Hook";
         } else if (path.equals("jack_o_lantern")) {
            return "Place a Jack o'Lantern";
         } else if (path.equals("end_stone")) {
            return "Place End Stone";
         } else if (path.contains("purpur")) {
            return "Place Purpur";
         } else if (path.contains("copper")) {
            return "Place a Copper Block";
         } else if (path.contains("brick")) {
            return "Place Bricks";
         } else if (path.equals("powder_snow")) {
            return "Place Powder Snow";
         } else if (path.equals("light")) {
            return "Place a Light Block";
         } else if (path.equals("barrier") || path.contains("command_block") || path.equals("structure_block") || path.equals("jigsaw")) {
            return "Place a Creative-Only Block";
         } else if (path.endsWith("sand") || path.endsWith("gravel") || path.equals("anvil")) {
            return "Place a Gravity-Affected Block";
         } else if (path.equals("iron_block")
            || path.equals("gold_block")
            || path.equals("diamond_block")
            || path.equals("emerald_block")
            || path.equals("netherite_block")) {
            return "Place a Valuable Block";
         } else if (path.contains("redstone") || path.equals("lever") || path.endsWith("button") || path.endsWith("rail")) {
            return "Place a Redstone Component";
         } else if (path.endsWith("_bed")) {
            return "Place a Bed";
         } else if (path.equals("chest")) {
            return "Place a Chest";
         } else if (path.equals("trapped_chest")) {
            return "Place a Trapped Chest";
         } else if (path.equals("crafting_table")) {
            return "Place a Crafting Table";
         } else if (path.equals("furnace")) {
            return "Place a Furnace";
         } else if (path.equals("bookshelf")) {
            return "Place a Bookshelf";
         } else if (path.equals("jukebox")) {
            return "Place a Jukebox";
         } else if (path.equals("armor_stand")) {
            return "Place an Armor Stand";
         } else if (path.equals("beacon")) {
            return "Place a Beacon";
         } else if (isWorkstation(path)) {
            return "Place a Workstation";
         } else if (path.endsWith("_log") || path.endsWith("_wood") || path.endsWith("_planks")) {
            return "Place Wood";
         } else if (path.contains("dirt")) {
            return "Place Dirt";
         } else if (path.contains("leaves") || path.contains("sapling") || path.contains("flower")) {
            return "Place Leaves or Plants";
         } else if (path.contains("wool")) {
            return "Place Wool";
         } else if (path.contains("glass")) {
            return "Place Glass";
         } else if (path.contains("concrete_powder")) {
            return "Place Concrete Powder";
         } else if (path.contains("concrete")) {
            return "Place Concrete";
         } else if (path.contains("glazed_terracotta")) {
            return "Place Glazed Terracotta";
         } else if (path.contains("terracotta")) {
            return "Place Terracotta";
         } else if (path.equals("iron_block")) {
            return "Place an Iron Block";
         } else if (path.equals("gold_block")) {
            return "Place a Gold Block";
         } else if (path.equals("diamond_block")) {
            return "Place a Diamond Block";
         } else if (path.equals("emerald_block")) {
            return "Place an Emerald Block";
         } else if (path.equals("lapis_block")) {
            return "Place a Lapis Block";
         } else if (path.contains("ice")) {
            return "Place Ice";
         } else if (path.contains("snow")) {
            return "Place Snow";
         } else if (path.endsWith("_button")) {
            return "Place a Button";
         } else {
            return path.equals("lever") ? "Place a Lever" : "Place a Block";
         }
      }
   }

   private static String selectUseBlockContext(BlockState state) {
      String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
      if (path.endsWith("_button")) {
         return "Press a Button";
      } else if (path.equals("bell")) {
         return "Hear a Bell Ring";
      } else if (path.equals("lever")) {
         return "Flip a Lever";
      } else if (path.endsWith("_door")) {
         return "Use a Door";
      } else if (!path.endsWith("_fence_gate")) {
         if (path.equals("crafter")) {
            return "Use a Crafter";
         } else if (path.equals("dispenser")) {
            return "Use a Dispenser";
         } else if (path.equals("dropper")) {
            return "Use a Dropper";
         } else if (path.equals("jukebox")) {
            return "Use a Jukebox";
         } else if (path.equals("loom")) {
            return "Use a Loom";
         } else if (path.contains("shulker_box")) {
            return "Use a Shulker Box";
         } else if (path.equals("stonecutter")) {
            return "Use a Stonecutter";
         } else if (path.equals("beacon")) {
            return "Use a Beacon";
         } else if (path.contains("campfire")) {
            return "Use a Campfire";
         } else if (path.equals("cartography_table")) {
            return "Use a Cartography Table";
         } else if (path.equals("cauldron") || path.endsWith("_cauldron")) {
            return "Use a Cauldron";
         } else if (path.equals("chiseled_bookshelf")) {
            return "Use a Chiseled Bookshelf";
         } else if (path.equals("composter")) {
            return "Use a Composter";
         } else if (path.equals("ender_chest")) {
            return "Use an Ender Chest";
         } else if (path.contains("shelf")) {
            return "Use Shelves";
         } else if (path.contains("chest")) {
            return "Open a Chest";
         } else if (path.equals("crafting_table")) {
            return "Use a Crafting Table";
         } else if (path.equals("furnace") || path.equals("blast_furnace") || path.equals("smoker")) {
            return "Use a Furnace";
         } else if (path.equals("anvil") || path.endsWith("_anvil")) {
            return "Use an Anvil";
         } else if (path.equals("enchanting_table")) {
            return "Use an Enchanting Table";
         } else if (path.equals("brewing_stand")) {
            return "Use a Brewing Stand";
         } else if (path.equals("grindstone")) {
            return "Use a Grindstone";
         } else {
            return path.equals("smithing_table") ? "Use a Smithing Table" : null;
         }
      } else {
         return state.hasProperty(BlockStateProperties.OPEN) && state.getValue(BlockStateProperties.OPEN) ? "Close a Fence Gate" : "Open a Fence Gate";
      }
   }

   private static String selectHeldBlockContext(ItemStack stack, BlockState clicked) {
      String item = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
      String block = BuiltInRegistries.BLOCK.getKey(clicked.getBlock()).getPath();
      if (item.equals("redstone")) {
         return "Place Redstone Dust";
      } else if ((item.equals("flint_and_steel") || item.equals("fire_charge")) && block.equals("tnt")) {
         return "Light TNT";
      } else if (block.equals("tnt")) {
         return "See TNT";
      } else if ((item.equals("flint_and_steel") || item.equals("fire_charge")) && block.contains("campfire")) {
         return "Light a Campfire";
      } else if ((item.equals("flint_and_steel") || item.equals("fire_charge")) && block.contains("candle")) {
         return "Light a Candle";
      } else if (!block.contains("campfire")
         || !item.contains("beef")
            && !item.contains("porkchop")
            && !item.contains("chicken")
            && !item.contains("mutton")
            && !item.contains("rabbit")
            && !item.equals("potato")) {
         if ((item.equals("water_bucket") || item.endsWith("_shovel")) && block.contains("campfire")) {
            return "Extinguish a Campfire";
         } else if (item.equals("water_bucket") && block.contains("candle")) {
            return "Extinguish a Candle";
         } else {
            return item.equals("shears") && block.equals("pumpkin") ? "Carve a Pumpkin" : null;
         }
      } else {
         return "Cook Food on a Campfire";
      }
   }

   private static String selectUseItemContext(ItemStack stack) {
      String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
      if (path.equals("firework_rocket")) {
         return "Set Off a Firework";
      } else if (path.equals("ender_pearl")) {
         return "Teleport with an Ender Pearl";
      } else if (path.equals("snowball")) {
         return "Snowball";
      } else {
         return !path.contains("apple")
               && !path.contains("bread")
               && !path.contains("carrot")
               && !path.contains("potato")
               && !path.contains("beef")
               && !path.contains("porkchop")
               && !path.contains("chicken")
               && !path.contains("mutton")
               && !path.contains("rabbit")
               && !path.contains("stew")
               && !path.contains("berries")
               && !path.contains("melon")
            ? null
            : "Eat Food";
      }
   }

   private static boolean isWorkstation(String path) {
      return path.equals("composter")
         || path.equals("barrel")
         || path.equals("blast_furnace")
         || path.equals("smoker")
         || path.equals("cartography_table")
         || path.equals("brewing_stand")
         || path.equals("fletching_table")
         || path.equals("cauldron")
         || path.equals("lectern")
         || path.equals("stonecutter")
         || path.equals("loom")
         || path.equals("smithing_table")
         || path.equals("grindstone");
   }

   private static boolean playObserved(ServerLevel level, Player player, Vec3 eventPosition, String title, long cooldown) {
      Villager speaker = nearbyVillagers(level, eventPosition, 16.0)
         .stream()
         .filter(villager -> !villager.isBaby() && !villager.isSleeping())
         .filter(villager -> cast(villager) == ContextualDialogueController.CastProfile.VILLAGER)
         .filter(villager -> villager.hasLineOfSight(player))
         .min(Comparator.comparingDouble(villager -> villager.distanceToSqr(eventPosition)))
         .orElse(null);
      DialogueCatalog.DialogueGroup group = DialogueCatalog.byTitle(title, "villager");
      return speaker != null && group != null && play(speaker, group, "observed:" + speaker.getUUID() + ":" + title, cooldown, player, null, true);
   }

   private static List<Villager> nearbyVillagers(ServerLevel level, Vec3 position, double range) {
      AABB area = AABB.ofSize(position, range * 2.0, range, range * 2.0);
      return level.getEntitiesOfClass(Villager.class, area, entity -> entity.isAlive() && !entity.getTags().contains("vnap_dialogue_test"));
   }

   private static boolean playTitle(LivingEntity speaker, String title, String cooldownKey, long cooldown) {
      DialogueCatalog.DialogueGroup group = DialogueCatalog.byTitle(title, speakerType(speaker));
      return group != null && play(speaker, group, cooldownKey, cooldown, null, null);
   }

   private static boolean playTitle(LivingEntity speaker, String title, String cooldownKey, long cooldown, Entity target) {
      DialogueCatalog.DialogueGroup group = DialogueCatalog.byTitle(title, speakerType(speaker));
      return group != null && play(speaker, group, cooldownKey, cooldown, target, null);
   }

   private static boolean playSharedTitle(LivingEntity speaker, String title, String cooldownKey, long cooldown, Entity target) {
      DialogueCatalog.DialogueGroup group = DialogueCatalog.byTitle(title, "villager");
      return group != null && play(speaker, group, cooldownKey, cooldown, target, null, true);
   }

   private static boolean playId(LivingEntity speaker, String id, String cooldownKey, long cooldown) {
      DialogueCatalog.DialogueGroup group = DialogueCatalog.byId(id);
      return group != null && play(speaker, group, cooldownKey, cooldown, null, null);
   }

   private static boolean playId(LivingEntity speaker, String id, String cooldownKey, long cooldown, Entity target) {
      DialogueCatalog.DialogueGroup group = DialogueCatalog.byId(id);
      return group != null && play(speaker, group, cooldownKey, cooldown, target, null);
   }

   private static boolean playSharedId(LivingEntity speaker, String id, String cooldownKey, long cooldown, Entity target) {
      DialogueCatalog.DialogueGroup group = DialogueCatalog.byId(id);
      return group != null && play(speaker, group, cooldownKey, cooldown, target, null, true);
   }

   private static boolean playId(LivingEntity speaker, String id, String cooldownKey, long cooldown, Vec3 target) {
      DialogueCatalog.DialogueGroup group = DialogueCatalog.byId(id);
      return group != null && play(speaker, group, cooldownKey, cooldown, null, target);
   }

   private static boolean play(LivingEntity speaker, DialogueCatalog.DialogueGroup group, String cooldownKey, long cooldown, Entity target, Vec3 targetPosition) {
      return play(speaker, group, cooldownKey, cooldown, target, targetPosition, false);
   }

   private static boolean play(
      LivingEntity speaker, DialogueCatalog.DialogueGroup group, String cooldownKey, long cooldown, Entity target, Vec3 targetPosition, boolean sharedAdult
   ) {
      boolean sharedVillagerVoice = speaker instanceof Villager villager
            && !villager.isBaby()
            && cast(villager) == ContextualDialogueController.CastProfile.VILLAGER
         || speaker instanceof WanderingTrader;
      boolean validSpeaker = matchesSpeaker(speaker, group) || sharedAdult && sharedVillagerVoice && group.speaker().equals("villager");
      boolean blockedByCondition = speaker instanceof Villager conditionVillager && !canSpeakDuringCondition(conditionVillager, group.id());
      if (speaker.level() instanceof ServerLevel level
         && validSpeaker
         
         && VillagerNewsSettings.dialogueEnabled()
         && !blockedByCondition
         && !(speaker instanceof Villager sleepingVillager && sleepingVillager.isSleeping() && !group.id().equals("asqzby"))
         && !isBusy(speaker)
         && ready(cooldownKey, VillagerNewsSettings.scaleCooldown(cooldown))) {
         List<Integer> recentVariants = SHARED_RECENT_VARIANTS.getOrDefault(group.id(), List.of());
         DialogueCatalog.DialogueVariant variant = group.chooseVariant(VillagerNewsSettings.rareVoicelines(), Set.copyOf(recentVariants));
         if (variant == null) {
            return false;
         } else {
            DialogueAnimationNetwork.send(level, speaker, group.id(), variant.index(), (int)variant.durationTicks());
            ACTIVE_SOUNDS.put(speaker.getUUID(), new ContextualDialogueController.ActiveSound(group.id(), ticks + variant.durationTicks()));
            COOLDOWNS.put(cooldownKey, ticks);
            int maximumWeight = group.variants().stream().mapToInt(DialogueCatalog.DialogueVariant::weight).max().orElse(1);
            int eligibleVariants = VillagerNewsSettings.rareVoicelines() == 0
               ? (int)group.variants().stream().filter(candidate -> candidate.weight() >= maximumWeight * 0.8).count()
               : group.variants().size();
            int historySize = Math.min(8, eligibleVariants - 1);
            if (historySize > 0) {
               List<Integer> updatedHistory = new ArrayList<>(recentVariants);
               updatedHistory.remove(Integer.valueOf(variant.index()));
               updatedHistory.add(variant.index());

               while (updatedHistory.size() > historySize) {
                  updatedHistory.remove(0);
               }

               SHARED_RECENT_VARIANTS.put(group.id(), updatedHistory);
            }

            markBusy(speaker, variant.durationTicks() + 10L);
            if (speaker instanceof Mob mob) {
               Vec3 position = target != null ? target.getEyePosition() : targetPosition;
               boolean lockMovement = !MOBILE_DIALOGUES.contains(group.id())
                  && !(
                     speaker instanceof Villager villagerx
                        && cast(villagerx) == ContextualDialogueController.CastProfile.UNREACHABLE
                        && group.id().equals("eltxge")
                  );
               SPEECH_TARGETS.put(
                  speaker.getUUID(),
                  new ContextualDialogueController.SpeechTarget(
                     target == null ? null : target.getUUID(), position, ticks + variant.durationTicks(), lockMovement
                  )
               );
               if (lockMovement) {
                  holdMob(mob, position);
               } else {
                  faceMob(mob, position);
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   private static boolean matchesSpeaker(LivingEntity speaker, DialogueCatalog.DialogueGroup group) {
      if (speaker instanceof Villager villager) {
         if (villager.isBaby()) {
            return BABY_DIALOGUES.contains(group.id());
         }

         if (BABY_DIALOGUES.contains(group.id())) {
            return false;
         }

         if (COSMETIC_RECIPIENT_DIALOGUES.contains(group.id())) {
            return true;
         }
      }

      return group.speaker().equals(speakerType(speaker));
   }

   public static boolean requiresBabySpeaker(String groupId) {
      return BABY_DIALOGUES.contains(groupId);
   }

   public static long playTestDialogue(ServerLevel level, LivingEntity speaker, DialogueCatalog.DialogueGroup group, int variantIndex, Entity target) {
      interrupt(speaker);
      DialogueCatalog.DialogueVariant variant = group.variants().stream().filter(candidate -> candidate.index() == variantIndex).findFirst().orElse(null);
      if (variant == null) {
         return 0L;
      } else {
         long duration = variant.durationTicks();
         DialogueAnimationNetwork.send(level, speaker, group.id(), variant.index(), (int)duration);
         ACTIVE_SOUNDS.put(speaker.getUUID(), new ContextualDialogueController.ActiveSound(group.id(), ticks + duration));
         markBusy(speaker, duration + 10L);
         if (speaker instanceof Mob mob) {
            Vec3 position = target == null ? null : target.getEyePosition();
            SPEECH_TARGETS.put(
               speaker.getUUID(), new ContextualDialogueController.SpeechTarget(target == null ? null : target.getUUID(), position, ticks + duration, true)
            );
            holdMob(mob, position);
         }

         return duration;
      }
   }

   public static void stopTestDialogue(LivingEntity speaker) {
      interrupt(speaker);
   }

   public static void onTradeCompleted(LivingEntity trader, Player player) {
      if (player != null) {
         ContextualDialogueController.TradeSession session = ACTIVE_TRADES.get(player.getUUID());
         if (session != null && session.traderId.equals(trader.getUUID())) {
            session.completed = true;
         }
      }

      String id = null;
      if (trader instanceof Villager villager && cast(villager) == ContextualDialogueController.CastProfile.VILLAGER) {
         id = "xmkwxd";
      } else if (trader instanceof WanderingTrader wanderingTrader) {
         id = "bvrbhy";
      }

      if (id != null && trader.level() instanceof ServerLevel level) {
         long due = Math.max(ticks + 1L, BUSY_UNTIL.getOrDefault(trader.getUUID(), ticks) + 1L);
         PENDING_SPEECH.add(new ContextualDialogueController.PendingSpeech(level, trader.getUUID(), id, player == null ? null : player.getUUID(), due));
      }
   }

   private static boolean ready(String key, long cooldown) {
      return ticks - COOLDOWNS.getOrDefault(key, -4611686018427387904L) >= cooldown;
   }

   private static boolean isBusy(LivingEntity entity) {
      ContextualDialogueController.ActiveSound sound = ACTIVE_SOUNDS.get(entity.getUUID());
      return BUSY_UNTIL.getOrDefault(entity.getUUID(), 0L) > ticks || sound != null && sound.endTick > ticks;
   }

   private static boolean isPlaying(LivingEntity entity, String groupId) {
      ContextualDialogueController.ActiveSound sound = ACTIVE_SOUNDS.get(entity.getUUID());
      return sound != null && sound.endTick > ticks && sound.groupId.equals(groupId);
   }

   private static boolean hasDamageLock(LivingEntity entity) {
      ContextualDialogueController.ActiveSound sound = ACTIVE_SOUNDS.get(entity.getUUID());
      return sound != null && sound.endTick > ticks && DAMAGE_LOCK_DIALOGUES.contains(sound.groupId);
   }

   private static void markBusy(LivingEntity entity, long duration) {
      BUSY_UNTIL.put(entity.getUUID(), ticks + duration);
   }

   private static void holdListener(LivingEntity listener, Entity speaker, long duration) {
      markBusy(listener, duration);
      if (listener instanceof Mob mob) {
         Vec3 position = speaker.getEyePosition();
         SPEECH_TARGETS.put(listener.getUUID(), new ContextualDialogueController.SpeechTarget(speaker.getUUID(), position, ticks + duration, true));
         holdMob(mob, position);
      }
   }

   private static void holdMob(Mob mob, Vec3 position) {
      mob.getNavigation().stop();
      mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      mob.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
      if (mob.onGround() && !mob.isPassenger()) {
         Vec3 movement = mob.getDeltaMovement();
         mob.setDeltaMovement(0.0, movement.y, 0.0);
      }

      faceMob(mob, position);
   }

   private static void faceMob(Mob mob, Vec3 position) {
      if (position != null) {
         mob.getLookControl().setLookAt(position.x, position.y, position.z, 15.0F, 10.0F);
         double x = position.x - mob.getX();
         double y = position.y - mob.getEyeY();
         double z = position.z - mob.getZ();
         double horizontal = Math.sqrt(x * x + z * z);
         if (horizontal > 0.01) {
            float targetYaw = (float)(Mth.atan2(z, x) * 180.0 / Math.PI) - 90.0F;
            float headOffset = Mth.clamp(Mth.wrapDegrees(targetYaw - mob.yBodyRot), -65.0F, 65.0F);
            float bodyYaw = easedRotation(mob.yBodyRot, targetYaw - headOffset, 12.0F, 0.35F);
            float headYaw = easedRotation(mob.getYHeadRot(), targetYaw, 15.0F, 0.4F);
            mob.setYRot(bodyYaw);
            mob.setYBodyRot(bodyYaw);
            mob.setYHeadRot(headYaw);
         }

         float targetPitch = (float)(-(Mth.atan2(y, horizontal) * 180.0 / Math.PI));
         mob.setXRot(easedRotation(mob.getXRot(), Mth.clamp(targetPitch, -90.0F, 90.0F), 10.0F, 0.3F));
      }
   }

   private static float easedRotation(float current, float target, float maximumStep, float proportion) {
      float distance = Math.abs(Mth.wrapDegrees(target - current));
      return Mth.approachDegrees(current, target, Mth.clamp(distance * proportion, 0.2F, maximumStep));
   }

   private static void interrupt(LivingEntity speaker) {
      if (speaker.level() instanceof ServerLevel level) {
         ACTIVE_SOUNDS.remove(speaker.getUUID());
         DialogueAnimationNetwork.stop(level, speaker);
         BUSY_UNTIL.remove(speaker.getUUID());
         SPEECH_TARGETS.remove(speaker.getUUID());
      }
   }

   private static boolean isWooly(Sheep sheep) {
      String name = sheep.getName().getString().toLowerCase(Locale.ROOT);
      return name.equals("wooly") || name.equals("wooly the sheep");
   }

   private static void normalizeSpecialEntity(Entity entity) {
      Component customName = entity.getCustomName();
      if (customName != null) {
         String name = customName.getString();
         String prefix = "{\"text\":\"";
         if (name.startsWith(prefix) && name.endsWith("\"}")) {
            String decoded = name.substring(prefix.length(), name.length() - 2);
            if (SPECIAL_NAMES.contains(decoded)) {
               entity.setCustomName(Component.literal(decoded));
            }
         }
      }
   }

   private static VillagerNewsData data(Villager villager) {
      return (VillagerNewsData)villager;
   }

   private static void ensureSpecialTrade(Villager villager) {
      ContextualDialogueController.CastProfile profile = cast(villager);

      Item result = switch (profile) {
         case MAYOR -> VillagerNewsItems.MAYOR_HAT;
         case TESTIFICATE_MAN -> VillagerNewsItems.TESTIFICATE_MAN_HELMET;
         case NUMBER_5 -> VillagerNewsItems.MOUSTACHE;
         case NUMBER_9 -> VillagerNewsItems.MICROPHONE;
         default -> null;
      };
      VillagerNewsData state = data(villager);
      if (result == null) {
         if (state.vnap$hasOriginalVillagerState()) {
            state.vnap$restoreOriginalVillagerState();
         }
      } else {
         state.vnap$captureOriginalVillagerState();
         if (villager.getVillagerData().getProfession() != VillagerProfession.NONE) {
            villager.setVillagerData(villager.getVillagerData().setProfession(VillagerProfession.NONE).setLevel(1));
         }

         villager.getOffers().removeIf(offer -> offer.getResult().getItem() != result);
         if (!villager.getOffers().stream().anyMatch(offer -> offer.getResult().getItem() == result)) {
            int price = profile == ContextualDialogueController.CastProfile.MAYOR ? 24 : 16;
            villager.getOffers().add(new MerchantOffer(new ItemStack(Items.EMERALD, price), new ItemStack(result), 16, 2, 0.1F));
         }
      }
   }

   public static boolean isSpecialTrader(Villager villager) {
      return switch (cast(villager)) {
         case MAYOR, TESTIFICATE_MAN, NUMBER_5, NUMBER_9 -> true;
         default -> false;
      };
   }

   private static boolean tryCreateNaturalSpecial(Villager villager, ServerLevel level) {
      if (VillagerNewsSettings.spawnSpecialVillagers() && data(villager).vnap$spawnReason() == MobSpawnType.STRUCTURE) {
         BlockPos spawn = level.getSharedSpawnPos();
         if (villager.distanceToSqr(Vec3.atCenterOf(spawn)) <= 1000000.0) {
            return false;
         } else {
            Scoreboard scoreboard = level.getServer().getScoreboard();
            Objective spawned = objective(scoreboard, "vnap_special");
            Objective xPosition = objective(scoreboard, "vnap_special_x");
            Objective zPosition = objective(scoreboard, "vnap_special_z");
            List<String> available = new ArrayList<>();

            for (String key : NATURAL_SPECIAL_NAMES.keySet()) {
               String holder = "$vnap_" + key;
               if (scoreboard.getOrCreatePlayerScore(holder, spawned).getScore() == 0) {
                  available.add(key);
               } else {
                  int x = scoreboard.getOrCreatePlayerScore(holder, xPosition).getScore();
                  int z = scoreboard.getOrCreatePlayerScore(holder, zPosition).getScore();
                  double dx = villager.getX() - x;
                  double dz = villager.getZ() - z;
                  if (dx * dx + dz * dz <= 22500.0) {
                     return false;
                  }
               }
            }

            if (available.isEmpty()) {
               return false;
            } else {
               String keyx = available.get(ThreadLocalRandom.current().nextInt(available.size()));
               if (keyx.equals("wooly")) {
                  Sheep sheep = (Sheep)EntityType.SHEEP.create(level);
                  if (sheep == null) {
                     return false;
                  }

                  sheep.copyPosition(villager);
                  sheep.setCustomName(Component.literal(NATURAL_SPECIAL_NAMES.get(keyx)));
                  sheep.setPersistenceRequired();
                  sheep.setColor(DyeColor.WHITE);
                  sheep.addTag("vnap_natural_special");
                  if (!level.addFreshEntity(sheep)) {
                     return false;
                  }

                  villager.discard();
               } else {
                  villager.setCustomName(Component.literal(NATURAL_SPECIAL_NAMES.get(keyx)));
                  villager.setPersistenceRequired();
                  villager.addTag("vnap_natural_special");
               }

               String holder = "$vnap_" + keyx;
               scoreboard.getOrCreatePlayerScore(holder, spawned).setScore(1);
               scoreboard.getOrCreatePlayerScore(holder, xPosition).setScore(villager.blockPosition().getX());
               scoreboard.getOrCreatePlayerScore(holder, zPosition).setScore(villager.blockPosition().getZ());
               return keyx.equals("wooly");
            }
         }
      } else {
         return false;
      }
   }

   private static void clearNaturalSpecial(ServerLevel level, Entity entity) {
      String key = naturalSpecialKey(entity);
      if (key != null) {
         Scoreboard scoreboard = level.getServer().getScoreboard();
         String holder = "$vnap_" + key;
         scoreboard.getOrCreatePlayerScore(holder, objective(scoreboard, "vnap_special")).setScore(0);
      }
   }

   private static String naturalSpecialKey(Entity entity) {
      if (entity instanceof Sheep sheep && isWooly(sheep)) {
         return "wooly";
      } else if (entity instanceof Villager villager) {
         return switch (cast(villager)) {
            case MAYOR -> "mayor";
            case TESTIFICATE_MAN -> "testificate";
            case NUMBER_5 -> "number_5";
            case NUMBER_9 -> "number_9";
            case UNREACHABLE -> "unreachable";
            default -> null;
         };
      } else {
         return null;
      }
   }

   private static Objective objective(Scoreboard scoreboard, String name) {
      Objective existing = scoreboard.getObjective(name);
      return existing == null ? scoreboard.addObjective(name, ObjectiveCriteria.DUMMY, Component.literal(name), ObjectiveCriteria.RenderType.INTEGER) : existing;
   }

   private static String orderedPair(UUID first, UUID second) {
      return first.compareTo(second) < 0 ? first + ":" + second : second + ":" + first;
   }

   private static String meetDialogue(ContextualDialogueController.CastProfile profile) {
      return switch (profile) {
         case MAYOR -> "lyatyf";
         case TESTIFICATE_MAN -> "zvamyb";
         case NUMBER_5 -> "kmvqxe";
         case NUMBER_9 -> "sifqsj";
         default -> null;
      };
   }

   private static ContextualDialogueController.CastProfile cast(Villager villager) {
      String name = villager.getName().getString().toLowerCase(Locale.ROOT);
      if (name.equals("mayor") || name.equals("the mayor") || name.equals("mayor villager")) {
         return ContextualDialogueController.CastProfile.MAYOR;
      } else if (name.equals("testificate man")) {
         return ContextualDialogueController.CastProfile.TESTIFICATE_MAN;
      } else if (name.equals("villager #5") || name.equals("villager number 5")) {
         return ContextualDialogueController.CastProfile.NUMBER_5;
      } else if (name.equals("villager #9") || name.equals("villager number 9")) {
         return ContextualDialogueController.CastProfile.NUMBER_9;
      } else {
         return !name.equals("villager unreachable") && !name.equals("can't catch me!")
            ? ContextualDialogueController.CastProfile.VILLAGER
            : ContextualDialogueController.CastProfile.UNREACHABLE;
      }
   }

   private static String speakerType(LivingEntity speaker) {
      if (speaker instanceof Villager villager) {
         return switch (cast(villager)) {
            case VILLAGER -> "villager";
            case MAYOR -> "mayor";
            case TESTIFICATE_MAN -> "testificate_man";
            case NUMBER_5 -> "number_5";
            case NUMBER_9 -> "number_9";
            case UNREACHABLE -> "unreachable";
         };
      } else if (speaker instanceof WanderingTrader) {
         return "wandering_trader";
      } else {
         return speaker instanceof Sheep sheep && isWooly(sheep) ? "wooly" : "";
      }
   }

   private record ActiveSound(String groupId, long endTick) {
   }

   private static enum CastProfile {
      VILLAGER("xfpjxq", "lvigit", "clbjww", "wyvzhk", "vevdkl"),
      MAYOR("dpwhhs", "xxehbq", "njyapy", "ssbhiv", "ltdnvy"),
      TESTIFICATE_MAN("nmwmrz", "luoibc", "mpbnsm", "fzoqwd", "fzoqwd"),
      NUMBER_5("xccwah", "legnsy", "sclaoa", "behifz", "behifz"),
      NUMBER_9("kzogzi", "ezgbfw", "snnkrl", "wrbvvp", "asuufu"),
      UNREACHABLE("eltxge", "eltxge", "eltxge", "wyvzhk", "vevdkl");

      private final String approach;
      private final String idle;
      private final String trade;
      private final String hurt;
      private final String attack;

      private CastProfile(String approach, String idle, String trade, String hurt, String attack) {
         this.approach = approach;
         this.idle = idle;
         this.trade = trade;
         this.hurt = hurt;
         this.attack = attack;
      }
   }

   private record PendingBell(ServerLevel level, Vec3 position, long dueTick) {
   }

   private record PendingBellReaction(ServerLevel level, UUID villagerId, Vec3 position, long dueTick, long expireTick) {
   }

   private record PendingConditionRelief(ServerLevel level, UUID villagerId, String dialogueId, long dueTick) {
   }

   private static final class PendingSleep {
      private final ServerLevel level;
      private final BlockPos bedPos;
      private long dueTick;
      private boolean bedtimeStarted;

      private PendingSleep(ServerLevel level, BlockPos bedPos, long dueTick, boolean bedtimeStarted) {
         this.level = level;
         this.bedPos = bedPos;
         this.dueTick = dueTick;
         this.bedtimeStarted = bedtimeStarted;
      }
   }

   private record PendingSpeech(ServerLevel level, UUID speakerId, String dialogueId, UUID targetId, long dueTick, boolean sharedAdult) {
      private PendingSpeech(ServerLevel level, UUID speakerId, String dialogueId, UUID targetId, long dueTick) {
         this(level, speakerId, dialogueId, targetId, dueTick, false);
      }
   }

   private record PendingWake(ServerLevel level, UUID villagerId, String dialogueId, UUID targetId, long dueTick) {
   }

   private static final class PlayerObservation {
      private Vec3 lastPosition = Vec3.ZERO;
      private int stillTicks;
      private int stareTicks;
      private int breakStreak;
      private long lastBreakTick = -4611686018427387904L;
      private String lastGameMode;
      private String lastPlayerContext;
      private BlockPos lastGroundPos = BlockPos.ZERO;
      private String lastGroundBlock = "";
   }

   private record SpeechTarget(UUID targetId, Vec3 position, long untilTick, boolean lockMovement) {
   }

   private record LastInteraction(int entityId, InteractionHand hand, long tick, InteractionResult result) {
   }

   private static final class TradeSession {
      private final ServerLevel level;
      private final UUID traderId;
      private final long createdTick;
      private boolean opened;
      private boolean completed;

      private TradeSession(ServerLevel level, UUID traderId, long createdTick) {
         this.level = level;
         this.traderId = traderId;
         this.createdTick = createdTick;
      }
   }

   private static final class UnreachableState {
      private long stateEnteredTick;
      private long nextPathTick;
      private boolean coolingDown;

      private UnreachableState(long tick) {
         this.stateEnteredTick = tick;
         this.nextPathTick = tick;
      }

      private void updateFleeing(long tick) {
         if (this.coolingDown && tick - this.stateEnteredTick > 240L) {
            this.coolingDown = false;
            this.stateEnteredTick = tick;
         }
      }

      private void stopFleeing(long tick) {
         if (this.coolingDown) {
            this.coolingDown = false;
            this.stateEnteredTick = tick;
         }
      }

      private boolean canTaunt(long tick) {
         return !this.coolingDown && tick - this.stateEnteredTick > 120L;
      }

      private void taunted(long tick) {
         this.coolingDown = true;
         this.stateEnteredTick = tick;
      }
   }

   private record VillagerSnapshot(
      boolean baby, String profession, int level, boolean working, String name, boolean poisoned, boolean slowed, boolean weakened, boolean suffocating
   ) {
   }

   private static List<ItemStack> inventoryStacks(Villager villager) {
      List<ItemStack> stacks = new ArrayList<>();
      for (int slot = 0; slot < villager.getInventory().getContainerSize(); slot++) {
         stacks.add(villager.getInventory().getItem(slot));
      }

      return stacks;
   }
}
