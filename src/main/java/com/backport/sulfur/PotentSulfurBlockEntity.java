package com.backport.sulfur;

import com.backport.Backport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

/** Port of 26.x PotentSulfurBlockEntity (noxious gas + geyser launch), with vanilla particles. */
public class PotentSulfurBlockEntity extends BlockEntity {
   public static BlockEntityType<PotentSulfurBlockEntity> TYPE;
   public int waitingCountdown = -1;
   public long eruptionTick = -1L;

   public static void register(net.minecraft.world.level.block.Block block) {
      TYPE = net.minecraft.core.Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Backport.id("potent_sulfur"),
         FabricBlockEntityTypeBuilder.create(PotentSulfurBlockEntity::new, block).build());
   }

   public PotentSulfurBlockEntity(BlockPos pos, BlockState state) {
      super(TYPE, pos, state);
   }

   @Override
   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      tag.putInt("countdown", this.waitingCountdown);
   }

   @Override
   public void load(CompoundTag tag) {
      super.load(tag);
      if (tag.contains("countdown")) {
         this.waitingCountdown = tag.getInt("countdown");
      }
   }

   @Override
   public void setLevel(Level level) {
      super.setLevel(level);
      if (this.eruptionTick == -1L) {
         this.eruptionTick = level.getGameTime();
      }
   }

   public static void tick(Level level, BlockPos pos, BlockState state, PotentSulfurBlockEntity be) {
      PotentSulfurBlock.State s = state.getValue(PotentSulfurBlock.STATE);
      if (s == PotentSulfurBlock.State.DRY) {
         return;
      }
      boolean client = level.isClientSide;
      BlockPos source = findSource(level, pos);
      if (source == null) {
         return;
      }
      long t = level.getGameTime();
      if (s == PotentSulfurBlock.State.WET || s == PotentSulfurBlock.State.DORMANT) {
         if (client && t % 20L == 0L) {
            level.addParticle(ParticleTypes.CLOUD, source.getX() + 0.5, source.getY() + 0.5, source.getZ() + 0.5, 0, 0.02, 0);
         }
         if (!client && t % 10L == 0L) {
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(source).inflate(2.5, 0.0, 2.5), EntitySelector.NO_SPECTATORS.and(EntitySelector.ENTITY_STILL_ALIVE))) {
               if (reachable(level, source, e)) {
                  e.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 0, true, true));
               }
            }
         }
      }
      if ((s == PotentSulfurBlock.State.DORMANT || s == PotentSulfurBlock.State.ERUPTING) && !client && t % 20L == 0L) {
         if (be.waitingCountdown <= 0) {
            int water = source.getY() - pos.getY() - 1;
            if (s == PotentSulfurBlock.State.DORMANT) {
               be.waitingCountdown = 10 * (water - 1) + 15 + level.random.nextInt(16);
            } else {
               be.waitingCountdown = water - 1 + 1 + level.random.nextInt(2);
            }
         }
         if (be.waitingCountdown > 0) {
            be.waitingCountdown--;
         }
         if (be.waitingCountdown == 0) {
            PotentSulfurBlock.State next = s == PotentSulfurBlock.State.DORMANT ? PotentSulfurBlock.State.ERUPTING : PotentSulfurBlock.State.DORMANT;
            level.setBlockAndUpdate(pos, state.setValue(PotentSulfurBlock.STATE, next));
            if (next == PotentSulfurBlock.State.DORMANT) {
               level.gameEvent(GameEvent.BLOCK_DEACTIVATE, pos, GameEvent.Context.of(state));
            }
         }
      }
      if (s == PotentSulfurBlock.State.ERUPTING || s == PotentSulfurBlock.State.CONTINUOUS) {
         if (client) {
            for (int i = 0; i < 2; i++) {
               level.addParticle(ParticleTypes.BUBBLE_COLUMN_UP, source.getX() + 0.2 + level.random.nextDouble() * 0.6, source.getY() + 0.1, source.getZ() + 0.2 + level.random.nextDouble() * 0.6, 0, 0.4, 0);
            }
            if (t % 20L == 0L) {
               level.addParticle(ParticleTypes.CLOUD, source.getX() + 0.5, source.getY() + 1.0, source.getZ() + 0.5, 0, 0.3, 0);
            }
         }
         int water = source.getY() - pos.getY() - 1;
         int height = unobstructed(level, pos.above(), water);
         AABB box = new AABB(pos.above()).expandTowards(0.0, height - 1, 0.0);
         for (Entity e : level.getEntitiesOfClass(Entity.class, box, EntitySelector.NO_SPECTATORS.and(EntitySelector.ENTITY_STILL_ALIVE))) {
            Vec3 v = e.getDeltaMovement();
            e.checkSlowFallDistance();
            if (!(e instanceof Player p && p.getAbilities().flying) && !e.isPassenger() && e.getType() != EntityType.ENDER_DRAGON && v.y < 0.3F + water * 0.1) {
               e.setDeltaMovement(v.add(0.0, 0.2F, 0.0));
               e.hurtMarked = true;
            }
         }
      }
   }

   private static boolean passable(BlockState s, Level level, BlockPos pos, CollisionContext ctx) {
      return s.isAir() || s.is(Blocks.WATER) || s.getCollisionShape(level, pos, ctx).isEmpty();
   }

   private static int unobstructed(Level level, BlockPos pos, int water) {
      int h = 6 * water;
      CollisionContext ctx = CollisionContext.empty();
      for (int i = 0; i < h; i++) {
         BlockPos p = pos.above(i);
         if (!passable(level.getBlockState(p), level, p, ctx)) {
            return i;
         }
      }
      return h;
   }

   private static BlockPos findSource(Level level, BlockPos origin) {
      int maxY = origin.getY() + 5;
      CollisionContext ctx = CollisionContext.empty();
      BlockPos.MutableBlockPos pos = origin.above().mutable();
      while (pos.getY() <= maxY) {
         BlockState s = level.getBlockState(pos);
         boolean water = level.getFluidState(pos).getType() == Fluids.WATER;
         if (!water || !s.is(Blocks.WATER) && !passable(s, level, pos, ctx)) {
            if (s.isAir() || passable(s, level, pos, ctx)) {
               return pos.immutable();
            }
            break;
         }
         pos.move(Direction.UP);
      }
      return null;
   }

   private static boolean reachable(Level level, BlockPos source, Entity ent) {
      Vec3 pos = ent.getEyePosition();
      BlockPos bp = BlockPos.containing(pos);
      if (!passable(level.getBlockState(bp), level, bp, CollisionContext.empty())) {
         return false;
      }
      if (pos.distanceToSqr(Vec3.atCenterOf(source)) > 9.0) {
         return false;
      }
      Vec3 below = Vec3.atCenterOf(source.below());
      Vec3 belowPos = new Vec3(pos.x, pos.y - 1.0, pos.z);
      if (level.getFluidState(BlockPos.containing(belowPos)).getType() != Fluids.WATER) {
         return false;
      }
      HitResult hit = level.clip(new ClipContext(below, belowPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, ent));
      return hit.getType() != HitResult.Type.BLOCK;
   }
}
