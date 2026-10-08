package mcjty.rftoolsutility.modules.teleporter.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nonnull;
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
import mcjty.lib.varia.Cached;
import mcjty.lib.varia.LevelTools;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsbase.api.machineinfo.IMachineInformation;
import mcjty.rftoolsutility.compat.RFToolsDimCompat;
import mcjty.rftoolsutility.modules.teleporter.TeleportConfiguration;
import mcjty.rftoolsutility.modules.teleporter.TeleportationTools;
import mcjty.rftoolsutility.modules.teleporter.TeleporterModule;
import mcjty.rftoolsutility.modules.teleporter.client.GuiMatterTransmitter;
import mcjty.rftoolsutility.modules.teleporter.data.MatterTransmitterData;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestination;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestinations;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class MatterTransmitterTileEntity extends TickingTileEntity {
   private UUID teleportingPlayer = null;
   private int teleportTimer = 0;
   private int cooldownTimer = 0;
   private int totalTicks;
   private int goodTicks;
   private int badTicks;
   private int rfPerTick = 0;
   private int status = 0;
   private int checkReceiverStatusCounter = 20;
   private final Cached<AABB> beamBox = Cached.of(this::createBeamBox);
   private final GenericEnergyStorage energyStorage = new GenericEnergyStorage(
      this,
      true,
      ((Integer)TeleportConfiguration.TRANSMITTER_MAXENERGY.get()).intValue(),
      ((Integer)TeleportConfiguration.TRANSMITTER_RECEIVEPERTICK.get()).intValue()
   );
   @Cap(type = CapType.ENERGY)
   private static final Function<MatterTransmitterTileEntity, GenericEnergyStorage> ENERGY_CAP = tile -> tile.energyStorage;
   @Cap(type = CapType.CONTAINER)
   private static final Function<MatterTransmitterTileEntity, MenuProvider> SCREEN_CAP = be -> new DefaultContainerProvider("Matter Transmitter")
      .containerSupplier(DefaultContainerProvider.empty(TeleporterModule.CONTAINER_MATTER_TRANSMITTER, be))
      .energyHandler(() -> be.energyStorage)
      .setupSync(be);
   private final DefaultInfusable infusable = new DefaultInfusable(this);
   @Cap(type = CapType.INFUSABLE)
   private static final Function<MatterTransmitterTileEntity, IInfusable> INFUSABLE_CAP = tile -> tile.infusable;
   private final IMachineInformation infoHandler = this.createMachineInfo();
   @GuiValue
   public static final Value<?, String> VALUE_NAME = Value.create(
      "name", Type.STRING, MatterTransmitterTileEntity::getName, MatterTransmitterTileEntity::setName
   );
   @GuiValue(name = "private")
   public static final Value<?, Boolean> VALUE_PRIVATE = Value.create(
      "private", Type.BOOLEAN, MatterTransmitterTileEntity::isPrivateAccess, MatterTransmitterTileEntity::setPrivateAccess
   );
   @GuiValue(name = "beam")
   public static final Value<?, Boolean> VALUE_BEAMHIDDEN = Value.create(
      "beam", Type.BOOLEAN, MatterTransmitterTileEntity::isBeamHidden, MatterTransmitterTileEntity::setBeamHidden
   );
   public static final Key<String> PARAM_PLAYER = new Key("player", Type.STRING);
   @ServerCommand
   public static final Command<?> CMD_ADDPLAYER = Command.create("receiver.addPlayer", (te, player, params) -> te.addPlayer((String)params.get(PARAM_PLAYER)));
   @ServerCommand
   public static final Command<?> CMD_DELPLAYER = Command.create("receiver.delPlayer", (te, player, params) -> te.delPlayer((String)params.get(PARAM_PLAYER)));
   @ServerCommand(type = String.class)
   public static final ListCommand<?, ?> CMD_GETPLAYERS = ListCommand.create(
      "rftoolsutility.transmitter.getPlayers",
      (te, player, params) -> te.getAllowedPlayers(),
      (te, player, params, list) -> GuiMatterTransmitter.storeAllowedPlayersForClient(list)
   );

   public MatterTransmitterTileEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)TeleporterModule.MATTER_TRANSMITTER.be().get(), pos, state);
   }

   public String getName() {
      MatterTransmitterData data = (MatterTransmitterData)this.getData(TeleporterModule.MATTERTRANSMITTER_DATA);
      return data.name() == null ? "" : data.name();
   }

   public void setName(String name) {
      MatterTransmitterData data = (MatterTransmitterData)this.getData(TeleporterModule.MATTERTRANSMITTER_DATA);
      data = data.withName(name);
      this.setData(TeleporterModule.MATTERTRANSMITTER_DATA, data);
   }

   public boolean isPrivateAccess() {
      MatterTransmitterData data = (MatterTransmitterData)this.getData(TeleporterModule.MATTERTRANSMITTER_DATA);
      return data.privateAccess();
   }

   public void setPrivateAccess(boolean privateAccess) {
      MatterTransmitterData data = (MatterTransmitterData)this.getData(TeleporterModule.MATTERTRANSMITTER_DATA);
      data = data.withPrivateAccess(privateAccess);
      this.setData(TeleporterModule.MATTERTRANSMITTER_DATA, data);
   }

   public boolean isBeamHidden() {
      MatterTransmitterData data = (MatterTransmitterData)this.getData(TeleporterModule.MATTERTRANSMITTER_DATA);
      return data.beamHidden();
   }

   public void setBeamHidden(boolean b) {
      MatterTransmitterData data = (MatterTransmitterData)this.getData(TeleporterModule.MATTERTRANSMITTER_DATA);
      data = data.withBeamHidden(b);
      this.setData(TeleporterModule.MATTERTRANSMITTER_DATA, data);
   }

   public boolean isOnce() {
      MatterTransmitterData data = (MatterTransmitterData)this.getData(TeleporterModule.MATTERTRANSMITTER_DATA);
      return data.once();
   }

   public boolean checkAccess(String player) {
      MatterTransmitterData data = (MatterTransmitterData)this.getData(TeleporterModule.MATTERTRANSMITTER_DATA);
      return !data.privateAccess() ? true : data.players().contains(player);
   }

   public boolean checkAccess(UUID player) {
      MatterTransmitterData data = (MatterTransmitterData)this.getData(TeleporterModule.MATTERTRANSMITTER_DATA);
      if (!data.privateAccess()) {
         return true;
      } else {
         ServerPlayer entity = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(player);
         return entity == null ? false : data.players().contains(entity.getDisplayName().getString());
      }
   }

   public int getStatus() {
      return this.status;
   }

   public List<String> getAllowedPlayers() {
      MatterTransmitterData data = (MatterTransmitterData)this.getData(TeleporterModule.MATTERTRANSMITTER_DATA);
      return new ArrayList<>(data.players());
   }

   public void addPlayer(String player) {
      MatterTransmitterData data = (MatterTransmitterData)this.getData(TeleporterModule.MATTERTRANSMITTER_DATA);
      if (!data.players().contains(player)) {
         data = data.addPlayer(player);
         this.setData(TeleporterModule.MATTERTRANSMITTER_DATA, data);
      }
   }

   public void delPlayer(String player) {
      MatterTransmitterData data = (MatterTransmitterData)this.getData(TeleporterModule.MATTERTRANSMITTER_DATA);
      if (data.players().contains(player)) {
         data = data.removePlayer(player);
         this.setData(TeleporterModule.MATTERTRANSMITTER_DATA, data);
      }
   }

   public void saveClientDataToNBT(CompoundTag tag, Provider provider) {
      MatterTransmitterData.CODEC
         .encodeStart(NbtOps.INSTANCE, (MatterTransmitterData)this.getData(TeleporterModule.MATTERTRANSMITTER_DATA))
         .result()
         .ifPresent(data -> tag.put("data", data));
      tag.putInt("status", this.status);
   }

   public void loadClientDataFromNBT(CompoundTag tag, Provider provider) {
      MatterTransmitterData.CODEC
         .decode(NbtOps.INSTANCE, tag.get("data"))
         .result()
         .ifPresent(data -> this.setData(TeleporterModule.MATTERTRANSMITTER_DATA, (MatterTransmitterData)data.getFirst()));
      this.status = tag.getIntOr("status", 0);
   }

   public void loadAdditional(ValueInput tag) {
      super.loadAdditional(tag);
      this.teleportTimer = tag.getIntOr("tpTimer", 0);
      this.cooldownTimer = tag.getIntOr("cooldownTimer", 0);
      this.totalTicks = tag.getIntOr("totalTicks", 0);
      this.goodTicks = tag.getIntOr("goodTicks", 0);
      this.badTicks = tag.getIntOr("badTicks", 0);
      this.teleportingPlayer = tag.getString("tpPlayer").flatMap(v -> {
         try {
            return Optional.of(UUID.fromString(v));
         } catch (IllegalArgumentException var2) {
            return Optional.empty();
         }
      }).orElse(null);
      this.status = tag.getIntOr("status", 0);
      this.rfPerTick = tag.getIntOr("rfPerTick", 0);
      this.energyStorage.load(tag, "energy");
      this.infusable.load(tag, "infusable");
   }

   public void saveAdditional(ValueOutput tag) {
      super.saveAdditional(tag);
      tag.putInt("tpTimer", this.teleportTimer);
      tag.putInt("cooldownTimer", this.cooldownTimer);
      tag.putInt("totalTicks", this.totalTicks);
      tag.putInt("goodTicks", this.goodTicks);
      tag.putInt("badTicks", this.badTicks);
      if (this.teleportingPlayer != null) {
         tag.putString("tpPlayer", this.teleportingPlayer.toString());
      }

      tag.putInt("status", this.status);
      tag.putInt("rfPerTick", this.rfPerTick);
      this.energyStorage.save(tag, "energy");
      this.infusable.save(tag, "infusable");
   }

   protected void applyImplicitComponents(DataComponentGetter input) {
      super.applyImplicitComponents(input);
      MatterTransmitterData data = (MatterTransmitterData)input.get(TeleporterModule.ITEM_MATTERTRANSMITTER_DATA);
      if (data != null) {
         this.setData(TeleporterModule.MATTERTRANSMITTER_DATA, data);
      }

      this.energyStorage.applyImplicitComponents((ItemEnergy)input.get(Registration.ITEM_ENERGY));
      this.infusable.applyImplicitComponents((ItemInfusable)input.get(Registration.ITEM_INFUSABLE));
   }

   protected void collectImplicitComponents(Builder builder) {
      super.collectImplicitComponents(builder);
      builder.set(TeleporterModule.ITEM_MATTERTRANSMITTER_DATA, (MatterTransmitterData)this.getData(TeleporterModule.MATTERTRANSMITTER_DATA));
      this.energyStorage.collectImplicitComponents(builder);
      this.infusable.collectImplicitComponents(builder);
   }

   public boolean isDialed() {
      MatterTransmitterData data = (MatterTransmitterData)this.getData(TeleporterModule.MATTERTRANSMITTER_DATA);
      return data.destinationId() != null || data.destination() != null && data.destination().isValid();
   }

   public Integer getTeleportId() {
      MatterTransmitterData data = (MatterTransmitterData)this.getData(TeleporterModule.MATTERTRANSMITTER_DATA);
      if (this.isDialed() && data.destinationId() == null) {
         this.getTeleportDestination();
      }

      return data.destinationId();
   }

   public TeleportDestination getTeleportDestination() {
      MatterTransmitterData data = (MatterTransmitterData)this.getData(TeleporterModule.MATTERTRANSMITTER_DATA);
      if (data.destinationId() != null) {
         TeleportDestinations teleportDestinations = TeleportDestinations.get(this.level);
         GlobalPos gc = teleportDestinations.getCoordinateForId(data.destinationId());
         return gc == null ? null : teleportDestinations.getDestination(gc.pos(), gc.dimension());
      } else {
         return data.destination();
      }
   }

   public void setTeleportDestination(TeleportDestination teleportDestination, boolean once) {
      MatterTransmitterData data = (MatterTransmitterData)this.getData(TeleporterModule.MATTERTRANSMITTER_DATA);
      data = data.withDestination(TeleportDestination.INVALID);
      data = data.withDestinationId(null);
      data = data.withOnce(once);
      if (teleportDestination != null) {
         TeleportDestinations destinations = TeleportDestinations.get(this.level);
         Integer id = destinations.getIdForCoordinate(GlobalPos.of(teleportDestination.getDimension(), teleportDestination.getCoordinate()));
         if (id == null) {
            data = data.withDestination(teleportDestination);
         } else {
            data = data.withDestinationId(id);
         }
      }

      this.setData(TeleporterModule.MATTERTRANSMITTER_DATA, data);
      this.markDirtyClient();
   }

   private void consumeIdlePower() {
      if ((Integer)TeleportConfiguration.rfMatterIdleTick.get() > 0 && this.teleportingPlayer == null) {
         if (this.energyStorage.getEnergyStored() >= (Integer)TeleportConfiguration.rfMatterIdleTick.get()) {
            this.energyStorage.consumeEnergy(((Integer)TeleportConfiguration.rfMatterIdleTick.get()).intValue());
         } else {
            this.setTeleportDestination(null, false);
         }
      }
   }

   protected void tickServer() {
      if (this.isDialed()) {
         this.consumeIdlePower();
         this.checkReceiverStatusCounter--;
         if (this.checkReceiverStatusCounter <= 0) {
            this.checkReceiverStatusCounter = 20;
            int newstatus;
            if (DialingDeviceTileEntity.isDestinationAnalyzerAvailable(this.level, this.getBlockPos())) {
               newstatus = this.checkReceiverStatus();
            } else {
               newstatus = 0;
            }

            if (newstatus != this.status) {
               this.status = newstatus;
               this.markDirtyClient();
            }
         }
      }

      MatterTransmitterData data = (MatterTransmitterData)this.getData(TeleporterModule.MATTERTRANSMITTER_DATA);
      if (!this.isCoolingDown()) {
         if (this.teleportingPlayer == null) {
            if (this.isDestinationValid()) {
               this.searchForNearestPlayer();
            }
         } else if (data.destination() == null && data.destinationId() == null) {
            Player player = this.level.getServer().getPlayerList().getPlayer(this.teleportingPlayer);
            if (player != null) {
               Logging.warn(player, "The destination vanished! Aborting.");
            }

            this.clearTeleport(80);
         } else if (this.isPlayerOutsideBeam()) {
            this.clearTeleport(80);
         } else {
            int rf = this.rfPerTick;
            if (this.energyStorage.getEnergyStored() < rf) {
               this.handleEnergyShortage();
            } else {
               this.setChanged();
               this.energyStorage.consumeEnergy(rf);
               this.goodTicks++;
               this.teleportTimer--;
               if (this.teleportTimer <= 0) {
                  this.performTeleport();
               }
            }
         }
      }
   }

   private int checkReceiverStatus() {
      TeleportDestination destination = this.getTeleportDestination();
      if (destination == null) {
         return 1;
      } else {
         ResourceKey<Level> dimension = destination.getDimension();
         int powerPercentage = RFToolsDimCompat.getPowerPercentage(this.level, dimension.identifier());
         if (powerPercentage >= 0 && powerPercentage < (Integer)TeleportConfiguration.DIMENSION_WARN_PERCENTAGE.get()) {
            return 1;
         } else {
            Level w = LevelTools.getLevel(this.level, dimension);
            if (w == null) {
               if ((Integer)TeleportConfiguration.matterTransmitterLoadWorld.get() == -1) {
                  return 2;
               }

               w = LevelTools.getLevel(dimension);
               this.checkReceiverStatusCounter = (Integer)TeleportConfiguration.matterTransmitterLoadWorld.get();
            }

            BlockPos c = destination.getCoordinate();
            boolean exists = LevelTools.isLoaded(w, c);
            if (!exists) {
               if ((Integer)TeleportConfiguration.matterTransmitterLoadChunk.get() == -1) {
                  return 2;
               }

               this.checkReceiverStatusCounter = (Integer)TeleportConfiguration.matterTransmitterLoadChunk.get();
            }

            if (w.getBlockEntity(c) instanceof MatterReceiverTileEntity receiver) {
               int status = receiver.checkStatus();
               return status == 0 ? 0 : 1;
            } else {
               return 1;
            }
         }
      }
   }

   private void clearTeleport(int cooldown) {
      this.setChanged();
      TeleportationTools.applyBadEffectIfNeeded(
         this.level.getServer().getPlayerList().getPlayer(this.teleportingPlayer), 0, this.badTicks, this.totalTicks, false
      );
      this.cooldownTimer = cooldown;
      this.teleportingPlayer = null;
   }

   private boolean isDestinationValid() {
      MatterTransmitterData data = (MatterTransmitterData)this.getData(TeleporterModule.MATTERTRANSMITTER_DATA);
      return data.destinationId() != null || data.destination() != null && data.destination().isValid();
   }

   private boolean isCoolingDown() {
      this.setChanged();
      this.cooldownTimer--;
      if (this.cooldownTimer <= 0) {
         this.cooldownTimer = 0;
         return false;
      } else {
         return true;
      }
   }

   private AABB createBeamBox() {
      int xCoord = this.getBlockPos().getX();
      int yCoord = this.getBlockPos().getY();
      int zCoord = this.getBlockPos().getZ();
      return new AABB(xCoord, yCoord + 1, zCoord, xCoord + 1, yCoord + 3, zCoord + 1);
   }

   private void searchForNearestPlayer() {
      List<Player> l = this.level.getEntitiesOfClass(Player.class, (AABB)this.beamBox.get());
      Entity nearestPlayer = this.findNearestPlayer(l);
      if (nearestPlayer == null) {
         this.cooldownTimer = 5;
      } else {
         AABB playerBB = nearestPlayer.getBoundingBox();
         if (playerBB.intersects((AABB)this.beamBox.get())) {
            this.startTeleportation(nearestPlayer);
         } else {
            this.cooldownTimer = 5;
         }
      }
   }

   private Entity findNearestPlayer(List<Player> l) {
      Entity nearestPlayer = null;
      double dmax = Double.MAX_VALUE;

      for (Entity entity : l) {
         if (entity instanceof Player player && !player.isPassenger() && !player.isVehicle()) {
            MatterTransmitterData data = (MatterTransmitterData)this.getData(TeleporterModule.MATTERTRANSMITTER_DATA);
            if (!this.isPrivateAccess() || data.players().contains(player.getDisplayName().getString())) {
               double d1 = entity.distanceToSqr(this.getBlockPos().getX() + 0.5, this.getBlockPos().getY() + 1.5, this.getBlockPos().getZ() + 0.5);
               if (d1 <= dmax) {
                  nearestPlayer = entity;
                  dmax = d1;
               }
            }
         }
      }

      return nearestPlayer;
   }

   private void performTeleport() {
      if (!this.isDestinationStillValid()) {
         ServerPlayer player = this.level.getServer().getPlayerList().getPlayer(this.teleportingPlayer);
         if (player != null) {
            TeleportationTools.applyBadEffectIfNeeded(player, 10, this.badTicks, this.totalTicks, false);
            Logging.warn(player, "Missing destination!");
         }

         this.clearTeleport(200);
      } else {
         TeleportDestination dest = this.getTeleportDestination();
         MatterTransmitterData data = (MatterTransmitterData)this.getData(TeleporterModule.MATTERTRANSMITTER_DATA);
         if (data.once()) {
            this.setTeleportDestination(null, false);
         }

         boolean boosted = DialingDeviceTileEntity.isMatterBoosterAvailable(this.level, this.getBlockPos());
         if (boosted && this.energyStorage.getEnergyStored() < (Integer)TeleportConfiguration.rfBoostedTeleport.get()) {
            boosted = false;
         }

         Player player = this.level.getServer().getPlayerList().getPlayer(this.teleportingPlayer);
         if (player != null) {
            boolean boostNeeded = TeleportationTools.performTeleport(player, dest, this.badTicks, this.totalTicks, boosted);
            if (boostNeeded) {
               this.energyStorage.consumeEnergy(((Integer)TeleportConfiguration.rfBoostedTeleport.get()).intValue());
            }
         }

         this.teleportingPlayer = null;
      }
   }

   private boolean isDestinationStillValid() {
      TeleportDestination dest = this.getTeleportDestination();
      return TeleportDestinations.get(this.level).isDestinationValid(dest);
   }

   private void handleEnergyShortage() {
      this.setChanged();
      this.badTicks++;
      if (TeleportationTools.mustInterrupt(this.badTicks, this.totalTicks)) {
         Player player = this.level.getServer().getPlayerList().getPlayer(this.teleportingPlayer);
         if (player != null) {
            Logging.warn(player, "Power failure during transit!");
         }

         this.clearTeleport(200);
      }
   }

   private boolean isPlayerOutsideBeam() {
      Player player = this.level.getServer().getPlayerList().getPlayer(this.teleportingPlayer);
      if (player == null) {
         return true;
      } else {
         AABB playerBB = player.getBoundingBox();
         if (!playerBB.intersects((AABB)this.beamBox.get())) {
            Logging.message(player, "Teleportation was interrupted!");
            return true;
         } else {
            return false;
         }
      }
   }

   public void startTeleportation(Entity entity) {
      if (this.cooldownTimer <= 0) {
         if (this.teleportingPlayer == null) {
            if (entity instanceof Player player) {
               if (!player.isPassenger() && !player.isVehicle()) {
                  MatterTransmitterData data = (MatterTransmitterData)this.getData(TeleporterModule.MATTERTRANSMITTER_DATA);
                  TeleportDestination dest = data.destination();
                  if (data.destinationId() != null) {
                     dest = this.getTeleportDestination();
                  }

                  if (dest != null && dest.isValid()) {
                     int defaultCost = TeleportationTools.calculateRFCost(this.level, this.getBlockPos(), dest);
                     int cost = (int)(defaultCost * (4.0F - this.infusable.getInfusedFactor()) / 4.0F);
                     if (this.energyStorage.getEnergyStored() < cost) {
                        Logging.warn(player, "Not enough power to start the teleport!");
                        this.cooldownTimer = 80;
                        return;
                     }

                     ResourceKey<Level> srcId = this.level.dimension();
                     ResourceKey<Level> dstId = dest.getDimension();
                     if (!TeleportationTools.checkValidTeleport(player, srcId, dstId)) {
                        this.cooldownTimer = 80;
                        return;
                     }

                     Logging.message(player, "Start teleportation...");
                     this.teleportingPlayer = player.getUUID();
                     int defaultTeleportTimer = TeleportationTools.calculateTime(this.level, this.getBlockPos(), dest);
                     int teleportTimer = (int)(defaultTeleportTimer * (1.2F - this.infusable.getInfusedFactor()) / 1.2F);
                     int defaultRf = (Integer)TeleportConfiguration.rfTeleportPerTick.get();
                     int rf = (int)(defaultRf * (4.0F - this.infusable.getInfusedFactor()) / 4.0F);
                     int totalRfUsed = cost + rf * (teleportTimer + 1);
                     this.rfPerTick = totalRfUsed / (teleportTimer + 1);
                     this.totalTicks = teleportTimer;
                     this.goodTicks = 0;
                     this.badTicks = 0;
                  } else {
                     Logging.warn(player, "Something is wrong with the destination!");
                  }
               } else {
                  this.cooldownTimer = 80;
               }
            }
         }
      }
   }

   @Nonnull
   private IMachineInformation createMachineInfo() {
      return new IMachineInformation() {
         private final String[] TAGS;
         private final String[] TAG_DESCRIPTIONS;

         {
            Objects.requireNonNull(MatterTransmitterTileEntity.this);
            this.TAGS = new String[]{"dim", "coord", "name"};
            this.TAG_DESCRIPTIONS = new String[]{
               "The dimension this transmitter is dialed too", "The coordinate this transmitter is dialed too", "The name of the destination"
            };
         }

         public int getTagCount() {
            return this.TAGS.length;
         }

         public String getTagName(int index) {
            return this.TAGS[index];
         }

         public String getTagDescription(int index) {
            return this.TAG_DESCRIPTIONS[index];
         }

         public String getData(int index, long millis) {
            TeleportDestination destination = MatterTransmitterTileEntity.this.getTeleportDestination();
            if (destination == null) {
               return "<not dialed>";
            } else {
               return switch (index) {
                  case 0 -> destination.getDimension().identifier().toString();
                  case 1 -> destination.getCoordinate().toString();
                  case 2 -> destination.getName();
                  default -> null;
               };
            }
         }
      };
   }
}
