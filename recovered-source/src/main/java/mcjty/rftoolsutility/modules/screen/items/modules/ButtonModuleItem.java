package mcjty.rftoolsutility.modules.screen.items.modules;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleGuiBuilder;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.TextAlign;
import mcjty.rftoolsbase.tools.GenericModuleItem;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.screen.ScreenConfiguration;
import mcjty.rftoolsutility.modules.screen.ScreenModule;
import mcjty.rftoolsutility.modules.screen.modules.ButtonScreenModule;
import mcjty.rftoolsutility.modules.screen.modulesclient.ButtonClientScreenModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;

public class ButtonModuleItem extends GenericModuleItem {
   public Codec<? extends IScreenModule<?, ?>> codec() {
      return ButtonScreenModule.CODEC;
   }

   public StreamCodec<RegistryFriendlyByteBuf, ? extends IScreenModule<?, ?>> streamCodec() {
      return ButtonScreenModule.STREAM_CODEC;
   }

   public DataComponentType<? extends IScreenModule<?, ?>> componentType() {
      return (DataComponentType<? extends IScreenModule<?, ?>>)ScreenModule.MODULE_BUTTON_DATA.get();
   }

   public IScreenModule<?, ?> createServerScreenModule() {
      return ButtonScreenModule.DEFAULT;
   }

   public IClientScreenModule<?> createClientScreenModule() {
      return new ButtonClientScreenModule();
   }

   protected int getUses(ItemStack stack) {
      return (Integer)ScreenConfiguration.BUTTON_RFPERTICK.get();
   }

   protected boolean hasGoldMessage(ItemStack stack) {
      return getChannel(stack) == -1;
   }

   protected String getInfoString(ItemStack stack) {
      int channel = getChannel(stack);
      return channel != -1 ? Integer.toString(channel) : "<unset>";
   }

   public ButtonModuleItem() {
      super(RFToolsUtility.setup.defaultProperties().durability(1));
   }

   public static int getChannel(ItemStack stack) {
      return data(stack).getChannel();
   }

   public String getModuleName() {
      return "Button";
   }

   public static ButtonScreenModule data(ItemStack stack) {
      ButtonScreenModule data = (ButtonScreenModule)stack.get(ScreenModule.MODULE_BUTTON_DATA);
      if (data == null) {
         data = ButtonScreenModule.DEFAULT;
      }

      return data;
   }

   public static void data(ItemStack stack, Function<ButtonScreenModule, ButtonScreenModule> setter) {
      ButtonScreenModule data = data(stack);
      data = setter.apply(data);
      stack.set(ScreenModule.MODULE_BUTTON_DATA, data);
   }

   public void createGui(IModuleGuiBuilder guiBuilder) {
      guiBuilder.label("Label:")
         .text((stack, s) -> data(stack, d -> d.withLine(s)), stack -> data(stack).getLine(), new String[]{"Label text"})
         .color((stack, c) -> data(stack, d -> d.withColor(c)), stack -> data(stack).getColor(), new String[]{"Label color"})
         .nl()
         .label("Button:")
         .text((stack, s) -> data(stack, d -> d.withButton(s)), stack -> data(stack).getButton(), new String[]{"Button text"})
         .color((stack, c) -> data(stack, d -> d.withButtonColor(c)), stack -> data(stack).getButtonColor(), new String[]{"Button color"})
         .nl()
         .toggle((stack, b) -> data(stack, d -> d.withToggle(b)), stack -> data(stack).isToggle(), "Toggle", new String[]{"Toggle button mode"})
         .choices(
            (stack, s) -> data(stack, d -> d.withAlign(TextAlign.get(s))),
            stack -> data(stack).getAlign().getSerializedName(),
            "Label alignment",
            new String[]{"Left", "Center", "Right"}
         )
         .nl();
   }

   public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
      return true;
   }
}
