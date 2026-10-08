package mcjty.rftoolsutility.modules.screen.modules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import mcjty.lib.varia.CapabilityTools;
import mcjty.lib.varia.LevelTools;
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
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public record FluidBarScreenModule(
   GlobalPos pos, ScreenModuleHelper helper, boolean active, String line, int color, TextAlign align, ILevelRenderHelper mbRenderer, String monitor
) implements IScreenModule<FluidBarScreenModule, IModuleDataContents> {
   public static final FluidBarScreenModule DEFAULT = new FluidBarScreenModule(
      GlobalPos.of(Level.OVERWORLD, BlockPos.ZERO), "", 16777215, TextAlign.ALIGN_LEFT, new ScreenLevelHelper(), ""
   );
   public static final Codec<FluidBarScreenModule> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            GlobalPos.CODEC.fieldOf("pos").forGetter(module -> module.pos),
            Codec.STRING.fieldOf("line").forGetter(module -> module.line),
            Codec.INT.fieldOf("color").forGetter(module -> module.color),
            TextAlign.CODEC.fieldOf("align").forGetter(module -> module.align),
            ScreenLevelHelper.CODEC.fieldOf("mbRenderer").forGetter(module -> (ScreenLevelHelper)module.mbRenderer),
            Codec.STRING.fieldOf("monitor").forGetter(module -> module.monitor)
         )
         .apply(instance, FluidBarScreenModule::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, FluidBarScreenModule> STREAM_CODEC = StreamCodec.composite(
      GlobalPos.STREAM_CODEC,
      module -> module.pos,
      ByteBufCodecs.STRING_UTF8,
      module -> module.line,
      ByteBufCodecs.INT,
      module -> module.color,
      TextAlign.STREAM_CODEC,
      module -> module.align,
      ScreenLevelHelper.STREAM_CODEC,
      module -> (ScreenLevelHelper)module.mbRenderer,
      ByteBufCodecs.STRING_UTF8,
      module -> module.monitor,
      FluidBarScreenModule::new
   );

   public FluidBarScreenModule(GlobalPos pos, String line, int color, TextAlign align, ILevelRenderHelper mbRenderer, String monitor) {
      this(pos, new ScreenModuleHelper(), false, line, color, align, mbRenderer, monitor);
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

   public TextAlign getAlign() {
      return this.align;
   }

   public int getPosColor() {
      return this.mbRenderer.getPosColor();
   }

   public int getNegColor() {
      return this.mbRenderer.getNegColor();
   }

   public boolean isHideBar() {
      return this.mbRenderer.isHideBar();
   }

   public FormatStyle getFormat() {
      return this.mbRenderer.getFormatStyle();
   }

   public BarMode getBarMode() {
      return this.mbRenderer.getBarMode();
   }

   public ILevelRenderHelper getMbRenderer() {
      return this.mbRenderer;
   }

   public FluidBarScreenModule withLine(String line) {
      return new FluidBarScreenModule(this.pos, this.helper, this.active, line, this.color, this.align, this.mbRenderer, this.monitor);
   }

   public FluidBarScreenModule withColor(int color) {
      return new FluidBarScreenModule(this.pos, this.helper, this.active, this.line, color, this.align, this.mbRenderer, this.monitor);
   }

   public FluidBarScreenModule withAlign(TextAlign align) {
      return new FluidBarScreenModule(this.pos, this.helper, this.active, this.line, this.color, align, this.mbRenderer, this.monitor);
   }

   public FluidBarScreenModule withPos(GlobalPos pos) {
      return new FluidBarScreenModule(pos, this.helper, this.active, this.line, this.color, this.align, this.mbRenderer, this.monitor);
   }

   public FluidBarScreenModule withMonitor(String monitor) {
      return new FluidBarScreenModule(this.pos, this.helper, this.active, this.line, this.color, this.align, this.mbRenderer, monitor);
   }

   public FluidBarScreenModule withActive(boolean active) {
      return new FluidBarScreenModule(this.pos, this.helper, active, this.line, this.color, this.align, this.mbRenderer, this.monitor);
   }

   public FluidBarScreenModule withPosColor(int posColor) {
      this.mbRenderer.setPosColor(posColor);
      return new FluidBarScreenModule(this.pos, this.helper, this.active, this.line, this.color, this.align, this.mbRenderer, this.monitor);
   }

   public FluidBarScreenModule withNegColor(int negColor) {
      this.mbRenderer.setNegColor(negColor);
      return new FluidBarScreenModule(this.pos, this.helper, this.active, this.line, this.color, this.align, this.mbRenderer, this.monitor);
   }

   public FluidBarScreenModule withHideBar(boolean hideBar) {
      this.mbRenderer.setHideBar(hideBar);
      return new FluidBarScreenModule(this.pos, this.helper, this.active, this.line, this.color, this.align, this.mbRenderer, this.monitor);
   }

   public FluidBarScreenModule withFormat(FormatStyle format) {
      this.mbRenderer.setFormatStyle(format);
      return new FluidBarScreenModule(this.pos, this.helper, this.active, this.line, this.color, this.align, this.mbRenderer, this.monitor);
   }

   public FluidBarScreenModule withBarMode(BarMode mode) {
      this.mbRenderer.setBarMode(mode);
      return new FluidBarScreenModule(this.pos, this.helper, this.active, this.line, this.color, this.align, this.mbRenderer, this.monitor);
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
            AtomicInteger contents = new AtomicInteger();
            AtomicInteger maxContents = new AtomicInteger();
            BlockEntity te = world.getBlockEntity(this.pos.pos());
            IFluidHandler hf = CapabilityTools.getFluidCapabilitySafe(te);
            if (hf != null && hf.getTanks() > 0) {
               if (!hf.getFluidInTank(0).isEmpty()) {
                  contents.set(hf.getFluidInTank(0).getAmount());
               }

               maxContents.set(hf.getTankCapacity(0));
            }

            return this.helper.getContentsValue(millis, contents.get(), maxContents.get());
         }
      }
   }

   public FluidBarScreenModule validate(Level world, BlockPos p, boolean isPlus) {
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
      return (Integer)ScreenConfiguration.FLUID_RFPERTICK.get();
   }

   public ItemStack mouseClick(ItemStack moduleStack, Level world, int x, int y, boolean clicked, Player player) {
      return ItemStack.EMPTY;
   }
}
