package mcjty.rftoolsutility.compat;

import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nullable;
import mcjty.lib.varia.Logging;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IElementFactory;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ITheOneProbe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;

public class TheOneProbeSupport implements Function<ITheOneProbe, Void> {
   public static ITheOneProbe probe;
   public static Identifier ELEMENT_SEQUENCER = Identifier.fromNamespaceAndPath("rftoolsutility", "elementseq");

   @Nullable
   public Void apply(ITheOneProbe theOneProbe) {
      probe = theOneProbe;
      Logging.log("Enabled support for The One Probe");
      probe.registerElementFactory(new IElementFactory() {
         {
            Objects.requireNonNull(TheOneProbeSupport.this);
         }

         public IElement createElement(RegistryFriendlyByteBuf buf) {
            return new ElementSequencer(buf);
         }

         public Identifier getId() {
            return TheOneProbeSupport.ELEMENT_SEQUENCER;
         }
      });
      return null;
   }

   public static IProbeInfo addSequenceElement(IProbeInfo probeInfo, long bits, int current, boolean large) {
      return probeInfo.element(new ElementSequencer(bits, current, large));
   }
}
