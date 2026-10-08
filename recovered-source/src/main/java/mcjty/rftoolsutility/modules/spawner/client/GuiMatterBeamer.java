package mcjty.rftoolsutility.modules.spawner.client;

import java.awt.Rectangle;
import javax.annotation.Nonnull;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Widget;
import mcjty.rftoolsutility.modules.spawner.SpawnerModule;
import mcjty.rftoolsutility.modules.spawner.blocks.MatterBeamerTileEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class GuiMatterBeamer extends GenericGuiContainer<MatterBeamerTileEntity, GenericContainer> {
   private static final int BEAMER_WIDTH = 180;
   private static final int BEAMER_HEIGHT = 152;
   private EnergyBar energyBar;
   private static final Identifier iconLocation = Identifier.fromNamespaceAndPath("rftoolsutility", "textures/gui/matterbeamer.png");

   public GuiMatterBeamer(GenericContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, ((BaseBlock)SpawnerModule.MATTER_BEAMER.block().get()).getManualEntry(), 180, 152);
   }

   public static void register(RegisterMenuScreensEvent event) {
      event.register(SpawnerModule.CONTAINER_MATTER_BEAMER.get(), GuiMatterBeamer::new);
   }

   public void init() {
      super.init();
      this.energyBar = ((EnergyBar)new EnergyBar().vertical().hint(10, 7, 8, 54)).showText(false);
      Panel toplevel = (Panel)((Panel)new Panel().background(iconLocation)).layout(new PositionalLayout()).children(new Widget[]{this.energyBar});
      toplevel.setBounds(new Rectangle(this.leftPos, this.topPos, this.imageWidth, this.imageHeight));
      this.window = new Window(this, toplevel);
   }

   public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      this.drawWindow(graphics, partialTicks, mouseX, mouseY);
      this.updateEnergyBar(this.energyBar);
   }
}
