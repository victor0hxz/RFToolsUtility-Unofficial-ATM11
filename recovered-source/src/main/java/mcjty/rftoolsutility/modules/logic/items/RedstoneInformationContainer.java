package mcjty.rftoolsutility.modules.logic.items;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.rftoolsbase.modules.tablet.items.TabletItem;
import mcjty.rftoolsutility.modules.logic.LogicBlockModule;
import mcjty.rftoolsutility.modules.logic.network.PacketSendRedstoneData;
import mcjty.rftoolsutility.modules.logic.tools.RedstoneChannels;
import mcjty.rftoolsutility.setup.RFToolsUtilityMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;

public class RedstoneInformationContainer extends GenericContainer {
   public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(0));
   private final Player player;
   private final Level world;
   private Map<Integer, Pair<String, Integer>> values = null;

   public void sendData(Map<Integer, Pair<String, Integer>> channelData) {
      this.values = channelData;
   }

   public Map<Integer, Pair<String, Integer>> getChannelData() {
      return this.values;
   }

   public static ItemStack getRedstoneInformationItem(Player player) {
      ItemStack tabletItem = player.getItemInHand(TabletItem.getHand(player));
      return tabletItem.getItem() instanceof RedstoneInformationItem
         ? tabletItem
         : TabletItem.getContainingItem(tabletItem, TabletItem.getCurrentSlot(tabletItem));
   }

   public RedstoneInformationContainer(int id, BlockPos pos, Player player) {
      super(LogicBlockModule.CONTAINER_REDSTONE_INFORMATION.get(), id, (ContainerFactory)CONTAINER_FACTORY.get(), pos, null, player);
      this.player = player;
      this.world = player.level();
   }

   public void setupInventories(IItemHandler itemHandler, Inventory inventory) {
   }

   public void broadcastChanges() {
      super.broadcastChanges();
      boolean dirty = false;
      RedstoneChannels redstoneChannels = RedstoneChannels.getChannels(this.world);
      ItemStack infoItem = getRedstoneInformationItem(this.player);
      Set<Integer> channels = RedstoneInformationItem.getChannels(infoItem);
      if (this.values != null && this.values.keySet().equals(new HashSet<>(channels))) {
         for (Integer channel : channels) {
            RedstoneChannels.RedstoneChannel c = redstoneChannels.getChannel(channel);
            if (c != null) {
               Pair<String, Integer> current = this.values.get(channel);
               if (current == null || (Integer)current.getRight() != c.getValue() || !((String)current.getLeft()).equals(c.getName())) {
                  this.values.put(channel, Pair.of(c.getName(), c.getValue()));
                  dirty = true;
               }
            }
         }
      } else {
         this.values = new HashMap<>();

         for (Integer channelx : channels) {
            RedstoneChannels.RedstoneChannel c = redstoneChannels.getChannel(channelx);
            if (c != null) {
               this.values.put(channelx, Pair.of(c.getName(), c.getValue()));
            }
         }

         dirty = true;
      }

      if (dirty) {
         PacketSendRedstoneData message = PacketSendRedstoneData.create(this.values);
         if (this.player instanceof ServerPlayer serverPlayer) {
            RFToolsUtilityMessages.sendToPlayer(message, serverPlayer);
         }
      }
   }
}
