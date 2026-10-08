package mcjty.rftoolsutility.modules.logic.blocks;

import java.util.function.Function;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.bindings.GuiValue;
import mcjty.lib.bindings.Value;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.typed.Type;
import mcjty.rftoolsutility.modules.logic.LogicBlockModule;
import mcjty.rftoolsutility.modules.logic.network.PacketSetChannelName;
import mcjty.rftoolsutility.modules.logic.tools.RedstoneChannels;
import mcjty.rftoolsutility.setup.RFToolsUtilityMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class RedstoneTransmitterTileEntity extends RedstoneChannelTileEntity {
   private int prevIn = -1;
   private String channelName;
   @GuiValue
   public static final Value<?, String> VALUE_NAME = Value.create(
      "name", Type.STRING, RedstoneTransmitterTileEntity::getChannelName, RedstoneTransmitterTileEntity::setChannelName
   );
   @Cap(type = CapType.CONTAINER)
   private static final Function<RedstoneTransmitterTileEntity, MenuProvider> SCREEN_CAP = be -> new DefaultContainerProvider("Redstone Receiver")
      .containerSupplier(DefaultContainerProvider.empty(LogicBlockModule.CONTAINER_REDSTONE_TRANSMITTER, be))
      .setupSync(be);

   public void setChannelName(String v) {
      if (this.level.isClientSide()) {
         this.channelName = v;
         RFToolsUtilityMessages.sendToServer(PacketSetChannelName.create(this.worldPosition, this.channelName));
      } else {
         if (this.getChannel() == -1) {
            this.getChannel(true);
         }

         RedstoneChannels channels = RedstoneChannels.getChannels(this.level);
         RedstoneChannels.RedstoneChannel ch = channels.getOrCreateChannel(this.getChannel());
         ch.setName(v);
         channels.setDirty();
         this.setChanged();
      }
   }

   private String getChannelName() {
      if (this.level.isClientSide()) {
         return this.channelName;
      } else if (this.getChannel() == -1) {
         return "";
      } else {
         RedstoneChannels channels = RedstoneChannels.getChannels(this.level);
         RedstoneChannels.RedstoneChannel ch = channels.getOrCreateChannel(this.getChannel());
         return ch.getName();
      }
   }

   public RedstoneTransmitterTileEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType<?>)LogicBlockModule.REDSTONE_TRANSMITTER.be().get(), pos, state);
   }

   public void saveClientDataToNBT(CompoundTag tag, Provider provider) {
      tag.putString("channelName", this.getChannelName());
   }

   public void loadClientDataFromNBT(CompoundTag tag, Provider provider) {
      this.channelName = tag.getStringOr("channelName", "");
   }

   @Override
   public void setChannel(int channel) {
      super.setChannel(channel);
      this.update();
   }

   public void update() {
      if (!this.level.isClientSide()) {
         if (this.getChannel() != -1) {
            if (this.powerLevel != this.prevIn) {
               this.prevIn = this.powerLevel;
               this.setChanged();
               RedstoneChannels channels = RedstoneChannels.getChannels(this.level);
               RedstoneChannels.RedstoneChannel ch = channels.getOrCreateChannel(this.getChannel());
               ch.setValue(this.powerLevel);
               channels.save();
            }
         }
      }
   }

   public void loadAdditional(ValueInput tag) {
      super.loadAdditional(tag);
      if (tag.getInt("prevIn").isPresent()) {
         this.prevIn = tag.getIntOr("prevIn", 0);
      } else {
         this.prevIn = tag.getBooleanOr("prevIn", false) ? 15 : 0;
      }
   }

   public void saveAdditional(ValueOutput tag) {
      super.saveAdditional(tag);
      tag.putInt("prevIn", this.prevIn);
   }
}
