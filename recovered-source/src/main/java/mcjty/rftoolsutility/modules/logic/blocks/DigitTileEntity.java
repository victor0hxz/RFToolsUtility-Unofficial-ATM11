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
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DigitTileEntity extends GenericTileEntity {
   private final LogicSupport support = new LogicSupport();

   public DigitTileEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)LogicBlockModule.DIGIT.be().get(), pos, state);
   }

   public static LogicSlabBlock createBlock() {
      return new LogicSlabBlock(
         new BlockBuilder()
            .topDriver(RFToolsUtilityTOPDriver.DRIVER)
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsutility.shiftmessage")})
            .infoShift(new InfoLine[]{TooltipBuilder.header()})
            .tileEntitySupplier(DigitTileEntity::new)
      );
   }

   public void saveClientDataToNBT(CompoundTag tag, Provider provider) {
      tag.putByte("powered", this.powerLevel);
   }

   public void loadClientDataFromNBT(CompoundTag tag, Provider provider) {
      if (tag.contains("powered")) {
         this.powerLevel = tag.getByteOr("powered", (byte)0);
      }
   }

   public void checkRedstone(Level world, BlockPos pos) {
      this.support.checkRedstone(this, world, pos);
   }

   public void setPowerInput(int powered) {
      if (this.powerLevel != powered) {
         this.powerLevel = (byte)powered;
         this.markDirtyClient();
      }
   }
}
