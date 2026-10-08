package mcjty.rftoolsutility.modules.teleporter;

import mcjty.lib.McJtyLib;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.rftoolsutility.modules.teleporter.blocks.MatterTransmitterBlock;

public class ClientCommandHandler {
   public static final String CMD_RETURN_DESTINATION_INFO = "returnDestinationInfo";
   public static final Key<Integer> PARAM_ID = new Key("id", Type.INTEGER);
   public static final Key<String> PARAM_NAME = new Key("name", Type.STRING);

   public static void registerCommands() {
      McJtyLib.registerClientCommand("rftoolsutility", "returnDestinationInfo", (player, arguments) -> {
         MatterTransmitterBlock.setDestinationInfo((Integer)arguments.get(PARAM_ID), (String)arguments.get(PARAM_NAME));
         return true;
      });
   }
}
