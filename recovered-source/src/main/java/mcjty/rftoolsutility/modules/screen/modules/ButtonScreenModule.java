package mcjty.rftoolsutility.modules.screen.modules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.varia.ComponentFactory;
import mcjty.lib.varia.CompositeStreamCodec;
import mcjty.rftoolsbase.api.screens.IScreenDataHelper;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.TextAlign;
import mcjty.rftoolsbase.api.screens.data.IModuleDataBoolean;
import mcjty.rftoolsutility.modules.logic.tools.RedstoneChannels;
import mcjty.rftoolsutility.modules.screen.ScreenConfiguration;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record ButtonScreenModule(String line, int channel, boolean toggle, String button, int color, int buttonColor, TextAlign align)
   implements IScreenModule<ButtonScreenModule, IModuleDataBoolean> {
   public static final ButtonScreenModule DEFAULT = new ButtonScreenModule("", -1, false, "", 16777215, 16777215, TextAlign.ALIGN_LEFT);
   public static final Codec<ButtonScreenModule> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.STRING.fieldOf("line").forGetter(module -> module.line),
            Codec.INT.fieldOf("channel").forGetter(module -> module.channel),
            Codec.BOOL.fieldOf("toggle").forGetter(module -> module.toggle),
            Codec.STRING.fieldOf("button").forGetter(module -> module.button),
            Codec.INT.fieldOf("color").forGetter(module -> module.color),
            Codec.INT.fieldOf("buttonColor").forGetter(module -> module.buttonColor),
            Codec.STRING.fieldOf("align").forGetter(module -> module.align.name())
         )
         .apply(instance, ButtonScreenModule::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, ButtonScreenModule> STREAM_CODEC = CompositeStreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      module -> module.line,
      ByteBufCodecs.INT,
      module -> module.channel,
      ByteBufCodecs.BOOL,
      module -> module.toggle,
      ByteBufCodecs.STRING_UTF8,
      module -> module.button,
      ByteBufCodecs.INT,
      module -> module.color,
      ByteBufCodecs.INT,
      module -> module.buttonColor,
      ByteBufCodecs.STRING_UTF8,
      module -> module.align.name(),
      ButtonScreenModule::new
   );

   public ButtonScreenModule(String line, int channel, boolean toggle, String button, int color, int buttonColor, String align) {
      this(line, channel, toggle, button, color, buttonColor, TextAlign.get(align));
   }

   public int getChannel() {
      return this.channel;
   }

   public String getLine() {
      return this.line;
   }

   public boolean isToggle() {
      return this.toggle;
   }

   public String getButton() {
      return this.button;
   }

   public int getColor() {
      return this.color;
   }

   public int getButtonColor() {
      return this.buttonColor;
   }

   public TextAlign getAlign() {
      return this.align;
   }

   public ButtonScreenModule withLine(String line) {
      return new ButtonScreenModule(line, this.channel, this.toggle, this.button, this.color, this.buttonColor, this.align);
   }

   public ButtonScreenModule withChannel(int channel) {
      return new ButtonScreenModule(this.line, channel, this.toggle, this.button, this.color, this.buttonColor, this.align);
   }

   public ButtonScreenModule withToggle(boolean toggle) {
      return new ButtonScreenModule(this.line, this.channel, toggle, this.button, this.color, this.buttonColor, this.align);
   }

   public ButtonScreenModule withButton(String button) {
      return new ButtonScreenModule(this.line, this.channel, this.toggle, button, this.color, this.buttonColor, this.align);
   }

   public ButtonScreenModule withColor(int color) {
      return new ButtonScreenModule(this.line, this.channel, this.toggle, this.button, color, this.buttonColor, this.align);
   }

   public ButtonScreenModule withButtonColor(int buttonColor) {
      return new ButtonScreenModule(this.line, this.channel, this.toggle, this.button, this.color, buttonColor, this.align);
   }

   public ButtonScreenModule withAlign(TextAlign align) {
      return new ButtonScreenModule(this.line, this.channel, this.toggle, this.button, this.color, this.buttonColor, align);
   }

   public IModuleDataBoolean getData(IScreenDataHelper helper, Level worldObj, long millis) {
      if (this.channel != -1 && this.toggle) {
         RedstoneChannels channels = RedstoneChannels.getChannels(worldObj);
         RedstoneChannels.RedstoneChannel ch = channels.getOrCreateChannel(this.channel);
         return helper.createBoolean(ch.getValue() > 0);
      } else {
         return null;
      }
   }

   public ButtonScreenModule validate(Level world, BlockPos pos, boolean isPlus) {
      return this;
   }

   public ItemStack mouseClick(ItemStack moduleStack, Level world, int x, int y, boolean clicked, Player player) {
      int xoffset;
      if (!this.line.isEmpty()) {
         xoffset = 80;
      } else {
         xoffset = 5;
      }

      if (x >= xoffset) {
         if (this.channel != -1) {
            if (this.toggle) {
               if (clicked) {
                  RedstoneChannels channels = RedstoneChannels.getChannels(world);
                  RedstoneChannels.RedstoneChannel ch = channels.getOrCreateChannel(this.channel);
                  ch.setValue(ch.getValue() == 0 ? 15 : 0);
                  channels.save();
               }
            } else {
               RedstoneChannels channels = RedstoneChannels.getChannels(world);
               RedstoneChannels.RedstoneChannel ch = channels.getOrCreateChannel(this.channel);
               ch.setValue(clicked ? 15 : 0);
               channels.save();
            }
         } else if (player != null) {
            player.sendSystemMessage(ComponentFactory.literal(ChatFormatting.RED + "Module is not linked to redstone channel!"));
         }
      }

      return ItemStack.EMPTY;
   }

   public int getRfPerTick() {
      return (Integer)ScreenConfiguration.BUTTON_RFPERTICK.get();
   }
}
