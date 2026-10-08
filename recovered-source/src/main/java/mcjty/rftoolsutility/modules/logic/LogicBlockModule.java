package mcjty.rftoolsutility.modules.logic;

import java.util.function.Supplier;
import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.items.BaseBlockItem;
import mcjty.lib.modules.IModule;
import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolsbase.modules.tablet.items.TabletItem;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.logic.blocks.AnalogTileEntity;
import mcjty.rftoolsutility.modules.logic.blocks.CounterTileEntity;
import mcjty.rftoolsutility.modules.logic.blocks.DigitTileEntity;
import mcjty.rftoolsutility.modules.logic.blocks.InvCheckerTileEntity;
import mcjty.rftoolsutility.modules.logic.blocks.RedstoneReceiverTileEntity;
import mcjty.rftoolsutility.modules.logic.blocks.RedstoneTransmitterBlock;
import mcjty.rftoolsutility.modules.logic.blocks.RedstoneTransmitterTileEntity;
import mcjty.rftoolsutility.modules.logic.blocks.SensorTileEntity;
import mcjty.rftoolsutility.modules.logic.blocks.SequencerTileEntity;
import mcjty.rftoolsutility.modules.logic.blocks.ThreeLogicTileEntity;
import mcjty.rftoolsutility.modules.logic.blocks.TimerTileEntity;
import mcjty.rftoolsutility.modules.logic.blocks.WireTileEntity;
import mcjty.rftoolsutility.modules.logic.client.GuiAnalog;
import mcjty.rftoolsutility.modules.logic.client.GuiCounter;
import mcjty.rftoolsutility.modules.logic.client.GuiInvChecker;
import mcjty.rftoolsutility.modules.logic.client.GuiRedstoneInformation;
import mcjty.rftoolsutility.modules.logic.client.GuiRedstoneReceiver;
import mcjty.rftoolsutility.modules.logic.client.GuiRedstoneTransmitter;
import mcjty.rftoolsutility.modules.logic.client.GuiSensor;
import mcjty.rftoolsutility.modules.logic.client.GuiSequencer;
import mcjty.rftoolsutility.modules.logic.client.GuiThreeLogic;
import mcjty.rftoolsutility.modules.logic.client.GuiTimer;
import mcjty.rftoolsutility.modules.logic.data.AnalogData;
import mcjty.rftoolsutility.modules.logic.data.CounterData;
import mcjty.rftoolsutility.modules.logic.data.IncCheckerData;
import mcjty.rftoolsutility.modules.logic.data.RedstoneChannelData;
import mcjty.rftoolsutility.modules.logic.data.RedstoneInformationData;
import mcjty.rftoolsutility.modules.logic.data.RedstoneReceiverData;
import mcjty.rftoolsutility.modules.logic.data.SensorData;
import mcjty.rftoolsutility.modules.logic.data.SequencerData;
import mcjty.rftoolsutility.modules.logic.data.ThreeLogicData;
import mcjty.rftoolsutility.modules.logic.data.TimerData;
import mcjty.rftoolsutility.modules.logic.items.RedstoneInformationContainer;
import mcjty.rftoolsutility.modules.logic.items.RedstoneInformationItem;
import mcjty.rftoolsutility.modules.screen.client.GuiTabletScreen;
import mcjty.rftoolsutility.setup.Registration;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public class LogicBlockModule implements IModule {
   public static final RBlock<LogicSlabBlock, BlockItem, AnalogTileEntity> ANALOG = Registration.RBLOCKS
      .registerBlock(
         "analog",
         AnalogTileEntity.class,
         AnalogTileEntity::createBlock,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         AnalogTileEntity::new
      );
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_ANALOG = Registration.CONTAINERS
      .register("analog", GenericContainer::createContainerType);
   public static final RBlock<LogicSlabBlock, BlockItem, CounterTileEntity> COUNTER = Registration.RBLOCKS
      .registerBlock(
         "counter",
         CounterTileEntity.class,
         CounterTileEntity::createBlock,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         CounterTileEntity::new
      );
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_COUNTER = Registration.CONTAINERS
      .register("counter", GenericContainer::createContainerType);
   public static final RBlock<LogicSlabBlock, BlockItem, DigitTileEntity> DIGIT = Registration.RBLOCKS
      .registerBlock(
         "digit",
         DigitTileEntity.class,
         DigitTileEntity::createBlock,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         DigitTileEntity::new
      );
   public static final RBlock<LogicSlabBlock, BlockItem, InvCheckerTileEntity> INVCHECKER = Registration.RBLOCKS
      .registerBlock(
         "invchecker",
         InvCheckerTileEntity.class,
         InvCheckerTileEntity::createBlock,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         InvCheckerTileEntity::new
      );
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_INVCHECKER = Registration.CONTAINERS
      .register("invchecker", GenericContainer::createContainerType);
   public static final RBlock<LogicSlabBlock, BlockItem, SensorTileEntity> SENSOR = Registration.RBLOCKS
      .registerBlock(
         "sensor",
         SensorTileEntity.class,
         SensorTileEntity::createBlock,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         SensorTileEntity::new
      );
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_SENSOR = Registration.CONTAINERS
      .register("sensor", GenericContainer::createContainerType);
   public static final RBlock<LogicSlabBlock, BlockItem, SequencerTileEntity> SEQUENCER = Registration.RBLOCKS
      .registerBlock(
         "sequencer",
         SequencerTileEntity.class,
         SequencerTileEntity::createBlock,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         SequencerTileEntity::new
      );
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_SEQUENCER = Registration.CONTAINERS
      .register("sequencer", GenericContainer::createContainerType);
   public static final RBlock<LogicSlabBlock, BlockItem, ThreeLogicTileEntity> LOGIC = Registration.RBLOCKS
      .registerBlock(
         "logic",
         ThreeLogicTileEntity.class,
         ThreeLogicTileEntity::createBlock,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         ThreeLogicTileEntity::new
      );
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_LOGIC = Registration.CONTAINERS.register("logic", GenericContainer::createContainerType);
   public static final RBlock<LogicSlabBlock, BlockItem, TimerTileEntity> TIMER = Registration.RBLOCKS
      .registerBlock(
         "timer",
         TimerTileEntity.class,
         TimerTileEntity::createBlock,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         TimerTileEntity::new
      );
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_TIMER = Registration.CONTAINERS.register("timer", GenericContainer::createContainerType);
   public static final RBlock<LogicSlabBlock, BlockItem, WireTileEntity> WIRE = Registration.RBLOCKS
      .registerBlock(
         "wire",
         WireTileEntity.class,
         WireTileEntity::createBlock,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         WireTileEntity::new
      );
   public static final RBlock<LogicSlabBlock, BlockItem, RedstoneReceiverTileEntity> REDSTONE_RECEIVER = Registration.RBLOCKS
      .registerBlock(
         "redstone_receiver",
         RedstoneReceiverTileEntity.class,
         RedstoneReceiverTileEntity::createBlock,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         RedstoneReceiverTileEntity::new
      );
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_REDSTONE_RECEIVER = Registration.CONTAINERS
      .register("redstone_receiver", GenericContainer::createContainerType);
   public static final RBlock<LogicSlabBlock, BlockItem, RedstoneTransmitterTileEntity> REDSTONE_TRANSMITTER = Registration.RBLOCKS
      .registerBlock(
         "redstone_transmitter",
         RedstoneTransmitterTileEntity.class,
         RedstoneTransmitterBlock::new,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         RedstoneTransmitterTileEntity::new
      );
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_REDSTONE_TRANSMITTER = Registration.CONTAINERS
      .register("redstone_transmitter", GenericContainer::createContainerType);
   public static final DeferredItem<RedstoneInformationItem> REDSTONE_INFORMATION = Registration.ITEMS
      .register("redstone_information", RFToolsUtility.tab(RedstoneInformationItem::new));
   public static final Supplier<MenuType<RedstoneInformationContainer>> CONTAINER_REDSTONE_INFORMATION = Registration.CONTAINERS
      .register(
         "redstone_information",
         () -> IMenuTypeExtension.create((windowId, inv, data) -> new RedstoneInformationContainer(windowId, null, SafeClientTools.getClientPlayer()))
      );
   public static final DeferredItem<TabletItem> TABLET_REDSTONE = Registration.ITEMS.register("tablet_redstone", RFToolsUtility.tab(TabletItem::new));
   public static final Supplier<AttachmentType<AnalogData>> ANALOG_DATA = Registration.ATTACHMENT_TYPES
      .register("analog_data", () -> AttachmentType.builder(AnalogData::createDefault).serialize(AnalogData.CODEC.fieldOf("data")).build());
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<AnalogData>> ITEM_ANALOG_DATA = Registration.COMPONENTS
      .registerComponentType("analog_data", builder -> builder.persistent(AnalogData.CODEC).networkSynchronized(AnalogData.STREAM_CODEC));
   public static final Supplier<AttachmentType<CounterData>> COUNTER_DATA = Registration.ATTACHMENT_TYPES
      .register("counter_data", () -> AttachmentType.builder(CounterData::createDefault).serialize(CounterData.CODEC.fieldOf("data")).build());
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<CounterData>> ITEM_COUNTER_DATA = Registration.COMPONENTS
      .registerComponentType("counter_data", builder -> builder.persistent(CounterData.CODEC).networkSynchronized(CounterData.STREAM_CODEC));
   public static final Supplier<AttachmentType<IncCheckerData>> INVCHECKER_DATA = Registration.ATTACHMENT_TYPES
      .register("invchecker_data", () -> AttachmentType.builder(IncCheckerData::createDefault).serialize(IncCheckerData.CODEC.fieldOf("data")).build());
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<IncCheckerData>> ITEM_INVCHECKER_DATA = Registration.COMPONENTS
      .registerComponentType("invchecker_data", builder -> builder.persistent(IncCheckerData.CODEC).networkSynchronized(IncCheckerData.STREAM_CODEC));
   public static final Supplier<AttachmentType<RedstoneChannelData>> REDSTONECHANNEL_DATA = Registration.ATTACHMENT_TYPES
      .register(
         "redstonechannel_data", () -> AttachmentType.builder(RedstoneChannelData::createDefault).serialize(RedstoneChannelData.CODEC.fieldOf("data")).build()
      );
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<RedstoneChannelData>> ITEM_REDSTONECHANNEL_DATA = Registration.COMPONENTS
      .registerComponentType(
         "redstonechannel_data", builder -> builder.persistent(RedstoneChannelData.CODEC).networkSynchronized(RedstoneChannelData.STREAM_CODEC)
      );
   public static final Supplier<AttachmentType<RedstoneReceiverData>> REDSTONERECEIVER_DATA = Registration.ATTACHMENT_TYPES
      .register(
         "redstonereceiver_data",
         () -> AttachmentType.builder(RedstoneReceiverData::createDefault).serialize(RedstoneReceiverData.CODEC.fieldOf("data")).build()
      );
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<RedstoneReceiverData>> ITEM_REDSTONERECEIVER_DATA = Registration.COMPONENTS
      .registerComponentType(
         "redstonereceiver_data", builder -> builder.persistent(RedstoneReceiverData.CODEC).networkSynchronized(RedstoneReceiverData.STREAM_CODEC)
      );
   public static final Supplier<AttachmentType<SensorData>> SENSOR_DATA = Registration.ATTACHMENT_TYPES
      .register("sensor_data", () -> AttachmentType.builder(SensorData::createDefault).serialize(SensorData.CODEC.fieldOf("data")).build());
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<SensorData>> ITEM_SENSOR_DATA = Registration.COMPONENTS
      .registerComponentType("sensor_data", builder -> builder.persistent(SensorData.CODEC).networkSynchronized(SensorData.STREAM_CODEC));
   public static final Supplier<AttachmentType<SequencerData>> SEQUENCER_DATA = Registration.ATTACHMENT_TYPES
      .register("sequencer_data", () -> AttachmentType.builder(SequencerData::createDefault).serialize(SequencerData.CODEC.fieldOf("data")).build());
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<SequencerData>> ITEM_SEQUENCER_DATA = Registration.COMPONENTS
      .registerComponentType("sequencer_data", builder -> builder.persistent(SequencerData.CODEC).networkSynchronized(SequencerData.STREAM_CODEC));
   public static final Supplier<AttachmentType<ThreeLogicData>> THREELOGIC_DATA = Registration.ATTACHMENT_TYPES
      .register("threelogic_data", () -> AttachmentType.builder(ThreeLogicData::createDefault).serialize(ThreeLogicData.CODEC.fieldOf("data")).build());
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<ThreeLogicData>> ITEM_THREELOGIC_DATA = Registration.COMPONENTS
      .registerComponentType("threelogic_data", builder -> builder.persistent(ThreeLogicData.CODEC).networkSynchronized(ThreeLogicData.STREAM_CODEC));
   public static final Supplier<AttachmentType<TimerData>> TIMER_DATA = Registration.ATTACHMENT_TYPES
      .register("timer_data", () -> AttachmentType.builder(TimerData::createDefault).serialize(TimerData.CODEC.fieldOf("data")).build());
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<TimerData>> ITEM_TIMER_DATA = Registration.COMPONENTS
      .registerComponentType("timer_data", builder -> builder.persistent(TimerData.CODEC).networkSynchronized(TimerData.STREAM_CODEC));
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<RedstoneInformationData>> ITEM_REDSTONE_INFORMATION_DATA = Registration.COMPONENTS
      .registerComponentType(
         "redstone_information_data", builder -> builder.persistent(RedstoneInformationData.CODEC).networkSynchronized(RedstoneInformationData.STREAM_CODEC)
      );

   public LogicBlockModule(IEventBus bus) {
      bus.addListener(this::registerMenuScreens);
   }

   public void init(FMLCommonSetupEvent event) {
   }

   public void registerMenuScreens(RegisterMenuScreensEvent event) {
      GuiAnalog.register(event);
      GuiCounter.register(event);
      GuiInvChecker.register(event);
      GuiSensor.register(event);
      GuiSequencer.register(event);
      GuiThreeLogic.register(event);
      GuiTimer.register(event);
      GuiRedstoneReceiver.register(event);
      GuiRedstoneTransmitter.register(event);
      GuiRedstoneInformation.register(event);
      GuiTabletScreen.register(event);
   }

   public void initClient(FMLClientSetupEvent event) {
   }

   public void initConfig(IEventBus bus) {
   }

   public void initDatagen(DataGen dataGen, Provider provider) {
   }
}
