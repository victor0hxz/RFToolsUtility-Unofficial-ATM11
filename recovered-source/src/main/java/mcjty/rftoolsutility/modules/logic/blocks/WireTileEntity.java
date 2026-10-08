package mcjty.rftoolsutility.modules.logic.blocks;

import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.tileentity.LogicSupport;
import mcjty.rftoolsutility.compat.RFToolsUtilityTOPDriver;
import mcjty.rftoolsutility.modules.logic.LogicBlockModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class WireTileEntity extends GenericTileEntity {
   private final LogicSupport support = new LogicSupport();
   private int loopDetector = 0;

   public static LogicSlabBlock createBlock() {
      return new LogicSlabBlock(
         new BlockBuilder()
            .topDriver(RFToolsUtilityTOPDriver.DRIVER)
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsutility.shiftmessage")})
            .infoShift(new InfoLine[]{TooltipBuilder.header()})
            .tileEntitySupplier(WireTileEntity::new)
      );
   }

   public WireTileEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)LogicBlockModule.WIRE.be().get(), pos, state);
   }

   public int getRedstoneOutput(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
      return side == LogicSupport.getFacing(state).getInputSide() ? this.powerLevel : 0;
   }

   public void checkRedstone(Level world, BlockPos pos) {
      this.support.checkRedstone(this, world, pos);
      if (this.loopDetector <= 0) {
         this.loopDetector++;
         BlockState state = world.getBlockState(pos);
         BlockPos offsetPos = pos.relative(LogicSupport.getFacing(state).getInputSide().getOpposite());
         if (world.hasChunkAt(offsetPos)) {
            world.neighborChanged(offsetPos, state.getBlock(), null);
         }

         this.loopDetector--;
      }
   }
}
