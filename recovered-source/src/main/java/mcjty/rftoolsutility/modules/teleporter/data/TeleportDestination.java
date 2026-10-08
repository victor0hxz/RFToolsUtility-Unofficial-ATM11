package mcjty.rftoolsutility.modules.teleporter.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import mcjty.lib.varia.BlockPosTools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public record TeleportDestination(GlobalPos pos, String name, boolean privateAccess, Set<String> allowedPlayers) {
   public static final TeleportDestination INVALID = new TeleportDestination(GlobalPos.of(Level.OVERWORLD, BlockPosTools.INVALID), "", false, null);
   public static final Codec<TeleportDestination> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            GlobalPos.CODEC.optionalFieldOf("pos").forGetter(d -> Optional.ofNullable(d.pos)),
            Codec.STRING.fieldOf("name").forGetter(d -> d.getName()),
            Codec.BOOL.fieldOf("privateAccess").forGetter(TeleportDestination::isPrivateAccess),
            Codec.list(Codec.STRING)
               .optionalFieldOf("allowedPlayers")
               .forGetter(d -> d.allowedPlayers == null ? Optional.empty() : Optional.of(new ArrayList<>(d.allowedPlayers)))
         )
         .apply(
            instance,
            (pos, name, priv, players) -> new TeleportDestination((GlobalPos)pos.orElse(null), name, priv, (Set<String>)players.map(HashSet::new).orElse(null))
         )
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, TeleportDestination> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.optional(GlobalPos.STREAM_CODEC),
      d -> Optional.ofNullable(d.pos),
      ByteBufCodecs.STRING_UTF8,
      d -> d.name,
      ByteBufCodecs.BOOL,
      d -> d.privateAccess,
      ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list())),
      d -> d.allowedPlayers == null ? Optional.empty() : Optional.of(new ArrayList<>(d.allowedPlayers)),
      (pos, name, priv, players) -> new TeleportDestination((GlobalPos)pos.orElse(null), name, priv, (Set<String>)players.map(HashSet::new).orElse(null))
   );

   public TeleportDestination(BlockPos coordinate, ResourceKey<Level> dimension) {
      this(GlobalPos.of(dimension, coordinate), "", false, null);
   }

   public boolean isValid() {
      return BlockPosTools.isValid(this.pos.pos());
   }

   public String getName() {
      return this.name;
   }

   public TeleportDestination withName(String name) {
      return new TeleportDestination(this.pos, name == null ? "" : name, this.privateAccess, this.allowedPlayers);
   }

   public GlobalPos getPos() {
      return this.pos;
   }

   public BlockPos getCoordinate() {
      return this.pos.pos();
   }

   public ResourceKey<Level> getDimension() {
      return this.pos.dimension();
   }

   public boolean isPrivateAccess() {
      return this.privateAccess;
   }

   @Nullable
   public Set<String> getAllowedPlayers() {
      return this.allowedPlayers;
   }

   public TeleportDestination withPrivateAccess(boolean privateAccess) {
      return new TeleportDestination(this.pos, this.name, privateAccess, this.allowedPlayers);
   }

   public boolean isAccessKnown() {
      return this.allowedPlayers != null;
   }

   public boolean checkAccess(Level level, UUID player) {
      if (!this.privateAccess) {
         return true;
      } else {
         Player playerByUuid = level.getServer().getPlayerList().getPlayer(player);
         return playerByUuid == null ? true : this.allowedPlayers.contains(playerByUuid.getDisplayName().getString());
      }
   }

   public TeleportDestination withAllowedPlayers(@Nullable Set<String> allowedPlayers) {
      return new TeleportDestination(this.pos, this.name, this.privateAccess, allowedPlayers == null ? null : new HashSet<>(allowedPlayers));
   }
}
