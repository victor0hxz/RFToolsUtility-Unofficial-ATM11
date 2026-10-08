package mcjty.rftoolsutility.modules.logic.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record AnalogData(float mulEqual, float mulLess, float mulGreater, int addEqual, int addLess, int addGreater) {
   public static final Codec<AnalogData> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.FLOAT.fieldOf("mul_eq").forGetter(AnalogData::mulEqual),
            Codec.FLOAT.fieldOf("mul_less").forGetter(AnalogData::mulLess),
            Codec.FLOAT.fieldOf("mul_greater").forGetter(AnalogData::mulGreater),
            Codec.INT.fieldOf("add_eq").forGetter(AnalogData::addEqual),
            Codec.INT.fieldOf("add_less").forGetter(AnalogData::addLess),
            Codec.INT.fieldOf("add_greater").forGetter(AnalogData::addGreater)
         )
         .apply(instance, AnalogData::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, AnalogData> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.FLOAT,
      d -> d.mulEqual,
      ByteBufCodecs.FLOAT,
      d -> d.mulLess,
      ByteBufCodecs.FLOAT,
      d -> d.mulGreater,
      ByteBufCodecs.INT,
      d -> d.addEqual,
      ByteBufCodecs.INT,
      d -> d.addLess,
      ByteBufCodecs.INT,
      d -> d.addGreater,
      AnalogData::new
   );

   public static AnalogData createDefault() {
      return new AnalogData(1.0F, 1.0F, 1.0F, 0, 0, 0);
   }

   public AnalogData withMulEqual(float mulEqual) {
      return new AnalogData(mulEqual, this.mulLess, this.mulGreater, this.addEqual, this.addLess, this.addGreater);
   }

   public AnalogData withMulLess(float mulLess) {
      return new AnalogData(this.mulEqual, mulLess, this.mulGreater, this.addEqual, this.addLess, this.addGreater);
   }

   public AnalogData withMulGreater(float mulGreater) {
      return new AnalogData(this.mulEqual, this.mulLess, mulGreater, this.addEqual, this.addLess, this.addGreater);
   }

   public AnalogData withAddEqual(int addEqual) {
      return new AnalogData(this.mulEqual, this.mulLess, this.mulGreater, addEqual, this.addLess, this.addGreater);
   }

   public AnalogData withAddLess(int addLess) {
      return new AnalogData(this.mulEqual, this.mulLess, this.mulGreater, this.addEqual, addLess, this.addGreater);
   }

   public AnalogData withAddGreater(int addGreater) {
      return new AnalogData(this.mulEqual, this.mulLess, this.mulGreater, this.addEqual, this.addLess, addGreater);
   }
}
