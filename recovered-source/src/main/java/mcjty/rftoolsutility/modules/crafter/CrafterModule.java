package mcjty.rftoolsutility.modules.crafter;

import java.util.function.Supplier;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.items.BaseBlockItem;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsutility.modules.crafter.blocks.CrafterBaseTE;
import mcjty.rftoolsutility.modules.crafter.blocks.CrafterBlock;
import mcjty.rftoolsutility.modules.crafter.blocks.CrafterContainer;
import mcjty.rftoolsutility.modules.crafter.client.GuiCrafter;
import mcjty.rftoolsutility.modules.crafter.data.CrafterData;
import mcjty.rftoolsutility.setup.Config;
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
import net.neoforged.neoforge.registries.DeferredHolder;

public class CrafterModule implements IModule {
   public static final RBlock<BaseBlock, BlockItem, CrafterBaseTE> CRAFTER1 = Registration.RBLOCKS
      .registerBlock(
         "crafter1",
         CrafterBaseTE.class,
         () -> new CrafterBlock(CrafterBaseTE::createTier1),
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         CrafterBaseTE::createTier1
      );
   public static final RBlock<BaseBlock, BlockItem, CrafterBaseTE> CRAFTER2 = Registration.RBLOCKS
      .registerBlock(
         "crafter2",
         CrafterBaseTE.class,
         () -> new CrafterBlock(CrafterBaseTE::createTier2),
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         CrafterBaseTE::createTier2
      );
   public static final RBlock<BaseBlock, BlockItem, CrafterBaseTE> CRAFTER3 = Registration.RBLOCKS
      .registerBlock(
         "crafter3",
         CrafterBaseTE.class,
         () -> new CrafterBlock(CrafterBaseTE::createTier3),
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         CrafterBaseTE::createTier3
      );
   public static final Supplier<MenuType<CrafterContainer>> CONTAINER_CRAFTER = Registration.CONTAINERS
      .register("crafter", GenericContainer::createContainerType);
   public static final Supplier<AttachmentType<CrafterData>> CRAFTER_DATA = Registration.ATTACHMENT_TYPES
      .register("crafter_data", () -> AttachmentType.builder(CrafterData::createDefault).serialize(CrafterData.CODEC.fieldOf("data")).build());
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<CrafterData>> ITEM_CRAFTER_DATA = Registration.COMPONENTS
      .registerComponentType("crafter_data", builder -> builder.persistent(CrafterData.CODEC).networkSynchronized(CrafterData.STREAM_CODEC));

   public CrafterModule(IEventBus bus) {
      bus.addListener(this::registerMenuScreens);
   }

   public void init(FMLCommonSetupEvent event) {
   }

   public void registerMenuScreens(RegisterMenuScreensEvent event) {
      GuiCrafter.register(event);
   }

   public void initClient(FMLClientSetupEvent event) {
   }

   public void initConfig(IEventBus bus) {
      CrafterConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
   }

   public void initDatagen(DataGen dataGen, Provider provider) {
   }
}
