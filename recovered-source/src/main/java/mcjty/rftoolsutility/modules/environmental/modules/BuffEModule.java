package mcjty.rftoolsutility.modules.environmental.modules;

import java.util.ArrayList;
import mcjty.rftoolsutility.modules.environmental.blocks.EnvironmentalControllerTileEntity;
import mcjty.rftoolsutility.playerprops.BuffProperties;
import mcjty.rftoolsutility.playerprops.PlayerBuff;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class BuffEModule implements EnvironmentModule {
   public static final int MAXTICKS = 180;
   private boolean active = false;
   private int ticks = 180;
   private final PlayerBuff buff;

   public BuffEModule(PlayerBuff buff) {
      this.buff = buff;
   }

   public boolean isActive() {
      return this.active;
   }

   @Override
   public void tick(Level world, BlockPos pos, int radius, int miny, int maxy, EnvironmentalControllerTileEntity controllerTileEntity) {
      if (this.active) {
         this.ticks--;
         if (this.ticks <= 0) {
            this.ticks = 180;
            double maxsqdist = radius * radius;

            for (Player player : new ArrayList(world.players())) {
               double py = player.getY();
               if (py >= miny && py <= maxy) {
                  double px = player.getX();
                  double pz = player.getZ();
                  double sqdist = (px - pos.getX()) * (px - pos.getX()) + (pz - pos.getZ()) * (pz - pos.getZ());
                  if (sqdist < maxsqdist && controllerTileEntity.isPlayerAffected(player)) {
                     BuffProperties.addBuffToPlayer(player, this.buff, 180);
                  }
               }
            }
         }
      }
   }

   @Override
   public boolean apply(Level world, BlockPos pos, LivingEntity entity, int duration) {
      return false;
   }

   @Override
   public void activate(boolean a) {
      if (this.active != a) {
         this.active = a;
         this.ticks = 1;
      }
   }

   public static BuffEModule create(PlayerBuff buff, final double rfPerTick) {
      return new BuffEModule(buff) {
         @Override
         public float getRfPerTick() {
            return (float)rfPerTick;
         }
      };
   }
}
