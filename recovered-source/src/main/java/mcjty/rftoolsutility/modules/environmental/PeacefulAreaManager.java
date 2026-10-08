package mcjty.rftoolsutility.modules.environmental;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsutility.modules.environmental.blocks.EnvironmentalControllerTileEntity;
import mcjty.rftoolsutility.modules.environmental.modules.EnvironmentModule;
import mcjty.rftoolsutility.modules.environmental.modules.PeacefulEModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class PeacefulAreaManager {
   private static final Map<GlobalPos, PeacefulAreaManager.PeacefulArea> areas = new HashMap<>();

   public static void markArea(GlobalPos coordinate, int radius, int miny, int maxy) {
      if (areas.containsKey(coordinate)) {
         areas.get(coordinate).touch().setArea(radius, miny, maxy);
      } else {
         PeacefulAreaManager.PeacefulArea area = new PeacefulAreaManager.PeacefulArea(radius, miny, maxy);
         areas.put(coordinate, area);
      }
   }

   public static boolean isPeaceful(GlobalPos coordinate) {
      if (areas.isEmpty()) {
         return false;
      } else {
         List<GlobalPos> toRemove = new ArrayList<>();
         boolean peaceful = false;
         long curtime = System.currentTimeMillis() - 10000L;

         for (Entry<GlobalPos, PeacefulAreaManager.PeacefulArea> entry : areas.entrySet()) {
            PeacefulAreaManager.PeacefulArea area = entry.getValue();
            GlobalPos entryCoordinate = entry.getKey();
            if (area.in(coordinate, entryCoordinate)) {
               peaceful = true;
            }

            if (area.getLastTouched() < curtime) {
               ServerLevel world = ServerLifecycleHooks.getCurrentServer().getLevel(entryCoordinate.dimension());
               if (world != null) {
                  BlockPos c = entryCoordinate.pos();
                  if (LevelTools.isLoaded(world, c)) {
                     boolean removeArea = true;
                     if (world.getBlockEntity(c) instanceof EnvironmentalControllerTileEntity controller) {
                        for (EnvironmentModule module : controller.getEnvironmentModules()) {
                           if (module instanceof PeacefulEModule && ((PeacefulEModule)module).isActive()) {
                              removeArea = false;
                              break;
                           }
                        }
                     }

                     if (removeArea) {
                        toRemove.add(entryCoordinate);
                     }
                  }
               }
            }
         }

         for (GlobalPos globalCoordinate : toRemove) {
            areas.remove(globalCoordinate);
         }

         return peaceful;
      }
   }

   public static class PeacefulArea {
      private float sqradius;
      private int miny;
      private int maxy;
      private long lastTouched;

      public PeacefulArea(float radius, int miny, int maxy) {
         this.sqradius = radius * radius;
         this.miny = miny;
         this.maxy = maxy;
         this.touch();
      }

      public PeacefulAreaManager.PeacefulArea setArea(float radius, int miny, int maxy) {
         this.sqradius = radius * radius;
         this.miny = miny;
         this.maxy = maxy;
         return this;
      }

      @Override
      public String toString() {
         return "PeacefulArea{sqradius=" + this.sqradius + ", miny=" + this.miny + ", maxy=" + this.maxy + ", lastTouched=" + this.lastTouched + "}";
      }

      public long getLastTouched() {
         return this.lastTouched;
      }

      public PeacefulAreaManager.PeacefulArea touch() {
         this.lastTouched = System.currentTimeMillis();
         return this;
      }

      public boolean in(GlobalPos coordinate, GlobalPos thisCoordinate) {
         if (!coordinate.dimension().equals(thisCoordinate.dimension())) {
            return false;
         } else {
            double py = coordinate.pos().getY();
            if (!(py < this.miny) && !(py > this.maxy)) {
               double px = coordinate.pos().getX() - thisCoordinate.pos().getX();
               double pz = coordinate.pos().getZ() - thisCoordinate.pos().getZ();
               double sqdist = px * px + pz * pz;
               return sqdist < this.sqradius;
            } else {
               return false;
            }
         }
      }
   }
}
