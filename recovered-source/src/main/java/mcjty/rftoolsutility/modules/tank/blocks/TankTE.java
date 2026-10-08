package mcjty.rftoolsutility.modules.tank.blocks;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nonnull;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.container.ItemInventory;
import mcjty.lib.api.fluids.ItemFluids;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.setup.Registration;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.CustomTank;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsutility.compat.RFToolsUtilityTOPDriver;
import mcjty.rftoolsutility.modules.tank.TankConfiguration;
import mcjty.rftoolsutility.modules.tank.TankModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

public class TankTE extends GenericTileEntity {
   public static final int SLOT_FILTER = 0;
   private int amount = -1;
   private Fluid clientFluid = null;
   public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(
      () -> new ContainerFactory(1).slot(SlotDefinition.specific(s -> s.getItem() instanceof BucketItem).in().out(), 0, 151, 10).playerSlots(10, 70)
   );
   private final GenericItemHandler items = GenericItemHandler.create(this, CONTAINER_FACTORY)
      .itemValid((slot, stack) -> stack.getItem() instanceof BucketItem)
      .onUpdate((slot, stack) -> this.updateFilterFluid(stack))
      .build();
   @Cap(type = CapType.ITEMS_AUTOMATION)
   private static final Function<TankTE, GenericItemHandler> ITEM_HANDLER = te -> te.items;
   private final CustomTank fluidHandler = this.createFluidHandler();
   @Cap(type = CapType.FLUIDS)
   private static final Function<TankTE, CustomTank> FLUID_HANDLER = te -> te.fluidHandler;
   @Cap(type = CapType.CONTAINER)
   private static final Function<TankTE, MenuProvider> SCREEN_CAP = be -> new DefaultContainerProvider("Tank")
      .containerSupplier(DefaultContainerProvider.container(TankModule.CONTAINER_TANK, CONTAINER_FACTORY, be))
      .itemHandler(() -> be.items)
      .setupSync(be);
   private Fluid filterFluid = null;

   public TankTE(BlockPos pos, BlockState state) {
      super((BlockEntityType)TankModule.TANK.be().get(), pos, state);
   }

