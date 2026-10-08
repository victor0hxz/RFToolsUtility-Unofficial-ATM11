package mcjty.rftoolsutility.modules.screen.modulesclient;

import java.util.Locale;
import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleRenderHelper;
import mcjty.rftoolsbase.api.screens.ModuleRenderInfo;
import mcjty.rftoolsbase.api.screens.IClientScreenModule.TransformMode;
import mcjty.rftoolsbase.api.screens.data.IModuleData;
import mcjty.rftoolsutility.modules.screen.items.modules.ClockModuleItem;
import mcjty.rftoolsutility.modules.screen.modules.ClockScreenModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ClockClientScreenModule implements IClientScreenModule<IModuleData> {
   public TransformMode getTransformMode(ItemStack moduleItem) {
      boolean large = ClockModuleItem.data(moduleItem).isLarge();
      return large ? TransformMode.TEXTLARGE : TransformMode.TEXT;
   }

   public int getHeight(ItemStack moduleItem) {
      boolean large = ClockModuleItem.data(moduleItem).isLarge();
      return large ? 20 : 10;
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
      ClockScreenModule data = ClockModuleItem.data(renderInfo.moduleStack);
      Minecraft minecraft = Minecraft.getInstance();
      long time = minecraft.level.getGameTime();
      long hour = (time / 1000L + 6L) % 24L;
      long minute = time % 1000L * 60L / 1000L;
      String timeString = String.format(Locale.ENGLISH, "%02d:%02d", hour, minute);
      int xoffset;
      int y;
      if (data.isLarge()) {
         xoffset = 4;
         y = currenty / 2 + 1;
      } else {
         xoffset = 7;
         y = currenty;
      }

      renderHelper.renderText(graphics, buffer, xoffset, y, data.getColor(), renderInfo, data.getLine() + " " + timeString);
   }

   public void mouseClick(ItemStack moduleStack, Level world, int x, int y, boolean clicked) {
   }

   public boolean needsServerData() {
      return false;
   }
}
