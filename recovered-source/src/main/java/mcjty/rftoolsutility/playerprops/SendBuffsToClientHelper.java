package mcjty.rftoolsutility.playerprops;

import java.util.ArrayList;
import mcjty.rftoolsutility.client.RenderGameOverlayEventHandler;

public class SendBuffsToClientHelper {
   public static void setBuffs(PacketSendBuffsToClient buffs) {
      RenderGameOverlayEventHandler.buffs = new ArrayList<>(buffs.getBuffs());
   }
}
