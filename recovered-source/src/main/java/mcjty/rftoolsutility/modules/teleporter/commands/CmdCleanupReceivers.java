package mcjty.rftoolsutility.modules.teleporter.commands;

import mcjty.rftoolsbase.commands.AbstractRfToolsCommand;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestinations;
import net.minecraft.world.entity.player.Player;

public class CmdCleanupReceivers extends AbstractRfToolsCommand {
   public String getHelp() {
      return "";
   }

   public String getCommand() {
      return "cleanup";
   }

   public int getPermissionLevel() {
      return 1;
   }

   public boolean isClientSide() {
      return false;
   }

   public void execute(Player sender, String[] args) {
      TeleportDestinations destinations = TeleportDestinations.get(sender.level());
      destinations.cleanupInvalid();
   }
}
