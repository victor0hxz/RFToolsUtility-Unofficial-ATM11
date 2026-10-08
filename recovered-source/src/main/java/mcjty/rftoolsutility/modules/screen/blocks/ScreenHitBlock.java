package mcjty.rftoolsutility.modules.screen.blocks;

import javax.annotation.Nonnull;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolsutility.modules.screen.ScreenModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class ScreenHitBlock extends BaseBlock implements IAttackableBlock {
   public ScreenHitBlock() {
      super(
         new BlockBuilder()
            .properties(Properties.of().strength(-1.0F, 3600000.0F).pushReaction(PushReaction.BLOCK).sound(SoundType.METAL))
            .tileEntitySupplier(ScreenHitTileEntity::new)
      );
   }

   @Nonnull
   public ItemStack getCloneItemStack(@Nonnull LevelReader worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, boolean includeData, Player player) {
      BlockPos screenPos = this.getScreenBlockPos(worldIn, pos);
      if (screenPos == null) {
         return ItemStack.EMPTY;
      } else {
         BlockState screenState = worldIn.getBlockState(screenPos);
         return screenState.getBlock().getCloneItemStack(worldIn, screenPos, screenState, includeData, player);
      }
   }

   public void attack(@Nonnull BlockState s, Level world, @Nonnull BlockPos pos, @Nonnull Player player) {
      this.doAttack(world, pos);
   }

   @Override
   public void doAttack(Level world, @NotNull BlockPos pos) {
      if (world.isClientSide()) {
         ScreenHitTileEntity screenHitTileEntity = (ScreenHitTileEntity)world.getBlockEntity(pos);
         int dx = screenHitTileEntity.getDx();
         int dy = screenHitTileEntity.getDy();
         int dz = screenHitTileEntity.getDz();
         BlockState state = world.getBlockState(pos.offset(dx, dy, dz));
         Block block = state.getBlock();
         if (block != ScreenModule.SCREEN.block().get() && block != ScreenModule.CREATIVE_SCREEN.block().get()) {
            return;
         }

         HitResult mouseOver = SafeClientTools.getClientMouseOver();
         ScreenTileEntity screenTileEntity = (ScreenTileEntity)world.getBlockEntity(pos.offset(dx, dy, dz));
         if (mouseOver instanceof BlockHitResult blockHit) {
            screenTileEntity.hitScreenClient(
               mouseOver.getLocation().x - pos.getX() - dx,
               mouseOver.getLocation().y - pos.getY() - dy,
               mouseOver.getLocation().z - pos.getZ() - dz,
               blockHit.getDirection(),
               (Direction)state.getValue(ScreenBlock.HORIZ_FACING)
            );
         }
      }
   }

   @Nonnull
   public InteractionResult useWithoutItem(
      @Nonnull BlockState state, Level world, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull BlockHitResult result
   ) {
      return this.activate(world, pos, state, player, player.getUsedItemHand(), result);
   }

   public InteractionResult activate(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, BlockHitResult result) {
      pos = this.getScreenBlockPos(world, pos);
      if (pos == null) {
         return InteractionResult.PASS;
      } else {
         Block block = world.getBlockState(pos).getBlock();
         return ((ScreenBlock)block).activate(world, pos, state, player, hand, result);
      }
   }

   public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation rot) {
      return state;
   }

   public BlockPos getScreenBlockPos(BlockGetter world, BlockPos pos) {
      ScreenHitTileEntity screenHitTileEntity = (ScreenHitTileEntity)world.getBlockEntity(pos);
      int dx = screenHitTileEntity.getDx();
      int dy = screenHitTileEntity.getDy();
      int dz = screenHitTileEntity.getDz();
      pos = pos.offset(dx, dy, dz);
      Block block = world.getBlockState(pos).getBlock();
      return block != ScreenModule.SCREEN.block().get() && block != ScreenModule.CREATIVE_SCREEN.block().get() ? null : pos;
   }

   @Nonnull
   public VoxelShape getShape(BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
      Direction facing = (Direction)state.getValue(BlockStateProperties.FACING);
      if (facing == Direction.NORTH) {
         return ScreenBlock.NORTH_AABB;
      } else if (facing == Direction.SOUTH) {
         return ScreenBlock.SOUTH_AABB;
      } else if (facing == Direction.WEST) {
         return ScreenBlock.WEST_AABB;
      } else if (facing == Direction.EAST) {
         return ScreenBlock.EAST_AABB;
      } else if (facing == Direction.UP) {
         return ScreenBlock.UP_AABB;
      } else {
         return facing == Direction.DOWN ? ScreenBlock.DOWN_AABB : ScreenBlock.BLOCK_AABB;
      }
   }

   @Nonnull
   public RenderShape getRenderShape(@Nonnull BlockState state) {
      return RenderShape.MODEL;
   }

   public boolean canEntityDestroy(BlockState state, BlockGetter world, BlockPos pos, Entity entity) {
      return false;
   }

   public void wasExploded(@Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Explosion explosion) {
   }

   protected void createBlockStateDefinition(@Nonnull Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
   }
}
