package mcjty.rftoolsutility.setup;

import javax.annotation.Nonnull;
import mcjty.lib.network.Networking;
import mcjty.lib.network.PacketSendClientCommand;
import mcjty.lib.network.PacketSendServerCommand;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.typed.TypedMap.Builder;
import mcjty.rftoolsutility.modules.logic.network.PacketRemoveChannel;
import mcjty.rftoolsutility.modules.logic.network.PacketSendRedstoneData;
import mcjty.rftoolsutility.modules.logic.network.PacketSetChannelName;
import mcjty.rftoolsutility.modules.logic.network.PacketSetRedstone;
import mcjty.rftoolsutility.modules.screen.network.PacketGetScreenData;
import mcjty.rftoolsutility.modules.screen.network.PacketModuleUpdate;
import mcjty.rftoolsutility.modules.screen.network.PacketReturnRfInRange;
import mcjty.rftoolsutility.modules.screen.network.PacketReturnScreenData;
import mcjty.rftoolsutility.modules.teleporter.network.PacketAllReceiversReady;
import mcjty.rftoolsutility.modules.teleporter.network.PacketGetAllReceivers;
import mcjty.rftoolsutility.modules.teleporter.network.PacketTargetsReady;
import mcjty.rftoolsutility.playerprops.PacketSendBuffsToClient;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RFToolsUtilityMessages {
   public static void registerMessages(RegisterPayloadHandlersEvent event) {
      PayloadRegistrar registrar = event.registrar("rftoolsutility").versioned("1.0").optional();
      registrar.playToServer(PacketGetAllReceivers.TYPE, PacketGetAllReceivers.CODEC, PacketGetAllReceivers::handle);
      registrar.playToServer(PacketGetScreenData.TYPE, PacketGetScreenData.CODEC, PacketGetScreenData::handle);
      registrar.playToServer(PacketModuleUpdate.TYPE, PacketModuleUpdate.CODEC, PacketModuleUpdate::handle);
      registrar.playToServer(PacketRemoveChannel.TYPE, PacketRemoveChannel.CODEC, PacketRemoveChannel::handle);
      registrar.playToServer(PacketSetRedstone.TYPE, PacketSetRedstone.CODEC, PacketSetRedstone::handle);
      registrar.playToServer(PacketSetChannelName.TYPE, PacketSetChannelName.CODEC, PacketSetChannelName::handle);
      registrar.playToClient(PacketAllReceiversReady.TYPE, PacketAllReceiversReady.CODEC);
      registrar.playToClient(PacketTargetsReady.TYPE, PacketTargetsReady.CODEC);
      registrar.playToClient(PacketSendBuffsToClient.TYPE, PacketSendBuffsToClient.CODEC);
      registrar.playToClient(PacketReturnScreenData.TYPE, PacketReturnScreenData.CODEC);
      registrar.playToClient(PacketReturnRfInRange.TYPE, PacketReturnRfInRange.CODEC);
      registrar.playToClient(PacketSendRedstoneData.TYPE, PacketSendRedstoneData.CODEC);
   }

   public static void registerClientMessages(RegisterClientPayloadHandlersEvent event) {
      event.register(PacketAllReceiversReady.TYPE, PacketAllReceiversReady::handle);
      event.register(PacketTargetsReady.TYPE, PacketTargetsReady::handle);
      event.register(PacketSendBuffsToClient.TYPE, PacketSendBuffsToClient::handle);
      event.register(PacketReturnScreenData.TYPE, PacketReturnScreenData::handle);
      event.register(PacketReturnRfInRange.TYPE, PacketReturnRfInRange::handle);
      event.register(PacketSendRedstoneData.TYPE, PacketSendRedstoneData::handle);
   }

   public static void sendToServer(String command, @Nonnull Builder argumentBuilder) {
      Networking.sendToServer(new PacketSendServerCommand("rftoolsutility", command, argumentBuilder.build()));
   }

   public static void sendToServer(String command) {
      Networking.sendToServer(new PacketSendServerCommand("rftoolsutility", command, TypedMap.EMPTY));
   }

   public static void sendToClient(Player player, String command, @Nonnull Builder argumentBuilder) {
      Networking.sendToPlayer(new PacketSendClientCommand("rftoolsutility", command, argumentBuilder.build()), player);
   }

   public static <T extends CustomPacketPayload> void sendToPlayer(T packet, Player player) {
      PacketDistributor.sendToPlayer((ServerPlayer)player, packet, new CustomPacketPayload[0]);
   }

   public static <T extends CustomPacketPayload> void sendToServer(T packet) {
      ClientPacketDistributor.sendToServer(packet, new CustomPacketPayload[0]);
   }
}
