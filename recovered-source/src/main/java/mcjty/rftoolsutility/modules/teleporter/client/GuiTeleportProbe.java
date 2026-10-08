package mcjty.rftoolsutility.modules.teleporter.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import mcjty.lib.base.StyleConfig;
import mcjty.lib.client.GuiTools;
import mcjty.lib.gui.BaseScreen;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.DefaultSelectionEvent;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Slider;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.gui.widgets.WidgetList;
import mcjty.lib.gui.widgets.Widgets;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.ComponentFactory;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestinationClientInfo;
import mcjty.rftoolsutility.modules.teleporter.network.PacketGetAllReceivers;
import mcjty.rftoolsutility.setup.CommandHandler;
import mcjty.rftoolsutility.setup.RFToolsUtilityMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class GuiTeleportProbe extends BaseScreen {
   private final int xSize = 356;
   private final int ySize = 180;
   private Window window;
   private WidgetList list;
   private static List<TeleportDestinationClientInfo> serverDestinationList = null;
   private static List<TeleportDestinationClientInfo> destinationList = null;
   private int listDirty = 0;

   public GuiTeleportProbe() {
      super(ComponentFactory.literal("Teleport Probe"));
   }

   public boolean isPauseScreen() {
      return false;
   }

   public void init() {
      super.init();
      int k = (this.width - 356) / 2;
      int l = (this.height - 180) / 2;
      this.list = ((WidgetList)new WidgetList().name("list")).event(new DefaultSelectionEvent() {
         {
            Objects.requireNonNull(GuiTeleportProbe.this);
         }

         public void doubleClick(int index) {
            GuiTeleportProbe.this.teleport(index);
         }
      });
      Slider listSlider = ((Slider)new Slider().desiredWidth(11)).vertical().scrollableName("list");
      Panel toplevel = (Panel)Widgets.horizontal(3, 1).children(new Widget[]{this.list, listSlider});
      toplevel.bounds(k, l, 356, 180);
      this.window = new Window(this, toplevel);
      serverDestinationList = null;
      destinationList = null;
      this.requestReceiversFromServer();
   }

   private void teleport(int index) {
      TeleportDestinationClientInfo destination = destinationList.get(index);
      BlockPos c = destination.destination().getCoordinate();
      RFToolsUtilityMessages.sendToServer(
         "forceTeleport",
         TypedMap.builder()
            .put(CommandHandler.PARAM_DIMENSION, destination.destination().getDimension().identifier().toString())
            .put(CommandHandler.PARAM_POS, c)
      );
   }

   public static void setReceivers(List<TeleportDestinationClientInfo> destinationList) {
      serverDestinationList = new ArrayList<>(destinationList);
   }

   private void requestReceiversFromServer() {
      RFToolsUtilityMessages.sendToServer(new PacketGetAllReceivers());
   }

   private void populateList() {
      if (serverDestinationList != null) {
         if (!serverDestinationList.equals(destinationList)) {
            destinationList = new ArrayList<>(serverDestinationList);
            this.list.removeChildren();

            for (TeleportDestinationClientInfo destination : destinationList) {
               BlockPos coordinate = destination.destination().getCoordinate();
               ResourceKey<Level> dim = destination.destination().getDimension();
               Panel panel = Widgets.horizontal();
               panel.children(
                  new Widget[]{
                     ((Label)((Label)Widgets.label(destination.destination().getName()).color(StyleConfig.colorTextInListNormal))
                           .horizontalAlignment(HorizontalAlignment.ALIGN_LEFT))
                        .desiredWidth(100),
                     ((Label)((Label)Widgets.label(BlockPosTools.toString(coordinate)).color(StyleConfig.colorTextInListNormal))
                           .horizontalAlignment(HorizontalAlignment.ALIGN_LEFT))
                        .desiredWidth(75),
                     ((Label)((Label)Widgets.label("Id " + dim).color(StyleConfig.colorTextInListNormal)).horizontalAlignment(HorizontalAlignment.ALIGN_LEFT))
                        .desiredWidth(75)
                  }
               );
               this.list.children(new Widget[]{panel});
            }
         }
      }
   }

   protected void renderInternal(GuiGraphicsExtractor graphics, int pMouseX, int pMouseY, float pPartialTick) {
      this.listDirty--;
      if (this.listDirty <= 0) {
         this.populateList();
         this.listDirty = 10;
      }

      this.window.draw(graphics);
      List<String> tooltips = this.window.getTooltips();
      if (tooltips != null) {
         int x = GuiTools.getRelativeX(this);
         int y = GuiTools.getRelativeY(this);
         int guiLeft = (this.width - 356) / 2;
         int guiTop = (this.height - 180) / 2;
         List<FormattedText> properties = tooltips.stream().<FormattedText>map(ComponentFactory::literal).collect(Collectors.toList());
         List var11 = Language.getInstance().getVisualOrder(properties);
      }
   }

   public static void open() {
      Minecraft.getInstance().setScreen(new GuiTeleportProbe());
   }
}
