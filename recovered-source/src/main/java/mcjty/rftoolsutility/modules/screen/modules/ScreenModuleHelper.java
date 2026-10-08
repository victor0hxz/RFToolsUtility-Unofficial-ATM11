package mcjty.rftoolsutility.modules.screen.modules;

import io.netty.buffer.ByteBuf;
import mcjty.rftoolsbase.api.screens.data.IModuleDataContents;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class ScreenModuleHelper {
   public static final double SMOOTHING = 0.5;
   private boolean showdiff = false;
   private long prevMillis = 0L;
   private long prevContents = 0L;
   private long lastPerTick = 0L;

   public IModuleDataContents getContentsValue(long millis, long contents, long maxContents) {
      if (this.showdiff) {
         if (this.prevMillis == 0L) {
            this.prevMillis = millis;
            this.prevContents = contents;
            return new ScreenModuleHelper.ModuleDataContents(contents, maxContents, this.lastPerTick);
         } else {
            if (millis > this.prevMillis + 500L) {
               long diffEnergy = contents - this.prevContents;
               long diff = millis - this.prevMillis;
               int ticks = (int)(diff * 20L / 1000L);
               if (ticks == 0) {
                  ticks = 1;
               }

               long measurement = diffEnergy / ticks;
               this.lastPerTick = (long)(this.lastPerTick * 0.5 + measurement * 0.5);
               this.prevMillis = millis;
               this.prevContents = contents;
            }

            return new ScreenModuleHelper.ModuleDataContents(contents, maxContents, this.lastPerTick);
         }
      } else {
         return new ScreenModuleHelper.ModuleDataContents(contents, maxContents, 0L);
      }
   }

   public void setShowdiff(boolean showdiff) {
      this.showdiff = showdiff;
   }

   public static class ModuleDataContents implements IModuleDataContents {
      public static final String ID = "rftoolsutility:contents";
      private final long contents;
      private final long maxContents;
      private final long lastPerTick;

      public String getId() {
         return "rftoolsutility:contents";
      }

      public ModuleDataContents(long contents, long maxContents, long lastPerTick) {
         this.contents = contents;
         this.maxContents = maxContents;
         this.lastPerTick = lastPerTick;
      }

      public ModuleDataContents(ByteBuf buf) {
         this.contents = buf.readLong();
         this.maxContents = buf.readLong();
         this.lastPerTick = buf.readLong();
      }

      public long getContents() {
         return this.contents;
      }

      public long getMaxContents() {
         return this.maxContents;
      }

      public long getLastPerTick() {
         return this.lastPerTick;
      }

      public void writeToBuf(RegistryFriendlyByteBuf buf) {
         buf.writeLong(this.contents);
         buf.writeLong(this.maxContents);
         buf.writeLong(this.lastPerTick);
      }
   }
}
