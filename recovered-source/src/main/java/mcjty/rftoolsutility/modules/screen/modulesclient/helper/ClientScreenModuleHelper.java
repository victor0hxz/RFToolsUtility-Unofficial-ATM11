package mcjty.rftoolsutility.modules.screen.modulesclient.helper;

import com.mojang.blaze3d.vertex.PoseStack;
import java.text.DecimalFormat;
import javax.annotation.Nonnull;
import mcjty.lib.client.RenderHelper;
import mcjty.rftoolsbase.api.screens.FormatStyle;
import mcjty.rftoolsbase.api.screens.ILevelRenderHelper;
import mcjty.rftoolsbase.api.screens.IModuleRenderHelper;
import mcjty.rftoolsbase.api.screens.ITextRenderHelper;
import mcjty.rftoolsbase.api.screens.ModuleRenderInfo;
import mcjty.rftoolsbase.api.screens.data.IModuleDataContents;
import mcjty.rftoolsbase.tools.ScreenTextHelper;
import mcjty.rftoolsutility.modules.screen.ScreenConfiguration;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MultiBufferSource;

public class ClientScreenModuleHelper implements IModuleRenderHelper {
   private static DecimalFormat dfCommas = new DecimalFormat("###,###");

   public void renderLevel(
      GuiGraphicsExtractor graphics,
      MultiBufferSource buffer,
      Font fontRenderer,
      int xoffset,
      int currenty,
      IModuleDataContents screenData,
      String label,
      boolean hidebar,
      boolean hidetext,
      boolean showpct,
      boolean showdiff,
      int poscolor,
      int negcolor,
      int gradient1,
      int gradient2,
      FormatStyle formatStyle
   ) {
      this.renderLevel(
         graphics,
         buffer,
         fontRenderer,
         xoffset,
         currenty,
         screenData,
         label,
         hidebar,
         hidetext,
         showpct,
         showdiff,
         poscolor,
         negcolor,
         gradient1,
         gradient2,
         formatStyle,
         null
      );
   }

   private void renderLevel(
      GuiGraphicsExtractor graphics,
      MultiBufferSource buffer,
      Font fontRenderer,
      int xoffset,
      int currenty,
      IModuleDataContents screenData,
      String label,
      boolean hidebar,
      boolean hidetext,
      boolean showpct,
      boolean showdiff,
      int poscolor,
      int negcolor,
      int gradient1,
      int gradient2,
      FormatStyle formatStyle,
      ModuleRenderInfo renderInfo
   ) {
      if (screenData != null) {
         long maxContents = screenData.getMaxContents();
         if (maxContents > 0L && !hidebar) {
            long contents = screenData.getContents();
            int width = 80 - xoffset + 7 + 40;
            long value = contents * width / maxContents;
            if (value < 0L) {
               value = 0L;
            } else if (value > width) {
               value = width;
            }

            RenderHelper.drawHorizontalGradientRect(
               graphics, buffer, xoffset, currenty, (int)(xoffset + value), currenty + 8, gradient1, gradient2, renderInfo.getLightmapValue()
            );
         }

         if (!hidetext) {
            String diffTxt = null;
            int col = poscolor;
            if (showdiff) {
               long diff = screenData.getLastPerTick();
               if (diff < 0L) {
                  col = negcolor;
                  diffTxt = diff + " " + label + "/t";
               } else {
                  diffTxt = "+" + diff + " " + label + "/t";
               }
            } else if (maxContents > 0L) {
               long contents = screenData.getContents();
               if (showpct) {
                  long value = contents * 100L / maxContents;
                  if (value < 0L) {
                     value = 0L;
                  } else if (value > 100L) {
                     value = 100L;
                  }

                  diffTxt = value + "%";
               } else {
                  diffTxt = this.format(String.valueOf(contents), formatStyle) + label;
               }
            }

            if (diffTxt != null) {
               ScreenTextHelper.renderScaled(
                  ScreenConfiguration.getTrueTypeFont(),
                  graphics,
                  buffer,
                  diffTxt,
                  xoffset,
                  currenty,
                  col,
                  (Boolean)ScreenConfiguration.useTruetype.get(),
                  renderInfo.getLightmapValue()
               );
            }
         }
      }
   }

   public ITextRenderHelper createTextRenderHelper() {
      return new ScreenTextHelper();
   }

   public ILevelRenderHelper createLevelRenderHelper() {
      return new ScreenLevelHelper();
   }

   public void renderText(GuiGraphicsExtractor graphics, MultiBufferSource buffer, int x, int y, int color, @Nonnull ModuleRenderInfo renderInfo, String text) {
      if (text != null) {
         ScreenTextHelper.renderScaled(
            ScreenConfiguration.getTrueTypeFont(), graphics, buffer, text, x, y, color, renderInfo.truetype, renderInfo.getLightmapValue()
         );
      }
   }

   public void renderTextTrimmed(
      PoseStack matrixStack, MultiBufferSource buffer, int x, int y, int color, @Nonnull ModuleRenderInfo renderInfo, String text, int maxwidth
   ) {
      if (text != null) {
         ScreenTextHelper.renderScaledTrimmed(
            ScreenConfiguration.getTrueTypeFont(), matrixStack, buffer, text, x, y, maxwidth / 4, color, renderInfo.truetype, renderInfo.getLightmapValue()
         );
      }
   }

   public String format(String in, FormatStyle style) {
      switch (style) {
         case MODE_FULL:
            return in;
         case MODE_COMPACT:
            long contents = Long.parseLong(in);
            int unit = 1000;
            if (contents < unit) {
               return in;
            }

            int exp = (int)(Math.log(contents) / Math.log(unit));
            char pre = "kMGTPE".charAt(exp - 1);
            return String.format("%.1f %s", contents / Math.pow(unit, exp), pre);
         case MODE_COMMAS:
            return dfCommas.format(Long.parseLong(in));
         default:
            return in;
      }
   }
}
