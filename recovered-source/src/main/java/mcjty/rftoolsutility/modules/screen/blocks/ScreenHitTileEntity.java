package mcjty.rftoolsutility.modules.screen.blocks;

import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolsutility.modules.screen.ScreenModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ScreenHitTileEntity extends GenericTileEntity {
   private int dx;
   private int dy;
   private int dz;

   public ScreenHitTileEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ScreenModule.SCREEN_HIT.be().get(), pos, state);
   }

   public void setRelativeLocation(int dx, int dy, int dz) {
      this.dx = dx;
      this.dy = dy;
      this.dz = dz;
      this.setChanged();
      BlockState state = this.getLevel().getBlockState(this.getBlockPos());
      this.getLevel().sendBlockUpdated(this.getBlockPos(), state, state, 3);
   }

   public int getDx() {
      return this.dx;
   }

   public int getDy() {
      return this.dy;
   }

   public int getDz() {
      return this.dz;
   }

   public void loadAdditional(ValueInput tag) {
      super.loadAdditional(tag);
      this.dx = tag.getIntOr("dx", 0);
      this.dy = tag.getIntOr("dy", 0);
      this.dz = tag.getIntOr("dz", 0);
   }

   public void saveAdditional(ValueOutput tag) {
      super.saveAdditional(tag);
      tag.putInt("dx", this.dx);
      tag.putInt("dy", this.dy);
      tag.putInt("dz", this.dz);
   }

   public void loadClientDataFromNBT(CompoundTag tag, Provider provider) {
      this.dx = tag.getIntOr("dx", 0);
      this.dy = tag.getIntOr("dy", 0);
      this.dz = tag.getIntOr("dz", 0);
   }

   public void saveClientDataToNBT(CompoundTag tag, Provider provider) {
      tag.putInt("dx", this.dx);
      tag.putInt("dy", this.dy);
      tag.putInt("dz", this.dz);
   }
}
