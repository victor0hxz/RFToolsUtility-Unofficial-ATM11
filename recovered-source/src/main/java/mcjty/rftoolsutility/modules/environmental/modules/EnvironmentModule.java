package mcjty.rftoolsutility.modules.environmental.modules;

import mcjty.rftoolsutility.modules.environmental.blocks.EnvironmentalControllerTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public interface EnvironmentModule {
   float getRfPerTick();

   void tick(Level var1, BlockPos var2, int var3, int var4, int var5, EnvironmentalControllerTileEntity var6);

   boolean apply(Level var1, BlockPos var2, LivingEntity var3, int var4);

   void activate(boolean var1);
}
