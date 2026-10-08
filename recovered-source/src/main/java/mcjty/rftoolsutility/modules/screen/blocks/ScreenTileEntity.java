package mcjty.rftoolsutility.modules.screen.blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.Function;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.container.ItemInventory;
import mcjty.lib.api.module.DefaultModuleSupport;
import mcjty.lib.api.module.IModuleSupport;
import mcjty.lib.bindings.GuiValue;
import mcjty.lib.bindings.Value;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ResultCommand;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.network.Networking;
import mcjty.lib.network.PacketServerCommandTyped;
import mcjty.lib.setup.Registration;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleProvider;
import mcjty.rftoolsbase.api.screens.IScreenDataHelper;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.IScreenModuleUpdater;
import mcjty.rftoolsbase.api.screens.ITooltipInfo;
import mcjty.rftoolsbase.api.screens.data.IModuleData;
import mcjty.rftoolsbase.api.screens.data.IModuleDataBoolean;
import mcjty.rftoolsbase.api.screens.data.IModuleDataContents;
import mcjty.rftoolsbase.api.screens.data.IModuleDataInteger;
import mcjty.rftoolsbase.api.screens.data.IModuleDataString;
import mcjty.rftoolsbase.tools.GenericModuleItem;
import mcjty.rftoolsutility.modules.screen.ScreenModule;
import mcjty.rftoolsutility.modules.screen.data.ModuleDataBoolean;
import mcjty.rftoolsutility.modules.screen.data.ModuleDataInteger;
import mcjty.rftoolsutility.modules.screen.data.ModuleDataString;
import mcjty.rftoolsutility.modules.screen.data.ScreenData;
import mcjty.rftoolsutility.modules.screen.items.modules.TextModuleItem;
import mcjty.rftoolsutility.modules.screen.modules.ScreenModuleHelper;
import mcjty.rftoolsutility.modules.screen.modules.TextScreenModule;
import mcjty.rftoolsutility.modules.screen.modulesclient.TextClientScreenModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.apache.commons.lang3.tuple.Pair;

