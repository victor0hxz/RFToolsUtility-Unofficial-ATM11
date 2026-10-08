package mcjty.rftoolsutility.modules.spawner.blocks;

import java.util.function.Function;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.container.ItemInventory;
import mcjty.lib.api.infusable.DefaultInfusable;
import mcjty.lib.api.infusable.IInfusable;
import mcjty.lib.api.infusable.ItemInfusable;
import mcjty.lib.api.power.ItemEnergy;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.network.Networking;
import mcjty.lib.network.PacketServerCommandTyped;
import mcjty.lib.setup.Registration;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.SoundTools;
import mcjty.rftoolsbase.RFToolsBase;
import mcjty.rftoolsutility.modules.spawner.SpawnerConfiguration;
import mcjty.rftoolsutility.modules.spawner.SpawnerModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.Lazy;

public class MatterBeamerTileEntity extends TickingTileEntity {
   public static final int TICKTIME = 20;
   public static final int SLOT_MATERIAL = 0;
   public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(
      () -> new ContainerFactory(1).slot(SlotDefinition.generic().in(), 0, 28, 8).playerSlots(10, 70)
   );
   private final GenericItemHandler items = GenericItemHandler.basic(this, CONTAINER_FACTORY);
   @Cap(type = CapType.ITEMS_AUTOMATION)
   private static final Function<MatterBeamerTileEntity, GenericItemHandler> ITEM_CAP = tile -> tile.items;
   private final GenericEnergyStorage energyStorage = new GenericEnergyStorage(
      this, true, SpawnerConfiguration.BEAMER_MAXENERGY, SpawnerConfiguration.BEAMER_RECEIVEPERTICK
   );
   @Cap(type = CapType.ENERGY)
   private static final Function<MatterBeamerTileEntity, GenericEnergyStorage> ENERGY_CAP = tile -> tile.energyStorage;
   @Cap(type = CapType.CONTAINER)
   private static final Function<MatterBeamerTileEntity, MenuProvider> screenHandler = be -> new DefaultContainerProvider("Matter Beamer")
      .containerSupplier(DefaultContainerProvider.container(SpawnerModule.CONTAINER_MATTER_BEAMER, CONTAINER_FACTORY, be))
      .itemHandler(() -> be.items)
      .energyHandler(() -> be.energyStorage)
      .setupSync(be);
   private final DefaultInfusable infusable = new DefaultInfusable(this);
   @Cap(type = CapType.INFUSABLE)
   private static final Function<MatterBeamerTileEntity, IInfusable> INFUSABLE_CAP = tile -> tile.infusable;
   public static final Key<BlockPos> VALUE_DESTINATION = new Key("destination", Type.BLOCKPOS);
   private BlockPos destination = null;
   private boolean glowing = false;
   private int ticker = 20;
   public static final Key<BlockPos> PARAM_DESTINATION = new Key("dest", Type.BLOCKPOS);
   @ServerCommand
   public static final Command<?> CMD_SETDESTINATION = Command.create(
      "setDestination", (te, player, params) -> te.setDestination((BlockPos)params.get(PARAM_DESTINATION))
   );

