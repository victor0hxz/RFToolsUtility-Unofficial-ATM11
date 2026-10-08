package mcjty.rftoolsutility.playerprops;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import mcjty.rftoolsutility.setup.RFToolsUtilityMessages;
import mcjty.rftoolsutility.setup.Registration;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class BuffProperties {
   public static final int BUFF_MAXTICKS = 180;
   private int buffTimeout;
   private final Map<PlayerBuff, Integer> buffs = new HashMap<>();
   private boolean oldAllowFlying = false;
   private boolean allowFlying = false;
   private boolean globalSyncNeeded = true;
   private boolean onElevator = false;
   public static final Codec<BuffProperties> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.BOOL.fieldOf("onElevator").forGetter(h -> h.onElevator),
            Codec.INT.fieldOf("buffTimeout").forGetter(h -> h.buffTimeout),
            Codec.BOOL.fieldOf("allowFlying").forGetter(h -> h.allowFlying),
            Codec.BOOL.fieldOf("oldAllowFlying").forGetter(h -> h.oldAllowFlying),
            Codec.INT.listOf().fieldOf("buffs").forGetter(BuffProperties::getBuffsAsList),
            Codec.INT.listOf().fieldOf("buffTimeouts").forGetter(BuffProperties::getTimeoutsAsList)
         )
         .apply(instance, BuffProperties::new)
   );

   public BuffProperties(boolean onElevator, int buffTimeout, boolean allowFlying, boolean oldAllowFlying, List<Integer> buffs, List<Integer> timeouts) {
      this.onElevator = onElevator;
      this.buffTimeout = buffTimeout;
      this.allowFlying = allowFlying;
      this.oldAllowFlying = oldAllowFlying;

      for (int i = 0; i < buffs.size(); i++) {
         this.buffs.put(PlayerBuff.values()[buffs.get(i)], timeouts.get(i));
      }
   }

   public BuffProperties() {
      this.buffTimeout = 0;
   }

   private List<Integer> getTimeoutsAsList() {
      return this.buffs.values().stream().toList();
   }

   private List<Integer> getBuffsAsList() {
      return this.buffs.keySet().stream().map(Enum::ordinal).toList();
   }

   private void syncBuffs(ServerPlayer player) {
      RFToolsUtilityMessages.sendToPlayer(PacketSendBuffsToClient.create(this.buffs), player);
   }

   public void tickBuffs(ServerPlayer player) {
      this.buffTimeout--;
      if (this.buffTimeout <= 0) {
         this.buffTimeout = 180;
         Map<PlayerBuff, Integer> copyBuffs = new HashMap<>(this.buffs);
         this.buffs.clear();
         boolean syncNeeded = false;

         for (Entry<PlayerBuff, Integer> entry : copyBuffs.entrySet()) {
            int timeout = entry.getValue();
            timeout -= 180;
            if (timeout > 0) {
               this.buffs.put(entry.getKey(), timeout);
            } else {
               syncNeeded = true;
            }
         }

         if (syncNeeded) {
            this.syncBuffs(player);
            this.performBuffs(player);
            this.globalSyncNeeded = false;
         }
      }

      if (this.globalSyncNeeded) {
         this.globalSyncNeeded = false;
         this.syncBuffs(player);
         this.performBuffs(player);
      }
   }

   private void performBuffs(ServerPlayer player) {
      boolean enableFlight = false;
      if (this.onElevator) {
         enableFlight = true;
         player.getAbilities().flying = true;
      } else {
         for (PlayerBuff buff : this.buffs.keySet()) {
            if (buff == PlayerBuff.BUFF_FLIGHT) {
               enableFlight = true;
               break;
            }
         }
      }

      boolean oldAllow = player.getAbilities().mayfly;
      if (enableFlight) {
         if (!this.allowFlying) {
            this.oldAllowFlying = player.getAbilities().mayfly;
            this.allowFlying = true;
         }

         player.getAbilities().mayfly = true;
      } else if (this.allowFlying) {
         player.getAbilities().mayfly = this.oldAllowFlying;
         if (player.getAbilities().instabuild) {
            player.getAbilities().mayfly = true;
         }

         this.allowFlying = false;
      }

      if (player.getAbilities().mayfly != oldAllow && !player.getAbilities().mayfly) {
         player.getAbilities().flying = false;
      }

      player.onUpdateAbilities();
   }

   public static void enableElevatorMode(Player player) {
      BuffProperties data = (BuffProperties)player.getData(Registration.ATTACHMENT_TYPE_BUFF_PROPERTIES);
      data.onElevator = true;
      data.performBuffs((ServerPlayer)player);
      player.setData(Registration.ATTACHMENT_TYPE_BUFF_PROPERTIES, data);
   }

   public static void disableElevatorMode(Player player) {
      BuffProperties data = (BuffProperties)player.getData(Registration.ATTACHMENT_TYPE_BUFF_PROPERTIES);
      data.onElevator = false;
      player.getAbilities().flying = false;
      data.performBuffs((ServerPlayer)player);
      player.setData(Registration.ATTACHMENT_TYPE_BUFF_PROPERTIES, data);
   }

   public static void addBuffToPlayer(Player player, PlayerBuff buff, int ticks) {
      BuffProperties data = (BuffProperties)player.getData(Registration.ATTACHMENT_TYPE_BUFF_PROPERTIES);
      data.addBuff((ServerPlayer)player, buff, ticks);
   }

   public void addBuff(ServerPlayer player, PlayerBuff buff, int ticks) {
      this.buffs.put(buff, ticks + 5);
      this.syncBuffs(player);
      this.performBuffs(player);
   }

   public Map<PlayerBuff, Integer> getBuffs() {
      return this.buffs;
   }

   public boolean hasBuff(PlayerBuff buff) {
      return this.buffs.containsKey(buff);
   }
}
