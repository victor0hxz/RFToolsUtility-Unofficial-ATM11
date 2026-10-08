package mcjty.rftoolsutility.modules.logic.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import mcjty.lib.worlddata.AbstractWorldData;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;

public class RedstoneChannels extends AbstractWorldData<RedstoneChannels> {
   private static final String REDSTONE_CHANNELS_NAME = "RfToolsRedstoneChannels";
   private int lastId = 0;
   private final Map<Integer, RedstoneChannels.RedstoneChannel> channels = new HashMap<>();

   public static RedstoneChannels getChannels(Level world) {
      return (RedstoneChannels)getData(world, RedstoneChannels::new, RedstoneChannels::new, "RfToolsRedstoneChannels");
   }

   public RedstoneChannels() {
   }

   public RedstoneChannels(CompoundTag tagCompound) {
      ListTag lst = tagCompound.getListOrEmpty("channels");

      for (int i = 0; i < lst.size(); i++) {
         CompoundTag tc = (CompoundTag)lst.getCompound(i).orElseGet(CompoundTag::new);
         int channel = tc.getIntOr("channel", 0);
         int v = tc.getIntOr("value", 0);
         String name = tc.getStringOr("name", "");
         RedstoneChannels.RedstoneChannel value = new RedstoneChannels.RedstoneChannel();
         value.value = v;
         value.setName(name);
         this.channels.put(channel, value);
      }

      this.lastId = tagCompound.getIntOr("lastId", 0);
   }

   public RedstoneChannels.RedstoneChannel getOrCreateChannel(int id) {
      RedstoneChannels.RedstoneChannel channel = this.channels.get(id);
      if (channel == null) {
         channel = new RedstoneChannels.RedstoneChannel();
         this.channels.put(id, channel);
      }

      return channel;
   }

   public RedstoneChannels.RedstoneChannel getChannel(int id) {
      return this.channels.get(id);
   }

   public void deleteChannel(int id) {
      this.channels.remove(id);
   }

   public int newChannel() {
      this.lastId++;
      return this.lastId;
   }

   @Nonnull
   public CompoundTag save(@Nonnull CompoundTag tagCompound, Provider provider) {
      ListTag lst = new ListTag();

      for (Entry<Integer, RedstoneChannels.RedstoneChannel> entry : this.channels.entrySet()) {
         CompoundTag tc = new CompoundTag();
         tc.putInt("channel", entry.getKey());
         tc.putInt("value", entry.getValue().getValue());
         tc.putString("name", entry.getValue().getName());
         lst.add(tc);
      }

      tagCompound.put("channels", lst);
      tagCompound.putInt("lastId", this.lastId);
      return tagCompound;
   }

   public static class RedstoneChannel {
      private int value = 0;
      private String name = "";

      public int getValue() {
         return this.value;
      }

      public void setValue(int value) {
         this.value = value;
      }

      public String getName() {
         return this.name;
      }

      public void setName(String name) {
         this.name = name;
      }
   }
}
