package com.vnap.network;

import com.vnap.VillagerNewsAddonPort;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;

public record VillagerNewsSettingsPayload(int chattiness, int rareVoicelines, boolean spawnSpecialVillagers, boolean canEdit) implements FabricPacket {
   public static final PacketType<VillagerNewsSettingsPayload> TYPE = PacketType.create(
      VillagerNewsAddonPort.id("settings"), VillagerNewsSettingsPayload::read
   );

   private static VillagerNewsSettingsPayload read(FriendlyByteBuf buffer) {
      return new VillagerNewsSettingsPayload(buffer.readVarInt(), buffer.readVarInt(), buffer.readBoolean(), buffer.readBoolean());
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeVarInt(this.chattiness());
      buffer.writeVarInt(this.rareVoicelines());
      buffer.writeBoolean(this.spawnSpecialVillagers());
      buffer.writeBoolean(this.canEdit());
   }

   @Override
   public PacketType<?> getType() {
      return TYPE;
   }
}
