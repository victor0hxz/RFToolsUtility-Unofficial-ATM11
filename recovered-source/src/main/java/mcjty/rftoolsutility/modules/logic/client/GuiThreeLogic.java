package mcjty.rftoolsutility.modules.logic.client;

import javax.annotation.Nonnull;
import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsutility.modules.logic.LogicBlockModule;
import mcjty.rftoolsutility.modules.logic.blocks.ThreeLogicTileEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class GuiThreeLogic extends GenericGuiContainer<ThreeLogicTileEntity, GenericContainer> {
   public GuiThreeLogic(GenericContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, ((LogicSlabBlock)LogicBlockModule.LOGIC.block().get()).getManualEntry());
   }

   public static void register(RegisterMenuScreensEvent event) {
      event.register(LogicBlockModule.CONTAINER_LOGIC.get(), GuiThreeLogic::new);
   }

   public void init() {
      this.window = new Window(this, this.getBE(), Identifier.fromNamespaceAndPath("rftoolsutility", "gui/threelogic.gui"));
      super.init();
      this.initializeFields();
      this.setupEvents();
   }

   private void setupEvents() {
      this.window
         .event(
            "choice",
            (source, params) -> {
               String name = source.getName();
               int i = Integer.parseInt(name.substring(name.length() - 1));
               String current = (String)params.get(ChoiceLabel.PARAM_CHOICE);
               int st;
               if ("On".equals(current)) {
                  st = 1;
               } else {
                  st = "Off".equals(current) ? 0 : -1;
               }

               this.sendServerCommandTyped(
                  ThreeLogicTileEntity.CMD_SETSTATE,
                  TypedMap.builder().put(ThreeLogicTileEntity.PARAM_INDEX, i).put(ThreeLogicTileEntity.PARAM_STATE, st).build()
               );
            }
         );
   }

   private void initializeFields() {
   }

   private void updateFields() {
      if (this.window != null) {
         ThreeLogicTileEntity tileEntity = (ThreeLogicTileEntity)this.getBE();

         for (int i = 0; i < 8; i++) {
            ChoiceLabel tl = (ChoiceLabel)this.window.findChild("choice" + i);
            int state = tileEntity.getState(i);
            switch (state) {
               case 0:
                  tl.choice("Off");
                  break;
               case 1:
                  tl.choice("On");
                  break;
               default:
                  tl.choice("Keep");
            }
         }
      }
   }

   public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int x, int y, float partialTicks) {
      this.updateFields();
      super.extractBackground(graphics, x, y, partialTicks);
   }
}
