package mcjty.rftoolsutility.modules.screen.blocks;

import mcjty.rftoolsutility.modules.screen.ScreenModule;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CreativeScreenTileEntity extends ScreenTileEntity {
   public CreativeScreenTileEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType<?>)ScreenModule.CREATIVE_SCREEN.be().get(), pos, state);
   }

   public CreativeScreenTileEntity(ResourceKey<Level> type, BlockPos pos) {
      super(
         (BlockEntityType<?>)ScreenModule.CREATIVE_SCREEN.be().get(), type, pos, ((ScreenBlock)ScreenModule.CREATIVE_SCREEN.block().get()).defaultBlockState()
      );
   }

   @Override
   public boolean isCreative() {
      return true;
   }
}
