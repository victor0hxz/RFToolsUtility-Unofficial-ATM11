package mcjty.rftoolsutility.modules.logic.blocks;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.container.ItemInventory;
import mcjty.lib.bindings.GuiValue;
import mcjty.lib.bindings.Value;
import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.setup.Registration;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.LogicSupport;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.LogicFacing;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsutility.compat.RFToolsUtilityTOPDriver;
import mcjty.rftoolsutility.modules.logic.LogicBlockModule;
import mcjty.rftoolsutility.modules.logic.data.SensorData;
import mcjty.rftoolsutility.modules.logic.tools.AreaType;
import mcjty.rftoolsutility.modules.logic.tools.GroupType;
import mcjty.rftoolsutility.modules.logic.tools.SensorType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;

public class SensorTileEntity extends TickingTileEntity {
   private final LogicSupport support = new LogicSupport();
   public static final int SLOT_ITEMMATCH = 0;
   public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(
      () -> new ContainerFactory(1).slot(SlotDefinition.ghost(), 0, 154, 24).playerSlots(10, 70)
   );
   private final GenericItemHandler items = GenericItemHandler.create(this, CONTAINER_FACTORY).itemValid(GenericItemHandler.no()).build();
   @Cap(type = CapType.ITEMS_AUTOMATION)
   private static final Function<SensorTileEntity, GenericItemHandler> ITEM_CAP = be -> be.items;
   @Cap(type = CapType.CONTAINER)
   private static final Function<SensorTileEntity, MenuProvider> SCREEN_CAP = be -> new DefaultContainerProvider("Sensor")
      .containerSupplier(DefaultContainerProvider.container(LogicBlockModule.CONTAINER_SENSOR, CONTAINER_FACTORY, be))
      .itemHandler(() -> be.items)
      .setupSync(be);
   @GuiValue
   public static final Value<SensorTileEntity, Integer> VALUE_NUMBER = Value.create(
      "number", Type.INTEGER, SensorTileEntity::getNumber, SensorTileEntity::setNumber
   );
   @GuiValue
   public static final Value<SensorTileEntity, String> VALUE_TYPE = Value.createEnum(
      "type", SensorType.values(), SensorTileEntity::getSensorType, SensorTileEntity::setSensorType
   );
   @GuiValue
   public static final Value<SensorTileEntity, String> VALUE_AREA = Value.createEnum(
      "area", AreaType.values(), SensorTileEntity::getAreaType, SensorTileEntity::setAreaType
   );
   @GuiValue
   public static final Value<SensorTileEntity, String> VALUE_GROUP = Value.createEnum(
      "group", GroupType.values(), SensorTileEntity::getGroupType, SensorTileEntity::setGroupType
   );
   private int checkCounter = 0;
   private AABB cachedBox = null;

