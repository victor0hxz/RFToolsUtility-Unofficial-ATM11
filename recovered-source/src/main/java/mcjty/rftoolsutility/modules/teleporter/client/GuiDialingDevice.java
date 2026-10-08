package mcjty.rftoolsutility.modules.teleporter.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import mcjty.lib.base.StyleConfig;
import mcjty.lib.blockcommands.ICommand;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.DefaultSelectionEvent;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.lib.gui.widgets.ImageLabel;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Slider;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.gui.widgets.WidgetList;
import mcjty.lib.gui.widgets.Widgets;
import mcjty.lib.network.Networking;
import mcjty.lib.network.PacketGetListFromServer;
import mcjty.lib.network.PacketRequestDataFromServer;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.ClientTools;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsbase.RFToolsBase;
import mcjty.rftoolsutility.modules.teleporter.TeleporterModule;
import mcjty.rftoolsutility.modules.teleporter.blocks.DialingDeviceTileEntity;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestination;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestinationClientInfo;
import mcjty.rftoolsutility.modules.teleporter.data.TransmitterInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class GuiDialingDevice extends GenericGuiContainer<DialingDeviceTileEntity, GenericContainer> {
   public static final int DIALER_WIDTH = 256;
   public static final int DIALER_HEIGHT = 224;
   private static final Identifier guielements = Identifier.fromNamespaceAndPath("rftoolsbase", "textures/gui/guielements.png");
   private EnergyBar energyBar;
   private WidgetList transmitterList;
   private WidgetList receiverList;
   private Button dialButton;
   private Button dialOnceButton;
   private Button interruptButton;
   private ImageChoiceLabel favoriteButton;
   private Button statusButton;
   private Label statusLabel;
   private boolean analyzerAvailable = false;
   private boolean lastDialedTransmitter = false;
   private boolean lastCheckedReceiver = false;
   public static int fromServer_receiverStatus = -1;
   public static int fromServer_dialResult = -1;
   public static List<TeleportDestinationClientInfo> fromServer_receivers = null;
   public static List<TransmitterInfo> fromServer_transmitters = null;
   private List<TeleportDestinationClientInfo> receivers = null;
   private boolean receiversFiltered = false;
   private List<TransmitterInfo> transmitters = null;
   private int listDirty = 10;

   public GuiDialingDevice(GenericContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, ((BaseBlock)TeleporterModule.DIALING_DEVICE.block().get()).getManualEntry(), 256, 224);
   }

   public static void setReceiverStatus(int receiverStatus) {
      fromServer_receiverStatus = receiverStatus;
   }

   public static void setDialResult(int dialResult) {
      fromServer_dialResult = dialResult;
   }

   public static void register(RegisterMenuScreensEvent event) {
      event.register(TeleporterModule.CONTAINER_DIALING_DEVICE.get(), GuiDialingDevice::new);
   }

   public void init() {
      super.init();
      Minecraft mc = this.minecraft;
      this.energyBar = ((EnergyBar)((EnergyBar)((EnergyBar)new EnergyBar().filledRectThickness(1)).horizontal().desiredWidth(80)).desiredHeight(12))
         .showText(false);
      Panel transmitterPanel = this.setupTransmitterPanel();
      Panel receiverPanel = this.setupReceiverPanel();
      this.dialButton = (Button)((Button)((Button)((Button)Widgets.button("Dial").channel("dial"))
               .tooltips(new String[]{"Start a connection between", "the selected transmitter", "and the selected receiver"}))
            .desiredHeight(14))
         .desiredWidth(65);
      this.dialOnceButton = (Button)((Button)((Button)((Button)Widgets.button("Dial once").channel("dialonce"))
               .tooltips(new String[]{"Dial a connection for a", "single teleport"}))
            .desiredHeight(14))
         .desiredWidth(65);
      this.interruptButton = (Button)((Button)((Button)((Button)Widgets.button("Interrupt").channel("interrupt"))
               .tooltips(new String[]{"Interrupt a connection", "for the selected transmitter"}))
            .desiredHeight(14))
         .desiredWidth(65);
      this.favoriteButton = (ImageChoiceLabel)((ImageChoiceLabel)((ImageChoiceLabel)new ImageChoiceLabel().channel("favorite")).desiredWidth(10))
         .desiredHeight(10);
      this.favoriteButton.choice("No", "Unfavorited receiver", guielements, 131, 19);
      this.favoriteButton.choice("Yes", "Favorited receiver", guielements, 115, 19);
      DialingDeviceTileEntity tileEntity = (DialingDeviceTileEntity)this.getBE();
      this.favoriteButton.setCurrentChoice(tileEntity.isShowOnlyFavorites() ? 1 : 0);
      Panel buttonPanel = (Panel)((Panel)Widgets.horizontal()
            .children(new Widget[]{this.dialButton, this.dialOnceButton, this.interruptButton, this.favoriteButton}))
         .desiredHeight(16);
      this.analyzerAvailable = DialingDeviceTileEntity.isDestinationAnalyzerAvailable(mc.level, tileEntity.getBlockPos());
      this.statusButton = (Button)((Button)((Button)((Button)Widgets.button("Check").channel("check")).desiredHeight(14)).desiredWidth(65))
         .enabled(this.analyzerAvailable);
      if (this.analyzerAvailable) {
         this.statusButton.tooltips(new String[]{"Check the status of", "the selected receiver"});
      } else {
         this.statusButton.tooltips(new String[]{"Check the status of", "the selected receiver", "(needs an adjacent analyzer!)"});
      }

      this.statusLabel = new Label();
      ((Label)((Label)this.statusLabel.desiredWidth(170)).desiredHeight(14)).filledRectThickness(1);
      Panel statusPanel = (Panel)((Panel)Widgets.horizontal().children(new Widget[]{this.statusButton, this.statusLabel})).desiredHeight(16);
      Panel toplevel = (Panel)((Panel)Widgets.vertical(3, 1).filledRectThickness(2))
         .children(new Widget[]{this.energyBar, transmitterPanel, receiverPanel, buttonPanel, statusPanel});
      toplevel.bounds(this.leftPos, this.topPos, 256, 224);
      this.window = new Window(this, toplevel);
      this.window.event("dial", (souce, params) -> this.dial(false));
      this.window.event("dialonce", (souce, params) -> this.dial(true));
      this.window.event("interrupt", (souce, params) -> this.interruptDial());
      this.window.event("favorite", (souce, params) -> this.changeShowFavorite());
      this.window.event("check", (souce, params) -> this.checkStatus());
      ClientTools.enableKeyboardRepeat();
      fromServer_receivers = null;
      fromServer_transmitters = null;
      this.listDirty = 0;
      this.clearSelectedStatus();
      this.requestReceivers();
      this.requestTransmitters();
   }

   private Panel setupReceiverPanel() {
      this.receiverList = ((WidgetList)((WidgetList)new WidgetList().name("receivers")).rowheight(14).desiredHeight(100))
         .propagateEventsToChildren(true)
         .event(new DefaultSelectionEvent() {
            {
               Objects.requireNonNull(GuiDialingDevice.this);
            }

            public void select(int index) {
               GuiDialingDevice.this.clearSelectedStatus();
            }

            public void doubleClick(int index) {
               GuiDialingDevice.this.hilightSelectedReceiver(index);
            }
         });
      Slider receiverSlider = ((Slider)((Slider)new Slider().desiredWidth(11)).desiredHeight(100)).vertical().scrollableName("receivers");
      return (Panel)((Panel)((Panel)Widgets.horizontal(3, 1).children(new Widget[]{this.receiverList, receiverSlider})).desiredHeight(106))
         .filledBackground(-6381922);
   }

   private Panel setupTransmitterPanel() {
      this.transmitterList = ((WidgetList)((WidgetList)new WidgetList().name("transmitters")).rowheight(18).desiredHeight(58))
         .event(new DefaultSelectionEvent() {
            {
               Objects.requireNonNull(GuiDialingDevice.this);
            }

            public void select(int index) {
               GuiDialingDevice.this.clearSelectedStatus();
               GuiDialingDevice.this.selectReceiverFromTransmitter();
            }

            public void doubleClick(int index) {
               GuiDialingDevice.this.hilightSelectedTransmitter(index);
            }
         });
      Slider transmitterSlider = ((Slider)((Slider)new Slider().desiredWidth(11)).desiredHeight(58)).vertical().scrollableName("transmitters");
      return (Panel)((Panel)((Panel)Widgets.horizontal(3, 1).children(new Widget[]{this.transmitterList, transmitterSlider})).desiredHeight(64))
         .filledBackground(-6381922);
   }

   private void clearSelectedStatus() {
      this.lastDialedTransmitter = false;
      this.lastCheckedReceiver = false;
   }

   private void hilightSelectedTransmitter(int index) {
      TransmitterInfo transmitterInfo = this.getSelectedTransmitter(index);
      if (transmitterInfo != null) {
         BlockPos c = transmitterInfo.getCoordinate();
         RFToolsBase.instance.clientInfo.hilightBlock(c, System.currentTimeMillis() + 5000L);
         this.minecraft.player.closeContainer();
      }
   }

   private void hilightSelectedReceiver(int index) {
      TeleportDestinationClientInfo destination = this.getSelectedReceiver(index);
      if (destination != null && BlockPosTools.isValid(destination.destination().getCoordinate())) {
         BlockPos c = destination.destination().getCoordinate();
         double distance = new Vec3(c.getX(), c.getY(), c.getZ()).distanceTo(this.minecraft.player.position());
         if (destination.destination().getDimension().equals(this.minecraft.level.dimension()) && !(distance > 150.0)) {
            RFToolsBase.instance.clientInfo.hilightBlock(c, System.currentTimeMillis() + 5000L);
            Logging.message(this.minecraft.player, "The receiver is now highlighted");
            this.minecraft.player.closeContainer();
         } else {
            Logging.warn(this.minecraft.player, "Receiver is too far to hilight!");
            this.minecraft.player.closeContainer();
         }
      }
   }

   private void setStatusError(String message) {
      this.statusLabel.text(message);
      this.statusLabel.color(-1);
      this.statusLabel.filledBackground(-65536, -7864320);
   }

   private void setStatusMessage(String message) {
      this.statusLabel.text(message);
      this.statusLabel.color(-16777216);
      this.statusLabel.filledBackground(-16711936, -16742400);
   }

   private void checkStatus() {
      int receiverSelected = this.receiverList.getSelected();
      TeleportDestinationClientInfo destination = this.getSelectedReceiver(receiverSelected);
      if (destination != null && BlockPosTools.isValid(destination.destination().getCoordinate())) {
         BlockPos c = destination.destination().getCoordinate();
         TypedMap params = TypedMap.builder()
            .put(DialingDeviceTileEntity.PARAM_POS, c)
            .put(DialingDeviceTileEntity.PARAM_DIMENSION, destination.destination().getDimension().identifier().toString())
            .build();
         DialingDeviceTileEntity tileEntity = (DialingDeviceTileEntity)this.getBE();
         Networking.sendToServer(
            PacketRequestDataFromServer.create(
               tileEntity.getDimension(), tileEntity.getBlockPos(), DialingDeviceTileEntity.CMD_CHECKSTATUS.name(), params, false
            )
         );
         this.lastCheckedReceiver = true;
         this.listDirty = 0;
      }
   }

   private void showStatus(int dialResult) {
      if ((dialResult & 8) != 0) {
         this.setStatusError("Dialing device power low!");
      } else if ((dialResult & 16) != 0) {
         this.setStatusError("Matter receiver power low!");
      } else if ((dialResult & 512) != 0) {
         this.setStatusError("Destination dimension power low!");
      } else if ((dialResult & 32) != 0) {
         this.setStatusError("No access to transmitter!");
      } else if ((dialResult & 64) != 0) {
         this.setStatusError("No access to receiver!");
      } else if ((dialResult & 4) != 0) {
         this.setStatusError("Invalid destination!");
      } else if ((dialResult & 256) != 0) {
         this.setStatusError("Invalid source!");
      } else if ((dialResult & 1) != 0) {
         this.setStatusError("Receiver blocked!");
      } else if ((dialResult & 2) != 0) {
         this.setStatusError("Transmitter blocked!");
      } else if ((dialResult & 1024) != 0) {
         this.setStatusError("Invalid transmitter!!");
      } else if ((dialResult & 128) != 0) {
         this.setStatusMessage("Interrupted!");
      } else {
         this.setStatusMessage("Dial ok!");
      }
   }

   private void selectReceiverFromTransmitter() {
      this.receiverList.selected(-1);
      TeleportDestination destination = this.getSelectedTransmitterDestination();
      if (destination != null && destination.getDimension() != null) {
         int i = 0;

         for (TeleportDestinationClientInfo receiver : this.receivers) {
            if (receiver.destination().getDimension() == destination.getDimension()
               && receiver.destination().getCoordinate().equals(destination.getCoordinate())) {
               this.receiverList.selected(i);
               return;
            }

            i++;
         }
      }
   }

   private void dial(boolean once) {
      int transmitterSelected = this.transmitterList.getSelected();
      TransmitterInfo transmitterInfo = this.getSelectedTransmitter(transmitterSelected);
      if (transmitterInfo != null) {
         int receiverSelected = this.receiverList.getSelected();
         TeleportDestinationClientInfo destination = this.getSelectedReceiver(receiverSelected);
         if (destination != null && BlockPosTools.isValid(destination.destination().getCoordinate())) {
            ICommand command = once ? DialingDeviceTileEntity.CMD_DIALONCE : DialingDeviceTileEntity.CMD_DIAL;
            TypedMap params = TypedMap.builder()
               .put(DialingDeviceTileEntity.PARAM_PLAYER_UUID, this.minecraft.player.getUUID())
               .put(DialingDeviceTileEntity.PARAM_TRANSMITTER, transmitterInfo.getCoordinate())
               .put(DialingDeviceTileEntity.PARAM_TRANS_DIMENSION, this.minecraft.level.dimension().identifier().toString())
               .put(DialingDeviceTileEntity.PARAM_POS, destination.destination().getCoordinate())
               .put(DialingDeviceTileEntity.PARAM_DIMENSION, destination.destination().getDimension().identifier().toString())
               .build();
            DialingDeviceTileEntity tileEntity = (DialingDeviceTileEntity)this.getBE();
            Networking.sendToServer(PacketRequestDataFromServer.create(tileEntity.getDimension(), tileEntity.getBlockPos(), command.name(), params, false));
            this.lastDialedTransmitter = true;
            this.listDirty = 0;
         }
      }
   }

   private TeleportDestinationClientInfo getSelectedReceiver(int receiverSelected) {
      if (receiverSelected == -1) {
         return null;
      } else {
         return receiverSelected >= this.receivers.size() ? null : this.receivers.get(receiverSelected);
      }
   }

   private void interruptDial() {
      int transmitterSelected = this.transmitterList.getSelected();
      TransmitterInfo transmitterInfo = this.getSelectedTransmitter(transmitterSelected);
      if (transmitterInfo != null) {
         TypedMap params = TypedMap.builder()
            .put(DialingDeviceTileEntity.PARAM_PLAYER_UUID, this.minecraft.player.getUUID())
            .put(DialingDeviceTileEntity.PARAM_TRANSMITTER, transmitterInfo.getCoordinate())
            .put(DialingDeviceTileEntity.PARAM_TRANS_DIMENSION, this.minecraft.level.dimension().identifier().toString())
            .put(DialingDeviceTileEntity.PARAM_POS, null)
            .put(DialingDeviceTileEntity.PARAM_DIMENSION, Level.OVERWORLD.identifier().toString())
            .build();
         DialingDeviceTileEntity tileEntity = (DialingDeviceTileEntity)this.getBE();
         Networking.sendToServer(
            PacketRequestDataFromServer.create(tileEntity.getDimension(), tileEntity.getBlockPos(), DialingDeviceTileEntity.CMD_DIAL.name(), params, false)
         );
         this.lastDialedTransmitter = true;
         this.listDirty = 0;
      }
   }

   private void requestReceivers() {
      TypedMap params = TypedMap.builder().put(DialingDeviceTileEntity.PARAM_PLAYER_UUID, this.minecraft.player.getUUID()).build();
      DialingDeviceTileEntity tileEntity = (DialingDeviceTileEntity)this.getBE();
      Networking.sendToServer(PacketGetListFromServer.create(tileEntity.getBlockPos(), DialingDeviceTileEntity.CMD_GETRECEIVERS.name(), params));
   }

   private void requestTransmitters() {
      DialingDeviceTileEntity tileEntity = (DialingDeviceTileEntity)this.getBE();
      Networking.sendToServer(PacketGetListFromServer.create(tileEntity.getBlockPos(), DialingDeviceTileEntity.CMD_GETTRANSMITTERS.name()));
   }

   private void changeShowFavorite() {
      boolean fav = this.favoriteButton.getCurrentChoiceIndex() == 1;
      this.sendServerCommandTyped(DialingDeviceTileEntity.CMD_SHOWFAVORITE, TypedMap.builder().put(DialingDeviceTileEntity.PARAM_FAVORITE, fav).build());
      this.listDirty = 0;
      this.transmitterList.selected(-1);
      this.receiverList.selected(-1);
   }

   private void changeFavorite() {
      int receiverSelected = this.receiverList.getSelected();
      TeleportDestinationClientInfo destination = this.getSelectedReceiver(receiverSelected);
      if (destination != null && BlockPosTools.isValid(destination.destination().getCoordinate())) {
         boolean favorite = destination.isFavorite();
         destination = destination.withFavorite(!favorite);
         this.receivers.add(receiverSelected, destination);
         this.sendServerCommandTyped(
            DialingDeviceTileEntity.CMD_FAVORITE,
            TypedMap.builder()
               .put(DialingDeviceTileEntity.PARAM_PLAYER, this.minecraft.player.getName().getString())
               .put(DialingDeviceTileEntity.PARAM_POS, destination.destination().getCoordinate())
               .put(DialingDeviceTileEntity.PARAM_DIMENSION, destination.destination().getDimension().identifier().toString())
               .put(DialingDeviceTileEntity.PARAM_FAVORITE, !favorite)
               .build()
         );
         this.listDirty = 0;
      }
   }

   private void populateReceivers() {
      List<TeleportDestinationClientInfo> newReceivers = fromServer_receivers;
      if (newReceivers != null) {
         boolean newReceiversFiltered = this.favoriteButton.getCurrentChoiceIndex() == 1;
         if (!newReceivers.equals(this.receivers) || newReceiversFiltered != this.receiversFiltered) {
            this.receiversFiltered = newReceiversFiltered;
            if (this.receiversFiltered) {
               this.receivers = new ArrayList<>();

               for (TeleportDestinationClientInfo receiver : newReceivers) {
                  if (receiver.isFavorite()) {
                     this.receivers.add(receiver);
                  }
               }
            } else {
               this.receivers = new ArrayList<>(newReceivers);
            }

            this.receiverList.removeChildren();

            for (TeleportDestinationClientInfo destination : this.receivers) {
               BlockPos coordinate = destination.destination().getCoordinate();
               String dimName = destination.getDimensionName();
               if (!BlockPosTools.isValid(coordinate) || dimName.trim().isEmpty()) {
                  dimName = "Id " + destination.destination().getDimension();
               }

               boolean favorite = destination.isFavorite();
               Panel panel = Widgets.horizontal(3, 1);
               panel.children(
                  new Widget[]{
                     ((Label)((Label)((Label)Widgets.label(destination.destination().getName()).color(StyleConfig.colorTextInListNormal))
                              .horizontalAlignment(HorizontalAlignment.ALIGN_LEFT))
                           .desiredWidth(96))
                        .tooltips(
                           new String[]{
                              "The name of the", "destination receiver:", destination.destination().getName() + " (" + BlockPosTools.toString(coordinate) + ")"
                           }
                        )
                  }
               );
               panel.children(
                  new Widget[]{
                     ((Label)((Label)((Label)((Label)Widgets.label(dimName).color(StyleConfig.colorTextInListNormal))
                                 .horizontalAlignment(HorizontalAlignment.ALIGN_LEFT))
                              .dynamic(true))
                           .tooltips(new String[]{"The name of the", "destination dimension:", dimName}))
                        .desiredWidth(110)
                  }
               );
               ImageChoiceLabel choiceLabel = (ImageChoiceLabel)new ImageChoiceLabel().event(newChoice -> this.changeFavorite()).desiredWidth(10);
               choiceLabel.choice("No", "Not favorited", guielements, 131, 19);
               choiceLabel.choice("Yes", "Favorited", guielements, 115, 19);
               choiceLabel.setCurrentChoice(favorite ? 1 : 0);
               panel.children(new Widget[]{choiceLabel});
               this.receiverList.children(new Widget[]{panel});
            }
         }
      }
   }

   private TeleportDestination getSelectedTransmitterDestination() {
      int transmitterSelected = this.transmitterList.getSelected();
      TransmitterInfo transmitterInfo = this.getSelectedTransmitter(transmitterSelected);
      if (transmitterInfo == null) {
         return null;
      } else {
         TeleportDestination destination = transmitterInfo.getTeleportDestination();
         return destination.isValid() ? destination : null;
      }
   }

   private void populateTransmitters() {
      List<TransmitterInfo> newTransmitters = fromServer_transmitters;
      if (newTransmitters != null) {
         if (!newTransmitters.equals(this.transmitters)) {
            this.transmitters = new ArrayList<>(newTransmitters);
            this.transmitterList.removeChildren();

            for (TransmitterInfo transmitterInfo : this.transmitters) {
               BlockPos coordinate = transmitterInfo.getCoordinate();
               TeleportDestination destination = transmitterInfo.getTeleportDestination();
               Panel panel = (Panel)Widgets.horizontal(3, 5)
                  .children(
                     new Widget[]{
                        ((Label)((Label)Widgets.label(transmitterInfo.getName()).color(StyleConfig.colorTextInListNormal))
                              .horizontalAlignment(HorizontalAlignment.ALIGN_LEFT))
                           .desiredWidth(102),
                        ((Label)((Label)Widgets.label(BlockPosTools.toString(coordinate)).color(StyleConfig.colorTextInListNormal)).dynamic(true))
                           .desiredWidth(90),
                        ((ImageLabel)new ImageLabel().image(guielements, destination.isValid() ? 80 : 96, 0)).desiredWidth(16)
                     }
                  );
               this.transmitterList.children(new Widget[]{panel});
            }

            if (this.transmitterList.getChildCount() == 1) {
               this.transmitterList.selected(0);
            }
         }
      }
   }

   public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      this.requestListsIfNeeded();
      this.populateReceivers();
      this.populateTransmitters();
      if (this.lastDialedTransmitter) {
         this.showStatus(fromServer_dialResult);
      } else if (this.lastCheckedReceiver) {
         this.showStatus(fromServer_receiverStatus);
      } else {
         this.statusLabel.text("");
         this.statusLabel.color(-16777216);
         this.statusLabel.filledBackground(-1);
      }

      this.enableButtons();
      this.drawWindow(graphics, partialTicks, mouseX, mouseY);
      this.updateEnergyBar(this.energyBar);
   }

   private void requestListsIfNeeded() {
      if (fromServer_receivers == null || fromServer_transmitters == null) {
         this.listDirty--;
         if (this.listDirty <= 0) {
            this.requestReceivers();
            this.requestTransmitters();
            this.listDirty = 10;
         }
      }
   }

   private String calculateDistance(int transmitterSelected, int receiverSelected) {
      TransmitterInfo transmitterInfo = this.getSelectedTransmitter(transmitterSelected);
      if (transmitterInfo == null) {
         return "?";
      } else {
         TeleportDestinationClientInfo teleportDestination = this.getSelectedReceiver(receiverSelected);
         return teleportDestination == null
            ? "?"
            : DialingDeviceTileEntity.calculateDistance(this.minecraft.level, transmitterInfo, teleportDestination.destination());
      }
   }

   private TransmitterInfo getSelectedTransmitter(int transmitterSelected) {
      if (transmitterSelected == -1) {
         return null;
      } else {
         return transmitterSelected >= this.transmitters.size() ? null : this.transmitters.get(transmitterSelected);
      }
   }

   private void enableButtons() {
      int transmitterSelected = this.transmitterList.getSelected();
      if (this.transmitters == null || transmitterSelected >= this.transmitters.size()) {
         transmitterSelected = -1;
         this.transmitterList.selected(-1);
      }

      int receiverSelected = this.receiverList.getSelected();
      if (this.receivers == null || receiverSelected >= this.receivers.size()) {
         receiverSelected = -1;
         this.receiverList.selected(-1);
      }

      if (transmitterSelected != -1 && receiverSelected != -1) {
         this.dialButton.enabled(true);
         this.dialOnceButton.enabled(true);
         String distance = this.calculateDistance(transmitterSelected, receiverSelected);
         this.dialButton
            .tooltips(new String[]{"Start a connection between", "the selected transmitter", "and the selected receiver.", "Distance: " + distance});
         this.dialOnceButton.tooltips(new String[]{"Dial a connection for a", "single teleport.", "Distance: " + distance});
      } else {
         this.dialButton.enabled(false);
         this.dialOnceButton.enabled(false);
         this.dialButton.tooltips(new String[]{"Start a connection between", "the selected transmitter", "and the selected receiver"});
         this.dialOnceButton.tooltips(new String[]{"Dial a connection for a", "single teleport"});
      }

      if (transmitterSelected != -1) {
         TeleportDestination destination = this.getSelectedTransmitterDestination();
         this.interruptButton.enabled(destination != null);
      } else {
         this.interruptButton.enabled(false);
      }

      if (receiverSelected != -1) {
         this.statusButton.enabled(this.analyzerAvailable);
      } else {
         this.statusButton.enabled(false);
      }
   }
}
