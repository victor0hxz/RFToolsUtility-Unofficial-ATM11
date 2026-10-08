package mcjty.rftoolsutility.modules.screen.items.modules;

import com.mojang.serialization.Codec;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.ComponentFactory;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.ModuleTools;
import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleGuiBuilder;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.TextAlign;
import mcjty.rftoolsbase.tools.GenericModuleItem;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.logic.blocks.RedstoneChannelTileEntity;
import mcjty.rftoolsutility.modules.screen.ScreenConfiguration;
import mcjty.rftoolsutility.modules.screen.ScreenModule;
import mcjty.rftoolsutility.modules.screen.modules.RedstoneScreenModule;
import mcjty.rftoolsutility.modules.screen.modulesclient.RedstoneClientScreenModule;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class RedstoneModuleItem extends GenericModuleItem {
   public RedstoneModuleItem() {
      super(RFToolsUtility.setup.defaultProperties().stacksTo(1).durability(1));
   }

   @Nullable
   public Codec<? extends IScreenModule<?, ?>> codec() {
      return RedstoneScreenModule.CODEC;
   }

   @Nullable
   public StreamCodec<RegistryFriendlyByteBuf, ? extends IScreenModule<?, ?>> streamCodec() {
      return RedstoneScreenModule.STREAM_CODEC;
   }

   @Nullable
   public DataComponentType<? extends IScreenModule<?, ?>> componentType() {
      return (DataComponentType<? extends IScreenModule<?, ?>>)ScreenModule.MODULE_REDSTONE_DATA.get();
   }

   public IScreenModule<?, ?> createServerScreenModule() {
      return RedstoneScreenModule.DEFAULT;
   }

   public IClientScreenModule<?> createClientScreenModule() {
      return new RedstoneClientScreenModule();
   }

   protected int getUses(ItemStack stack) {
      return (Integer)ScreenConfiguration.REDSTONE_RFPERTICK.get();
   }

   protected boolean hasGoldMessage(ItemStack stack) {
      return !BlockPosTools.isValid(data(stack).getPos().pos());
   }

   protected String getInfoString(ItemStack stack) {
      RedstoneScreenModule data = data(stack);
      return ModuleTools.getTargetString(data.getMonitor(), data.getPos());
   }

   public void appendHoverText(
      @Nonnull ItemStack itemStack, @Nullable TooltipContext context, TooltipDisplay display, @Nonnull Consumer<Component> list, @Nonnull TooltipFlag flag
   ) {
      super.appendHoverText(itemStack, context, display, list, flag);
      RedstoneScreenModule data = data(itemStack);
      int channel = data.getChannel();
      list.accept(ComponentFactory.literal(ChatFormatting.YELLOW + "Channel: " + channel));
   }

   public String getModuleName() {
      return "Red";
   }

   public static RedstoneScreenModule data(ItemStack stack) {
      RedstoneScreenModule data = (RedstoneScreenModule)stack.get(ScreenModule.MODULE_REDSTONE_DATA);
      if (data == null) {
         data = RedstoneScreenModule.DEFAULT;
      }

      return data;
   }

   public static void data(ItemStack stack, Function<RedstoneScreenModule, RedstoneScreenModule> setter) {
      RedstoneScreenModule data = data(stack);
      data = setter.apply(data);
      stack.set(ScreenModule.MODULE_REDSTONE_DATA, data);
   }

   public void createGui(IModuleGuiBuilder guiBuilder) {
      guiBuilder.label("Label:")
         .text((stack, s) -> data(stack, d -> d.withLine(s)), stack -> data(stack).getLine(), new String[]{"Label text"})
         .color((stack, c) -> data(stack, d -> d.withColor(c)), stack -> data(stack).getColor(), new String[]{"Color for the label"})
         .nl()
         .label("Yes:")
         .text((stack, s) -> data(stack, d -> d.withYestext(s)), stack -> data(stack).getYestext(), new String[]{"Positive text"})
         .color((stack, c) -> data(stack, d -> d.withYescolor(c)), stack -> data(stack).getYescolor(), new String[]{"Color for the positive text"})
         .nl()
         .label("No:")
         .text((stack, s) -> data(stack, d -> d.withNotext(s)), stack -> data(stack).getNotext(), new String[]{"Negative text"})
         .color((stack, c) -> data(stack, d -> d.withNocolor(c)), stack -> data(stack).getNocolor(), new String[]{"Color for the negative text"})
         .nl()
         .choices(
            (stack, c) -> data(stack, d -> d.withAlign(TextAlign.get(c))),
            stack -> data(stack).getAlign().getSerializedName(),
            "Label alignment",
            new String[]{"Left", "Center", "Right"}
         )
         .toggle(
            (stack, b) -> data(stack, d -> d.withAnalog(b)), stack -> data(stack).isAnalog(), "Analog mode", new String[]{"Whether to show the exact level"}
         )
         .nl()
         .label("Block:")
         .block(stack -> data(stack).getPos(), stack -> data(stack).getMonitor())
         .nl();
   }

   @Nonnull
   public InteractionResult useOn(UseOnContext context) {
      Level world = context.getLevel();
      if (world.isClientSide()) {
         return InteractionResult.SUCCESS;
      } else {
         ItemStack stack = context.getItemInHand();
         BlockPos pos = context.getClickedPos();
         BlockEntity te = world.getBlockEntity(pos);
         Player player = context.getPlayer();
         Direction facing = context.getClickedFace();
         RedstoneScreenModule data = data(stack);
         int channel = -1;
         if (te instanceof RedstoneChannelTileEntity) {
            channel = ((RedstoneChannelTileEntity)te).getChannel(true);
            ModuleTools.clearPositionInModule(stack);
            if (channel != -1) {
               data = data.withChannel(channel);
               Logging.message(player, "Redstone module is set to channel '" + channel + "'");
            } else {
               data = data.withChannel(-1);
               Logging.message(player, "Redstone module is cleared");
            }

            stack.set(ScreenModule.MODULE_REDSTONE_DATA, data);
            return InteractionResult.SUCCESS;
         } else {
            data = data.withChannel(-1);
            data = data.withPos(GlobalPos.of(world.dimension(), pos));
            data = data.withSide(facing);
            Logging.message(player, "Redstone module is set to " + pos);
            stack.set(ScreenModule.MODULE_REDSTONE_DATA, data);
            return InteractionResult.SUCCESS;
         }
      }
   }
}
