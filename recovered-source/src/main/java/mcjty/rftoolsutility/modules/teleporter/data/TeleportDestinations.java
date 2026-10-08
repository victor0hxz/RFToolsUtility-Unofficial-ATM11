package mcjty.rftoolsutility.modules.teleporter.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.LevelTools;
import mcjty.lib.varia.Logging;
import mcjty.lib.worlddata.AbstractWorldData;
import mcjty.rftoolsutility.modules.teleporter.blocks.MatterReceiverTileEntity;
import mcjty.rftoolsutility.playerprops.FavoriteDestinationsProperties;
import mcjty.rftoolsutility.playerprops.PlayerExtendedProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TeleportDestinations extends AbstractWorldData<TeleportDestinations> {
   private static final String TPDESTINATIONS_NAME = "TPDestinations";
   private final Map<GlobalPos, TeleportDestination> destinations = new HashMap<>();
   private final Map<Integer, GlobalPos> destinationById = new HashMap<>();
   private final Map<GlobalPos, Integer> destinationIdByCoordinate = new HashMap<>();
   private int lastId = 0;

   public TeleportDestinations() {
   }

   public TeleportDestinations(CompoundTag tag) {
      this.lastId = tag.getIntOr("lastId", 0);
      this.readDestinationsFromNBT(tag);
   }

   public static String getDestinationName(TeleportDestinations destinations, int receiverId) {
      GlobalPos coordinate = destinations.getCoordinateForId(receiverId);
      String name;
      if (coordinate == null) {
         name = "?";
      } else {
         TeleportDestination destination = destinations.getDestination(coordinate);
         if (destination == null) {
            name = "?";
         } else {
            name = destination.getName();
            if (name == null || name.isEmpty()) {
               name = BlockPosTools.toString(destination.getCoordinate()) + " (" + destination.getDimension().identifier().getPath() + ")";
            }
         }
      }

      return name;
   }

   public void cleanupInvalid() {
      for (GlobalPos key : new HashSet<>(this.destinations.keySet())) {
         Level transWorld = LevelTools.getLevel(key.dimension());
         boolean removed = false;
         if (transWorld == null) {
            Logging.log("Receiver on dimension " + key.dimension().identifier().getPath() + " removed because world can't be loaded!");
            removed = true;
         } else {
            BlockPos c = key.pos();

            BlockEntity te;
            try {
               te = transWorld.getBlockEntity(c);
            } catch (Exception var9) {
               te = null;
            }

            if (!(te instanceof MatterReceiverTileEntity)) {
               Logging.log("Receiver at " + c + " on dimension " + key.dimension().identifier().getPath() + " removed because there is no receiver there!");
               removed = true;
            }
         }

         if (removed) {
            this.destinations.remove(key);
         }
      }
   }

   public static TeleportDestinations get(Level world) {
      return (TeleportDestinations)getData(world, TeleportDestinations::new, TeleportDestinations::new, "TPDestinations");
   }

   public Collection<TeleportDestinationClientInfo> getValidDestinations(Level level, UUID player) {
      FavoriteDestinationsProperties properties = null;
      if (player != null) {
         MinecraftServer server = level.getServer();

         for (ServerPlayer entity : server.getPlayerList().getPlayers()) {
            if (player.equals(entity.getUUID())) {
               properties = PlayerExtendedProperties.getFavoriteDestinations(entity);
               break;
            }
         }
      }

      List<TeleportDestinationClientInfo> result = new ArrayList<>();

      for (TeleportDestination destination : this.destinations.values()) {
         TeleportDestinationClientInfo destinationClientInfo = new TeleportDestinationClientInfo(destination);
         BlockPos c = destination.getCoordinate();
         Level world = LevelTools.getLevel(destination.getDimension());
         String dimName = "<Unknown>";
         if (world != null) {
            dimName = world.dimension().identifier().getPath();
         }

         destinationClientInfo = destinationClientInfo.withDimensionName(dimName);
         if (world != null) {
            if (!destination.isAccessKnown() && world.getBlockEntity(c) instanceof MatterReceiverTileEntity receiver) {
               destination = receiver.updateDestination();
            }

            if (destination.isAccessKnown() && player != null && !destination.checkAccess(world, player)) {
               continue;
            }
         }

         if (properties != null) {
            destinationClientInfo = destinationClientInfo.withFavorite(properties.isDestinationFavorite(GlobalPos.of(destination.getDimension(), c)));
         }

         result.add(destinationClientInfo);
      }

      Collections.sort(result);
      return result;
   }

   public boolean isDestinationValid(TeleportDestination destination) {
      GlobalPos key = GlobalPos.of(destination.getDimension(), destination.getCoordinate());
      return this.destinations.containsKey(key);
   }

   public void assignId(GlobalPos key, int id) {
      this.destinationById.put(id, key);
      this.destinationIdByCoordinate.put(key, id);
   }

   public int getNewId(GlobalPos key) {
      if (this.destinationIdByCoordinate.containsKey(key)) {
         return this.destinationIdByCoordinate.get(key);
      } else {
         this.lastId++;
         this.destinationById.put(this.lastId, key);
         this.destinationIdByCoordinate.put(key, this.lastId);
         return this.lastId;
      }
   }

   public Integer getIdForCoordinate(GlobalPos key) {
      return this.destinationIdByCoordinate.get(key);
   }

   public GlobalPos getCoordinateForId(int id) {
      return this.destinationById.get(id);
   }

   public TeleportDestination addDestination(GlobalPos key) {
      if (!this.destinations.containsKey(key)) {
         TeleportDestination teleportDestination = new TeleportDestination(key.pos(), key.dimension());
         this.destinations.put(key, teleportDestination);
      }

      return this.destinations.get(key);
   }

   public void removeDestinationsInDimension(ResourceKey<Level> dimension) {
      Set<GlobalPos> keysToRemove = new HashSet<>();

      for (Entry<GlobalPos, TeleportDestination> entry : this.destinations.entrySet()) {
         if (entry.getKey().dimension().equals(dimension)) {
            keysToRemove.add(entry.getKey());
         }
      }

      for (GlobalPos key : keysToRemove) {
         this.removeDestination(key.pos(), key.dimension());
      }
   }

   public void removeDestination(BlockPos coordinate, ResourceKey<Level> dimension) {
      if (coordinate != null) {
         GlobalPos key = GlobalPos.of(dimension, coordinate);
         this.destinations.remove(key);
         Integer id = this.destinationIdByCoordinate.get(key);
         if (id != null) {
            this.destinationById.remove(id);
            this.destinationIdByCoordinate.remove(key);
         }
      }
   }

   public TeleportDestination getDestination(GlobalPos coordinate) {
      return this.destinations.get(coordinate);
   }

   public TeleportDestination getDestination(BlockPos coordinate, ResourceKey<Level> dimension) {
      return this.destinations.get(GlobalPos.of(dimension, coordinate));
   }

   public void setDestination(GlobalPos coordinate, TeleportDestination destination) {
      this.destinations.put(coordinate, destination);
   }

   private void readDestinationsFromNBT(CompoundTag tagCompound) {
      ListTag lst = tagCompound.getListOrEmpty("destinations");

      for (int i = 0; i < lst.size(); i++) {
         CompoundTag tc = (CompoundTag)lst.getCompound(i).orElseGet(CompoundTag::new);
         TeleportDestination.CODEC.decode(NbtOps.INSTANCE, tc.get("dest")).result().ifPresent(data -> {
            GlobalPos pos = ((TeleportDestination)data.getFirst()).getPos();
            this.destinations.put(pos, (TeleportDestination)data.getFirst());
            if (tc.contains("id")) {
               int id = tc.getIntOr("id", 0);
               this.destinationById.put(id, pos);
               this.destinationIdByCoordinate.put(pos, id);
            }
         });
      }
   }

   @Nonnull
   public CompoundTag save(@Nonnull CompoundTag tagCompound, Provider provider) {
      ListTag destinations = this.destinations.values().stream().map(destination -> {
         CompoundTag tag = new CompoundTag();
         TeleportDestination.CODEC.encodeStart(NbtOps.INSTANCE, destination).result().ifPresent(data -> tag.put("dest", data));
         Integer id = this.destinationIdByCoordinate.get(destination.getPos());
         if (id != null) {
            tag.putInt("id", id);
         }

         return tag;
      }).collect(Collectors.toCollection(ListTag::new));
      tagCompound.put("destinations", destinations);
      tagCompound.putInt("lastId", this.lastId);
      return tagCompound;
   }
}
