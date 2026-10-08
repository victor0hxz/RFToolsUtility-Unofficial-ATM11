package mcjty.rftoolsutility.modules.logic.blocks;

import java.util.function.Function;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.bindings.GuiValue;
import mcjty.lib.bindings.Value;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.typed.Type;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsutility.compat.RFToolsUtilityTOPDriver;
import mcjty.rftoolsutility.modules.logic.LogicBlockModule;
import mcjty.rftoolsutility.modules.logic.data.RedstoneChannelData;
import mcjty.rftoolsutility.modules.logic.data.RedstoneReceiverData;
import mcjty.rftoolsutility.modules.logic.tools.RedstoneChannels;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class RedstoneReceiverTileEntity extends RedstoneChannelTileEntity {
   @GuiValue
   public static final Value<RedstoneReceiverTileEntity, Boolean> VALUE_ANALOG = Value.create(
      "analog", Type.BOOLEAN, RedstoneReceiverTileEntity::getAnalog, RedstoneReceiverTileEntity::setAnalog
   );
   @Cap(type = CapType.CONTAINER)
   private static final Function<RedstoneReceiverTileEntity, MenuProvider> SCREEN_CAP = be -> new DefaultContainerProvider("Redstone Receiver")
      .containerSupplier(DefaultContainerProvider.empty(LogicBlockModule.CONTAINER_REDSTONE_RECEIVER, be))
      .setupSync(be);

   public RedstoneReceiverTileEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType<?>)LogicBlockModule.REDSTONE_RECEIVER.be().get(), pos, state);
   }

   public static RedstoneChannelBlock createBlock() {
      return new RedstoneChannelBlock(
         new BlockBuilder()
            .topDriver(RFToolsUtilityTOPDriver.DRIVER)
            .manualEntry(ManualHelper.create("rftoolsbase:logic/redstone_receiver"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsutility.shiftmessage")})
            .infoShift(
               new InfoLine[]{TooltipBuilder.header(), TooltipBuilder.gold(), TooltipBuilder.parameter("channel", RedstoneChannelBlock::getChannelString)}
            )
            .tileEntitySupplier(RedstoneReceiverTileEntity::new)
      );
   }

   public boolean getAnalog() {
      RedstoneReceiverData data = (RedstoneReceiverData)this.getData(LogicBlockModule.REDSTONERECEIVER_DATA);
      return data.analog();
   }

   public void setAnalog(boolean analog) {
      RedstoneReceiverData data = (RedstoneReceiverData)this.getData(LogicBlockModule.REDSTONERECEIVER_DATA);
      data = data.withAnalog(analog);
      this.setData(LogicBlockModule.REDSTONERECEIVER_DATA, data);
   }

   public void tickServer() {
      this.support.setRedstoneState(this, this.checkOutput());
   }

   public int checkOutput() {
      RedstoneChannelData cdata = (RedstoneChannelData)this.getData(LogicBlockModule.REDSTONECHANNEL_DATA);
      if (cdata.channel() != -1) {
         RedstoneReceiverData data = (RedstoneReceiverData)this.getData(LogicBlockModule.REDSTONERECEIVER_DATA);
         RedstoneChannels channels = RedstoneChannels.getChannels(this.level);
         RedstoneChannels.RedstoneChannel ch = channels.getChannel(cdata.channel());
         if (ch != null) {
            int newout = ch.getValue();
            if (!data.analog() && newout > 0) {
               return 15;
            }

            return newout;
         }
      }

      return 0;
   }

   public void loadAdditional(ValueInput tag) {
      super.loadAdditional(tag);
      this.support.setPowerOutput(tag.getIntOr("rs", 0));
   }

   public void saveAdditional(ValueOutput tag) {
      super.saveAdditional(tag);
      tag.putInt("rs", this.support.getPowerOutput());
   }

   @Override
   protected void applyImplicitComponents(DataComponentGetter input) {
      super.applyImplicitComponents(input);
      RedstoneReceiverData data = (RedstoneReceiverData)input.get(LogicBlockModule.ITEM_REDSTONERECEIVER_DATA);
      if (data != null) {
         this.setData(LogicBlockModule.REDSTONERECEIVER_DATA, data);
      }
   }

   @Override
   protected void collectImplicitComponents(Builder builder) {
      super.collectImplicitComponents(builder);
      builder.set(LogicBlockModule.ITEM_REDSTONERECEIVER_DATA, (RedstoneReceiverData)this.getData(LogicBlockModule.REDSTONERECEIVER_DATA));
   }
}