public class ScreenTileEntity extends TickingTileEntity {
   public List<String> infoReceived = Collections.emptyList();
   @GuiValue
   public static final Value<ScreenTileEntity, Boolean> VALUE_BRIGHT = Value.create(
      "bright", Type.BOOLEAN, ScreenTileEntity::isBright, ScreenTileEntity::setBright
   );
   private final GenericItemHandler items = GenericItemHandler.create(this, ScreenContainer.CONTAINER_FACTORY)
      .onUpdate((slot, stack) -> this.resetModules())
      .build();
   @Cap(type = CapType.ITEMS_AUTOMATION)
   private static final Function<ScreenTileEntity, GenericItemHandler> ITEM_CAP = be -> be.items;
   @Cap(type = CapType.CONTAINER)
   private static final Function<ScreenTileEntity, MenuProvider> SCREEN_CAP = be -> new DefaultContainerProvider("Screen")
      .containerSupplier((windowId, player) -> ScreenContainer.create(windowId, be.getBlockPos(), be, player))
      .itemHandler(() -> be.items)
      .setupSync(be);
   @Cap(type = CapType.MODULE)
   private static final Function<ScreenTileEntity, IModuleSupport> MODULE_CAP = be -> new DefaultModuleSupport(0, 10) {
      public boolean isModule(ItemStack itemStack) {
         return itemStack.getItem() instanceof IModuleProvider;
      }
   };
   public static final Map<GlobalPos, Map<Integer, IModuleData>> screenData = new HashMap<>();
   private List<Pair<ItemStack, IClientScreenModule<?>>> clientScreenModules = null;
   private ResourceKey<Level> dummyType = null;
   private boolean needsServerData = false;
   private boolean showHelp = true;
   private boolean powerOn = false;
   private boolean connected = false;
   private int hoveringModule = -1;
   private int hoveringX = -1;
   private int hoveringY = -1;
   public static final int SIZE_NORMAL = 0;
   public static final int SIZE_LARGE = 1;
   public static final int SIZE_HUGE = 2;
   private List<IScreenModule<?, ?>> screenModules = null;
   private Map<ScreenTileEntity.ActivatedModule, ScreenTileEntity.ModuleTicker> clickedModules = new HashMap<>();
   private int totalRfPerTick = 0;
   private boolean controllerNeededInCreative = false;
   public long lastTime = 0L;
   private static List<Pair<ItemStack, IClientScreenModule<?>>> helpingScreenModules = null;
   private final IScreenDataHelper screenDataHelper = new IScreenDataHelper() {
      {
         Objects.requireNonNull(ScreenTileEntity.this);
      }

      public IModuleDataInteger createInteger(int i) {
         return new ModuleDataInteger(i);
      }

      public IModuleDataBoolean createBoolean(boolean b) {
         return new ModuleDataBoolean(b);
      }

      public IModuleDataString createString(String b) {
         return new ModuleDataString(b);
      }

      public IModuleDataContents createContents(long contents, long maxContents, long lastPerTick) {
         return new ScreenModuleHelper.ModuleDataContents(contents, maxContents, lastPerTick);
      }
   };
   public static final Key<Integer> PARAM_X = new Key("x", Type.INTEGER);
   public static final Key<Integer> PARAM_Y = new Key("y", Type.INTEGER);
   public static final Key<Integer> PARAM_MODULE = new Key("module", Type.INTEGER);
   public static final Key<Integer> PARAM_TRUETYPE = new Key("truetype", Type.INTEGER);
   @ServerCommand
   public static final Command<?> CMD_CLICK = Command.create(
      "screen.click",
      (te, player, params) -> te.hitScreenServer(player, (Integer)params.get(PARAM_X), (Integer)params.get(PARAM_Y), (Integer)params.get(PARAM_MODULE))
   );
   @ServerCommand
   public static final Command<?> CMD_HOVER = Command.create("screen.hover", (te, player, params) -> {
      te.hoveringX = (Integer)params.get(PARAM_X);
      te.hoveringY = (Integer)params.get(PARAM_Y);
      te.hoveringModule = (Integer)params.get(PARAM_MODULE);
   });
   @ServerCommand
   public static final Command<?> CMD_SETTRUETYPE = Command.create(
      "screen.setTruetype", (te, player, params) -> te.setTrueTypeMode((Integer)params.get(PARAM_TRUETYPE))
   );
   public static final Key<List<String>> PARAM_INFO = new Key("info", Type.STRING_LIST);
   @ServerCommand
   public static final ResultCommand<?> CMD_SCREEN_INFO = ResultCommand.create("getScreenInfo", (te, player, params) -> {
      IScreenModule<?, ?> module = te.getHoveringModule((Integer)params.get(PARAM_MODULE));
      List<String> info = Collections.emptyList();
      if (module instanceof ITooltipInfo) {
         info = ((ITooltipInfo)module).getInfo(te.level, (Integer)params.get(PARAM_X), (Integer)params.get(PARAM_Y));
      }

      return TypedMap.builder().put(PARAM_INFO, info).build();
   }, (te, player, params) -> te.infoReceived = (List<String>)params.get(PARAM_INFO));

