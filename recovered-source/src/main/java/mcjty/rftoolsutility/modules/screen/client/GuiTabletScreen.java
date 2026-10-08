package mcjty.rftoolsutility.modules.screen.client;

import javax.annotation.Nonnull;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Widgets;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsutility.modules.screen.ScreenModule;
import mcjty.rftoolsutility.modules.screen.blocks.ScreenContainer;
import mcjty.rftoolsutility.modules.screen.blocks.ScreenTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class GuiTabletScreen extends GenericGuiContainer<ScreenTileEntity, ScreenContainer> {
   public static final int WIDTH = 200;
   public static final int HEIGHT = 190;

   public GuiTabletScreen(ScreenContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, ManualEntry.EMPTY, 200, 190);
   }

   public static void register(RegisterMenuScreensEvent event) {
      ScreenConstructor<ScreenContainer, GuiTabletScreen> factory = (container, inventory, title) -> {
         BlockEntity te = container.getBe();
         return (GuiTabletScreen)Tools.safeMap(te, tile -> new GuiTabletScreen(container, inventory, Component.literal("Title")), "Invalid tile entity!");
      };
      event.register(ScreenModule.CONTAINER_SCREEN_REMOTE.get(), factory);
      event.register(ScreenModule.CONTAINER_SCREEN_REMOTE_CREATIVE.get(), factory);
   }

   public void init() {
      super.init();
      Panel toplevel = Widgets.positional();
      toplevel.bounds(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
      this.window = new Window(this, toplevel);
   }

   public boolean mouseClicked(double x, double y, int button) {
      x -= 102.0;
      y -= 32.0;
      double dx = 1.0 - x / 60.0;
      double dy = 1.0 - y / 60.0;
      ScreenTileEntity tileEntity = (ScreenTileEntity)this.getBE();
      ScreenTileEntity.ModuleRaytraceResult result = tileEntity.getHitModule(dx, dy, 0.0, Direction.NORTH, Direction.NORTH, 1);
      if (result != null) {
         tileEntity.hitScreenClient(result);
      }

      return false;
   }

   public void render(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      ScreenTileEntity tileEntity = (ScreenTileEntity)this.getBE();
      tileEntity.tickMe();
      BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
      buffer.endBatch();
   }
}
