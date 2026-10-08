package mcjty.rftoolsutility.modules.screen.modulesclient;

import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleRenderHelper;
import mcjty.rftoolsbase.api.screens.ModuleRenderInfo;
import mcjty.rftoolsbase.api.screens.IClientScreenModule.TransformMode;
import mcjty.rftoolsutility.modules.screen.modules.InventoryScreenModule;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class InventoryClientScreenModule implements IClientScreenModule<InventoryScreenModule.ModuleDataStacks> {
   public TransformMode getTransformMode(ItemStack moduleItem) {
      return TransformMode.ITEM;
   }

   public int getHeight(ItemStack moduleItem) {
      return 22;
   }

   public void render(
      GuiGraphicsExtractor graphics,
      MultiBufferSource buffer,
      IModuleRenderHelper renderHelper,
      Font fontRenderer,
      int currenty,
      InventoryScreenModule.ModuleDataStacks screenData,
      ModuleRenderInfo renderInfo
   ) {
   }

   public void mouseClick(ItemStack moduleStack, Level world, int x, int y, boolean clicked) {
   }

   public boolean needsServerData() {
      return true;
   }
}
