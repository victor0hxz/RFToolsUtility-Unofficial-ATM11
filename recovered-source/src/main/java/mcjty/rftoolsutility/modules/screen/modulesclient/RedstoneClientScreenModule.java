package mcjty.rftoolsutility.modules.screen.modulesclient;

import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleRenderHelper;
import mcjty.rftoolsbase.api.screens.ITextRenderHelper;
import mcjty.rftoolsbase.api.screens.ModuleRenderInfo;
import mcjty.rftoolsbase.api.screens.IClientScreenModule.TransformMode;
import mcjty.rftoolsbase.api.screens.data.IModuleDataInteger;
import mcjty.rftoolsbase.tools.ScreenTextHelper;
import mcjty.rftoolsutility.modules.screen.items.modules.RedstoneModuleItem;
import mcjty.rftoolsutility.modules.screen.modules.RedstoneScreenModule;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RedstoneClientScreenModule implements IClientScreenModule<IModuleDataInteger> {
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
      IModuleDataInteger screenData,
      ModuleRenderInfo renderInfo
   ) {
      RedstoneScreenModule data = RedstoneModuleItem.data(renderInfo.moduleStack);
      int xoffset;
      if (!data.getLine().isEmpty()) {
         this.labelCache.setup(data.getLine(), 160, renderInfo);
         this.labelCache.align(data.getAlign());
         this.labelCache.renderText(graphics, buffer, 0, currenty, data.getColor(), renderInfo);
         xoffset = 47;
      } else {
         xoffset = 7;
      }

      String text;
      int col;
      if (screenData != null) {
         int power = screenData.get();
         boolean rs = power > 0;
         if (data.isAnalog()) {
            text = Integer.toString(power);
         } else {
            text = rs ? data.getYestext() : data.getNotext();
         }

         col = rs ? data.getYescolor() : data.getNocolor();
      } else {
         text = "<invalid>";
         col = 16711680;
      }

      renderHelper.renderText(graphics, buffer, xoffset, currenty, col, renderInfo, text);
   }

   public void mouseClick(ItemStack moduleStack, Level world, int x, int y, boolean clicked) {
   }

   public boolean needsServerData() {
      return true;
   }
}
