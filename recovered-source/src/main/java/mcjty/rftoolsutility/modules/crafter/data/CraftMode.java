package mcjty.rftoolsutility.modules.crafter.data;

import com.mojang.serialization.Codec;
import mcjty.lib.varia.NamedEnum;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public enum CraftMode implements NamedEnum<CraftMode> {
   EXT("Ext"),
   INT("Int"),
   EXTC("ExtC");

   private final String description;
   public static final Codec<CraftMode> CODEC = StringRepresentable.fromEnum(CraftMode::values);
   public static final StreamCodec<FriendlyByteBuf, CraftMode> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(CraftMode.class);

   private CraftMode(String description) {
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
