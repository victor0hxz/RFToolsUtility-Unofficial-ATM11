package mcjty.rftoolsutility.modules.teleporter.blocks;

import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.tileentity.LogicSupport;
import mcjty.lib.varia.BlockPosTools;
import mcjty.rftoolsutility.modules.teleporter.TeleportationTools;
import mcjty.rftoolsutility.modules.teleporter.TeleporterModule;
import mcjty.rftoolsutility.modules.teleporter.data.SimpleDialerData;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestinations;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleDialerTileEntity extends GenericTileEntity {
   private final LogicSupport support = new LogicSupport();
   private boolean prevIn = false;

   public SimpleDialerTileEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)TeleporterModule.SIMPLE_DIALER.be().get(), pos, state);
   }

   public void checkRedstone(Level world, BlockPos pos) {
      this.support.checkRedstone(this, world, pos);
   }

   public int getRedstoneOutput(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
      return this.support.getRedstoneOutput(state, side);
   }

   public void update() {
      SimpleDialerData data = (SimpleDialerData)this.getData(TeleporterModule.SIMPLEDIALER_DATA);
      GlobalPos transmitter = data.transmitter();
      if (BlockPosTools.isValid(transmitter.pos())) {
         if (this.powerLevel > 0 != this.prevIn) {
            this.prevIn = this.powerLevel > 0;
            this.setChanged();
            if (this.powerLevel > 0) {
               TeleportDestinations destinations = TeleportDestinations.get(this.level);
               BlockPos coordinate = null;
               ResourceKey<Level> dim = Level.OVERWORLD;
               if (data.receiver() != -1) {
                  GlobalPos gc = destinations.getCoordinateForId(data.receiver());
                  if (gc != null) {
                     coordinate = gc.pos();
                     dim = gc.dimension();
                  }
               }

               int dial = TeleportationTools.dial(this.getLevel(), null, null, transmitter.pos(), transmitter.dimension(), coordinate, dim, data.onceMode());
               if (dial != 0) {
               }
            }
         }
      }
   }

   public boolean isOnceMode() {
      SimpleDialerData data = (SimpleDialerData)this.getData(TeleporterModule.SIMPLEDIALER_DATA);
      return data.onceMode();
   }

   public void setOnceMode(boolean onceMode) {
      SimpleDialerData data = (SimpleDialerData)this.getData(TeleporterModule.SIMPLEDIALER_DATA);
      data = data.withOnceMode(onceMode);
      this.setData(TeleporterModule.SIMPLEDIALER_DATA, data);
   }

   public GlobalPos getTransmitter() {
      SimpleDialerData data = (SimpleDialerData)this.getData(TeleporterModule.SIMPLEDIALER_DATA);
      return data.transmitter();
   }

   public int getReceiver() {
      SimpleDialerData data = (SimpleDialerData)this.getData(TeleporterModule.SIMPLEDIALER_DATA);
      return data.receiver();
   }

   protected void applyImplicitComponents(DataComponentGetter input) {
      super.applyImplicitComponents(input);
      SimpleDialerData data = (SimpleDialerData)input.get(TeleporterModule.ITEM_SIMPLEDIALER_DATA);
      if (data != null) {
         this.setData(TeleporterModule.SIMPLEDIALER_DATA, data);
      }
   }

   protected void collectImplicitComponents(Builder builder) {
      super.collectImplicitComponents(builder);
      builder.set(TeleporterModule.ITEM_SIMPLEDIALER_DATA, (SimpleDialerData)this.getData(TeleporterModule.SIMPLEDIALER_DATA));
   }
}
