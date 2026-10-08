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
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.tileentity.LogicSupport;
import mcjty.lib.typed.Type;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsutility.compat.RFToolsUtilityTOPDriver;
import mcjty.rftoolsutility.modules.logic.LogicBlockModule;
import mcjty.rftoolsutility.modules.logic.data.CounterData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class CounterTileEntity extends GenericTileEntity {
   private final LogicSupport support = new LogicSupport();
   private boolean prevIn = false;
   @GuiValue
   public static final Value<CounterTileEntity, Integer> VALUE_COUNTER = Value.create(
      "counter", Type.INTEGER, CounterTileEntity::getCounter, CounterTileEntity::setCounter
   );
   @GuiValue
   public static final Value<CounterTileEntity, Integer> VALUE_CURRENT = Value.create(
      "current", Type.INTEGER, CounterTileEntity::getCurrent, CounterTileEntity::setCurrent
   );
   @Cap(type = CapType.CONTAINER)
   private static final Function<CounterTileEntity, MenuProvider> screenHandler = be -> new DefaultContainerProvider("Counter")
      .containerSupplier(DefaultContainerProvider.empty(LogicBlockModule.CONTAINER_COUNTER, be))
      .setupSync(be);

   public CounterTileEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)LogicBlockModule.COUNTER.be().get(), pos, state);
   }

   public static LogicSlabBlock createBlock() {
      return new LogicSlabBlock(
         new BlockBuilder()
            .topDriver(RFToolsUtilityTOPDriver.DRIVER)
            .manualEntry(ManualHelper.create("rftoolsbase:logic/counter"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsutility.shiftmessage")})
            .infoShift(new InfoLine[]{TooltipBuilder.header()})
            .tileEntitySupplier(CounterTileEntity::new)
      );
   }

   public int getRedstoneOutput(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
      return this.support.getRedstoneOutput(state, side);
   }

   public int getCounter() {
      CounterData data = (CounterData)this.getData(LogicBlockModule.COUNTER_DATA);
      return data.counter();
   }

   public int getCurrent() {
      CounterData data = (CounterData)this.getData(LogicBlockModule.COUNTER_DATA);
      return data.current();
   }

   public void setCounter(int counter) {
      CounterData data = (CounterData)this.getData(LogicBlockModule.COUNTER_DATA);
      if (counter != data.counter()) {
         data = data.withCounter(counter).withCurrent(0);
         this.setData(LogicBlockModule.COUNTER_DATA, data);
         this.support.setRedstoneState(this, 0);
      }
   }

   public void setCurrent(int current) {
      CounterData data = (CounterData)this.getData(LogicBlockModule.COUNTER_DATA);
      data = data.withCurrent(current);
      this.setData(LogicBlockModule.COUNTER_DATA, data);
   }

   protected void update() {
      if (!this.level.isClientSide()) {
         boolean pulse = this.powerLevel > 0 && !this.prevIn;
         this.prevIn = this.powerLevel > 0;
         int newout = 0;
         if (pulse) {
            CounterData data = (CounterData)this.getData(LogicBlockModule.COUNTER_DATA);
            int current = data.current();
            int counter = data.counter();
            if (++current >= counter) {
               current = 0;
               newout = 15;
            }

            data = data.withCurrent(current);
            this.setData(LogicBlockModule.COUNTER_DATA, data);
            this.support.setRedstoneState(this, newout);
         }
      }
   }

   public void loadAdditional(ValueInput tag) {
      super.loadAdditional(tag);
      this.support.setPowerOutput(tag.getBooleanOr("rs", false) ? 15 : 0);
      this.prevIn = tag.getBooleanOr("prevIn", false);
   }

   public void saveAdditional(ValueOutput tag) {
      super.saveAdditional(tag);
      tag.putBoolean("rs", this.support.getPowerOutput() > 0);
      tag.putBoolean("prevIn", this.prevIn);
   }

   public void checkRedstone(Level world, BlockPos pos) {
      this.support.checkRedstone(this, world, pos);
      this.update();
   }
}
