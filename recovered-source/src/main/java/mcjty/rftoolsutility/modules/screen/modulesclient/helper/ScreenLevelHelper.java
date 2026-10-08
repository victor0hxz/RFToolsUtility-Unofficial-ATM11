package mcjty.rftoolsutility.modules.screen.modulesclient.helper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.lib.client.RenderHelper;
import mcjty.lib.varia.CompositeStreamCodec;
import mcjty.rftoolsbase.api.screens.BarMode;
import mcjty.rftoolsbase.api.screens.FormatStyle;
import mcjty.rftoolsbase.api.screens.ILevelRenderHelper;
import mcjty.rftoolsbase.api.screens.ModuleRenderInfo;
import mcjty.rftoolsbase.api.screens.data.IModuleDataContents;
import mcjty.rftoolsbase.tools.ScreenTextHelper;
import mcjty.rftoolsutility.modules.screen.ScreenConfiguration;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class ScreenLevelHelper implements ILevelRenderHelper {
   private boolean hidebar = false;
   private FormatStyle formatStyle = FormatStyle.MODE_FULL;
   private BarMode barMode = BarMode.MODE_TEXT;
   private int poscolor = 16777215;
   private int negcolor = 16777215;
   private int gradient1 = -65536;
   private int gradient2 = -13421824;
   private String label = "";
   public static final Codec<ScreenLevelHelper> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.BOOL.fieldOf("hidebar").forGetter(module -> module.hidebar),
            FormatStyle.CODEC.fieldOf("formatStyle").forGetter(module -> module.formatStyle),
            BarMode.CODEC.fieldOf("barMode").forGetter(module -> module.barMode),
            Codec.INT.fieldOf("poscolor").forGetter(module -> module.poscolor),
            Codec.INT.fieldOf("negcolor").forGetter(module -> module.negcolor),
            Codec.INT.fieldOf("gradient1").forGetter(module -> module.gradient1),
            Codec.INT.fieldOf("gradient2").forGetter(module -> module.gradient2),
            Codec.STRING.fieldOf("label").forGetter(module -> module.label)
         )
         .apply(instance, ScreenLevelHelper::new)
   );
   public static final StreamCodec<FriendlyByteBuf, ScreenLevelHelper> STREAM_CODEC = CompositeStreamCodec.composite(
      ByteBufCodecs.BOOL,
      module -> module.hidebar,
      FormatStyle.STREAM_CODEC,
      module -> module.formatStyle,
      BarMode.STREAM_CODEC,
      module -> module.barMode,
      ByteBufCodecs.INT,
      module -> module.poscolor,
      ByteBufCodecs.INT,
      module -> module.negcolor,
      ByteBufCodecs.INT,
      module -> module.gradient1,
      ByteBufCodecs.INT,
      module -> module.gradient2,
      ByteBufCodecs.STRING_UTF8,
      module -> module.label,
      ScreenLevelHelper::new
   );
   private static DecimalFormat dfCommas = new DecimalFormat("###,###");

   public ScreenLevelHelper(boolean hidebar, FormatStyle formatStyle, BarMode barMode, int poscolor, int negcolor, int gradient1, int gradient2, String label) {
      this.hidebar = hidebar;
      this.formatStyle = formatStyle;
      this.barMode = barMode;
      this.poscolor = poscolor;
      this.negcolor = negcolor;
      this.gradient1 = gradient1;
      this.gradient2 = gradient2;
      this.label = label;
   }

   public ScreenLevelHelper() {
   }

   public void render(
      GuiGraphicsExtractor graphics, MultiBufferSource buffer, int x, int y, @Nullable IModuleDataContents data, @Nonnull ModuleRenderInfo renderInfo
   ) {
      if (data != null) {
         long maxContents = data.getMaxContents();
         if (maxContents > 0L && !this.hidebar) {
            long contents = data.getContents();
            int width = 80 - x + 7 + 40;
            long value = contents * width / maxContents;
            if (value < 0L) {
               value = 0L;
            } else if (value > width) {
               value = width;
            }

            RenderHelper.drawHorizontalGradientRect(
               graphics, buffer, x, y, (int)(x + value), y + 8, this.gradient1, this.gradient2, renderInfo.getLightmapValue()
            );
         }

         if (!this.barMode.hideText()) {
            String diffTxt = null;
            int col = this.poscolor;
            if (this.barMode.showPerTick()) {
               long diff = data.getLastPerTick();
               if (diff < 0L) {
                  col = this.negcolor;
                  diffTxt = diff + " " + this.label + "/t";
               } else {
                  diffTxt = "+" + diff + " " + this.label + "/t";
               }
            } else if (maxContents > 0L) {
               long contents = data.getContents();
               if (this.barMode.showPercentage()) {
                  long value = contents * 100L / maxContents;
                  if (value < 0L) {
                     value = 0L;
                  } else if (value > 100L) {
                     value = 100L;
                  }

                  diffTxt = value + "%";
               } else {
                  diffTxt = this.format(String.valueOf(contents), this.formatStyle) + this.label;
               }
            }

            if (diffTxt != null) {
               ScreenTextHelper.renderScaled(
                  ScreenConfiguration.getTrueTypeFont(), graphics, buffer, diffTxt, x, y, col, renderInfo.truetype, renderInfo.getLightmapValue()
               );
            }
         }
      }
   }

   public ILevelRenderHelper label(String label) {
      this.label = label;
      return this;
   }

   public ILevelRenderHelper settings(boolean hidebar, BarMode barMode) {
      this.hidebar = hidebar;
      this.barMode = barMode;
      return this;
   }

   public ILevelRenderHelper color(int poscolor, int negcolor) {
      this.poscolor = poscolor;
      this.negcolor = negcolor;
      return this;
   }

   public ILevelRenderHelper gradient(int gradient1, int gradient2) {
      this.gradient1 = gradient1;
      this.gradient2 = gradient2;
      return this;
   }

   public ILevelRenderHelper format(FormatStyle formatStyle) {
      this.formatStyle = formatStyle;
      return this;
   }

   public int getPosColor() {
      return this.poscolor;
   }

   public int getNegColor() {
      return this.negcolor;
   }

   public int getGradient1() {
      return this.gradient1;
   }

   public int getGradient2() {
      return this.gradient2;
   }

   public FormatStyle getFormatStyle() {
      return this.formatStyle;
   }

   public boolean isHideBar() {
      return this.hidebar;
   }

   public BarMode getBarMode() {
      return this.barMode;
   }

   public String getLabel() {
      return this.label;
   }

   public void setPosColor(int poscolor) {
      this.poscolor = poscolor;
   }

   public void setNegColor(int negcolor) {
      this.negcolor = negcolor;
   }

   public void setGradient1(int gradient1) {
      this.gradient1 = gradient1;
   }

   public void setGradient2(int gradient2) {
      this.gradient2 = gradient2;
   }

   public void setFormatStyle(FormatStyle formatStyle) {
      this.formatStyle = formatStyle;
   }

   public void setHideBar(boolean hidebar) {
      this.hidebar = hidebar;
   }

   public void setBarMode(BarMode barMode) {
      this.barMode = barMode;
   }

   public void setLabel(String label) {
      this.label = label;
   }

   private String format(String in, FormatStyle style) {
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
