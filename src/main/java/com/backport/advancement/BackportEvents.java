package com.backport.advancement;

import com.backport.Backport;
import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

/** One scripted trigger for the 26.x advancements whose vanilla trigger does not exist in 1.20.1. */
public final class BackportEvents extends SimpleCriterionTrigger<BackportEvents.Instance> {
   public static final BackportEvents TRIGGER = new BackportEvents();
   private static final ResourceLocation ID = Backport.id("event");

   private BackportEvents() {
   }

   public static void init() {
      CriteriaTriggers.register(TRIGGER);
   }

   /** Fires every advancement criterion whose event name matches and whose threshold the value reaches. */
   public static void fire(ServerPlayer player, String event, double value) {
      TRIGGER.trigger(player, i -> i.event.equals(event) && value >= i.min);
   }

   public static void fire(ServerPlayer player, String event) {
      fire(player, event, 0.0);
   }

   @Override
   public ResourceLocation getId() {
      return ID;
   }

   @Override
   protected Instance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext context) {
      return new Instance(predicate, GsonHelper.getAsString(json, "event"), GsonHelper.getAsDouble(json, "min", 0.0));
   }

   public static final class Instance extends AbstractCriterionTriggerInstance {
      final String event;
      final double min;

      Instance(ContextAwarePredicate predicate, String event, double min) {
         super(ID, predicate);
         this.event = event;
         this.min = min;
      }

      @Override
      public JsonObject serializeToJson(SerializationContext context) {
         JsonObject json = super.serializeToJson(context);
         json.addProperty("event", this.event);
         json.addProperty("min", this.min);
         return json;
      }
   }
}
