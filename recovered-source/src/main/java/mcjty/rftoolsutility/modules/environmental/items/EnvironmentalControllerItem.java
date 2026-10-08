package mcjty.rftoolsutility.modules.environmental.items;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.environmental.EnvModuleProvider;
import mcjty.rftoolsutility.modules.environmental.EnvironmentalConfiguration;
import mcjty.rftoolsutility.modules.environmental.modules.BuffEModule;
import mcjty.rftoolsutility.modules.environmental.modules.EnvironmentModule;
import mcjty.rftoolsutility.modules.environmental.modules.NoTeleportEModule;
import mcjty.rftoolsutility.modules.environmental.modules.PeacefulEModule;
import mcjty.rftoolsutility.modules.environmental.modules.PotionEffectModule;
import mcjty.rftoolsutility.playerprops.PlayerBuff;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.util.Lazy;

public abstract class EnvironmentalControllerItem extends Item implements EnvModuleProvider, ITooltipSettings {
   private final Lazy<TooltipBuilder> tooltipBuilder;

   private EnvironmentalControllerItem(Lazy<TooltipBuilder> tooltipBuilder) {
      super(RFToolsUtility.setup.defaultProperties().stacksTo(16));
      this.tooltipBuilder = tooltipBuilder;
   }

   public void appendHoverText(
      @Nonnull ItemStack itemStack, TooltipContext context, TooltipDisplay display, @Nonnull Consumer<Component> list, @Nonnull TooltipFlag flag
   ) {
      super.appendHoverText(itemStack, context, display, list, flag);
      ((TooltipBuilder)this.tooltipBuilder.get()).makeTooltip(Tools.getId(this), itemStack, list, flag);
   }

   private static InfoLine[] createInfoLines(InfoLine[] inner, DoubleValue rfPerTick) {
      InfoLine[] lines = new InfoLine[1 + inner.length + 1];
      lines[0] = TooltipBuilder.header();
      System.arraycopy(inner, 0, lines, 1, inner.length);
      lines[lines.length - 1] = TooltipBuilder.parameter("power", stack -> rfPerTick.get() + " RF/tick (per cubic block)");
      return lines;
   }

   public static EnvironmentalControllerItem create(
      final String name, final Supplier<? extends EnvironmentModule> supplier, DoubleValue rfPerTick, InfoLine... tooltips
   ) {
      return new EnvironmentalControllerItem(
         Lazy.of(
            () -> new TooltipBuilder()
               .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsutility.shiftmessage")})
               .infoShift(createInfoLines(tooltips, rfPerTick))
         )
      ) {
         @Override
         public Supplier<? extends EnvironmentModule> getServerEnvironmentModule() {
            return supplier;
         }

         @Override
         public String getName() {
            return name;
         }
      };
   }

   public static EnvironmentalControllerItem createBlindnessModule() {
      return create(
         "Blindness",
         () -> PotionEffectModule.create(
            "blindness",
            0,
            PlayerBuff.BUFF_BLINDNESS,
            (Double)EnvironmentalConfiguration.BLINDNESS_RFPERTICK.get(),
            () -> (Boolean)EnvironmentalConfiguration.blindnessAvailable.get()
         ),
         EnvironmentalConfiguration.BLINDNESS_RFPERTICK,
         TooltipBuilder.warning(stack -> !(Boolean)EnvironmentalConfiguration.blindnessAvailable.get())
      );
   }

   public static EnvironmentalControllerItem createFeatherfallingModule() {
      return create(
         "Feather",
         () -> BuffEModule.create(PlayerBuff.BUFF_FEATHERFALLING, (Double)EnvironmentalConfiguration.FEATHERFALLING_RFPERTICK.get()),
         EnvironmentalConfiguration.FEATHERFALLING_RFPERTICK,
         TooltipBuilder.gold()
      );
   }

   public static EnvironmentalControllerItem createFeatherfallingPlusModule() {
      return create(
         "Feather+",
         () -> BuffEModule.create(PlayerBuff.BUFF_FEATHERFALLINGPLUS, (Double)EnvironmentalConfiguration.FEATHERFALLINGPLUS_RFPERTICK.get()),
         EnvironmentalConfiguration.FEATHERFALLINGPLUS_RFPERTICK,
         TooltipBuilder.gold()
      );
   }

   public static EnvironmentalControllerItem createFlightModule() {
      return create(
         "Flight",
         () -> BuffEModule.create(PlayerBuff.BUFF_FLIGHT, (Double)EnvironmentalConfiguration.FLIGHT_RFPERTICK.get()),
         EnvironmentalConfiguration.FLIGHT_RFPERTICK
      );
   }

   public static EnvironmentalControllerItem createGlowingModule() {
      return create(
         "Glowing",
         () -> PotionEffectModule.create("glowing", 0, PlayerBuff.BUFF_GLOWING, (Double)EnvironmentalConfiguration.GLOWING_RFPERTICK.get()),
         EnvironmentalConfiguration.GLOWING_RFPERTICK
      );
   }

   public static EnvironmentalControllerItem createHasteModule() {
      return create(
         "Haste",
         () -> PotionEffectModule.create("haste", 0, PlayerBuff.BUFF_HASTE, (Double)EnvironmentalConfiguration.HASTE_RFPERTICK.get()),
         EnvironmentalConfiguration.HASTE_RFPERTICK
      );
   }

   public static EnvironmentalControllerItem createHastePlusModule() {
      return create(
         "Haste+",
         () -> PotionEffectModule.create("haste", 2, PlayerBuff.BUFF_HASTEPLUS, (Double)EnvironmentalConfiguration.HASTEPLUS_RFPERTICK.get()),
         EnvironmentalConfiguration.HASTEPLUS_RFPERTICK
      );
   }

