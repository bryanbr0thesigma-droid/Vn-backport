package com.backport.trial;

import com.backport.BackportSounds;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class VaultBlockEntity extends BlockEntity {
   public static BlockEntityType<VaultBlockEntity> TYPE;

   public static void register(net.minecraft.world.level.block.Block block) {
      TYPE = net.minecraft.core.Registry.register(net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE, com.backport.Backport.id("vault"),
         FabricBlockEntityTypeBuilder.create(VaultBlockEntity::new, block).build());
   }

   // config
   private String lootTable = "backport:chests/trial_chambers/reward";
   private String displayLootTable;
   private double activationRange = 4.0;
   private double deactivationRange = 4.5;
   private ItemStack keyItem = ItemStack.EMPTY;
   private boolean keyDefault = true;
   // server data
   private final Set<UUID> rewardedPlayers = new LinkedHashSet<>();
   private long stateResumesAt;
   private final List<ItemStack> itemsToEject = new ArrayList<>();
   private int totalEjections;
   private long lastFailSound;
   // shared
   private ItemStack displayItem = ItemStack.EMPTY;
   private Set<UUID> connected = new LinkedHashSet<>();
   public float spin;
   public float oSpin;

   public VaultBlockEntity(BlockPos pos, BlockState state) {
      super(TYPE, pos, state);
   }

   private ItemStack key() {
      return this.keyDefault ? new ItemStack(com.backport.BackportItems.TRIAL_KEY) : this.keyItem;
   }

   public ItemStack getDisplayItem() {
      return this.displayItem;
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      CompoundTag c = new CompoundTag();
      c.putString("loot_table", this.lootTable);
      if (this.displayLootTable != null) c.putString("override_loot_table_to_display", this.displayLootTable);
      c.putDouble("activation_range", this.activationRange);
      c.putDouble("deactivation_range", this.deactivationRange);
      c.put("key_item", this.key().save(new CompoundTag()));
      tag.put("config", c);
      CompoundTag s = new CompoundTag();
      ListTag rp = new ListTag();
      this.rewardedPlayers.forEach(u -> rp.add(NbtUtils.createUUID(u)));
      s.put("rewarded_players", rp);
      s.putLong("state_updating_resumes_at", this.stateResumesAt);
      ListTag items = new ListTag();
      this.itemsToEject.forEach(i -> items.add(i.save(new CompoundTag())));
      s.put("items_to_eject", items);
      s.putInt("total_ejections_needed", this.totalEjections);
      tag.put("server_data", s);
      tag.put("shared_data", this.sharedTag());
   }

   private CompoundTag sharedTag() {
      CompoundTag sh = new CompoundTag();
      if (!this.displayItem.isEmpty()) sh.put("display_item", this.displayItem.save(new CompoundTag()));
      ListTag cp = new ListTag();
      this.connected.forEach(u -> cp.add(NbtUtils.createUUID(u)));
      sh.put("connected_players", cp);
      sh.putDouble("connected_particles_range", this.deactivationRange);
      return sh;
   }

   public void load(CompoundTag tag) {
      super.load(tag);
      if (tag.contains("config")) {
         CompoundTag c = tag.getCompound("config");
         if (c.contains("loot_table")) this.lootTable = c.getString("loot_table");
         this.displayLootTable = c.contains("override_loot_table_to_display") ? c.getString("override_loot_table_to_display") : null;
         if (c.contains("activation_range")) this.activationRange = c.getDouble("activation_range");
         if (c.contains("deactivation_range")) this.deactivationRange = c.getDouble("deactivation_range");
         if (c.contains("key_item")) {
            CompoundTag k = c.getCompound("key_item");
            // structure files store {count, id}; saved form is {Count, id}
            if (k.contains("count") && !k.contains("Count")) k.putByte("Count", (byte) k.getInt("count"));
            this.keyItem = ItemStack.of(k);
            this.keyDefault = this.keyItem.isEmpty();
         }
      }
      if (tag.contains("server_data")) {
         CompoundTag s = tag.getCompound("server_data");
         this.rewardedPlayers.clear();
         for (Tag t : s.getList("rewarded_players", Tag.TAG_INT_ARRAY)) this.rewardedPlayers.add(NbtUtils.loadUUID(t));
         this.stateResumesAt = s.getLong("state_updating_resumes_at");
         this.itemsToEject.clear();
         for (Tag t : s.getList("items_to_eject", Tag.TAG_COMPOUND)) this.itemsToEject.add(ItemStack.of((CompoundTag) t));
         this.totalEjections = s.getInt("total_ejections_needed");
      }
      this.readShared(tag.getCompound("shared_data"));
   }

   private void readShared(CompoundTag sh) {
      this.displayItem = sh.contains("display_item") ? ItemStack.of(sh.getCompound("display_item")) : ItemStack.EMPTY;
      this.connected = new LinkedHashSet<>();
      for (Tag t : sh.getList("connected_players", Tag.TAG_INT_ARRAY)) this.connected.add(NbtUtils.loadUUID(t));
   }

   @Nullable
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public CompoundTag getUpdateTag() {
      CompoundTag t = new CompoundTag();
      t.put("shared_data", this.sharedTag());
      return t;
   }

   // ---- client ----
   public void tickClient(Level level, BlockPos pos, BlockState state) {
      this.oSpin = this.spin;
      this.spin = Mth.wrapDegrees(this.spin + 10.0F);
      RandomSource r = level.getRandom();
      boolean omin = state.getValue(VaultBlock.OMINOUS);
      if (r.nextFloat() <= 0.5F) {
         double x = pos.getX() + Mth.nextDouble(r, 0.1, 0.9), y = pos.getY() + Mth.nextDouble(r, 0.25, 0.75), z = pos.getZ() + Mth.nextDouble(r, 0.1, 0.9);
         level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
         if (!this.displayItem.isEmpty()) level.addParticle(omin ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMALL_FLAME, x, y, z, 0, 0, 0);
      }
      if (level.getGameTime() % 20L == 0L && !this.connected.isEmpty()) {
         Direction f = state.getValue(VaultBlock.FACING);
         Vec3 key = Vec3.atBottomCenterOf(pos).add(f.getStepX() * 0.5, 1.75, f.getStepZ() * 0.5);
         for (UUID u : this.connected) {
            Player p = level.getPlayerByUUID(u);
            if (p != null && p.blockPosition().distSqr(pos) <= Mth.square(this.deactivationRange)) {
               Vec3 dir = key.vectorTo(p.position().add(0.0, p.getBbHeight() / 2.0F, 0.0));
               int n = Mth.nextInt(r, 2, 5);
               for (int i = 0; i < n; i++) {
                  Vec3 d = dir.offsetRandom(r, 1.0F).scale(0.05);
                  level.addParticle(ParticleTypes.ENCHANT, key.x, key.y, key.z, d.x * 20, d.y * 20, d.z * 20);
               }
            }
         }
      }
      if (!this.displayItem.isEmpty() && r.nextFloat() <= 0.02F) {
         level.playLocalSound(pos, BackportSounds.BLOCK_VAULT_AMBIENT, SoundSource.BLOCKS, r.nextFloat() * 0.25F + 0.75F, r.nextFloat() + 0.5F, false);
      }
   }

   public void handleUpdateTag(CompoundTag tag) {
      this.readShared(tag.getCompound("shared_data"));
   }

   // ---- server ----
   private boolean dirtyShared;

   public void tickServer(ServerLevel level, BlockPos pos, BlockState state) {
      VaultState cur = state.getValue(VaultBlock.STATE);
      if (level.getGameTime() % 20L == 0L && cur == VaultState.ACTIVE) this.cycleDisplay(level, cur, pos);
      BlockState next = state;
      if (level.getGameTime() >= this.stateResumesAt) {
         VaultState n = this.nextState(level, pos, cur);
         next = state.setValue(VaultBlock.STATE, n);
         if (next != state) this.setVaultState(level, pos, state, next);
      }
      if (this.dirtyShared) {
         this.setChanged();
         level.sendBlockUpdated(pos, state, next, 2);
         this.dirtyShared = false;
      }
   }

   private void setDisplay(ItemStack s) {
      if (!ItemStack.matches(this.displayItem, s)) {
         this.displayItem = s.copy();
         this.dirtyShared = true;
         this.setChanged();
      }
   }

   private void updateConnected(ServerLevel level, BlockPos pos, double range) {
      Set<UUID> now = new HashSet<>();
      for (Player p : level.getPlayers(p -> !p.isSpectator() && p.blockPosition().closerThan(pos, range))) {
         if (!this.rewardedPlayers.contains(p.getUUID())) now.add(p.getUUID());
      }
      if (!this.connected.equals(now)) {
         this.connected = new LinkedHashSet<>(now);
         this.dirtyShared = true;
         this.setChanged();
      }
   }

   private VaultState connectedState(ServerLevel level, BlockPos pos, double range) {
      this.updateConnected(level, pos, range);
      this.stateResumesAt = level.getGameTime() + 20L;
      return this.connected.isEmpty() ? VaultState.INACTIVE : VaultState.ACTIVE;
   }

   private VaultState nextState(ServerLevel level, BlockPos pos, VaultState cur) {
      switch (cur) {
         case INACTIVE:
            return this.connectedState(level, pos, this.activationRange);
         case ACTIVE:
            return this.connectedState(level, pos, this.deactivationRange);
         case UNLOCKING:
            this.stateResumesAt = level.getGameTime() + 20L;
            return VaultState.EJECTING;
         default: {
            if (this.itemsToEject.isEmpty()) {
               this.totalEjections = 0;
               return this.connectedState(level, pos, this.deactivationRange);
            }
            float progress = this.totalEjections == 1 ? 1.0F : 1.0F - Mth.inverseLerp((float) this.itemsToEject.size(), 1.0F, (float) this.totalEjections);
            ItemStack out = this.itemsToEject.remove(this.itemsToEject.size() - 1);
            DefaultDispenseItemBehavior.spawnItem(level, out, 2, Direction.UP, Vec3.atBottomCenterOf(pos).relative(Direction.UP, 1.2));
            TrialFx.event(level, 3017, pos, 0);
            level.playSound(null, pos, BackportSounds.BLOCK_VAULT_EJECT_ITEM, SoundSource.BLOCKS, 1.0F, 0.8F + 0.4F * progress);
            this.setDisplay(this.itemsToEject.isEmpty() ? ItemStack.EMPTY : this.itemsToEject.get(this.itemsToEject.size() - 1));
            this.stateResumesAt = level.getGameTime() + 20L;
            this.setChanged();
            return VaultState.EJECTING;
         }
      }
   }

   private void setVaultState(ServerLevel level, BlockPos pos, BlockState from, BlockState to) {
      VaultState a = from.getValue(VaultBlock.STATE);
      VaultState b = to.getValue(VaultBlock.STATE);
      level.setBlockAndUpdate(pos, to);
      boolean omin = to.getValue(VaultBlock.OMINOUS);
      if (a == VaultState.EJECTING) level.playSound(null, pos, BackportSounds.BLOCK_VAULT_CLOSE_SHUTTER, SoundSource.BLOCKS, 1.0F, 1.0F);
      switch (b) {
         case INACTIVE -> {
            this.setDisplay(ItemStack.EMPTY);
            TrialFx.event(level, 3016, pos, omin ? 1 : 0);
         }
         case ACTIVE -> {
            if (this.displayItem.isEmpty()) this.cycleDisplay(level, b, pos);
            TrialFx.event(level, 3015, pos, omin ? 1 : 0);
         }
         case UNLOCKING -> level.playSound(null, pos, BackportSounds.BLOCK_VAULT_INSERT_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
         case EJECTING -> level.playSound(null, pos, BackportSounds.BLOCK_VAULT_OPEN_SHUTTER, SoundSource.BLOCKS, 1.0F, 1.0F);
      }
   }

   private boolean canEject(VaultState st) {
      return !this.key().isEmpty() && st != VaultState.INACTIVE;
   }

   private void cycleDisplay(ServerLevel level, VaultState st, BlockPos pos) {
      if (!this.canEject(st)) {
         this.setDisplay(ItemStack.EMPTY);
         return;
      }
      LootTable lt = level.getServer().getLootData().getLootTable(new ResourceLocation(this.displayLootTable != null ? this.displayLootTable : this.lootTable));
      LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos)).create(LootContextParamSets.CHEST);
      List<ItemStack> results = lt.getRandomItems(params);
      this.setDisplay(results.isEmpty() ? ItemStack.EMPTY : results.get(level.getRandom().nextInt(results.size())));
   }

   private void failSound(ServerLevel level, BlockPos pos, net.minecraft.sounds.SoundEvent s) {
      if (level.getGameTime() >= this.lastFailSound + 15L) {
         level.playSound(null, pos, s, SoundSource.BLOCKS, 1.0F, 1.0F);
         this.lastFailSound = level.getGameTime();
      }
   }

   public void tryInsertKey(ServerLevel level, BlockPos pos, BlockState state, Player player, ItemStack stack) {
      VaultState st = state.getValue(VaultBlock.STATE);
      if (!this.canEject(st)) return;
      ItemStack key = this.key();
      if (!(ItemStack.isSameItemSameTags(stack, key) && stack.getCount() >= key.getCount())) {
         this.failSound(level, pos, BackportSounds.BLOCK_VAULT_INSERT_ITEM_FAIL);
      } else if (this.rewardedPlayers.contains(player.getUUID())) {
         this.failSound(level, pos, BackportSounds.BLOCK_VAULT_REJECT_REWARDED_PLAYER);
      } else {
         LootTable lt = level.getServer().getLootData().getLootTable(new ResourceLocation(this.lootTable));
         LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
            .withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player).create(LootContextParamSets.CHEST);
         List<ItemStack> items = lt.getRandomItems(params);
         if (items.isEmpty()) return;
         player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
         if (!player.getAbilities().instabuild) stack.shrink(key.getCount());
         this.itemsToEject.clear();
         this.itemsToEject.addAll(items);
         this.totalEjections = items.size();
         this.setDisplay(this.itemsToEject.get(this.itemsToEject.size() - 1));
         this.stateResumesAt = level.getGameTime() + 14L;
         this.setVaultState(level, pos, state, state.setValue(VaultBlock.STATE, VaultState.UNLOCKING));
         this.rewardedPlayers.add(player.getUUID());
         this.updateConnected(level, pos, this.deactivationRange);
         this.setChanged();
      }
   }
}
