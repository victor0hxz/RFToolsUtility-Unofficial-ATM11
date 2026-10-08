package mcjty.rftoolsutility.modules.environmental.client;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mcjty.lib.base.StyleConfig;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.lib.gui.widgets.IntegerField;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.ScrollableLabel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.gui.widgets.WidgetList;
import mcjty.lib.network.Networking;
import mcjty.lib.network.PacketGetListFromServer;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsutility.modules.environmental.EnvironmentalModule;
import mcjty.rftoolsutility.modules.environmental.blocks.EnvironmentalControllerTileEntity;
import mcjty.rftoolsutility.modules.environmental.blocks.EnvironmentalMode;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class GuiEnvironmentalController extends GenericGuiContainer<EnvironmentalControllerTileEntity, GenericContainer> {
   public static final String BLACKLIST = "BL";
   public static final String WHITELIST = "WL";
   public static final String HOSTILE = "Host";
   public static final String PASSIVE = "Pass";
   public static final String MOBS = "Mobs";
   public static final String ALL = "All";
   private List<String> players = null;
   private int listDirty = 0;
   private int updateInhibit = 0;
   private EnergyBar energyBar;
   private IntegerField minyTextField;
   private IntegerField maxyTextField;
   private TextField nameField;
   private WidgetList playersList;
   private ChoiceLabel modeLabel;

   public GuiEnvironmentalController(GenericContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, ManualHelper.create("rftoolsbase:machines/environmental"));
   }

   public static void register(RegisterMenuScreensEvent event) {
      event.register(EnvironmentalModule.CONTAINER_ENVIRONENTAL_CONTROLLER.get(), GuiEnvironmentalController::new);
   }

   public void init() {
      this.window = new Window(this, this.getBE(), Identifier.fromNamespaceAndPath("rftoolsutility", "gui/environmental.gui"));
      super.init();
      this.initializeFields();
      this.setupEvents();
      this.listDirty = 0;
      this.requestPlayers();
   }

   private void initializeFields() {
      this.energyBar = (EnergyBar)this.window.findChild("energybar");
      this.playersList = (WidgetList)this.window.findChild("players");
      this.minyTextField = (IntegerField)this.window.findChild("miny");
      this.maxyTextField = (IntegerField)this.window.findChild("maxy");
      this.nameField = (TextField)this.window.findChild("name");
      this.modeLabel = (ChoiceLabel)this.window.findChild("mode");
   }

   private void updateFields() {
      if (this.window != null) {
         EnvironmentalControllerTileEntity tileEntity = (EnvironmentalControllerTileEntity)this.getBE();
         ((ImageChoiceLabel)this.window.findChild("redstone")).setCurrentChoice(tileEntity.getRSMode().ordinal());
         int r = tileEntity.getRadius();
         if (r < 5) {
            r = 5;
         } else if (r > 100) {
            r = 100;
         }

         if (this.updateInhibit <= 0) {
            ((ScrollableLabel)this.window.findChild("radius")).realValue(r);
            this.minyTextField.integer(tileEntity.getMiny());
            this.maxyTextField.integer(tileEntity.getMaxy());
         } else {
            this.updateInhibit--;
         }

         switch (tileEntity.getMode()) {
            case MODE_BLACKLIST:
               this.modeLabel.choice("BL");
               break;
            case MODE_WHITELIST:
               this.modeLabel.choice("WL");
               break;
            case MODE_HOSTILE:
               this.modeLabel.choice("Host");
               break;
            case MODE_PASSIVE:
               this.modeLabel.choice("Pass");
               break;
            case MODE_MOBS:
               this.modeLabel.choice("Mobs");
               break;
            case MODE_ALL:
               this.modeLabel.choice("All");
         }

         this.updateEnergyBar(this.energyBar);
      }
   }

   private void setupEvents() {
      this.window.event("add", (source, params) -> this.addPlayer());
      this.window.event("del", (source, params) -> this.delPlayer());
      this.window.event("mode", (source, params) -> this.changeMode((String)params.get(ChoiceLabel.PARAM_CHOICE)));
      this.window.event("miny", (source, params) -> this.sendBounds());
      this.window.event("maxy", (source, params) -> this.sendBounds());
   }

   private void changeMode(String newAccess) {
      EnvironmentalMode newmode;
      if ("All".equals(newAccess)) {
         newmode = EnvironmentalMode.MODE_ALL;
      } else if ("BL".equals(newAccess)) {
         newmode = EnvironmentalMode.MODE_BLACKLIST;
      } else if ("WL".equals(newAccess)) {
         newmode = EnvironmentalMode.MODE_WHITELIST;
      } else if ("Mobs".equals(newAccess)) {
         newmode = EnvironmentalMode.MODE_MOBS;
      } else if ("Pass".equals(newAccess)) {
         newmode = EnvironmentalMode.MODE_PASSIVE;
      } else {
         newmode = EnvironmentalMode.MODE_HOSTILE;
      }

      this.sendServerCommandTyped(
         EnvironmentalControllerTileEntity.CMD_SETMODE, TypedMap.builder().put(EnvironmentalControllerTileEntity.PARAM_MODE, newmode.ordinal()).build()
      );
   }

   private void addPlayer() {
      this.sendServerCommandTyped(
         EnvironmentalControllerTileEntity.CMD_ADDPLAYER,
         TypedMap.builder().put(EnvironmentalControllerTileEntity.PARAM_NAME, this.nameField.getText()).build()
      );
      this.listDirty = 0;
   }

   private void delPlayer() {
      this.sendServerCommandTyped(
         EnvironmentalControllerTileEntity.CMD_DELPLAYER,
         TypedMap.builder().put(EnvironmentalControllerTileEntity.PARAM_NAME, this.players.get(this.playersList.getSelected())).build()
      );
      this.listDirty = 0;
   }

   private void requestPlayers() {
      EnvironmentalControllerTileEntity tileEntity = (EnvironmentalControllerTileEntity)this.getBE();
      Networking.sendToServer(PacketGetListFromServer.create(tileEntity.getBlockPos(), EnvironmentalControllerTileEntity.CMD_GETPLAYERS.name()));
   }

   private void populatePlayers() {
      EnvironmentalControllerTileEntity tileEntity = (EnvironmentalControllerTileEntity)this.getBE();
      this.players = new ArrayList<>(tileEntity.getPlayersAsList());
      this.players.sort(null);
      this.playersList.removeChildren();

      for (String player : this.players) {
         this.playersList
            .children(
               new Widget[]{
                  ((Label)((Label)new Label().text(player)).color(StyleConfig.colorTextInListNormal)).horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)
               }
            );
      }
   }

   private void requestListsIfNeeded() {
      this.listDirty--;
      if (this.listDirty <= 0) {
         this.requestPlayers();
         this.listDirty = 20;
      }
   }

   private void sendBounds() {
      int miny = this.minyTextField.getInt();
      int maxy = this.maxyTextField.getInt();
      this.updateInhibit = 10;
      this.sendServerCommandTyped(
         EnvironmentalControllerTileEntity.CMD_SETBOUNDS,
         TypedMap.builder().put(EnvironmentalControllerTileEntity.PARAM_MIN, miny).put(EnvironmentalControllerTileEntity.PARAM_MAX, maxy).build()
      );
   }

   public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      this.updateFields();
      this.requestListsIfNeeded();
      this.populatePlayers();
      this.enableButtons();
      this.drawWindow(graphics, partialTicks, mouseX, mouseY);
   }

   private void enableButtons() {
      this.window.setFlag("selected", this.playersList.getSelected() != -1);
      String name = this.nameField.getText();
      this.window.setFlag("name", name != null && !name.isEmpty());
   }
}
