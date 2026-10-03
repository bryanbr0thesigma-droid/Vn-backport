package com.backport.poplar;



import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PoplarBoatItem extends Item {
   private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);
   private final boolean hasChest;

   public PoplarBoatItem(boolean hasChest, Item.Properties properties) {
      super(properties);
      this.hasChest = hasChest;
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      HitResult hit = Item.getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
      if (hit.getType() == HitResult.Type.MISS) {
         return InteractionResultHolder.pass(stack);
      } else {
         Vec3 view = player.getViewVector(1.0F);
         List<Entity> entities = level.getEntities(player, player.getBoundingBox().expandTowards(view.scale(5.0)).inflate(1.0), ENTITY_PREDICATE);
         if (!entities.isEmpty()) {
            Vec3 eye = player.getEyePosition();

            for (Entity entity : entities) {
               AABB box = entity.getBoundingBox().inflate(entity.getPickRadius());
               if (box.contains(eye)) {
                  return InteractionResultHolder.pass(stack);
               }
            }
         }

         if (hit.getType() == HitResult.Type.BLOCK) {
            Boat boat = this.hasChest
               ? new PoplarChestBoat(level, hit.getLocation().x, hit.getLocation().y, hit.getLocation().z)
               : new PoplarBoat(level, hit.getLocation().x, hit.getLocation().y, hit.getLocation().z);
            boat.setYRot(player.getYRot());
            if (!level.noCollision(boat, boat.getBoundingBox())) {
               return InteractionResultHolder.fail(stack);
            } else {
               if (!level.isClientSide) {
                  level.addFreshEntity(boat);
                  level.gameEvent(player, GameEvent.ENTITY_PLACE, hit.getLocation());
                  if (!player.getAbilities().instabuild) {
                     stack.shrink(1);
                  }
               }

               player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
               return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }
         } else {
            return InteractionResultHolder.pass(stack);
         }
      }
   }
}
