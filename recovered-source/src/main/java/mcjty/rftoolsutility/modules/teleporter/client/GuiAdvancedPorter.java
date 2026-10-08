package mcjty.rftoolsutility.modules.teleporter.client;

import javax.annotation.Nonnull;
import mcjty.lib.gui.GuiItemScreen;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.gui.widgets.Widgets;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsutility.modules.teleporter.items.porter.ChargedPorterItem;
import mcjty.rftoolsutility.setup.CommandHandler;
import mcjty.rftoolsutility.setup.RFToolsUtilityMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class GuiAdvancedPorter extends GuiItemScreen {
   private static final int xSize = 340;
   private static final int ySize = 136;
   private final Panel[] panels = new Panel[8];
   private final TextField[] destinations = new TextField[8];
   private static int target = -1;
   private static int[] targets = new int[8];
   private static String[] names = new String[8];

   public GuiAdvancedPorter() {
      super(340, 136, ChargedPorterItem.MANUAL);
   }

   public static void setInfo(int target, int[] targets, String[] names) {
      GuiAdvancedPorter.target = target;
      GuiAdvancedPorter.targets = targets;
      GuiAdvancedPorter.names = names;
   }

   public void init() {
      super.init();
      int k = (this.width - 340) / 2;
      int l = (this.height - 136) / 2;
      Panel toplevel = (Panel)Widgets.vertical(2, 0).filledRectThickness(2);

      for (int i = 0; i < 8; i++) {
         this.destinations[i] = new TextField();
         this.panels[i] = this.createPanel(this.destinations[i], i);
         toplevel.children(new Widget[]{this.panels[i]});
      }

      toplevel.bounds(k, l, 340, 136);
      this.window = new Window(this, toplevel);
      this.updateInfoFromServer();
   }

   private Panel createPanel(TextField destination, int i) {
      return (Panel)((Panel)Widgets.horizontal().desiredHeight(16))
         .children(new Widget[]{destination, ((Button)((Button)Widgets.button("Set").desiredWidth(30)).desiredHeight(16)).event(() -> {
            if (targets[i] != -1) {
               RFToolsUtilityMessages.sendToServer("setTarget", TypedMap.builder().put(CommandHandler.PARAM_TARGET, targets[i]));
               target = targets[i];
            }
         }), ((Button)((Button)Widgets.button("Clear").desiredWidth(40)).desiredHeight(16)).event(() -> {
            if (targets[i] != -1 && targets[i] == target) {
               target = -1;
            }

            RFToolsUtilityMessages.sendToServer("clearTarget", TypedMap.builder().put(CommandHandler.PARAM_TARGET, i));
            targets[i] = -1;
         })});
   }

   private void updateInfoFromServer() {
      RFToolsUtilityMessages.sendToServer("getTargets");
   }

   private void setTarget(int i) {
      this.panels[i].filledBackground(-1);
      if (targets[i] == -1) {
         this.destinations[i].text("No target set");
      } else {
         this.destinations[i].text(targets[i] + ": " + names[i]);
         this.destinations[i].setSelection(0, 0);
         this.destinations[i].clearSelection();
         if (targets[i] == target) {
            this.panels[i].filledBackground(-1123021);
         }
      }
   }

   protected void renderInternal(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      for (int i = 0; i < 8; i++) {
         this.setTarget(i);
      }

      this.drawWindow(graphics, mouseX, mouseY, partialTicks);
   }

   public static void open() {
      Minecraft.getInstance().setScreen(new GuiAdvancedPorter());
   }
}
