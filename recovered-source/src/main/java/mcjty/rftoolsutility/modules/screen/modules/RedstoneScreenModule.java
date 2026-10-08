package mcjty.rftoolsutility.modules.screen.modules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.CompositeStreamCodec;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.api.screens.IScreenDataHelper;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.TextAlign;
import mcjty.rftoolsbase.api.screens.data.IModuleDataInteger;
import mcjty.rftoolsutility.modules.logic.tools.RedstoneChannels;
import mcjty.rftoolsutility.modules.screen.ScreenConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record RedstoneScreenModule(
   int channel,
   GlobalPos pos,
   Direction side,
   boolean active,
   String line,
   String yestext,
   String notext,
   int color,
   int yescolor,
   int nocolor,
   boolean analog,
   TextAlign align,
   String monitor
) implements IScreenModule<RedstoneScreenModule, IModuleDataInteger> {
   public static final RedstoneScreenModule DEFAULT = new RedstoneScreenModule(
      -1, GlobalPos.of(Level.OVERWORLD, BlockPosTools.INVALID), null, false, "", "on", "off", 16777215, 16777215, 16777215, false, TextAlign.ALIGN_LEFT, ""
   );
   public static final Codec<RedstoneScreenModule> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.INT.fieldOf("channel").forGetter(module -> module.channel),
            GlobalPos.CODEC.fieldOf("pos").forGetter(module -> module.pos),
            Direction.CODEC.optionalFieldOf("side").forGetter(module -> Optional.ofNullable(module.side)),
            Codec.STRING.fieldOf("line").forGetter(module -> module.line),
            Codec.STRING.fieldOf("yestext").forGetter(module -> module.yestext),
            Codec.STRING.fieldOf("notext").forGetter(module -> module.notext),
            Codec.INT.fieldOf("color").forGetter(module -> module.color),
            Codec.INT.fieldOf("yescolor").forGetter(module -> module.yescolor),
            Codec.INT.fieldOf("nocolor").forGetter(module -> module.nocolor),
            Codec.BOOL.fieldOf("analog").forGetter(module -> module.analog),
            TextAlign.CODEC.fieldOf("align").forGetter(module -> module.align),
            Codec.STRING.fieldOf("monitor").forGetter(module -> module.monitor)
         )
         .apply(instance, RedstoneScreenModule::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, RedstoneScreenModule> STREAM_CODEC = CompositeStreamCodec.composite(
      ByteBufCodecs.INT,
      module -> module.channel,
      GlobalPos.STREAM_CODEC,
      module -> module.pos,
      ByteBufCodecs.optional(Direction.STREAM_CODEC),
      module -> Optional.ofNullable(module.side),
      ByteBufCodecs.STRING_UTF8,
      module -> module.line,
      ByteBufCodecs.STRING_UTF8,
      module -> module.yestext,
      ByteBufCodecs.STRING_UTF8,
      module -> module.notext,
      ByteBufCodecs.INT,
      module -> module.color,
      ByteBufCodecs.INT,
      module -> module.yescolor,
      ByteBufCodecs.INT,
      module -> module.nocolor,
      ByteBufCodecs.BOOL,
      module -> module.analog,
      TextAlign.STREAM_CODEC,
      module -> module.align,
      ByteBufCodecs.STRING_UTF8,
      module -> module.monitor,
      RedstoneScreenModule::new
   );

   public RedstoneScreenModule(
      int channel,
      GlobalPos pos,
      Direction side,
      String line,
      String yestext,
      String notext,
      int color,
      int yescolor,
      int nocolor,
      boolean analog,
      TextAlign align,
      String monitor
   ) {
      this(channel, pos, side, false, line, yestext, notext, color, yescolor, nocolor, analog, align, monitor);
   }

   public RedstoneScreenModule(
      int channel,
      GlobalPos pos,
      Optional<Direction> side,
      String line,
      String yestext,
      String notext,
      int color,
      int yescolor,
      int nocolor,
      boolean analog,
      TextAlign align,
      String monitor
   ) {
      this(channel, pos, side.orElse(null), false, line, yestext, notext, color, yescolor, nocolor, analog, align, monitor);
   }

   public RedstoneScreenModule(int channel, GlobalPos pos, Direction side) {
      this(channel, pos, side, false, "", "on", "off", 16777215, 16777215, 16777215, false, TextAlign.ALIGN_LEFT, "");
   }

   public int getChannel() {
      return this.channel;
   }

   public Direction getSide() {
      return this.side;
   }

   public GlobalPos getPos() {
      return this.pos;
   }

   public String getMonitor() {
      return this.monitor;
   }

   public String getLine() {
      return this.line;
   }

   public String getYestext() {
      return this.yestext;
   }

   public String getNotext() {
      return this.notext;
   }

   public int getColor() {
      return this.color;
   }

   public int getYescolor() {
      return this.yescolor;
   }

   public int getNocolor() {
      return this.nocolor;
   }

   public boolean isAnalog() {
      return this.analog;
   }

   public TextAlign getAlign() {
      return this.align;
   }

   public RedstoneScreenModule withChannel(int channel) {
      return new RedstoneScreenModule(
         channel,
         this.pos,
         this.side,
         this.active,
         this.line,
         this.yestext,
         this.notext,
         this.color,
         this.yescolor,
         this.nocolor,
         this.analog,
         this.align,
         this.monitor
      );
   }

   public RedstoneScreenModule withPos(GlobalPos pos) {
      return new RedstoneScreenModule(
         this.channel,
         pos,
         this.side,
         this.active,
         this.line,
         this.yestext,
         this.notext,
         this.color,
         this.yescolor,
         this.nocolor,
         this.analog,
         this.align,
         this.monitor
      );
   }

   public RedstoneScreenModule withSide(Direction side) {
      return new RedstoneScreenModule(
         this.channel,
         this.pos,
         side,
         this.active,
         this.line,
         this.yestext,
         this.notext,
         this.color,
         this.yescolor,
         this.nocolor,
         this.analog,
         this.align,
         this.monitor
      );
   }

   public RedstoneScreenModule withActive(boolean active) {
      return new RedstoneScreenModule(
         this.channel,
         this.pos,
         this.side,
         active,
         this.line,
         this.yestext,
         this.notext,
         this.color,
         this.yescolor,
         this.nocolor,
         this.analog,
         this.align,
         this.monitor
      );
   }

   public RedstoneScreenModule withLine(String line) {
      return new RedstoneScreenModule(
         this.channel,
         this.pos,
         this.side,
         this.active,
         line,
         this.yestext,
         this.notext,
         this.color,
         this.yescolor,
         this.nocolor,
         this.analog,
         this.align,
         this.monitor
      );
   }

   public RedstoneScreenModule withYestext(String yestext) {
      return new RedstoneScreenModule(
         this.channel,
         this.pos,
         this.side,
         this.active,
         this.line,
         yestext,
         this.notext,
         this.color,
         this.yescolor,
         this.nocolor,
         this.analog,
         this.align,
         this.monitor
      );
   }

   public RedstoneScreenModule withNotext(String notext) {
      return new RedstoneScreenModule(
         this.channel,
         this.pos,
         this.side,
         this.active,
         this.line,
         this.yestext,
         notext,
         this.color,
         this.yescolor,
         this.nocolor,
         this.analog,
         this.align,
         this.monitor
      );
   }

   public RedstoneScreenModule withColor(int color) {
      return new RedstoneScreenModule(
         this.channel,
         this.pos,
         this.side,
         this.active,
         this.line,
         this.yestext,
         this.notext,
         color,
         this.yescolor,
         this.nocolor,
         this.analog,
         this.align,
         this.monitor
      );
   }

   public RedstoneScreenModule withYescolor(int yescolor) {
      return new RedstoneScreenModule(
         this.channel,
         this.pos,
         this.side,
         this.active,
         this.line,
         this.yestext,
         this.notext,
         this.color,
         yescolor,
         this.nocolor,
         this.analog,
         this.align,
         this.monitor
      );
   }

   public RedstoneScreenModule withNocolor(int nocolor) {
      return new RedstoneScreenModule(
         this.channel,
         this.pos,
         this.side,
         this.active,
         this.line,
         this.yestext,
         this.notext,
         this.color,
         this.yescolor,
         nocolor,
         this.analog,
         this.align,
         this.monitor
      );
   }

   public RedstoneScreenModule withAnalog(boolean analog) {
      return new RedstoneScreenModule(
         this.channel,
         this.pos,
         this.side,
         this.active,
         this.line,
         this.yestext,
         this.notext,
         this.color,
         this.yescolor,
         this.nocolor,
         analog,
         this.align,
         this.monitor
      );
   }

   public RedstoneScreenModule withAlign(TextAlign align) {
      return new RedstoneScreenModule(
         this.channel,
         this.pos,
         this.side,
         this.active,
         this.line,
         this.yestext,
         this.notext,
         this.color,
         this.yescolor,
         this.nocolor,
         this.analog,
         align,
         this.monitor
      );
   }

   public RedstoneScreenModule withMonitor(String monitor) {
      return new RedstoneScreenModule(
         this.channel,
         this.pos,
         this.side,
         this.active,
         this.line,
         this.yestext,
         this.notext,
         this.color,
         this.yescolor,
         this.nocolor,
         this.analog,
         this.align,
         monitor
      );
   }

   public IModuleDataInteger getData(IScreenDataHelper helper, Level worldObj, long millis) {
      if (!this.active) {
         return null;
      } else if (this.channel == -1) {
         if (BlockPosTools.isValid(this.pos.pos())) {
            Level world = LevelTools.getLevel(worldObj, this.pos.dimension());
            if (world != null) {
               int powerTo = world.getSignal(this.pos.pos().relative(this.side), this.side.getOpposite());
               return helper.createInteger(powerTo);
            }
         }

         return null;
      } else {
         RedstoneChannels channels = RedstoneChannels.getChannels(worldObj);
         if (channels == null) {
            return null;
         } else {
            RedstoneChannels.RedstoneChannel ch = channels.getOrCreateChannel(this.channel);
            return ch == null ? null : helper.createInteger(ch.getValue());
         }
      }
   }

   public RedstoneScreenModule validate(Level world, BlockPos p, boolean isPlus) {
      if (isPlus) {
         return this.withActive(true);
      } else if (this.channel != -1) {
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
      return (Integer)ScreenConfiguration.REDSTONE_RFPERTICK.get();
   }

   public ItemStack mouseClick(ItemStack moduleStack, Level world, int x, int y, boolean clicked, Player player) {
      return ItemStack.EMPTY;
   }
}
