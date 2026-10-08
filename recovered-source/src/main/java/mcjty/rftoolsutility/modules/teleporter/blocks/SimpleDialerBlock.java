package mcjty.rftoolsutility.modules.teleporter.blocks;

import javax.annotation.Nonnull;
import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsutility.compat.RFToolsUtilityTOPDriver;
import mcjty.rftoolsutility.modules.teleporter.TeleporterModule;
import mcjty.rftoolsutility.modules.teleporter.data.SimpleDialerData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;

public class SimpleDialerBlock extends LogicSlabBlock {
   public SimpleDialerBlock() {
      super(
         new BlockBuilder()
            .topDriver(RFToolsUtilityTOPDriver.DRIVER)
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsutility.shiftmessage")})
            .infoShift(
               new InfoLine[]{
                  TooltipBuilder.header(),
                  TooltipBuilder.parameter("transmitter", SimpleDialerBlock::getTransmitterInfo),
                  TooltipBuilder.parameter("receiver", SimpleDialerBlock::getReceiverInfo),
                  TooltipBuilder.parameter("once", SimpleDialerBlock::hasOnce, stack -> hasOnce(stack) ? "Once mode enabled" : "")
               }
            )
            .tileEntitySupplier(SimpleDialerTileEntity::new)
      );
   }

   private static boolean hasOnce(ItemStack stack) {
      SimpleDialerData data = (SimpleDialerData)stack.get(TeleporterModule.ITEM_SIMPLEDIALER_DATA);
      return data != null ? data.onceMode() : false;
   }

   private static String getTransmitterInfo(ItemStack stack) {
      SimpleDialerData data = (SimpleDialerData)stack.get(TeleporterModule.ITEM_SIMPLEDIALER_DATA);
      if (data != null && BlockPosTools.isValid(data.transmitter().pos())) {
         int transX = data.transmitter().pos().getX();
         int transY = data.transmitter().pos().getY();
         int transZ = data.transmitter().pos().getZ();
         String dim = data.transmitter().dimension().identifier().toString();
         return transX + "," + transY + "," + transZ + " (dim " + dim + ")";
      } else {
         return "<unset>";
      }
   }

   private static String getReceiverInfo(ItemStack stack) {
      SimpleDialerData data = (SimpleDialerData)stack.get(TeleporterModule.ITEM_SIMPLEDIALER_DATA);
      return data != null && data.receiver() != -1 ? Integer.toString(data.receiver()) : "<unset>";
   }

   protected boolean wrenchUse(Level world, BlockPos pos, Direction side, Player player) {
      if (!world.isClientSide()) {
         SimpleDialerTileEntity simpleDialerTileEntity = (SimpleDialerTileEntity)world.getBlockEntity(pos);
         if (simpleDialerTileEntity != null) {
            boolean onceMode = !simpleDialerTileEntity.isOnceMode();
            simpleDialerTileEntity.setOnceMode(onceMode);
            if (onceMode) {
               Logging.message(player, "Enabled 'dial once' mode");
            } else {
               Logging.message(player, "Disabled 'dial once' mode");
            }
         }
      }

      return true;
   }

   public void neighborChanged(
      @Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull Orientation orientation, boolean isMoving
   ) {
      super.neighborChanged(state, world, pos, blockIn, orientation, isMoving);
      if (world.getBlockEntity(pos) instanceof SimpleDialerTileEntity simpleDialer) {
         simpleDialer.update();
      }
   }
}
