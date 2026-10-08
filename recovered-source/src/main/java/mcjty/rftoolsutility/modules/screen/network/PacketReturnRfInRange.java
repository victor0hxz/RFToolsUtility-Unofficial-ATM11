package mcjty.rftoolsutility.modules.screen.network;

import java.util.HashMap;
import java.util.Map;
import mcjty.rftoolsutility.modules.screen.MachineInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketReturnRfInRange(Map<BlockPos, MachineInfo> levels) implements CustomPacketPayload {
   public static final Identifier ID = Identifier.fromNamespaceAndPath("rftoolsutility", "returnrfinrange");
   public static final Type<PacketReturnRfInRange> TYPE = new Type(ID);
   public static Map<BlockPos, MachineInfo> clientLevels;
   public static final StreamCodec<FriendlyByteBuf, PacketReturnRfInRange> CODEC = StreamCodec.composite(
      ByteBufCodecs.map(HashMap::new, BlockPos.STREAM_CODEC, MachineInfo.STREAM_CODEC), PacketReturnRfInRange::levels, PacketReturnRfInRange::new
   );

   public static PacketReturnRfInRange create(Map<BlockPos, MachineInfo> result) {
      return new PacketReturnRfInRange(result);
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public Map<BlockPos, MachineInfo> getLevels() {
      return this.levels;
   }

   public void handle(IPayloadContext ctx) {
      ctx.enqueueWork(() -> clientLevels = this.levels);
   }
}
