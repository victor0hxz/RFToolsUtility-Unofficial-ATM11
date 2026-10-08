package mcjty.rftoolsutility.modules.logic.blocks;

import java.util.function.Function;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.container.ItemInventory;
import mcjty.lib.bindings.GuiValue;
import mcjty.lib.bindings.Value;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.gui.widgets.TagSelector;
import mcjty.lib.setup.Registration;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.LogicSupport;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.CapabilityTools;
import mcjty.lib.varia.InventoryTools;
import mcjty.lib.varia.TagTools;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsutility.compat.RFToolsUtilityTOPDriver;
import mcjty.rftoolsutility.modules.logic.LogicBlockModule;
import mcjty.rftoolsutility.modules.logic.data.IncCheckerData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.items.IItemHandler;

public class InvCheckerTileEntity extends TickingTileEntity {
   private final LogicSupport support = new LogicSupport();
   public static final int SLOT_ITEMMATCH = 0;
   public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(
      () -> new ContainerFactory(1).slot(SlotDefinition.ghost(), 0, 154, 24).playerSlots(10, 70)
   );
   private final GenericItemHandler items = GenericItemHandler.create(this, CONTAINER_FACTORY).itemValid(GenericItemHandler.no()).build();
   @Cap(type = CapType.ITEMS_AUTOMATION)
   private static final Function<InvCheckerTileEntity, GenericItemHandler> ITEM_CAP = tile -> tile.items;
   @Cap(type = CapType.CONTAINER)
   private static final Function<InvCheckerTileEntity, MenuProvider> SCREEN_CAP = be -> new DefaultContainerProvider("Inventory Checker")
      .containerSupplier(DefaultContainerProvider.container(LogicBlockModule.CONTAINER_INVCHECKER, CONTAINER_FACTORY, be))
      .itemHandler(() -> be.items)
      .setupSync(be);
   @GuiValue
   public static final Value<InvCheckerTileEntity, Integer> VALUE_AMOUNT = Value.create(
      "amount", Type.INTEGER, InvCheckerTileEntity::getAmount, InvCheckerTileEntity::setAmount
   );
   @GuiValue
   public static final Value<InvCheckerTileEntity, Integer> VALUE_SLOT = Value.create(
      "slot", Type.INTEGER, InvCheckerTileEntity::getSlot, InvCheckerTileEntity::setSlot
   );
   @GuiValue(name = "damage")
   public static final Value<InvCheckerTileEntity, String> VALUE_DAMAGE_MODE = Value.createEnum(
      "damage", InvCheckerDamageMode.values(), InvCheckerTileEntity::getDamageMode, InvCheckerTileEntity::setDamageMode
   );
   private int checkCounter = 0;
   @ServerCommand
   public static final Command<?> CMD_SETTAG = Command.create("inv.setTag", (te, player, params) -> te.setTagByName((String)params.get(TagSelector.PARAM_TAG)));

