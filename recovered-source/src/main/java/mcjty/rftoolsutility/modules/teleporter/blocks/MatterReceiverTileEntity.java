package mcjty.rftoolsutility.modules.teleporter.blocks;

import java.util.ArrayList;
import java.util.Set;
import java.util.function.Function;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.infusable.DefaultInfusable;
import mcjty.lib.api.infusable.IInfusable;
import mcjty.lib.api.infusable.ItemInfusable;
import mcjty.lib.api.power.ItemEnergy;
import mcjty.lib.bindings.GuiValue;
import mcjty.lib.bindings.Value;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ListCommand;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.setup.Registration;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.rftoolsutility.modules.teleporter.TeleportConfiguration;
import mcjty.rftoolsutility.modules.teleporter.TeleporterModule;
import mcjty.rftoolsutility.modules.teleporter.client.GuiMatterReceiver;
import mcjty.rftoolsutility.modules.teleporter.data.MatterReceiverData;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestination;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestinations;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class MatterReceiverTileEntity extends TickingTileEntity {
   @GuiValue
   public static final Value<?, String> VALUE_NAME = Value.create("name", Type.STRING, MatterReceiverTileEntity::getName, MatterReceiverTileEntity::setName);
   @GuiValue(name = "private")
   public static final Value<?, Boolean> VALUE_PRIVATE = Value.create(
      "private", Type.BOOLEAN, MatterReceiverTileEntity::isPrivateAccess, MatterReceiverTileEntity::setPrivateAccess
   );
   private final GenericEnergyStorage energyStorage = new GenericEnergyStorage(
      this,
      true,
      ((Integer)TeleportConfiguration.RECEIVER_MAXENERGY.get()).intValue(),
      ((Integer)TeleportConfiguration.RECEIVER_RECEIVEPERTICK.get()).intValue()
   );
   @Cap(type = CapType.ENERGY)
   private static final Function<MatterReceiverTileEntity, GenericEnergyStorage> ENERGY_CAP = tile -> tile.energyStorage;
   @Cap(type = CapType.CONTAINER)
   private static final Function<MatterReceiverTileEntity, MenuProvider> screenHandler = tile -> new DefaultContainerProvider("Matter Receiver")
      .containerSupplier(DefaultContainerProvider.empty(TeleporterModule.CONTAINER_MATTER_RECEIVER, tile))
      .energyHandler(() -> tile.energyStorage)
      .setupSync(tile);
   private final DefaultInfusable infusable = new DefaultInfusable(this);
   @Cap(type = CapType.INFUSABLE)
   private static final Function<MatterReceiverTileEntity, IInfusable> INFUSABLE_CAP = tile -> tile.infusable;
   private BlockPos cachedPos;
   public static final Key<String> PARAM_PLAYER = new Key("player", Type.STRING);
   @ServerCommand
   public static final Command<?> CMD_ADDPLAYER = Command.create("receiver.addPlayer", (te, player, params) -> te.addPlayer((String)params.get(PARAM_PLAYER)));
   @ServerCommand
   public static final Command<?> CMD_DELPLAYER = Command.create("receiver.delPlayer", (te, player, params) -> te.delPlayer((String)params.get(PARAM_PLAYER)));
   @ServerCommand(type = String.class)
   public static final ListCommand<?, ?> CMD_GETPLAYERS = ListCommand.create(
      "rftoolsutility.receiver.getPlayers",
      (te, player, params) -> new ArrayList<>(te.getAllowedPlayers()),
      (te, player, params, list) -> GuiMatterReceiver.storeAllowedPlayersForClient(list)
   );

   public MatterReceiverTileEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)TeleporterModule.MATTER_RECEIVER.be().get(), pos, state);
   }

   public DefaultInfusable getInfusable() {
      return this.infusable;
   }

   public String getName() {
      MatterReceiverData data = (MatterReceiverData)this.getData(TeleporterModule.MATTERRECEIVER_DATA);
      return data.name() == null ? "" : data.name();
   }

   public int getOrCalculateID() {
      MatterReceiverData data = (MatterReceiverData)this.getData(TeleporterModule.MATTERRECEIVER_DATA);
      int id = data.id();
      if (id == -1) {
         TeleportDestinations destinations = TeleportDestinations.get(this.level);
         GlobalPos gc = GlobalPos.of(this.level.dimension(), this.getBlockPos());
         id = destinations.getNewId(gc);
         destinations.save();
         this.setId(id);
      }

      return id;
   }

   public int getId() {
      MatterReceiverData data = (MatterReceiverData)this.getData(TeleporterModule.MATTERRECEIVER_DATA);
      return data.id();
   }

   public void setId(int id) {
      MatterReceiverData data = (MatterReceiverData)this.getData(TeleporterModule.MATTERRECEIVER_DATA);
      data = data.withId(id);
      this.setData(TeleporterModule.MATTERRECEIVER_DATA, data);
   }

   public void setName(String name) {
      MatterReceiverData data = (MatterReceiverData)this.getData(TeleporterModule.MATTERRECEIVER_DATA);
      data = data.withName(name);
      this.setData(TeleporterModule.MATTERRECEIVER_DATA, data);
      if (!this.level.isClientSide()) {
         TeleportDestinations destinations = TeleportDestinations.get(this.level);
         TeleportDestination destination = destinations.getDestination(this.getBlockPos(), this.level.dimension());
         if (destination != null) {
            destination = destination.withName(name);
            destinations.setDestination(GlobalPos.of(this.level.dimension(), this.getBlockPos()), destination);
            destinations.save();
         }
      }
   }

   public void storeEnergy(long amount) {
      this.energyStorage.setEnergy(amount);
   }

   protected void tickServer() {
      if (!this.getBlockPos().equals(this.cachedPos)) {
         TeleportDestinations destinations = TeleportDestinations.get(this.level);
         destinations.removeDestination(this.cachedPos, this.level.dimension());
         this.cachedPos = this.getBlockPos();
         GlobalPos gc = GlobalPos.of(this.level.dimension(), this.getBlockPos());
         MatterReceiverData data = (MatterReceiverData)this.getData(TeleporterModule.MATTERRECEIVER_DATA);
         int id = data.id();
         if (id == -1) {
            id = destinations.getNewId(gc);
            this.setData(TeleporterModule.MATTERRECEIVER_DATA, data.withId(id));
         } else {
            destinations.assignId(gc, id);
         }

         destinations.addDestination(gc);
         destinations.save();
         this.setChanged();
      }
   }

   public boolean isPrivateAccess() {
      MatterReceiverData data = (MatterReceiverData)this.getData(TeleporterModule.MATTERRECEIVER_DATA);
      return data.privateAccess();
   }

   public void setPrivateAccess(boolean privateAccess) {
      MatterReceiverData data = (MatterReceiverData)this.getData(TeleporterModule.MATTERRECEIVER_DATA);
      data = data.withShowFav(privateAccess);
      this.setData(TeleporterModule.MATTERRECEIVER_DATA, data);
      this.updateDestination();
   }

   public TeleportDestination updateDestination() {
      if (this.level.isClientSide()) {
         return null;
      } else {
         TeleportDestinations destinations = TeleportDestinations.get(this.level);
         MatterReceiverData data = (MatterReceiverData)this.getData(TeleporterModule.MATTERRECEIVER_DATA);
         GlobalPos gc = GlobalPos.of(this.level.dimension(), this.getBlockPos());
         TeleportDestination destination = destinations.getDestination(gc.pos(), gc.dimension());
         if (destination != null) {
            destination = destination.withName(data.name());
            int id = data.id();
            if (id == -1) {
               id = destinations.getNewId(gc);
               data = data.withId(id);
               this.setData(TeleporterModule.MATTERRECEIVER_DATA, data);
            } else {
               destinations.assignId(gc, id);
            }

            TeleportDestination var7 = destination.withPrivateAccess(data.privateAccess());
            destination = var7.withAllowedPlayers(data.players());
            destinations.setDestination(gc, destination);
            destinations.save();
         }

         this.setChanged();
         return destination;
      }
   }

   public Set<String> getAllowedPlayers() {
      MatterReceiverData data = (MatterReceiverData)this.getData(TeleporterModule.MATTERRECEIVER_DATA);
      return data.players();
   }

   public void addPlayer(String player) {
      MatterReceiverData data = (MatterReceiverData)this.getData(TeleporterModule.MATTERRECEIVER_DATA);
      if (!data.players().contains(player)) {
         data = data.addPlayer(player);
         this.setData(TeleporterModule.MATTERRECEIVER_DATA, data);
         this.updateDestination();
      }
   }

   public void delPlayer(String player) {
      MatterReceiverData data = (MatterReceiverData)this.getData(TeleporterModule.MATTERRECEIVER_DATA);
      if (data.players().contains(player)) {
         data = data.removePlayer(player);
         this.setData(TeleporterModule.MATTERRECEIVER_DATA, data);
         this.updateDestination();
      }
   }

   public int checkStatus() {
      if (!this.level.getBlockState(this.getBlockPos().above()).isAir()) {
         return 1;
      } else if (!this.level.getBlockState(this.getBlockPos().above(2)).isAir()) {
         return 1;
      } else {
         return this.getStoredPower() < TeleportConfiguration.rfPerTeleportReceiver.get() ? 16 : 0;
      }
   }

   private int getStoredPower() {
      return this.energyStorage.getEnergyStored();
   }

   public void loadAdditional(ValueInput tag) {
      super.loadAdditional(tag);
      this.cachedPos = new BlockPos(tag.getIntOr("cachedX", 0), tag.getIntOr("cachedY", 0), tag.getIntOr("cachedZ", 0));
      this.energyStorage.load(tag, "energy");
      this.infusable.load(tag, "infusable");
   }

   public void saveAdditional(ValueOutput tag) {
      super.saveAdditional(tag);
      if (this.cachedPos != null) {
         tag.putInt("cachedX", this.cachedPos.getX());
         tag.putInt("cachedY", this.cachedPos.getY());
         tag.putInt("cachedZ", this.cachedPos.getZ());
      }

      this.energyStorage.save(tag, "energy");
      this.infusable.save(tag, "infusable");
   }

   protected void applyImplicitComponents(DataComponentGetter input) {
      super.applyImplicitComponents(input);
      MatterReceiverData data = (MatterReceiverData)input.get(TeleporterModule.ITEM_MATTERRECEIVER_DATA);
      if (data != null) {
         this.setData(TeleporterModule.MATTERRECEIVER_DATA, data);
      }

      this.energyStorage.applyImplicitComponents((ItemEnergy)input.get(Registration.ITEM_ENERGY));
      this.infusable.applyImplicitComponents((ItemInfusable)input.get(Registration.ITEM_INFUSABLE));
   }

   protected void collectImplicitComponents(Builder builder) {
      super.collectImplicitComponents(builder);
      builder.set(TeleporterModule.ITEM_MATTERRECEIVER_DATA, (MatterReceiverData)this.getData(TeleporterModule.MATTERRECEIVER_DATA));
      this.energyStorage.collectImplicitComponents(builder);
      this.infusable.collectImplicitComponents(builder);
   }
}
