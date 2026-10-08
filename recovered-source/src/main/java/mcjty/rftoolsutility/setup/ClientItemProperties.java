package mcjty.rftoolsutility.setup;

import com.mojang.serialization.MapCodec;
import mcjty.rftoolsutility.modules.spawner.SpawnerConfiguration;
import mcjty.rftoolsutility.modules.spawner.SpawnerModule;
import mcjty.rftoolsutility.modules.spawner.data.SyringeData;
import mcjty.rftoolsutility.modules.teleporter.TeleporterModule;
import mcjty.rftoolsutility.modules.teleporter.data.ChargedPorterData;
import mcjty.rftoolsutility.modules.teleporter.items.porter.ChargedPorterItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class ClientItemProperties {
   private ClientItemProperties() {
   }

   public record PorterCharge() implements RangeSelectItemModelProperty {
      public static final MapCodec<ClientItemProperties.PorterCharge> MAP_CODEC = MapCodec.unit(new ClientItemProperties.PorterCharge());

      public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
         if (stack.getItem() instanceof ChargedPorterItem porter) {
            ChargedPorterData data = (ChargedPorterData)stack.get(TeleporterModule.ITEM_CHARGEDPORTER_DATA);
            long energy = data == null ? 0L : data.energy();
            long capacity = Math.max(1, porter.getCapacity().get());
            long chargeLevel = 9L * Math.max(0L, energy) / capacity;
            chargeLevel = Math.max(0L, Math.min(8L, chargeLevel));
            return (float)(9L - chargeLevel);
         } else {
            return 9.0F;
         }
      }

      public MapCodec<ClientItemProperties.PorterCharge> type() {
         return MAP_CODEC;
      }
   }

   public record SyringeLevel() implements RangeSelectItemModelProperty {
      public static final MapCodec<ClientItemProperties.SyringeLevel> MAP_CODEC = MapCodec.unit(new ClientItemProperties.SyringeLevel());

      public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
         SyringeData data = (SyringeData)stack.get(SpawnerModule.ITEM_SYRINGE_DATA);
         int raw = data == null ? 0 : Math.max(0, data.level());
         int max = Math.max(1, (Integer)SpawnerConfiguration.maxMobInjections.get());
         return Math.max(0, Math.min(5, raw * 5 / max));
      }

      public MapCodec<ClientItemProperties.SyringeLevel> type() {
         return MAP_CODEC;
      }
   }
}
