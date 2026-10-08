package mcjty.rftoolsutility.modules.screen;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public class ScreenConfiguration {
   public static final String CATEGORY_SCREEN = "screen";
   public static IntValue CONTROLLER_MAXENERGY;
   public static IntValue CONTROLLER_RECEIVEPERTICK;
   public static IntValue BUTTON_RFPERTICK;
   public static IntValue CLOCK_RFPERTICK;
   public static IntValue COMPUTER_RFPERTICK;
   public static IntValue COUNTER_RFPERTICK;
   public static IntValue DIMENSION_RFPERTICK;
   public static IntValue ENERGY_RFPERTICK;
   public static IntValue FLUID_RFPERTICK;
   public static IntValue ITEMSTACK_RFPERTICK;
   public static IntValue MACHINEINFO_RFPERTICK;
   public static IntValue REDSTONE_RFPERTICK;
   public static IntValue TEXT_RFPERTICK;
   public static IntValue SCREEN_REFRESH_TIMING;
   public static BooleanValue useTruetype;
   public static BooleanValue forceNoTruetype;
   public static ConfigValue<String> font;
   public static DoubleValue fontSize;
   public static ConfigValue<String> additionalCharacters;
   public static Identifier trueTypeFont = null;

   public static void init(Builder SERVER_BUILDER, Builder CLIENT_BUILDER) {
      SERVER_BUILDER.comment("Settings for the screen system").push("screen");
      CLIENT_BUILDER.comment("Settings for the screen system").push("screen");
      CONTROLLER_MAXENERGY = SERVER_BUILDER.comment("Maximum RF storage that the screen controller can hold")
         .defineInRange("screenControllerMaxRF", 60000, 0, Integer.MAX_VALUE);
      CONTROLLER_RECEIVEPERTICK = SERVER_BUILDER.comment("RF per tick that the the screen controller can receive")
         .defineInRange("screenControllerRFPerTick", 1000, 0, Integer.MAX_VALUE);
      BUTTON_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the button module").defineInRange("buttonRFPerTick", 9, 0, Integer.MAX_VALUE);
      CLOCK_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the clock module").defineInRange("clockRFPerTick", 1, 0, Integer.MAX_VALUE);
      COMPUTER_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the computer module").defineInRange("computerRFPerTick", 4, 0, Integer.MAX_VALUE);
      COUNTER_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the counter module").defineInRange("counterRFPerTick", 4, 0, Integer.MAX_VALUE);
      DIMENSION_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the dimension module")
         .defineInRange("dimensionRFPerTick", 6, 0, Integer.MAX_VALUE);
      ENERGY_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the energy module").defineInRange("energyRFPerTick", 4, 0, Integer.MAX_VALUE);
      FLUID_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the fluid module").defineInRange("fluidRFPerTick", 4, 0, Integer.MAX_VALUE);
      ITEMSTACK_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the itemstack module")
         .defineInRange("itemstackRFPerTick", 4, 0, Integer.MAX_VALUE);
      MACHINEINFO_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the machine information module")
         .defineInRange("machineInfoRFPerTick", 4, 0, Integer.MAX_VALUE);
      REDSTONE_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the redstone module").defineInRange("redstoneRFPerTick", 4, 0, Integer.MAX_VALUE);
      TEXT_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the text module").defineInRange("textRFPerTick", 0, 0, Integer.MAX_VALUE);
      useTruetype = CLIENT_BUILDER.comment("Set to true for TrueType font, set to false for vanilla font").define("useTruetype", false);
      forceNoTruetype = CLIENT_BUILDER.comment(
            "Set to true for force TrueType to be disabled in all cases. Use this in case the truetype font is causing issues"
         )
         .define("forceNoTruetype", false);
      font = CLIENT_BUILDER.comment("The default truetype font to use").define("fontName", "rftoolsutility:ubuntu");
      fontSize = CLIENT_BUILDER.comment("The size of the font").defineInRange("fontSize", 40.0, 0.0, 1000000.0);
      additionalCharacters = CLIENT_BUILDER.comment("Additional characters that should be supported by the truetype system").define("additionalCharacters", "");
      SCREEN_REFRESH_TIMING = SERVER_BUILDER.comment(
            "How many times the screen will update. Higher numbers make the screens less accurate but better for network bandwidth"
         )
         .defineInRange("screenRefreshTiming", 500, 0, Integer.MAX_VALUE);
      CLIENT_BUILDER.pop();
      SERVER_BUILDER.pop();
   }

   public static Identifier getTrueTypeFont() {
      if (trueTypeFont == null) {
         trueTypeFont = Identifier.parse((String)font.get());
      }

      return trueTypeFont;
   }
}
