package mcjty.rftoolsutility.modules.logic.tools;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import mcjty.lib.varia.NamedEnum;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public enum SequencerMode implements NamedEnum<SequencerMode> {
   MODE_ONCE1("Once1"),
   MODE_ONCE2("Once2"),
   MODE_LOOP1("Loop1"),
   MODE_LOOP2("Loop2"),
   MODE_LOOP3("Loop3"),
   MODE_STEP("Step"),
   MODE_LOOP4("Loop4");

   public static final Codec<SequencerMode> CODEC = StringRepresentable.fromEnum(SequencerMode::values);
   public static final StreamCodec<FriendlyByteBuf, SequencerMode> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(SequencerMode.class);
   private static final Map<String, SequencerMode> modeToMode = new HashMap<>();
   private final String name;

   private SequencerMode(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public String[] getDescription() {
      return new String[]{this.name};
   }

   public static SequencerMode getMode(String mode) {
      return modeToMode.get(mode);
   }

   public String getSerializedName() {
      return this.name;
   }

   static {
      for (SequencerMode mode : values()) {
         modeToMode.put(mode.name, mode);
      }
   }
}
