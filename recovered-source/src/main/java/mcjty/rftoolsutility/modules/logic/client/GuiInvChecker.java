package mcjty.rftoolsutility.modules.logic.client;

import javax.annotation.Nonnull;
import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.TagSelector;
import mcjty.rftoolsutility.modules.logic.LogicBlockModule;
import mcjty.rftoolsutility.modules.logic.blocks.InvCheckerTileEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class GuiInvChecker extends GenericGuiContainer<InvCheckerTileEntity, GenericContainer> {
   public GuiInvChecker(GenericContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, ((LogicSlabBlock)LogicBlockModule.INVCHECKER.block().get()).getManualEntry());
   }

   public static void register(RegisterMenuScreensEvent event) {
      event.register(LogicBlockModule.CONTAINER_INVCHECKER.get(), GuiInvChecker::new);
   }

   public void init() {
      this.window = new Window(this, this.getBE(), Identifier.fromNamespaceAndPath("rftoolsutility", "gui/invchecker.gui"));
      super.init();
   }

   private void updateFields() {
      if (this.window != null) {
         InvCheckerTileEntity tileEntity = (InvCheckerTileEntity)this.getBE();
         ((TagSelector)this.window.findChild("tags")).current(tileEntity.getTagName());
      }
   }

   public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int x, int y, float partialTicks) {
      this.updateFields();
      super.extractBackground(graphics, x, y, partialTicks);
   }
}
