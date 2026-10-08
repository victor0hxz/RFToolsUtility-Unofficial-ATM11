package mcjty.rftoolsutility.modules.teleporter.commands;

import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.ComponentFactory;
import mcjty.rftoolsbase.commands.AbstractRfToolsCommand;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestinationClientInfo;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestinations;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class CmdListReceivers extends AbstractRfToolsCommand {
   public String getHelp() {
      return "";
   }

   public String getCommand() {
      return "list";
   }

   public int getPermissionLevel() {
      return 0;
   }

   public boolean isClientSide() {
      return false;
   }

   public void execute(Player sender, String[] args) {
      TeleportDestinations destinations = TeleportDestinations.get(sender.level());

      for (TeleportDestinationClientInfo clientInfo : destinations.getValidDestinations(sender.level(), null)) {
         ResourceKey<Level> type = clientInfo.destination().getDimension();
         Component component = ComponentFactory.literal(
            "    Receiver: dimension=" + type.identifier().getPath() + ", location=" + BlockPosTools.toString(clientInfo.destination().getCoordinate())
         );
         sender.sendSystemMessage(component);
      }
   }
}
