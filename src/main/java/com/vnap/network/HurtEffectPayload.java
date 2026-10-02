package com.vnap.network;

import com.vnap.VillagerNewsAddonPort;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;

public record HurtEffectPayload(UUID entityId, String effectId) implements FabricPacket {
   public static final PacketType<HurtEffectPayload> TYPE = PacketType.create(VillagerNewsAddonPort.id("hurt_effect"), HurtEffectPayload::read);

   private static HurtEffectPayload read(FriendlyByteBuf buffer) {
      return new HurtEffectPayload(buffer.readUUID(), buffer.readUtf(2));
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeUUID(this.entityId());
      buffer.writeUtf(this.effectId(), 2);
   }

   @Override
   public PacketType<?> getType() {
      return TYPE;
   }
}
