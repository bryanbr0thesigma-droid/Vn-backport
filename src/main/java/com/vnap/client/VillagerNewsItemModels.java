package com.vnap.client;

import com.vnap.VillagerNewsAddonPort;
import com.vnap.item.VillagerNewsItems;
import java.util.Map;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * The 26.x port selected item models by display context through item definitions. 1.20.1 has no
 * such selector, so the held and worn models are registered as extra models and swapped in by
 * {@link com.vnap.mixin.client.ItemRendererMixin}.
 */
public final class VillagerNewsItemModels {
   private static final Map<Item, ResourceLocation> HELD = Map.of(
      VillagerNewsItems.HANDBOOK, VillagerNewsAddonPort.id("item/handbook_held"),
      VillagerNewsItems.MICROPHONE, VillagerNewsAddonPort.id("item/microphone_held")
   );
   private static final Map<Item, ResourceLocation> WORN = Map.of(
      VillagerNewsItems.MAYOR_HAT, VillagerNewsAddonPort.id("item/mayor_hat_worn"),
      VillagerNewsItems.MOUSTACHE, VillagerNewsAddonPort.id("item/moustache_worn"),
      VillagerNewsItems.TESTIFICATE_MAN_HELMET, VillagerNewsAddonPort.id("item/testificate_man_helmet_worn"),
      VillagerNewsItems.VILLAGER_NOSE, VillagerNewsAddonPort.id("item/villager_nose_worn")
   );

   private VillagerNewsItemModels() {
   }

   public static void register() {
      ModelLoadingPlugin.register(context -> {
         context.addModels(HELD.values());
         context.addModels(WORN.values());
      });
   }

   /** Returns the replacement model for this stack and display context, or null to keep the flat sprite. */
   public static BakedModel variant(ItemStack stack, ItemDisplayContext context) {
      ResourceLocation id = null;
      switch (context) {
         case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> id = HELD.get(stack.getItem());
         case HEAD -> id = WORN.get(stack.getItem());
         default -> {
         }
      }

      return id == null ? null : Minecraft.getInstance().getModelManager().getModel(id);
   }
}
