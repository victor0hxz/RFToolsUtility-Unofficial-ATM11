package mcjty.rftoolsutility.playerprops;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSendBuffsToClient(List<PlayerBuff> buffs) implements CustomPacketPayload {
   public static final Identifier ID = Identifier.fromNamespaceAndPath("rftoolsutility", "sendbuffstoclient");
   public static final Type<PacketSendBuffsToClient> TYPE = new Type(ID);
   public static final StreamCodec<RegistryFriendlyByteBuf, PacketSendBuffsToClient> CODEC = StreamCodec.of((buf, packet) -> {
      buf.writeByte(packet.buffs().size());

      for (PlayerBuff buff : packet.buffs()) {
         buf.writeByte(buff.ordinal());
      }
   }, buf -> {
      int size = buf.readByte();
      List<PlayerBuff> buffs = new ArrayList<>(size);

      for (int i = 0; i < size; i++) {
         buffs.add(PlayerBuff.values()[buf.readByte()]);
      }

      return new PacketSendBuffsToClient(buffs);
   });

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static PacketSendBuffsToClient create(Map<PlayerBuff, Integer> buffs) {
      return new PacketSendBuffsToClient(new ArrayList<>(buffs.keySet()));
   }

   public List<PlayerBuff> getBuffs() {
      return this.buffs;
   }

   public void handle(IPayloadContext ctx) {
      ctx.enqueueWork(() -> SendBuffsToClientHelper.setBuffs(this));
   }
}
