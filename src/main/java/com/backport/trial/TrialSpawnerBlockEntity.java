package com.backport.trial;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class TrialSpawnerBlockEntity extends BlockEntity {
   
   public static net.minecraft.world.level.block.entity.BlockEntityType<TrialSpawnerBlockEntity> TYPE;

   public static void register(net.minecraft.world.level.block.Block block) {
      TYPE = net.minecraft.core.Registry.register(net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE, com.backport.Backport.id("trial_spawner"),
         FabricBlockEntityTypeBuilder.create(TrialSpawnerBlockEntity::new, block).build());
   }

   private String normalId = "";
   private String ominousId = "";
   private int cooldownLength = 36000;
   private int playerRange = 14;
   private final Set<UUID> detectedPlayers = new HashSet<>();
   private final Set<UUID> currentMobs = new HashSet<>();
   private long cooldownEndsAt;
   private long nextMobSpawnsAt;
   private int totalMobsSpawned;
   private CompoundTag nextEntity;
   private TrialConfig.Spawn nextSpawn;
   private String ejectingLoot;
   private boolean ominous;
   // client
   @Nullable
   private Entity displayEntity;
   public double spin;
   public double oSpin;

   public TrialSpawnerBlockEntity(BlockPos pos, BlockState state) {
      super(TYPE, pos, state);
   }

   private TrialConfig config(ServerLevel level) {
      return TrialConfig.load(level.getServer(), this.ominous ? this.ominousId : this.normalId);
   }

   private TrialSpawnerState state() {
      return this.getBlockState().getValue(TrialSpawnerBlock.STATE);
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      tag.putString("normal_config", this.normalId);
      tag.putString("ominous_config", this.ominousId);
      tag.putInt("target_cooldown_length", this.cooldownLength);
      tag.putInt("required_player_range", this.playerRange);
      ListTag players = new ListTag();
      this.detectedPlayers.forEach(u -> players.add(NbtUtils.createUUID(u)));
      tag.put("registered_players", players);
      ListTag mobs = new ListTag();
      this.currentMobs.forEach(u -> mobs.add(NbtUtils.createUUID(u)));
      tag.put("current_mobs", mobs);
      tag.putLong("cooldown_ends_at", this.cooldownEndsAt);
      tag.putLong("next_mob_spawns_at", this.nextMobSpawnsAt);
      tag.putInt("total_mobs_spawned", this.totalMobsSpawned);
      if (this.nextEntity != null) tag.put("spawn_entity", this.nextEntity);
      if (this.ejectingLoot != null) tag.putString("ejecting_loot_table", this.ejectingLoot);
   }

   public void load(CompoundTag tag) {
      super.load(tag);
      this.normalId = tag.getString("normal_config");
      this.ominousId = tag.getString("ominous_config");
      if (tag.contains("target_cooldown_length")) this.cooldownLength = tag.getInt("target_cooldown_length");
      if (tag.contains("required_player_range")) this.playerRange = tag.getInt("required_player_range");
      this.detectedPlayers.clear();
      for (Tag t : tag.getList("registered_players", Tag.TAG_INT_ARRAY)) this.detectedPlayers.add(NbtUtils.loadUUID(t));
      this.currentMobs.clear();
      for (Tag t : tag.getList("current_mobs", Tag.TAG_INT_ARRAY)) this.currentMobs.add(NbtUtils.loadUUID(t));
      this.cooldownEndsAt = tag.getLong("cooldown_ends_at");
      this.nextMobSpawnsAt = tag.getLong("next_mob_spawns_at");
      this.totalMobsSpawned = tag.getInt("total_mobs_spawned");
      this.nextEntity = tag.contains("spawn_entity") ? tag.getCompound("spawn_entity") : null;
      this.ejectingLoot = tag.contains("ejecting_loot_table") ? tag.getString("ejecting_loot_table") : null;
      this.displayEntity = null;
   }

   @Nullable
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public CompoundTag getUpdateTag() {
      CompoundTag tag = new CompoundTag();
      tag.putLong("next_mob_spawns_at", this.nextMobSpawnsAt);
      if (this.nextEntity != null) tag.put("spawn_entity", this.nextEntity);
      return tag;
   }

   private void markUpdated() {
      this.setChanged();
      if (this.level != null) this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
   }

   private void setState(Level level, TrialSpawnerState s) {
      this.setChanged();
      level.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(TrialSpawnerBlock.STATE, s));
   }

   /** Called from egg/command-style overrides. */
   public void setEntityId(EntityType<?> type) {
      if (this.level == null) return;
      CompoundTag e = new CompoundTag();
      e.putString("id", net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
      this.nextEntity = e;
      this.currentMobs.clear();
      this.detectedPlayers.clear();
      this.totalMobsSpawned = 0;
      this.setState(this.level, TrialSpawnerState.INACTIVE);
      this.markUpdated();
   }

   // ----- server logic -----
   private boolean canSpawnInLevel(ServerLevel level) {
      if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) return false;
      return level.getDifficulty() != Difficulty.PEACEFUL;
   }

   private boolean hasMobToSpawn(ServerLevel level) {
      this.ensureNext(level);
      return (this.nextEntity != null && this.nextEntity.contains("id")) || !this.config(level).spawns.isEmpty();
   }

   private void ensureNext(ServerLevel level) {
      if (this.nextEntity == null) {
         TrialConfig.Spawn s = this.config(level).randomSpawn(level.random);
         this.nextSpawn = s;
         this.nextEntity = s == null ? new CompoundTag() : s.entity.copy();
         this.markUpdated();
      }
   }

   private TrialConfig.Spawn nextSpawn(ServerLevel level) {
      this.ensureNext(level);
      if (this.nextSpawn == null) {
         for (TrialConfig.Spawn s : this.config(level).spawns) {
            if (s.entity.equals(this.nextEntity)) {
               this.nextSpawn = s;
               break;
            }
         }
      }
      return this.nextSpawn;
   }

   private static boolean inLineOfSight(Level level, Vec3 origin, Vec3 dest) {
      BlockHitResult hit = level.clip(new ClipContext(dest, origin, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, new net.minecraft.world.entity.decoration.ArmorStand(level, 0.0, 0.0, 0.0)));
      return hit.getBlockPos().equals(BlockPos.containing(origin)) || hit.getType() == HitResult.Type.MISS;
   }

   private List<UUID> detect(ServerLevel level, BlockPos pos, boolean los) {
      return level.getPlayers(p -> p.blockPosition().closerThan(pos, this.playerRange) && !p.isCreative() && !p.isSpectator()).stream()
         .filter(p -> !los || inLineOfSight(level, Vec3.atCenterOf(pos), p.getEyePosition()))
         .map(Entity::getUUID).toList();
   }

   private void tryDetectPlayers(ServerLevel level, BlockPos pos) {
      if ((pos.asLong() + level.getGameTime()) % 20L != 0L) return;
      TrialSpawnerState st = this.state();
      if (st == TrialSpawnerState.COOLDOWN && this.ominous) return;
      List<UUID> los = this.detect(level, pos, true);
      boolean becameOminous = false;
      if (!this.ominous && !los.isEmpty()) {
         for (UUID u : los) {
            Player p = level.getPlayerByUUID(u);
            if (p != null && p.hasEffect(MobEffects.BAD_OMEN)) {
               p.removeEffect(MobEffects.BAD_OMEN);
               TrialFx.event(level, 3020, BlockPos.containing(p.getEyePosition()), 0);
               this.applyOminous(level, pos);
               becameOminous = true;
               break;
            }
         }
      }
      if (st != TrialSpawnerState.COOLDOWN || becameOminous) {
         boolean first = this.detectedPlayers.isEmpty();
         List<UUID> found = first ? los : this.detect(level, pos, false);
         if (this.detectedPlayers.addAll(found)) {
            this.nextMobSpawnsAt = Math.max(level.getGameTime() + 40L, this.nextMobSpawnsAt);
            if (!becameOminous) TrialFx.event(level, this.ominous ? 3019 : 3013, pos, this.detectedPlayers.size());
         }
      }
   }

   private void applyOminous(ServerLevel level, BlockPos pos) {
      level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(TrialSpawnerBlock.OMINOUS, true));
      TrialFx.event(level, 3020, pos, 1);
      this.ominous = true;
      for (UUID id : this.currentMobs) {
         Entity e = level.getEntity(id);
         if (e != null) {
            TrialFx.event(level, 3012, e.blockPosition(), 0);
            e.discard();
         }
      }
      TrialConfig cfg = this.config(level);
      if (!cfg.spawns.isEmpty()) {
         this.nextEntity = null;
         this.nextSpawn = null;
      }
      this.totalMobsSpawned = 0;
      this.currentMobs.clear();
      this.nextMobSpawnsAt = level.getGameTime() + cfg.ticksBetweenSpawn;
      this.cooldownEndsAt = level.getGameTime() + 160L;
      this.markUpdated();
   }

   private void removeOminous(ServerLevel level, BlockPos pos) {
      level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(TrialSpawnerBlock.OMINOUS, false));
      this.ominous = false;
      this.nextEntity = null;
      this.nextSpawn = null;
   }

   private void resetStatistics() {
      this.detectedPlayers.clear();
      this.totalMobsSpawned = 0;
      this.nextMobSpawnsAt = 0L;
      this.cooldownEndsAt = 0L;
   }

   private void reset() {
      this.currentMobs.clear();
      this.nextEntity = null;
      this.nextSpawn = null;
      this.resetStatistics();
   }

   public void tickServer(ServerLevel level, BlockPos pos, boolean isOminous) {
      this.ominous = isOminous;
      TrialSpawnerState cur = this.state();
      TrialConfig cfg = this.config(level);
      if (this.currentMobs.removeIf(id -> untracked(level, pos, id))) {
         this.nextMobSpawnsAt = level.getGameTime() + cfg.ticksBetweenSpawn;
      }
      TrialSpawnerState next = this.next(cur, pos, level, cfg);
      if (next != cur) this.setState(level, next);
   }

   private static boolean untracked(ServerLevel level, BlockPos pos, UUID id) {
      Entity e = level.getEntity(id);
      return e == null || !e.isAlive() || e.blockPosition().distSqr(pos) > 47 * 47;
   }

   private TrialSpawnerState next(TrialSpawnerState cur, BlockPos pos, ServerLevel level, TrialConfig cfg) {
      RandomSource random = level.getRandom();
      switch (cur) {
         case INACTIVE: {
            this.ensureNext(level);
            return this.nextEntity == null || !this.nextEntity.contains("id") ? cur : TrialSpawnerState.WAITING_FOR_PLAYERS;
         }
         case WAITING_FOR_PLAYERS: {
            if (!this.canSpawnInLevel(level)) {
               this.resetStatistics();
               return cur;
            }
            if (!this.hasMobToSpawn(level)) return TrialSpawnerState.INACTIVE;
            this.tryDetectPlayers(level, pos);
            return this.detectedPlayers.isEmpty() ? cur : TrialSpawnerState.ACTIVE;
         }
         case ACTIVE: {
            if (!this.canSpawnInLevel(level)) {
               this.resetStatistics();
               return TrialSpawnerState.WAITING_FOR_PLAYERS;
            }
            if (!this.hasMobToSpawn(level)) return TrialSpawnerState.INACTIVE;
            int extra = Math.max(0, this.detectedPlayers.size() - 1);
            this.tryDetectPlayers(level, pos);
            if (this.ominous) this.spawnOminousItem(level, pos, cfg);
            if (this.totalMobsSpawned >= cfg.targetTotal(extra)) {
               if (this.currentMobs.isEmpty()) {
                  this.cooldownEndsAt = level.getGameTime() + this.cooldownLength;
                  this.totalMobsSpawned = 0;
                  this.nextMobSpawnsAt = 0L;
                  return TrialSpawnerState.WAITING_FOR_REWARD_EJECTION;
               }
            } else if (level.getGameTime() >= this.nextMobSpawnsAt && this.currentMobs.size() < cfg.targetSimultaneous(extra)) {
               this.spawnMob(level, pos, cfg).ifPresent(id -> {
                  this.currentMobs.add(id);
                  this.totalMobsSpawned++;
                  this.nextMobSpawnsAt = level.getGameTime() + cfg.ticksBetweenSpawn;
                  TrialConfig.Spawn s = cfg.randomSpawn(random);
                  if (s != null) {
                     this.nextSpawn = s;
                     this.nextEntity = s.entity.copy();
                     this.markUpdated();
                  }
               });
            }
            return cur;
         }
         case WAITING_FOR_REWARD_EJECTION: {
            long started = this.cooldownEndsAt - this.cooldownLength;
            if ((float) level.getGameTime() >= (float) started + 40.0F) {
               level.playSound(null, pos, com.backport.BackportSounds.BLOCK_TRIAL_SPAWNER_OPEN_SHUTTER, SoundSource.BLOCKS, 1.0F, 1.0F);
               return TrialSpawnerState.EJECTING_REWARD;
            }
            return cur;
         }
         case EJECTING_REWARD: {
            long started = this.cooldownEndsAt - this.cooldownLength;
            if ((float) (level.getGameTime() - started) % 30.0F != 0.0F) return cur;
            if (this.detectedPlayers.isEmpty()) {
               level.playSound(null, pos, com.backport.BackportSounds.BLOCK_TRIAL_SPAWNER_CLOSE_SHUTTER, SoundSource.BLOCKS, 1.0F, 1.0F);
               this.ejectingLoot = null;
               return TrialSpawnerState.COOLDOWN;
            }
            if (this.ejectingLoot == null) {
               TrialConfig.Loot l = cfg.randomLoot(random);
               this.ejectingLoot = l == null ? null : l.table;
            }
            if (this.ejectingLoot != null) this.ejectReward(level, pos, this.ejectingLoot);
            this.detectedPlayers.remove(this.detectedPlayers.iterator().next());
            return cur;
         }
         default: { // COOLDOWN
            this.tryDetectPlayers(level, pos);
            if (!this.detectedPlayers.isEmpty()) {
               this.totalMobsSpawned = 0;
               this.nextMobSpawnsAt = 0L;
               return TrialSpawnerState.ACTIVE;
            }
            if (level.getGameTime() >= this.cooldownEndsAt) {
               this.removeOminous(level, pos);
               this.reset();
               return TrialSpawnerState.WAITING_FOR_PLAYERS;
            }
            return cur;
         }
      }
   }

   private void spawnOminousItem(ServerLevel level, BlockPos pos, TrialConfig cfg) {
      if (level.getGameTime() < this.cooldownEndsAt) return;
      List<Player> players = this.detectedPlayers.stream().map(level::getPlayerByUUID).filter(p -> p != null && !p.isCreative() && !p.isSpectator() && p.isAlive()
         && p.distanceToSqr(Vec3.atCenterOf(pos)) <= Mth.square(this.playerRange)).toList();
      if (players.isEmpty()) return;
      Player target = players.get(level.random.nextInt(players.size()));
      LootTable table = level.getServer().getLootData().getLootTable(new ResourceLocation(cfg.ominousDrops));
      List<ItemStack> drops = table.getRandomItems(new LootParams.Builder(level).create(LootContextParamSets.EMPTY));
      if (drops.isEmpty()) return;
      ItemStack stack = drops.get(level.random.nextInt(drops.size())).copy();
      stack.setCount(1);
      Vec3 at = target.position().add(0.0, target.getBbHeight() + 2.0 + level.random.nextInt(3), 0.0);
      ItemEntity ie = new ItemEntity(level, at.x, at.y, at.z, stack);
      ie.setDeltaMovement(Vec3.ZERO);
      level.addFreshEntity(ie);
      level.playSound(null, BlockPos.containing(at), com.backport.BackportSounds.BLOCK_TRIAL_SPAWNER_SPAWN_ITEM_BEGIN, SoundSource.BLOCKS, 1.0F, 1.0F);
      this.cooldownEndsAt = level.getGameTime() + 160L;
   }

   private void ejectReward(ServerLevel level, BlockPos pos, String table) {
      LootTable lt = level.getServer().getLootData().getLootTable(new ResourceLocation(table));
      List<ItemStack> drops = lt.getRandomItems(new LootParams.Builder(level).create(LootContextParamSets.EMPTY));
      if (!drops.isEmpty()) {
         for (ItemStack s : drops) {
            DefaultDispenseItemBehavior.spawnItem(level, s, 2, Direction.UP, Vec3.atBottomCenterOf(pos).relative(Direction.UP, 1.2));
         }
         TrialFx.event(level, 3014, pos, 0);
      }
   }

   private Optional<UUID> spawnMob(ServerLevel level, BlockPos spawnerPos, TrialConfig cfg) {
      RandomSource random = level.getRandom();
      this.ensureNext(level);
      CompoundTag tag = this.nextEntity.copy();
      Optional<EntityType<?>> type = EntityType.by(tag);
      if (type.isEmpty()) return Optional.empty();
      Vec3 sp = new Vec3(
         spawnerPos.getX() + (random.nextDouble() - random.nextDouble()) * cfg.spawnRange + 0.5,
         spawnerPos.getY() + random.nextInt(3) - 1,
         spawnerPos.getZ() + (random.nextDouble() - random.nextDouble()) * cfg.spawnRange + 0.5);
      if (!level.noCollision(type.get().getAABB(sp.x, sp.y, sp.z))) return Optional.empty();
      if (!inLineOfSight(level, Vec3.atCenterOf(spawnerPos), sp)) return Optional.empty();
      BlockPos bp = BlockPos.containing(sp);
      Entity entity = EntityType.loadEntityRecursive(tag, level, e -> {
         e.moveTo(sp.x, sp.y, sp.z, random.nextFloat() * 360.0F, 0.0F);
         return e;
      });
      if (entity == null) return Optional.empty();
      if (entity instanceof Mob mob) {
         if (!mob.checkSpawnObstruction(level)) return Optional.empty();
         if (tag.size() == 1 && tag.contains("id")) {
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.SPAWNER, null, null);
         }
         mob.setPersistenceRequired();
         TrialConfig.Spawn s = this.nextSpawn(level);
         if (s != null && s.equipmentLoot != null) this.equip(level, mob, s);
      }
      if (!level.tryAddFreshEntityWithPassengers(entity)) return Optional.empty();
      int flame = this.ominous ? 1 : 0;
      TrialFx.event(level, 3011, spawnerPos, flame);
      TrialFx.event(level, 3012, bp, flame);
      level.gameEvent(entity, GameEvent.ENTITY_PLACE, bp);
      return Optional.of(entity.getUUID());
   }

   private void equip(ServerLevel level, Mob mob, TrialConfig.Spawn s) {
      LootTable lt = level.getServer().getLootData().getLootTable(new ResourceLocation(s.equipmentLoot));
      List<ItemStack> items = lt.getRandomItems(new LootParams.Builder(level).create(LootContextParamSets.EMPTY));
      for (ItemStack it : items) {
         EquipmentSlot slot = Mob.getEquipmentSlotForItem(it);
         mob.setItemSlot(slot, it);
         mob.setDropChance(slot, s.dropChance);
      }
   }

   // ----- client -----
   public void tickClient(Level level, BlockPos pos, boolean isOminous) {
      TrialSpawnerState st = this.state();
      RandomSource random = level.getRandom();
      ParticleOptions small = isOminous ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMALL_FLAME;
      ParticleOptions flame = isOminous ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME;
      switch (st) {
         case WAITING_FOR_PLAYERS, WAITING_FOR_REWARD_EJECTION, EJECTING_REWARD -> {
            if (random.nextInt(2) == 0) {
               Vec3 v = Vec3.atCenterOf(pos).offsetRandom(random, 0.9F);
               level.addParticle(small, v.x, v.y, v.z, 0, 0, 0);
            }
         }
         case ACTIVE -> {
            Vec3 v = Vec3.atCenterOf(pos).offsetRandom(random, 1.0F);
            level.addParticle(ParticleTypes.SMOKE, v.x, v.y, v.z, 0, 0, 0);
            level.addParticle(flame, v.x, v.y, v.z, 0, 0, 0);
         }
         case COOLDOWN -> {
            Vec3 v = Vec3.atCenterOf(pos).offsetRandom(random, 0.9F);
            if (random.nextInt(3) == 0) level.addParticle(ParticleTypes.SMOKE, v.x, v.y, v.z, 0, 0, 0);
            if (level.getGameTime() % 20L == 0L) {
               Vec3 top = Vec3.atCenterOf(pos).add(0.0, 0.5, 0.0);
               int n = random.nextInt(4) + 20;
               for (int i = 0; i < n; i++) level.addParticle(ParticleTypes.SMOKE, top.x, top.y, top.z, 0, 0, 0);
            }
         }
         default -> {
         }
      }
      if (st.hasSpinningMob()) {
         double delay = Math.max(0L, this.nextMobSpawnsAt - level.getGameTime());
         this.oSpin = this.spin;
         this.spin = (this.spin + st.spinSpeed / (delay + 200.0)) % 360.0;
      }
      if (st.capableOfSpawning && random.nextFloat() <= 0.02F) {
         level.playLocalSound(pos, isOminous ? com.backport.BackportSounds.BLOCK_TRIAL_SPAWNER_AMBIENT_OMINOUS : com.backport.BackportSounds.BLOCK_TRIAL_SPAWNER_AMBIENT,
            SoundSource.BLOCKS, random.nextFloat() * 0.25F + 0.75F, random.nextFloat() + 0.5F, false);
      }
   }

   @Nullable
   public Entity getDisplayEntity(Level level) {
      if (!this.state().hasSpinningMob()) return null;
      if (this.displayEntity == null && this.nextEntity != null && this.nextEntity.contains("id")) {
         CompoundTag copy = this.nextEntity.copy();
         this.displayEntity = EntityType.loadEntityRecursive(copy, level, e -> e);
         if (this.displayEntity != null) this.displayEntity.setNoGravity(true);
      }
      return this.displayEntity;
   }
}
