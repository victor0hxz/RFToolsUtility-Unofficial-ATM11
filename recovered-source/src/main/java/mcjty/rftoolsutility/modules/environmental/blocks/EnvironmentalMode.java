package mcjty.rftoolsutility.modules.environmental.blocks;

import com.mojang.serialization.Codec;
import mcjty.lib.varia.NamedEnum;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public enum EnvironmentalMode implements NamedEnum<EnvironmentalMode> {
   MODE_BLACKLIST("blacklist"),
   MODE_WHITELIST("whitelist"),
   MODE_HOSTILE("hostile"),
   MODE_PASSIVE("passive"),
   MODE_MOBS("mobs"),
   MODE_ALL("all");

   private final String name;
   public static final Codec<EnvironmentalMode> CODEC = StringRepresentable.fromEnum(EnvironmentalMode::values);
   public static final StreamCodec<FriendlyByteBuf, EnvironmentalMode> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(EnvironmentalMode.class);

   private EnvironmentalMode(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public String[] getDescription() {
      return new String[]{this.name};
   }

   public String getSerializedName() {
      return this.name;
   }
}
