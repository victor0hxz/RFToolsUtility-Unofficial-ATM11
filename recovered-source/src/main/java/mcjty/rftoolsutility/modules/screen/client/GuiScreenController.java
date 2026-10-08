package mcjty.rftoolsutility.modules.screen.client;

import javax.annotation.Nonnull;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.gui.widgets.Widgets;
import mcjty.rftoolsutility.modules.screen.ScreenModule;
import mcjty.rftoolsutility.modules.screen.blocks.ScreenControllerBlock;
import mcjty.rftoolsutility.modules.screen.blocks.ScreenControllerTileEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class GuiScreenController extends GenericGuiContainer<ScreenControllerTileEntity, GenericContainer> {
   public static final int CONTROLLER_WIDTH = 180;
   public static final int CONTROLLER_HEIGHT = 152;
   private EnergyBar energyBar;
   private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath("rftoolsutility", "textures/gui/screencontroller.png");

   public GuiScreenController(GenericContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, ((ScreenControllerBlock)ScreenModule.SCREEN_CONTROLLER.block().get()).getManualEntry(), 180, 152);
   }

   public static void register(RegisterMenuScreensEvent event) {
      event.register(ScreenModule.CONTAINER_SCREEN_CONTROLLER.get(), GuiScreenController::new);
   }

   public void init() {
      super.init();
      this.energyBar = ((EnergyBar)new EnergyBar().vertical().hint(10, 7, 8, 54)).showText(false);
      Button scanButton = (Button)((Button)Widgets.button(30, 7, 50, 14, "Scan").name("scan"))
         .tooltips(new String[]{"Find all nearby screens", "and connect to them"});
      Button detachButton = (Button)((Button)Widgets.button(90, 7, 50, 14, "Detach").name("detach")).tooltips(new String[]{"Detach from all screens"});
      Label infoLabel = Widgets.label(30, 25, 140, 14, "");
      Panel toplevel = (Panel)((Panel)Widgets.positional().background(BACKGROUND)).children(new Widget[]{this.energyBar, scanButton, detachButton, infoLabel});
      toplevel.bounds(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
      this.window = new Window(this, toplevel);
      this.window.action("scan", this.getBE(), ScreenControllerTileEntity.CMD_SCAN);
      this.window.action("detach", this.getBE(), ScreenControllerTileEntity.CMD_DETACH);
   }

   public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      this.drawWindow(graphics, partialTicks, mouseX, mouseY);
      this.updateEnergyBar(this.energyBar);
   }
}