   public static BaseBlock createBlock() {
      return new BaseBlock(
         new BlockBuilder()
            .topDriver(RFToolsUtilityTOPDriver.DRIVER)
            .tileEntitySupplier(TankTE::new)
            .manualEntry(ManualHelper.create("rftoolsbase:machines/tank"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsutility.shiftmessage")})
            .infoShift(
               new InfoLine[]{
                  TooltipBuilder.header(),
                  TooltipBuilder.parameter(
                     "contents", stack -> getFluidString(stack) + " (" + Integer.toString((Integer)TankConfiguration.MAXCAPACITY.get()) + " mb)"
                  )
               }
            )
      ) {
         public RotationType getRotationType() {
            return RotationType.NONE;
         }
      };
   }

   private static String getFluidString(ItemStack stack) {
      ItemFluids data = (ItemFluids)stack.get(Registration.ITEM_FLUIDS);
      if (data != null) {
         List<FluidStack> list = data.fluids();
         if (!list.isEmpty()) {
            FluidStack fluid = list.get(0);
            return fluid.getAmount() + "mb " + fluid.getHoverName().getString();
         }
      }

      return "<empty>";
   }

   public void loadAdditional(ValueInput tag) {
      super.loadAdditional(tag);
      this.amount = tag.getIntOr("level", 0);
      this.fluidHandler.load(tag, "tank");
      this.items.load(tag, "items");
   }

   public void saveAdditional(ValueOutput tag) {
      super.saveAdditional(tag);
      tag.putInt("level", this.amount);
      this.fluidHandler.save(tag, "tank");
      this.items.save(tag, "items");
   }

   protected void applyImplicitComponents(DataComponentGetter input) {
      super.applyImplicitComponents(input);
      this.fluidHandler.applyImplicitComponents((ItemFluids)input.get(Registration.ITEM_FLUIDS));
      this.items.applyImplicitComponents((ItemInventory)input.get(Registration.ITEM_INVENTORY));
   }

   protected void collectImplicitComponents(Builder builder) {
      super.collectImplicitComponents(builder);
      this.fluidHandler.collectImplicitComponents(builder);
      this.items.collectImplicitComponents(builder);
   }

   public void loadClientDataFromNBT(CompoundTag tag, Provider provider) {
      ItemFluids.ITEM_FLUIDS_CODEC.decode(NbtOps.INSTANCE, tag.get("ItemFluids")).result().ifPresent(fluids -> {
         List<FluidStack> list = ((ItemFluids)fluids.getFirst()).fluids();
         if (!list.isEmpty()) {
            this.fluidHandler.setFluid(list.getFirst());
         }
      });
      this.clientFluid = this.fluidHandler.getFluid().getFluid();
      this.amount = tag.getIntOr("level", 0);
   }

   public void saveClientDataToNBT(CompoundTag tag, Provider provider) {
      ItemFluids itemFluids = new ItemFluids(List.of(this.fluidHandler.getFluid()));
      ItemFluids.ITEM_FLUIDS_CODEC.encodeStart(NbtOps.INSTANCE, itemFluids).result().ifPresent(nbt -> tag.put("ItemFluids", nbt));
      tag.putInt("level", this.amount);
   }

   private void updateFilterFluid(ItemStack stack) {
      this.filterFluid = FluidUtil.getFluidContained(stack).<Fluid>map(FluidStack::getFluid).orElse(null);
   }

   public InteractionResult onBlockActivated(BlockState state, Player player, InteractionHand hand, BlockHitResult result) {
      if (!this.level.isClientSide()) {
         ItemStack heldItem = player.getItemInHand(hand);
         FluidActionResult fillResult = FluidUtil.tryEmptyContainerAndStow(heldItem, this.fluidHandler, null, Integer.MAX_VALUE, player, true);
         if (fillResult.isSuccess()) {
            player.setItemInHand(hand, fillResult.getResult());
            return InteractionResult.SUCCESS;
         } else {
            fillResult = FluidUtil.tryFillContainerAndStow(heldItem, this.fluidHandler, null, Integer.MAX_VALUE, player, true);
            if (fillResult.isSuccess()) {
               player.setItemInHand(hand, fillResult.getResult());
               return InteractionResult.SUCCESS;
            } else {
               return InteractionResult.PASS;
            }
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   public void onDataPacket(Connection net, ValueInput input) {
      int oldLevel = this.computeLevel(this.fluidHandler);
      super.onDataPacket(net, input);
      this.amount = this.computeLevel(this.fluidHandler);
      if (oldLevel != this.amount || !this.fluidHandler.getFluid().getFluid().equals(this.clientFluid)) {
         this.clientFluid = this.fluidHandler.getFluid().getFluid();
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }
   }

   private void updateLevel(CustomTank tank) {
      this.markDirtyQuick();
      int newlevel = this.computeLevel(tank);
      if (this.amount != newlevel || !tank.getFluid().getFluid().equals(this.clientFluid)) {
         this.amount = newlevel;
         this.clientFluid = tank.getFluid().getFluid();
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }
   }

   private int computeLevel(CustomTank tank) {
      int amount = tank.getFluidAmount();
      if (amount <= 0) {
         return 0;
      } else {
         int total = 8 * amount / tank.getCapacity() + 1;
         if (total > 8) {
            total = 8;
         }

         return total;
      }
   }

   @Nonnull
   private CustomTank createFluidHandler() {
      return new CustomTank((Integer)TankConfiguration.MAXCAPACITY.get()) {
         {
            Objects.requireNonNull(TankTE.this);
         }

         protected void onContentsChanged() {
            TankTE.this.updateLevel(this);
         }

         public boolean isFluidValid(FluidStack stack) {
            return TankTE.this.filterFluid == null ? true : TankTE.this.filterFluid == stack.getFluid();
         }
      };
   }
}
