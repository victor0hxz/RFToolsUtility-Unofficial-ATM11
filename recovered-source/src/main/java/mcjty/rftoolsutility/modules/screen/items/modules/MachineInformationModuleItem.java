package mcjty.rftoolsutility.modules.screen.items.modules;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import javax.annotation.Nonnull;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.ModuleTools;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.api.machineinfo.CapabilityMachineInformation;
import mcjty.rftoolsbase.api.machineinfo.IMachineInformation;
import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleGuiBuilder;
import mcjty.rftoolsbase.api.screens.IModuleProvider;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleGuiBuilder.Choice;
import mcjty.rftoolsbase.tools.GenericModuleItem;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.screen.ScreenConfiguration;
import mcjty.rftoolsutility.modules.screen.ScreenModule;
import mcjty.rftoolsutility.modules.screen.modules.MachineInformationScreenModule;
import mcjty.rftoolsutility.modules.screen.modulesclient.MachineInformationClientScreenModule;
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

public class MachineInformationModuleItem extends GenericModuleItem implements IModuleProvider {
   private static final Choice[] EMPTY_CHOICES = new Choice[0];

   public MachineInformationModuleItem() {
      super(RFToolsUtility.setup.defaultProperties().stacksTo(1).durability(1));
   }

   @Nullable
   public Codec<? extends IScreenModule<?, ?>> codec() {
      return MachineInformationScreenModule.CODEC;
   }

   @Nullable
   public StreamCodec<RegistryFriendlyByteBuf, ? extends IScreenModule<?, ?>> streamCodec() {
      return MachineInformationScreenModule.STREAM_CODEC;
   }

   @Nullable
   public DataComponentType<? extends IScreenModule<?, ?>> componentType() {
      return (DataComponentType<? extends IScreenModule<?, ?>>)ScreenModule.MODULE_MACHINEINFO_DATA.get();
   }

   public IScreenModule<?, ?> createServerScreenModule() {
      return MachineInformationScreenModule.DEFAULT;
   }

   public IClientScreenModule<?> createClientScreenModule() {
      return new MachineInformationClientScreenModule();
   }

   protected int getUses(ItemStack stack) {
      return (Integer)ScreenConfiguration.MACHINEINFO_RFPERTICK.get();
   }

   protected boolean hasGoldMessage(ItemStack stack) {
      return !BlockPosTools.isValid(data(stack).getPos().pos());
   }

   protected String getInfoString(ItemStack stack) {
      MachineInformationScreenModule data = data(stack);
      return ModuleTools.getTargetString(data.getMonitor(), data.getPos());
   }

   public String getModuleName() {
      return "Info";
   }

   public static MachineInformationScreenModule data(ItemStack stack) {
      MachineInformationScreenModule data = (MachineInformationScreenModule)stack.get(ScreenModule.MODULE_MACHINEINFO_DATA);
      if (data == null) {
         data = MachineInformationScreenModule.DEFAULT;
      }

      return data;
   }

   public static void data(ItemStack stack, Function<MachineInformationScreenModule, MachineInformationScreenModule> setter) {
      MachineInformationScreenModule data = data(stack);
      data = setter.apply(data);
      stack.set(ScreenModule.MODULE_MACHINEINFO_DATA, data);
   }

   public void createGui(IModuleGuiBuilder guiBuilder) {
      Level world = guiBuilder.getWorld();
      MachineInformationScreenModule currentData = data(guiBuilder.getCurrentModule());
      Choice[] choices = EMPTY_CHOICES;
      if (currentData.getPos().dimension().equals(world.dimension())) {
         BlockEntity tileEntity = world.getBlockEntity(currentData.getPos().pos());
         if (tileEntity != null) {
            IMachineInformation capability = (IMachineInformation)world.getCapability(
               CapabilityMachineInformation.MACHINE_INFORMATION_CAPABILITY, currentData.getPos().pos(), null
            );
            if (capability != null) {
               int count = capability.getTagCount();
               Choice[] cs = new Choice[count];

               for (int i = 0; i < count; i++) {
                  cs[i] = new Choice(capability.getTagName(i), new String[]{capability.getTagDescription(i)});
               }

               choices = cs;
            }
         }
      }

      guiBuilder.label("L:")
         .color((stack, c) -> data(stack, d -> d.withLabcolor(c)), stack -> data(stack).getLabcolor(), new String[]{"Color for the label"})
         .label("Txt:")
         .color((stack, c) -> data(stack, d -> d.withTxtcolor(c)), stack -> data(stack).getTxtcolor(), new String[]{"Color for the text"})
         .nl()
         .choices((stack, s) -> data(stack, d -> d.withTag(s)), stack -> data(stack).getTag(), choices)
         .nl()
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
      MachineInformationScreenModule data = data(stack);
      IMachineInformation capability = (IMachineInformation)world.getCapability(CapabilityMachineInformation.MACHINE_INFORMATION_CAPABILITY, pos, null);
      if (te != null && capability != null) {
         data = data.withPos(GlobalPos.of(world.dimension(), pos));
         String name = "<invalid>";
         if (!world.getBlockState(pos).isAir()) {
            name = Tools.getReadableName(world, pos);
         }

         data = data.withMonitor(name);
         if (world.isClientSide()) {
            Logging.message(player, "Machine Information module is set to block '" + name + "'");
         }
      } else {
         data = data.withPos(GlobalPos.of(Level.OVERWORLD, BlockPosTools.INVALID));
         data = data.withMonitor("");
         if (world.isClientSide()) {
            Logging.message(player, "Machine Information module is cleared");
         }
      }

      stack.set(ScreenModule.MODULE_MACHINEINFO_DATA, data);
      return InteractionResult.SUCCESS;
   }
}
