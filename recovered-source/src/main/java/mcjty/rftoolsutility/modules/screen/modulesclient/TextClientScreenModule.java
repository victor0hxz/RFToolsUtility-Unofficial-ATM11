package mcjty.rftoolsutility.modules.screen.modulesclient;

import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleRenderHelper;
import mcjty.rftoolsbase.api.screens.ITextRenderHelper;
import mcjty.rftoolsbase.api.screens.ModuleRenderInfo;
import mcjty.rftoolsbase.api.screens.IClientScreenModule.TransformMode;
import mcjty.rftoolsbase.api.screens.data.IModuleData;
import mcjty.rftoolsbase.tools.ScreenTextHelper;
import mcjty.rftoolsutility.modules.screen.items.modules.TextModuleItem;
import mcjty.rftoolsutility.modules.screen.modules.TextScreenModule;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TextClientScreenModule implements IClientScreenModule<IModuleData> {
   private final ITextRenderHelper cache = new ScreenTextHelper();

   public TransformMode getTransformMode(ItemStack moduleItem) {
      return this.cache.isLarge() ? TransformMode.TEXTLARGE : TransformMode.TEXT;
   }

   public int getHeight(ItemStack moduleItem) {
      return this.cache.isLarge() ? 20 : 10;
   }

   public void render(
      GuiGraphicsExtractor graphics,
      MultiBufferSource buffer,
      IModuleRenderHelper renderHelper,
      Font fontRenderer,
      int currenty,
      IModuleData screenData,
      ModuleRenderInfo renderInfo
   ) {
      TextScreenModule data = TextModuleItem.data(renderInfo.moduleStack);
      this.cache.setup(data.getLine(), 512, renderInfo);
      this.cache.align(data.getAlign());
      this.cache.large(data.isLarge());
      int y = this.cache.isLarge() ? currenty / 2 + 1 : currenty;
      this.cache.renderText(graphics, buffer, 0, y, data.getColor(), renderInfo);
   }

   public void mouseClick(ItemStack moduleStack, Level world, int x, int y, boolean clicked) {
   }

   public boolean needsServerData() {
      return false;
   }
}