   public static LogicSlabBlock createBlock() {
      return new LogicSlabBlock(
         new BlockBuilder()
            .topDriver(RFToolsUtilityTOPDriver.DRIVER)
            .manualEntry(ManualHelper.create("rftoolsbase:logic/sensor"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsutility.shiftmessage")})
            .infoShift(new InfoLine[]{TooltipBuilder.header()})
            .tileEntitySupplier(SensorTileEntity::new)
      );
   }

   public SensorTileEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)LogicBlockModule.SENSOR.be().get(), pos, state);
   }

   public int getNumber() {
      SensorData data = (SensorData)this.getData(LogicBlockModule.SENSOR_DATA);
      return data.number();
   }

   public void setNumber(int number) {
      SensorData data = (SensorData)this.getData(LogicBlockModule.SENSOR_DATA);
      data = data.withNumber(number);
      this.setData(LogicBlockModule.SENSOR_DATA, data);
   }

   public SensorType getSensorType() {
      SensorData data = (SensorData)this.getData(LogicBlockModule.SENSOR_DATA);
      return data.sensorType();
   }

   public void setSensorType(SensorType sensorType) {
      SensorData data = (SensorData)this.getData(LogicBlockModule.SENSOR_DATA);
      data = data.withSensorType(sensorType);
      this.setData(LogicBlockModule.SENSOR_DATA, data);
      this.cachedBox = null;
   }

   public AreaType getAreaType() {
      SensorData data = (SensorData)this.getData(LogicBlockModule.SENSOR_DATA);
      return data.areaType();
   }

   public void setAreaType(AreaType areaType) {
      SensorData data = (SensorData)this.getData(LogicBlockModule.SENSOR_DATA);
      data = data.withAreaType(areaType);
      this.setData(LogicBlockModule.SENSOR_DATA, data);
      this.cachedBox = null;
   }

   public GroupType getGroupType() {
      SensorData data = (SensorData)this.getData(LogicBlockModule.SENSOR_DATA);
      return data.groupType();
   }

   public void setGroupType(GroupType groupType) {
      SensorData data = (SensorData)this.getData(LogicBlockModule.SENSOR_DATA);
      data = data.withGroupType(groupType);
      this.setData(LogicBlockModule.SENSOR_DATA, data);
      this.cachedBox = null;
   }

   protected void tickServer() {
      this.checkCounter--;
      if (this.checkCounter <= 0) {
         this.checkCounter = 10;
         this.support.setRedstoneState(this, this.checkSensor() ? 15 : 0);
      }
   }

   public void checkRedstone(Level world, BlockPos pos) {
      this.support.checkRedstone(this, world, pos);
   }

   public int getRedstoneOutput(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
      return this.support.getRedstoneOutput(state, side);
   }

   public boolean checkSensor() {
      LogicFacing facing = LogicSupport.getFacing(this.level.getBlockState(this.getBlockPos()));
      Direction inputSide = facing.getInputSide();
      BlockPos newpos = this.getBlockPos().relative(inputSide);

      return switch (this.getSensorType()) {
         case SENSOR_BLOCK -> this.checkBlockOrFluid(newpos, facing, inputSide, this::checkBlock);
         case SENSOR_FLUID -> this.checkBlockOrFluid(newpos, facing, inputSide, this::checkFluid);
         case SENSOR_GROWTHLEVEL -> this.checkGrowthLevel(newpos, facing, inputSide);
         case SENSOR_ENTITIES -> this.checkEntities(newpos, facing, inputSide, Entity.class);
         case SENSOR_PLAYERS -> this.checkEntities(newpos, facing, inputSide, Player.class);
         case SENSOR_HOSTILE -> this.checkEntitiesHostile(newpos, facing, inputSide);
         case SENSOR_PASSIVE -> this.checkEntitiesPassive(newpos, facing, inputSide);
         case SENSOR_ITEMS -> this.checkEntityItems(newpos, facing, inputSide);
      };
   }

   private boolean checkBlockOrFluid(BlockPos newpos, LogicFacing facing, Direction dir, Function<BlockPos, Boolean> blockChecker) {
      int blockCount = this.getAreaType().getBlockCount();
      if (blockCount > 0) {
         Boolean x = this.checkBlockOrFluidRow(newpos, dir, blockChecker, blockCount);
         if (x != null) {
            return x;
         }
      } else if (blockCount < 0) {
         Direction downSide = facing.getSide();
         Direction inputSide = facing.getInputSide();
         Direction rightSide = LogicSlabBlock.rotateLeft(downSide, inputSide);
         Direction leftSide = LogicSlabBlock.rotateRight(downSide, inputSide);
         blockCount = -blockCount;
         Boolean x = this.checkBlockOrFluidRow(newpos, dir, blockChecker, blockCount);
         if (x != null) {
            return x;
         }

         for (int i = 1; i <= (blockCount - 1) / 2; i++) {
            BlockPos p = newpos.relative(leftSide, i);
            x = this.checkBlockOrFluidRow(p, dir, blockChecker, blockCount);
            if (x != null) {
               return x;
            }

            p = newpos.relative(rightSide, i);
            x = this.checkBlockOrFluidRow(p, dir, blockChecker, blockCount);
            if (x != null) {
               return x;
            }
         }
      }

      return this.getGroupType() == GroupType.GROUP_ALL;
   }

   private Boolean checkBlockOrFluidRow(BlockPos newpos, Direction dir, Function<BlockPos, Boolean> blockChecker, int count) {
      SensorData data = (SensorData)this.getData(LogicBlockModule.SENSOR_DATA);

      for (int i = 0; i < count; i++) {
         boolean result = blockChecker.apply(newpos);
         if (result && data.groupType() == GroupType.GROUP_ONE) {
            return true;
         }

         if (!result && data.groupType() == GroupType.GROUP_ALL) {
            return false;
         }

         newpos = newpos.relative(dir);
      }

      return null;
   }

   private boolean checkBlock(BlockPos newpos) {
      BlockState state = this.level.getBlockState(newpos);
      ItemStack matcher = this.items.getStackInSlot(0);
      if (matcher.isEmpty()) {
         return state.canOcclude();
      } else {
         ItemStack stack = state.getBlock().getCloneItemStack(this.level, newpos, state, false, null);
         return !stack.isEmpty() ? matcher.getItem() == stack.getItem() : matcher.getItem() == state.getBlock().asItem();
      }
   }

   private boolean checkFluid(BlockPos newpos) {
      BlockState state = this.level.getBlockState(newpos);
      ItemStack matcher = this.items.getStackInSlot(0);
      Block block = state.getBlock();
      if (matcher.isEmpty()) {
         return block instanceof LiquidBlock ? !this.level.getBlockState(newpos).isAir() : false;
      } else {
         ItemStack stack = block.getCloneItemStack(this.level, newpos, state, false, null);
         Item matcherItem = matcher.getItem();
         FluidStack matcherFluidStack = null;
         if (matcherItem instanceof BucketItem) {
            matcherFluidStack = new FluidBucketWrapper(matcher).getFluid();
            return this.checkFluid(block, matcherFluidStack, state, newpos);
         } else {
            return false;
         }
      }
   }

   private boolean checkFluid(Block block, FluidStack matcherFluidStack, BlockState state, BlockPos newpos) {
      if (matcherFluidStack == null) {
         return this.level.getBlockState(newpos).isAir();
      } else {
         Fluid matcherFluid = matcherFluidStack.getFluid();
         if (matcherFluid == null) {
            return false;
         } else {
            Block matcherFluidBlock = matcherFluid.defaultFluidState().createLegacyBlock().getBlock();
            return matcherFluidBlock == block;
         }
      }
   }

   private boolean checkGrowthLevel(BlockPos newpos, LogicFacing facing, Direction dir) {
      int blockCount = this.getAreaType().getBlockCount();
      if (blockCount > 0) {
         Boolean x = this.checkGrowthLevelRow(newpos, dir, blockCount);
         if (x != null) {
            return x;
         }
      } else if (blockCount < 0) {
         Direction downSide = facing.getSide();
         Direction inputSide = facing.getInputSide();
         Direction rightSide = LogicSlabBlock.rotateLeft(downSide, inputSide);
         Direction leftSide = LogicSlabBlock.rotateRight(downSide, inputSide);
         blockCount = -blockCount;
         Boolean x = this.checkGrowthLevelRow(newpos, dir, blockCount);
         if (x != null) {
            return x;
         }

         for (int i = 1; i <= (blockCount - 1) / 2; i++) {
            BlockPos p = newpos.relative(leftSide, i);
            x = this.checkGrowthLevelRow(p, dir, blockCount);
            if (x != null) {
               return x;
            }

            p = newpos.relative(rightSide, i);
            x = this.checkGrowthLevelRow(p, dir, blockCount);
            if (x != null) {
               return x;
            }
         }
      }

      return this.getGroupType() == GroupType.GROUP_ALL;
   }

   private Boolean checkGrowthLevelRow(BlockPos newpos, Direction dir, int blockCount) {
      SensorData data = (SensorData)this.getData(LogicBlockModule.SENSOR_DATA);

      for (int i = 0; i < blockCount; i++) {
         boolean result = this.checkGrowthLevel(newpos);
         if (result && data.groupType() == GroupType.GROUP_ONE) {
            return true;
         }

         if (!result && data.groupType() == GroupType.GROUP_ALL) {
            return false;
         }

         newpos = newpos.relative(dir);
      }

      return null;
   }

   private boolean checkGrowthLevel(BlockPos newpos) {
      BlockState state = this.level.getBlockState(newpos);
      int pct = 0;

      for (Property<?> property : state.getProperties()) {
         if ("age".equals(property.getName())) {
            if (property.getValueClass() == Integer.class) {
               int age = (Integer)state.getValue(property);
               int maxAge = Collections.max(property.getPossibleValues());
               pct = age * 100 / maxAge;
            }
            break;
         }
      }

      return pct >= this.getNumber();
   }

   public void invalidateCache() {
      this.cachedBox = null;
   }

   private AABB getCachedBox(BlockPos pos1, LogicFacing facing, Direction dir) {
      if (this.cachedBox == null) {
         int n = this.getAreaType().getBlockCount();
         if (n > 0) {
            this.cachedBox = new AABB(pos1);
            if (n > 1) {
               BlockPos pos2 = pos1.relative(dir, n - 1);
               this.cachedBox = this.cachedBox.minmax(new AABB(pos2));
            }

            this.cachedBox = this.cachedBox.expandTowards(0.1, 0.1, 0.1);
         } else {
            n = -n;
            this.cachedBox = new AABB(pos1);
            Direction downSide = facing.getSide();
            Direction inputSide = facing.getInputSide();
            Direction rightSide = LogicSlabBlock.rotateLeft(downSide, inputSide);
            Direction leftSide = LogicSlabBlock.rotateRight(downSide, inputSide);
            if (n > 1) {
               BlockPos pos2 = pos1.relative(dir, n - 1);
               this.cachedBox = this.cachedBox.minmax(new AABB(pos2));
            }

            BlockPos pos2 = pos1.relative(leftSide, (n - 1) / 2);
            this.cachedBox = this.cachedBox.minmax(new AABB(pos2));
            pos2 = pos1.relative(rightSide, (n - 1) / 2);
            this.cachedBox = this.cachedBox.minmax(new AABB(pos2));
         }
      }

      return this.cachedBox;
   }

   private boolean checkEntityItems(BlockPos pos1, LogicFacing facing, Direction dir) {
      List<? extends Entity> entities = this.level.getEntitiesOfClass(ItemEntity.class, this.getCachedBox(pos1, facing, dir));
      SensorData data = (SensorData)this.getData(LogicBlockModule.SENSOR_DATA);
      int cnt = 0;

      for (Entity entity : entities) {
         if (entity instanceof ItemEntity itemEntity) {
            cnt += itemEntity.getItem().getCount();
            if (cnt >= data.number()) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean checkEntities(BlockPos pos1, LogicFacing facing, Direction dir, Class<? extends Entity> clazz) {
      List<? extends Entity> entities = this.level.getEntitiesOfClass(clazz, this.getCachedBox(pos1, facing, dir));
      return entities.size() >= this.getNumber();
   }

   private boolean checkEntitiesHostile(BlockPos pos1, LogicFacing facing, Direction dir) {
      List<? extends Entity> entities = this.level.getEntitiesOfClass(PathfinderMob.class, this.getCachedBox(pos1, facing, dir));
      SensorData data = (SensorData)this.getData(LogicBlockModule.SENSOR_DATA);
      int cnt = 0;

      for (Entity entity : entities) {
         if (entity instanceof Enemy) {
            if (++cnt >= data.number()) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean checkEntitiesPassive(BlockPos pos1, LogicFacing facing, Direction dir) {
      List<? extends Entity> entities = this.level.getEntitiesOfClass(PathfinderMob.class, this.getCachedBox(pos1, facing, dir));
      SensorData data = (SensorData)this.getData(LogicBlockModule.SENSOR_DATA);
      int cnt = 0;

      for (Entity entity : entities) {
         if (entity instanceof Mob && !(entity instanceof Enemy)) {
            if (++cnt >= data.number()) {
               return true;
            }
         }
      }

      return false;
   }

   public void loadAdditional(ValueInput tag) {
      super.loadAdditional(tag);
      this.support.setPowerOutput(tag.getBooleanOr("rs", false) ? 15 : 0);
      this.items.load(tag, "items");
   }

   public void saveAdditional(ValueOutput tag) {
      super.saveAdditional(tag);
      tag.putBoolean("rs", this.support.getPowerOutput() > 0);
      this.items.save(tag, "items");
   }

   protected void applyImplicitComponents(DataComponentGetter input) {
      super.applyImplicitComponents(input);
      SensorData data = (SensorData)input.get(LogicBlockModule.ITEM_SENSOR_DATA);
      if (data != null) {
         this.setData(LogicBlockModule.SENSOR_DATA, data);
      }

      this.items.applyImplicitComponents((ItemInventory)input.get(Registration.ITEM_INVENTORY));
   }

   protected void collectImplicitComponents(Builder builder) {
      super.collectImplicitComponents(builder);
      builder.set(LogicBlockModule.ITEM_SENSOR_DATA, (SensorData)this.getData(LogicBlockModule.SENSOR_DATA));
      this.items.collectImplicitComponents(builder);
   }

   public void rotateBlock(Rotation axis) {
      this.invalidateCache();
   }
}
