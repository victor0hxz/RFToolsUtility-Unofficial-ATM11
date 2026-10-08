package mcjty.rftoolsutility.setup;

import mcjty.lib.McJtyLib;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsutility.modules.teleporter.PorterTools;
import net.minecraft.core.BlockPos;

public class CommandHandler {
   public static final String CMD_GET_DESTINATION_INFO = "getDestinationInfo";
   public static final Key<Integer> PARAM_ID = new Key("id", Type.INTEGER);
   public static final String CMD_CLEAR_TARGET = "clearTarget";
   public static final String CMD_SET_TARGET = "setTarget";
   public static final Key<Integer> PARAM_TARGET = new Key("target", Type.INTEGER);
   public static final String CMD_GET_TARGETS = "getTargets";
   public static final String CMD_FORCE_TELEPORT = "forceTeleport";
   public static final Key<String> PARAM_DIMENSION = new Key("dimension", Type.STRING);
   public static final Key<BlockPos> PARAM_POS = new Key("pos", Type.BLOCKPOS);

   public static void registerCommands() {
      McJtyLib.registerCommand("rftoolsutility", "getDestinationInfo", (player, arguments) -> {
         PorterTools.returnDestinationInfo(player, (Integer)arguments.get(PARAM_ID));
         return true;
      });
      McJtyLib.registerCommand("rftoolsutility", "clearTarget", (player, arguments) -> {
         PorterTools.clearTarget(player, (Integer)arguments.get(PARAM_TARGET));
         return true;
      });
      McJtyLib.registerCommand("rftoolsutility", "setTarget", (player, arguments) -> {
         PorterTools.setTarget(player, (Integer)arguments.get(PARAM_TARGET));
         return true;
      });
      McJtyLib.registerCommand("rftoolsutility", "getTargets", (player, arguments) -> {
         PorterTools.returnTargets(player);
         return true;
      });
      McJtyLib.registerCommand("rftoolsutility", "forceTeleport", (player, arguments) -> {
         PorterTools.forceTeleport(player, LevelTools.getId((String)arguments.get(PARAM_DIMENSION)), (BlockPos)arguments.get(PARAM_POS));
         return true;
      });
   }
}
