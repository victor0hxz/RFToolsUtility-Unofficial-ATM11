package mcjty.rftoolsutility.modules.spawner;

import java.util.function.Supplier;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.items.BaseBlockItem;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.spawner.blocks.MatterBeamerBlock;
import mcjty.rftoolsutility.modules.spawner.blocks.MatterBeamerTileEntity;
import mcjty.rftoolsutility.modules.spawner.blocks.SpawnerTileEntity;
import mcjty.rftoolsutility.modules.spawner.client.GuiMatterBeamer;
import mcjty.rftoolsutility.modules.spawner.client.GuiSpawner;
import mcjty.rftoolsutility.modules.spawner.data.SpawnerData;
import mcjty.rftoolsutility.modules.spawner.data.SyringeData;
import mcjty.rftoolsutility.modules.spawner.items.SyringeItem;
import mcjty.rftoolsutility.modules.spawner.recipes.SpawnerRecipe;
import mcjty.rftoolsutility.modules.spawner.recipes.SpawnerRecipeSerializer;
import mcjty.rftoolsutility.modules.spawner.recipes.SpawnerRecipeType;
import mcjty.rftoolsutility.setup.Config;
import mcjty.rftoolsutility.setup.Registration;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public class SpawnerModule implements IModule {
   public static final RBlock<BaseBlock, BlockItem, MatterBeamerTileEntity> MATTER_BEAMER = Registration.RBLOCKS
      .registerBlock(
         "matter_beamer",
         MatterBeamerTileEntity.class,
         MatterBeamerBlock::new,
         block -> new BaseBlockItem((Block)block.get(), mcjty.rftoolsbase.setup.Registration.createStandardProperties()),
         MatterBeamerTileEntity::new
      );
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_MATTER_BEAMER = Registration.CONTAINERS
      .register("matter_beamer", GenericContainer::createContainerType);
   public static final RBlock<BaseBlock, BlockItem, SpawnerTileEntity> SPAWNER = Registration.RBLOCKS
      .registerBlock(
         "spawner",
         SpawnerTileEntity.class,
         SpawnerTileEntity::createBlock,
         block -> new BaseBlockItem((Block)block.get(), mcjty.rftoolsbase.setup.Registration.createStandardProperties()),
         SpawnerTileEntity::new
      );
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_SPAWNER = Registration.CONTAINERS
      .register("spawner", GenericContainer::createContainerType);
   public static final DeferredItem<SyringeItem> SYRINGE = Registration.ITEMS.register("syringe", RFToolsUtility.tab(SyringeItem::new));
   public static final Supplier<RecipeSerializer<SpawnerRecipe>> SPAWNER_SERIALIZER = Registration.RECIPE_SERIALIZERS
      .register("spawner", () -> SpawnerRecipeSerializer.SERIALIZER);
   public static final Identifier SPAWNER_RECIPE_TYPE_ID = Identifier.fromNamespaceAndPath("rftoolsutility", "spawner");
   public static final Supplier<SpawnerRecipeType> SPAWNER_RECIPE_TYPE = Registration.RECIPE_TYPES.register("spawner", SpawnerRecipeType::new);
   public static final Supplier<AttachmentType<SpawnerData>> SPAWNER_DATA = Registration.ATTACHMENT_TYPES
      .register("spawner_data", () -> AttachmentType.builder(SpawnerData::createDefault).serialize(SpawnerData.CODEC.fieldOf("data")).build());
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<SpawnerData>> ITEM_SPAWNER_DATA = Registration.COMPONENTS
      .registerComponentType("spawner_data", builder -> builder.persistent(SpawnerData.CODEC).networkSynchronized(SpawnerData.STREAM_CODEC));
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<SyringeData>> ITEM_SYRINGE_DATA = Registration.COMPONENTS
      .registerComponentType("syringe_data", builder -> builder.persistent(SyringeData.CODEC).networkSynchronized(SyringeData.STREAM_CODEC));

   public SpawnerModule(IEventBus bus) {
      bus.addListener(this::registerMenuScreens);
   }

   public void registerMenuScreens(RegisterMenuScreensEvent event) {
      GuiMatterBeamer.register(event);
      GuiSpawner.register(event);
   }

   public void init(FMLCommonSetupEvent event) {
   }

   public void initClient(FMLClientSetupEvent event) {
      event.enqueueWork(() -> SyringeItem.initOverrides((SyringeItem)SYRINGE.get()));
   }

   public void initConfig(IEventBus bus) {
      SpawnerConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
   }

   public void initDatagen(DataGen dataGen, Provider provider) {
   }
}
