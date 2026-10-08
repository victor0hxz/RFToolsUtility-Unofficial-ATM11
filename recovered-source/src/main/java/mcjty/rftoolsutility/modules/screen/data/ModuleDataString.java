package mcjty.rftoolsutility.modules.screen.data;

import io.netty.buffer.ByteBuf;
import mcjty.rftoolsbase.api.screens.data.IModuleDataString;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class ModuleDataString implements IModuleDataString {
   public static final String ID = "rftoolsutility:string";
   private final String s;

   public String getId() {
      return "rftoolsutility:string";
   }

   public ModuleDataString(String s) {
      this.s = s;
   }

   public ModuleDataString(ByteBuf buf) {
      this.s = ((FriendlyByteBuf)buf).readUtf(32767);
   }

   public String get() {
      return this.s;
   }

   public void writeToBuf(RegistryFriendlyByteBuf buf) {
      buf.writeUtf(this.s);
   }
}
