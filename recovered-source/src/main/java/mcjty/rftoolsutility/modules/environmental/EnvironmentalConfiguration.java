package mcjty.rftoolsutility.modules.environmental;

import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public class EnvironmentalConfiguration {
   public static final String CATEGORY_ENVIRONMENTAL = "environmental";
   public static IntValue ENVIRONMENTAL_MAXENERGY;
   public static IntValue ENVIRONMENTAL_RECEIVEPERTICK;
   public static IntValue MIN_USAGE;
   public static DoubleValue FEATHERFALLING_RFPERTICK;
   public static DoubleValue FEATHERFALLINGPLUS_RFPERTICK;
   public static DoubleValue FLIGHT_RFPERTICK;
   public static DoubleValue GLOWING_RFPERTICK;
   public static DoubleValue HASTE_RFPERTICK;
   public static DoubleValue HASTEPLUS_RFPERTICK;
   public static DoubleValue NIGHTVISION_RFPERTICK;
   public static DoubleValue PEACEFUL_RFPERTICK;
   public static DoubleValue REGENERATION_RFPERTICK;
   public static DoubleValue REGENERATIONPLUS_RFPERTICK;
   public static DoubleValue SATURATION_RFPERTICK;
   public static DoubleValue SATURATIONPLUS_RFPERTICK;
   public static DoubleValue SPEED_RFPERTICK;
   public static DoubleValue SPEEDPLUS_RFPERTICK;
   public static DoubleValue WATERBREATHING_RFPERTICK;
   public static DoubleValue LUCK_RFPERTICK;
   public static DoubleValue NOTELEPORT_RFPERTICK;
   public static DoubleValue BLINDNESS_RFPERTICK;
   public static DoubleValue WEAKNESS_RFPERTICK;
   public static DoubleValue POISON_RFPERTICK;
   public static DoubleValue SLOWNESS_RFPERTICK;
   public static BooleanValue blindnessAvailable;
   public static BooleanValue weaknessAvailable;
   public static BooleanValue poisonAvailable;
   public static BooleanValue slownessAvailable;
   public static DoubleValue mobsPowerMultiplier;

   public static void init(Builder SERVER_BUILDER, Builder CLIENT_BUILDER) {
      SERVER_BUILDER.comment("Settings for the environmental controller").push("environmental");
      CLIENT_BUILDER.comment("Settings for the environmental controller").push("environmental");
      ENVIRONMENTAL_MAXENERGY = SERVER_BUILDER.comment("Maximum RF storage that the environmental controller can hold")
         .defineInRange("environmentalMaxRF", 500000, 0, Integer.MAX_VALUE);
      ENVIRONMENTAL_RECEIVEPERTICK = SERVER_BUILDER.comment("RF per tick that the the environmental controller can receive")
         .defineInRange("environmentalRFPerTick", 20000, 0, Integer.MAX_VALUE);
      MIN_USAGE = SERVER_BUILDER.comment("The minimum RF/tick usage that an active controller consumes")
         .defineInRange("environmentalMinRFUsage", 5, 0, Integer.MAX_VALUE);
      mobsPowerMultiplier = SERVER_BUILDER.comment("When the environmental controller is used on mobs the power usage is multiplied with this")
         .defineInRange("mobsPowerMultiplier", 2.0, 0.0, 1.0E8);
      FEATHERFALLING_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the feather falling module")
         .defineInRange("featherfallingRFPerTick", 0.001, 0.0, 1.0E9);
      FEATHERFALLINGPLUS_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the feather falling plus module")
         .defineInRange("featherfallingPlusRFPerTick", 0.003, 0.0, 1.0E9);
      FLIGHT_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the flight module").defineInRange("flightRFPerTick", 0.004, 0.0, 1.0E9);
      GLOWING_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the glowing module").defineInRange("glowingRFPerTick", 0.001, 0.0, 1.0E9);
      HASTE_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the haste module").defineInRange("hasteRFPerTick", 0.001, 0.0, 1.0E9);
      HASTEPLUS_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the haste plus module").defineInRange("hastePlusRFPerTick", 0.003, 0.0, 1.0E9);
      NIGHTVISION_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the night vision module")
         .defineInRange("nightvisionRFPerTick", 0.001, 0.0, 1.0E9);
      PEACEFUL_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the peaceful module").defineInRange("peacefulRFPerTick", 0.001, 0.0, 1.0E9);
      REGENERATION_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the regeneration module")
         .defineInRange("regenerationRFPerTick", 0.0015, 0.0, 1.0E9);
      REGENERATIONPLUS_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the regeneration plus module")
         .defineInRange("regenerationPlusRFPerTick", 0.0045, 0.0, 1.0E9);
      SATURATION_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the saturation module").defineInRange("saturationRFPerTick", 0.001, 0.0, 1.0E9);
      SATURATIONPLUS_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the saturation plus module")
         .defineInRange("saturationPlusRFPerTick", 0.003, 0.0, 1.0E9);
      SPEED_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the speed module").defineInRange("speedRFPerTick", 0.001, 0.0, 1.0E9);
      SPEEDPLUS_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the speed plus module").defineInRange("speedPlusRFPerTick", 0.003, 0.0, 1.0E9);
      WATERBREATHING_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the wather breathing module")
         .defineInRange("watherBreathingRFPerTick", 0.001, 0.0, 1.0E9);
      LUCK_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the luck module").defineInRange("luckRFPerTick", 0.002, 0.0, 1.0E9);
      NOTELEPORT_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the noTeleport module").defineInRange("noTeleportRFPerTick", 0.002, 0.0, 1.0E9);
      BLINDNESS_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the blindness module").defineInRange("blindnessRFPerTick", 0.01, 0.0, 1.0E9);
      blindnessAvailable = SERVER_BUILDER.comment("Set to true to make the blindness module work on players").define("blindnessAvailable", false);
      WEAKNESS_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the weakness module").defineInRange("weaknessRFPerTick", 0.01, 0.0, 1.0E9);
      weaknessAvailable = SERVER_BUILDER.comment("Set to true to make the weakness module work on players").define("weaknessAvailable", false);
      POISON_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the poison module").defineInRange("poisonRFPerTick", 0.02, 0.0, 1.0E9);
      poisonAvailable = SERVER_BUILDER.comment("Set to true to make the poison module work on players").define("poisonAvailable", false);
      SLOWNESS_RFPERTICK = SERVER_BUILDER.comment("RF per tick/per block for the slowness module").defineInRange("slownessRFPerTick", 0.012, 0.0, 1.0E9);
      slownessAvailable = SERVER_BUILDER.comment("Set to true to make the slowness module work on players").define("slownessAvailable", false);
      SERVER_BUILDER.pop();
      CLIENT_BUILDER.pop();
   }
}
