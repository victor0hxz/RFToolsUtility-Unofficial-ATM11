package mcjty.rftoolsutility.setup;

import java.util.List;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;

public final class ClientSetup {
   private ClientSetup() {
   }

   public static List<Identifier> onTextureStitch() {
      return List.of();
   }

   public static void registerRangeItemModelProperties(RegisterRangeSelectItemModelPropertyEvent event) {
      event.register(Identifier.fromNamespaceAndPath("rftoolsutility", "charge"), ClientItemProperties.PorterCharge.MAP_CODEC);
      event.register(Identifier.fromNamespaceAndPath("rftoolsutility", "level"), ClientItemProperties.SyringeLevel.MAP_CODEC);
   }
}
