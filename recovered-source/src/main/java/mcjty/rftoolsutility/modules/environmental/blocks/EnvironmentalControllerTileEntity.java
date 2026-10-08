package mcjty.rftoolsutility.modules.environmental.blocks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.container.ItemInventory;
import mcjty.lib.api.information.IPowerInformation;
import mcjty.lib.api.infusable.DefaultInfusable;
import mcjty.lib.api.infusable.IInfusable;
import mcjty.lib.api.infusable.ItemInfusable;
import mcjty.lib.api.module.DefaultModuleSupport;
import mcjty.lib.api.module.IModuleSupport;
import mcjty.lib.api.power.ItemEnergy;
import mcjty.lib.bindings.GuiValue;
import mcjty.lib.bindings.Value;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ListCommand;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.lib.setup.Registration;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsutility.compat.RFToolsUtilityTOPDriver;
import mcjty.rftoolsutility.modules.environmental.EnvModuleProvider;
import mcjty.rftoolsutility.modules.environmental.EnvironmentalConfiguration;
import mcjty.rftoolsutility.modules.environmental.EnvironmentalModule;
import mcjty.rftoolsutility.modules.environmental.data.EnvironmentalData;
import mcjty.rftoolsutility.modules.environmental.modules.EnvironmentModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.Lazy;

