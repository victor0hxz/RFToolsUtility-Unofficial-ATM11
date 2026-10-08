package mcjty.rftoolsutility.modules.teleporter.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import mcjty.lib.base.StyleConfig;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Slider;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.gui.widgets.WidgetList;
import mcjty.lib.gui.widgets.Widgets;
import mcjty.lib.network.Networking;
import mcjty.lib.network.PacketGetListFromServer;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.ClientTools;
import mcjty.rftoolsutility.modules.teleporter.TeleporterModule;
import mcjty.rftoolsutility.modules.teleporter.blocks.MatterTransmitterTileEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class GuiMatterTransmitter extends GenericGuiContainer<MatterTransmitterTileEntity, GenericContainer> {
   public static final int MATTER_WIDTH = 180;
   public static final int MATTER_HEIGHT = 160;
   public static final String ACCESS_PRIVATE = "Private";
   public static final String ACCESS_PUBLIC = "Public";
   private EnergyBar energyBar;
   private ChoiceLabel privateSetting;
   private WidgetList allowedPlayers;
   private Button addButton;
   private Button delButton;
   private TextField playerNameField;
   private List<String> players = null;
   private int listDirty = 0;
   private static Set<String> fromServer_allowedPlayers = new HashSet<>();

   public static void storeAllowedPlayersForClient(List<String> players) {
      fromServer_allowedPlayers = new HashSet<>(players);
   }

   public GuiMatterTransmitter(GenericContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, ((BaseBlock)TeleporterModule.MATTER_TRANSMITTER.block().get()).getManualEntry(), 180, 160);
   }

   public static void register(RegisterMenuScreensEvent event) {
      event.register(TeleporterModule.CONTAINER_MATTER_TRANSMITTER.get(), GuiMatterTransmitter::new);
   }

   public boolean isPauseScreen() {
      return false;
   }

   public void init() {
      super.init();
      this.energyBar = ((EnergyBar)((EnergyBar)((EnergyBar)new EnergyBar().filledRectThickness(1)).horizontal().desiredHeight(12)).desiredWidth(80))
         .showText(false);
      TextField nameField = (TextField)((TextField)new TextField().name("name"))
         .tooltips(new String[]{"Use this name to", "identify this transmitter", "in the dialer"});
      Panel namePanel = (Panel)((Panel)Widgets.horizontal().children(new Widget[]{Widgets.label("Name:"), nameField})).desiredHeight(16);
      this.privateSetting = ((ChoiceLabel)((ChoiceLabel)((ChoiceLabel)new ChoiceLabel().name("private"))
               .choices(new String[]{"Public", "Private"})
               .desiredHeight(14))
            .desiredWidth(60))
         .choiceTooltip("Public", new String[]{"Everyone can access this transmitter", "and change the dialing destination"})
         .choiceTooltip("Private", new String[]{"Only people in the access list below", "can access this transmitter"});
      ToggleButton beamToggle = (ToggleButton)((ToggleButton)((ToggleButton)((ToggleButton)((ToggleButton)new ToggleButton().name("beam")).text("Hide"))
               .checkMarker(true)
               .desiredHeight(14))
            .desiredWidth(49))
         .tooltips(new String[]{"Hide the teleportation beam"});
      Panel privatePanel = (Panel)((Panel)Widgets.horizontal().children(new Widget[]{Widgets.label("Access:"), this.privateSetting, beamToggle}))
         .desiredHeight(16);
      this.allowedPlayers = (WidgetList)new WidgetList().name("allowedplayers");
      Slider allowedPlayerSlider = ((Slider)new Slider().desiredWidth(10)).vertical().scrollableName("allowedplayers");
      Panel allowedPlayersPanel = (Panel)((Panel)Widgets.horizontal(3, 1).children(new Widget[]{this.allowedPlayers, allowedPlayerSlider}))
         .filledBackground(-6381922);
      this.playerNameField = new TextField();
      this.addButton = (Button)((Button)((Button)((Button)Widgets.button("Add").channel("addplayer")).desiredHeight(13)).desiredWidth(34))
         .tooltips(new String[]{"Add a player to the access list"});
      this.delButton = (Button)((Button)((Button)((Button)Widgets.button("Del").channel("delplayer")).desiredHeight(13)).desiredWidth(34))
         .tooltips(new String[]{"Remove the selected player", "from the access list"});
      Panel buttonPanel = (Panel)((Panel)Widgets.horizontal().children(new Widget[]{this.playerNameField, this.addButton, this.delButton})).desiredHeight(16);
      Panel toplevel = (Panel)((Panel)new Panel().filledRectThickness(2))
         .layout(((VerticalLayout)((VerticalLayout)new VerticalLayout().setHorizontalMargin(3)).setVerticalMargin(3)).setSpacing(1))
         .children(new Widget[]{this.energyBar, namePanel, privatePanel, allowedPlayersPanel, buttonPanel});
      toplevel.bounds(this.leftPos, this.topPos, 180, 160);
      this.window = new Window(this, toplevel);
      ClientTools.enableKeyboardRepeat();
      this.listDirty = 0;
      this.requestPlayers();
      MatterTransmitterTileEntity tileEntity = (MatterTransmitterTileEntity)this.getBE();
      this.window.bind("name", tileEntity, "name");
      this.window.bind("private", tileEntity, "private");
      this.window.bind("beam", tileEntity, "beam");
      this.window.event("addplayer", (source, params) -> this.addPlayer());
      this.window.event("delplayer", (source, params) -> this.delPlayer());
   }

   private void addPlayer() {
      this.sendServerCommandTyped(
         MatterTransmitterTileEntity.CMD_ADDPLAYER, TypedMap.builder().put(MatterTransmitterTileEntity.PARAM_PLAYER, this.playerNameField.getText()).build()
      );
      this.listDirty = 0;
   }

   private void delPlayer() {
      String name = this.playerNameField.getText();
      int selected = this.allowedPlayers.getSelected();
      if (selected >= 0 && selected < this.players.size()) {
         name = this.players.get(selected);
      }

      this.sendServerCommandTyped(MatterTransmitterTileEntity.CMD_DELPLAYER, TypedMap.builder().put(MatterTransmitterTileEntity.PARAM_PLAYER, name).build());
      this.listDirty = 0;
   }

   private void requestPlayers() {
      MatterTransmitterTileEntity tileEntity = (MatterTransmitterTileEntity)this.getBE();
      Networking.sendToServer(PacketGetListFromServer.create(tileEntity.getBlockPos(), MatterTransmitterTileEntity.CMD_GETPLAYERS.name()));
   }

   private void populatePlayers() {
      List<String> newPlayers = new ArrayList<>(fromServer_allowedPlayers);
      Collections.sort(newPlayers);
      if (!newPlayers.equals(this.players)) {
         this.players = new ArrayList<>(newPlayers);
         this.allowedPlayers.removeChildren();

         for (String player : this.players) {
            this.allowedPlayers
               .children(
                  new Widget[]{((Label)Widgets.label(player).color(StyleConfig.colorTextInListNormal)).horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)}
               );
         }
      }
   }

   private void requestListsIfNeeded() {
      this.listDirty--;
      if (this.listDirty <= 0) {
         this.requestPlayers();
         this.listDirty = 20;
      }
   }

   private void updateFields() {
      if (this.window != null) {
         this.updateEnergyBar(this.energyBar);
      }
   }

   public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      this.requestListsIfNeeded();
      this.populatePlayers();
      this.enableButtons();
      this.updateFields();
      this.drawWindow(graphics, partialTicks, mouseX, mouseY);
   }

   private void enableButtons() {
      boolean isPrivate = "Private".equals(this.privateSetting.getCurrentChoice());
      this.allowedPlayers.enabled(isPrivate);
      this.playerNameField.enabled(isPrivate);
      int isPlayerSelected = this.allowedPlayers.getSelected();
      this.delButton.enabled(isPrivate && isPlayerSelected != -1);
      String name = this.playerNameField.getText();
      this.addButton.enabled(isPrivate && name != null && !name.isEmpty());
   }
}
