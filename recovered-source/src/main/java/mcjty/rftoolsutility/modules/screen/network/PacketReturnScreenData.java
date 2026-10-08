package mcjty.rftoolsutility.modules.screen.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.api.screens.data.IModuleData;
import mcjty.rftoolsbase.api.screens.data.IModuleDataFactory;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.screen.blocks.ScreenTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketReturnScreenData(GlobalPos pos, Map<Integer, IModuleData> screenData) implements CustomPacketPayload {
   public static final Identifier ID = Identifier.fromNamespaceAndPath("rftoolsutility", "returnscreendata");
   public static final Type<PacketReturnScreenData> TYPE = new Type(ID);
   public static final StreamCodec<RegistryFriendlyByteBuf, PacketReturnScreenData> CODEC = StreamCodec.of((buf, packet) -> {
      buf.writeBlockPos(packet.pos().pos());
      Identifier.STREAM_CODEC.encode(buf, packet.pos().dimension().identifier());
      buf.writeInt(packet.screenData().size());

      for (Entry<Integer, IModuleData> me : packet.screenData().entrySet()) {
         buf.writeInt(me.getKey());
         IModuleData c = me.getValue();
         buf.writeInt(RFToolsUtility.screenModuleRegistry.getShortId(c.getId()));
         c.writeToBuf(buf);
      }
   }, buf -> {
      BlockPos pos = buf.readBlockPos();
      ResourceKey<Level> dim = LevelTools.getId((Identifier)Identifier.STREAM_CODEC.decode(buf));
      int size = buf.readInt();
      Map<Integer, IModuleData> screenData = new HashMap<>(size);

      for (int i = 0; i < size; i++) {
         int key = buf.readInt();
         int shortId = buf.readInt();
         String id = RFToolsUtility.screenModuleRegistry.getNormalId(shortId);
         IModuleDataFactory<?> dataFactory = RFToolsUtility.screenModuleRegistry.getModuleDataFactory(id);
         IModuleData data = dataFactory.createData(buf);
         screenData.put(key, data);
      }

      return new PacketReturnScreenData(GlobalPos.of(dim, pos), screenData);
   });

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public GlobalPos getPos() {
      return this.pos;
   }

   public Map<Integer, IModuleData> getScreenData() {
      return this.screenData;
   }

   public void handle(IPayloadContext ctx) {
      ctx.enqueueWork(() -> ScreenTileEntity.screenData.put(this.getPos(), this.getScreenData()));
   }
}
