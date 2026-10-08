package mcjty.rftoolsutility.modules.screen;

import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record MachineInfo(long energy, long maxEnergy, Long energyPerTick) {
   public static final StreamCodec<FriendlyByteBuf, MachineInfo> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_LONG,
      MachineInfo::energy,
      ByteBufCodecs.VAR_LONG,
      MachineInfo::maxEnergy,
      ByteBufCodecs.optional(ByteBufCodecs.VAR_LONG),
      b -> Optional.ofNullable(b.energyPerTick),
      (e, m, pt) -> new MachineInfo(e, m, (Long)pt.orElse(null))
   );
}