   public InvCheckerTileEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)LogicBlockModule.INVCHECKER.be().get(), pos, state);
   }

   public static LogicSlabBlock createBlock() {
      return new LogicSlabBlock(
         new BlockBuilder()
            .topDriver(RFToolsUtilityTOPDriver.DRIVER)
            .manualEntry(ManualHelper.create("rftoolsbase:logic/invchecker"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsutility.shiftmessage")})
            .infoShift(new InfoLine[]{TooltipBuilder.header()})
            .tileEntitySupplier(InvCheckerTileEntity::new)
      );
   }

   public void checkRedstone(Level world, BlockPos pos) {
      this.support.checkRedstone(this, world, pos);
   }

   public int getRedstoneOutput(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
      return this.support.getRedstoneOutput(state, side);
   }

   public int getAmount() {
      return ((IncCheckerData)this.getData(LogicBlockModule.INVCHECKER_DATA)).amount();
   }

   public void setAmount(int amount) {
      IncCheckerData data = (IncCheckerData)this.getData(LogicBlockModule.INVCHECKER_DATA);
      this.setData(LogicBlockModule.INVCHECKER_DATA, data.withAmount(amount));
   }

   public int getSlot() {
      return ((IncCheckerData)this.getData(LogicBlockModule.INVCHECKER_DATA)).slot();
   }

   public void setSlot(int slot) {
      IncCheckerData data = (IncCheckerData)this.getData(LogicBlockModule.INVCHECKER_DATA);
      this.setData(LogicBlockModule.INVCHECKER_DATA, data.withSlot(slot));
   }

   public InvCheckerDamageMode getDamageMode() {
      return ((IncCheckerData)this.getData(LogicBlockModule.INVCHECKER_DATA)).useDamage();
   }

   public void setDamageMode(InvCheckerDamageMode mode) {
      IncCheckerData data = (IncCheckerData)this.getData(LogicBlockModule.INVCHECKER_DATA);
      this.setData(LogicBlockModule.INVCHECKER_DATA, data.withUseDamage(mode));
   }

   public TagKey<Item> getTag() {
      IncCheckerData data = (IncCheckerData)this.getData(LogicBlockModule.INVCHECKER_DATA);
      return data.tag();
   }

   public void setTag(TagKey<Item> tag) {
      IncCheckerData data = (IncCheckerData)this.getData(LogicBlockModule.INVCHECKER_DATA);
      this.setData(LogicBlockModule.INVCHECKER_DATA, data.withTag(tag));
   }

   public void setTagByName(String tagName) {
      IncCheckerData data = (IncCheckerData)this.getData(LogicBlockModule.INVCHECKER_DATA);
      if (tagName == null) {
         this.setData(LogicBlockModule.INVCHECKER_DATA, data.withTag(null));
      } else {
         this.setData(LogicBlockModule.INVCHECKER_DATA, data.withTag(this.getiNamedTag(tagName)));
      }

      this.markDirtyClient();
   }

   private TagKey<Item> getiNamedTag(String tagName) {
      return TagTools.createItemTagKey(Identifier.parse(tagName));
   }

   public String getTagName() {
      IncCheckerData data = (IncCheckerData)this.getData(LogicBlockModule.INVCHECKER_DATA);
      return data.tag() == null ? null : data.tag().location().toString();
   }

   public InvCheckerDamageMode isUseDamage() {
      IncCheckerData data = (IncCheckerData)this.getData(LogicBlockModule.INVCHECKER_DATA);
      return data.useDamage();
   }

   public void setUseDamage(InvCheckerDamageMode useDamage) {
      IncCheckerData data = (IncCheckerData)this.getData(LogicBlockModule.INVCHECKER_DATA);
      this.setData(LogicBlockModule.INVCHECKER_DATA, data.withUseDamage(useDamage));
   }

   protected void tickServer() {
      this.checkCounter--;
      if (this.checkCounter <= 0) {
         this.checkCounter = 10;
         this.support.setRedstoneState(this, this.checkOutput() ? 15 : 0);
      }
   }

   public boolean checkOutput() {
      Direction inputSide = LogicSupport.getFacing(this.level.getBlockState(this.getBlockPos())).getInputSide();
      BlockPos inputPos = this.getBlockPos().relative(inputSide);
      BlockEntity te = this.level.getBlockEntity(inputPos);
      if (InventoryTools.isInventory(te)) {
         IItemHandler capability = CapabilityTools.getItemCapabilitySafe(te);
         if (capability != null) {
            IncCheckerData data = (IncCheckerData)this.getData(LogicBlockModule.INVCHECKER_DATA);
            if (data.slot() >= 0 && data.slot() < capability.getSlots()) {
               ItemStack stack = capability.getStackInSlot(data.slot());
               if (!stack.isEmpty()) {
                  int nr = this.isItemMatching(stack);
                  if (nr >= data.amount()) {
                     if (data.tag() != null) {
                        return stack.getItem().builtInRegistryHolder().is(data.tag());
                     }

                     return true;
                  }
               }
            }

            return false;
         }
      }

      return false;
   }

   private int isItemMatching(ItemStack stack) {
      int nr = 0;
      ItemStack matcher = this.items.getStackInSlot(0);
      if (!matcher.isEmpty()) {
         IncCheckerData data = (IncCheckerData)this.getData(LogicBlockModule.INVCHECKER_DATA);
         if (data.useDamage() == InvCheckerDamageMode.DMG_MATCH) {
            if (ItemStack.isSameItem(matcher, stack)) {
               nr = stack.getCount();
            }
         } else if (matcher.getItem() == stack.getItem()) {
            nr = stack.getCount();
         }
      } else {
         nr = stack.getCount();
      }

      return nr;
   }

   public void loadAdditional(ValueInput tag) {
      super.loadAdditional(tag);
      this.support.setPowerOutput(tag.getBooleanOr("rs", false) ? 15 : 0);
      this.items.load(tag, "items");
   }

   public void saveAdditional(ValueOutput tag) {
      super.saveAdditional(tag);
      tag.putBoolean("rs", this.support.getPowerOutput() > 0);
      this.items.save(tag, "items");
   }

   protected void applyImplicitComponents(DataComponentGetter input) {
      super.applyImplicitComponents(input);
      IncCheckerData data = (IncCheckerData)input.get(LogicBlockModule.ITEM_INVCHECKER_DATA);
      if (data != null) {
         this.setData(LogicBlockModule.INVCHECKER_DATA, data);
      }

      this.items.applyImplicitComponents((ItemInventory)input.get(Registration.ITEM_INVENTORY));
   }

   protected void collectImplicitComponents(Builder builder) {
      super.collectImplicitComponents(builder);
      builder.set(LogicBlockModule.ITEM_INVCHECKER_DATA, (IncCheckerData)this.getData(LogicBlockModule.INVCHECKER_DATA));
      this.items.collectImplicitComponents(builder);
   }

   public void saveClientDataToNBT(CompoundTag tag, Provider provider) {
      if (this.getTag() != null) {
         tag.putString("tag", this.getTag().location().toString());
      }
   }

   public void loadClientDataFromNBT(CompoundTag tag, Provider provider) {
      String tagString = tag.getStringOr("tag", "");
      if (!tagString.isEmpty()) {
         this.setTag(this.getiNamedTag(tagString));
      } else {
         this.setTag(null);
      }
   }
}