   public MatterBeamerTileEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)SpawnerModule.MATTER_BEAMER.be().get(), pos, state);
   }

   public boolean isPowered() {
      return this.powerLevel != 0;
   }

   public boolean isGlowing() {
      return this.glowing;
   }

   protected void tickServer() {
      if (this.powerLevel == 0) {
         this.disableBlockGlow();
      } else {
         this.ticker--;
         if (this.ticker <= 0) {
            this.ticker = 20;
            BlockEntity te = null;
            if (this.destination != null) {
               te = this.level.getBlockEntity(this.destination);
               if (!(te instanceof SpawnerTileEntity)) {
                  this.setDestination(null);
               } else {
                  ItemStack itemStack = this.items.getStackInSlot(0);
                  if (itemStack.isEmpty()) {
                     this.disableBlockGlow();
                  } else {
                     SpawnerTileEntity spawnerTileEntity = (SpawnerTileEntity)te;
                     int maxblocks = (int)(SpawnerConfiguration.beamBlocksPerSend * (1.01 + this.infusable.getInfusedFactor() * 2.0));
                     int numblocks = Math.min(maxblocks, itemStack.getCount());
                     int rf = (int)(SpawnerConfiguration.beamRfPerObject * numblocks * (4.0F - this.infusable.getInfusedFactor()) / 4.0F);
                     if (this.energyStorage.getEnergyStored() >= rf) {
                        this.energyStorage.consumeEnergy(rf);
                        if (spawnerTileEntity.addMatter(itemStack, numblocks, this.infusable.getInfusedFactor())) {
                           this.items.decrStackSize(0, numblocks);
                           this.enableBlockGlow();
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void disableBlockGlow() {
      if (this.glowing) {
         this.glowing = false;
         this.level.setBlock(this.worldPosition, (BlockState)this.getBlockState().setValue(BlockStateProperties.LIT, this.glowing), 11);
         this.markDirtyQuick();
      }
   }

   private void enableBlockGlow() {
      if (!this.glowing) {
         this.glowing = true;
         this.level.setBlock(this.worldPosition, (BlockState)this.getBlockState().setValue(BlockStateProperties.LIT, this.glowing), 11);
         this.markDirtyQuick();
      }
   }

   public void onDataPacket(Connection net, ValueInput input) {
      boolean oldglowing = this.glowing;
      super.onDataPacket(net, input);
      if (this.level.isClientSide() && oldglowing != this.glowing) {
         this.level.setBlock(this.worldPosition, (BlockState)this.getBlockState().setValue(BlockStateProperties.LIT, this.glowing), 11);
      }
   }

   public boolean wrenchUse(Level world, BlockPos pos, Direction side, Player player) {
      if (world.isClientSide()) {
         world.playLocalSound(
            pos.getX(),
            pos.getY(),
            pos.getZ(),
            SoundTools.findSound(Identifier.fromNamespaceAndPath("minecraft", "block.note_block.pling")),
            SoundSource.BLOCKS,
            1.0F,
            1.0F,
            false
         );
         this.useWrench(player);
      }

      return true;
   }

   private void useWrench(Player player) {
      BlockPos coord = RFToolsBase.instance.clientInfo.getSelectedTE();
      BlockEntity tileEntity = null;
      if (coord != null) {
         tileEntity = this.level.getBlockEntity(coord);
      }

      if (!(tileEntity instanceof MatterBeamerTileEntity)) {
         RFToolsBase.instance.clientInfo.setSelectedTE(this.getBlockPos());
         SpawnerTileEntity destinationTE = this.getDestinationTE();
         if (destinationTE == null) {
            RFToolsBase.instance.clientInfo.setDestinationTE(null);
         } else {
            RFToolsBase.instance.clientInfo.setDestinationTE(destinationTE.getBlockPos());
         }

         Logging.message(player, "Select a spawner as destination");
      } else if (coord.equals(this.getBlockPos())) {
         RFToolsBase.instance.clientInfo.setSelectedTE(null);
         RFToolsBase.instance.clientInfo.setDestinationTE(null);
         this.setDestination(null);
         Logging.message(player, "Destination cleared!");
      }
   }

   public void setDestination(BlockPos destination) {
      this.destination = destination;
      this.disableBlockGlow();
      this.setChanged();
      if (this.level.isClientSide()) {
         PacketServerCommandTyped packet = PacketServerCommandTyped.create(
            this.getBlockPos(), this.getDimension(), CMD_SETDESTINATION.name(), TypedMap.builder().put(PARAM_DESTINATION, destination).build()
         );
         Networking.sendToServer(packet);
      } else {
         this.setChanged();
      }
   }

   public BlockPos getDestination() {
      return this.destination;
   }

   private SpawnerTileEntity getDestinationTE() {
      if (this.destination == null) {
         return null;
      } else {
         BlockEntity te = this.level.getBlockEntity(this.destination);
         if (te instanceof SpawnerTileEntity) {
            return (SpawnerTileEntity)te;
         } else {
            this.destination = null;
            this.setChanged();
            return null;
         }
      }
   }

   protected void loadAdditional(ValueInput tag) {
      super.loadAdditional(tag);
      this.destination = BlockPosTools.read(tag, "dest");
      this.glowing = tag.getBooleanOr("glowing", false);
      this.energyStorage.load(tag, "energy");
      this.items.load(tag, "items");
      this.infusable.load(tag, "infusable");
   }

   public void saveAdditional(ValueOutput tag) {
      super.saveAdditional(tag);
      BlockPosTools.write(tag, "dest", this.destination);
      tag.putBoolean("glowing", this.glowing);
      this.energyStorage.save(tag, "energy");
      this.items.save(tag, "items");
      this.infusable.save(tag, "infusable");
   }

   protected void applyImplicitComponents(DataComponentGetter input) {
      super.applyImplicitComponents(input);
      this.energyStorage.applyImplicitComponents((ItemEnergy)input.get(Registration.ITEM_ENERGY));
      this.items.applyImplicitComponents((ItemInventory)input.get(Registration.ITEM_INVENTORY));
      this.infusable.applyImplicitComponents((ItemInfusable)input.get(Registration.ITEM_INFUSABLE));
   }

   protected void collectImplicitComponents(Builder builder) {
      super.collectImplicitComponents(builder);
      this.energyStorage.collectImplicitComponents(builder);
      this.items.collectImplicitComponents(builder);
      this.infusable.collectImplicitComponents(builder);
   }

   public void saveClientDataToNBT(CompoundTag tag, Provider provider) {
      BlockPosTools.write(tag, "dest", this.destination);
      tag.putBoolean("glowing", this.glowing);
   }

   public void loadClientDataFromNBT(CompoundTag tag, Provider provider) {
      this.destination = BlockPosTools.read(tag, "dest");
      this.glowing = tag.getBooleanOr("glowing", false);
   }
}
