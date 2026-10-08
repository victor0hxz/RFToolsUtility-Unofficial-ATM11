package mcjty.rftoolsutility.compat;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import mcjty.lib.compat.theoneprobe.McJtyLibTOPDriver;
import mcjty.lib.compat.theoneprobe.TOPDriver;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.ITooltipInfo;
import mcjty.rftoolsutility.modules.environmental.EnvironmentalModule;
import mcjty.rftoolsutility.modules.logic.LogicBlockModule;
import mcjty.rftoolsutility.modules.logic.blocks.RedstoneChannelBlock;
import mcjty.rftoolsutility.modules.logic.blocks.RedstoneReceiverTileEntity;
import mcjty.rftoolsutility.modules.logic.tools.RedstoneChannels;
import mcjty.rftoolsutility.modules.logic.tools.SensorType;
import mcjty.rftoolsutility.modules.screen.blocks.ScreenBlock;
import mcjty.rftoolsutility.modules.spawner.SpawnerModule;
import mcjty.rftoolsutility.modules.teleporter.TeleporterModule;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestinations;
import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class RFToolsUtilityTOPDriver implements TOPDriver {
   public static final RFToolsUtilityTOPDriver DRIVER = new RFToolsUtilityTOPDriver();
   private final Map<Identifier, TOPDriver> drivers = new HashMap<>();

   public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
      Block block = blockState.getBlock();
      Identifier id = Tools.getId(block);
      if (!this.drivers.containsKey(id)) {
         if (block instanceof ScreenBlock) {
            this.drivers.put(id, new RFToolsUtilityTOPDriver.ScreenDriver());
         } else if (block == TeleporterModule.MATTER_RECEIVER.block().get()) {
            this.drivers.put(id, new RFToolsUtilityTOPDriver.MatterReceiverDriver());
         } else if (block == TeleporterModule.MATTER_TRANSMITTER.block().get()) {
            this.drivers.put(id, new RFToolsUtilityTOPDriver.MatterTransmitterDriver());
         } else if (block == TeleporterModule.SIMPLE_DIALER.block().get()) {
            this.drivers.put(id, new RFToolsUtilityTOPDriver.SimpleDialerDriver());
         } else if (block == LogicBlockModule.COUNTER.block().get()) {
            this.drivers.put(id, new RFToolsUtilityTOPDriver.CounterDriver());
         } else if (block == LogicBlockModule.INVCHECKER.block().get()) {
            this.drivers.put(id, new RFToolsUtilityTOPDriver.InvCheckerDriver());
         } else if (block == LogicBlockModule.SENSOR.block().get()) {
            this.drivers.put(id, new RFToolsUtilityTOPDriver.SensorDriver());
         } else if (block == LogicBlockModule.SEQUENCER.block().get()) {
            this.drivers.put(id, new RFToolsUtilityTOPDriver.SequencerDriver());
         } else if (block == LogicBlockModule.TIMER.block().get()) {
            this.drivers.put(id, new RFToolsUtilityTOPDriver.TimerDriver());
         } else if (block == LogicBlockModule.DIGIT.block().get()) {
            this.drivers.put(id, new RFToolsUtilityTOPDriver.DigitDriver());
         } else if (block == SpawnerModule.MATTER_BEAMER.block().get()) {
            this.drivers.put(id, new RFToolsUtilityTOPDriver.MatterBeamerDriver());
         } else if (block == SpawnerModule.SPAWNER.block().get()) {
            this.drivers.put(id, new RFToolsUtilityTOPDriver.SpawnerDriver());
         } else if (block == EnvironmentalModule.ENVIRONENTAL_CONTROLLER.block().get()) {
            this.drivers.put(id, new RFToolsUtilityTOPDriver.EnvironmentalDriver());
         } else if (block instanceof RedstoneChannelBlock) {
            this.drivers.put(id, new RFToolsUtilityTOPDriver.RedstoneChannelDriver());
         } else {
            this.drivers.put(id, new RFToolsUtilityTOPDriver.DefaultDriver());
         }
      }

      TOPDriver driver = this.drivers.get(id);
      if (driver != null) {
         driver.addProbeInfo(mode, probeInfo, player, world, blockState, data);
      }
   }

   public static class CounterDriver extends RFToolsUtilityTOPDriver.DefaultDriver {
      @Override
      public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
         super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
         Tools.safeConsume(world.getBlockEntity(data.getPos()), te -> probeInfo.text(CompoundText.createLabelInfo("Current: ", te.getCurrent())));
      }
   }

   private static class DefaultDriver implements TOPDriver {
      public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
         McJtyLibTOPDriver.DRIVER.addStandardProbeInfo(mode, probeInfo, player, world, blockState, data);
      }
   }

   public static class DigitDriver extends RFToolsUtilityTOPDriver.DefaultDriver {
      @Override
      public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
         super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
         Tools.safeConsume(world.getBlockEntity(data.getPos()), te -> probeInfo.text(CompoundText.createLabelInfo("Power: ", te.getPowerLevel())));
      }
   }

   public static class EnvironmentalDriver extends RFToolsUtilityTOPDriver.DefaultDriver {
      @Override
      public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
         super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
         Tools.safeConsume(world.getBlockEntity(data.getPos()), te -> {
            int rfPerTick = te.getTotalRfPerTick();
            int volume = te.getVolume();
            if (te.isActive()) {
               probeInfo.text(ChatFormatting.GREEN + "Active " + rfPerTick + " RF/tick (#" + volume + ")");
            } else {
               probeInfo.text(ChatFormatting.GREEN + "Inactive (#" + volume + ")");
            }

            int radius = te.getRadius();
            int miny = te.getMiny();
            int maxy = te.getMaxy();
            probeInfo.text(ChatFormatting.GREEN + "Area: radius " + radius + " (" + miny + "/" + maxy + ")");
         });
      }
   }

   public static class InvCheckerDriver extends RFToolsUtilityTOPDriver.DefaultDriver {
      @Override
      public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
         super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
         Tools.safeConsume(world.getBlockEntity(data.getPos()), te -> {
            boolean rc = te.checkOutput();
            probeInfo.text(CompoundText.createLabelInfo("Output: ", rc ? "on" : "off"));
         });
      }
   }

   public static class MatterBeamerDriver extends RFToolsUtilityTOPDriver.DefaultDriver {
      @Override
      public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
         super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
         Tools.safeConsume(world.getBlockEntity(data.getPos()), te -> {
            BlockPos coordinate = te.getDestination();
            if (coordinate == null) {
               probeInfo.text(CompoundText.create().style(TextStyleClass.ERROR).text("Not connected to a spawner!"));
            } else {
               probeInfo.text(CompoundText.create().style(TextStyleClass.INFO).text("Connected!"));
            }

            probeInfo.text(CompoundText.createLabelInfo("Power: ", te.getPowerLevel()));
         });
      }
   }

   public static class MatterReceiverDriver extends RFToolsUtilityTOPDriver.DefaultDriver {
      @Override
      public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
         super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
         Tools.safeConsume(world.getBlockEntity(data.getPos()), te -> {
            String name = te.getName();
            int id = te.getId();
            if (name != null && !name.isEmpty()) {
               probeInfo.text(CompoundText.create().style(TextStyleClass.INFO).text("Name: " + name + (id == -1 ? "" : ", Id: " + id)));
            } else {
               probeInfo.text(CompoundText.create().style(TextStyleClass.INFO).text(id == -1 ? "" : "Id: " + id));
            }
         });
      }
   }

   public static class MatterTransmitterDriver extends RFToolsUtilityTOPDriver.DefaultDriver {
      @Override
      public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
         super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
         Tools.safeConsume(world.getBlockEntity(data.getPos()), te -> {
            probeInfo.text(CompoundText.createLabelInfo("Name: ", te.getName()));
            if (te.isDialed()) {
               Integer teleportId = te.getTeleportId();
               TeleportDestinations destinations = TeleportDestinations.get(world);
               String name = "?";
               if (teleportId != null) {
                  name = TeleportDestinations.getDestinationName(destinations, teleportId);
               }

               probeInfo.text(CompoundText.create().style(TextStyleClass.HIGHLIGHTED).text("[DIALED to " + name + "]"));
            }

            if (te.isOnce()) {
               probeInfo.text(CompoundText.create().style(TextStyleClass.HIGHLIGHTED).text("[ONCE]"));
            }
         });
      }
   }

   public static class RedstoneChannelDriver extends RFToolsUtilityTOPDriver.DefaultDriver {
      @Override
      public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
         super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
         Tools.safeConsume(world.getBlockEntity(data.getPos()), te -> {
            int channel = te.getChannel(false);
            if (channel == -1) {
               probeInfo.text(CompoundText.create().style(TextStyleClass.WARNING).text("No channel set! Right-click with another"));
               probeInfo.text(CompoundText.create().style(TextStyleClass.WARNING).text("transmitter or receiver to pair"));
            } else {
               RedstoneChannels.RedstoneChannel c = RedstoneChannels.getChannels(world).getChannel(channel);
               if (c != null && !c.getName().isEmpty()) {
                  probeInfo.text(CompoundText.createLabelInfo("Channel: ", channel + " (" + c.getName() + ")"));
               } else {
                  probeInfo.text(CompoundText.createLabelInfo("Channel: ", channel));
               }
            }

            if (te instanceof RedstoneReceiverTileEntity) {
               probeInfo.text(CompoundText.createLabelInfo("Analog mode: ", ((RedstoneReceiverTileEntity)te).getAnalog()));
               probeInfo.text(CompoundText.createLabelInfo("Output: ", ((RedstoneReceiverTileEntity)te).checkOutput()));
            }
         });
      }
   }

   public static class ScreenDriver extends RFToolsUtilityTOPDriver.DefaultDriver {
      @Override
      public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
         super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
         Tools.safeConsume(world.getBlockEntity(data.getPos()), te -> {
            if (!te.isConnected() && te.isControllerNeeded()) {
               probeInfo.text(CompoundText.create().style(TextStyleClass.WARNING).text("[NOT CONNECTED]"));
            }

            if (!te.isCreative()) {
               boolean power = te.isPowerOn();
               if (!power) {
                  probeInfo.text(CompoundText.create().style(TextStyleClass.WARNING).text("[NO POWER]"));
               }

               if (mode == ProbeMode.EXTENDED) {
                  int rfPerTick = te.getTotalRfPerTick();
                  probeInfo.text(CompoundText.createLabelInfo(power ? "Consuming " : "Needs ", rfPerTick + " RF/tick"));
               }
            }

            IScreenModule<?, ?> module = te.getHoveringModule();
            if (module instanceof ITooltipInfo) {
               for (String s : ((ITooltipInfo)module).getInfo(world, te.getHoveringX(), te.getHoveringY())) {
                  probeInfo.text(CompoundText.create().text(s));
               }
            }
         });
      }
   }

   public static class SensorDriver extends RFToolsUtilityTOPDriver.DefaultDriver {
      @Override
      public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
         super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
         Tools.safeConsume(world.getBlockEntity(data.getPos()), te -> {
            SensorType sensorType = te.getSensorType();
            if (sensorType.isSupportsNumber()) {
               probeInfo.text(CompoundText.createLabelInfo("Type: ", sensorType.getName() + " (" + te.getNumber() + ")"));
            } else {
               probeInfo.text(CompoundText.createLabelInfo("Type: ", sensorType.getName()));
            }

            int blockCount = te.getAreaType().getBlockCount();
            if (blockCount == 1) {
               probeInfo.text(CompoundText.createLabelInfo("Area: ", "1 block"));
            } else if (blockCount < 0) {
               probeInfo.text(CompoundText.createLabelInfo("Area: ", -blockCount + "x" + -blockCount + " blocks"));
            } else {
               probeInfo.text(CompoundText.createLabelInfo("Area: ", blockCount + " blocks"));
            }

            boolean rc = te.checkSensor();
            probeInfo.text(CompoundText.createLabelInfo("Output: ", rc ? "on" : "off"));
         });
      }
   }

   public static class SequencerDriver extends RFToolsUtilityTOPDriver.DefaultDriver {
      @Override
      public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
         super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
         Tools.safeConsume(
            world.getBlockEntity(data.getPos()),
            te -> {
               IProbeInfo horizontal = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
               horizontal.text(CompoundText.createLabelInfo("Mode: ", te.getMode().getName()));
               TheOneProbeSupport.addSequenceElement(horizontal, te.getCycleBits(), te.getCurrentStep(), mode == ProbeMode.EXTENDED);
               int currentStep = te.getCurrentStep();
               boolean rc = te.checkOutput();
               probeInfo.text(
                  CompoundText.create()
                     .style(TextStyleClass.LABEL)
                     .text("Step: ")
                     .style(TextStyleClass.INFO)
                     .text(String.valueOf(currentStep))
                     .style(TextStyleClass.LABEL)
                     .text(" -> ")
                     .style(TextStyleClass.INFO)
                     .text(rc ? "on" : "off")
               );
            }
         );
      }
   }

   public static class SimpleDialerDriver extends RFToolsUtilityTOPDriver.DefaultDriver {
      @Override
      public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
         super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
         Tools.safeConsume(
            world.getBlockEntity(data.getPos()),
            te -> {
               GlobalPos trans = te.getTransmitter();
               if (BlockPosTools.isValid(trans.pos())) {
                  probeInfo.text(
                     CompoundText.createLabelInfo(
                        "Transmitter at: ", BlockPosTools.toString(trans.pos()) + " (dim " + trans.dimension().identifier().toString() + ")"
                     )
                  );
               }

               int receiver = te.getReceiver();
               if (receiver != -1) {
                  probeInfo.text(CompoundText.createLabelInfo("Receiver: ", receiver));
               }

               if (te.isOnceMode()) {
                  probeInfo.text(CompoundText.create().style(TextStyleClass.INFO).text("Dial Once mode enabled"));
               }
            }
         );
      }
   }

   public static class SpawnerDriver extends RFToolsUtilityTOPDriver.DefaultDriver {
      @Override
      public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
         super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
         Tools.safeConsume(world.getBlockEntity(data.getPos()), te -> {
            DecimalFormat fmt = new DecimalFormat("#.##");
            fmt.setRoundingMode(RoundingMode.DOWN);
            probeInfo.text(CompoundText.createLabelInfo("Key Matter: ", fmt.format(te.getMatter(0))));
            probeInfo.text(CompoundText.createLabelInfo("Bulk Matter: ", fmt.format(te.getMatter(1))));
            probeInfo.text(CompoundText.createLabelInfo("Living Matter: ", fmt.format(te.getMatter(2))));
         });
      }
   }

   public static class TimerDriver extends RFToolsUtilityTOPDriver.DefaultDriver {
      @Override
      public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
         super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
         Tools.safeConsume(world.getBlockEntity(data.getPos()), te -> probeInfo.text(CompoundText.createLabelInfo("Time: ", te.getTimer())));
      }
   }
}
