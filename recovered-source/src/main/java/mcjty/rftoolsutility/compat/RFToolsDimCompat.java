package mcjty.rftoolsutility.compat;

import java.util.function.Function;
import mcjty.rftoolsbase.api.dimension.IDimensionInformation;
import mcjty.rftoolsbase.api.dimension.IDimensionManager;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModList;

public class RFToolsDimCompat {
   private static boolean registered = false;
   public static IDimensionManager dimensionManager = null;

   public static void register() {
      if (ModList.get().isLoaded("rftoolsdim")) {
         registerInternal();
      }
   }

   private static void registerInternal() {
      if (!registered) {
         registered = true;
         InterModComms.sendTo("rftoolsdim", "getDimensionManager", RFToolsDimCompat.GetDimensionManager::new);
      }
   }

   public static int getPowerPercentage(Level world, Identifier id) {
      if (dimensionManager != null) {
         IDimensionInformation data = dimensionManager.getDimensionInformation(world, id);
         if (data != null) {
            long maxEnergy = data.getMaxEnergy(world);
            return maxEnergy <= 0L ? -1 : (int)(data.getEnergy() * 100L / maxEnergy);
         } else {
            return -1;
         }
      } else {
         return -1;
      }
   }

   public static class GetDimensionManager implements Function<IDimensionManager, Void> {
      public Void apply(IDimensionManager tm) {
         RFToolsDimCompat.dimensionManager = tm;
         return null;
      }
   }
}
