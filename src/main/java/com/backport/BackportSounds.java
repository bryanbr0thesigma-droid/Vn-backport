package com.backport;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public final class BackportSounds {
   public static final SoundEvent BLOCK_COPPER_BULB_BREAK = register("block.copper_bulb.break");
   public static final SoundEvent BLOCK_COPPER_BULB_FALL = register("block.copper_bulb.fall");
   public static final SoundEvent BLOCK_COPPER_BULB_HIT = register("block.copper_bulb.hit");
   public static final SoundEvent BLOCK_COPPER_BULB_PLACE = register("block.copper_bulb.place");
   public static final SoundEvent BLOCK_COPPER_BULB_STEP = register("block.copper_bulb.step");
   public static final SoundEvent BLOCK_COPPER_BULB_TURN_OFF = register("block.copper_bulb.turn_off");
   public static final SoundEvent BLOCK_COPPER_BULB_TURN_ON = register("block.copper_bulb.turn_on");
   public static final SoundEvent BLOCK_COPPER_CHEST_CLOSE = register("block.copper_chest.close");
   public static final SoundEvent BLOCK_COPPER_CHEST_OPEN = register("block.copper_chest.open");
   public static final SoundEvent BLOCK_COPPER_CHEST_OXIDIZED_CLOSE = register("block.copper_chest_oxidized.close");
   public static final SoundEvent BLOCK_COPPER_CHEST_OXIDIZED_OPEN = register("block.copper_chest_oxidized.open");
   public static final SoundEvent BLOCK_COPPER_CHEST_WEATHERED_CLOSE = register("block.copper_chest_weathered.close");
   public static final SoundEvent BLOCK_COPPER_CHEST_WEATHERED_OPEN = register("block.copper_chest_weathered.open");
   public static final SoundEvent BLOCK_COPPER_DOOR_CLOSE = register("block.copper_door.close");
   public static final SoundEvent BLOCK_COPPER_DOOR_OPEN = register("block.copper_door.open");
   public static final SoundEvent BLOCK_COPPER_GOLEM_STATUE_BREAK = register("block.copper_golem_statue.break");
   public static final SoundEvent BLOCK_COPPER_GOLEM_STATUE_FALL = register("block.copper_golem_statue.fall");
   public static final SoundEvent BLOCK_COPPER_GOLEM_STATUE_HIT = register("block.copper_golem_statue.hit");
   public static final SoundEvent BLOCK_COPPER_GOLEM_STATUE_PLACE = register("block.copper_golem_statue.place");
   public static final SoundEvent BLOCK_COPPER_GOLEM_STATUE_STEP = register("block.copper_golem_statue.step");
   public static final SoundEvent BLOCK_COPPER_TRAPDOOR_CLOSE = register("block.copper_trapdoor.close");
   public static final SoundEvent BLOCK_COPPER_TRAPDOOR_OPEN = register("block.copper_trapdoor.open");
   public static final SoundEvent ENTITY_COPPER_GOLEM_DEATH = register("entity.copper_golem.death");
   public static final SoundEvent ENTITY_COPPER_GOLEM_HURT = register("entity.copper_golem.hurt");
   public static final SoundEvent ENTITY_COPPER_GOLEM_ITEM_DROP = register("entity.copper_golem.item_drop");
   public static final SoundEvent ENTITY_COPPER_GOLEM_ITEM_NO_DROP = register("entity.copper_golem.item_no_drop");
   public static final SoundEvent ENTITY_COPPER_GOLEM_NO_ITEM_GET = register("entity.copper_golem.no_item_get");
   public static final SoundEvent ENTITY_COPPER_GOLEM_NO_ITEM_NO_GET = register("entity.copper_golem.no_item_no_get");
   public static final SoundEvent ENTITY_COPPER_GOLEM_SHEAR = register("entity.copper_golem.shear");
   public static final SoundEvent ENTITY_COPPER_GOLEM_SPAWN = register("entity.copper_golem.spawn");
   public static final SoundEvent ENTITY_COPPER_GOLEM_SPIN = register("entity.copper_golem.spin");
   public static final SoundEvent ENTITY_COPPER_GOLEM_STEP = register("entity.copper_golem.step");
   public static final SoundEvent ENTITY_COPPER_GOLEM_BECOME_STATUE = register("entity.copper_golem_become_statue");
   public static final SoundEvent ENTITY_COPPER_GOLEM_OXIDIZED_DEATH = register("entity.copper_golem_oxidized.death");
   public static final SoundEvent ENTITY_COPPER_GOLEM_OXIDIZED_HURT = register("entity.copper_golem_oxidized.hurt");
   public static final SoundEvent ENTITY_COPPER_GOLEM_OXIDIZED_SPIN = register("entity.copper_golem_oxidized.spin");
   public static final SoundEvent ENTITY_COPPER_GOLEM_OXIDIZED_STEP = register("entity.copper_golem_oxidized.step");
   public static final SoundEvent ENTITY_COPPER_GOLEM_WEATHERED_DEATH = register("entity.copper_golem_weathered.death");
   public static final SoundEvent ENTITY_COPPER_GOLEM_WEATHERED_HURT = register("entity.copper_golem_weathered.hurt");
   public static final SoundEvent ENTITY_COPPER_GOLEM_WEATHERED_SPIN = register("entity.copper_golem_weathered.spin");
   public static final SoundEvent ENTITY_COPPER_GOLEM_WEATHERED_STEP = register("entity.copper_golem_weathered.step");

   private BackportSounds() {
   }

   private static SoundEvent register(String name) {
      return Registry.register(BuiltInRegistries.SOUND_EVENT, Backport.id(name), SoundEvent.createVariableRangeEvent(Backport.id(name)));
   }

   public static void init() {
   }
}
