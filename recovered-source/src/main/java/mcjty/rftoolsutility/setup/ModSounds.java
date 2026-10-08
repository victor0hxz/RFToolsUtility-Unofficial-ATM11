package mcjty.rftoolsutility.setup;

import java.util.function.Supplier;
import mcjty.lib.varia.SoundTools;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
   public static final Supplier<SoundEvent> WHOOSH = Registration.SOUNDS
      .register("teleport_whoosh", () -> SoundTools.createSoundEvent(Identifier.fromNamespaceAndPath("rftoolsutility", "teleport_whoosh")));
   public static final Supplier<SoundEvent> ERROR = Registration.SOUNDS
      .register("teleport_error", () -> SoundTools.createSoundEvent(Identifier.fromNamespaceAndPath("rftoolsutility", "teleport_error")));

   public static void init() {
   }
}
