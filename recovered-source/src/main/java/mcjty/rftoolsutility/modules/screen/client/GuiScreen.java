package mcjty.rftoolsutility.modules.screen.client;

import javax.annotation.Nonnull;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.gui.widgets.Widgets;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.ClientTools;
import mcjty.lib.varia.LegacyCapabilities;
import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleProvider;
import mcjty.rftoolsutility.modules.screen.ScreenModule;
import mcjty.rftoolsutility.modules.screen.blocks.ScreenBlock;
import mcjty.rftoolsutility.modules.screen.blocks.ScreenContainer;
import mcjty.rftoolsutility.modules.screen.blocks.ScreenTileEntity;
import mcjty.rftoolsutility.modules.screen.modulesclient.helper.ScreenModuleGuiBuilder;
import mcjty.rftoolsutility.modules.screen.network.PacketModuleUpdate;
import mcjty.rftoolsutility.setup.RFToolsUtilityMessages;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class GuiScreen extends GenericGuiContainer<ScreenTileEntity, ScreenContainer> {
   public static final int SCREEN_WIDTH = 256;
   public static final int SCREEN_HEIGHT = 224;
   private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath("rftoolsutility", "textures/gui/screen.png");
   private Panel toplevel;
   private final ToggleButton[] toggleButtons = new ToggleButton[11];
   private final Panel[] modulePanels = new Panel[11];
   private final IClientScreenModule<?>[] clientScreenModules = new IClientScreenModule[11];
   private final ItemStack[] cachedModuleStacks = new ItemStack[11];
   private ChoiceLabel trueType;
   private int selected = -1;

   public GuiScreen(ScreenContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, ((ScreenBlock)ScreenModule.SCREEN.block().get()).getManualEntry(), 256, 224);
   }

   public static void register(RegisterMenuScreensEvent event) {
      event.register(ScreenModule.CONTAINER_SCREEN.get(), GuiScreen::new);
   }

   public void init() {
      super.init();
      this.toplevel = (Panel)Widgets.positional().background(BACKGROUND);

      for (int i = 0; i < 11; i++) {
         this.toggleButtons[i] = (ToggleButton)((ToggleButton)((ToggleButton)new ToggleButton().hint(30, 7 + i * 18 + 1, 40, 16)).enabled(false))
            .tooltips(new String[]{"Open the gui for this", "module"});
         int finalI = i;
         this.toggleButtons[i].event(() -> this.selectPanel(finalI));
         this.toplevel.children(new Widget[]{this.toggleButtons[i]});
         this.modulePanels[i] = null;
         this.clientScreenModules[i] = null;
         this.cachedModuleStacks[i] = ItemStack.EMPTY;
      }

      ToggleButton bright = (ToggleButton)((ToggleButton)((ToggleButton)((ToggleButton)new ToggleButton().name("bright")).text("Bright"))
            .checkMarker(true)
            .tooltips(new String[]{"Toggle full brightness"}))
         .hint(85, 123, 55, 14);
      this.toplevel.children(new Widget[]{bright, Widgets.label(144, 123, 30, 14, "Font:").horizontalAlignment(HorizontalAlignment.ALIGN_RIGHT)});
      this.trueType = (ChoiceLabel)((ChoiceLabel)new ChoiceLabel()
            .choices(new String[]{"Default", "Truetype", "Vanilla"})
            .tooltips(new String[]{"Set truetype font mode", "for the screen"}))
         .hint(179, 123, 68, 14);
      ScreenTileEntity be = (ScreenTileEntity)this.getBE();
      int trueTypeMode = be.getTrueTypeMode();
      if (trueTypeMode == 0) {
         this.trueType.choice("Default");
      } else if (trueTypeMode == -1) {
         this.trueType.choice("Vanilla");
      } else {
         this.trueType.choice("Truetype");
      }

      this.trueType
         .event(
            b -> this.sendServerCommandTyped(
               ScreenTileEntity.CMD_SETTRUETYPE, TypedMap.builder().put(ScreenTileEntity.PARAM_TRUETYPE, this.getCurrentTruetypeChoice()).build()
            )
         );
      this.toplevel.children(new Widget[]{this.trueType});
      this.toplevel.bounds(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
      this.window = new Window(this, this.toplevel);
      this.window.bind("bright", be, "bright");
      ClientTools.enableKeyboardRepeat();
      this.selected = -1;
   }

   private int getCurrentTruetypeChoice() {
      String c = this.trueType.getCurrentChoice();
      if ("Default".equals(c)) {
         return 0;
      } else {
         return "Truetype".equals(c) ? 1 : -1;
      }
   }

   private void selectPanel(int i) {
      if (this.toggleButtons[i].isPressed()) {
         this.selected = i;
      } else {
         this.selected = -1;
      }
   }

   private void refreshButtons() {
      IItemHandler h = (IItemHandler)this.getBE().getLevel().getCapability(LegacyCapabilities.ITEM_BLOCK, this.getBE().getBlockPos(), null);
      if (h != null) {
         for (int i = 0; i < 11; i++) {
            ItemStack slot = h.getStackInSlot(i);
            if (!slot.isEmpty() && ScreenBlock.hasModuleProvider(slot)) {
               IModuleProvider moduleProvider = ScreenBlock.getModuleProvider(slot);
               if (moduleProvider != null) {
                  this.installModuleGui(i, slot, moduleProvider);
               }
            } else {
               this.uninstallModuleGui(i);
            }

            if (this.modulePanels[i] != null) {
               this.modulePanels[i].visible(this.selected == i);
               this.toggleButtons[i].pressed(this.selected == i);
            }
         }
      }
   }

   private void uninstallModuleGui(int i) {
      this.toggleButtons[i].enabled(false);
      this.toggleButtons[i].pressed(false);
      this.toggleButtons[i].text("");
      this.clientScreenModules[i] = null;
      this.cachedModuleStacks[i] = ItemStack.EMPTY;
      this.toplevel.removeChild(this.modulePanels[i]);
      this.modulePanels[i] = null;
      if (this.selected == i) {
         this.selected = -1;
      }
   }

   private void installModuleGui(int i, ItemStack slot, IModuleProvider moduleProvider) {
      if (this.modulePanels[i] == null || !ItemStack.isSameItemSameComponents(slot, this.cachedModuleStacks[i])) {
         this.toggleButtons[i].enabled(true);
         this.toplevel.removeChild(this.modulePanels[i]);
         IClientScreenModule<?> clientScreenModule = moduleProvider.createClientScreenModule();
         this.clientScreenModules[i] = clientScreenModule;
         this.cachedModuleStacks[i] = slot.copy();
         ScreenModuleGuiBuilder guiBuilder = new ScreenModuleGuiBuilder(this.minecraft, this, slot, () -> {
            IItemHandler handler = (IItemHandler)this.getBE().getLevel().getCapability(LegacyCapabilities.ITEM_BLOCK, this.getBE().getBlockPos(), null);
            if (handler instanceof IItemHandlerModifiable) {
               ((IItemHandlerModifiable)handler).setStackInSlot(i, slot);
            }

            this.cachedModuleStacks[i] = slot.copy();
            RFToolsUtilityMessages.sendToServer(PacketModuleUpdate.create(this.getBE().getBlockPos(), i, slot));
         });
         moduleProvider.createGui(guiBuilder);
         this.modulePanels[i] = guiBuilder.build();
         this.modulePanels[i].hint(80, 8, 170, 114);
         ((Panel)this.modulePanels[i].filledRectThickness(-2)).filledBackground(-7631989);
         this.toplevel.children(new Widget[]{this.modulePanels[i]});
         this.toggleButtons[i].text(moduleProvider.getModuleName());
      }
   }

   public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      this.refreshButtons();
      this.drawWindow(graphics, partialTicks, mouseX, mouseY);
   }
}
