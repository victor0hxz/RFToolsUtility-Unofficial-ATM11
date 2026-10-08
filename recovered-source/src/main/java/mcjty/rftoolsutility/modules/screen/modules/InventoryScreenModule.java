package mcjty.rftoolsutility.modules.screen.modules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.CapabilityTools;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.api.screens.IScreenDataHelper;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.data.IModuleData;
import mcjty.rftoolsutility.modules.screen.ScreenConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;

public record InventoryScreenModule(int slot1, int slot2, int slot3, int slot4, GlobalPos pos, boolean active, String monitor)
   implements IScreenModule<InventoryScreenModule, InventoryScreenModule.ModuleDataStacks> {
   public static final InventoryScreenModule DEFAULT = new InventoryScreenModule(
      -1, -1, -1, -1, GlobalPos.of(Level.OVERWORLD, BlockPosTools.INVALID), false, ""
   );
   public static final Codec<InventoryScreenModule> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.INT.fieldOf("slot1").forGetter(module -> module.slot1),
            Codec.INT.fieldOf("slot2").forGetter(module -> module.slot2),
            Codec.INT.fieldOf("slot3").forGetter(module -> module.slot3),
            Codec.INT.fieldOf("slot4").forGetter(module -> module.slot4),
            GlobalPos.CODEC.fieldOf("pos").forGetter(module -> module.pos),
            Codec.STRING.fieldOf("monitor").forGetter(module -> module.monitor)
         )
         .apply(instance, InventoryScreenModule::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, InventoryScreenModule> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.INT,
      module -> module.slot1,
      ByteBufCodecs.INT,
      module -> module.slot2,
      ByteBufCodecs.INT,
      module -> module.slot3,
      ByteBufCodecs.INT,
      module -> module.slot4,
      GlobalPos.STREAM_CODEC,
      module -> module.pos,
      ByteBufCodecs.STRING_UTF8,
      module -> module.monitor,
      InventoryScreenModule::new
   );

   public InventoryScreenModule(int slot1, int slot2, int slot3, int slot4, GlobalPos pos, String monitor) {
      this(slot1, slot2, slot3, slot4, pos, false, monitor);
   }

   public int getSlot1() {
      return this.slot1;
   }

   public int getSlot2() {
      return this.slot2;
   }

   public int getSlot3() {
      return this.slot3;
   }

   public int getSlot4() {
      return this.slot4;
   }

   public GlobalPos getPos() {
      return this.pos;
   }

   public String getMonitor() {
      return this.monitor;
   }

   public InventoryScreenModule withSlot1(int slot1) {
      return new InventoryScreenModule(slot1, this.slot2, this.slot3, this.slot4, this.pos, this.active, this.monitor);
   }

   public InventoryScreenModule withSlot2(int slot2) {
      return new InventoryScreenModule(this.slot1, slot2, this.slot3, this.slot4, this.pos, this.active, this.monitor);
   }

   public InventoryScreenModule withSlot3(int slot3) {
      return new InventoryScreenModule(this.slot1, this.slot2, slot3, this.slot4, this.pos, this.active, this.monitor);
   }

   public InventoryScreenModule withSlot4(int slot4) {
      return new InventoryScreenModule(this.slot1, this.slot2, this.slot3, slot4, this.pos, this.active, this.monitor);
   }

   public InventoryScreenModule withPos(GlobalPos pos) {
      return new InventoryScreenModule(this.slot1, this.slot2, this.slot3, this.slot4, pos, this.active, this.monitor);
   }

   public InventoryScreenModule withMonitor(String monitor) {
      return new InventoryScreenModule(this.slot1, this.slot2, this.slot3, this.slot4, this.pos, this.active, monitor);
   }

   public InventoryScreenModule withActive(boolean active) {
      return new InventoryScreenModule(this.slot1, this.slot2, this.slot3, this.slot4, this.pos, active, this.monitor);
   }

   public InventoryScreenModule.ModuleDataStacks getData(IScreenDataHelper helper, Level worldObj, long millis) {
      if (!this.active) {
         return null;
      } else {
         Level world = LevelTools.getLevel(worldObj, this.pos.dimension());
         if (world == null) {
            return null;
         } else if (!LevelTools.isLoaded(world, this.pos.pos())) {
            return null;
         } else {
            BlockEntity te = world.getBlockEntity(this.pos.pos());
            if (te == null) {
               return null;
            } else {
               IItemHandler h = CapabilityTools.getItemCapabilitySafe(te);
               if (h != null) {
                  ItemStack stack1 = this.getItemStack(h, this.slot1);
                  ItemStack stack2 = this.getItemStack(h, this.slot2);
                  ItemStack stack3 = this.getItemStack(h, this.slot3);
                  ItemStack stack4 = this.getItemStack(h, this.slot4);
                  return new InventoryScreenModule.ModuleDataStacks(stack1, stack2, stack3, stack4);
               } else {
                  return null;
               }
            }
         }
      }
   }

   private ItemStack getItemStack(Container inventory, int slot) {
      if (slot == -1) {
         return ItemStack.EMPTY;
      } else {
         return slot < inventory.getContainerSize() ? inventory.getItem(slot) : ItemStack.EMPTY;
      }
   }

   private ItemStack getItemStack(IItemHandler itemHandler, int slot) {
      if (slot == -1) {
         return ItemStack.EMPTY;
      } else {
         return slot < itemHandler.getSlots() ? itemHandler.getStackInSlot(slot) : ItemStack.EMPTY;
      }
   }

   public InventoryScreenModule validate(Level world, BlockPos p, boolean isPlus) {
      if (isPlus) {
         return this.withActive(true);
      } else {
         if (LevelTools.isLoaded(world, this.pos.pos()) && Objects.equals(this.pos.dimension(), world.dimension())) {
            int dx = Math.abs(this.pos.pos().getX() - p.getX());
            int dy = Math.abs(this.pos.pos().getY() - p.getY());
            int dz = Math.abs(this.pos.pos().getZ() - p.getZ());
            if (dx <= 64 && dy <= 64 && dz <= 64) {
               return this.withActive(true);
            }
         }

         return this.withActive(false);
      }
   }

   public int getRfPerTick() {
      return (Integer)ScreenConfiguration.ITEMSTACK_RFPERTICK.get();
   }

   public ItemStack mouseClick(ItemStack moduleStack, Level world, int x, int y, boolean clicked, Player player) {
      return ItemStack.EMPTY;
   }

   public static class ModuleDataStacks implements IModuleData {
      public static final String ID = "rftoolsutility:itemStacks";
      private final ItemStack[] stacks = new ItemStack[4];

      public String getId() {
         return "rftoolsutility:itemStacks";
      }

      public ModuleDataStacks(ItemStack stack1, ItemStack stack2, ItemStack stack3, ItemStack stack4) {
         this.stacks[0] = stack1;
         this.stacks[1] = stack2;
         this.stacks[2] = stack3;
         this.stacks[3] = stack4;
      }

      public ModuleDataStacks(RegistryFriendlyByteBuf buf) {
         for (int i = 0; i < 4; i++) {
            this.stacks[i] = NetworkTools.readItemStack(buf);
         }
      }

      public ItemStack getStack(int idx) {
         return this.stacks[idx];
      }

      public void writeToBuf(RegistryFriendlyByteBuf buf) {
         this.writeStack(buf, this.stacks[0]);
         this.writeStack(buf, this.stacks[1]);
         this.writeStack(buf, this.stacks[2]);
         this.writeStack(buf, this.stacks[3]);
      }

      private void writeStack(RegistryFriendlyByteBuf buf, ItemStack stack) {
         NetworkTools.writeItemStack(buf, stack);
      }
   }
}
