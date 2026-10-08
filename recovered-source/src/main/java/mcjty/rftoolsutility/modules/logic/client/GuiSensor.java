package mcjty.rftoolsutility.modules.logic.client;

import javax.annotation.Nonnull;
import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.varia.NamedEnum;
import mcjty.rftoolsutility.modules.logic.LogicBlockModule;
import mcjty.rftoolsutility.modules.logic.blocks.SensorTileEntity;
import mcjty.rftoolsutility.modules.logic.tools.SensorType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class GuiSensor extends GenericGuiContainer<SensorTileEntity, GenericContainer> {
   private ChoiceLabel typeLabel;

   public GuiSensor(GenericContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, ((LogicSlabBlock)LogicBlockModule.SENSOR.block().get()).getManualEntry());
   }

   public static void register(RegisterMenuScreensEvent event) {
      event.register(LogicBlockModule.CONTAINER_SENSOR.get(), GuiSensor::new);
   }

   public void init() {
      this.window = new Window(this, this.getBE(), Identifier.fromNamespaceAndPath("rftoolsutility", "gui/sensor.gui"));
      super.init();
      this.initializeFields();
   }

   private void initializeFields() {
      this.typeLabel = (ChoiceLabel)this.window.findChild("type");
   }

   private void updateFields() {
      if (this.window != null) {
         SensorType sensorType = (SensorType)NamedEnum.getEnumByName(this.typeLabel.getCurrentChoice(), SensorType.values());
         if (sensorType != null) {
            this.window.setFlag("number", sensorType.isSupportsNumber());
            this.window.setFlag("group", sensorType.isSupportsGroup());
         }
      }
   }

   public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      this.updateFields();
      this.drawWindow(graphics, partialTicks, mouseX, mouseY);
   }
}
