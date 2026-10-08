package mcjty.rftoolsutility.modules.spawner.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record SpawnerData(float matter0, float matter1, float matter2, Identifier mob) {
   public static final Codec<SpawnerData> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.FLOAT.fieldOf("matter0").forGetter(d -> d.matter0),
            Codec.FLOAT.fieldOf("matter1").forGetter(d -> d.matter1),
            Codec.FLOAT.fieldOf("matter2").forGetter(d -> d.matter2),
            Identifier.CODEC.optionalFieldOf("mob").forGetter(d -> Optional.ofNullable(d.mob))
         )
         .apply(instance, (m0, m1, m2, mob) -> new SpawnerData(m0, m1, m2, (Identifier)mob.orElse(null)))
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, SpawnerData> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.FLOAT,
      d -> d.matter0,
      ByteBufCodecs.FLOAT,
      d -> d.matter1,
      ByteBufCodecs.FLOAT,
      d -> d.matter2,
      ByteBufCodecs.optional(Identifier.STREAM_CODEC),
      d -> Optional.ofNullable(d.mob),
      (m0, m1, m2, mob) -> new SpawnerData(m0, m1, m2, (Identifier)mob.orElse(null))
   );

   public static SpawnerData createDefault() {
      return new SpawnerData(0.0F, 0.0F, 0.0F, null);
   }

   public SpawnerData withMatter0(float matter0) {
      return new SpawnerData(matter0, this.matter1, this.matter2, this.mob);
   }

   public SpawnerData withMatter1(float matter1) {
      return new SpawnerData(this.matter0, matter1, this.matter2, this.mob);
   }

   public SpawnerData withMatter2(float matter2) {
      return new SpawnerData(this.matter0, this.matter1, matter2, this.mob);
   }

   public SpawnerData withMob(Identifier mob) {
      return new SpawnerData(this.matter0, this.matter1, this.matter2, mob);
   }

   public float getMatter(int index) {
      return switch (index) {
         case 0 -> this.matter0;
         case 1 -> this.matter1;
         case 2 -> this.matter2;
         default -> 0.0F;
      };
   }

   public SpawnerData withMatter(int index, float matter) {
      return switch (index) {
         case 0 -> new SpawnerData(matter, this.matter1, this.matter2, this.mob);
         case 1 -> new SpawnerData(this.matter0, matter, this.matter2, this.mob);
         case 2 -> new SpawnerData(this.matter0, this.matter1, matter, this.mob);
         default -> this;
      };
   }
}
