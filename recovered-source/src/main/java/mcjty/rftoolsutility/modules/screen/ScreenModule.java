package mcjty.rftoolsutility.modules.screen;

import java.util.function.Supplier;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.items.BaseBlockItem;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsbase.modules.tablet.items.TabletItem;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.screen.blocks.CreativeScreenTileEntity;
import mcjty.rftoolsutility.modules.screen.blocks.ScreenBlock;
import mcjty.rftoolsutility.modules.screen.blocks.ScreenContainer;
import mcjty.rftoolsutility.modules.screen.blocks.ScreenControllerBlock;
import mcjty.rftoolsutility.modules.screen.blocks.ScreenControllerTileEntity;
import mcjty.rftoolsutility.modules.screen.blocks.ScreenHitBlock;
import mcjty.rftoolsutility.modules.screen.blocks.ScreenHitTileEntity;
import mcjty.rftoolsutility.modules.screen.blocks.ScreenTileEntity;
import mcjty.rftoolsutility.modules.screen.client.GuiScreen;
import mcjty.rftoolsutility.modules.screen.client.GuiScreenController;
import mcjty.rftoolsutility.modules.screen.data.ScreenData;
import mcjty.rftoolsutility.modules.screen.items.ScreenLinkItem;
import mcjty.rftoolsutility.modules.screen.items.ScreenTabletItem;
import mcjty.rftoolsutility.modules.screen.items.modules.ButtonModuleItem;
import mcjty.rftoolsutility.modules.screen.items.modules.ClockModuleItem;
import mcjty.rftoolsutility.modules.screen.items.modules.CounterModuleItem;
import mcjty.rftoolsutility.modules.screen.items.modules.CounterPlusModuleItem;
import mcjty.rftoolsutility.modules.screen.items.modules.EnergyModuleItem;
import mcjty.rftoolsutility.modules.screen.items.modules.EnergyPlusModuleItem;
import mcjty.rftoolsutility.modules.screen.items.modules.FluidModuleItem;
import mcjty.rftoolsutility.modules.screen.items.modules.FluidPlusModuleItem;
import mcjty.rftoolsutility.modules.screen.items.modules.InventoryModuleItem;
import mcjty.rftoolsutility.modules.screen.items.modules.InventoryPlusModuleItem;
import mcjty.rftoolsutility.modules.screen.items.modules.MachineInformationModuleItem;
import mcjty.rftoolsutility.modules.screen.items.modules.RedstoneModuleItem;
import mcjty.rftoolsutility.modules.screen.items.modules.TextModuleItem;
import mcjty.rftoolsutility.modules.screen.modules.ButtonScreenModule;
import mcjty.rftoolsutility.modules.screen.modules.ClockScreenModule;
import mcjty.rftoolsutility.modules.screen.modules.CounterScreenModule;
import mcjty.rftoolsutility.modules.screen.modules.EnergyBarScreenModule;
import mcjty.rftoolsutility.modules.screen.modules.FluidBarScreenModule;
import mcjty.rftoolsutility.modules.screen.modules.InventoryScreenModule;
import mcjty.rftoolsutility.modules.screen.modules.MachineInformationScreenModule;
import mcjty.rftoolsutility.modules.screen.modules.RedstoneScreenModule;
import mcjty.rftoolsutility.modules.screen.modules.TextScreenModule;
import mcjty.rftoolsutility.setup.Config;
import mcjty.rftoolsutility.setup.Registration;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public class ScreenModule implements IModule {
   public static final RBlock<ScreenBlock, BlockItem, ScreenTileEntity> SCREEN = Registration.RBLOCKS
      .registerBlock(
         "screen",
         ScreenTileEntity.class,
         () -> new ScreenBlock(ScreenTileEntity::new, false),
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         ScreenTileEntity::new
      );
   public static final RBlock<ScreenBlock, BlockItem, CreativeScreenTileEntity> CREATIVE_SCREEN = Registration.RBLOCKS
      .registerBlock(
         "creative_screen",
         CreativeScreenTileEntity.class,
         () -> new ScreenBlock(CreativeScreenTileEntity::new, true),
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         CreativeScreenTileEntity::new
      );
   public static final Supplier<MenuType<ScreenContainer>> CONTAINER_SCREEN = Registration.CONTAINERS.register("screen", GenericContainer::createContainerType);
   public static final Supplier<MenuType<ScreenContainer>> CONTAINER_SCREEN_REMOTE = Registration.CONTAINERS
      .register("screen_remote", () -> GenericContainer.createRemoteContainerType(ScreenTileEntity::new, ScreenContainer::createRemote, 11));
   public static final Supplier<MenuType<ScreenContainer>> CONTAINER_SCREEN_REMOTE_CREATIVE = Registration.CONTAINERS
      .register(
         "screen_remote_creative", () -> GenericContainer.createRemoteContainerType(CreativeScreenTileEntity::new, ScreenContainer::createRemoteCreative, 11)
      );
   public static final RBlock<ScreenHitBlock, BlockItem, ScreenHitTileEntity> SCREEN_HIT = Registration.RBLOCKS
      .registerBlock("screen_hitblock", ScreenHitTileEntity.class, ScreenHitBlock::new, null, ScreenHitTileEntity::new);
   public static final RBlock<ScreenControllerBlock, BlockItem, ScreenControllerTileEntity> SCREEN_CONTROLLER = Registration.RBLOCKS
      .registerBlock(
         "screen_controller",
         ScreenControllerTileEntity.class,
         ScreenControllerBlock::new,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         ScreenControllerTileEntity::new
      );
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_SCREEN_CONTROLLER = Registration.CONTAINERS
      .register("screen_controller", GenericContainer::createContainerType);
   public static final DeferredItem<Item> TEXT_MODULE = Registration.ITEMS.register("text_module", RFToolsUtility.tab(TextModuleItem::new));
   public static final DeferredItem<Item> ENERGY_MODULE = Registration.ITEMS.register("energy_module", RFToolsUtility.tab(EnergyModuleItem::new));
   public static final DeferredItem<Item> ENERGYPLUS_MODULE = Registration.ITEMS.register("energyplus_module", RFToolsUtility.tab(EnergyPlusModuleItem::new));
   public static final DeferredItem<Item> INVENTORY_MODULE = Registration.ITEMS.register("inventory_module", RFToolsUtility.tab(InventoryModuleItem::new));
   public static final DeferredItem<Item> INVENTORYPLUS_MODULE = Registration.ITEMS
      .register("inventoryplus_module", RFToolsUtility.tab(InventoryPlusModuleItem::new));
   public static final DeferredItem<Item> CLOCK_MODULE = Registration.ITEMS.register("clock_module", RFToolsUtility.tab(ClockModuleItem::new));
   public static final DeferredItem<Item> FLUID_MODULE = Registration.ITEMS.register("fluid_module", RFToolsUtility.tab(FluidModuleItem::new));
   public static final DeferredItem<Item> FLUIDPLUS_MODULE = Registration.ITEMS.register("fluidplus_module", RFToolsUtility.tab(FluidPlusModuleItem::new));
   public static final DeferredItem<Item> MACHINEINFORMATION_MODULE = Registration.ITEMS
      .register("machineinformation_module", RFToolsUtility.tab(MachineInformationModuleItem::new));
   public static final DeferredItem<Item> BUTTON_MODULE = Registration.ITEMS.register("button_module", RFToolsUtility.tab(ButtonModuleItem::new));
   public static final DeferredItem<Item> REDSTONE_MODULE = Registration.ITEMS.register("redstone_module", RFToolsUtility.tab(RedstoneModuleItem::new));
   public static final DeferredItem<Item> COUNTER_MODULE = Registration.ITEMS.register("counter_module", RFToolsUtility.tab(CounterModuleItem::new));
   public static final DeferredItem<Item> COUNTERPLUS_MODULE = Registration.ITEMS
      .register("counterplus_module", RFToolsUtility.tab(CounterPlusModuleItem::new));
   public static final DeferredItem<TabletItem> TABLET_SCREEN = Registration.ITEMS.register("tablet_screen", RFToolsUtility.tab(ScreenTabletItem::new));
   public static final DeferredItem<ScreenLinkItem> SCREEN_LINK = Registration.ITEMS.register("screen_link", RFToolsUtility.tab(ScreenLinkItem::new));
   public static final Supplier<AttachmentType<ScreenData>> SCREEN_DATA = Registration.ATTACHMENT_TYPES
      .register("screen_data", () -> AttachmentType.builder(ScreenData::createDefault).serialize(ScreenData.CODEC.fieldOf("data")).build());
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<ScreenData>> ITEM_SCREEN_DATA = Registration.COMPONENTS
      .registerComponentType("screen_data", builder -> builder.persistent(ScreenData.CODEC).networkSynchronized(ScreenData.STREAM_CODEC));
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<ClockScreenModule>> MODULE_CLOCK_DATA = Registration.COMPONENTS
      .registerComponentType("module_clock_data", builder -> builder.persistent(ClockScreenModule.CODEC).networkSynchronized(ClockScreenModule.STREAM_CODEC));
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<EnergyBarScreenModule>> MODULE_ENERGY_BAR_DATA = Registration.COMPONENTS
      .registerComponentType(
         "module_energy_bar_data", builder -> builder.persistent(EnergyBarScreenModule.CODEC).networkSynchronized(EnergyBarScreenModule.STREAM_CODEC)
      );
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<ButtonScreenModule>> MODULE_BUTTON_DATA = Registration.COMPONENTS
      .registerComponentType("module_button_data", builder -> builder.persistent(ButtonScreenModule.CODEC).networkSynchronized(ButtonScreenModule.STREAM_CODEC));
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<CounterScreenModule>> MODULE_COUNTER_DATA = Registration.COMPONENTS
      .registerComponentType(
         "module_counter_data", builder -> builder.persistent(CounterScreenModule.CODEC).networkSynchronized(CounterScreenModule.STREAM_CODEC)
      );
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<FluidBarScreenModule>> MODULE_FLUIDBAR_DATA = Registration.COMPONENTS
      .registerComponentType(
         "module_fluidbar_data", builder -> builder.persistent(FluidBarScreenModule.CODEC).networkSynchronized(FluidBarScreenModule.STREAM_CODEC)
      );
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<InventoryScreenModule>> MODULE_INVENTORY_DATA = Registration.COMPONENTS
      .registerComponentType(
         "module_inventory_data", builder -> builder.persistent(InventoryScreenModule.CODEC).networkSynchronized(InventoryScreenModule.STREAM_CODEC)
      );
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<MachineInformationScreenModule>> MODULE_MACHINEINFO_DATA = Registration.COMPONENTS
      .registerComponentType(
         "module_machineinfo_data",
         builder -> builder.persistent(MachineInformationScreenModule.CODEC).networkSynchronized(MachineInformationScreenModule.STREAM_CODEC)
      );
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<RedstoneScreenModule>> MODULE_REDSTONE_DATA = Registration.COMPONENTS
      .registerComponentType(
         "module_redstone_data", builder -> builder.persistent(RedstoneScreenModule.CODEC).networkSynchronized(RedstoneScreenModule.STREAM_CODEC)
      );
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<TextScreenModule>> MODULE_TEXT_DATA = Registration.COMPONENTS
      .registerComponentType("module_text_data", builder -> builder.persistent(TextScreenModule.CODEC).networkSynchronized(TextScreenModule.STREAM_CODEC));

   public ScreenModule(IEventBus bus) {
      bus.addListener(this::registerMenuScreens);
   }

   public void init(FMLCommonSetupEvent event) {
   }

   public void registerMenuScreens(RegisterMenuScreensEvent event) {
      GuiScreen.register(event);
      GuiScreenController.register(event);
   }

   public void initClient(FMLClientSetupEvent event) {
   }

   public void initConfig(IEventBus bus) {
      ScreenConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
   }

   public void initDatagen(DataGen dataGen, Provider provider) {
   }
}
