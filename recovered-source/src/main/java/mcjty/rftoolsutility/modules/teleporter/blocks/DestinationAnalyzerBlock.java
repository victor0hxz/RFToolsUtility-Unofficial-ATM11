package mcjty.rftoolsutility.modules.teleporter.blocks;

import javax.annotation.Nullable;
import mcjty.lib.setup.RegistrationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

public class DestinationAnalyzerBlock extends Block {
   public DestinationAnalyzerBlock() {
      super(RegistrationContext.prepareBlockProperties(Properties.of().sound(SoundType.METAL).strength(2.0F, 6.0F)));
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockPos pos = context.getClickedPos();
      Player placer = context.getPlayer();
      return (BlockState)super.getStateForPlacement(context).setValue(BlockStateProperties.FACING, getFacingFromEntity(pos, placer));
   }

   public static Direction getFacingFromEntity(BlockPos clickedBlock, LivingEntity entityIn) {
      if (Mth.abs((float)entityIn.getX() - clickedBlock.getX()) < 2.0F && Mth.abs((float)entityIn.getZ() - clickedBlock.getZ()) < 2.0F) {
         double d0 = entityIn.getY() + entityIn.getEyeHeight();
         if (d0 - clickedBlock.getY() > 2.0) {
            return Direction.UP;
         }

         if (clickedBlock.getY() - d0 > 0.0) {
            return Direction.DOWN;
         }
      }

      return entityIn.getDirection().getOpposite();
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{BlockStateProperties.FACING});
   }
}
