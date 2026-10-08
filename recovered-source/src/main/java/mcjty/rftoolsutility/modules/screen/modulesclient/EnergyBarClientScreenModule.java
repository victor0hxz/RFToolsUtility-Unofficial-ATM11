package mcjty.rftoolsutility.modules.screen.modulesclient;

import mcjty.lib.varia.BlockPosTools;
import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleRenderHelper;
import mcjty.rftoolsbase.api.screens.ITextRenderHelper;
import mcjty.rftoolsbase.api.screens.ModuleRenderInfo;
import mcjty.rftoolsbase.api.screens.IClientScreenModule.TransformMode;
import mcjty.rftoolsbase.api.screens.data.IModuleDataContents;
import mcjty.rftoolsbase.tools.ScreenTextHelper;
import mcjty.rftoolsutility.modules.screen.items.modules.EnergyModuleItem;
import mcjty.rftoolsutility.modules.screen.modules.EnergyBarScreenModule;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EnergyBarClientScreenModule implements IClientScreenModule<IModuleDataContents> {
   private final ITextRenderHelper labelCache = new ScreenTextHelper();

   public TransformMode getTransformMode(ItemStack moduleItem) {
      return TransformMode.TEXT;
   }

   public int getHeight(ItemStack moduleItem) {
      return 10;
   }

   public void render(
      GuiGraphicsExtractor graphics,
      MultiBufferSource buffer,
      IModuleRenderHelper renderHelper,
      Font fontRenderer,
      int currenty,
      IModuleDataContents screenData,
      ModuleRenderInfo renderInfo
   ) {
      EnergyBarScreenModule data = EnergyModuleItem.data(renderInfo.moduleStack);
      int xoffset;
      if (!data.getLine().isEmpty()) {
         this.labelCache.setup(data.getLine(), 160, renderInfo);
         this.labelCache.align(data.getAlign());
         this.labelCache.renderText(graphics, buffer, 0, currenty, data.getColor(), renderInfo);
         xoffset = 47;
      } else {
         xoffset = 7;
      }

      if (BlockPosTools.isValid(data.getPos().pos())) {
         data.getRfRenderer().render(graphics, buffer, xoffset, currenty, screenData, renderInfo);
      } else {
         renderHelper.renderText(graphics, buffer, xoffset, currenty, 16711680, renderInfo, "<invalid>");
      }
   }

   public void mouseClick(ItemStack moduleStack, Level world, int x, int y, boolean clicked) {
   }

   public boolean needsServerData() {
      return true;
   }
}
