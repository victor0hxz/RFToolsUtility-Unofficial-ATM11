package mcjty.rftoolsutility.modules.screen.data;

import io.netty.buffer.ByteBuf;
import mcjty.rftoolsbase.api.screens.data.IModuleDataInteger;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class ModuleDataInteger implements IModuleDataInteger {
   public static final String ID = "rftoolsutility:integer";
   public final int i;

   public String getId() {
      return "rftoolsutility:integer";
   }

   public ModuleDataInteger(int i) {
      this.i = i;
   }

   public ModuleDataInteger(ByteBuf buf) {
      this.i = buf.readInt();
   }

   public int get() {
      return this.i;
   }

   public void writeToBuf(RegistryFriendlyByteBuf buf) {
      buf.writeInt(this.i);
   }
}
