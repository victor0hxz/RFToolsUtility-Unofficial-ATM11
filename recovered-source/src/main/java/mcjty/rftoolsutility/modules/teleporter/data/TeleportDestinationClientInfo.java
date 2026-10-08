package mcjty.rftoolsutility.modules.teleporter.data;

import java.util.function.BiConsumer;
import java.util.function.Function;
import mcjty.lib.blockcommands.ISerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TeleportDestinationClientInfo(TeleportDestination destination, String dimensionName, boolean favorite)
   implements Comparable<TeleportDestinationClientInfo> {
   public static final TeleportDestinationClientInfo DEFAULT = new TeleportDestinationClientInfo(TeleportDestination.INVALID, "", false);
   public static final StreamCodec<RegistryFriendlyByteBuf, TeleportDestinationClientInfo> STREAM_CODEC = StreamCodec.composite(
      TeleportDestination.STREAM_CODEC,
      d -> d.destination,
      ByteBufCodecs.STRING_UTF8,
      TeleportDestinationClientInfo::getDimensionName,
      ByteBufCodecs.BOOL,
      TeleportDestinationClientInfo::isFavorite,
      TeleportDestinationClientInfo::new
   );

   public TeleportDestinationClientInfo(TeleportDestination destination) {
      this(destination, "", false);
   }

   public String getDimensionName() {
      return this.dimensionName;
   }

   public TeleportDestinationClientInfo withDimensionName(String dimensionName) {
      return new TeleportDestinationClientInfo(this.destination, dimensionName, this.favorite);
   }

   public TeleportDestinationClientInfo withFavorite(boolean favorite) {
      return new TeleportDestinationClientInfo(this.destination, this.dimensionName, favorite);
   }

   public boolean isFavorite() {
      return this.favorite;
   }

   public int compareTo(TeleportDestinationClientInfo o) {
      if (this.destination.getDimension().compareTo(o.destination().getDimension()) < 0) {
         return -1;
      } else {
         return this.destination.getDimension().compareTo(o.destination.getDimension()) > 0 ? 1 : this.destination.getName().compareTo(o.destination.getName());
      }
   }

   public static class Serializer implements ISerializer<TeleportDestinationClientInfo> {
      public Function<RegistryFriendlyByteBuf, TeleportDestinationClientInfo> getDeserializer() {
         return buf -> (TeleportDestinationClientInfo)TeleportDestinationClientInfo.STREAM_CODEC.decode(buf);
      }

      public BiConsumer<RegistryFriendlyByteBuf, TeleportDestinationClientInfo> getSerializer() {
         return (buf, s) -> TeleportDestinationClientInfo.STREAM_CODEC.encode(buf, s);
      }
   }
}
