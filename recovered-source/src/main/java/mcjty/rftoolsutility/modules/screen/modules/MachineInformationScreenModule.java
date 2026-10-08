package mcjty.rftoolsutility.modules.screen.modules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.api.machineinfo.CapabilityMachineInformation;
import mcjty.rftoolsbase.api.machineinfo.IMachineInformation;
import mcjty.rftoolsbase.api.screens.IScreenDataHelper;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.data.IModuleDataString;
import mcjty.rftoolsutility.modules.screen.ScreenConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public record MachineInformationScreenModule(int tag, GlobalPos pos, boolean active, int labcolor, int txtcolor, String monitor)
   implements IScreenModule<MachineInformationScreenModule, IModuleDataString> {
   public static final MachineInformationScreenModule DEFAULT = new MachineInformationScreenModule(
      0, GlobalPos.of(Level.OVERWORLD, BlockPosTools.INVALID), false, 16777215, 16777215, ""
   );
   public static final Codec<MachineInformationScreenModule> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.INT.fieldOf("tag").forGetter(module -> module.tag),
            GlobalPos.CODEC.fieldOf("pos").forGetter(module -> module.pos),
            Codec.INT.fieldOf("labcolor").forGetter(module -> module.labcolor),
            Codec.INT.fieldOf("txtcolor").forGetter(module -> module.txtcolor),
            Codec.STRING.fieldOf("monitor").forGetter(module -> module.monitor)
         )
         .apply(instance, MachineInformationScreenModule::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, MachineInformationScreenModule> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.INT,
      module -> module.tag,
      GlobalPos.STREAM_CODEC,
      module -> module.pos,
      ByteBufCodecs.INT,
      module -> module.labcolor,
      ByteBufCodecs.INT,
      module -> module.txtcolor,
      ByteBufCodecs.STRING_UTF8,
      module -> module.monitor,
      MachineInformationScreenModule::new
   );

   public MachineInformationScreenModule(int tag, GlobalPos pos, int labcolor, int txtcolor, String monitor) {
      this(tag, pos, false, labcolor, txtcolor, monitor);
   }

   public int getTag() {
      return this.tag;
   }

   public int getLabcolor() {
      return this.labcolor;
   }

   public int getTxtcolor() {
      return this.txtcolor;
   }

   public GlobalPos getPos() {
      return this.pos;
   }

   public String getMonitor() {
      return this.monitor;
   }

   public MachineInformationScreenModule withLabcolor(int labcolor) {
      return new MachineInformationScreenModule(this.tag, this.pos, this.active, labcolor, this.txtcolor, this.monitor);
   }

   public MachineInformationScreenModule withTxtcolor(int txtcolor) {
      return new MachineInformationScreenModule(this.tag, this.pos, this.active, this.labcolor, txtcolor, this.monitor);
   }

   public MachineInformationScreenModule withMonitor(String monitor) {
      return new MachineInformationScreenModule(this.tag, this.pos, this.active, this.labcolor, this.txtcolor, monitor);
   }

   public MachineInformationScreenModule withTag(int tag) {
      return new MachineInformationScreenModule(tag, this.pos, this.active, this.labcolor, this.txtcolor, this.monitor);
   }

   public MachineInformationScreenModule withPos(GlobalPos pos) {
      return new MachineInformationScreenModule(this.tag, pos, this.active, this.labcolor, this.txtcolor, this.monitor);
   }

   public MachineInformationScreenModule withActive(boolean active) {
      return new MachineInformationScreenModule(this.tag, this.pos, active, this.labcolor, this.txtcolor, this.monitor);
   }

   public IModuleDataString getData(IScreenDataHelper helper, Level worldObj, long millis) {
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
            if (te == null) {
               return null;
            } else {
               IMachineInformation h = (IMachineInformation)te.getLevel()
                  .getCapability(CapabilityMachineInformation.MACHINE_INFORMATION_CAPABILITY, te.getBlockPos(), null);
               if (h == null) {
                  return null;
               } else {
                  String info;
                  if (this.tag >= 0 && this.tag < h.getTagCount()) {
                     info = h.getData(this.tag, millis);
                  } else {
                     info = "[BAD TAG]";
                  }

                  return helper.createString(info);
               }
            }
         }
      }
   }

   public MachineInformationScreenModule validate(Level world, BlockPos p, boolean isPlus) {
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
      return (Integer)ScreenConfiguration.MACHINEINFO_RFPERTICK.get();
   }

   public ItemStack mouseClick(ItemStack moduleStack, Level world, int x, int y, boolean clicked, Player player) {
      return ItemStack.EMPTY;
   }
}
