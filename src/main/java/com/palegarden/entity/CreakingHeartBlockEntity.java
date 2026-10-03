package com.palegarden.entity;

import com.mojang.datafixers.util.Either;
import com.palegarden.PaleBlocks;
import com.palegarden.PaleEntities;
import com.palegarden.PaleSounds;
import com.palegarden.block.CreakingHeartBlock;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.joml.Vector3f;

public class CreakingHeartBlockEntity extends BlockEntity {
   public static final BlockEntityType<CreakingHeartBlockEntity> TYPE = FabricBlockEntityTypeBuilder.create(CreakingHeartBlockEntity::new, PaleBlocks.CREAKING_HEART).build();
   private static final Optional<Creaking> NO_CREAKING = Optional.empty();
   @Nullable
   private Either<Creaking, UUID> creakingInfo;
   private long ticksExisted;
   private int ticker;
   private int emitter;
   @Nullable
   private Vec3 emitterTarget;
   private int outputSignal;

   public CreakingHeartBlockEntity(BlockPos pos, BlockState state) {
      super(TYPE, pos, state);
   }

   public static void init() {
      net.minecraft.core.Registry.register(net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE, com.palegarden.PaleGarden.id("creaking_heart"), TYPE);
   }

   public static void serverTick(Level level, BlockPos pos, BlockState state, CreakingHeartBlockEntity heart) {
      heart.ticksExisted++;
      if (level instanceof ServerLevel serverLevel) {
         int signal = heart.computeAnalogOutputSignal();
         if (heart.outputSignal != signal) {
            heart.outputSignal = signal;
            level.updateNeighbourForOutputSignal(pos, PaleBlocks.CREAKING_HEART);
         }

         if (heart.emitter > 0) {
            if (heart.emitter > 50) {
               heart.emitParticles(serverLevel, 1, true);
               heart.emitParticles(serverLevel, 1, false);
            }

            if (heart.emitter % 10 == 0 && heart.emitterTarget != null) {
               heart.getCreakingProtector().ifPresent(c -> heart.emitterTarget = c.getBoundingBox().getCenter());
               Vec3 center = Vec3.atCenterOf(pos);
               float f = 0.2F + 0.8F * (100 - heart.emitter) / 100.0F;
               Vec3 at = center.subtract(heart.emitterTarget).scale(f).add(heart.emitterTarget);
               BlockPos soundPos = BlockPos.containing(at);
               float volume = heart.emitter / 2.0F / 100.0F + 0.5F;
               serverLevel.playSound(null, soundPos, PaleSounds.BLOCK_CREAKING_HEART_HURT, SoundSource.BLOCKS, volume, 1.0F);
            }

            heart.emitter--;
         }

         if (heart.ticker-- < 0) {
            heart.ticker = heart.level == null ? 20 : heart.level.random.nextInt(5) + 20;
            if (heart.creakingInfo == null) {
               if (!CreakingHeartBlock.hasRequiredLogs(state, level, pos)) {
                  level.setBlock(pos, state.setValue(CreakingHeartBlock.ACTIVE, false), 3);
               } else if (state.getValue(CreakingHeartBlock.ACTIVE)
                  && CreakingHeartBlock.isNaturalNight(level)
                  && level.getDifficulty() != Difficulty.PEACEFUL
                  && serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
                  Player player = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 32.0, false);
                  if (player != null) {
                     Creaking creaking = spawnProtector(serverLevel, heart);
                     if (creaking != null) {
                        heart.setCreakingInfo(creaking);
                        creaking.playCreakingSound(PaleSounds.ENTITY_CREAKING_SPAWN);
                        level.playSound(null, heart.getBlockPos(), PaleSounds.BLOCK_CREAKING_HEART_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);
                     }
                  }
               }
            } else {
               Optional<Creaking> protector = heart.getCreakingProtector();
               if (protector.isPresent()) {
                  Creaking creaking = protector.get();
                  if (!CreakingHeartBlock.isNaturalNight(level) || heart.distanceToCreaking() > 34.0 || creaking.playerIsStuckInYou()) {
                     heart.removeProtector(null);
                     return;
                  }

                  if (!CreakingHeartBlock.hasRequiredLogs(state, level, pos) && heart.creakingInfo == null) {
                     level.setBlock(pos, state.setValue(CreakingHeartBlock.ACTIVE, false), 3);
                  }
               }
            }
         }
      }
   }

   private double distanceToCreaking() {
      return this.getCreakingProtector().map(c -> Math.sqrt(c.distanceToSqr(Vec3.atBottomCenterOf(this.getBlockPos())))).orElse(0.0);
   }

   private void clearCreakingInfo() {
      this.creakingInfo = null;
      this.setChanged();
   }

   public void setCreakingInfo(Creaking creaking) {
      this.creakingInfo = Either.left(creaking);
      this.setChanged();
   }

   public void setCreakingInfo(UUID uuid) {
      this.creakingInfo = Either.right(uuid);
      this.ticksExisted = 0L;
      this.setChanged();
   }

   private Optional<Creaking> getCreakingProtector() {
      if (this.creakingInfo == null) {
         return NO_CREAKING;
      } else {
         if (this.creakingInfo.left().isPresent()) {
            Creaking creaking = this.creakingInfo.left().get();
            if (!creaking.isRemoved()) {
               return Optional.of(creaking);
            }

            this.setCreakingInfo(creaking.getUUID());
         }

         if (this.level instanceof ServerLevel serverLevel && this.creakingInfo.right().isPresent()) {
            UUID uuid = this.creakingInfo.right().get();
            if (serverLevel.getEntity(uuid) instanceof Creaking found) {
               this.setCreakingInfo(found);
               return Optional.of(found);
            } else {
               if (this.ticksExisted >= 30L) {
                  this.clearCreakingInfo();
               }

               return NO_CREAKING;
            }
         } else {
            return NO_CREAKING;
         }
      }
   }

   @Nullable
   private static Creaking spawnProtector(ServerLevel level, CreakingHeartBlockEntity heart) {
      BlockPos pos = heart.getBlockPos();
      Optional<Creaking> spawned = SpawnUtil.trySpawnMob(PaleEntities.CREAKING, MobSpawnType.SPAWNER, level, pos, 5, 16, 8, SpawnUtil.Strategy.ON_TOP_OF_COLLIDER);
      if (spawned.isEmpty()) {
         return null;
      } else {
         Creaking creaking = spawned.get();
         level.gameEvent(creaking, GameEvent.ENTITY_PLACE, creaking.position());
         level.broadcastEntityEvent(creaking, (byte)60);
         creaking.setTransient(pos);
         return creaking;
      }
   }

   public void creakingHurt() {
      Optional<Creaking> hurtProtector = this.getCreakingProtector();
      if (hurtProtector.isPresent() && this.level instanceof ServerLevel serverLevel && this.emitter <= 0) {
         Creaking creaking = hurtProtector.get();
         this.emitParticles(serverLevel, 20, false);
         int count = this.level.getRandom().nextIntBetweenInclusive(2, 3);

         for (int i = 0; i < count; i++) {
            this.spreadResin().ifPresent(pos -> {
               this.level.playSound(null, pos, PaleSounds.BLOCK_RESIN_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
               this.level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(this.level.getBlockState(pos)));
            });
         }

         this.emitter = 100;
         this.emitterTarget = creaking.getBoundingBox().getCenter();
      }
   }

   private Optional<BlockPos> spreadResin() {
      Mutable<BlockPos> result = new MutableObject<>(null);
      BlockPos.breadthFirstTraversal(this.worldPosition, 2, 64, (pos, consumer) -> {
         for (Direction direction : shuffled(this.level.random)) {
            BlockPos neighbor = pos.relative(direction);
            if (this.level.getBlockState(neighbor).is(PaleBlocks.PALE_OAK_LOGS)) {
               consumer.accept(neighbor);
            }
         }
      }, pos -> {
         if (!this.level.getBlockState(pos).is(PaleBlocks.PALE_OAK_LOGS)) {
            return true;
         } else {
            for (Direction direction : shuffled(this.level.random)) {
               BlockPos neighbor = pos.relative(direction);
               BlockState neighborState = this.level.getBlockState(neighbor);
               Direction face = direction.getOpposite();
               if (neighborState.isAir()) {
                  neighborState = PaleBlocks.RESIN_CLUMP.defaultBlockState();
               } else if (neighborState.is(Blocks.WATER) && neighborState.getFluidState().isSource()) {
                  neighborState = PaleBlocks.RESIN_CLUMP.defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED, true);
               }

               if (neighborState.is(PaleBlocks.RESIN_CLUMP) && !MultifaceBlock.hasFace(neighborState, face)) {
                  this.level.setBlock(neighbor, neighborState.setValue(MultifaceBlock.getFaceProperty(face), true), 3);
                  result.setValue(neighbor);
                  return false;
               }
            }

            return true;
         }
      });
      return Optional.ofNullable(result.getValue());
   }

   private static java.util.List<Direction> shuffled(RandomSource random) {
      java.util.List<Direction> list = new java.util.ArrayList<>(java.util.List.of(Direction.values()));
      java.util.Collections.shuffle(list, new java.util.Random(random.nextLong()));
      return list;
   }

   private void emitParticles(ServerLevel level, int count, boolean towardHeart) {
      Optional<Creaking> emitProtector = this.getCreakingProtector();
      if (emitProtector.isPresent()) {
         Creaking creaking = emitProtector.get();
         int color = towardHeart ? 16545810 : 6250335;
         float r = (color >> 16 & 255) / 255.0F;
         float g = (color >> 8 & 255) / 255.0F;
         float b = (color & 255) / 255.0F;
         RandomSource random = level.random;

         for (int i = 0; i < count; i++) {
            AABB box = creaking.getBoundingBox();
            Vec3 a = new Vec3(box.minX, box.minY, box.minZ).add(random.nextDouble() * box.getXsize(), random.nextDouble() * box.getYsize(), random.nextDouble() * box.getZsize());
            Vec3 c = Vec3.atLowerCornerOf(this.getBlockPos()).add(random.nextDouble(), random.nextDouble(), random.nextDouble());
            Vec3 from = towardHeart ? c : a;
            Vec3 to = towardHeart ? a : c;
            Vec3 motion = to.subtract(from).scale(0.04);
            level.sendParticles(new DustParticleOptions(new Vector3f(r, g, b), 1.0F), from.x, from.y, from.z, 0, motion.x, motion.y, motion.z, 1.0);
         }
      }
   }

   public void removeProtector(@Nullable DamageSource source) {
      Optional<Creaking> removeProtector = this.getCreakingProtector();
      if (removeProtector.isPresent()) {
         Creaking creaking = removeProtector.get();
         if (source == null) {
            creaking.tearDown();
         } else {
            creaking.creakingDeathEffects(source);
            creaking.setTearingDown();
            creaking.setHealth(0.0F);
         }

         this.clearCreakingInfo();
      }
   }

   public boolean isProtector(Creaking creaking) {
      return this.getCreakingProtector().map(c -> c == creaking).orElse(false);
   }

   public int getAnalogOutputSignal() {
      return this.outputSignal;
   }

   public int computeAnalogOutputSignal() {
      if (this.creakingInfo != null && !this.getCreakingProtector().isEmpty()) {
         double distance = this.distanceToCreaking();
         double normalized = Mth.clamp(distance, 0.0, 32.0) / 32.0;
         return 15 - (int)Math.floor(normalized * 15.0);
      } else {
         return 0;
      }
   }

   public void load(CompoundTag tag) {
      super.load(tag);
      if (tag.hasUUID("creaking")) {
         this.setCreakingInfo(tag.getUUID("creaking"));
      } else {
         this.clearCreakingInfo();
      }
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      if (this.creakingInfo != null) {
         tag.putUUID("creaking", this.creakingInfo.map(Entity::getUUID, uuid -> uuid));
      }
   }
}
