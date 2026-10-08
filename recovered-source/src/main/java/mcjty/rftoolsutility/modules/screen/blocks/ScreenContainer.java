package mcjty.rftoolsutility.modules.screen.blocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolsutility.modules.screen.ScreenModule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.util.Lazy;

public class ScreenContainer extends GenericContainer {
   public static final int SLOT_MODULES = 0;
   public static final int SCREEN_MODULES = 11;
   public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(
      () -> new ContainerFactory(11).box(SlotDefinition.generic().in(), 0, 7, 8, 1, 11).playerSlots(85, 142)
   );

   private ScreenContainer(MenuType type, int id, BlockPos pos, @Nullable GenericTileEntity te, @Nonnull Player player) {
      super(type, id, (ContainerFactory)CONTAINER_FACTORY.get(), pos, te, player);
   }

   public static ScreenContainer create(int id, BlockPos pos, @Nullable GenericTileEntity te, @Nonnull Player player) {
      return new ScreenContainer(ScreenModule.CONTAINER_SCREEN.get(), id, pos, te, player);
   }

   public static ScreenContainer createRemote(int id, BlockPos pos, @Nullable GenericTileEntity te, @Nonnull Player player) {
      return new ScreenContainer(ScreenModule.CONTAINER_SCREEN_REMOTE.get(), id, pos, te, player) {
         @Override
         protected boolean isRemoteContainer() {
            return true;
         }
      };
   }

   public static ScreenContainer createRemoteCreative(int id, BlockPos pos, @Nullable GenericTileEntity te, @Nonnull Player player) {
      return new ScreenContainer(ScreenModule.CONTAINER_SCREEN_REMOTE_CREATIVE.get(), id, pos, te, player) {
         @Override
         protected boolean isRemoteContainer() {
            return true;
         }
      };
   }

   protected boolean isRemoteContainer() {
      return false;
   }

   public boolean stillValid(@Nonnull Player player) {
      return !this.isRemoteContainer() ? super.stillValid(player) : this.be == null || !this.be.isRemoved();
   }
}
