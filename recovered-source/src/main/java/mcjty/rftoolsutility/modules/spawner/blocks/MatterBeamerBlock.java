package mcjty.rftoolsutility.modules.spawner.blocks;

import javax.annotation.Nonnull;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsutility.compat.RFToolsUtilityTOPDriver;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

public class MatterBeamerBlock extends BaseBlock {
   public MatterBeamerBlock() {
      super(
         new BlockBuilder()
            .tileEntitySupplier(MatterBeamerTileEntity::new)
            .topDriver(RFToolsUtilityTOPDriver.DRIVER)
            .infusable()
            .manualEntry(ManualHelper.create("rftoolsutility:machines/matter_beamer"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsutility.shiftmessage")})
            .infoShift(new InfoLine[]{TooltipBuilder.header(), TooltipBuilder.gold()})
      );
   }

   public RotationType getRotationType() {
      return RotationType.NONE;
   }

   protected void createBlockStateDefinition(@Nonnull Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{BlockStateProperties.LIT});
   }
}
