package com.palegarden;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;

public final class PaleSounds {
   public static final SoundEvent BLOCK_CREAKING_HEART_BREAK = register("block.creaking_heart.break");
   public static final SoundEvent BLOCK_CREAKING_HEART_FALL = register("block.creaking_heart.fall");
   public static final SoundEvent BLOCK_CREAKING_HEART_HIT = register("block.creaking_heart.hit");
   public static final SoundEvent BLOCK_CREAKING_HEART_HURT = register("block.creaking_heart.hurt");
   public static final SoundEvent BLOCK_CREAKING_HEART_IDLE = register("block.creaking_heart.idle");
   public static final SoundEvent BLOCK_CREAKING_HEART_PLACE = register("block.creaking_heart.place");
   public static final SoundEvent BLOCK_CREAKING_HEART_SPAWN = register("block.creaking_heart.spawn");
   public static final SoundEvent BLOCK_CREAKING_HEART_STEP = register("block.creaking_heart.step");
   public static final SoundEvent BLOCK_EYEBLOSSOM_CLOSE = register("block.eyeblossom.close");
   public static final SoundEvent BLOCK_EYEBLOSSOM_CLOSE_LONG = register("block.eyeblossom.close_long");
   public static final SoundEvent BLOCK_EYEBLOSSOM_IDLE = register("block.eyeblossom.idle");
   public static final SoundEvent BLOCK_EYEBLOSSOM_OPEN = register("block.eyeblossom.open");
   public static final SoundEvent BLOCK_EYEBLOSSOM_OPEN_LONG = register("block.eyeblossom.open_long");
   public static final SoundEvent BLOCK_PALE_HANGING_MOSS_IDLE = register("block.pale_hanging_moss.idle");
   public static final SoundEvent BLOCK_RESIN_BREAK = register("block.resin.break");
   public static final SoundEvent BLOCK_RESIN_FALL = register("block.resin.fall");
   public static final SoundEvent BLOCK_RESIN_PLACE = register("block.resin.place");
   public static final SoundEvent BLOCK_RESIN_STEP = register("block.resin.step");
   public static final SoundEvent BLOCK_RESIN_BRICKS_BREAK = register("block.resin_bricks.break");
   public static final SoundEvent BLOCK_RESIN_BRICKS_FALL = register("block.resin_bricks.fall");
   public static final SoundEvent BLOCK_RESIN_BRICKS_HIT = register("block.resin_bricks.hit");
   public static final SoundEvent BLOCK_RESIN_BRICKS_PLACE = register("block.resin_bricks.place");
   public static final SoundEvent BLOCK_RESIN_BRICKS_STEP = register("block.resin_bricks.step");
   public static final SoundEvent EMPTY = register("empty");
   public static final SoundEvent ENTITY_CREAKING_ACTIVATE = register("entity.creaking.activate");
   public static final SoundEvent ENTITY_CREAKING_AMBIENT = register("entity.creaking.ambient");
   public static final SoundEvent ENTITY_CREAKING_ATTACK = register("entity.creaking.attack");
   public static final SoundEvent ENTITY_CREAKING_DEACTIVATE = register("entity.creaking.deactivate");
   public static final SoundEvent ENTITY_CREAKING_DEATH = register("entity.creaking.death");
   public static final SoundEvent ENTITY_CREAKING_FREEZE = register("entity.creaking.freeze");
   public static final SoundEvent ENTITY_CREAKING_SPAWN = register("entity.creaking.spawn");
   public static final SoundEvent ENTITY_CREAKING_STEP = register("entity.creaking.step");
   public static final SoundEvent ENTITY_CREAKING_SWAY = register("entity.creaking.sway");
   public static final SoundEvent ENTITY_CREAKING_TWITCH = register("entity.creaking.twitch");
   public static final SoundEvent ENTITY_CREAKING_UNFREEZE = register("entity.creaking.unfreeze");

   public static final SoundType CREAKING_HEART = new SoundType(1.0F, 1.0F, BLOCK_CREAKING_HEART_BREAK, BLOCK_CREAKING_HEART_STEP, BLOCK_CREAKING_HEART_PLACE, BLOCK_CREAKING_HEART_HIT, BLOCK_CREAKING_HEART_FALL);
   public static final SoundType RESIN = new SoundType(1.0F, 1.0F, BLOCK_RESIN_BREAK, BLOCK_RESIN_STEP, BLOCK_RESIN_PLACE, EMPTY, BLOCK_RESIN_FALL);
   public static final SoundType RESIN_BRICKS = new SoundType(1.0F, 1.0F, BLOCK_RESIN_BRICKS_BREAK, BLOCK_RESIN_BRICKS_STEP, BLOCK_RESIN_BRICKS_PLACE, BLOCK_RESIN_BRICKS_HIT, BLOCK_RESIN_BRICKS_FALL);

   private PaleSounds() {
   }

   private static SoundEvent register(String name) {
      return Registry.register(BuiltInRegistries.SOUND_EVENT, PaleGarden.id(name), SoundEvent.createVariableRangeEvent(PaleGarden.id(name)));
   }

   public static void init() {
   }
}
