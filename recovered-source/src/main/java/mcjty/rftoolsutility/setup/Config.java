package mcjty.rftoolsutility.setup;

import mcjty.lib.modules.Modules;
import mcjty.rftoolsutility.modules.screen.ScreenConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.config.ModConfigEvent.Reloading;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;

@EventBusSubscriber(modid = "rftoolsutility")
public class Config {
   public static final String CATEGORY_GENERAL = "general";
   public static final Builder SERVER_BUILDER = new Builder();
   public static final Builder CLIENT_BUILDER = new Builder();
   public static ModConfigSpec SERVER_CONFIG;
   public static ModConfigSpec CLIENT_CONFIG;

   public static void register(ModContainer container, IEventBus bus, Modules modules) {
      setupGeneralConfig();
      modules.initConfig(bus);
      SERVER_CONFIG = SERVER_BUILDER.build();
      CLIENT_CONFIG = CLIENT_BUILDER.build();
      container.registerConfig(Type.CLIENT, CLIENT_CONFIG);
      container.registerConfig(Type.SERVER, SERVER_CONFIG);
   }

   private static void setupGeneralConfig() {
      SERVER_BUILDER.comment("General settings").push("general");
      CLIENT_BUILDER.comment("General settings").push("general");
      SERVER_BUILDER.pop();
      CLIENT_BUILDER.pop();
   }

   @SubscribeEvent
   public static void onConfigReload(Reloading event) {
      ScreenConfiguration.trueTypeFont = null;
   }
}
