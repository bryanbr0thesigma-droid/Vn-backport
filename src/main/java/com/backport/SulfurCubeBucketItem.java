package com.backport;

import com.backport.entity.SulfurCube;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.gameevent.GameEvent;

public class SulfurCubeBucketItem extends Item {
   public SulfurCubeBucketItem(Properties props) {
      super(props);
   }

   @Override
   public InteractionResult useOn(UseOnContext ctx) {
      if (!(ctx.getLevel() instanceof ServerLevel level)) {
         return InteractionResult.SUCCESS;
      }
      BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace());
      SulfurCube cube = BackportEntities.SULFUR_CUBE.create(level);
      if (cube == null) {
         return InteractionResult.FAIL;
      }
      ItemStack stack = ctx.getItemInHand();
      cube.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, ctx.getRotation(), 0.0F);
      cube.setFromBucket(true);
      if (stack.hasTag()) {
         cube.setAge(stack.getTag().getInt("Age"));
         if (stack.getTag().getBoolean("Baby")) {
            cube.setBaby(true);
         }
      }
      if (stack.hasCustomHoverName()) {
         cube.setCustomName(stack.getHoverName());
      }
      cube.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.BUCKET, null, null);
      level.addFreshEntity(cube);
      level.playSound(null, pos, BackportSounds.ITEM_BUCKET_EMPTY_SULFUR_CUBE, net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
      level.gameEvent(ctx.getPlayer(), GameEvent.ENTITY_PLACE, pos);
      if (ctx.getPlayer() != null && !ctx.getPlayer().getAbilities().instabuild) {
         ctx.getPlayer().setItemInHand(ctx.getHand(), new ItemStack(Items.BUCKET));
      }
      return InteractionResult.CONSUME;
   }
}
