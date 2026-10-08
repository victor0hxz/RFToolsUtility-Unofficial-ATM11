package mcjty.rftoolsutility.modules.tank;

import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public class TankConfiguration {
   public static final String CATEGORY_TANK = "tank";
   public static IntValue MAXCAPACITY;

   public static void init(Builder SERVER_BUILDER, Builder CLIENT_BUILDER) {
      SERVER_BUILDER.comment("Settings for the tank").push("tank");
      CLIENT_BUILDER.comment("Settings for the tank").push("tank");
      MAXCAPACITY = SERVER_BUILDER.comment("Maximum tank capacity (in mb)").defineInRange("maxCapacity", 32000, 0, Integer.MAX_VALUE);
      SERVER_BUILDER.pop();
      CLIENT_BUILDER.pop();
   }
}
