package mcjty.rftoolsutility.modules.crafter.data;

import com.mojang.serialization.Codec;
import mcjty.lib.varia.NamedEnum;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public enum SpeedMode implements NamedEnum<SpeedMode> {
   SLOW("Slow"),
   FAST("Fast");

   private final String description;
   public static final Codec<SpeedMode> CODEC = StringRepresentable.fromEnum(SpeedMode::values);
   public static final StreamCodec<FriendlyByteBuf, SpeedMode> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(SpeedMode.class);

   private SpeedMode(String description) {
      this.description = description;
   }

   public String getName() {
      return this.description;
   }

   public String[] getDescription() {
      return new String[]{this.description};
   }

   public String getSerializedName() {
      return this.description;
   }
}
