package mcjty.rftoolsutility.modules.screen.blocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolsbase.api.screens.IModuleProvider;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsutility.compat.RFToolsUtilityTOPDriver;
import mcjty.rftoolsutility.modules.screen.ScreenModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class ScreenBlock extends BaseBlock implements IAttackableBlock {
   public static final EnumProperty<Direction> HORIZ_FACING = EnumProperty.create("horizfacing", Direction.class, Plane.HORIZONTAL);
   private final boolean creative;
   private static final ScreenBlock.Setup[] transitions = new ScreenBlock.Setup[]{
      new ScreenBlock.Setup(0, false),
      new ScreenBlock.Setup(0, true),
      new ScreenBlock.Setup(1, false),
      new ScreenBlock.Setup(1, true),
      new ScreenBlock.Setup(2, false),
      new ScreenBlock.Setup(2, true)
   };
   public static final VoxelShape BLOCK_AABB = Shapes.box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
   public static final VoxelShape NORTH_AABB = Shapes.box(0.01F, 0.01F, 0.9375, 0.99F, 0.99F, 1.0);
   public static final VoxelShape SOUTH_AABB = Shapes.box(0.01F, 0.01F, 0.0, 0.99F, 0.99F, 0.0625);
   public static final VoxelShape WEST_AABB = Shapes.box(0.9375, 0.01F, 0.01F, 1.0, 0.99F, 0.99F);
   public static final VoxelShape EAST_AABB = Shapes.box(0.0, 0.01F, 0.01F, 0.0625, 0.99F, 0.99F);
   public static final VoxelShape UP_AABB = Shapes.box(0.01F, 0.0, 0.01F, 1.0, 0.061875F, 0.99F);
   public static final VoxelShape DOWN_AABB = Shapes.box(0.01F, 0.9375, 0.01F, 0.99F, 1.0, 0.99F);

   public ScreenBlock(BlockEntitySupplier<BlockEntity> supplier, boolean creative) {
      super(
         new BlockBuilder()
            .topDriver(RFToolsUtilityTOPDriver.DRIVER)
            .manualEntry(ManualHelper.create("rftoolsbase:machines/screen"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsutility.shiftmessage")})
            .infoShift(new InfoLine[]{TooltipBuilder.header()})
            .tileEntitySupplier(supplier)
      );
      this.creative = creative;
   }

   public boolean isCreative() {
      return this.creative;
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)super.getStateForPlacement(context).setValue(HORIZ_FACING, context.getPlayer().getDirection().getOpposite());
   }

   public static boolean hasModuleProvider(ItemStack stack) {
      return stack.getItem() instanceof IModuleProvider;
   }

   public static IModuleProvider getModuleProvider(ItemStack stack) {
      return stack.getItem() instanceof IModuleProvider moduleProvider ? moduleProvider : null;
   }

   public InteractionResult activate(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, BlockHitResult result) {
      return this.useWithoutItem(state, world, pos, player, result);
   }

   public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation rot) {
      return state;
   }

   public void attack(@Nonnull BlockState state, Level world, @Nonnull BlockPos pos, @Nonnull Player player) {
      this.doAttack(world, pos);
   }

   @Override
   public void doAttack(Level world, @NotNull BlockPos pos) {
      if (world.isClientSide()) {
         HitResult mouseOver = SafeClientTools.getClientMouseOver();
         ScreenTileEntity screenTileEntity = (ScreenTileEntity)world.getBlockEntity(pos);
         if (mouseOver instanceof BlockHitResult blockResult) {
            screenTileEntity.hitScreenClient(
               mouseOver.getLocation().x - pos.getX(),
               mouseOver.getLocation().y - pos.getY(),
               mouseOver.getLocation().z - pos.getZ(),
               blockResult.getDirection(),
               (Direction)world.getBlockState(pos).getValue(HORIZ_FACING)
            );
         }
      }
   }

   private void setInvisibleBlockSafe(Level world, BlockPos pos, int dx, int dy, int dz, Direction facing) {
      int yy = pos.getY() + dy;
      if (yy >= world.getMinY() && yy < world.getMaxY()) {
         int xx = pos.getX() + dx;
         int zz = pos.getZ() + dz;
         BlockPos posO = new BlockPos(xx, yy, zz);
         if (world.isEmptyBlock(posO)) {
            world.setBlock(
               posO, (BlockState)((ScreenHitBlock)ScreenModule.SCREEN_HIT.block().get()).defaultBlockState().setValue(BlockStateProperties.FACING, facing), 3
            );
            ScreenHitTileEntity screenHitTileEntity = (ScreenHitTileEntity)world.getBlockEntity(posO);
            screenHitTileEntity.setRelativeLocation(-dx, -dy, -dz);
         }
      }
   }

   private void setInvisibleBlocks(Level world, BlockPos pos, int size) {
      BlockState state = world.getBlockState(pos);
      Direction facing = (Direction)state.getValue(BlockStateProperties.FACING);
      Direction horizontalFacing = (Direction)state.getValue(HORIZ_FACING);

      for (int i = 0; i <= size; i++) {
         for (int j = 0; j <= size; j++) {
            if (i != 0 || j != 0) {
               if (facing == Direction.NORTH) {
                  this.setInvisibleBlockSafe(world, pos, -i, -j, 0, facing);
               } else if (facing == Direction.SOUTH) {
                  this.setInvisibleBlockSafe(world, pos, i, -j, 0, facing);
               } else if (facing == Direction.WEST) {
                  this.setInvisibleBlockSafe(world, pos, 0, -i, j, facing);
               } else if (facing == Direction.EAST) {
                  this.setInvisibleBlockSafe(world, pos, 0, -i, -j, facing);
               } else if (facing == Direction.UP) {
                  switch (horizontalFacing) {
                     case NORTH:
                        this.setInvisibleBlockSafe(world, pos, -i, 0, -j, facing);
                        break;
                     case SOUTH:
                        this.setInvisibleBlockSafe(world, pos, i, 0, j, facing);
                        break;
                     case WEST:
                        this.setInvisibleBlockSafe(world, pos, -i, 0, j, facing);
                        break;
                     case EAST:
                        this.setInvisibleBlockSafe(world, pos, i, 0, -j, facing);
                  }
               } else if (facing == Direction.DOWN) {
                  switch (horizontalFacing) {
                     case NORTH:
                        this.setInvisibleBlockSafe(world, pos, -i, 0, j, facing);
                        break;
                     case SOUTH:
                        this.setInvisibleBlockSafe(world, pos, i, 0, -j, facing);
                        break;
                     case WEST:
                        this.setInvisibleBlockSafe(world, pos, i, 0, j, facing);
                        break;
                     case EAST:
                        this.setInvisibleBlockSafe(world, pos, -i, 0, -j, facing);
                  }
               }
            }
         }
      }
   }

   private void clearInvisibleBlockSafe(Level world, BlockPos pos) {
      if (pos.getY() >= world.getMinY() && pos.getY() < world.getMaxY()) {
         if (world.getBlockState(pos).getBlock() == ScreenModule.SCREEN_HIT.block().get()) {
            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
         }
      }
   }

   private void clearInvisibleBlocks(Level world, BlockPos pos, BlockState state, int size) {
      Direction facing = (Direction)state.getValue(BlockStateProperties.FACING);
      Direction horizontalFacing = (Direction)state.getValue(HORIZ_FACING);

      for (int i = 0; i <= size; i++) {
         for (int j = 0; j <= size; j++) {
            if (i != 0 || j != 0) {
               if (facing == Direction.NORTH) {
                  this.clearInvisibleBlockSafe(world, pos.offset(-i, -j, 0));
               } else if (facing == Direction.SOUTH) {
                  this.clearInvisibleBlockSafe(world, pos.offset(i, -j, 0));
               } else if (facing == Direction.WEST) {
                  this.clearInvisibleBlockSafe(world, pos.offset(0, -i, j));
               } else if (facing == Direction.EAST) {
                  this.clearInvisibleBlockSafe(world, pos.offset(0, -i, -j));
               } else if (facing == Direction.UP) {
                  switch (horizontalFacing) {
                     case NORTH:
                        this.clearInvisibleBlockSafe(world, pos.offset(-i, 0, -j));
                        break;
                     case SOUTH:
                        this.clearInvisibleBlockSafe(world, pos.offset(i, 0, j));
                        break;
                     case WEST:
                        this.clearInvisibleBlockSafe(world, pos.offset(-i, 0, j));
                        break;
                     case EAST:
                        this.clearInvisibleBlockSafe(world, pos.offset(i, 0, -j));
                  }
               } else if (facing == Direction.DOWN) {
                  switch (horizontalFacing) {
                     case NORTH:
                        this.clearInvisibleBlockSafe(world, pos.offset(-i, 0, j));
                        break;
                     case SOUTH:
                        this.clearInvisibleBlockSafe(world, pos.offset(i, 0, -j));
                        break;
                     case WEST:
                        this.clearInvisibleBlockSafe(world, pos.offset(i, 0, j));
                        break;
                     case EAST:
                        this.clearInvisibleBlockSafe(world, pos.offset(-i, 0, -j));
                  }
               }
            }
         }
      }
   }

   protected boolean wrenchUse(Level world, BlockPos pos, Direction side, Player player) {
      this.cycleSizeTranspMode(world, pos);
      return true;
   }

   protected void createBlockStateDefinition(@Nonnull Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{HORIZ_FACING});
   }

   public void cycleSizeTranspMode(Level world, BlockPos pos) {
      ScreenTileEntity screenTileEntity = (ScreenTileEntity)world.getBlockEntity(pos);
      BlockState state = world.getBlockState(pos);
      this.clearInvisibleBlocks(world, pos, state, screenTileEntity.getSize());

      for (int i = 0; i < transitions.length; i++) {
         ScreenBlock.Setup setup = transitions[i];
         if (setup.isTransparent() == screenTileEntity.isTransparent() && setup.getSize() == screenTileEntity.getSize()) {
            ScreenBlock.Setup next = transitions[(i + 1) % transitions.length];
            screenTileEntity.setTransparent(next.isTransparent());
            screenTileEntity.setSize(next.getSize());
            this.setInvisibleBlocks(world, pos, screenTileEntity.getSize());
            break;
         }
      }
   }

   public void cycleSizeMode(Level world, BlockPos pos) {
      ScreenTileEntity screenTileEntity = (ScreenTileEntity)world.getBlockEntity(pos);
      BlockState state = world.getBlockState(pos);
      this.clearInvisibleBlocks(world, pos, state, screenTileEntity.getSize());

      for (int i = 0; i < transitions.length; i++) {
         ScreenBlock.Setup setup = transitions[i];
         if (setup.isTransparent() == screenTileEntity.isTransparent() && setup.getSize() == screenTileEntity.getSize()) {
            ScreenBlock.Setup next = transitions[(i + 2) % transitions.length];
            screenTileEntity.setTransparent(next.isTransparent());
            screenTileEntity.setSize(next.getSize());
            this.setInvisibleBlocks(world, pos, screenTileEntity.getSize());
            break;
         }
      }
   }

   public void cycleTranspMode(Level world, BlockPos pos) {
      ScreenTileEntity screenTileEntity = (ScreenTileEntity)world.getBlockEntity(pos);
      BlockState state = world.getBlockState(pos);
      this.clearInvisibleBlocks(world, pos, state, screenTileEntity.getSize());

      for (int i = 0; i < transitions.length; i++) {
         ScreenBlock.Setup setup = transitions[i];
         if (setup.isTransparent() == screenTileEntity.isTransparent() && setup.getSize() == screenTileEntity.getSize()) {
            ScreenBlock.Setup next = transitions[i % 2 == 0 ? i + 1 : i - 1];
            screenTileEntity.setTransparent(next.isTransparent());
            screenTileEntity.setSize(next.getSize());
            this.setInvisibleBlocks(world, pos, screenTileEntity.getSize());
            break;
         }
      }
   }

   protected boolean openGui(Level world, int x, int y, int z, Player player) {
      ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
      if (!itemStack.isEmpty() && itemStack.getItem() == Items.BLACK_DYE) {
         int damage = itemStack.getDamageValue();
         if (damage < 0) {
            damage = 0;
         } else if (damage > 15) {
            damage = 15;
         }

         DyeColor color = DyeColor.byId(damage);
         ScreenTileEntity screenTileEntity = (ScreenTileEntity)world.getBlockEntity(new BlockPos(x, y, z));
         screenTileEntity.setColor(color.getMapColor().col);
         return true;
      } else if (player.isShiftKeyDown()) {
         return super.openGui(world, x, y, z, player);
      } else {
         if (world.isClientSide()) {
            this.activateOnClient(world, new BlockPos(x, y, z));
         }

         return true;
      }
   }

   private void activateOnClient(Level world, BlockPos pos) {
      HitResult mouseOver = SafeClientTools.getClientMouseOver();
      ScreenTileEntity screenTileEntity = (ScreenTileEntity)world.getBlockEntity(pos);
      if (mouseOver instanceof BlockHitResult hitResult) {
         screenTileEntity.hitScreenClient(
            mouseOver.getLocation().x - pos.getX(),
            mouseOver.getLocation().y - pos.getY(),
            mouseOver.getLocation().z - pos.getZ(),
            hitResult.getDirection(),
            (Direction)world.getBlockState(pos).getValue(HORIZ_FACING)
         );
      }
   }

   @Nonnull
   public VoxelShape getShape(BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
      return switch ((Direction)state.getValue(BlockStateProperties.FACING)) {
         case NORTH -> NORTH_AABB;
         case SOUTH -> SOUTH_AABB;
         case WEST -> WEST_AABB;
         case EAST -> EAST_AABB;
         case UP -> UP_AABB;
         case DOWN -> DOWN_AABB;
         default -> throw new MatchException(null, null);
      };
   }

   @Nonnull
   public RenderShape getRenderShape(@Nonnull BlockState state) {
      return RenderShape.MODEL;
   }

   public void setPlacedBy(@Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState state, LivingEntity entityLivingBase, @Nonnull ItemStack itemStack) {
      super.setPlacedBy(world, pos, state, entityLivingBase, itemStack);
      if (entityLivingBase instanceof Player) {
      }

      if (world.getBlockEntity(pos) instanceof ScreenTileEntity screen && screen.getSize() > 0) {
         this.setInvisibleBlocks(world, pos, screen.getSize());
      }
   }

   public void onRemove(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState newstate, boolean isMoving) {
      if (world.getBlockEntity(pos) instanceof ScreenTileEntity screen && screen.getSize() > 0) {
         this.clearInvisibleBlocks(world, pos, state, screen.getSize());
      }

      super.onRemove(state, world, pos, newstate, isMoving);
   }

   private static class Setup {
      private final boolean transparent;
      private final int size;

      public Setup(int size, boolean transparent) {
         this.size = size;
         this.transparent = transparent;
      }

      public int getSize() {
         return this.size;
      }

      public boolean isTransparent() {
         return this.transparent;
      }
   }
}
