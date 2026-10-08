package mcjty.rftoolsutility;

import java.util.function.Function;
import java.util.function.Supplier;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.modules.Modules;
import mcjty.lib.varia.LegacyCapabilities;
import mcjty.rftoolsbase.api.screens.IScreenModuleRegistry;
import mcjty.rftoolsbase.api.teleportation.ITeleportationManager;
import mcjty.rftoolsutility.apiimpl.teleportation.TeleportationManager;
import mcjty.rftoolsutility.modules.crafter.CrafterModule;
import mcjty.rftoolsutility.modules.environmental.EnvironmentalModule;
import mcjty.rftoolsutility.modules.logic.LogicBlockModule;
import mcjty.rftoolsutility.modules.screen.ScreenModule;
import mcjty.rftoolsutility.modules.screen.ScreenModuleRegistry;
import mcjty.rftoolsutility.modules.spawner.SpawnerModule;
import mcjty.rftoolsutility.modules.tank.TankModule;
import mcjty.rftoolsutility.modules.teleporter.TeleporterModule;
import mcjty.rftoolsutility.modules.teleporter.items.porter.ChargedPorterEnergyHandler;
import mcjty.rftoolsutility.modules.teleporter.items.porter.ChargedPorterItem;
import mcjty.rftoolsutility.setup.ClientSetup;
import mcjty.rftoolsutility.setup.Config;
import mcjty.rftoolsutility.setup.ModSetup;
import mcjty.rftoolsutility.setup.RFToolsUtilityMessages;
import mcjty.rftoolsutility.setup.Registration;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.capabilities.Capabilities.Energy;
import net.neoforged.neoforge.data.event.GatherDataEvent.Client;

@Mod("rftoolsutility")
public class RFToolsUtility {
   public static final String MODID = "rftoolsutility";
   public static final ModSetup setup = new ModSetup();
   public static RFToolsUtility instance;
   private final Modules modules = new Modules();
   public static final ScreenModuleRegistry screenModuleRegistry = new ScreenModuleRegistry();

   public RFToolsUtility(ModContainer mod, IEventBus bus, Dist dist) {
      instance = this;
      this.setupModules(bus, dist);
      Config.register(mod, bus, this.modules);
      Registration.register(bus);
      bus.addListener(setup::init);
      bus.addListener(this.modules::init);
      bus.addListener(this::processIMC);
      bus.addListener(this::onDataGen);
      bus.addListener(RFToolsUtilityMessages::registerMessages);
      bus.addListener(setup.getBlockCapabilityRegistrar(Registration.RBLOCKS));
      bus.addListener(this::onRegisterCapabilities);
      if (dist.isClient()) {
         bus.addListener(RFToolsUtilityMessages::registerClientMessages);
         bus.addListener(ClientSetup::registerRangeItemModelProperties);
         bus.addListener(this.modules::initClient);
      }
   }

   public static <T extends Item> Supplier<T> tab(Supplier<T> supplier) {
      return setup.tab(supplier);
   }

   private void onDataGen(Client event) {
      DataGen datagen = new DataGen("rftoolsutility", event);
      this.modules.datagen(datagen, event.getLookupProvider());
      datagen.generate();
   }

   private void processIMC(InterModProcessEvent event) {
      event.getIMCStream().forEach(message -> {
         if ("getTeleportationManager".equals(message.method())) {
            Supplier<Function<ITeleportationManager, Void>> supplier = message.messageSupplier();
            supplier.get().apply(new TeleportationManager());
         } else if ("getScreenModuleRegistry".equalsIgnoreCase(message.method())) {
            Supplier<Function<IScreenModuleRegistry, Void>> supplier = message.messageSupplier();
            supplier.get().apply(screenModuleRegistry);
         }
      });
   }

   private void setupModules(IEventBus bus, Dist dist) {
      this.modules.register(new CrafterModule(bus));
      this.modules.register(new LogicBlockModule(bus));
      this.modules.register(new ScreenModule(bus));
      this.modules.register(new SpawnerModule(bus));
      this.modules.register(new TankModule(bus, dist));
      this.modules.register(new TeleporterModule(bus));
      this.modules.register(new EnvironmentalModule(bus, dist));
   }

   private void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
      Registration.ITEMS.getRegister().getEntries().forEach(entry -> {
         Item item = (Item)entry.get();
         if (item instanceof ChargedPorterItem porter) {
            event.registerItem(LegacyCapabilities.ENERGY_ITEM, (stack, context) -> porter.createEnergyStorage(stack), new ItemLike[]{item});
            event.registerItem(Energy.ITEM, (stack, context) -> context == null ? null : new ChargedPorterEnergyHandler(context, porter), new ItemLike[]{item});
         }
      });
   }
}
