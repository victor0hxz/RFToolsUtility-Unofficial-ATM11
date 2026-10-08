package mcjty.rftoolsutility.modules.crafter.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.lib.container.BaseSlot;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.container.SlotFactory;
import mcjty.lib.container.SlotType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolsbase.modules.filter.items.FilterModuleItem;
import mcjty.rftoolsutility.modules.crafter.CrafterModule;
import mcjty.rftoolsutility.modules.crafter.data.CrafterData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.items.IItemHandler;

public class CrafterContainer extends GenericContainer {
   public static final int SLOT_CRAFTINPUT = 0;
   public static final int SLOT_CRAFTOUTPUT = 9;
   public static final int SLOT_BUFFER = 10;
   public static final int BUFFER_SIZE = 26;
   public static final int SLOT_BUFFEROUT = 36;
   public static final int BUFFEROUT_SIZE = 4;
   public static final int SLOT_FILTER_MODULE = 40;
   public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(
      () -> new ContainerFactory(41)
         .box(SlotDefinition.ghost(), 0, 193, 7, 3, 3)
         .slot(SlotDefinition.ghostOut(), 9, 193, 65)
         .box(SlotDefinition.generic().in(), 10, 13, 97, 13, 2)
         .box(SlotDefinition.generic().out(), 36, 31, 142, 2, 2)
         .slot(SlotDefinition.specific(stack -> stack.getItem() instanceof FilterModuleItem), 40, 157, 43)
         .playerSlots(85, 142)
   );

   public CrafterContainer(int id, ContainerFactory factory, BlockPos pos, @Nullable GenericTileEntity te, @Nonnull Player player) {
      super(CrafterModule.CONTAINER_CRAFTER.get(), id, factory, pos, te, player);
   }

   protected Slot createSlot(SlotFactory slotFactory, Player playerEntity, IItemHandler inventory, int index, int x, int y, SlotType slotType) {
      final CrafterBaseTE c = (CrafterBaseTE)this.be;
      if (index >= 10 && index < 36 && slotType == SlotType.SLOT_GENERIC) {
         return new BaseSlot(inventory, this.be, index, x, y) {
            {
               Objects.requireNonNull(CrafterContainer.this);
            }

            public boolean mayPlace(@Nonnull ItemStack stack) {
               return !c.isItemValidForSlot(this.getSlotIndex(), stack) ? false : super.mayPlace(stack);
            }

            public void setChanged() {
               c.noRecipesWork = false;
               super.setChanged();
            }
         };
      } else {
         return (Slot)(index >= 36 && index < 40 && slotType == SlotType.SLOT_GENERIC ? new BaseSlot(inventory, this.be, index, x, y) {
            {
               Objects.requireNonNull(CrafterContainer.this);
            }

            public boolean mayPlace(@Nonnull ItemStack stack) {
               return !c.isItemValidForSlot(this.getSlotIndex(), stack) ? false : super.mayPlace(stack);
            }

            public void setChanged() {
               c.noRecipesWork = false;
               super.setChanged();
            }
         } : super.createSlot(slotFactory, playerEntity, inventory, index, x, y, slotType));
      }
   }

   public void clicked(int index, int button, @Nonnull ContainerInput mode, @Nonnull Player player) {
      if (mode == ContainerInput.QUICK_MOVE && index >= 10 && index < 36) {
         CrafterBaseTE c = (CrafterBaseTE)this.be;
         int offset = index - 10;
         List<ItemStack> ghostSlots = new ArrayList<>(c.getGhostSlots());
         ItemStack ghostSlot = ghostSlots.get(offset);
         ItemStack clickedWith = this.getCarried();
         if (!clickedWith.isEmpty() && !ghostSlot.isEmpty() && !ItemStack.isSameItemSameComponents(ghostSlot, clickedWith)) {
            ItemStack copy = clickedWith.copy();
            copy.setCount(1);
            if (this.be instanceof CrafterBaseTE) {
               CrafterData data = (CrafterData)this.be.getData(CrafterModule.CRAFTER_DATA);
               ghostSlots.set(offset, copy);
               this.be.setData(CrafterModule.CRAFTER_DATA, data.withGhostSlots(ghostSlots));
               this.be.setChanged();
               this.forceBroadcast();
            }

            this.broadcastChanges();
            return;
         }
      }

      super.clicked(index, button, mode, player);
   }
}
