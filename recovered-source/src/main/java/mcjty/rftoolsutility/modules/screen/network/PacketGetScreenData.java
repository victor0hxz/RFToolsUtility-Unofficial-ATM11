package mcjty.rftoolsutility.modules.screen.network;

import java.util.Map;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.api.screens.data.IModuleData;
import mcjty.rftoolsutility.modules.screen.blocks.ScreenTileEntity;
import mcjty.rftoolsutility.setup.RFToolsUtilityMessages;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketGetScreenData(String modid, GlobalPos pos, Long millis) implements CustomPacketPayload {
   public static final Identifier ID = Identifier.fromNamespaceAndPath("rftoolsutility", "getscreendata");
   public static final Type<PacketGetScreenData> TYPE = new Type(ID);
   public static final StreamCodec<FriendlyByteBuf, PacketGetScreenData> CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      PacketGetScreenData::modid,
      GlobalPos.STREAM_CODEC,
      b -> b.pos(),
      ByteBufCodecs.VAR_LONG,
      PacketGetScreenData::millis,
      PacketGetScreenData::new
   );

   public static PacketGetScreenData create(String modid, GlobalPos pos, long millis) {
      return new PacketGetScreenData(modid, pos, millis);
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public void handle(IPayloadContext ctx) {
      ctx.enqueueWork(() -> {
         Player player = ctx.player();
         Level world = player.level();
         Level var7 = LevelTools.getLevel(world, this.pos.dimension());
         if (var7.hasChunkAt(this.pos.pos()) && var7.getBlockEntity(this.pos.pos()) instanceof ScreenTileEntity screen) {
            Map<Integer, IModuleData> screenData = screen.getScreenData(this.millis);
            PacketReturnScreenData msg = new PacketReturnScreenData(this.pos, screenData);
            RFToolsUtilityMessages.sendToPlayer(msg, player);
         }
      });
   }
}
