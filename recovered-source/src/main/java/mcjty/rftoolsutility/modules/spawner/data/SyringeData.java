package mcjty.rftoolsutility.modules.spawner.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record SyringeData(Identifier mob, int level) {
   public static final Codec<SyringeData> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("mob").forGetter(d -> Optional.ofNullable(d.mob)), Codec.INT.fieldOf("level").forGetter(d -> d.level)
         )
         .apply(instance, (mob, level) -> new SyringeData((Identifier)mob.orElse(null), level))
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, SyringeData> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.optional(Identifier.STREAM_CODEC),
      d -> Optional.ofNullable(d.mob),
      ByteBufCodecs.INT,
      d -> d.level,
      (mob, level) -> new SyringeData((Identifier)mob.orElse(null), level)
   );

   public static SyringeData createDefault() {
      return new SyringeData(null, -1);
   }

   public SyringeData withMob(Identifier mob) {
      return new SyringeData(mob, this.level);
   }

   public SyringeData withLevel(int level) {
      return new SyringeData(this.mob, level);
   }
}
