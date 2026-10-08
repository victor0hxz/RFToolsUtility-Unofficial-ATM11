package mcjty.rftoolsutility.modules.teleporter.network;

import java.util.List;
import mcjty.rftoolsutility.modules.teleporter.client.GuiTeleportProbe;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestinationClientInfo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketAllReceiversReady(List<TeleportDestinationClientInfo> destinationList) implements CustomPacketPayload {
   public static final Identifier ID = Identifier.fromNamespaceAndPath("rftoolsutility", "allreceiversready");
   public static final Type<PacketAllReceiversReady> TYPE = new Type(ID);
   public static final StreamCodec<RegistryFriendlyByteBuf, PacketAllReceiversReady> CODEC = StreamCodec.composite(
      TeleportDestinationClientInfo.STREAM_CODEC.apply(ByteBufCodecs.list()), d -> d.destinationList, PacketAllReceiversReady::new
   );

   public PacketAllReceiversReady(List<TeleportDestinationClientInfo> destinationList) {
      this.destinationList.addAll(destinationList);
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public void handle(IPayloadContext ctx) {
      ctx.enqueueWork(() -> GuiTeleportProbe.setReceivers(this.destinationList));
   }
}
