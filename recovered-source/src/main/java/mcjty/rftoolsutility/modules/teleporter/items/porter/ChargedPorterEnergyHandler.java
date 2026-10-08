package mcjty.rftoolsutility.modules.teleporter.items.porter;

import mcjty.rftoolsutility.modules.teleporter.TeleporterModule;
import mcjty.rftoolsutility.modules.teleporter.data.ChargedPorterData;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class ChargedPorterEnergyHandler implements EnergyHandler {
   private final ItemAccess access;
   private final ChargedPorterItem item;

   public ChargedPorterEnergyHandler(ItemAccess access, ChargedPorterItem item) {
      this.access = access;
      this.item = item;
   }

   private ChargedPorterData data(ItemResource resource) {
      ChargedPorterData data = (ChargedPorterData)resource.get((DataComponentType)TeleporterModule.ITEM_CHARGEDPORTER_DATA.get());
      return data == null ? ChargedPorterData.createDefault() : data;
   }

   public long getAmountAsLong() {
      int count = this.access.getAmount();
      return count <= 0 ? 0L : (long)count * Math.max(0, this.data(this.access.getResource()).energy());
   }

   public long getCapacityAsLong() {
      int count = this.access.getAmount();
      return count <= 0 ? 0L : (long)count * Math.max(0, this.item.getCapacity().get());
   }

   public int insert(int amount, TransactionContext transaction) {
      TransferPreconditions.checkNonNegative(amount);
      int count = this.access.getAmount();
      if (amount != 0 && count > 0) {
         ItemResource resource = this.access.getResource();
         ChargedPorterData data = this.data(resource);
         int capacity = Math.max(0, this.item.getCapacity().get());
         int current = Math.max(0, Math.min(capacity, data.energy()));
         int perItemRequest = amount / count;
         int perItem = Math.min(Math.max(0, this.item.getMaxReceive().get()), Math.min(perItemRequest, capacity - current));
         if (perItem <= 0) {
            return 0;
         } else {
            ItemResource updated = resource.with((DataComponentType)TeleporterModule.ITEM_CHARGEDPORTER_DATA.get(), data.withEnergy(current + perItem));
            if (updated.isEmpty()) {
               return 0;
            } else {
               int exchanged = this.access.exchange(updated, count, transaction);
               return perItem * exchanged;
            }
         }
      } else {
         return 0;
      }
   }

   public int extract(int amount, TransactionContext transaction) {
      TransferPreconditions.checkNonNegative(amount);
      return 0;
   }
}
