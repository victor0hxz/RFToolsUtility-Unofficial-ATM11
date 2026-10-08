package mcjty.rftoolsutility.modules.screen.data;

import io.netty.buffer.ByteBuf;
import mcjty.rftoolsbase.api.screens.data.IModuleDataBoolean;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class ModuleDataBoolean implements IModuleDataBoolean {
   public static final String ID = "rftoolsutility:bool";
   private final boolean b;

   public String getId() {
      return "rftoolsutility:bool";
   }

   public ModuleDataBoolean(boolean b) {
      this.b = b;
   }

   public ModuleDataBoolean(ByteBuf buf) {
      this.b = buf.readBoolean();
   }

   public boolean get() {
      return this.b;
   }

   public void writeToBuf(RegistryFriendlyByteBuf buf) {
      buf.writeBoolean(this.b);
   }
}
