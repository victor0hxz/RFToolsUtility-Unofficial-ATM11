package mcjty.rftoolsutility.modules.screen.items.modules;

import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import mcjty.lib.crafting.IComponentsToPreserve;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.ModuleTools;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleGuiBuilder;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.TextAlign;
import mcjty.rftoolsbase.tools.GenericModuleItem;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.logic.blocks.CounterTileEntity;
import mcjty.rftoolsutility.modules.screen.ScreenConfiguration;
import mcjty.rftoolsutility.modules.screen.ScreenModule;
import mcjty.rftoolsutility.modules.screen.modules.CounterScreenModule;
import mcjty.rftoolsutility.modules.screen.modulesclient.CounterClientScreenModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CounterModuleItem extends GenericModuleItem implements IComponentsToPreserve {
   public CounterModuleItem() {
      super(RFToolsUtility.setup.defaultProperties().stacksTo(1).durability(1));
   }

   @Nullable
   public Codec<? extends IScreenModule<?, ?>> codec() {
      return CounterScreenModule.CODEC;
   }

   @Nullable
   public StreamCodec<RegistryFriendlyByteBuf, ? extends IScreenModule<?, ?>> streamCodec() {
      return CounterScreenModule.STREAM_CODEC;
   }

   @Nullable
   public DataComponentType<? extends IScreenModule<?, ?>> componentType() {
      return (DataComponentType<? extends IScreenModule<?, ?>>)ScreenModule.MODULE_COUNTER_DATA.get();
   }

   public IScreenModule<?, ?> createServerScreenModule() {
      return CounterScreenModule.DEFAULT;
   }

   public IClientScreenModule<?> createClientScreenModule() {
      return new CounterClientScreenModule();
   }

   protected int getUses(ItemStack stack) {
      return (Integer)ScreenConfiguration.COUNTER_RFPERTICK.get();
   }

   protected boolean hasGoldMessage(ItemStack stack) {
      return !BlockPosTools.isValid(data(stack).getPos().pos());
   }

   protected String getInfoString(ItemStack stack) {
      CounterScreenModule data = data(stack);
      return ModuleTools.getTargetString(data.getMonitor(), data.getPos());
   }

   public String getModuleName() {
      return "Count";
   }

   public static CounterScreenModule data(ItemStack stack) {
      CounterScreenModule data = (CounterScreenModule)stack.get(ScreenModule.MODULE_COUNTER_DATA);
      if (data == null) {
         data = CounterScreenModule.DEFAULT;
      }

      return data;
   }

   public static void data(ItemStack stack, Consumer<CounterScreenModule> setter) {
      CounterScreenModule data = data(stack);
      setter.accept(data);
      stack.set(ScreenModule.MODULE_COUNTER_DATA, data);
   }

   public void createGui(IModuleGuiBuilder guiBuilder) {
      guiBuilder.label("Label:")
         .text((stack, s) -> data(stack, d -> d.withLine(s)), stack -> data(stack).getLine(), new String[]{"Label text"})
         .nl()
         .label("L:")
         .color((stack, c) -> data(stack, d -> d.withColor(c)), stack -> data(stack).getColor(), new String[]{"Color for the label"})
         .label("C:")
         .color((stack, c) -> data(stack, d -> d.withCntcolor(c)), stack -> data(stack).getCntcolor(), new String[]{"Color for the counter"})
         .nl()
         .format((stack, f) -> data(stack, d -> d.withFormat(f)), stack -> data(stack).getFormat())
         .choices(
            (stack, c) -> data(stack, d -> d.withAlign(TextAlign.get(c))),
            stack -> data(stack).getAlign().getSerializedName(),
            "Label alignment",
            new String[]{"Left", "Center", "Right"}
         )
         .nl()
         .label("Block:")
         .block(stack -> data(stack).getPos(), stack -> data(stack).getMonitor())
         .nl();
   }

   @Nonnull
   public InteractionResult useOn(UseOnContext context) {
      ItemStack stack = context.getItemInHand();
      Level world = context.getLevel();
      BlockPos pos = context.getClickedPos();
      Direction facing = context.getClickedFace();
      Player player = context.getPlayer();
      BlockEntity te = world.getBlockEntity(pos);
      CounterScreenModule data = data(stack);
      if (te instanceof CounterTileEntity) {
         data = data.withPos(GlobalPos.of(world.dimension(), pos));
         BlockState state = world.getBlockState(pos);
         Block block = state.getBlock();
         String name = "<invalid>";
         if (block != null && !state.isAir()) {
            name = Tools.getReadableName(world, pos);
         }

         data = data.withMonitor(name);
         if (world.isClientSide()) {
            Logging.message(player, "Counter module is set to block '" + name + "'");
         }
      } else {
         data = data.withPos(GlobalPos.of(Level.OVERWORLD, BlockPosTools.INVALID));
         data = data.withMonitor("");
         if (world.isClientSide()) {
            Logging.message(player, "Counter module is cleared");
         }
      }

      stack.set(ScreenModule.MODULE_COUNTER_DATA, data);
      return InteractionResult.SUCCESS;
   }

   public Collection<DataComponentType<?>> getComponentsToPreserve() {
      return List.of((DataComponentType<?>)ScreenModule.MODULE_COUNTER_DATA.get());
   }
}
