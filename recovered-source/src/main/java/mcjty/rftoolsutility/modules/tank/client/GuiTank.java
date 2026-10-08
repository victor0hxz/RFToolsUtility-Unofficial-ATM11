package mcjty.rftoolsutility.modules.tank.client;

import javax.annotation.Nonnull;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.WidgetList;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.LegacyCapabilities;
import mcjty.rftoolsutility.modules.tank.TankModule;
import mcjty.rftoolsutility.modules.tank.blocks.TankTE;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class GuiTank extends GenericGuiContainer<TankTE, GenericContainer> {
   private EnergyBar energyBar;
   private WidgetList recipeList;
   private ChoiceLabel keepItem;
   private ChoiceLabel internalRecipe;
   private Button applyButton;
   private static final Identifier iconGuiElements = Identifier.fromNamespaceAndPath("rftoolsbase", "textures/gui/guielements.png");

   public GuiTank(GenericContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, ((BaseBlock)TankModule.TANK.block().get()).getManualEntry());
   }

   public static void register(RegisterMenuScreensEvent event) {
      event.register(TankModule.CONTAINER_TANK.get(), GuiTank::new);
   }

   public void init() {
      this.window = new Window(this, this.getBE(), Identifier.fromNamespaceAndPath("rftoolsutility", "gui/tank.gui"));
      super.init();
   }

   public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      if (this.window != null) {
         this.drawWindow(graphics, partialTicks, mouseX, mouseY);
         GenericTileEntity be = this.getBE();
         IFluidHandler fluidHandler = (IFluidHandler)be.getLevel().getCapability(LegacyCapabilities.FLUID_BLOCK, be.getBlockPos(), null);
         if (fluidHandler != null) {
         }
      }
   }
}
