package mcjty.rftoolsutility.modules.teleporter.items.porter;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.crafting.IComponentsToPreserve;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.EnergyTools;
import mcjty.lib.varia.IEnergyItem;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.teleporter.TeleportConfiguration;
import mcjty.rftoolsutility.modules.teleporter.TeleportationTools;
import mcjty.rftoolsutility.modules.teleporter.TeleporterModule;
import mcjty.rftoolsutility.modules.teleporter.blocks.MatterReceiverTileEntity;
import mcjty.rftoolsutility.modules.teleporter.data.ChargedPorterData;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestination;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestinations;
import mcjty.rftoolsutility.setup.ForgeEventHandlers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class ChargedPorterItem extends Item implements IEnergyItem, IComponentsToPreserve, ITooltipSettings {
   private final Supplier<Integer> capacity;
   private final Supplier<Integer> maxReceive;
   private final int maxExtract;
   public static final ManualEntry MANUAL = ManualHelper.create("rftoolsbase:machines/teleporter");
   private final Lazy<TooltipBuilder> tooltipBuilder = Lazy.of(
      () -> new TooltipBuilder()
         .info(
            new InfoLine[]{
               TooltipBuilder.parameter("energy", this::getEnergyString),
               TooltipBuilder.parameter("target", this::hasTarget, this::getTargetString),
               TooltipBuilder.key("message.rftoolsutility.shiftmessage")
            }
         )
         .infoShift(
            new InfoLine[]{
               TooltipBuilder.header(),
               TooltipBuilder.parameter("energy", this::getEnergyString),
               TooltipBuilder.parameter("target", this::hasTarget, this::getTargetString)
            }
         )
   );

   public Supplier<Integer> getCapacity() {
      return this.capacity;
   }

   public Supplier<Integer> getMaxReceive() {
      return this.maxReceive;
   }

   public int getMaxExtract() {
      return this.maxExtract;
   }

   private String getEnergyString(ItemStack stack) {
      ChargedPorterData data = (ChargedPorterData)stack.get(TeleporterModule.ITEM_CHARGEDPORTER_DATA);
      return data != null ? Integer.toString(data.energy()) : "0";
   }

   private boolean hasTarget(ItemStack stack) {
      ChargedPorterData data = (ChargedPorterData)stack.get(TeleporterModule.ITEM_CHARGEDPORTER_DATA);
      return data != null ? data.currentTarget() != -1 : false;
   }

   private String getTargetString(ItemStack stack) {
      ChargedPorterData data = (ChargedPorterData)stack.get(TeleporterModule.ITEM_CHARGEDPORTER_DATA);
      return data != null ? Integer.toString(data.currentTarget()) : "<not set>";
   }

   public ManualEntry getManualEntry() {
      return MANUAL;
   }

   public ChargedPorterItem() {
      this(TeleportConfiguration.CHARGEDPORTER_MAXENERGY);
   }

   protected ChargedPorterItem(Supplier<Integer> capacity) {
      super(RFToolsUtility.setup.defaultProperties().stacksTo(1).durability(1));
      this.capacity = capacity;
      this.maxReceive = TeleportConfiguration.CHARGEDPORTER_RECEIVEPERTICK;
      this.maxExtract = 0;
   }

   public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
      return oldStack.isEmpty() != newStack.isEmpty() ? true : oldStack.getItem() != newStack.getItem();
   }

   public void inventoryTick(@Nonnull ItemStack stack, ServerLevel worldIn, @Nonnull Entity entityIn, EquipmentSlot slot) {
      ChargedPorterData data = (ChargedPorterData)stack.get(TeleporterModule.ITEM_CHARGEDPORTER_DATA);
      if (data != null) {
         if (entityIn instanceof Player player) {
            int timer = data.tpTimer();
            if (timer >= 0) {
               if (--timer <= 0) {
                  int var13 = -1;
                  stack.set(TeleporterModule.ITEM_CHARGEDPORTER_DATA, data.withTpTimer(var13));
                  TeleportDestinations destinations = TeleportDestinations.get(worldIn);
                  int target = data.currentTarget();
                  GlobalPos coordinate = destinations.getCoordinateForId(target);
                  if (coordinate == null) {
                     Logging.message(player, ChatFormatting.RED + "Something went wrong! The target has disappeared!");
                     TeleportationTools.applyEffectForSeverity(player, 3, false);
                     return;
                  }

                  TeleportDestination destination = destinations.getDestination(coordinate);
                  ForgeEventHandlers.addPlayerToTeleportHere(destination, player);
               } else {
                  stack.set(TeleporterModule.ITEM_CHARGEDPORTER_DATA, data.withTpTimer(timer));
               }
            }
         }
      }
   }

   public static void initOverrides(ChargedPorterItem item) {
   }

   protected int getSpeedBonus() {
      return 1;
   }

   @Nonnull
   public InteractionResult use(@Nonnull Level world, @Nonnull Player player, @Nonnull InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!player.isShiftKeyDown()) {
         this.startTeleport(stack, player, world);
      } else {
         this.selectReceiver(stack, world, player);
      }

      return super.use(world, player, hand);
   }

   protected void selectReceiver(ItemStack stack, Level world, Player player) {
   }

   @Nonnull
   public InteractionResult useOn(UseOnContext context) {
      Player player = context.getPlayer();
      InteractionHand hand = context.getHand();
      Level world = context.getLevel();
      BlockPos pos = context.getClickedPos();
      ItemStack stack = player.getItemInHand(hand);
      if (player.isShiftKeyDown()) {
         BlockEntity te = world.getBlockEntity(pos);
         this.setTarget(stack, player, world, te);
      } else {
         this.startTeleport(stack, player, world);
      }

      return InteractionResult.SUCCESS;
   }

   private void startTeleport(ItemStack stack, Player player, Level world) {
      if (!world.isClientSide()) {
         ChargedPorterData data = (ChargedPorterData)stack.get(TeleporterModule.ITEM_CHARGEDPORTER_DATA);
         if (data == null || data.currentTarget() == -1) {
            Logging.message(player, ChatFormatting.RED + "The charged porter has no target.");
         } else if (data.tpTimer() >= 0) {
            Logging.message(player, ChatFormatting.RED + "Already teleporting!");
         } else {
            int target = data.currentTarget();
            TeleportDestinations destinations = TeleportDestinations.get(world);
            GlobalPos coordinate = destinations.getCoordinateForId(target);
            if (coordinate == null) {
               Logging.message(player, ChatFormatting.RED + "Something went wrong! The target has disappeared!");
               TeleportationTools.applyEffectForSeverity(player, 3, false);
            } else {
               TeleportDestination destination = destinations.getDestination(coordinate);
               if (TeleportationTools.checkValidTeleport(player, world.dimension(), destination.getDimension())) {
                  BlockPos playerCoordinate = new BlockPos((int)player.getX(), (int)player.getY(), (int)player.getZ());
                  int cost = TeleportationTools.calculateRFCost(world, playerCoordinate, destination);
                  cost = (int)(cost * 1.5F);
                  long energy = this.getEnergyStoredL(stack);
                  if (cost > energy) {
                     Logging.message(player, ChatFormatting.RED + "Not enough energy to start the teleportation!");
                  } else {
                     data = this.extractEnergyNoMax(data, cost, false);
                     int ticks = TeleportationTools.calculateTime(world, playerCoordinate, destination);
                     ticks /= this.getSpeedBonus();
                     data = data.withTpTimer(ticks);
                     stack.set(TeleporterModule.ITEM_CHARGEDPORTER_DATA, data);
                     Logging.message(player, ChatFormatting.YELLOW + "Start teleportation!");
                  }
               }
            }
         }
      }
   }

   private void setTarget(ItemStack stack, Player player, Level world, BlockEntity te) {
      if (!world.isClientSide()) {
         ChargedPorterData data = (ChargedPorterData)stack.get(TeleporterModule.ITEM_CHARGEDPORTER_DATA);
         if (data == null) {
            data = ChargedPorterData.createDefault();
         }

         int id = -1;
         if (te instanceof MatterReceiverTileEntity receiver) {
            TeleportDestination destination = receiver.updateDestination();
            if (!destination.checkAccess(world, player.getUUID())) {
               Logging.message(player, ChatFormatting.RED + "You have no access to target this receiver!");
               return;
            }

            id = receiver.getId();
         }

         if (id != -1) {
            data = this.selectOnReceiver(player, world, data, id);
         } else {
            data = this.selectOnThinAir(player, world, data, stack);
         }

         stack.set(TeleporterModule.ITEM_CHARGEDPORTER_DATA, data);
      }
   }

   protected ChargedPorterData selectOnReceiver(Player player, Level world, ChargedPorterData data, int id) {
      Logging.message(player, "Charged porter target is set to " + id + ".");
      return data.withCurrentTarget(id);
   }

   protected ChargedPorterData selectOnThinAir(Player player, Level world, ChargedPorterData data, ItemStack stack) {
      if (world.isClientSide()) {
         Logging.message(player, "Charged porter is cleared.");
      }

      return data.withCurrentTarget(-1);
   }

   public void appendHoverText(
      @Nonnull ItemStack itemStack, TooltipContext context, TooltipDisplay display, @Nonnull Consumer<Component> list, @Nonnull TooltipFlag flag
   ) {
      super.appendHoverText(itemStack, context, display, list, flag);
      ((TooltipBuilder)this.tooltipBuilder.get()).makeTooltip(Tools.getId(this), itemStack, list, flag);
   }

   public long receiveEnergyL(ItemStack container, long maxReceive, boolean simulate) {
      ChargedPorterData data = (ChargedPorterData)container.get(TeleporterModule.ITEM_CHARGEDPORTER_DATA);
      if (data == null) {
         data = ChargedPorterData.createDefault();
      }

      int energy = data.energy();
      int energyReceived = Math.min(this.capacity.get() - energy, Math.min(this.maxReceive.get(), EnergyTools.unsignedClampToInt(maxReceive)));
      if (!simulate) {
         energy += energyReceived;
         data = data.withEnergy(energy);
         container.set(TeleporterModule.ITEM_CHARGEDPORTER_DATA, data);
      }

      return energyReceived;
   }

   public long extractEnergyL(ItemStack container, long maxExtract, boolean simulate) {
      ChargedPorterData data = (ChargedPorterData)container.get(TeleporterModule.ITEM_CHARGEDPORTER_DATA);
      if (data == null) {
         data = ChargedPorterData.createDefault();
      }

      int energy = data.energy();
      int energyExtracted = Math.min(energy, Math.min(this.maxExtract, EnergyTools.unsignedClampToInt(maxExtract)));
      if (!simulate) {
         energy -= energyExtracted;
         data = data.withEnergy(energy);
         container.set(TeleporterModule.ITEM_CHARGEDPORTER_DATA, data);
      }

      return energyExtracted;
   }

   public ChargedPorterData extractEnergyNoMax(ChargedPorterData data, int maxExtract, boolean simulate) {
      int energy = data.energy();
      int energyExtracted = Math.min(energy, maxExtract);
      if (!simulate) {
         energy -= energyExtracted;
         data = data.withEnergy(energy);
      }

      return data;
   }

   public long getEnergyStoredL(ItemStack container) {
      ChargedPorterData data = (ChargedPorterData)container.get(TeleporterModule.ITEM_CHARGEDPORTER_DATA);
      return data == null ? 0L : data.energy();
   }

   public long getMaxEnergyStoredL(ItemStack container) {
      return this.capacity.get().intValue();
   }

   public IEnergyStorage createEnergyStorage(final ItemStack container) {
      return new IEnergyStorage() {
         {
            Objects.requireNonNull(ChargedPorterItem.this);
         }

         public int receiveEnergy(int maxReceive, boolean simulate) {
            return (int)ChargedPorterItem.this.receiveEnergyL(container, maxReceive, simulate);
         }

         public int extractEnergy(int maxExtract, boolean simulate) {
            return (int)ChargedPorterItem.this.extractEnergyL(container, maxExtract, simulate);
         }

         public int getEnergyStored() {
            return (int)ChargedPorterItem.this.getEnergyStoredL(container);
         }

         public int getMaxEnergyStored() {
            return (int)ChargedPorterItem.this.getMaxEnergyStoredL(container);
         }

         public boolean canExtract() {
            return false;
         }

         public boolean canReceive() {
            return true;
         }
      };
   }

   public Collection<DataComponentType<?>> getComponentsToPreserve() {
      return List.of((DataComponentType<?>)TeleporterModule.ITEM_CHARGEDPORTER_DATA.get());
   }
}
