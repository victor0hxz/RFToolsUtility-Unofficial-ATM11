package mcjty.rftoolsutility.modules.screen.items.modules;

import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;
import mcjty.lib.crafting.IComponentsToPreserve;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.CapabilityTools;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.ModuleTools;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleGuiBuilder;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.TextAlign;
import mcjty.rftoolsbase.tools.GenericModuleItem;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.screen.ScreenConfiguration;
import mcjty.rftoolsutility.modules.screen.ScreenModule;
import mcjty.rftoolsutility.modules.screen.modules.FluidBarScreenModule;
import mcjty.rftoolsutility.modules.screen.modulesclient.FluidBarClientScreenModule;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class FluidModuleItem extends GenericModuleItem implements IComponentsToPreserve {
   public FluidModuleItem() {
      super(RFToolsUtility.setup.defaultProperties().stacksTo(1).durability(1));
   }

   @Nullable
   public Codec<? extends IScreenModule<?, ?>> codec() {
      return FluidBarScreenModule.CODEC;
   }

   @Nullable
   public StreamCodec<RegistryFriendlyByteBuf, ? extends IScreenModule<?, ?>> streamCodec() {
      return FluidBarScreenModule.STREAM_CODEC;
   }

   @Nullable
   public DataComponentType<? extends IScreenModule<?, ?>> componentType() {
      return (DataComponentType<? extends IScreenModule<?, ?>>)ScreenModule.MODULE_FLUIDBAR_DATA.get();
   }

   public IScreenModule<?, ?> createServerScreenModule() {
      return FluidBarScreenModule.DEFAULT;
   }

   public IClientScreenModule<?> createClientScreenModule() {
      return new FluidBarClientScreenModule();
   }

   protected int getUses(ItemStack stack) {
      return (Integer)ScreenConfiguration.FLUID_RFPERTICK.get();
   }

   protected boolean hasGoldMessage(ItemStack stack) {
      return !BlockPosTools.isValid(data(stack).getPos().pos());
   }

   protected String getInfoString(ItemStack stack) {
      FluidBarScreenModule data = data(stack);
      return ModuleTools.getTargetString(data.getMonitor(), data.getPos());
   }

   public String getModuleName() {
      return "Fluid";
   }

   public static FluidBarScreenModule data(ItemStack stack) {
      FluidBarScreenModule data = (FluidBarScreenModule)stack.get(ScreenModule.MODULE_FLUIDBAR_DATA);
      if (data == null) {
         data = FluidBarScreenModule.DEFAULT;
      }

      return data;
   }

   public static void data(ItemStack stack, Function<FluidBarScreenModule, FluidBarScreenModule> setter) {
      FluidBarScreenModule data = data(stack);
      data = setter.apply(data);
      stack.set(ScreenModule.MODULE_FLUIDBAR_DATA, data);
   }

   public void createGui(IModuleGuiBuilder guiBuilder) {
      guiBuilder.label("Label:")
         .text((stack, s) -> data(stack, d -> d.withLine(s)), stack -> data(stack).getLine(), new String[]{"Label text"})
         .color((stack, c) -> data(stack, d -> d.withColor(c)), stack -> data(stack).getColor(), new String[]{"Color for the label"})
         .nl()
         .label("mb+:")
         .color((stack, c) -> data(stack, d -> d.withPosColor(c)), stack -> data(stack).getPosColor(), new String[]{"Color for the mb text"})
         .label("mb-:")
         .color((stack, c) -> data(stack, d -> d.withNegColor(c)), stack -> data(stack).getNegColor(), new String[]{"Color for the negative", "mb/tick ratio"})
         .nl()
         .toggleNegative(
            (stack, b) -> data(stack, d -> d.withHideBar(b)), stack -> data(stack).isHideBar(), "Bar", new String[]{"Toggle visibility of the", "fluid bar"}
         )
         .mode((stack, m) -> data(stack, d -> d.withBarMode(m)), stack -> data(stack).getBarMode(), "mb")
         .format((stack, f) -> data(stack, d -> d.withFormat(f)), stack -> data(stack).getFormat())
         .nl()
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
      FluidBarScreenModule data = data(stack);
      if (CapabilityTools.getFluidCapabilitySafe(te) != null) {
         data = data.withPos(GlobalPos.of(world.dimension(), pos));
         String name = "<invalid>";
         if (!world.getBlockState(pos).isAir()) {
            name = Tools.getReadableName(world, pos);
         }

         data = data.withMonitor(name);
         if (world.isClientSide()) {
            Logging.message(player, "Fluid module is set to block '" + name + "'");
         }
      } else {
         data = data.withPos(GlobalPos.of(Level.OVERWORLD, BlockPosTools.INVALID));
         data = data.withMonitor("");
         if (world.isClientSide()) {
            Logging.message(player, "Fluid module is cleared");
         }
      }

      stack.set(ScreenModule.MODULE_FLUIDBAR_DATA, data);
      return InteractionResult.SUCCESS;
   }

   public Collection<DataComponentType<?>> getComponentsToPreserve() {
      return List.of((DataComponentType<?>)ScreenModule.MODULE_FLUIDBAR_DATA.get());
   }
}
