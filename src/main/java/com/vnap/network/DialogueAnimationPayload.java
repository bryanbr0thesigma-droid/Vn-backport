package com.vnap.network;

import com.vnap.VillagerNewsAddonPort;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;

public record DialogueAnimationPayload(UUID entityId, String groupId, int variantIndex, int durationTicks) implements FabricPacket {
   public static final PacketType<DialogueAnimationPayload> TYPE = PacketType.create(
      VillagerNewsAddonPort.id("dialogue_animation"), DialogueAnimationPayload::read
   );

   private static DialogueAnimationPayload read(FriendlyByteBuf buffer) {
      return new DialogueAnimationPayload(buffer.readUUID(), buffer.readUtf(64), buffer.readVarInt(), buffer.readVarInt());
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeUUID(this.entityId());
      buffer.writeUtf(this.groupId(), 64);
      buffer.writeVarInt(this.variantIndex());
      buffer.writeVarInt(this.durationTicks());
   }

   @Override
   public PacketType<?> getType() {
      return TYPE;
   }
}
