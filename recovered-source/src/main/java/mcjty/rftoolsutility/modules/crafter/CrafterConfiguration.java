package mcjty.rftoolsutility.modules.crafter;

import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public class CrafterConfiguration {
   public static final String CATEGORY_CRAFTER = "crafter";
   public static IntValue MAXENERGY;
   public static IntValue RECEIVEPERTICK;
   public static IntValue rfPerOperation;
   public static IntValue speedOperations;

   public static void init(Builder SERVER_BUILDER, Builder CLIENT_BUILDER) {
      SERVER_BUILDER.comment("Settings for the crafter").push("crafter");
      CLIENT_BUILDER.comment("Settings for the crafter").push("crafter");
      rfPerOperation = SERVER_BUILDER.comment("Amount of RF used per crafting operation").defineInRange("rfPerOperation", 100, 0, Integer.MAX_VALUE);
      speedOperations = SERVER_BUILDER.comment("How many operations to do at once in fast mode").defineInRange("speedOperations", 5, 0, Integer.MAX_VALUE);
      MAXENERGY = SERVER_BUILDER.comment("Maximum RF storage that the crafter can hold").defineInRange("crafterMaxRF", 50000, 0, Integer.MAX_VALUE);
      RECEIVEPERTICK = SERVER_BUILDER.comment("RF per tick that the crafter can receive").defineInRange("crafterRFPerTick", 500, 0, Integer.MAX_VALUE);
      SERVER_BUILDER.pop();
      CLIENT_BUILDER.pop();
   }
}
