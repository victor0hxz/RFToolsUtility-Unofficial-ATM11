package mcjty.rftoolsutility.modules.teleporter.network;

import java.util.ArrayList;
import java.util.List;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestination;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestinationClientInfo;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestinations;
import mcjty.rftoolsutility.setup.RFToolsUtilityMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public record PacketGetAllReceivers() implements CustomPacketPayload {
   public static final Identifier ID = Identifier.fromNamespaceAndPath("rftoolsutility", "getallreceivers");
   public static final Type<PacketGetAllReceivers> TYPE = new Type(ID);
   public static final PacketGetAllReceivers INSTANCE = new PacketGetAllReceivers();
   public static final StreamCodec<FriendlyByteBuf, PacketGetAllReceivers> CODEC = StreamCodec.unit(INSTANCE);

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public void handle(IPayloadContext ctx) {
      ctx.enqueueWork(() -> {
         Player player = ctx.player();
         TeleportDestinations destinations = TeleportDestinations.get(player.level());
         List<TeleportDestinationClientInfo> destinationList = new ArrayList<>(destinations.getValidDestinations(player.level(), null));
         this.addDimensions(player.level(), destinationList);
         this.addRfToolsDimensions(player.level(), destinationList);
         PacketAllReceiversReady msg = new PacketAllReceiversReady(destinationList);
         RFToolsUtilityMessages.sendToPlayer(msg, player);
      });
   }

   private void addDimensions(Level worldObj, List<TeleportDestinationClientInfo> destinationList) {
      MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

      for (ServerLevel world : server.getAllLevels()) {
         ResourceKey<Level> id = world.dimension();
         TeleportDestination destination = new TeleportDestination(new BlockPos(0, 70, 0), id);
         destination = destination.withName("Dimension: " + id.identifier().getPath());
         TeleportDestinationClientInfo teleportDestinationClientInfo = new TeleportDestinationClientInfo(destination);
         String dimName = id.identifier().getPath();
         teleportDestinationClientInfo = teleportDestinationClientInfo.withDimensionName(dimName);
         destinationList.add(teleportDestinationClientInfo);
      }
   }

   private void addRfToolsDimensions(Level world, List<TeleportDestinationClientInfo> destinationList) {
   }
}
