package mcjty.rftoolsutility.modules.logic.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.rftoolsutility.modules.logic.tools.SequencerMode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SequencerData(long bits, SequencerMode sequencerMode, int delay, int stepcount, boolean endstate) {
   public static final Codec<SequencerData> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.LONG.fieldOf("bits").forGetter(SequencerData::bits),
            SequencerMode.CODEC.fieldOf("sequencerMode").forGetter(SequencerData::sequencerMode),
            Codec.INT.fieldOf("delay").forGetter(SequencerData::delay),
            Codec.INT.fieldOf("stepcount").forGetter(SequencerData::stepcount),
            Codec.BOOL.fieldOf("endstate").forGetter(SequencerData::endstate)
         )
         .apply(instance, SequencerData::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, SequencerData> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_LONG,
      SequencerData::bits,
      SequencerMode.STREAM_CODEC,
      SequencerData::sequencerMode,
      ByteBufCodecs.INT,
      SequencerData::delay,
      ByteBufCodecs.INT,
      SequencerData::stepcount,
      ByteBufCodecs.BOOL,
      SequencerData::endstate,
      SequencerData::new
   );

   public static SequencerData createDefault() {
      return new SequencerData(0L, SequencerMode.MODE_ONCE1, 1, 64, false);
   }

   public SequencerData withBits(long bits) {
      return new SequencerData(bits, this.sequencerMode, this.delay, this.stepcount, this.endstate);
   }

   public SequencerData withSequencerMode(SequencerMode sequencerMode) {
      return new SequencerData(this.bits, sequencerMode, this.delay, this.stepcount, this.endstate);
   }

   public SequencerData withDelay(int delay) {
      return new SequencerData(this.bits, this.sequencerMode, delay, this.stepcount, this.endstate);
   }

   public SequencerData withStepcount(int stepcount) {
      return new SequencerData(this.bits, this.sequencerMode, this.delay, stepcount, this.endstate);
   }

   public SequencerData withEndstate(boolean endstate) {
      return new SequencerData(this.bits, this.sequencerMode, this.delay, this.stepcount, endstate);
   }
}
