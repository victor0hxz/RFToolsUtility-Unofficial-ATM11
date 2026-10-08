package mcjty.rftoolsutility.modules.logic.client;

import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.rftoolsutility.modules.logic.LogicBlockModule;
import mcjty.rftoolsutility.modules.logic.blocks.RedstoneTransmitterTileEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class GuiRedstoneTransmitter extends GenericGuiContainer<RedstoneTransmitterTileEntity, GenericContainer> {
   public GuiRedstoneTransmitter(GenericContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, ((LogicSlabBlock)LogicBlockModule.REDSTONE_TRANSMITTER.block().get()).getManualEntry());
   }

   public static void register(RegisterMenuScreensEvent event) {
      event.register(LogicBlockModule.CONTAINER_REDSTONE_TRANSMITTER.get(), GuiRedstoneTransmitter::new);
   }

   public void init() {
      this.window = new Window(this, this.getBE(), Identifier.fromNamespaceAndPath("rftoolsutility", "gui/redstone_transmitter.gui"));
      super.init();
      this.window.bind("name", this.getBE(), RedstoneTransmitterTileEntity.VALUE_NAME.key().name());
   }
}
