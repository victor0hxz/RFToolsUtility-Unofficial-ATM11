package mcjty.rftoolsutility.modules.screen.modules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.CompositeStreamCodec;
import mcjty.lib.varia.EnergyTools;
import mcjty.lib.varia.LevelTools;
import mcjty.lib.varia.EnergyTools.EnergyLevel;
import mcjty.rftoolsbase.api.screens.BarMode;
import mcjty.rftoolsbase.api.screens.FormatStyle;
import mcjty.rftoolsbase.api.screens.ILevelRenderHelper;
import mcjty.rftoolsbase.api.screens.IScreenDataHelper;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.TextAlign;
import mcjty.rftoolsbase.api.screens.data.IModuleDataContents;
import mcjty.rftoolsutility.modules.screen.ScreenConfiguration;
import mcjty.rftoolsutility.modules.screen.modulesclient.helper.ScreenLevelHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public record EnergyBarScreenModule(
   GlobalPos pos,
   Direction side,
   ScreenModuleHelper helper,
   boolean active,
   String line,
   int color,
   TextAlign align,
   ILevelRenderHelper rfRenderer,
   String monitor
) implements IScreenModule<EnergyBarScreenModule, IModuleDataContents> {
   public static final EnergyBarScreenModule DEFAULT = new EnergyBarScreenModule(
      GlobalPos.of(Level.OVERWORLD, BlockPosTools.INVALID),
      Direction.DOWN,
      new ScreenModuleHelper(),
      false,
      "",
      16777215,
      TextAlign.ALIGN_LEFT,
      new ScreenLevelHelper().gradient(-65536, -13421824),
      ""
   );
   public static final Codec<EnergyBarScreenModule> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            GlobalPos.CODEC.fieldOf("pos").forGetter(module -> module.pos),
            Direction.CODEC.fieldOf("side").forGetter(module -> module.side),
            Codec.STRING.fieldOf("line").forGetter(module -> module.line),
            Codec.INT.fieldOf("color").forGetter(module -> module.color),
            TextAlign.CODEC.fieldOf("align").forGetter(module -> module.align),
            ScreenLevelHelper.CODEC.fieldOf("rfRenderer").forGetter(module -> (ScreenLevelHelper)module.rfRenderer),
            Codec.STRING.fieldOf("monitor").forGetter(module -> module.monitor)
         )
         .apply(instance, EnergyBarScreenModule::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, EnergyBarScreenModule> STREAM_CODEC = CompositeStreamCodec.composite(
      GlobalPos.STREAM_CODEC,
      module -> module.pos,
      Direction.STREAM_CODEC,
      module -> module.side,
      ByteBufCodecs.STRING_UTF8,
      module -> module.line,
      ByteBufCodecs.INT,
      module -> module.color,
      TextAlign.STREAM_CODEC,
      module -> module.align,
      ScreenLevelHelper.STREAM_CODEC,
      module -> (ScreenLevelHelper)module.rfRenderer,
      ByteBufCodecs.STRING_UTF8,
      module -> module.monitor,
      EnergyBarScreenModule::new
   );

   public EnergyBarScreenModule(GlobalPos pos, Direction side, String line, int color, TextAlign align, ILevelRenderHelper rfRenderer, String monitor) {
      this(pos, side, new ScreenModuleHelper(), false, line, color, align, rfRenderer, monitor);
   }

   public String getLine() {
      return this.line;
   }

   public int getColor() {
      return this.color;
   }

   public GlobalPos getPos() {
      return this.pos;
   }

   public String getMonitor() {
      return this.monitor;
   }

   public Direction getSide() {
      return this.side;
   }

   public TextAlign getAlign() {
      return this.align;
   }

   public int getPosColor() {
      return this.rfRenderer.getPosColor();
   }

   public int getNegColor() {
      return this.rfRenderer.getNegColor();
   }

   public boolean isHideBar() {
      return this.rfRenderer.isHideBar();
   }

   public FormatStyle getFormat() {
      return this.rfRenderer.getFormatStyle();
   }

   public BarMode getBarMode() {
      return this.rfRenderer.getBarMode();
   }

   public ILevelRenderHelper getRfRenderer() {
      return this.rfRenderer;
   }

   public EnergyBarScreenModule withLine(String line) {
      return new EnergyBarScreenModule(this.pos, this.side, this.helper, this.active, line, this.color, this.align, this.rfRenderer, this.monitor);
   }

   public EnergyBarScreenModule withColor(int color) {
      return new EnergyBarScreenModule(this.pos, this.side, this.helper, this.active, this.line, color, this.align, this.rfRenderer, this.monitor);
   }

   public EnergyBarScreenModule withAlign(TextAlign align) {
      return new EnergyBarScreenModule(this.pos, this.side, this.helper, this.active, this.line, this.color, align, this.rfRenderer, this.monitor);
   }

   public EnergyBarScreenModule withPos(GlobalPos pos) {
      return new EnergyBarScreenModule(pos, this.side, this.helper, this.active, this.line, this.color, this.align, this.rfRenderer, this.monitor);
   }

   public EnergyBarScreenModule withMonitor(String monitor) {
      return new EnergyBarScreenModule(this.pos, this.side, this.helper, this.active, this.line, this.color, this.align, this.rfRenderer, monitor);
   }

   public EnergyBarScreenModule withActive(boolean active) {
      return new EnergyBarScreenModule(this.pos, this.side, this.helper, active, this.line, this.color, this.align, this.rfRenderer, this.monitor);
   }

   public EnergyBarScreenModule withSide(Direction side) {
      return new EnergyBarScreenModule(this.pos, side, this.helper, this.active, this.line, this.color, this.align, this.rfRenderer, this.monitor);
   }

   public EnergyBarScreenModule withPosColor(int posColor) {
      this.rfRenderer.setPosColor(posColor);
      return new EnergyBarScreenModule(this.pos, this.side, this.helper, this.active, this.line, this.color, this.align, this.rfRenderer, this.monitor);
   }

   public EnergyBarScreenModule withNegColor(int negColor) {
      this.rfRenderer.setNegColor(negColor);
      return new EnergyBarScreenModule(this.pos, this.side, this.helper, this.active, this.line, this.color, this.align, this.rfRenderer, this.monitor);
   }

   public EnergyBarScreenModule withHideBar(boolean hideBar) {
      this.rfRenderer.setHideBar(hideBar);
      return new EnergyBarScreenModule(this.pos, this.side, this.helper, this.active, this.line, this.color, this.align, this.rfRenderer, this.monitor);
   }

   public EnergyBarScreenModule withFormat(FormatStyle format) {
      this.rfRenderer.setFormatStyle(format);
      return new EnergyBarScreenModule(this.pos, this.side, this.helper, this.active, this.line, this.color, this.align, this.rfRenderer, this.monitor);
   }

   public EnergyBarScreenModule withBarMode(BarMode mode) {
      this.rfRenderer.setBarMode(mode);
      return new EnergyBarScreenModule(this.pos, this.side, this.helper, this.active, this.line, this.color, this.align, this.rfRenderer, this.monitor);
   }

   public IModuleDataContents getData(IScreenDataHelper h, Level worldObj, long millis) {
      if (!this.active) {
         return null;
      } else {
         Level world = LevelTools.getLevel(worldObj, this.pos.dimension());
         if (world == null) {
            return null;
         } else if (!LevelTools.isLoaded(world, this.pos.pos())) {
            return null;
         } else {
            BlockEntity te = world.getBlockEntity(this.pos.pos());
            if (!EnergyTools.isEnergyTE(te, this.side)) {
               return null;
            } else {
               EnergyLevel energyLevel = EnergyTools.getEnergyLevelMulti(te, this.side);
               long energy = energyLevel.energy();
               long maxEnergy = energyLevel.maxEnergy();
               return this.helper.getContentsValue(millis, energy, maxEnergy);
            }
         }
      }
   }

   public EnergyBarScreenModule validate(Level world, BlockPos p, boolean isPlus) {
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
      return (Integer)ScreenConfiguration.ENERGY_RFPERTICK.get();
   }

   public ItemStack mouseClick(ItemStack moduleStack, Level world, int x, int y, boolean clicked, Player player) {
      return ItemStack.EMPTY;
   }
}
