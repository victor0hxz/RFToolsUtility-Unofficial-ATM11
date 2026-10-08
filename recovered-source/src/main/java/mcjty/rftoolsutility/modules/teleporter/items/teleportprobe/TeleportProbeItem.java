package mcjty.rftoolsutility.modules.teleporter.items.teleportprobe;

import javax.annotation.Nonnull;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.teleporter.client.GuiTeleportProbe;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TeleportProbeItem extends Item implements ITooltipSettings {
   public TeleportProbeItem() {
      super(RFToolsUtility.setup.defaultProperties().stacksTo(1).durability(1));
   }

   @Nonnull
   public InteractionResult use(Level world, Player player, @Nonnull InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (world.isClientSide()) {
         GuiTeleportProbe.open();
         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.SUCCESS;
      }
   }

   public ManualEntry getManualEntry() {
      return ManualHelper.create("rftoolsutility:machines/teleporter");
   }
}
