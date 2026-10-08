package mcjty.rftoolsutility.modules.teleporter.blocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsutility.compat.RFToolsUtilityTOPDriver;
import mcjty.rftoolsutility.modules.teleporter.TeleporterModule;
import mcjty.rftoolsutility.modules.teleporter.data.MatterReceiverData;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestinations;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MatterReceiverBlock extends BaseBlock {
   public MatterReceiverBlock() {
      super(
         new BlockBuilder()
            .topDriver(RFToolsUtilityTOPDriver.DRIVER)
            .manualEntry(ManualHelper.create("rftoolsbase:machines/matter_receiver"))
            .infusable()
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsutility.shiftmessage")})
            .infoShift(new InfoLine[]{TooltipBuilder.header(), TooltipBuilder.gold(), TooltipBuilder.parameter("info", MatterReceiverBlock::getName)})
            .tileEntitySupplier(MatterReceiverTileEntity::new)
      );
   }

   private static String getName(ItemStack stack) {
      MatterReceiverData data = (MatterReceiverData)stack.get(TeleporterModule.ITEM_MATTERRECEIVER_DATA);
      return data != null ? data.name() : "<unset>";
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockState state = super.getStateForPlacement(context);
      Level world = context.getLevel();
      if (world.isClientSide()) {
         return state;
      } else {
         TeleportDestinations destinations = TeleportDestinations.get(world);
         BlockPos pos = context.getClickedPos();
         GlobalPos gc = GlobalPos.of(world.dimension(), pos);
         destinations.getNewId(gc);
         destinations.addDestination(gc);
         destinations.save();
         return state;
      }
   }

   public void setPlacedBy(@Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState state, LivingEntity placer, @Nonnull ItemStack stack) {
      if (!world.isClientSide() && world.getBlockEntity(pos) instanceof MatterReceiverTileEntity receiver) {
         receiver.getOrCalculateID();
         receiver.updateDestination();
      }

      this.setOwner(world, pos, placer);
   }

   public void onRemove(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState newstate, boolean isMoving) {
      super.onRemove(state, world, pos, newstate, isMoving);
      if (!world.isClientSide()) {
         TeleportDestinations destinations = TeleportDestinations.get(world);
         destinations.removeDestination(pos, world.dimension());
         destinations.save();
      }
   }

   public RotationType getRotationType() {
      return RotationType.NONE;
   }
}
