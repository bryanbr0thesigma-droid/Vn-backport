package com.backport;

import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.phys.Vec3;

/** Admin helper: spawns a zombie horse jockey exactly as natural spawning does. */
public final class JockeyCommand {
   private JockeyCommand() {
   }

   public static void init() {
      CommandRegistrationCallback.EVENT.register((dispatcher, ctx, selection) -> dispatcher.register(Commands.literal("zombiehorsejockey")
         .requires(s -> s.hasPermission(2))
         .executes(c -> {
            ServerLevel level = c.getSource().getLevel();
            Vec3 pos = c.getSource().getPosition();
            ZombieHorse horse = EntityType.ZOMBIE_HORSE.create(level);
            if (horse == null) {
               return 0;
            }
            horse.moveTo(pos.x, pos.y, pos.z, c.getSource().getRotation().y, 0.0F);
            horse.finalizeSpawn(level, level.getCurrentDifficultyAt(horse.blockPosition()), MobSpawnType.NATURAL, null, null);
            level.addFreshEntity(horse);
            c.getSource().sendSuccess(() -> Component.literal("Spawned a zombie horse jockey"), true);
            return Command.SINGLE_SUCCESS;
         })));
   }
}
