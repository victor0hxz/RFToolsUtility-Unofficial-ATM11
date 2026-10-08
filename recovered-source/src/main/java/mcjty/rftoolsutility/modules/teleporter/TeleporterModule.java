package mcjty.rftoolsutility.modules.teleporter;

import java.util.function.Supplier;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.items.BaseBlockItem;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.teleporter.blocks.DestinationAnalyzerBlock;
import mcjty.rftoolsutility.modules.teleporter.blocks.DialingDeviceBlock;
import mcjty.rftoolsutility.modules.teleporter.blocks.DialingDeviceTileEntity;
import mcjty.rftoolsutility.modules.teleporter.blocks.MatterBoosterBlock;
import mcjty.rftoolsutility.modules.teleporter.blocks.MatterReceiverBlock;
import mcjty.rftoolsutility.modules.teleporter.blocks.MatterReceiverTileEntity;
import mcjty.rftoolsutility.modules.teleporter.blocks.MatterTransmitterBlock;
import mcjty.rftoolsutility.modules.teleporter.blocks.MatterTransmitterTileEntity;
import mcjty.rftoolsutility.modules.teleporter.blocks.SimpleDialerBlock;
import mcjty.rftoolsutility.modules.teleporter.blocks.SimpleDialerItemBlock;
import mcjty.rftoolsutility.modules.teleporter.blocks.SimpleDialerTileEntity;
import mcjty.rftoolsutility.modules.teleporter.client.GuiDialingDevice;
import mcjty.rftoolsutility.modules.teleporter.client.GuiMatterReceiver;
import mcjty.rftoolsutility.modules.teleporter.client.GuiMatterTransmitter;
import mcjty.rftoolsutility.modules.teleporter.data.ChargedPorterData;
import mcjty.rftoolsutility.modules.teleporter.data.DialingDeviceData;
import mcjty.rftoolsutility.modules.teleporter.data.MatterReceiverData;
import mcjty.rftoolsutility.modules.teleporter.data.MatterTransmitterData;
import mcjty.rftoolsutility.modules.teleporter.data.SimpleDialerData;
import mcjty.rftoolsutility.modules.teleporter.items.porter.AdvancedChargedPorterItem;
import mcjty.rftoolsutility.modules.teleporter.items.porter.ChargedPorterItem;
import mcjty.rftoolsutility.modules.teleporter.items.teleportprobe.TeleportProbeItem;
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
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public class TeleporterModule implements IModule {
   public static final RBlock<BaseBlock, BlockItem, MatterTransmitterTileEntity> MATTER_TRANSMITTER = Registration.RBLOCKS
      .registerBlock(
         "matter_transmitter",
         MatterTransmitterTileEntity.class,
         MatterTransmitterBlock::new,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         MatterTransmitterTileEntity::new
      );
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_MATTER_TRANSMITTER = Registration.CONTAINERS
      .register("matter_transmitter", GenericContainer::createContainerType);
   public static final RBlock<BaseBlock, BlockItem, MatterReceiverTileEntity> MATTER_RECEIVER = Registration.RBLOCKS
      .registerBlock(
         "matter_receiver",
         MatterReceiverTileEntity.class,
         MatterReceiverBlock::new,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         MatterReceiverTileEntity::new
      );
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_MATTER_RECEIVER = Registration.CONTAINERS
      .register("matter_receiver", GenericContainer::createContainerType);
   public static final RBlock<BaseBlock, BlockItem, DialingDeviceTileEntity> DIALING_DEVICE = Registration.RBLOCKS
      .registerBlock(
         "dialing_device",
         DialingDeviceTileEntity.class,
         DialingDeviceBlock::new,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         DialingDeviceTileEntity::new
      );
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_DIALING_DEVICE = Registration.CONTAINERS
      .register("dialing_device", GenericContainer::createContainerType);
   public static final DeferredBlock<DestinationAnalyzerBlock> DESTINATION_ANALYZER = Registration.BLOCKS
      .register("destination_analyzer", DestinationAnalyzerBlock::new);
   public static final DeferredItem<Item> DESTINATION_ANALYZER_ITEM = Registration.ITEMS
      .register("destination_analyzer", RFToolsUtility.tab(() -> new BlockItem((Block)DESTINATION_ANALYZER.get(), Registration.createStandardProperties())));
   public static final DeferredBlock<MatterBoosterBlock> MATTER_BOOSTER = Registration.BLOCKS.register("matter_booster", MatterBoosterBlock::new);
   public static final DeferredItem<Item> MATTER_BOOSTER_ITEM = Registration.ITEMS
      .register("matter_booster", RFToolsUtility.tab(() -> new BlockItem((Block)MATTER_BOOSTER.get(), Registration.createStandardProperties())));
   public static final RBlock<LogicSlabBlock, BlockItem, SimpleDialerTileEntity> SIMPLE_DIALER = Registration.RBLOCKS
      .registerBlock(
         "simple_dialer",
         SimpleDialerTileEntity.class,
         SimpleDialerBlock::new,
         block -> new SimpleDialerItemBlock((Block)block.get()),
         SimpleDialerTileEntity::new
      );
   public static final DeferredItem<TeleportProbeItem> TELEPORT_PROBE = Registration.ITEMS
      .register("teleport_probe", RFToolsUtility.tab(TeleportProbeItem::new));
   public static final DeferredItem<ChargedPorterItem> CHARGED_PORTER = Registration.ITEMS
      .register("charged_porter", RFToolsUtility.tab(ChargedPorterItem::new));
   public static final DeferredItem<AdvancedChargedPorterItem> ADVANCED_CHARGED_PORTER = Registration.ITEMS
      .register("advanced_charged_porter", RFToolsUtility.tab(AdvancedChargedPorterItem::new));
   public static final Supplier<AttachmentType<DialingDeviceData>> DIALINGDEVICE_DATA = Registration.ATTACHMENT_TYPES
      .register("dialingdevice_data", () -> AttachmentType.builder(DialingDeviceData::createDefault).serialize(DialingDeviceData.CODEC.fieldOf("data")).build());
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<DialingDeviceData>> ITEM_DIALINGDEVICE_DATA = Registration.COMPONENTS
      .registerComponentType("dialingdevice_data", builder -> builder.persistent(DialingDeviceData.CODEC).networkSynchronized(DialingDeviceData.STREAM_CODEC));
   public static final Supplier<AttachmentType<MatterReceiverData>> MATTERRECEIVER_DATA = Registration.ATTACHMENT_TYPES
      .register(
         "matterreceiver_data", () -> AttachmentType.builder(() -> MatterReceiverData.DEFAULT).serialize(MatterReceiverData.CODEC.fieldOf("data")).build()
      );
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<MatterReceiverData>> ITEM_MATTERRECEIVER_DATA = Registration.COMPONENTS
      .registerComponentType(
         "matterreceiver_data", builder -> builder.persistent(MatterReceiverData.CODEC).networkSynchronized(MatterReceiverData.STREAM_CODEC)
      );
   public static final Supplier<AttachmentType<MatterTransmitterData>> MATTERTRANSMITTER_DATA = Registration.ATTACHMENT_TYPES
      .register(
         "mattertransmitter_data",
         () -> AttachmentType.builder(MatterTransmitterData::createDefault).serialize(MatterTransmitterData.CODEC.fieldOf("data")).build()
      );
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<MatterTransmitterData>> ITEM_MATTERTRANSMITTER_DATA = Registration.COMPONENTS
      .registerComponentType(
         "mattertransmitter_data", builder -> builder.persistent(MatterTransmitterData.CODEC).networkSynchronized(MatterTransmitterData.STREAM_CODEC)
      );
   public static final Supplier<AttachmentType<SimpleDialerData>> SIMPLEDIALER_DATA = Registration.ATTACHMENT_TYPES
      .register("simpledialer_data", () -> AttachmentType.builder(SimpleDialerData::createDefault).serialize(SimpleDialerData.CODEC.fieldOf("data")).build());
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<SimpleDialerData>> ITEM_SIMPLEDIALER_DATA = Registration.COMPONENTS
      .registerComponentType("simpledialer_data", builder -> builder.persistent(SimpleDialerData.CODEC).networkSynchronized(SimpleDialerData.STREAM_CODEC));
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<ChargedPorterData>> ITEM_CHARGEDPORTER_DATA = Registration.COMPONENTS
      .registerComponentType("chargedporter_data", builder -> builder.persistent(ChargedPorterData.CODEC).networkSynchronized(ChargedPorterData.STREAM_CODEC));

   public TeleporterModule(IEventBus bus) {
      bus.addListener(this::registerMenuScreens);
   }

   public void init(FMLCommonSetupEvent event) {
   }

   public void registerMenuScreens(RegisterMenuScreensEvent event) {
      GuiDialingDevice.register(event);
      GuiMatterTransmitter.register(event);
      GuiMatterReceiver.register(event);
   }

   public void initClient(FMLClientSetupEvent event) {
      event.enqueueWork(() -> {
         ClientCommandHandler.registerCommands();
         ChargedPorterItem.initOverrides((ChargedPorterItem)CHARGED_PORTER.get());
         ChargedPorterItem.initOverrides((ChargedPorterItem)ADVANCED_CHARGED_PORTER.get());
      });
   }

   public void initConfig(IEventBus bus) {
      TeleportConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
   }

   public void initDatagen(DataGen dataGen, Provider lookup) {
   }
}
