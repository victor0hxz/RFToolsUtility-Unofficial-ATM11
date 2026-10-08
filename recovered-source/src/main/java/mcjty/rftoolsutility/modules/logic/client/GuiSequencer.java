package mcjty.rftoolsutility.modules.logic.client;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsutility.modules.logic.LogicBlockModule;
import mcjty.rftoolsutility.modules.logic.blocks.SequencerTileEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class GuiSequencer extends GenericGuiContainer<SequencerTileEntity, GenericContainer> {
   private final List<ImageChoiceLabel> bits = new ArrayList<>();

   public GuiSequencer(GenericContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, ((LogicSlabBlock)LogicBlockModule.SEQUENCER.block().get()).getManualEntry());
   }

   public static void register(RegisterMenuScreensEvent event) {
      event.register(LogicBlockModule.CONTAINER_SEQUENCER.get(), GuiSequencer::new);
   }

   public void init() {
      this.window = new Window(this, this.getBE(), Identifier.fromNamespaceAndPath("rftoolsutility", "gui/sequencer.gui"));
      super.init();
      this.initializeFields();
      this.setupEvents();
   }

   private void initializeFields() {
      for (int row = 0; row < 8; row++) {
         for (int col = 0; col < 8; col++) {
            int bit = row * 8 + col;
            this.bits.add((ImageChoiceLabel)this.window.findChild("grid" + bit));
         }
      }
   }

   private void updateFields() {
      if (this.window != null) {
         SequencerTileEntity tileEntity = (SequencerTileEntity)this.getBE();

         for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
               int bit = row * 8 + col;
               ImageChoiceLabel label = (ImageChoiceLabel)this.window.findChild("grid" + bit);
               label.setCurrentChoice(tileEntity.getCycleBit(bit) ? 1 : 0);
            }
         }
      }
   }

   public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int x, int y, float partialTicks) {
      this.updateFields();
      super.extractBackground(graphics, x, y, partialTicks);
   }

   private void setupEvents() {
      this.window.event("grid", (source, params) -> {
         int bit = Integer.parseInt(source.getName().substring("grid".length()));
         this.changeBit(bit, (String)params.get(ImageChoiceLabel.PARAM_CHOICE));
      });
      this.window.event("flip", (source, params) -> this.flipGrid());
      this.window.event("clear", (source, params) -> this.fillGrid());
   }

   private void flipGrid() {
      for (ImageChoiceLabel bit : this.bits) {
         bit.setCurrentChoice(1 - bit.getCurrentChoiceIndex());
      }

      SequencerTileEntity tileEntity = (SequencerTileEntity)this.getBE();
      tileEntity.flipCycleBits();
      this.sendServerCommandTyped(SequencerTileEntity.CMD_FLIPBITS, TypedMap.EMPTY);
   }

   private void fillGrid() {
      for (ImageChoiceLabel bit : this.bits) {
         bit.setCurrentChoice(0);
      }

      SequencerTileEntity tileEntity = (SequencerTileEntity)this.getBE();
      tileEntity.clearCycleBits();
      this.sendServerCommandTyped(SequencerTileEntity.CMD_CLEARBITS, TypedMap.EMPTY);
   }

   private void changeBit(int bit, String choice) {
      boolean newChoice = "1".equals(choice);
      SequencerTileEntity tileEntity = (SequencerTileEntity)this.getBE();
      tileEntity.setCycleBit(bit, newChoice);
      this.sendServerCommandTyped(
         SequencerTileEntity.CMD_SETBIT, TypedMap.builder().put(SequencerTileEntity.PARAM_BIT, bit).put(SequencerTileEntity.PARAM_CHOICE, newChoice).build()
      );
   }
}
