package mcjty.rftoolsutility.modules.logic.blocks;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.tileentity.LogicSupport;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.LogicFacing;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsutility.compat.RFToolsUtilityTOPDriver;
import mcjty.rftoolsutility.modules.logic.LogicBlockModule;
import mcjty.rftoolsutility.modules.logic.data.ThreeLogicData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ThreeLogicTileEntity extends GenericTileEntity {
   private final LogicSupport support = new LogicSupport();
   @Cap(type = CapType.CONTAINER)
   private static final Function<ThreeLogicTileEntity, MenuProvider> SCREEN_CAP = be -> new DefaultContainerProvider("Logic")
      .containerSupplier(DefaultContainerProvider.empty(LogicBlockModule.CONTAINER_LOGIC, be))
      .data(LogicBlockModule.THREELOGIC_DATA, ThreeLogicData.STREAM_CODEC, ThreeLogicData.CODEC)
      .setupSync(be);
   public static final Key<Integer> PARAM_INDEX = new Key("index", Type.INTEGER);
   public static final Key<Integer> PARAM_STATE = new Key("state", Type.INTEGER);
   @ServerCommand
   public static final Command<?> CMD_SETSTATE = Command.create("logic.setState", (te, player, params) -> {
      ThreeLogicData data = (ThreeLogicData)te.getData(LogicBlockModule.THREELOGIC_DATA);
      int[] logicTable = data.logicTableCopy();
      logicTable[params.get(PARAM_INDEX)] = (Integer)params.get(PARAM_STATE);
      te.setData(LogicBlockModule.THREELOGIC_DATA, data.withLogicTable(logicTable));
      te.checkRedstone(te.level, te.worldPosition);
   });
   private static final Set<BlockPos> loopDetector = new HashSet<>();

   public static LogicSlabBlock createBlock() {
      return new LogicSlabBlock(
         new BlockBuilder()
            .topDriver(RFToolsUtilityTOPDriver.DRIVER)
            .manualEntry(ManualHelper.create("rftoolsbase:logic/logic"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsutility.shiftmessage")})
            .infoShift(new InfoLine[]{TooltipBuilder.header()})
            .tileEntitySupplier(ThreeLogicTileEntity::new)
      );
   }

   public ThreeLogicTileEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)LogicBlockModule.LOGIC.be().get(), pos, state);
   }

   public int getRedstoneOutput(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
      return this.support.getRedstoneOutput(state, side);
   }

   public int getState(int index) {
      ThreeLogicData data = (ThreeLogicData)this.getData(LogicBlockModule.THREELOGIC_DATA);
      return data.logicTable()[index];
   }

   public void checkRedstone() {
      ThreeLogicData data = (ThreeLogicData)this.getData(LogicBlockModule.THREELOGIC_DATA);
      int s = data.logicTable()[this.powerLevel];
      if (s != -1) {
         this.support.setRedstoneState(this, s == 1 ? 15 : 0);
      }
   }

   public void loadAdditional(ValueInput tag) {
      super.loadAdditional(tag);
      this.support.setPowerOutput(tag.getBooleanOr("rs", false) ? 15 : 0);
   }

   public void saveAdditional(ValueOutput tag) {
      super.saveAdditional(tag);
      tag.putBoolean("rs", this.support.getPowerOutput() > 0);
   }

   public void checkRedstone(Level world, BlockPos pos) {
      BlockState state = world.getBlockState(pos);
      if (loopDetector.add(pos)) {
         try {
            LogicFacing facing = LogicSupport.getFacing(state);
            Direction downSide = facing.getSide();
            Direction inputSide = facing.getInputSide();
            Direction leftSide = LogicSlabBlock.rotateLeft(downSide, inputSide);
            Direction rightSide = LogicSlabBlock.rotateRight(downSide, inputSide);
            int powered1 = this.support.getInputStrength(world, pos, leftSide) > 0 ? 1 : 0;
            int powered2 = this.support.getInputStrength(world, pos, inputSide) > 0 ? 2 : 0;
            int powered3 = this.support.getInputStrength(world, pos, rightSide) > 0 ? 4 : 0;
            this.setPowerInput(powered1 + powered2 + powered3);
            this.checkRedstone();
         } finally {
            loopDetector.remove(pos);
         }
      }
   }
}
