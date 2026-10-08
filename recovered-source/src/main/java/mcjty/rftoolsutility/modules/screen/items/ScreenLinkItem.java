package mcjty.rftoolsutility.modules.screen.items;

import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.client.GuiTools;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.ComponentFactory;
import mcjty.lib.varia.LegacyCapabilities;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.ModuleTools;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.api.various.ITabletSupport;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.screen.ScreenModule;
import mcjty.rftoolsutility.modules.screen.blocks.ScreenContainer;
import mcjty.rftoolsutility.modules.screen.blocks.ScreenTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.items.IItemHandler;

public class ScreenLinkItem extends Item implements ITabletSupport, ITooltipSettings {
   private final Lazy<TooltipBuilder> tooltipBuilder = Lazy.of(
      () -> new TooltipBuilder()
         .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsutility.shiftmessage")})
         .infoShift(new InfoLine[]{TooltipBuilder.header(), TooltipBuilder.gold(), TooltipBuilder.parameter("info", this::getInfoString)})
   );

   protected String getInfoString(ItemStack stack) {
      return ModuleTools.getTargetString(stack);
   }

   public ScreenLinkItem() {
      super(RFToolsUtility.setup.defaultProperties().durability(1));
   }

   public void appendHoverText(
      @Nonnull ItemStack itemStack, @Nullable TooltipContext context, TooltipDisplay display, @Nonnull Consumer<Component> list, @Nonnull TooltipFlag flag
   ) {
      super.appendHoverText(itemStack, context, display, list, flag);
      ((TooltipBuilder)this.tooltipBuilder.get()).makeTooltip(Tools.getId(this), itemStack, list, flag);
   }

   public Item getInstalledTablet() {
      return (Item)ScreenModule.TABLET_SCREEN.get();
   }

   public void openGui(@Nonnull Player player, @Nonnull ItemStack tabletItem, @Nonnull ItemStack containingItem) {
      BlockPos pos = ModuleTools.getPositionFromModule(containingItem);
      ResourceKey<Level> dimensionType = ModuleTools.getDimensionFromModule(containingItem);
      GuiTools.openRemoteGui(
         player,
         dimensionType,
         pos,
         te -> new MenuProvider() {
            {
               Objects.requireNonNull(ScreenLinkItem.this);
            }

            @Nonnull
            public Component getDisplayName() {
               return ComponentFactory.literal("Remote Screen");
            }

            public AbstractContainerMenu createMenu(int id, @Nonnull Inventory inventory, @Nonnull Player playerx) {
               boolean creative = false;
               if (te instanceof ScreenTileEntity screenTe) {
                  creative = screenTe.isCreative();
               }

               ScreenContainer container = creative
                  ? ScreenContainer.createRemoteCreative(id, pos, (GenericTileEntity)te, playerx)
                  : ScreenContainer.createRemote(id, pos, (GenericTileEntity)te, playerx);
               IItemHandler h = (IItemHandler)te.getLevel().getCapability(LegacyCapabilities.ITEM_BLOCK, te.getBlockPos(), null);
               if (h != null) {
                  container.setupInventories(h, inventory);
               }

               return container;
            }
         }
      );
   }

   @Nonnull
   public InteractionResult use(Level world, Player player, @Nonnull InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!world.isClientSide()) {
         this.openGui(player, stack, stack);
         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.SUCCESS;
      }
   }

   @Nonnull
   public InteractionResult useOn(UseOnContext context) {
      ItemStack stack = context.getItemInHand();
      Level world = context.getLevel();
      BlockPos pos = context.getClickedPos();
      Direction facing = context.getClickedFace();
      Player player = context.getPlayer();
      BlockEntity te = world.getBlockEntity(pos);
      if (te instanceof ScreenTileEntity) {
         String name = "<invalid>";
         if (!world.getBlockState(pos).isAir()) {
            name = Tools.getReadableName(world, pos);
         }

         ModuleTools.setPositionInModule(stack, world.dimension(), pos, name);
         if (world.isClientSide()) {
            Logging.message(player, "Screen link is set to block '" + name + "'");
         }
      } else {
         ModuleTools.clearPositionInModule(stack);
         if (world.isClientSide()) {
            Logging.message(player, "Screen link is cleared");
         }
      }

      return InteractionResult.SUCCESS;
   }

   public ManualEntry getManualEntry() {
      return ManualHelper.create("rftoolsutility:machines/screen_link");
   }
}
