package mcjty.rftoolsutility.modules.screen.items.modules;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleGuiBuilder;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.tools.GenericModuleItem;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.screen.ScreenConfiguration;
import mcjty.rftoolsutility.modules.screen.ScreenModule;
import mcjty.rftoolsutility.modules.screen.modules.ClockScreenModule;
import mcjty.rftoolsutility.modules.screen.modulesclient.ClockClientScreenModule;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public class ClockModuleItem extends GenericModuleItem {
   public ClockModuleItem() {
      super(RFToolsUtility.setup.defaultProperties().stacksTo(16).durability(1));
   }

   public Codec<? extends IScreenModule<?, ?>> codec() {
      return ClockScreenModule.CODEC;
   }

   public StreamCodec<RegistryFriendlyByteBuf, ? extends IScreenModule<?, ?>> streamCodec() {
      return ClockScreenModule.STREAM_CODEC;
   }

   public DataComponentType<? extends IScreenModule<?, ?>> componentType() {
      return (DataComponentType<? extends IScreenModule<?, ?>>)ScreenModule.MODULE_CLOCK_DATA.get();
   }

   public IScreenModule<?, ?> createServerScreenModule() {
      return ClockScreenModule.DEFAULT;
   }

   public IClientScreenModule<?> createClientScreenModule() {
      return new ClockClientScreenModule();
   }

   protected int getUses(ItemStack stack) {
      return (Integer)ScreenConfiguration.CLOCK_RFPERTICK.get();
   }

   public String getModuleName() {
      return "Clock";
   }

   public static ClockScreenModule data(ItemStack stack) {
      ClockScreenModule data = (ClockScreenModule)stack.get(ScreenModule.MODULE_CLOCK_DATA);
      if (data == null) {
         data = ClockScreenModule.DEFAULT;
      }

      return data;
   }

   public static void data(ItemStack stack, Function<ClockScreenModule, ClockScreenModule> setter) {
      ClockScreenModule data = data(stack);
      data = setter.apply(data);
      stack.set(ScreenModule.MODULE_CLOCK_DATA, data);
   }

   public void createGui(IModuleGuiBuilder guiBuilder) {
      guiBuilder.label("Label:")
         .text((stack, s) -> data(stack, d -> d.withLine(s)), stack -> data(stack).getLine(), new String[]{"Label text"})
         .color((stack, c) -> data(stack, d -> d.withColor(c)), stack -> data(stack).getColor(), new String[]{"Label color"})
         .nl()
         .toggle((stack, b) -> data(stack, d -> d.withLarge(b)), stack -> data(stack).isLarge(), "Large", new String[]{"Large or small font"})
         .nl();
   }
}
