package mcjty.rftoolsutility.setup;

import java.util.function.Supplier;
import mcjty.lib.blocks.RBlockRegistry;
import mcjty.lib.setup.DeferredBlocks;
import mcjty.lib.setup.DeferredItems;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.teleporter.TeleporterModule;
import mcjty.rftoolsutility.playerprops.BuffProperties;
import mcjty.rftoolsutility.playerprops.FavoriteDestinationsProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.DataComponents;
import net.neoforged.neoforge.registries.NeoForgeRegistries.Keys;

public class Registration {
   public static final RBlockRegistry RBLOCKS = new RBlockRegistry("rftoolsutility", RFToolsUtility.setup::addTabItem);
   public static final DeferredBlocks BLOCKS = DeferredBlocks.create("rftoolsutility");
   public static final DeferredItems ITEMS = DeferredItems.create("rftoolsutility");
   public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, "rftoolsutility");
   public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(BuiltInRegistries.MENU, "rftoolsutility");
   public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, "rftoolsutility");
   public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, "rftoolsutility");
   public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, "rftoolsutility");
   public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, "rftoolsutility");
   public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "rftoolsutility");
   public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(Keys.ATTACHMENT_TYPES, "rftoolsutility");
   public static final DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, "rftoolsutility");
   public static final Supplier<AttachmentType<BuffProperties>> ATTACHMENT_TYPE_BUFF_PROPERTIES = ATTACHMENT_TYPES.register(
      "buff_properties", () -> AttachmentType.builder(BuffProperties::new).serialize(BuffProperties.CODEC.fieldOf("data")).copyOnDeath().build()
   );
   public static final Supplier<AttachmentType<FavoriteDestinationsProperties>> ATTACHMENT_TYPE_FAVORITE_DESTINATIONS_PROPERTIES = ATTACHMENT_TYPES.register(
      "favorite_destinations_properties",
      () -> AttachmentType.builder(a -> new FavoriteDestinationsProperties())
         .serialize(FavoriteDestinationsProperties.CODEC.fieldOf("data"))
         .copyOnDeath()
         .build()
   );
   public static Supplier<CreativeModeTab> TAB = TABS.register(
      "rftoolsutility",
      () -> CreativeModeTab.builder()
         .title(Component.translatable("itemGroup.rftoolsutility"))
         .icon(() -> new ItemStack((ItemLike)TeleporterModule.CHARGED_PORTER.get()))
         .withTabsBefore(new ResourceKey[]{CreativeModeTabs.SPAWN_EGGS})
         .displayItems((featureFlags, output) -> RFToolsUtility.setup.populateTab(output))
         .build()
   );

   public static void register(IEventBus bus) {
      RBLOCKS.register(bus);
      BLOCKS.register(bus);
      ITEMS.register(bus);
      TILES.register(bus);
      CONTAINERS.register(bus);
      SOUNDS.register(bus);
      ENTITIES.register(bus);
      RECIPE_SERIALIZERS.register(bus);
      RECIPE_TYPES.register(bus);
      TABS.register(bus);
      ATTACHMENT_TYPES.register(bus);
      COMPONENTS.register(bus);
      ModSounds.init();
   }

   public static Properties createStandardProperties() {
      return RFToolsUtility.setup.defaultProperties();
   }
}