public class EnvironmentalControllerTileEntity extends TickingTileEntity {
   public static final String COMPONENT_NAME = "environmental_controller";
   public static final int ENV_MODULES = 7;
   public static final int SLOT_MODULES = 0;
   public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(
      () -> new ContainerFactory(7).box(SlotDefinition.specific(s -> s.getItem() instanceof EnvModuleProvider).in().out(), 0, 7, 8, 1, 7).playerSlots(27, 142)
   );
   private final GenericItemHandler items = GenericItemHandler.create(this, CONTAINER_FACTORY)
      .itemValid((slot, stack) -> stack.getItem() instanceof EnvModuleProvider)
      .onUpdate((slot, stack) -> this.environmentModules = null)
      .build();
   @Cap(type = CapType.ITEMS_AUTOMATION)
   private static final Function<EnvironmentalControllerTileEntity, GenericItemHandler> ITEM_CAP = tile -> tile.items;
   private final GenericEnergyStorage energyStorage = new GenericEnergyStorage(
      this,
      true,
      ((Integer)EnvironmentalConfiguration.ENVIRONMENTAL_MAXENERGY.get()).intValue(),
      ((Integer)EnvironmentalConfiguration.ENVIRONMENTAL_RECEIVEPERTICK.get()).intValue()
   );
   @Cap(type = CapType.ENERGY)
   private static final Function<EnvironmentalControllerTileEntity, GenericEnergyStorage> ENERGY_CAP = tile -> tile.energyStorage;
   private final DefaultInfusable infusable = new DefaultInfusable(this);
   @Cap(type = CapType.INFUSABLE)
   private static final Function<EnvironmentalControllerTileEntity, IInfusable> INFUSABLE_CAP = tile -> tile.infusable;
   private final IPowerInformation powerInfoHandler = this.createPowerInfo();
   @Cap(type = CapType.POWER_INFO)
   private static final Function<EnvironmentalControllerTileEntity, IPowerInformation> POWER_INFO_CAP = tile -> tile.powerInfoHandler;
   @Cap(type = CapType.MODULE)
   private static final Function<EnvironmentalControllerTileEntity, IModuleSupport> MODULE_CAP = tile -> new DefaultModuleSupport(0, 6) {
      public boolean isModule(ItemStack itemStack) {
         return itemStack.getItem() instanceof EnvModuleProvider;
      }
   };
   @Cap(type = CapType.CONTAINER)
   private static final Function<EnvironmentalControllerTileEntity, MenuProvider> SCREEN_CAP = tile -> new DefaultContainerProvider("Environmental Controller")
      .containerSupplier(DefaultContainerProvider.container(EnvironmentalModule.CONTAINER_ENVIRONENTAL_CONTROLLER, CONTAINER_FACTORY, tile))
      .itemHandler(() -> tile.items)
      .energyHandler(() -> tile.energyStorage)
      .setupSync(tile);
   private List<EnvironmentModule> environmentModules = null;
   private int totalRfPerTick = 0;
   @GuiValue
   public static final Value<?, ?> VALUE_MODE = Value.createEnum(
      "mode", EnvironmentalMode.values(), EnvironmentalControllerTileEntity::getMode, EnvironmentalControllerTileEntity::setMode
   );
   @GuiValue
   public static final Value<?, ?> VALUE_RADIUS = Value.create(
      "radius", Type.INTEGER, EnvironmentalControllerTileEntity::getRadius, EnvironmentalControllerTileEntity::setRadius
   );
   @GuiValue
   public static final Value<?, ?> VALUE_MINY = Value.create(
      "miny", Type.INTEGER, EnvironmentalControllerTileEntity::getMiny, EnvironmentalControllerTileEntity::setMiny
   );
   @GuiValue
   public static final Value<?, ?> VALUE_MAXY = Value.create(
      "maxy", Type.INTEGER, EnvironmentalControllerTileEntity::getMaxy, EnvironmentalControllerTileEntity::setMaxy
   );
   private int volume = -1;
   private boolean active = false;
   private int powerTimeout = 0;
   @ServerCommand
   public static final Command<?> CMD_RSMODE = Command.create(
      "env.setRsMode", (te, player, params) -> te.setRSMode(RedstoneMode.values()[params.get(ImageChoiceLabel.PARAM_CHOICE_IDX)])
   );
   public static final Key<Integer> PARAM_MIN = new Key("min", Type.INTEGER);
   public static final Key<Integer> PARAM_MAX = new Key("max", Type.INTEGER);
   @ServerCommand
   public static final Command<?> CMD_SETBOUNDS = Command.create("env.setBounds", (te, player, params) -> {
      te.setMiny((Integer)params.get(PARAM_MIN));
      te.setMaxy((Integer)params.get(PARAM_MAX));
   });
   public static final Key<Integer> PARAM_MODE = new Key("mode", Type.INTEGER);
   @ServerCommand
   public static final Command<?> CMD_SETMODE = Command.create(
      "env.setBlacklist", (te, player, params) -> te.setMode(EnvironmentalMode.values()[params.get(PARAM_MODE)])
   );
   public static final Key<String> PARAM_NAME = new Key("name", Type.STRING);
   @ServerCommand
   public static final Command<?> CMD_ADDPLAYER = Command.create("env.addPlayer", (te, player, params) -> te.addPlayer((String)params.get(PARAM_NAME)));
   @ServerCommand
   public static final Command<?> CMD_DELPLAYER = Command.create("env.delPlayer", (te, player, params) -> te.delPlayer((String)params.get(PARAM_NAME)));
   @ServerCommand(type = String.class)
   public static final ListCommand<?, ?> CMD_GETPLAYERS = ListCommand.create(
      "rftoolsutility.env.getPlayers", (te, player, params) -> te.getPlayersAsList(), (te, player, params, list) -> {
         EnvironmentalData data = (EnvironmentalData)te.getData(EnvironmentalModule.ENVIRONMENTAL_DATA);
         data = data.withPlayers(new HashSet<>(list));
         te.setData(EnvironmentalModule.ENVIRONMENTAL_DATA, data);
      }
   );

   public EnvironmentalControllerTileEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)EnvironmentalModule.ENVIRONENTAL_CONTROLLER.be().get(), pos, state);
   }

   public static BaseBlock createBlock() {
      return new BaseBlock(
         new BlockBuilder()
            .properties(Properties.of().strength(2.0F).sound(SoundType.METAL).lightLevel(value -> 13))
            .tileEntitySupplier(EnvironmentalControllerTileEntity::new)
            .topDriver(RFToolsUtilityTOPDriver.DRIVER)
            .infusable()
            .manualEntry(ManualHelper.create("rftoolsbase:machines/environmental"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsutility.shiftmessage")})
            .infoShift(new InfoLine[]{TooltipBuilder.header(), TooltipBuilder.gold()})
      ) {
         public RotationType getRotationType() {
            return RotationType.NONE;
         }
      };
   }

   protected boolean needsRedstoneMode() {
      return true;
   }

   public EnvironmentalMode getMode() {
      EnvironmentalData data = (EnvironmentalData)this.getData(EnvironmentalModule.ENVIRONMENTAL_DATA);
      return data.mode();
   }

   public void setMode(EnvironmentalMode mode) {
      EnvironmentalData data = (EnvironmentalData)this.getData(EnvironmentalModule.ENVIRONMENTAL_DATA);
      data = data.withMode(mode);
      this.setData(EnvironmentalModule.ENVIRONMENTAL_DATA, data);
   }

   private float getPowerMultiplier() {
      EnvironmentalData data = (EnvironmentalData)this.getData(EnvironmentalModule.ENVIRONMENTAL_DATA);

      return switch (data.mode()) {
         case MODE_BLACKLIST, MODE_WHITELIST -> 1.0F;
         case MODE_HOSTILE, MODE_PASSIVE, MODE_MOBS, MODE_ALL -> (float)((Double)EnvironmentalConfiguration.mobsPowerMultiplier.get()).doubleValue();
      };
   }

   public boolean isEntityAffected(Entity entity) {
      EnvironmentalData data = (EnvironmentalData)this.getData(EnvironmentalModule.ENVIRONMENTAL_DATA);
      switch (data.mode()) {
         case MODE_BLACKLIST:
            if (entity instanceof Player) {
               return this.isPlayerAffected((Player)entity);
            }

            return false;
         case MODE_WHITELIST:
            if (entity instanceof Player) {
               return this.isPlayerAffected((Player)entity);
            }

            return false;
         case MODE_HOSTILE:
            return entity instanceof Enemy;
         case MODE_PASSIVE:
            return entity instanceof Mob && !(entity instanceof Enemy);
         case MODE_MOBS:
            return entity instanceof Mob;
         case MODE_ALL:
            if (entity instanceof Player) {
               return this.isPlayerAffected((Player)entity);
            }

            return true;
         default:
            return false;
      }
   }

   public boolean isPlayerAffected(Player player) {
      EnvironmentalData data = (EnvironmentalData)this.getData(EnvironmentalModule.ENVIRONMENTAL_DATA);
      if (data.mode() == EnvironmentalMode.MODE_WHITELIST) {
         return data.players().contains(player.getName().getString());
      } else {
         return data.mode() == EnvironmentalMode.MODE_BLACKLIST
            ? !data.players().contains(player.getName().getString())
            : data.mode() == EnvironmentalMode.MODE_ALL;
      }
   }

   public List<String> getPlayersAsList() {
      EnvironmentalData data = (EnvironmentalData)this.getData(EnvironmentalModule.ENVIRONMENTAL_DATA);
      return new ArrayList<>(data.players());
   }

   private void addPlayer(String player) {
      EnvironmentalData data = (EnvironmentalData)this.getData(EnvironmentalModule.ENVIRONMENTAL_DATA);
      Set<String> players = data.players();
      if (!players.contains(player)) {
         players.add(player);
         this.setData(EnvironmentalModule.ENVIRONMENTAL_DATA, data.withPlayers(players));
      }
   }

   private void delPlayer(String player) {
      EnvironmentalData data = (EnvironmentalData)this.getData(EnvironmentalModule.ENVIRONMENTAL_DATA);
      Set<String> players = data.players();
      if (players.contains(player)) {
         players.remove(player);
         this.setData(EnvironmentalModule.ENVIRONMENTAL_DATA, data.withPlayers(players));
      }
   }

   public boolean isActive() {
      return this.active;
   }

   public int getTotalRfPerTick() {
      if (this.environmentModules == null) {
         this.getEnvironmentModules();
      }

      float factor = this.infusable.getInfusedFactor();
      int rfNeeded = (int)(this.totalRfPerTick * this.getPowerMultiplier() * (4.0F - factor) / 4.0F);
      if (this.environmentModules.isEmpty()) {
         return rfNeeded;
      } else {
         if (rfNeeded < (Integer)EnvironmentalConfiguration.MIN_USAGE.get()) {
            rfNeeded = (Integer)EnvironmentalConfiguration.MIN_USAGE.get();
         }

         return rfNeeded;
      }
   }

   public int getVolume() {
      if (this.volume == -1) {
         EnvironmentalData data = (EnvironmentalData)this.getData(EnvironmentalModule.ENVIRONMENTAL_DATA);
         this.volume = (int)(data.radius() * data.radius() * Math.PI * (data.maxy() - data.miny() + 1));
      }

      return this.volume;
   }

   public int getRadius() {
      EnvironmentalData data = (EnvironmentalData)this.getData(EnvironmentalModule.ENVIRONMENTAL_DATA);
      return data.radius();
   }

   public void setRadius(int radius) {
      EnvironmentalData data = (EnvironmentalData)this.getData(EnvironmentalModule.ENVIRONMENTAL_DATA);
      data = data.withRadius(radius);
      this.setData(EnvironmentalModule.ENVIRONMENTAL_DATA, data);
      this.volume = -1;
      this.environmentModules = null;
   }

   public int getMiny() {
      EnvironmentalData data = (EnvironmentalData)this.getData(EnvironmentalModule.ENVIRONMENTAL_DATA);
      return data.miny();
   }

   public void setMiny(int miny) {
      if (miny != Integer.MIN_VALUE) {
         EnvironmentalData data = (EnvironmentalData)this.getData(EnvironmentalModule.ENVIRONMENTAL_DATA);
         data = data.withMiny(miny);
         this.setData(EnvironmentalModule.ENVIRONMENTAL_DATA, data);
         this.volume = -1;
         this.environmentModules = null;
      }
   }

   public int getMaxy() {
      EnvironmentalData data = (EnvironmentalData)this.getData(EnvironmentalModule.ENVIRONMENTAL_DATA);
      return data.maxy();
   }

   public void setMaxy(int maxy) {
      if (maxy != Integer.MIN_VALUE) {
         EnvironmentalData data = (EnvironmentalData)this.getData(EnvironmentalModule.ENVIRONMENTAL_DATA);
         data = data.withMaxy(maxy);
         this.setData(EnvironmentalModule.ENVIRONMENTAL_DATA, data);
         this.volume = -1;
         this.environmentModules = null;
      }
   }

   protected void tickServer() {
      if (this.powerTimeout > 0) {
         this.powerTimeout--;
      } else {
         long rf = this.energyStorage.getEnergyStored();
         if (!this.isMachineEnabled()) {
            rf = 0L;
         }

         this.getEnvironmentModules();
         int rfNeeded = this.getTotalRfPerTick();
         if (rfNeeded <= rf && !this.environmentModules.isEmpty()) {
            this.energyStorage.consumeEnergy(rfNeeded);

            for (EnvironmentModule module : this.environmentModules) {
               module.activate(true);
               EnvironmentalData data = (EnvironmentalData)this.getData(EnvironmentalModule.ENVIRONMENTAL_DATA);
               module.tick(this.level, this.getBlockPos(), data.radius(), data.miny(), data.maxy(), this);
            }

            if (!this.active) {
               this.active = true;
               this.markDirtyClient();
            }
         } else {
            this.deactivate();
            this.powerTimeout = 20;
         }
      }
   }

   public void deactivate() {
      for (EnvironmentModule module : this.environmentModules) {
         module.activate(false);
      }

      if (this.active) {
         this.active = false;
         this.markDirtyClient();
      }
   }

   public void setPowerInput(int powered) {
      if (this.powerLevel != powered) {
         this.powerTimeout = 0;
      }

      super.setPowerInput(powered);
   }

   public List<EnvironmentModule> getEnvironmentModules() {
      if (this.environmentModules == null) {
         int volume = this.getVolume();
         this.totalRfPerTick = 0;
         this.environmentModules = new ArrayList<>();

         for (int i = 0; i < this.items.getSlots(); i++) {
            ItemStack itemStack = this.items.getStackInSlot(i);
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof EnvModuleProvider moduleProvider) {
               Supplier<? extends EnvironmentModule> supplier = moduleProvider.getServerEnvironmentModule();
               EnvironmentModule environmentModule = supplier.get();
               this.environmentModules.add(environmentModule);
               this.totalRfPerTick = this.totalRfPerTick + (int)(environmentModule.getRfPerTick() * volume);
            }
         }
      }

      return this.environmentModules;
   }

   public void loadClientDataFromNBT(CompoundTag tag, Provider provider) {
      this.active = tag.getBooleanOr("active", false);
   }

   public void saveClientDataToNBT(CompoundTag tag, Provider provider) {
      tag.putBoolean("active", this.active);
   }

   public void saveAdditional(ValueOutput tag) {
      super.saveAdditional(tag);
      tag.putInt("rfPerTick", this.totalRfPerTick);
      tag.putBoolean("active", this.active);
      this.energyStorage.save(tag, "energy");
      this.items.save(tag, "items");
      this.infusable.save(tag, "infusable");
   }

   public void loadAdditional(ValueInput tag) {
      super.loadAdditional(tag);
      this.totalRfPerTick = tag.getIntOr("rfPerTick", 0);
      this.active = tag.getBooleanOr("active", false);
      this.energyStorage.load(tag, "energy");
      this.items.load(tag, "items");
      this.infusable.load(tag, "infusable");
   }

   protected void applyImplicitComponents(DataComponentGetter input) {
      super.applyImplicitComponents(input);
      EnvironmentalData data = (EnvironmentalData)input.get(EnvironmentalModule.ITEM_ENVIRONMENTAL_DATA);
      if (data != null) {
         this.setData(EnvironmentalModule.ENVIRONMENTAL_DATA, data);
      }

      this.energyStorage.applyImplicitComponents((ItemEnergy)input.get(Registration.ITEM_ENERGY));
      this.items.applyImplicitComponents((ItemInventory)input.get(Registration.ITEM_INVENTORY));
      this.infusable.applyImplicitComponents((ItemInfusable)input.get(Registration.ITEM_INFUSABLE));
   }

   protected void collectImplicitComponents(Builder builder) {
      super.collectImplicitComponents(builder);
      builder.set(EnvironmentalModule.ITEM_ENVIRONMENTAL_DATA, (EnvironmentalData)this.getData(EnvironmentalModule.ENVIRONMENTAL_DATA));
      this.energyStorage.collectImplicitComponents(builder);
      this.items.collectImplicitComponents(builder);
      this.infusable.collectImplicitComponents(builder);
   }

   public void onReplaced(Level world, BlockPos pos, BlockState state, BlockState newstate) {
      this.deactivate();
   }

   @Nonnull
   private IPowerInformation createPowerInfo() {
      return new IPowerInformation() {
         {
            Objects.requireNonNull(EnvironmentalControllerTileEntity.this);
         }

         public long getEnergyDiffPerTick() {
            return EnvironmentalControllerTileEntity.this.isActive() ? -EnvironmentalControllerTileEntity.this.getTotalRfPerTick() : 0L;
         }

         public String getEnergyUnitName() {
            return "RF";
         }

         public boolean isMachineActive() {
            return EnvironmentalControllerTileEntity.this.isActive();
         }

         public boolean isMachineRunning() {
            return EnvironmentalControllerTileEntity.this.isActive();
         }

         public String getMachineStatus() {
            return EnvironmentalControllerTileEntity.this.isActive() ? "active" : "idle";
         }
      };
   }
}
