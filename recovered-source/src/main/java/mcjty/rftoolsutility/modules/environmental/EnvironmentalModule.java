package mcjty.rftoolsutility.modules.environmental;

import java.util.function.Supplier;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.items.BaseBlockItem;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.environmental.blocks.EnvironmentalControllerTileEntity;
import mcjty.rftoolsutility.modules.environmental.client.ClientSetup;
import mcjty.rftoolsutility.modules.environmental.client.GuiEnvironmentalController;
import mcjty.rftoolsutility.modules.environmental.data.EnvironmentalData;
import mcjty.rftoolsutility.modules.environmental.items.EnvironmentalControllerItem;
import mcjty.rftoolsutility.modules.environmental.recipes.SyringeBasedRecipe;
import mcjty.rftoolsutility.modules.environmental.recipes.SyringeRecipeSerializer;
import mcjty.rftoolsutility.modules.environmental.recipes.SyringeRecipeType;
import mcjty.rftoolsutility.setup.Config;
import mcjty.rftoolsutility.setup.Registration;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public class EnvironmentalModule implements IModule {
   public static final RBlock<BaseBlock, BlockItem, EnvironmentalControllerTileEntity> ENVIRONENTAL_CONTROLLER = Registration.RBLOCKS
      .registerBlock(
         "environmental_controller",
         EnvironmentalControllerTileEntity.class,
         EnvironmentalControllerTileEntity::createBlock,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         EnvironmentalControllerTileEntity::new
      );
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_ENVIRONENTAL_CONTROLLER = Registration.CONTAINERS
      .register("environmental_controller", GenericContainer::createContainerType);
   public static final DeferredItem<Item> MODULE_TEMPLATE = Registration.ITEMS
      .register("module_template", RFToolsUtility.tab(() -> new Item(Registration.createStandardProperties())));
   public static final DeferredItem<Item> MODULEPLUS_TEMPLATE = Registration.ITEMS
      .register("moduleplus_template", RFToolsUtility.tab(() -> new Item(Registration.createStandardProperties())));
   public static final DeferredItem<EnvironmentalControllerItem> BLINDNESS_MODULE = Registration.ITEMS
      .register("blindness_module", RFToolsUtility.tab(EnvironmentalControllerItem::createBlindnessModule));
   public static final DeferredItem<EnvironmentalControllerItem> FEATHERFALLING_MODULE = Registration.ITEMS
      .register("featherfalling_module", RFToolsUtility.tab(EnvironmentalControllerItem::createFeatherfallingModule));
   public static final DeferredItem<EnvironmentalControllerItem> FEATHERFALLINGPLUS_MODULE = Registration.ITEMS
      .register("featherfallingplus_module", RFToolsUtility.tab(EnvironmentalControllerItem::createFeatherfallingPlusModule));
   public static final DeferredItem<EnvironmentalControllerItem> HASTE_MODULE = Registration.ITEMS
      .register("haste_module", RFToolsUtility.tab(EnvironmentalControllerItem::createHasteModule));
   public static final DeferredItem<EnvironmentalControllerItem> HASTEPLUS_MODULE = Registration.ITEMS
      .register("hasteplus_module", RFToolsUtility.tab(EnvironmentalControllerItem::createHastePlusModule));
   public static final DeferredItem<EnvironmentalControllerItem> FLIGHT_MODULE = Registration.ITEMS
      .register("flight_module", RFToolsUtility.tab(EnvironmentalControllerItem::createFlightModule));
   public static final DeferredItem<EnvironmentalControllerItem> GLOWING_MODULE = Registration.ITEMS
      .register("glowing_module", RFToolsUtility.tab(EnvironmentalControllerItem::createGlowingModule));
   public static final DeferredItem<EnvironmentalControllerItem> LUCK_MODULE = Registration.ITEMS
      .register("luck_module", RFToolsUtility.tab(EnvironmentalControllerItem::createLuckModule));
   public static final DeferredItem<EnvironmentalControllerItem> NIGHTVISION_MODULE = Registration.ITEMS
      .register("nightvision_module", RFToolsUtility.tab(EnvironmentalControllerItem::createNightvisionModule));
   public static final DeferredItem<EnvironmentalControllerItem> NOTELEPORT_MODULE = Registration.ITEMS
      .register("noteleport_module", RFToolsUtility.tab(EnvironmentalControllerItem::createNoteleportModule));
   public static final DeferredItem<EnvironmentalControllerItem> PEACEFUL_MODULE = Registration.ITEMS
      .register("peaceful_module", RFToolsUtility.tab(EnvironmentalControllerItem::createPeacefulModule));
   public static final DeferredItem<EnvironmentalControllerItem> POISON_MODULE = Registration.ITEMS
      .register("poison_module", RFToolsUtility.tab(EnvironmentalControllerItem::createPoisonModule));
   public static final DeferredItem<EnvironmentalControllerItem> REGENERATION_MODULE = Registration.ITEMS
      .register("regeneration_module", RFToolsUtility.tab(EnvironmentalControllerItem::createRegenerationModule));
   public static final DeferredItem<EnvironmentalControllerItem> REGENERATIONPLUS_MODULE = Registration.ITEMS
      .register("regenerationplus_module", RFToolsUtility.tab(EnvironmentalControllerItem::createRegenerationPlusModule));
   public static final DeferredItem<EnvironmentalControllerItem> SATURATION_MODULE = Registration.ITEMS
      .register("saturation_module", RFToolsUtility.tab(EnvironmentalControllerItem::createSaturationModule));
   public static final DeferredItem<EnvironmentalControllerItem> SATURATIONPLUS_MODULE = Registration.ITEMS
      .register("saturationplus_module", RFToolsUtility.tab(EnvironmentalControllerItem::createSaturationPlusModule));
   public static final DeferredItem<EnvironmentalControllerItem> SLOWNESS_MODULE = Registration.ITEMS
      .register("slowness_module", RFToolsUtility.tab(EnvironmentalControllerItem::createSlownessModule));
   public static final DeferredItem<EnvironmentalControllerItem> SPEED_MODULE = Registration.ITEMS
      .register("speed_module", RFToolsUtility.tab(EnvironmentalControllerItem::createSpeedModule));
   public static final DeferredItem<EnvironmentalControllerItem> SPEEDPLUS_MODULE = Registration.ITEMS
      .register("speedplus_module", RFToolsUtility.tab(EnvironmentalControllerItem::createSpeedPlusModule));
   public static final DeferredItem<EnvironmentalControllerItem> WATERBREATHING_MODULE = Registration.ITEMS
      .register("waterbreathing_module", RFToolsUtility.tab(EnvironmentalControllerItem::createWaterbreathingModule));
   public static final DeferredItem<EnvironmentalControllerItem> WEAKNESS_MODULE = Registration.ITEMS
      .register("weakness_module", RFToolsUtility.tab(EnvironmentalControllerItem::createWeaknessModule));
   public static final Supplier<AttachmentType<EnvironmentalData>> ENVIRONMENTAL_DATA = Registration.ATTACHMENT_TYPES
      .register("environmental_data", () -> AttachmentType.builder(EnvironmentalData::createDefault).serialize(EnvironmentalData.CODEC.fieldOf("data")).build());
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<EnvironmentalData>> ITEM_ENVIRONMENTAL_DATA = Registration.COMPONENTS
      .registerComponentType("environmental_data", builder -> builder.persistent(EnvironmentalData.CODEC).networkSynchronized(EnvironmentalData.STREAM_CODEC));
   public static final Supplier<RecipeSerializer<SyringeBasedRecipe>> SYRINGE_SERIALIZER = Registration.RECIPE_SERIALIZERS
      .register("syringe", () -> SyringeRecipeSerializer.SERIALIZER);
   public static final Identifier SYRINGE_RECIPE_TYPE_ID = Identifier.fromNamespaceAndPath("rftoolsutility", "syringe");
   public static final Supplier<SyringeRecipeType> SYRINGE_RECIPE_TYPE = Registration.RECIPE_TYPES.register("syringe", SyringeRecipeType::new);

   public EnvironmentalModule(IEventBus bus, Dist dist) {
      bus.addListener(this::registerMenuScreens);
   }

   public void init(FMLCommonSetupEvent event) {
   }

   public void registerMenuScreens(RegisterMenuScreensEvent event) {
      GuiEnvironmentalController.register(event);
   }

   public void initClient(FMLClientSetupEvent event) {
      ClientSetup.initClient();
   }

   public void initConfig(IEventBus bus) {
      EnvironmentalConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
   }

   public void initDatagen(DataGen dataGen, Provider provider) {
   }
}
