package mcjty.rftoolsutility.modules.teleporter.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.infusable.DefaultInfusable;
import mcjty.lib.api.infusable.IInfusable;
import mcjty.lib.api.infusable.ItemInfusable;
import mcjty.lib.api.power.ItemEnergy;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ListCommand;
import mcjty.lib.blockcommands.ResultCommand;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.setup.Registration;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.LevelTools;
import mcjty.lib.varia.OrientationTools;
import mcjty.rftoolsutility.compat.RFToolsDimCompat;
import mcjty.rftoolsutility.modules.teleporter.TeleportConfiguration;
import mcjty.rftoolsutility.modules.teleporter.TeleportationTools;
import mcjty.rftoolsutility.modules.teleporter.TeleporterModule;
import mcjty.rftoolsutility.modules.teleporter.client.GuiDialingDevice;
import mcjty.rftoolsutility.modules.teleporter.data.DialingDeviceData;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestination;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestinationClientInfo;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestinations;
import mcjty.rftoolsutility.modules.teleporter.data.TransmitterInfo;
import mcjty.rftoolsutility.playerprops.FavoriteDestinationsProperties;
import mcjty.rftoolsutility.playerprops.PlayerExtendedProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class DialingDeviceTileEntity extends GenericTileEntity {
   public static final Key<Integer> PARAM_STATUS = new Key("status", Type.INTEGER);
   public static final int DIAL_RECEIVER_BLOCKED_MASK = 1;
   public static final int DIAL_TRANSMITTER_BLOCKED_MASK = 2;
   public static final int DIAL_INVALID_DESTINATION_MASK = 4;
   public static final int DIAL_DIALER_POWER_LOW_MASK = 8;
   public static final int DIAL_RECEIVER_POWER_LOW_MASK = 16;
   public static final int DIAL_TRANSMITTER_NOACCESS = 32;
   public static final int DIAL_RECEIVER_NOACCESS = 64;
   public static final int DIAL_INTERRUPTED = 128;
   public static final int DIAL_INVALID_SOURCE_MASK = 256;
   public static final int DIAL_DIMENSION_POWER_LOW_MASK = 512;
   public static final int DIAL_INVALID_TRANSMITTER = 1024;
   public static final int DIAL_OK = 0;
   public static final String COMPONENT_NAME = "dialing_device";
   private final GenericEnergyStorage energyStorage = new GenericEnergyStorage(
      this, true, ((Integer)TeleportConfiguration.DIALER_MAXENERGY.get()).intValue(), ((Integer)TeleportConfiguration.DIALER_RECEIVEPERTICK.get()).intValue()
   );
   @Cap(type = CapType.ENERGY)
   private static final Function<DialingDeviceTileEntity, GenericEnergyStorage> ENERGY_CAP = tile -> tile.energyStorage;
   @Cap(type = CapType.CONTAINER)
   private static final Function<DialingDeviceTileEntity, MenuProvider> screenHandler = tile -> new DefaultContainerProvider("Dialing Device")
      .containerSupplier(DefaultContainerProvider.empty(TeleporterModule.CONTAINER_DIALING_DEVICE, tile))
      .energyHandler(() -> tile.energyStorage)
      .setupSync(tile);
   private final DefaultInfusable infusable = new DefaultInfusable(this);
   @Cap(type = CapType.INFUSABLE)
   private static final Function<DialingDeviceTileEntity, IInfusable> INFUSABLE_CAP = tile -> tile.infusable;
   public static final Key<UUID> PARAM_PLAYER_UUID = new Key("playerUuid", Type.UUID);
   @ServerCommand(type = TeleportDestinationClientInfo.class, serializer = TeleportDestinationClientInfo.Serializer.class)
   public static final ListCommand<?, ?> CMD_GETRECEIVERS = ListCommand.create(
      "rftoolsutility.dialer.getReceivers",
      (te, player, params) -> te.searchReceivers((UUID)params.get(PARAM_PLAYER_UUID)),
      (te, player, params, list) -> GuiDialingDevice.fromServer_receivers = list
   );
   @ServerCommand(type = TransmitterInfo.class, serializer = TransmitterInfo.Serializer.class)
   public static final ListCommand<?, ?> CMD_GETTRANSMITTERS = ListCommand.create(
      "rftoolsutility.dialer.getTransmitters",
      (te, player, params) -> te.searchTransmitters(),
      (te, player, params, list) -> GuiDialingDevice.fromServer_transmitters = list
   );
   public static final Key<String> PARAM_PLAYER = new Key("player", Type.STRING);
   public static final Key<BlockPos> PARAM_POS = new Key("pos", Type.BLOCKPOS);
   public static final Key<String> PARAM_DIMENSION = new Key("dimension", Type.STRING);
   public static final Key<BlockPos> PARAM_TRANSMITTER = new Key("transmitter", Type.BLOCKPOS);
   public static final Key<String> PARAM_TRANS_DIMENSION = new Key("transDimension", Type.STRING);
   public static final Key<Boolean> PARAM_FAVORITE = new Key("favorite", Type.BOOLEAN);
   @ServerCommand
   public static final Command<?> CMD_FAVORITE = Command.create("dialer.favorite", (te, p, params) -> {
      String player = (String)params.get(PARAM_PLAYER);
      BlockPos receiver = (BlockPos)params.get(PARAM_POS);
      String dimension = (String)params.get(PARAM_DIMENSION);
      boolean favorite = (Boolean)params.get(PARAM_FAVORITE);
      te.changeFavorite(player, receiver, LevelTools.getId(dimension), favorite);
   });
   @ServerCommand
   public static final Command<?> CMD_SHOWFAVORITE = Command.create(
      "dialer.showFavorite", (te, player, params) -> te.setShowOnlyFavorites((Boolean)params.get(PARAM_FAVORITE))
   );
   @ServerCommand
   public static final ResultCommand<?> CMD_CHECKSTATUS = ResultCommand.create("checkStatus", (te, player, params) -> {
      BlockPos c = (BlockPos)params.get(PARAM_POS);
      String dim = (String)params.get(PARAM_DIMENSION);
      return TypedMap.builder().put(PARAM_STATUS, te.checkStatus(c, LevelTools.getId(dim))).build();
   }, (te, player, params) -> GuiDialingDevice.setReceiverStatus((Integer)params.get(PARAM_STATUS)));
   @ServerCommand
   public static final ResultCommand<?> CMD_DIAL = ResultCommand.create("dial", (te, player, params) -> {
      UUID playerUUID = (UUID)params.get(PARAM_PLAYER_UUID);
      BlockPos transmitter = (BlockPos)params.get(PARAM_TRANSMITTER);
      String transDim = (String)params.get(PARAM_TRANS_DIMENSION);
      BlockPos c = (BlockPos)params.get(PARAM_POS);
      String dim = (String)params.get(PARAM_DIMENSION);
      return TypedMap.builder().put(PARAM_STATUS, te.dial(playerUUID, transmitter, LevelTools.getId(transDim), c, LevelTools.getId(dim), false)).build();
   }, (te, player, params) -> GuiDialingDevice.setDialResult((Integer)params.get(PARAM_STATUS)));
   @ServerCommand
   public static final ResultCommand<?> CMD_DIALONCE = ResultCommand.create("dialOnce", (te, player, params) -> {
      UUID playerUUID = (UUID)params.get(PARAM_PLAYER_UUID);
      BlockPos transmitter = (BlockPos)params.get(PARAM_TRANSMITTER);
      String transDim = (String)params.get(PARAM_TRANS_DIMENSION);
      BlockPos c = (BlockPos)params.get(PARAM_POS);
      String dim = (String)params.get(PARAM_DIMENSION);
      return TypedMap.builder().put(PARAM_STATUS, te.dial(playerUUID, transmitter, LevelTools.getId(transDim), c, LevelTools.getId(dim), true)).build();
   }, (te, player, params) -> GuiDialingDevice.setDialResult((Integer)params.get(PARAM_STATUS)));

   public DialingDeviceTileEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)TeleporterModule.DIALING_DEVICE.be().get(), pos, state);
   }

   public static String calculateDistance(Level world, TransmitterInfo transmitterInfo, TeleportDestination teleportDestination) {
      if (!world.dimension().equals(teleportDestination.getDimension())) {
         return "dimension warp";
      } else {
         BlockPos c1 = transmitterInfo.getCoordinate();
         BlockPos c2 = teleportDestination.getCoordinate();
         double dist = new Vec3(c1.getX(), c1.getY(), c1.getZ()).distanceTo(new Vec3(c2.getX(), c2.getY(), c2.getZ()));
         return Integer.toString((int)dist);
      }
   }

   public static boolean isMatterBoosterAvailable(Level world, BlockPos pos) {
      for (Direction facing : OrientationTools.DIRECTION_VALUES) {
         if (((MatterBoosterBlock)TeleporterModule.MATTER_BOOSTER.get()).equals(world.getBlockState(pos.relative(facing)).getBlock())) {
            return true;
         }
      }

      return false;
   }

   public static boolean isDestinationAnalyzerAvailable(Level world, BlockPos pos) {
      for (Direction facing : OrientationTools.DIRECTION_VALUES) {
         if (((DestinationAnalyzerBlock)TeleporterModule.DESTINATION_ANALYZER.get()).equals(world.getBlockState(pos.relative(facing)).getBlock())) {
            return true;
         }
      }

      return false;
   }

   public DefaultInfusable getInfusable() {
      return this.infusable;
   }

   public boolean isShowOnlyFavorites() {
      return ((DialingDeviceData)this.getData(TeleporterModule.DIALINGDEVICE_DATA)).showFav();
   }

   public void setShowOnlyFavorites(boolean showOnlyFavorites) {
      DialingDeviceData data = (DialingDeviceData)this.getData(TeleporterModule.DIALINGDEVICE_DATA);
      data = data.withShowFav(showOnlyFavorites);
      this.setData(TeleporterModule.DIALINGDEVICE_DATA, data);
   }

   public void loadAdditional(ValueInput tag) {
      super.loadAdditional(tag);
      this.energyStorage.load(tag, "energy");
      this.infusable.load(tag, "infusable");
   }

   public void saveAdditional(ValueOutput tag) {
      super.saveAdditional(tag);
      this.energyStorage.save(tag, "energy");
      this.infusable.save(tag, "infusable");
   }

   protected void applyImplicitComponents(DataComponentGetter input) {
      super.applyImplicitComponents(input);
      DialingDeviceData data = (DialingDeviceData)input.get(TeleporterModule.ITEM_DIALINGDEVICE_DATA);
      if (data != null) {
         this.setData(TeleporterModule.DIALINGDEVICE_DATA, data);
      }

      this.energyStorage.applyImplicitComponents((ItemEnergy)input.get(Registration.ITEM_ENERGY));
      this.infusable.applyImplicitComponents((ItemInfusable)input.get(Registration.ITEM_INFUSABLE));
   }

   protected void collectImplicitComponents(Builder builder) {
      super.collectImplicitComponents(builder);
      builder.set(TeleporterModule.ITEM_DIALINGDEVICE_DATA, (DialingDeviceData)this.getData(TeleporterModule.DIALINGDEVICE_DATA));
      this.energyStorage.collectImplicitComponents(builder);
      this.infusable.collectImplicitComponents(builder);
   }

   private List<TeleportDestinationClientInfo> searchReceivers(UUID player) {
      TeleportDestinations destinations = TeleportDestinations.get(this.level);
      return new ArrayList<>(destinations.getValidDestinations(this.level, player));
   }

   public List<TransmitterInfo> searchTransmitters() {
      int x = this.getBlockPos().getX();
      int y = this.getBlockPos().getY();
      int z = this.getBlockPos().getZ();
      int hrange = (Integer)TeleportConfiguration.horizontalDialerRange.get();
      int vrange = (Integer)TeleportConfiguration.verticalDialerRange.get();
      List<TransmitterInfo> transmitters = new ArrayList<>();

      for (int dy = -vrange; dy <= vrange; dy++) {
         int yy = y + dy;
         if (yy >= this.level.getMinY() && yy < this.level.getMaxY()) {
            for (int dz = -hrange; dz <= hrange; dz++) {
               int zz = z + dz;

               for (int dx = -hrange; dx <= hrange; dx++) {
                  int xx = x + dx;
                  if (dx != 0 || dy != 0 || dz != 0) {
                     BlockPos c = new BlockPos(xx, yy, zz);
                     BlockState state = this.level.getBlockState(c);
                     if (this.level.getBlockEntity(c) instanceof MatterTransmitterTileEntity transmitter) {
                        transmitters.add(new TransmitterInfo(c, transmitter.getName(), transmitter.getTeleportDestination()));
                     }
                  }
               }
            }
         }
      }

      return transmitters;
   }

   private void changeFavorite(String playerName, BlockPos receiver, ResourceKey<Level> dimension, boolean favorite) {
      for (ServerPlayer entity : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
         if (playerName.equals(entity.getName().getString())) {
            FavoriteDestinationsProperties favoriteDestinations = PlayerExtendedProperties.getFavoriteDestinations(entity);
            if (favoriteDestinations != null) {
               favoriteDestinations.setDestinationFavorite(GlobalPos.of(dimension, receiver), favorite);
               PlayerExtendedProperties.setFavoriteDestinations(entity, favoriteDestinations);
            }

            return;
         }
      }
   }

   private int dial(UUID player, BlockPos transmitter, ResourceKey<Level> transDim, BlockPos coordinate, ResourceKey<Level> dimension, boolean once) {
      return TeleportationTools.dial(this.level, this, player, transmitter, transDim, coordinate, dimension, once);
   }

   private int checkStatus(BlockPos c, ResourceKey<Level> dim) {
      int defaultCost = (Integer)TeleportConfiguration.rfPerCheck.get();
      int cost = (int)(defaultCost * (2.0F - this.infusable.getInfusedFactor()) / 2.0F);
      int s;
      if (this.energyStorage.getEnergy() < cost) {
         s = 8;
      } else {
         this.energyStorage.consumeEnergy(cost);
         s = 0;
      }

      if (s != 0) {
         return s;
      } else {
         Level w = LevelTools.getLevel(dim);
         if (w == null) {
            TeleportDestinations destinations = TeleportDestinations.get(this.level);
            destinations.cleanupInvalid();
            return 4;
         } else if (w.getBlockEntity(c) instanceof MatterReceiverTileEntity matterReceiver) {
            int var11 = RFToolsDimCompat.getPowerPercentage(this.level, dim.identifier());
            System.out.println("powerPercentage = " + var11);
            return var11 >= 0 && var11 < TeleportConfiguration.DIMENSION_WARN_PERCENTAGE.get() ? 512 : matterReceiver.checkStatus();
         } else {
            TeleportDestinations destinations = TeleportDestinations.get(this.level);
            destinations.cleanupInvalid();
            return 4;
         }
      }
   }
}
