package mcjty.rftoolsutility.modules.logic.blocks;

import java.util.function.Function;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.bindings.GuiValue;
import mcjty.lib.bindings.Value;
import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.LogicSupport;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.lib.typed.Type;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbase.tools.TickOrderHandler;
import mcjty.rftoolsbase.tools.TickOrderHandler.IOrderTicker;
import mcjty.rftoolsbase.tools.TickOrderHandler.Rank;
import mcjty.rftoolsutility.compat.RFToolsUtilityTOPDriver;
import mcjty.rftoolsutility.modules.logic.LogicBlockModule;
import mcjty.rftoolsutility.modules.logic.data.TimerData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TimerTileEntity extends TickingTileEntity implements IOrderTicker {
   private final LogicSupport support = new LogicSupport();
   private boolean prevIn = false;
   private int timer = 0;
   @GuiValue
   public static final Value<TimerTileEntity, Integer> VALUE_DELAY = Value.create("delay", Type.INTEGER, TimerTileEntity::getDelay, TimerTileEntity::setDelay);
   @GuiValue(name = "pauses")
   public static final Value<TimerTileEntity, Boolean> VALUE_PAUSES = Value.create(
      "pauses", Type.BOOLEAN, TimerTileEntity::isRedstonePauses, TimerTileEntity::setRedstonePauses
   );
   @Cap(type = CapType.CONTAINER)
   private static final Function<TimerTileEntity, MenuProvider> screenHandler = be -> new DefaultContainerProvider("Timer")
      .containerSupplier(DefaultContainerProvider.empty(LogicBlockModule.CONTAINER_TIMER, be))
      .setupSync(be);

   public static LogicSlabBlock createBlock() {
      return new LogicSlabBlock(
         new BlockBuilder()
            .topDriver(RFToolsUtilityTOPDriver.DRIVER)
            .manualEntry(ManualHelper.create("rftoolsbase:logic/timer"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsutility.shiftmessage")})
            .infoShift(new InfoLine[]{TooltipBuilder.header()})
            .tileEntitySupplier(TimerTileEntity::new)
      );
   }

   public TimerTileEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)LogicBlockModule.TIMER.be().get(), pos, state);
   }

   public int getTimer() {
      return this.timer;
   }

   protected void tickServer() {
      TickOrderHandler.queue(this);
   }

   public Rank getRank() {
      return Rank.RANK_3;
   }

   public int getDelay() {
      TimerData data = (TimerData)this.getData(LogicBlockModule.TIMER_DATA);
      return data.delay();
   }

   public void setDelay(int delay) {
      TimerData data = (TimerData)this.getData(LogicBlockModule.TIMER_DATA);
      data = data.withDelay(delay);
      this.setData(LogicBlockModule.TIMER_DATA, data);
   }

   public boolean isRedstonePauses() {
      TimerData data = (TimerData)this.getData(LogicBlockModule.TIMER_DATA);
      return data.redstonePauses();
   }

   public void setRedstonePauses(boolean redstonePauses) {
      TimerData data = (TimerData)this.getData(LogicBlockModule.TIMER_DATA);
      data = data.withRedstonePauses(redstonePauses);
      this.setData(LogicBlockModule.TIMER_DATA, data);
   }

   public void tickOnServer() {
      boolean pulse = this.powerLevel > 0 && !this.prevIn;
      this.prevIn = this.powerLevel > 0;
      this.setChanged();
      TimerData data = (TimerData)this.getData(LogicBlockModule.TIMER_DATA);
      if (pulse) {
         this.timer = data.delay();
      }

      if (!data.redstonePauses() || !this.prevIn) {
         this.timer--;
      }

      int newout;
      if (this.timer <= 0) {
         this.timer = data.delay();
         newout = 15;
      } else {
         newout = 0;
      }

      this.support.setRedstoneState(this, newout);
   }

   public void checkRedstone(Level world, BlockPos pos) {
      this.support.checkRedstone(this, world, pos);
   }

   public int getRedstoneOutput(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
      return this.support.getRedstoneOutput(state, side);
   }

   public void loadAdditional(ValueInput tag) {
      super.loadAdditional(tag);
      this.support.setPowerOutput(tag.getBooleanOr("rs", false) ? 15 : 0);
      this.prevIn = tag.getBooleanOr("prevIn", false);
      this.timer = tag.getIntOr("timer", 0);
   }

   public void saveAdditional(ValueOutput tag) {
      super.saveAdditional(tag);
      tag.putBoolean("rs", this.support.getPowerOutput() > 0);
      tag.putBoolean("prevIn", this.prevIn);
      tag.putInt("timer", this.timer);
   }
}
