package com.backport.loot;

import com.backport.Backport;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

/** Where the 26.x discs come from: Lava Chicken (zombie jockey), Tears (reflected ghast fireball), Bounce (sulfur-cave mineshaft chests). */
public final class DiscDrops {
   private DiscDrops() {
   }

   public static void init() {
      LootTableEvents.MODIFY.register((rm, lootManager, id, builder, source) -> {
         if (!source.isBuiltin()) {
            return;
         }
         if (id.equals(EntityType.ZOMBIE.getDefaultLootTable())) {
            builder.pool(LootPool.lootPool().setRolls(net.minecraft.world.level.storage.loot.providers.number.ConstantValue.exactly(1))
               .when(LootItemKilledByPlayerCondition.killedByPlayer())
               .when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity()
                  .flags(EntityFlagsPredicate.Builder.flags().setIsBaby(true).build()).vehicle(EntityPredicate.Builder.entity().of(EntityType.CHICKEN).build())))
               .add(LootItem.lootTableItem(BuiltInRegistries.ITEM.get(Backport.id("music_disc_lava_chicken")))).build());
         } else if (id.equals(EntityType.GHAST.getDefaultLootTable())) {
            builder.pool(LootPool.lootPool().setRolls(net.minecraft.world.level.storage.loot.providers.number.ConstantValue.exactly(1))
               .when(LootItemKilledByPlayerCondition.killedByPlayer())
               .when(DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType()
                  .direct(EntityPredicate.Builder.entity().of(EntityType.FIREBALL)).tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE))))
               .add(LootItem.lootTableItem(BuiltInRegistries.ITEM.get(Backport.id("music_disc_tears")))).build());
         } else if (id.equals(new ResourceLocation("minecraft", "chests/abandoned_mineshaft"))) {
            ResourceKey<Biome> sulfur = ResourceKey.create(Registries.BIOME, Backport.id("sulfur_caves"));
            builder.pool(LootPool.lootPool().setRolls(net.minecraft.world.level.storage.loot.providers.number.ConstantValue.exactly(1))
               .when(LocationCheck.checkLocation(LocationPredicate.Builder.location().setBiome(sulfur)))
               .when(LootItemRandomChanceCondition.randomChance(0.1F))
               .add(LootItem.lootTableItem(BuiltInRegistries.ITEM.get(Backport.id("music_disc_bounce")))).build());
         }
      });
   }
}
