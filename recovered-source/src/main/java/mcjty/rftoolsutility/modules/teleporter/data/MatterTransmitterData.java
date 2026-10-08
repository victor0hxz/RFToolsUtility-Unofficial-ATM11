package mcjty.rftoolsutility.modules.teleporter.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.CompositeStreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;

public record MatterTransmitterData(
   TeleportDestination destination, Integer destinationId, boolean beamHidden, String name, boolean once, boolean privateAccess, Set<String> players
) {
   public static final Codec<MatterTransmitterData> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            TeleportDestination.CODEC.fieldOf("destination").forGetter(MatterTransmitterData::destination),
            Codec.INT.optionalFieldOf("destinationId").forGetter(d -> Optional.ofNullable(d.destinationId)),
            Codec.BOOL.fieldOf("beamHidden").forGetter(MatterTransmitterData::beamHidden),
            Codec.STRING.optionalFieldOf("name").forGetter(d -> Optional.ofNullable(d.name)),
            Codec.BOOL.fieldOf("once").forGetter(MatterTransmitterData::once),
            Codec.BOOL.fieldOf("privateAccess").forGetter(MatterTransmitterData::privateAccess),
            Codec.list(Codec.STRING).fieldOf("players").forGetter(d -> new ArrayList<>(d.players()))
         )
         .apply(
            instance,
            (dest, destId, beam, name, once, priv, players) -> new MatterTransmitterData(
               dest, (Integer)destId.orElse(null), beam, (String)name.orElse(null), once, priv, new HashSet<>(players)
            )
         )
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, MatterTransmitterData> STREAM_CODEC = CompositeStreamCodec.composite(
      TeleportDestination.STREAM_CODEC,
      d -> d.destination,
      ByteBufCodecs.optional(ByteBufCodecs.INT),
      d -> Optional.ofNullable(d.destinationId),
      ByteBufCodecs.BOOL,
      d -> d.beamHidden,
      ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
      d -> Optional.ofNullable(d.name),
      ByteBufCodecs.BOOL,
      d -> d.once,
      ByteBufCodecs.BOOL,
      d -> d.privateAccess,
      ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
      d -> new ArrayList<>(d.players),
      (dest, destId, beam, name, once, priv, players) -> new MatterTransmitterData(
         dest, (Integer)destId.orElse(null), beam, (String)name.orElse(null), once, priv, new HashSet<>(players)
      )
   );

   public static MatterTransmitterData createDefault() {
      return new MatterTransmitterData(new TeleportDestination(BlockPosTools.INVALID, Level.OVERWORLD), null, false, null, false, false, Collections.emptySet());
   }

   public MatterTransmitterData withDestination(TeleportDestination destination) {
      return new MatterTransmitterData(destination, this.destinationId, this.beamHidden, this.name, this.once, this.privateAccess, this.players);
   }

   public MatterTransmitterData withDestinationId(Integer destinationId) {
      return new MatterTransmitterData(this.destination, destinationId, this.beamHidden, this.name, this.once, this.privateAccess, this.players);
   }

   public MatterTransmitterData withBeamHidden(boolean beamHidden) {
      return new MatterTransmitterData(this.destination, this.destinationId, beamHidden, this.name, this.once, this.privateAccess, this.players);
   }

   public MatterTransmitterData withName(String name) {
      return new MatterTransmitterData(this.destination, this.destinationId, this.beamHidden, name, this.once, this.privateAccess, this.players);
   }

   public MatterTransmitterData withOnce(boolean once) {
      return new MatterTransmitterData(this.destination, this.destinationId, this.beamHidden, this.name, once, this.privateAccess, this.players);
   }

   public MatterTransmitterData withPrivateAccess(boolean privateAccess) {
      return new MatterTransmitterData(this.destination, this.destinationId, this.beamHidden, this.name, this.once, privateAccess, this.players);
   }

   public MatterTransmitterData withPlayers(Set<String> players) {
      return new MatterTransmitterData(this.destination, this.destinationId, this.beamHidden, this.name, this.once, this.privateAccess, players);
   }

   public MatterTransmitterData addPlayer(String player) {
      Set<String> newPlayers = new HashSet<>(this.players);
      newPlayers.add(player);
      return new MatterTransmitterData(this.destination, this.destinationId, this.beamHidden, this.name, this.once, this.privateAccess, newPlayers);
   }

   public MatterTransmitterData removePlayer(String player) {
      Set<String> newPlayers = new HashSet<>(this.players);
      newPlayers.remove(player);
      return new MatterTransmitterData(this.destination, this.destinationId, this.beamHidden, this.name, this.once, this.privateAccess, newPlayers);
   }
}