   public static EnvironmentalControllerItem createLuckModule() {
      return create(
         "Luck",
         () -> PotionEffectModule.create("luck", 0, PlayerBuff.BUFF_LUCK, (Double)EnvironmentalConfiguration.LUCK_RFPERTICK.get()),
         EnvironmentalConfiguration.LUCK_RFPERTICK
      );
   }

   public static EnvironmentalControllerItem createNightvisionModule() {
      return create(
         "Vision",
         () -> PotionEffectModule.create("night_vision", 0, PlayerBuff.BUFF_NIGHTVISION, (Double)EnvironmentalConfiguration.NIGHTVISION_RFPERTICK.get()),
         EnvironmentalConfiguration.NIGHTVISION_RFPERTICK
      );
   }

   public static EnvironmentalControllerItem createNoteleportModule() {
      return create("NoTP", NoTeleportEModule::new, EnvironmentalConfiguration.NOTELEPORT_RFPERTICK);
   }

   public static EnvironmentalControllerItem createPeacefulModule() {
      return create("Peace", PeacefulEModule::new, EnvironmentalConfiguration.PEACEFUL_RFPERTICK);
   }

   public static EnvironmentalControllerItem createPoisonModule() {
      return create(
         "Poison",
         () -> PotionEffectModule.create(
            "poison",
            1,
            PlayerBuff.BUFF_POISON,
            (Double)EnvironmentalConfiguration.POISON_RFPERTICK.get(),
            () -> (Boolean)EnvironmentalConfiguration.poisonAvailable.get()
         ),
         EnvironmentalConfiguration.POISON_RFPERTICK,
         TooltipBuilder.warning(stack -> !(Boolean)EnvironmentalConfiguration.poisonAvailable.get())
      );
   }

   public static EnvironmentalControllerItem createRegenerationModule() {
      return create(
         "Regen",
         () -> PotionEffectModule.create("regeneration", 0, PlayerBuff.BUFF_REGENERATION, (Double)EnvironmentalConfiguration.REGENERATION_RFPERTICK.get()),
         EnvironmentalConfiguration.REGENERATION_RFPERTICK
      );
   }

   public static EnvironmentalControllerItem createRegenerationPlusModule() {
      return create(
         "Regen+",
         () -> PotionEffectModule.create(
            "regeneration", 2, PlayerBuff.BUFF_REGENERATIONPLUS, (Double)EnvironmentalConfiguration.REGENERATIONPLUS_RFPERTICK.get()
         ),
         EnvironmentalConfiguration.REGENERATIONPLUS_RFPERTICK
      );
   }

   public static EnvironmentalControllerItem createSaturationModule() {
      return create(
         "Saturation",
         () -> PotionEffectModule.create("saturation", 0, PlayerBuff.BUFF_SATURATION, (Double)EnvironmentalConfiguration.SATURATION_RFPERTICK.get()),
         EnvironmentalConfiguration.SATURATION_RFPERTICK
      );
   }

   public static EnvironmentalControllerItem createSaturationPlusModule() {
      return create(
         "Saturation+",
         () -> PotionEffectModule.create("saturation", 2, PlayerBuff.BUFF_SATURATIONPLUS, (Double)EnvironmentalConfiguration.SATURATIONPLUS_RFPERTICK.get()),
         EnvironmentalConfiguration.SATURATIONPLUS_RFPERTICK
      );
   }

   public static EnvironmentalControllerItem createSlownessModule() {
      return create(
         "Slowness",
         () -> PotionEffectModule.create(
            "slowness",
            3,
            PlayerBuff.BUFF_SLOWNESS,
            (Double)EnvironmentalConfiguration.SLOWNESS_RFPERTICK.get(),
            () -> (Boolean)EnvironmentalConfiguration.slownessAvailable.get()
         ),
         EnvironmentalConfiguration.SLOWNESS_RFPERTICK,
         TooltipBuilder.warning(stack -> !(Boolean)EnvironmentalConfiguration.slownessAvailable.get())
      );
   }

   public static EnvironmentalControllerItem createSpeedModule() {
      return create(
         "Speed",
         () -> PotionEffectModule.create("speed", 0, PlayerBuff.BUFF_SPEED, (Double)EnvironmentalConfiguration.SPEED_RFPERTICK.get()),
         EnvironmentalConfiguration.SPEED_RFPERTICK
      );
   }

   public static EnvironmentalControllerItem createSpeedPlusModule() {
      return create(
         "Speed+",
         () -> PotionEffectModule.create("speed", 2, PlayerBuff.BUFF_SPEEDPLUS, (Double)EnvironmentalConfiguration.SPEEDPLUS_RFPERTICK.get()),
         EnvironmentalConfiguration.SPEEDPLUS_RFPERTICK
      );
   }

   public static EnvironmentalControllerItem createWaterbreathingModule() {
      return create(
         "Water",
         () -> PotionEffectModule.create(
            "water_breathing", 0, PlayerBuff.BUFF_WATERBREATHING, (Double)EnvironmentalConfiguration.WATERBREATHING_RFPERTICK.get()
         ),
         EnvironmentalConfiguration.WATERBREATHING_RFPERTICK
      );
   }

   public static EnvironmentalControllerItem createWeaknessModule() {
      return create(
         "Weakness",
         () -> PotionEffectModule.create(
            "weakness",
            1,
            PlayerBuff.BUFF_WEAKNESS,
            (Double)EnvironmentalConfiguration.WEAKNESS_RFPERTICK.get(),
            () -> (Boolean)EnvironmentalConfiguration.weaknessAvailable.get()
         ),
         EnvironmentalConfiguration.WEAKNESS_RFPERTICK,
         TooltipBuilder.warning(stack -> !(Boolean)EnvironmentalConfiguration.weaknessAvailable.get())
      );
   }
}
