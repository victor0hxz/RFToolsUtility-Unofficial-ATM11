package mcjty.rftoolsutility.modules.teleporter;

import java.util.UUID;
import mcjty.lib.api.infusable.DefaultInfusable;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.varia.DamageTools;
import mcjty.lib.varia.LegacyCapabilities;
import mcjty.lib.varia.LevelTools;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.SoundTools;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsutility.modules.teleporter.blocks.DialingDeviceTileEntity;
import mcjty.rftoolsutility.modules.teleporter.blocks.MatterReceiverTileEntity;
import mcjty.rftoolsutility.modules.teleporter.blocks.MatterTransmitterTileEntity;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestination;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestinations;
import mcjty.rftoolsutility.setup.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class TeleportationTools {
   public static final int STATUS_OK = 0;
   public static final int STATUS_WARN = 1;
   public static final int STATUS_UNKNOWN = 2;
   public static MobEffect confusion;
   public static MobEffect harm;
   public static MobEffect wither;

   public static void getPotions() {
      if (confusion == null) {
         confusion = Tools.getEffect(Identifier.fromNamespaceAndPath("minecraft", "nausea"));
         harm = Tools.getEffect(Identifier.fromNamespaceAndPath("minecraft", "instant_damage"));
         wither = Tools.getEffect(Identifier.fromNamespaceAndPath("minecraft", "wither"));
      }
   }

   public static void applyEffectForSeverity(Player player, int severity, boolean boostNeeded) {
      getPotions();
      switch (severity) {
         case 1:
            if (boostNeeded) {
            }
         case 2:
         default:
            break;
         case 3:
            player.hurt(DamageTools.getGenericDamageSource(player), 0.5F);
            break;
         case 4:
            player.hurt(DamageTools.getGenericDamageSource(player), 0.5F);
            break;
         case 5:
            player.hurt(DamageTools.getGenericDamageSource(player), 1.0F);
            break;
         case 6:
            player.hurt(DamageTools.getGenericDamageSource(player), 1.0F);
            break;
         case 7:
            player.hurt(DamageTools.getGenericDamageSource(player), 2.0F);
            break;
         case 8:
            player.hurt(DamageTools.getGenericDamageSource(player), 2.0F);
            break;
         case 9:
            player.hurt(DamageTools.getGenericDamageSource(player), 3.0F);
            break;
         case 10:
            player.hurt(DamageTools.getGenericDamageSource(player), 3.0F);
      }
   }

   public static int calculateRFCost(Level world, BlockPos c1, TeleportDestination teleportDestination) {
      if (!world.dimension().equals(teleportDestination.getDimension())) {
         return (Integer)TeleportConfiguration.rfStartTeleportBaseDim.get();
      } else {
         BlockPos c2 = teleportDestination.getCoordinate();
         double dist = new Vec3(c1.getX(), c1.getY(), c1.getZ()).distanceTo(new Vec3(c2.getX(), c2.getY(), c2.getZ()));
         int rf = (Integer)TeleportConfiguration.rfStartTeleportBaseLocal.get()
            + (int)(((Integer)TeleportConfiguration.rfStartTeleportDist.get()).intValue() * dist);
         if (rf > (Integer)TeleportConfiguration.rfStartTeleportBaseDim.get()) {
            rf = (Integer)TeleportConfiguration.rfStartTeleportBaseDim.get();
         }

         return rf;
      }
   }

   public static int calculateTime(Level world, BlockPos c1, TeleportDestination teleportDestination) {
      if (!world.dimension().equals(teleportDestination.getDimension())) {
         return (Integer)TeleportConfiguration.timeTeleportBaseDim.get();
      } else {
         BlockPos c2 = teleportDestination.getCoordinate();
         double dist = new Vec3(c1.getX(), c1.getY(), c1.getZ()).distanceTo(new Vec3(c2.getX(), c2.getY(), c2.getZ()));
         int time = (Integer)TeleportConfiguration.timeTeleportBaseLocal.get()
            + (int)(((Integer)TeleportConfiguration.timeTeleportDist.get()).intValue() * dist / 1000.0);
         if (time > (Integer)TeleportConfiguration.timeTeleportBaseDim.get()) {
            time = (Integer)TeleportConfiguration.timeTeleportBaseDim.get();
         }

         return time;
      }
   }

   public static boolean performTeleport(Player player, TeleportDestination dest, int bad, int good, boolean boosted) {
      BlockPos c = dest.getCoordinate();
      BlockPos old = new BlockPos((int)player.getX(), (int)player.getY(), (int)player.getZ());
      ResourceKey<Level> oldId = player.level().dimension();
      if (!allowTeleport(player, oldId, old, dest.getDimension(), dest.getCoordinate())) {
         return false;
      } else {
         if (!oldId.equals(dest.getDimension())) {
            mcjty.lib.varia.TeleportationTools.teleportToDimension(player, dest.getDimension(), c.getX() + 0.5, c.getY() + 1.5, c.getZ() + 0.5);
         } else {
            player.teleportTo(c.getX() + 0.5, c.getY() + 1, c.getZ() + 0.5);
         }

         if ((Boolean)TeleportConfiguration.whooshMessage.get()) {
            Logging.message(player, "Whoosh!");
         }

         boolean boostNeeded = false;
         int severity = consumeReceiverEnergy(player, dest.getCoordinate(), dest.getDimension());
         if (severity > 0 && boosted) {
            boostNeeded = true;
            severity = 1;
         }

         severity = applyBadEffectIfNeeded(player, severity, bad, good, boostNeeded);
         if (severity <= 0 && (Double)TeleportConfiguration.teleportVolume.get() >= 0.01) {
            SoundTools.playSound(
               player.level(), ModSounds.WHOOSH.get(), player.getX(), player.getY(), player.getZ(), (Double)TeleportConfiguration.teleportVolume.get(), 1.0
            );
         }

         if ((Boolean)TeleportConfiguration.logTeleportUsages.get()) {
            Logging.log(
               "Teleport: Player "
                  + player.getName()
                  + " from "
                  + old
                  + " (dim "
                  + oldId
                  + ") to "
                  + dest.getCoordinate()
                  + " (dim "
                  + dest.getDimension()
                  + ") with severity "
                  + severity
            );
         }

         return boostNeeded;
      }
   }

   public static int dial(
      Level worldObj,
      DialingDeviceTileEntity dialingDeviceTileEntity,
      UUID player,
      BlockPos transmitter,
      ResourceKey<Level> transDim,
      BlockPos coordinate,
      ResourceKey<Level> dimension,
      boolean once
   ) {
      Level transWorld = LevelTools.getLevel(transDim);
      if (transWorld == null) {
         return 256;
      } else {
         MatterTransmitterTileEntity transmitterTileEntity = (MatterTransmitterTileEntity)transWorld.getBlockEntity(transmitter);
         if (transmitterTileEntity == null) {
            return 1024;
         } else if (player != null && !transmitterTileEntity.checkAccess(player)) {
            return 32;
         } else if (coordinate == null) {
            transmitterTileEntity.setTeleportDestination(null, false);
            return 128;
         } else {
            TeleportDestination teleportDestination = findDestination(worldObj, coordinate, dimension);
            if (teleportDestination == null) {
               return 4;
            } else {
               BlockPos c = teleportDestination.getCoordinate();
               Level recWorld = LevelTools.getLevel(teleportDestination.getDimension());
               if (recWorld == null) {
                  recWorld = LevelTools.getLevel(worldObj, teleportDestination.getDimension());
                  if (recWorld == null) {
                     return 4;
                  }
               }

               if (recWorld.getBlockEntity(c) instanceof MatterReceiverTileEntity receiver) {
                  TeleportDestination destination = receiver.updateDestination();
                  if (player != null && !destination.checkAccess(recWorld, player)) {
                     return 64;
                  } else if (!checkBeam(transmitter, transWorld, 1, 4, 2)) {
                     return 2;
                  } else {
                     if (dialingDeviceTileEntity != null) {
                        IEnergyStorage h = (IEnergyStorage)dialingDeviceTileEntity.getLevel()
                           .getCapability(LegacyCapabilities.ENERGY_BLOCK, dialingDeviceTileEntity.getBlockPos(), null);
                        if (h == null) {
                           return 8;
                        }

                        int defaultCost = (Integer)TeleportConfiguration.rfPerDial.get();
                        DefaultInfusable inf = dialingDeviceTileEntity.getInfusable();
                        int cost = (int)(defaultCost * (2.0F - inf.getInfusedFactor()) / 2.0F);
                        if (h.getEnergyStored() < cost) {
                           return 8;
                        }

                        ((GenericEnergyStorage)h).consumeEnergy(cost);
                     }

                     transmitterTileEntity.setTeleportDestination(teleportDestination, once);
                     return 0;
                  }
               } else {
                  return 4;
               }
            }
         }
      }
   }

   private static int consumeReceiverEnergy(Player player, BlockPos c, ResourceKey<Level> dimension) {
      Level world = LevelTools.getLevel(player.level(), dimension);
      if (world == null) {
         Logging.warn(player, "Something went wrong with the destination!");
         return 0;
      } else if (world.getBlockEntity(c) instanceof MatterReceiverTileEntity receiver) {
         IEnergyStorage h = (IEnergyStorage)receiver.getLevel().getCapability(LegacyCapabilities.ENERGY_BLOCK, receiver.getBlockPos(), null);
         if (h == null) {
            Logging.warn(player, "The matter receiver has no power!");
            return 0;
         } else {
            int defaultCost = (Integer)TeleportConfiguration.rfPerTeleportReceiver.get();
            DefaultInfusable inf = receiver.getInfusable();
            int rf = (int)(defaultCost * (2.0F - inf.getInfusedFactor()) / 2.0F);
            if (rf <= 0) {
               return 0;
            } else {
               int extracted = Math.min(rf, h.getEnergyStored());
               ((GenericEnergyStorage)h).consumeEnergy(rf);
               long remainingRf = ((GenericEnergyStorage)h).getEnergy();
               if (remainingRf <= 1L) {
                  Logging.warn(player, "The matter receiver has run out of power!");
               } else if (remainingRf < (Integer)TeleportConfiguration.RECEIVER_MAXENERGY.get() / 10) {
                  Logging.warn(player, "The matter receiver is getting very low on power!");
               } else if (remainingRf < (Integer)TeleportConfiguration.RECEIVER_MAXENERGY.get() / 5) {
                  Logging.warn(player, "The matter receiver is getting low on power!");
               }

               return 10 - extracted * 10 / rf;
            }
         }
      } else {
         Logging.warn(player, "Something went wrong with the destination!");
         return 0;
      }
   }

   public static int calculateSeverity(int bad, int total) {
      if (total == 0) {
         total = 1;
      }

      int severity = bad * 10 / total;
      if (mustInterrupt(bad, total)) {
         severity += 2;
      }

      if (severity > 10) {
         severity = 10;
      }

      return severity;
   }

   public static int applyBadEffectIfNeeded(Player player, int severity, int bad, int total, boolean boostNeeded) {
      if (player == null) {
         return 0;
      } else {
         severity += calculateSeverity(bad, total);
         if (severity > 10) {
            severity = 10;
         }

         if (severity <= 0) {
            return 0;
         } else {
            if ((Double)TeleportConfiguration.teleportErrorVolume.get() >= 0.01) {
               SoundTools.playSound(
                  player.level(),
                  ModSounds.ERROR.get(),
                  player.getX(),
                  player.getY(),
                  player.getZ(),
                  (Double)TeleportConfiguration.teleportErrorVolume.get(),
                  1.0
               );
            }

            applyEffectForSeverity(player, severity, boostNeeded);
            return severity;
         }
      }
   }

   public static boolean mustInterrupt(int bad, int total) {
      return bad > total / 2;
   }

   public static boolean allowTeleport(Entity entity, ResourceKey<Level> sourceDim, BlockPos source, ResourceKey<Level> destDim, BlockPos dest) {
      return true;
   }

   public static TeleportDestination findDestination(Level worldObj, BlockPos coordinate, ResourceKey<Level> dimension) {
      TeleportDestinations destinations = TeleportDestinations.get(worldObj);
      return destinations.getDestination(coordinate, dimension);
   }

   public static boolean checkBeam(BlockPos c, Level world, int dy1, int dy2, int errory) {
      for (int dy = dy1; dy <= dy2; dy++) {
         BlockPos pos = new BlockPos(c.getX(), c.getY() + dy, c.getZ());
         if (!world.getBlockState(pos).isAir()) {
            if (dy <= errory) {
               return false;
            }
            break;
         }
      }

      return true;
   }

   public static boolean checkValidTeleport(Player player, ResourceKey<Level> srcId, ResourceKey<Level> dstId) {
      if ((Boolean)TeleportConfiguration.preventInterdimensionalTeleports.get() && srcId.equals(dstId)) {
         Logging.warn(player, "Teleportation in the same dimension is not allowed!");
         return false;
      } else if (TeleportConfiguration.getBlacklistedTeleportationDestinations().contains(dstId)) {
         Logging.warn(player, "Teleportation to that dimension is not allowed!");
         return false;
      } else if (TeleportConfiguration.getBlacklistedTeleportationSources().contains(srcId)) {
         Logging.warn(player, "Teleportation from this dimension is not allowed!");
         return false;
      } else {
         return true;
      }
   }
}