   public ScreenTileEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ScreenModule.SCREEN.be().get(), pos, state);
   }

   public ScreenTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   public ScreenTileEntity(ResourceKey<Level> world, BlockPos pos) {
      this(pos, ((ScreenBlock)ScreenModule.SCREEN.block().get()).defaultBlockState());
      this.dummyType = world;
   }

   public ScreenTileEntity(BlockEntityType<?> type, ResourceKey<Level> world, BlockPos pos, BlockState state) {
      this(type, pos, state);
      this.dummyType = world;
   }

   public boolean isDummy() {
      return this.dummyType != null;
   }

   public ResourceKey<Level> getDimension() {
      return this.dummyType != null ? this.dummyType : super.getDimension();
   }

   protected void tickClient() {
      this.tickMe();
   }

   public void tickMe() {
      if (!this.clickedModules.isEmpty()) {
         Map<ScreenTileEntity.ActivatedModule, ScreenTileEntity.ModuleTicker> newClickedModules = new HashMap<>();

         for (Entry<ScreenTileEntity.ActivatedModule, ScreenTileEntity.ModuleTicker> cm : this.clickedModules.entrySet()) {
            cm.getValue().ticks--;
            ScreenTileEntity.ActivatedModule activatedModule = cm.getKey();
            if (cm.getValue().ticks > 0) {
               newClickedModules.put(activatedModule, cm.getValue());
            } else {
               List<Pair<ItemStack, IClientScreenModule<?>>> modules = this.getClientScreenModules();
               if (activatedModule.module < modules.size()) {
                  Pair<ItemStack, IClientScreenModule<?>> pair = modules.get(activatedModule.module);
                  if (pair != null && pair.getRight() != null) {
                     ((IClientScreenModule)pair.getRight()).mouseClick((ItemStack)pair.getLeft(), this.level, activatedModule.x, activatedModule.y, false);
                  }
               }
            }
         }

         this.clickedModules = newClickedModules;
      }
   }

   protected void tickServer() {
      if (!this.clickedModules.isEmpty()) {
         Map<ScreenTileEntity.ActivatedModule, ScreenTileEntity.ModuleTicker> newClickedModules = new HashMap<>();

         for (Entry<ScreenTileEntity.ActivatedModule, ScreenTileEntity.ModuleTicker> cm : this.clickedModules.entrySet()) {
            cm.getValue().ticks--;
            ScreenTileEntity.ActivatedModule activatedModule = cm.getKey();
            if (cm.getValue().ticks > 0) {
               newClickedModules.put(activatedModule, cm.getValue());
            } else {
               List<IScreenModule<?, ?>> modules = this.getScreenModules();
               if (activatedModule.module < modules.size()) {
                  ItemStack moduleStack = this.items.getStackInSlot(activatedModule.module);
                  IScreenModule<?, ?> module = modules.get(activatedModule.module);
                  ItemStack updated = module.mouseClick(moduleStack, this.level, activatedModule.x, activatedModule.y, false, null);
                  if (!updated.isEmpty()) {
                     this.items.setStackInSlot(activatedModule.module, updated);
                     this.markDirtyClient();
                     moduleStack = updated;
                  }

                  if (module instanceof IScreenModuleUpdater updater) {
                     updated = updater.update(moduleStack, this.level, null);
                     if (!updated.isEmpty()) {
                        this.items.setStackInSlot(activatedModule.module, updated);
                        this.markDirtyClient();
                     }
                  }
               }
            }
         }

         this.clickedModules = newClickedModules;
      }
   }

   private void resetModules() {
      this.clientScreenModules = null;
      this.screenModules = null;
      this.clickedModules.clear();
      this.showHelp = true;
   }

   private boolean isActivated(int index) {
      for (ScreenTileEntity.ActivatedModule module : this.clickedModules.keySet()) {
         if (module.module == index) {
            return true;
         }
      }

      return false;
   }

   public void focusModuleClient(double hitX, double hitY, double hitZ, Direction side, Direction horizontalFacing) {
      ScreenData data = (ScreenData)this.getData(ScreenModule.SCREEN_DATA);
      ScreenTileEntity.ModuleRaytraceResult result = this.getHitModule(hitX, hitY, hitZ, side, horizontalFacing, data.size());
      int x;
      int y;
      int module;
      if (result == null) {
         x = -1;
         y = -1;
         module = -1;
      } else {
         x = result.x();
         y = result.y() - result.currenty();
         module = result.moduleIndex();
      }

      if (x != this.hoveringX || y != this.hoveringY || module != this.hoveringModule) {
         PacketServerCommandTyped packet = PacketServerCommandTyped.create(
            this.getBlockPos(), this.getDimension(), CMD_HOVER.name(), TypedMap.builder().put(PARAM_X, x).put(PARAM_Y, y).put(PARAM_MODULE, module).build()
         );
         Networking.sendToServer(packet);
         this.hoveringX = x;
         this.hoveringY = y;
         this.hoveringModule = module;
      }
   }

   public void hitScreenClient(double hitX, double hitY, double hitZ, Direction side, Direction horizontalFacing) {
      ScreenData data = (ScreenData)this.getData(ScreenModule.SCREEN_DATA);
      ScreenTileEntity.ModuleRaytraceResult result = this.getHitModule(hitX, hitY, hitZ, side, horizontalFacing, data.size());
      if (result != null) {
         this.hitScreenClient(result);
      }
   }

   public void hitScreenClient(ScreenTileEntity.ModuleRaytraceResult result) {
      List<Pair<ItemStack, IClientScreenModule<?>>> modules = this.getClientScreenModules();
      int module = result.moduleIndex();
      if (!this.isActivated(module)) {
         ((IClientScreenModule)modules.get(module).getRight())
            .mouseClick((ItemStack)modules.get(module).getLeft(), this.level, result.x(), result.y() - result.currenty(), true);
         this.clickedModules.put(new ScreenTileEntity.ActivatedModule(module, result.x(), result.y()), new ScreenTileEntity.ModuleTicker(3));
         PacketServerCommandTyped packet = PacketServerCommandTyped.create(
            this.getBlockPos(),
            this.getDimension(),
            CMD_CLICK.name(),
            TypedMap.builder().put(PARAM_X, result.x()).put(PARAM_Y, result.y() - result.currenty()).put(PARAM_MODULE, module).build()
         );
         Networking.sendToServer(packet);
      }
   }

   public ScreenTileEntity.ModuleRaytraceResult getHitModule(double hitX, double hitY, double hitZ, Direction side, Direction horizontalFacing, int size) {
      float dx;
      float dy;
      float factor = size + 1.0F;
      dx = 0.0F;
      dy = 0.0F;
      label41:
      switch (side) {
         case NORTH:
            dx = (float)((1.0 - hitX) / factor);
            dy = (float)((1.0 - hitY) / factor);
            break;
         case SOUTH:
            dx = (float)(hitX / factor);
            dy = (float)((1.0 - hitY) / factor);
            break;
         case WEST:
            dx = (float)(hitZ / factor);
            dy = (float)((1.0 - hitY) / factor);
            break;
         case EAST:
            dx = (float)((1.0 - hitZ) / factor);
            dy = (float)((1.0 - hitY) / factor);
            break;
         case UP:
            switch (horizontalFacing) {
               case NORTH:
                  dx = (float)((1.0 - hitX) / factor);
                  dy = (float)((1.0 - hitZ) / factor);
                  break label41;
               case SOUTH:
                  dx = (float)(hitX / factor);
                  dy = (float)(hitZ / factor);
                  break label41;
               case WEST:
                  dx = (float)(hitZ / factor);
                  dy = (float)((1.0 - hitX) / factor);
                  break label41;
               case EAST:
                  dx = (float)((1.0 - hitZ) / factor);
                  dy = (float)(hitX / factor);
               default:
                  break label41;
            }
         case DOWN:
            switch (horizontalFacing) {
               case NORTH:
                  dx = (float)((1.0 - hitX) / factor);
                  dy = (float)(hitZ / factor);
                  break label41;
               case SOUTH:
                  dx = (float)(hitX / factor);
                  dy = (float)((1.0 - hitZ) / factor);
                  break label41;
               case WEST:
                  dx = (float)(hitZ / factor);
                  dy = (float)(hitX / factor);
                  break label41;
               case EAST:
                  dx = (float)((1.0 - hitZ) / factor);
                  dy = (float)((1.0 - hitX) / factor);
               default:
                  break label41;
            }
         default:
            return null;
      }

      int x = (int)(dx * 128.0F);
      int y = (int)(dy * 128.0F);
      int currenty = 7;
      int moduleIndex = 0;
      List<Pair<ItemStack, IClientScreenModule<?>>> clientScreenModules = this.getClientScreenModules();

      for (Pair<ItemStack, IClientScreenModule<?>> pair : clientScreenModules) {
         if (pair != null) {
            IClientScreenModule<?> module = (IClientScreenModule<?>)pair.getRight();
            if (module != null) {
               int height = module.getHeight((ItemStack)pair.getLeft());
               if (currenty + height <= 124) {
                  if (currenty <= y && y < currenty + height) {
                     break;
                  }

                  currenty += height;
               }
            }
         }

         moduleIndex++;
      }

      return moduleIndex >= clientScreenModules.size() ? null : new ScreenTileEntity.ModuleRaytraceResult(moduleIndex, x, y, currenty);
   }

   private void hitScreenServer(Player player, int x, int y, int module) {
      List<IScreenModule<?, ?>> screenModules = this.getScreenModules();
      IScreenModule<?, ?> screenModule = screenModules.get(module);
      if (screenModule != null) {
         ItemStack moduleStack = this.items.getStackInSlot(module);
         ItemStack updated = screenModule.mouseClick(moduleStack, this.level, x, y, true, player);
         if (!updated.isEmpty()) {
            this.items.setStackInSlot(module, updated);
            this.markDirtyClient();
            moduleStack = updated;
         }

         if (screenModule instanceof IScreenModuleUpdater updater) {
            updated = updater.update(moduleStack, this.level, player);
            if (!updated.isEmpty()) {
               this.items.setStackInSlot(module, updated);
               this.markDirtyClient();
            }
         }

         this.clickedModules.put(new ScreenTileEntity.ActivatedModule(module, x, y), new ScreenTileEntity.ModuleTicker(5));
      }
   }

   public void loadAdditional(ValueInput tag) {
      super.loadAdditional(tag);
      this.items.load(tag, "items");
      this.powerOn = tag.getBooleanOr("powerOn", false);
      this.connected = tag.getBooleanOr("connected", false);
      this.totalRfPerTick = tag.getIntOr("rfPerTick", 0);
      this.controllerNeededInCreative = tag.getBooleanOr("controllerNeededInCreative", false);
      this.resetModules();
   }

   public void saveAdditional(ValueOutput tag) {
      super.saveAdditional(tag);
      this.items.save(tag, "items");
      tag.putBoolean("powerOn", this.powerOn);
      tag.putBoolean("connected", this.connected);
      tag.putInt("rfPerTick", this.totalRfPerTick);
      tag.putBoolean("controllerNeededInCreative", this.controllerNeededInCreative);
   }

   protected void applyImplicitComponents(DataComponentGetter input) {
      super.applyImplicitComponents(input);
      ScreenData data = (ScreenData)input.get(ScreenModule.ITEM_SCREEN_DATA);
      if (data != null) {
         this.setData(ScreenModule.SCREEN_DATA, data);
      }

      this.items.applyImplicitComponents((ItemInventory)input.get(Registration.ITEM_INVENTORY));
   }

   protected void collectImplicitComponents(Builder builder) {
      super.collectImplicitComponents(builder);
      builder.set(ScreenModule.ITEM_SCREEN_DATA, (ScreenData)this.getData(ScreenModule.SCREEN_DATA));
      this.items.collectImplicitComponents(builder);
   }

   public void saveClientDataToNBT(CompoundTag tag, Provider provider) {
      tag.putBoolean("powerOn", this.powerOn);
      tag.putBoolean("connected", this.connected);
      ScreenData.CODEC.encodeStart(NbtOps.INSTANCE, (ScreenData)this.getData(ScreenModule.SCREEN_DATA)).result().ifPresent(data -> tag.put("data", data));
      this.items.save(tag, "items", provider);
   }

   public void loadClientDataFromNBT(CompoundTag tag, Provider provider) {
      this.powerOn = tag.getBooleanOr("powerOn", false);
      this.connected = tag.getBooleanOr("connected", false);
      ScreenData.CODEC.decode(NbtOps.INSTANCE, tag.get("data")).result().ifPresent(data -> this.setData(ScreenModule.SCREEN_DATA, (ScreenData)data.getFirst()));
      this.items.load(tag, "items", provider);
      this.resetModules();
   }

   public int getColor() {
      ScreenData data = (ScreenData)this.getData(ScreenModule.SCREEN_DATA);
      return data.color();
   }

   public void setColor(int color) {
      ScreenData data = (ScreenData)this.getData(ScreenModule.SCREEN_DATA);
      data = data.withColor(color);
      this.setData(ScreenModule.SCREEN_DATA, data);
   }

   public void setSize(int size) {
      ScreenData data = (ScreenData)this.getData(ScreenModule.SCREEN_DATA);
      data = data.withSize(size);
      this.setData(ScreenModule.SCREEN_DATA, data);
   }

   public int getSize() {
      ScreenData data = (ScreenData)this.getData(ScreenModule.SCREEN_DATA);
      return data.size();
   }

   public boolean isBright() {
      ScreenData data = (ScreenData)this.getData(ScreenModule.SCREEN_DATA);
      return data.bright();
   }

   public void setBright(boolean bright) {
      ScreenData data = (ScreenData)this.getData(ScreenModule.SCREEN_DATA);
      data = data.withBright(bright);
      this.setData(ScreenModule.SCREEN_DATA, data);
   }

   public int getTrueTypeMode() {
      ScreenData data = (ScreenData)this.getData(ScreenModule.SCREEN_DATA);
      return data.trueTypeMode();
   }

   public void setTrueTypeMode(int trueTypeMode) {
      ScreenData data = (ScreenData)this.getData(ScreenModule.SCREEN_DATA);
      data = data.withTrueTypeMode(trueTypeMode);
      this.setData(ScreenModule.SCREEN_DATA, data);
   }

   public void setTransparent(boolean transparent) {
      ScreenData data = (ScreenData)this.getData(ScreenModule.SCREEN_DATA);
      data = data.withTransparent(transparent);
      this.setData(ScreenModule.SCREEN_DATA, data);
   }

   public boolean isTransparent() {
      ScreenData data = (ScreenData)this.getData(ScreenModule.SCREEN_DATA);
      return data.transparent();
   }

   public void setPower(boolean power) {
      if (this.powerOn != power) {
         this.powerOn = power;
         this.markDirtyClient();
      }
   }

   public boolean isPowerOn() {
      return this.powerOn;
   }

   public boolean isRenderable() {
      if (this.powerOn) {
         return true;
      } else {
         return this.isShowHelp() ? true : this.isCreative();
      }
   }

   public boolean isCreative() {
      return false;
   }

   public void setConnected(boolean c) {
      if (this.connected != c) {
         this.connected = c;
         this.setChanged();
      }
   }

   public boolean isConnected() {
      return this.connected;
   }

   public void updateModuleData(int slot, ItemStack newStack) {
      this.items.setStackInSlot(slot, newStack);
      this.screenModules = null;
      this.clientScreenModules = null;
      this.markDirtyClient();
   }

   public static List<Pair<ItemStack, IClientScreenModule<?>>> getHelpingScreenModules() {
      if (helpingScreenModules == null) {
         helpingScreenModules = new ArrayList<>();
         addLine("Read me", 7838207, true);
         addLine("", 16777215, false);
         addLine("Sneak-right click for", 16777215, false);
         addLine("GUI and insertion of", 16777215, false);
         addLine("modules", 16777215, false);
         addLine("", 16777215, false);
         addLine("Use Screen Controller", 16777215, false);
         addLine("to power screens", 16777215, false);
         addLine("remotely", 16777215, false);
      }

      return helpingScreenModules;
   }

   private static void addLine(String s, int color, boolean large) {
      ItemStack textModuleItem = new ItemStack((ItemLike)ScreenModule.TEXT_MODULE.get());
      TextScreenModule data = TextModuleItem.data(textModuleItem);
      data = data.withLine(s);
      data = data.withColor(color);
      data = data.withLarge(large);
      textModuleItem.set(ScreenModule.MODULE_TEXT_DATA, data);
      TextClientScreenModule t1 = new TextClientScreenModule();
      helpingScreenModules.add(Pair.of(textModuleItem, t1));
   }

   public List<Pair<ItemStack, IClientScreenModule<?>>> getClientScreenModules() {
      if (this.clientScreenModules == null) {
         this.needsServerData = false;
         this.showHelp = true;
         this.clientScreenModules = new ArrayList<>();

         for (int i = 0; i < this.items.getSlots(); i++) {
            ItemStack itemStack = this.items.getStackInSlot(i);
            if (!itemStack.isEmpty() && ScreenBlock.hasModuleProvider(itemStack)) {
               IModuleProvider moduleProvider = ScreenBlock.getModuleProvider(itemStack);
               IClientScreenModule<?> clientScreenModule = moduleProvider.createClientScreenModule();
               this.clientScreenModules.add(Pair.of(itemStack, clientScreenModule));
               if (clientScreenModule.needsServerData()) {
                  this.needsServerData = true;
               }

               this.showHelp = false;
            } else {
               this.clientScreenModules.add(null);
            }
         }
      }

      return this.clientScreenModules;
   }

   public boolean isShowHelp() {
      return this.showHelp;
   }

   public boolean isNeedsServerData() {
      return this.needsServerData;
   }

   public int getTotalRfPerTick() {
      if (this.isCreative()) {
         return 0;
      } else {
         if (this.screenModules == null) {
            this.getScreenModules();
         }

         return this.totalRfPerTick;
      }
   }

   public boolean isControllerNeeded() {
      if (!this.isCreative()) {
         return true;
      } else {
         if (this.screenModules == null) {
            this.getScreenModules();
         }

         return this.controllerNeededInCreative;
      }
   }

   public List<IScreenModule<?, ?>> getScreenModules() {
      if (this.screenModules == null) {
         this.totalRfPerTick = 0;
         this.controllerNeededInCreative = false;
         this.screenModules = new ArrayList<>();

         for (int i = 0; i < this.items.getSlots(); i++) {
            ItemStack itemStack = this.items.getStackInSlot(i);
            if (!itemStack.isEmpty() && ScreenBlock.hasModuleProvider(itemStack)) {
               IModuleProvider moduleProvider = ScreenBlock.getModuleProvider(itemStack);
               IScreenModule<?, ?> screenModule = moduleProvider.componentType() != null ? (IScreenModule)itemStack.get(moduleProvider.componentType()) : null;
               if (screenModule == null) {
                  screenModule = moduleProvider.createServerScreenModule();
               }

               boolean isPlus = itemStack.getItem() instanceof GenericModuleItem mi && mi.isPlusModule();
               screenModule = screenModule.validate(this.level, this.getBlockPos(), isPlus);
               this.screenModules.add(screenModule);
               this.totalRfPerTick = this.totalRfPerTick + screenModule.getRfPerTick() * (isPlus ? 5 : 1);
               if (screenModule.needsController()) {
                  this.controllerNeededInCreative = true;
               }
            } else {
               this.screenModules.add(null);
            }
         }
      }

      return this.screenModules;
   }

   public Map<Integer, IModuleData> getScreenData(long millis) {
      Map<Integer, IModuleData> map = new HashMap<>();
      List<IScreenModule<?, ?>> screenModules = this.getScreenModules();
      int moduleIndex = 0;

      for (IScreenModule<?, ?> module : screenModules) {
         if (module != null) {
            IModuleData data = module.getData(this.screenDataHelper, this.level, millis);
            if (data != null) {
               map.put(moduleIndex, data);
            }
         }

         moduleIndex++;
      }

      return map;
   }

   public IScreenModule<?, ?> getHoveringModule() {
      return this.getHoveringModule(this.hoveringModule);
   }

   public IScreenModule<?, ?> getHoveringModule(int hoveringModule) {
      if (hoveringModule == -1) {
         return null;
      } else {
         this.getScreenModules();
         return hoveringModule >= 0 && hoveringModule < this.screenModules.size() ? this.screenModules.get(hoveringModule) : null;
      }
   }

   public int getHoveringX() {
      return this.hoveringX;
   }

   public int getHoveringY() {
      return this.hoveringY;
   }

   private record ActivatedModule(int module, int x, int y) {
   }

   public record ModuleRaytraceResult(int moduleIndex, int x, int y, int currenty) {
   }

   private static class ModuleTicker {
      private int ticks;

      public ModuleTicker(int ticks) {
         this.ticks = ticks;
      }
   }
}
