package mcjty.rftoolsutility.modules.logic.client;

import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.rftoolsutility.modules.logic.LogicBlockModule;
import mcjty.rftoolsutility.modules.logic.blocks.CounterTileEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class GuiCounter extends GenericGuiContainer<CounterTileEntity, GenericContainer> {
   public GuiCounter(GenericContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, ((LogicSlabBlock)LogicBlockModule.COUNTER.block().get()).getManualEntry());
   }

   public static void register(RegisterMenuScreensEvent event) {
      event.register(LogicBlockModule.CONTAINER_COUNTER.get(), GuiCounter::new);
   }

   public void init() {
      this.window = new Window(this, this.getBE(), Identifier.fromNamespaceAndPath("rftoolsutility", "gui/counter.gui"));
      super.init();
   }
}
