package mcjty.rftoolsutility.modules.screen.modules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.CompositeStreamCodec;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.api.screens.FormatStyle;
import mcjty.rftoolsbase.api.screens.IScreenDataHelper;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.TextAlign;
import mcjty.rftoolsbase.api.screens.data.IModuleDataInteger;
import mcjty.rftoolsutility.modules.logic.blocks.CounterTileEntity;
import mcjty.rftoolsutility.modules.screen.ScreenConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record CounterScreenModule(GlobalPos pos, boolean active, String line, int color, int cntcolor, FormatStyle format, TextAlign align, String monitor)
   implements IScreenModule<CounterScreenModule, IModuleDataInteger> {
   public static final CounterScreenModule DEFAULT = new CounterScreenModule(
      GlobalPos.of(Level.OVERWORLD, BlockPosTools.INVALID), "", 16777215, 16777215, FormatStyle.MODE_FULL, TextAlign.ALIGN_LEFT, ""
   );
   public static final Codec<CounterScreenModule> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            GlobalPos.CODEC.fieldOf("pos").forGetter(module -> module.pos),
            Codec.STRING.fieldOf("line").forGetter(module -> module.line),
            Codec.INT.fieldOf("color").forGetter(module -> module.color),
            Codec.INT.fieldOf("cntcolor").forGetter(module -> module.cntcolor),
            FormatStyle.CODEC.fieldOf("format").forGetter(module -> module.format),
            TextAlign.CODEC.fieldOf("align").forGetter(module -> module.align),
            Codec.STRING.fieldOf("monitor").forGetter(module -> module.monitor)
         )
         .apply(instance, CounterScreenModule::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, CounterScreenModule> STREAM_CODEC = CompositeStreamCodec.composite(
      GlobalPos.STREAM_CODEC,
      module -> module.pos,
      ByteBufCodecs.STRING_UTF8,
      module -> module.line,
      ByteBufCodecs.INT,
      module -> module.color,
      ByteBufCodecs.INT,
      module -> module.cntcolor,
      FormatStyle.STREAM_CODEC,
      module -> module.format,
      TextAlign.STREAM_CODEC,
      module -> module.align,
      ByteBufCodecs.STRING_UTF8,
      module -> module.monitor,
      CounterScreenModule::new
   );

   public CounterScreenModule(GlobalPos pos, String line, int color, int cntcolor, FormatStyle format, TextAlign align, String monitor) {
      this(pos, false, line, color, cntcolor, format, align, monitor);
   }

   public String getLine() {
      return this.line;
   }

   public int getColor() {
      return this.color;
   }

   public int getCntcolor() {
      return this.cntcolor;
   }

   public TextAlign getAlign() {
      return this.align;
   }

   public FormatStyle getFormat() {
      return this.format;
   }

   public GlobalPos getPos() {
      return this.pos;
   }

   public String getMonitor() {
      return this.monitor;
   }

   public CounterScreenModule withLine(String line) {
      return new CounterScreenModule(this.pos, this.active, line, this.color, this.cntcolor, this.format, this.align, this.monitor);
   }

   public CounterScreenModule withColor(int color) {
      return new CounterScreenModule(this.pos, this.active, this.line, color, this.cntcolor, this.format, this.align, this.monitor);
   }

   public CounterScreenModule withCntcolor(int cntcolor) {
      return new CounterScreenModule(this.pos, this.active, this.line, this.color, cntcolor, this.format, this.align, this.monitor);
   }

   public CounterScreenModule withFormat(FormatStyle format) {
      return new CounterScreenModule(this.pos, this.active, this.line, this.color, this.cntcolor, format, this.align, this.monitor);
   }

   public CounterScreenModule withAlign(TextAlign align) {
      return new CounterScreenModule(this.pos, this.active, this.line, this.color, this.cntcolor, this.format, align, this.monitor);
   }

   public CounterScreenModule withMonitor(String monitor) {
      return new CounterScreenModule(this.pos, this.active, this.line, this.color, this.cntcolor, this.format, this.align, monitor);
   }

   public CounterScreenModule withPos(GlobalPos pos) {
      return new CounterScreenModule(pos, this.active, this.line, this.color, this.cntcolor, this.format, this.align, this.monitor);
   }

   public CounterScreenModule withActive(boolean active) {
      return new CounterScreenModule(this.pos, active, this.line, this.color, this.cntcolor, this.format, this.align, this.monitor);
   }

   public IModuleDataInteger getData(IScreenDataHelper helper, Level worldObj, long millis) {
      if (!this.active) {
         return null;
      } else {
         Level world = LevelTools.getLevel(worldObj, this.pos.dimension());
         if (world == null) {
            return null;
         } else if (!LevelTools.isLoaded(world, this.pos.pos())) {
            return null;
         } else {
            return world.getBlockEntity(this.pos.pos()) instanceof CounterTileEntity counterTileEntity
               ? helper.createInteger(counterTileEntity.getCurrent())
               : null;
         }
      }
   }

   public CounterScreenModule validate(Level world, BlockPos p, boolean isPlus) {
      if (isPlus) {
         return this.withActive(true);
      } else {
         if (LevelTools.isLoaded(world, this.pos.pos()) && Objects.equals(this.pos.dimension(), world.dimension())) {
            int dx = Math.abs(this.pos.pos().getX() - p.getX());
            int dy = Math.abs(this.pos.pos().getY() - p.getY());
            int dz = Math.abs(this.pos.pos().getZ() - p.getZ());
            if (dx <= 64 && dy <= 64 && dz <= 64) {
               return this.withActive(true);
            }
         }

         return this.withActive(false);
      }
   }

   public int getRfPerTick() {
      return (Integer)ScreenConfiguration.COUNTER_RFPERTICK.get();
   }

   public ItemStack mouseClick(ItemStack moduleStack, Level world, int x, int y, boolean clicked, Player player) {
      return ItemStack.EMPTY;
   }
}
