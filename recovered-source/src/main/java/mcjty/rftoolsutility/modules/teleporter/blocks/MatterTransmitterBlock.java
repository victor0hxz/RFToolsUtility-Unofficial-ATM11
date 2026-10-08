package mcjty.rftoolsutility.modules.teleporter.blocks;

import javax.annotation.Nonnull;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsutility.compat.RFToolsUtilityTOPDriver;
import mcjty.rftoolsutility.modules.teleporter.TeleporterModule;
import mcjty.rftoolsutility.modules.teleporter.data.MatterTransmitterData;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestination;
import mcjty.rftoolsutility.setup.CommandHandler;
import mcjty.rftoolsutility.setup.RFToolsUtilityMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MatterTransmitterBlock extends BaseBlock {
   public static Integer clientSideId = null;
   public static String clientSideName = "?";
   private static long lastTime = 0L;

   public MatterTransmitterBlock() {
      super(
         new BlockBuilder()
            .topDriver(RFToolsUtilityTOPDriver.DRIVER)
            .manualEntry(ManualHelper.create("rftoolsbase:machines/matter_transmitter"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsutility.shiftmessage")})
            .infusable()
            .infoShift(
               new InfoLine[]{
                  TooltipBuilder.header(),
                  TooltipBuilder.gold(),
                  TooltipBuilder.parameter("info", MatterTransmitterBlock::getName),
                  TooltipBuilder.parameter("once", MatterTransmitterBlock::hasOnce, stack -> hasOnce(stack) ? "[ONCE]" : ""),
                  TooltipBuilder.parameter("dialed", MatterTransmitterBlock::getDialInfoClient)
               }
            )
            .tileEntitySupplier(MatterTransmitterTileEntity::new)
      );
   }

   public static void setDestinationInfo(Integer id, String name) {
      clientSideId = id;
      clientSideName = name;
   }

   private static String getName(ItemStack stack) {
      MatterTransmitterData data = (MatterTransmitterData)stack.get(TeleporterModule.ITEM_MATTERTRANSMITTER_DATA);
      return data != null ? data.name() : "<unset>";
   }

   private static boolean hasOnce(ItemStack stack) {
      MatterTransmitterData data = (MatterTransmitterData)stack.get(TeleporterModule.ITEM_MATTERTRANSMITTER_DATA);
      return data != null ? data.once() : false;
   }

   private static String getDialInfoClient(ItemStack stack) {
      MatterTransmitterData data = (MatterTransmitterData)stack.get(TeleporterModule.ITEM_MATTERTRANSMITTER_DATA);
      if (data == null) {
         return "<undialed>";
      } else {
         TeleportDestination destination = data.destination();
         boolean dialed = destination != null && destination.isValid() || data.destinationId() != null;
         if (dialed) {
            Integer destId = data.destinationId();
            if (System.currentTimeMillis() - lastTime > 500L) {
               lastTime = System.currentTimeMillis();
               RFToolsUtilityMessages.sendToServer("getDestinationInfo", TypedMap.builder().put(CommandHandler.PARAM_ID, destId));
            }

            String destname = "?";
            if (clientSideId != null && clientSideId == destId) {
               destname = clientSideName;
            }

            return destname;
         } else {
            return destination.getName();
         }
      }
   }

   public void setPlacedBy(@Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState state, LivingEntity placer, @Nonnull ItemStack stack) {
      this.setOwner(world, pos, placer);
   }

   public RotationType getRotationType() {
      return RotationType.NONE;
   }
}
