package mcjty.rftoolsutility.modules.screen.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.infusable.DefaultInfusable;
import mcjty.lib.api.infusable.IInfusable;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.rftoolsutility.modules.screen.ScreenConfiguration;
import mcjty.rftoolsutility.modules.screen.ScreenModule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.Lazy;

public class ScreenControllerTileEntity extends TickingTileEntity {
   public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(0).playerSlots(10, 70));
   public static final String COMPONENT_NAME = "screen_controller";
   private final GenericEnergyStorage energyStorage = new GenericEnergyStorage(
      this,
      true,
      ((Integer)ScreenConfiguration.CONTROLLER_MAXENERGY.get()).intValue(),
      ((Integer)ScreenConfiguration.CONTROLLER_RECEIVEPERTICK.get()).intValue()
   );
   @Cap(type = CapType.ENERGY)
   private static final Function<ScreenControllerTileEntity, GenericEnergyStorage> ENERGY_CAP = tile -> tile.energyStorage;
   private final IInfusable infusable = new DefaultInfusable(this);
   @Cap(type = CapType.INFUSABLE)
   private static final Function<ScreenControllerTileEntity, IInfusable> INFUSABLE_CAP = tile -> tile.infusable;
   @Cap(type = CapType.CONTAINER)
   private static final Function<ScreenControllerTileEntity, MenuProvider> screenHandler = be -> new DefaultContainerProvider("Screen Controller")
      .containerSupplier(DefaultContainerProvider.container(ScreenModule.CONTAINER_SCREEN_CONTROLLER, CONTAINER_FACTORY, be))
      .energyHandler(() -> be.energyStorage)
      .setupSync(be);
   private List<BlockPos> connectedScreens = new ArrayList<>();
   private int tickCounter = 20;
   @ServerCommand
   public static final Command<?> CMD_SCAN = Command.create("scan", (te, player, params) -> te.scan());
   @ServerCommand
   public static final Command<?> CMD_DETACH = Command.create("detach", (te, player, params) -> te.detach());

   public ScreenControllerTileEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ScreenModule.SCREEN_CONTROLLER.be().get(), pos, state);
   }

   public void loadAdditional(ValueInput tag) {
      super.loadAdditional(tag);
      int[] xes = tag.getIntArray("screensx").orElseGet(() -> new int[0]);
      int[] yes = tag.getIntArray("screensy").orElseGet(() -> new int[0]);
      int[] zes = tag.getIntArray("screensz").orElseGet(() -> new int[0]);
      this.connectedScreens.clear();

      for (int i = 0; i < xes.length; i++) {
         this.connectedScreens.add(new BlockPos(xes[i], yes[i], zes[i]));
      }

      this.energyStorage.setEnergy(tag.getLongOr("Energy", 0L));
   }

   public void saveAdditional(ValueOutput tag) {
      super.saveAdditional(tag);
      int[] xes = new int[this.connectedScreens.size()];
      int[] yes = new int[this.connectedScreens.size()];
      int[] zes = new int[this.connectedScreens.size()];

      for (int i = 0; i < this.connectedScreens.size(); i++) {
         BlockPos c = this.connectedScreens.get(i);
         xes[i] = c.getX();
         yes[i] = c.getY();
         zes[i] = c.getZ();
      }

      tag.putIntArray("screensx", xes);
      tag.putIntArray("screensy", yes);
      tag.putIntArray("screensz", zes);
      tag.putLong("Energy", this.energyStorage.getEnergy());
   }

   protected void tickServer() {
      this.tickCounter--;
      if (this.tickCounter <= 0) {
         this.tickCounter = 20;
         long rf = this.energyStorage.getEnergy();
         long rememberRf = rf;
         boolean fixesAreNeeded = false;

         for (BlockPos c : this.connectedScreens) {
            if (this.level.getBlockEntity(c) instanceof ScreenTileEntity screen) {
               int rfModule = screen.getTotalRfPerTick() * 20;
               if (rfModule > rf) {
                  screen.setPower(false);
               } else {
                  rf -= rfModule;
                  screen.setPower(true);
               }
            } else {
               fixesAreNeeded = true;
            }
         }

         if (rf < rememberRf) {
            this.energyStorage.consumeEnergy(rememberRf - rf);
         }

         if (fixesAreNeeded) {
            List<BlockPos> newScreens = new ArrayList<>();

            for (BlockPos cx : this.connectedScreens) {
               BlockEntity te = this.level.getBlockEntity(cx);
               if (te instanceof ScreenTileEntity) {
                  newScreens.add(cx);
               }
            }

            this.connectedScreens = newScreens;
            this.setChanged();
         }
      }
   }

   private void scan() {
      this.detach();
      float factor = this.infusable.getInfusedFactor();
      int radius = 32 + (int)(factor * 32.0F);
      int xCoord = this.getBlockPos().getX();
      int yCoord = this.getBlockPos().getY();
      int zCoord = this.getBlockPos().getZ();

      for (int y = yCoord - radius; y <= yCoord + radius; y++) {
         if (y >= this.level.getMinY() && y < this.level.getMaxY()) {
            for (int x = xCoord - radius; x <= xCoord + radius; x++) {
               for (int z = zCoord - radius; z <= zCoord + radius; z++) {
                  BlockPos spos = new BlockPos(x, y, z);
                  if (this.level.getBlockState(spos).getBlock() instanceof ScreenBlock
                     && this.level.getBlockEntity(spos) instanceof ScreenTileEntity ste
                     && !ste.isConnected()
                     && ste.isControllerNeeded()) {
                     this.connectedScreens.add(spos);
                     ste.setConnected(true);
                  }
               }
            }
         }
      }

      this.setChanged();
   }

   public void detach() {
      for (BlockPos c : this.connectedScreens) {
         if (this.level.getBlockEntity(c) instanceof ScreenTileEntity screen) {
            screen.setPower(false);
            screen.setConnected(false);
         }
      }

      this.connectedScreens.clear();
      this.setChanged();
   }

   public List<BlockPos> getConnectedScreens() {
      return this.connectedScreens;
   }
}
