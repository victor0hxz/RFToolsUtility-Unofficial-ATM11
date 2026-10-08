package mcjty.rftoolsutility.modules.logic.client;

import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Slider;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.gui.widgets.WidgetList;
import mcjty.lib.gui.widgets.Widgets;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolsutility.modules.logic.LogicBlockModule;
import mcjty.rftoolsutility.modules.logic.items.RedstoneInformationContainer;
import mcjty.rftoolsutility.modules.logic.items.RedstoneInformationItem;
import mcjty.rftoolsutility.modules.logic.network.PacketRemoveChannel;
import mcjty.rftoolsutility.modules.logic.network.PacketSetRedstone;
import mcjty.rftoolsutility.setup.RFToolsUtilityMessages;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.apache.commons.lang3.tuple.Pair;

public class GuiRedstoneInformation extends GenericGuiContainer<GenericTileEntity, RedstoneInformationContainer> {
   private static final Identifier iconLocation = Identifier.fromNamespaceAndPath("rftoolsutility", "textures/gui/redstone_information.png");
   private static final Identifier guiElements = Identifier.fromNamespaceAndPath("rftoolsbase", "textures/gui/guielements.png");
   public static final int WIDTH = 200;
   public static final int HEIGHT = 190;
   private WidgetList list;

   public GuiRedstoneInformation(RedstoneInformationContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, RedstoneInformationItem.MANUAL, 200, 190);
   }

   @Nonnull
   public static GuiRedstoneInformation createRedstoneInformationGui(RedstoneInformationContainer container, Inventory inventory, Component textComponent) {
      return new GuiRedstoneInformation(container, inventory, textComponent);
   }

   public static void register(RegisterMenuScreensEvent event) {
      event.register(LogicBlockModule.CONTAINER_REDSTONE_INFORMATION.get(), GuiRedstoneInformation::createRedstoneInformationGui);
   }

   public void init() {
      super.init();
      this.list = ((WidgetList)Widgets.list(5, 5, 180, 180).name("list")).propagateEventsToChildren(true);
      Slider slider = Widgets.slider(185, 5, 10, 180).scrollableName("list");
      Panel toplevel = (Panel)((Panel)Widgets.positional().background(iconLocation)).children(new Widget[]{this.list, slider});
      toplevel.bounds(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
      this.window = new Window(this, toplevel);
      this.fillList();
   }

   private void removeChannel(int channel) {
      RFToolsUtilityMessages.sendToServer(PacketRemoveChannel.create(channel));
   }

   private void setRedstone(int channel, String newChoice) {
      RFToolsUtilityMessages.sendToServer(PacketSetRedstone.create(channel, "1".equals(newChoice) ? 15 : 0));
   }

   private boolean isDirty() {
      Map<Integer, Pair<String, Integer>> data = ((RedstoneInformationContainer)this.menu).getChannelData();
      if (data == null) {
         return true;
      } else if (data.size() != this.list.getChildCount()) {
         return true;
      } else {
         for (int i = 0; i < this.list.getChildCount(); i++) {
            Panel panel = (Panel)this.list.getChild(i);
            Integer channel = (Integer)panel.getUserObject();
            if (!data.containsKey(channel)) {
               return true;
            }
         }

         return false;
      }
   }

   private void updateList() {
      if (this.isDirty()) {
         this.fillList();
      }

      Map<Integer, Pair<String, Integer>> data = ((RedstoneInformationContainer)this.menu).getChannelData();

      for (int i = 0; i < this.list.getChildCount(); i++) {
         Panel panel = (Panel)this.list.getChild(i);
         Integer channel = (Integer)panel.getUserObject();
         Pair<String, Integer> pair = data.get(channel);
         if (pair != null) {
            Label name = (Label)panel.findChild("name");
            ImageChoiceLabel choice = (ImageChoiceLabel)panel.findChild("choice");
            Label value = (Label)panel.findChild("value");
            if (((String)pair.getLeft()).isEmpty()) {
               name.text(String.valueOf(channel));
            } else {
               name.text(channel + " (" + (String)pair.getLeft() + ")");
            }

            choice.setCurrentChoice(pair.getRight() > 0 ? "1" : "0");
            value.text(Integer.toString((Integer)pair.getRight()));
         }
      }
   }

   protected void drawWindow(GuiGraphicsExtractor graphics, float partialTicks, int x, int y) {
      this.updateList();
      super.drawWindow(graphics, partialTicks, x, y);
   }

   private void fillList() {
      this.list.removeChildren();
      Map<Integer, Pair<String, Integer>> data = ((RedstoneInformationContainer)this.menu).getChannelData();
      if (data != null) {
         Set<Integer> channels = data.keySet();
         channels.stream()
            .sorted()
            .forEach(
               channel -> {
                  Panel panel = (Panel)((Panel)Widgets.horizontal().desiredHeight(18)).userObject(channel);
                  ImageChoiceLabel choice = ((ImageChoiceLabel)((ImageChoiceLabel)((ImageChoiceLabel)new ImageChoiceLabel().name("choice")).desiredWidth(16))
                        .desiredHeight(16))
                     .choice("0", "Redstone off", guiElements, 16, 0)
                     .choice("1", "Redstone on", guiElements, 32, 0)
                     .event(newChoice -> this.setRedstone(channel, newChoice));
                  Label valueLabel = (Label)((Label)((Label)Widgets.label("0").name("value")).desiredWidth(30))
                     .horizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
                  panel.children(
                     new Widget[]{
                        ((Label)((Label)Widgets.label(String.valueOf(channel)).name("name")).desiredWidth(60))
                           .horizontalAlignment(HorizontalAlignment.ALIGN_LEFT),
                        choice,
                        valueLabel,
                        Widgets.button("Remove").event(() -> this.removeChannel(channel))
                     }
                  );
                  this.list.children(new Widget[]{panel});
               }
            );
      }
   }
}
