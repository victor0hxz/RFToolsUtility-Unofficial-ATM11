package mcjty.rftoolsutility.modules.logic.blocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsutility.compat.RFToolsUtilityTOPDriver;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;

public class RedstoneTransmitterBlock extends RedstoneChannelBlock {
   public RedstoneTransmitterBlock() {
      super(
         new BlockBuilder()
            .topDriver(RFToolsUtilityTOPDriver.DRIVER)
            .manualEntry(ManualHelper.create("rftoolsbase:logic/redstone_transmitter"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsutility.shiftmessage")})
            .infoShift(
               new InfoLine[]{TooltipBuilder.header(), TooltipBuilder.gold(), TooltipBuilder.parameter("channel", RedstoneChannelBlock::getChannelString)}
            )
            .tileEntitySupplier(RedstoneTransmitterTileEntity::new)
      );
   }

   public void neighborChanged(
      @Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull Orientation orientation, boolean isMoving
   ) {
      super.neighborChanged(state, world, pos, blockIn, orientation, isMoving);
      RedstoneTransmitterTileEntity te = (RedstoneTransmitterTileEntity)world.getBlockEntity(pos);
      te.update();
   }

   public void setPlacedBy(@Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity placer, @Nonnull ItemStack stack) {
      super.setPlacedBy(world, pos, state, placer, stack);
      if (!world.isClientSide()) {
         ((RedstoneTransmitterTileEntity)world.getBlockEntity(pos)).update();
      }
   }
}
