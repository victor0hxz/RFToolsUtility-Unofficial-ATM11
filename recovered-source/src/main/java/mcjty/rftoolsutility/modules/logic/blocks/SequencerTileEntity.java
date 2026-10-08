package mcjty.rftoolsutility.modules.logic.blocks;

import java.util.function.Function;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.bindings.GuiValue;
import mcjty.lib.bindings.Value;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.LogicSupport;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbase.tools.TickOrderHandler;
import mcjty.rftoolsbase.tools.TickOrderHandler.IOrderTicker;
import mcjty.rftoolsbase.tools.TickOrderHandler.Rank;
import mcjty.rftoolsutility.compat.RFToolsUtilityTOPDriver;
import mcjty.rftoolsutility.modules.logic.LogicBlockModule;
import mcjty.rftoolsutility.modules.logic.data.SequencerData;
import mcjty.rftoolsutility.modules.logic.tools.SequencerMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SequencerTileEntity extends TickingTileEntity implements IOrderTicker {
   private final LogicSupport support = new LogicSupport();
   private int currentStep = -1;
   public static final Key<Integer> PARAM_BIT = new Key("bit", Type.INTEGER);
   public static final Key<Boolean> PARAM_CHOICE = new Key("choice", Type.BOOLEAN);
   @GuiValue
   public static final Value<SequencerTileEntity, String> VALUE_MODE = Value.createEnum(
      "mode", SequencerMode.values(), SequencerTileEntity::getMode, SequencerTileEntity::setMode
   );
   @GuiValue
   public static final Value<SequencerTileEntity, Boolean> VALUE_ENDSTATE = Value.create(
      "endstate", Type.BOOLEAN, SequencerTileEntity::getEndState, SequencerTileEntity::setEndState
   );
   @GuiValue
   public static final Value<SequencerTileEntity, Integer> VALUE_STEPCOUNT = Value.create(
      "stepcount", Type.INTEGER, SequencerTileEntity::getStepcount, SequencerTileEntity::setStepcount
   );
   @GuiValue
   public static final Value<SequencerTileEntity, Integer> VALUE_DELAY = Value.create(
      "delay", Type.INTEGER, SequencerTileEntity::getDelay, SequencerTileEntity::setDelay
   );
   private boolean prevIn = false;
   private int timer = 0;
   @Cap(type = CapType.CONTAINER)
   private static final Function<SequencerTileEntity, MenuProvider> SCREEN_CAP = be -> new DefaultContainerProvider("Sequencer")
      .containerSupplier(DefaultContainerProvider.empty(LogicBlockModule.CONTAINER_SEQUENCER, be))
      .data(LogicBlockModule.SEQUENCER_DATA, SequencerData.STREAM_CODEC, SequencerData.CODEC)
      .setupSync(be);
   @ServerCommand
   public static final Command<?> CMD_FLIPBITS = Command.create("sequencer.flipBits", (te, player, params) -> te.flipCycleBits());
   @ServerCommand
   public static final Command<?> CMD_CLEARBITS = Command.create("sequencer.clearBits", (te, player, params) -> te.clearCycleBits());
   @ServerCommand
   public static final Command<?> CMD_SETBIT = Command.create(
      "sequencer.setBit", (te, player, params) -> te.setCycleBit((Integer)params.get(PARAM_BIT), (Boolean)params.get(PARAM_CHOICE))
   );

   public boolean getEndState() {
      SequencerData data = (SequencerData)this.getData(LogicBlockModule.SEQUENCER_DATA);
      return data.endstate();
   }

   public void setEndState(boolean endstate) {
      SequencerData data = (SequencerData)this.getData(LogicBlockModule.SEQUENCER_DATA);
      data = data.withEndstate(endstate);
      this.setData(LogicBlockModule.SEQUENCER_DATA, data);
   }

   public int getStepcount() {
      SequencerData data = (SequencerData)this.getData(LogicBlockModule.SEQUENCER_DATA);
      return data.stepcount();
   }

   public void setStepcount(int stepcount) {
      SequencerData data = (SequencerData)this.getData(LogicBlockModule.SEQUENCER_DATA);
      data = data.withStepcount(stepcount);
      this.setData(LogicBlockModule.SEQUENCER_DATA, data);
   }

   public int getDelay() {
      SequencerData data = (SequencerData)this.getData(LogicBlockModule.SEQUENCER_DATA);
      return data.delay();
   }

   public void setDelay(int delay) {
      SequencerData data = (SequencerData)this.getData(LogicBlockModule.SEQUENCER_DATA);
      data = data.withDelay(delay);
      this.setData(LogicBlockModule.SEQUENCER_DATA, data);
   }

   public static LogicSlabBlock createBlock() {
      return new LogicSlabBlock(
         new BlockBuilder()
            .topDriver(RFToolsUtilityTOPDriver.DRIVER)
            .manualEntry(ManualHelper.create("rftoolsbase:logic/sequencer"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsutility.shiftmessage")})
            .infoShift(new InfoLine[]{TooltipBuilder.header()})
            .tileEntitySupplier(SequencerTileEntity::new)
      );
   }

   public SequencerTileEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)LogicBlockModule.SEQUENCER.be().get(), pos, state);
   }

   public void checkRedstone(Level world, BlockPos pos) {
      this.support.checkRedstone(this, world, pos);
   }

   public int getRedstoneOutput(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
      return this.support.getRedstoneOutput(state, side);
   }

   public SequencerMode getMode() {
      SequencerData data = (SequencerData)this.getData(LogicBlockModule.SEQUENCER_DATA);
      return data.sequencerMode();
   }

   public void setMode(SequencerMode mode) {
      SequencerData data = (SequencerData)this.getData(LogicBlockModule.SEQUENCER_DATA);
      data = data.withSequencerMode(mode);
      this.setData(LogicBlockModule.SEQUENCER_DATA, data);
      switch (mode) {
         case MODE_ONCE1:
         case MODE_ONCE2:
         case MODE_LOOP3:
         case MODE_LOOP4:
            this.currentStep = -1;
            break;
         case MODE_LOOP1:
         case MODE_LOOP2:
         case MODE_STEP:
            this.currentStep = 0;
      }

      this.setChanged();
   }

   public int getCurrentStep() {
      return this.currentStep;
   }

   public boolean getCycleBit(int bit) {
      SequencerData data = (SequencerData)this.getData(LogicBlockModule.SEQUENCER_DATA);
      return (data.bits() >> bit & 1L) == 1L;
   }

   public long getCycleBits() {
      SequencerData data = (SequencerData)this.getData(LogicBlockModule.SEQUENCER_DATA);
      return data.bits();
   }

   public void setCycleBit(int bit, boolean flag) {
      SequencerData data = (SequencerData)this.getData(LogicBlockModule.SEQUENCER_DATA);
      long cycleBits = data.bits();
      if (flag) {
         cycleBits |= 1L << bit;
      } else {
         cycleBits &= ~(1L << bit);
      }

      data = data.withBits(cycleBits);
      this.setData(LogicBlockModule.SEQUENCER_DATA, data);
   }

   public void flipCycleBits() {
      SequencerData data = (SequencerData)this.getData(LogicBlockModule.SEQUENCER_DATA);
      data = data.withBits(~data.bits());
      this.setData(LogicBlockModule.SEQUENCER_DATA, data);
   }

   public void clearCycleBits() {
      SequencerData data = (SequencerData)this.getData(LogicBlockModule.SEQUENCER_DATA);
      data = data.withBits(0L);
      this.setData(LogicBlockModule.SEQUENCER_DATA, data);
   }

   protected void tickServer() {
      TickOrderHandler.queue(this);
   }

   public Rank getRank() {
      return Rank.RANK_4;
   }

   public void tickOnServer() {
      boolean pulse = this.powerLevel > 0 && !this.prevIn;
      this.prevIn = this.powerLevel > 0;
      if (pulse) {
         this.handlePulse();
      }

      this.setChanged();
      this.timer--;
      SequencerData data = (SequencerData)this.getData(LogicBlockModule.SEQUENCER_DATA);
      if (this.timer <= 0) {
         this.timer = data.delay();
         this.support.setRedstoneState(this, this.checkOutput() ? 15 : 0);
         this.handleCycle(this.powerLevel > 0);
      } else if (this.timer > data.delay()) {
         this.timer = data.delay();
      }
   }

   public boolean checkOutput() {
      return this.currentStep == -1 ? ((SequencerData)this.getData(LogicBlockModule.SEQUENCER_DATA)).endstate() : this.getCycleBit(this.currentStep);
   }

   private void handleCycle(boolean redstone) {
      SequencerData data = (SequencerData)this.getData(LogicBlockModule.SEQUENCER_DATA);
      switch (data.sequencerMode()) {
         case MODE_ONCE1:
         case MODE_ONCE2:
            if (this.currentStep != -1) {
               this.nextStepAndStop();
            }
            break;
         case MODE_LOOP3:
            if (redstone) {
               this.nextStep();
            }
            break;
         case MODE_LOOP4:
            if (redstone) {
               this.nextStep();
            } else {
               this.currentStep = -1;
            }
            break;
         case MODE_LOOP1:
            this.nextStep();
            break;
         case MODE_LOOP2:
            this.nextStep();
         case MODE_STEP:
      }
   }

   private void handlePulse() {
      SequencerData data = (SequencerData)this.getData(LogicBlockModule.SEQUENCER_DATA);
      switch (data.sequencerMode()) {
         case MODE_ONCE1:
            if (this.currentStep == -1) {
               this.currentStep = 0;
            }
            break;
         case MODE_ONCE2:
            this.currentStep = 0;
         case MODE_LOOP3:
         case MODE_LOOP4:
         case MODE_LOOP1:
         default:
            break;
         case MODE_LOOP2:
            this.currentStep = 0;
            break;
         case MODE_STEP:
            this.nextStep();
      }
   }

   private void nextStep() {
      SequencerData data = (SequencerData)this.getData(LogicBlockModule.SEQUENCER_DATA);
      this.currentStep++;
      if (this.currentStep >= data.stepcount()) {
         this.currentStep = 0;
      }
   }

   private void nextStepAndStop() {
      SequencerData data = (SequencerData)this.getData(LogicBlockModule.SEQUENCER_DATA);
      this.currentStep++;
      if (this.currentStep >= data.stepcount()) {
         this.currentStep = -1;
      }
   }

   public void loadAdditional(ValueInput tag) {
      super.loadAdditional(tag);
      this.support.setPowerOutput(tag.getBooleanOr("rs", false) ? 15 : 0);
      this.currentStep = tag.getIntOr("step", 0);
      this.prevIn = tag.getBooleanOr("prevIn", false);
      this.timer = tag.getIntOr("timer", 0);
   }

   public void saveAdditional(ValueOutput tag) {
      super.saveAdditional(tag);
      tag.putBoolean("rs", this.support.getPowerOutput() > 0);
      tag.putInt("step", this.currentStep);
      tag.putBoolean("prevIn", this.prevIn);
      tag.putInt("timer", this.timer);
   }
}
