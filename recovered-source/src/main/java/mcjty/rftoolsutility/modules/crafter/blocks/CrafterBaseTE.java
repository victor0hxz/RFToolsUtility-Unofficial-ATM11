package mcjty.rftoolsutility.modules.crafter.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.container.ItemInventory;
import mcjty.lib.api.infusable.DefaultInfusable;
import mcjty.lib.api.infusable.IInfusable;
import mcjty.lib.api.infusable.ItemInfusable;
import mcjty.lib.api.power.ItemEnergy;
import mcjty.lib.bindings.GuiValue;
import mcjty.lib.bindings.Value;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.container.UndoableItemHandler;
import mcjty.lib.crafting.BaseRecipe;
import mcjty.lib.setup.Registration;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.Cached;
import mcjty.lib.varia.InventoryTools;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsbase.api.compat.JEIRecipeAcceptor;
import mcjty.rftoolsbase.modules.filter.items.FilterModuleItem;
import mcjty.rftoolsutility.modules.crafter.CrafterConfiguration;
import mcjty.rftoolsutility.modules.crafter.CrafterModule;
import mcjty.rftoolsutility.modules.crafter.data.CraftMode;
import mcjty.rftoolsutility.modules.crafter.data.CrafterData;
import mcjty.rftoolsutility.modules.crafter.data.CraftingRecipe;
import mcjty.rftoolsutility.modules.crafter.data.KeepMode;
import mcjty.rftoolsutility.modules.crafter.data.SpeedMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class CrafterBaseTE extends TickingTileEntity implements JEIRecipeAcceptor {
   private final int supportedRecipes;
   private final GenericItemHandler items = GenericItemHandler.create(this, CrafterContainer.CONTAINER_FACTORY)
      .itemValid(this::isItemValidForSlot)
      .onUpdate((slot, stack) -> this.clearCacheOrUpdateRecipe(slot))
      .build();
   @Cap(type = CapType.ITEMS_AUTOMATION)
   private static final Function<CrafterBaseTE, GenericItemHandler> ITEM_CAP = be -> be.items;
   private final GenericEnergyStorage energyStorage = new GenericEnergyStorage(
      this, true, ((Integer)CrafterConfiguration.MAXENERGY.get()).intValue(), ((Integer)CrafterConfiguration.RECEIVEPERTICK.get()).intValue()
   );
   @Cap(type = CapType.ENERGY)
   private static final Function<CrafterBaseTE, GenericEnergyStorage> ENERGY_CAP = be -> be.energyStorage;
   @Cap(type = CapType.CONTAINER)
   private static final Function<CrafterBaseTE, MenuProvider> SCREEN_CAP = be -> new DefaultContainerProvider("Crafter")
      .containerSupplier(
         (windowId, player) -> new CrafterContainer(windowId, (ContainerFactory)CrafterContainer.CONTAINER_FACTORY.get(), be.getBlockPos(), be, player)
      )
      .itemHandler(() -> be.items)
      .energyHandler(() -> be.energyStorage)
      .data(CrafterModule.CRAFTER_DATA, CrafterData.STREAM_CODEC, CrafterData.CODEC)
      .setupSync(be);
   private final DefaultInfusable infusable = new DefaultInfusable(this);
   @Cap(type = CapType.INFUSABLE)
   private static final Function<CrafterBaseTE, IInfusable> INFUSABLE_CAP = be -> be.infusable;
   private final Cached<Predicate<ItemStack>> filterCache = Cached.of(this::createFilterCache);
   @GuiValue
   public static final Value<CrafterBaseTE, String> SPEED_MODE = Value.createEnum(
      "speedMode", SpeedMode.values(), CrafterBaseTE::getSpeedMode, CrafterBaseTE::setSpeedMode
   );
   private int selected = -1;
   @GuiValue
   public static final Value<CrafterBaseTE, Integer> SELECTED = Value.create("selected", Type.INTEGER, CrafterBaseTE::getSelected, CrafterBaseTE::setSelected);
   @GuiValue
   public static final Value<CrafterBaseTE, String> CRAFT_MODE = Value.createEnum(
      "craftMode", CraftMode.values(), CrafterBaseTE::getCraftMode, CrafterBaseTE::setCraftMode
   );
   @GuiValue
   public static final Value<CrafterBaseTE, String> KEEP_ONE = Value.createEnum(
      "keepOne", KeepMode.values(), CrafterBaseTE::getKeepOne, CrafterBaseTE::setKeepOne
   );
   public boolean noRecipesWork = false;
   private static CraftingInput workInventory = CraftingInput.of(3, 3, createList());
   @ServerCommand
   public static final Command<?> CMD_REMEMBER = Command.create("crafter.remember", (te, player, params) -> te.rememberItems(player));
   @ServerCommand
   public static final Command<?> CMD_FORGET = Command.create("crafter.forget", (te, player, params) -> te.forgetItems(player));
   @ServerCommand
   public static final Command<?> CMD_APPLY = Command.create("crafter.apply", (te, player, params) -> te.applyRecipe());

   public static CrafterBaseTE createTier1(BlockPos pos, BlockState state) {
      return new CrafterBaseTE((BlockEntityType)CrafterModule.CRAFTER1.be().get(), pos, state, 2);
   }

   public static CrafterBaseTE createTier2(BlockPos pos, BlockState state) {
      return new CrafterBaseTE((BlockEntityType)CrafterModule.CRAFTER2.be().get(), pos, state, 4);
   }

   public static CrafterBaseTE createTier3(BlockPos pos, BlockState state) {
      return new CrafterBaseTE((BlockEntityType)CrafterModule.CRAFTER3.be().get(), pos, state, 8);
   }

   private static List<ItemStack> createList() {
      List<ItemStack> list = new ArrayList<>();

      for (int i = 0; i < 9; i++) {
         list.add(ItemStack.EMPTY);
      }

      return list;
   }

   private void clearCacheOrUpdateRecipe(Integer slot) {
      this.noRecipesWork = false;
      if (this.level != null && !this.level.isClientSide()) {
         if (slot == 40) {
            this.filterCache.clear();
         } else if (slot >= 0 && slot < 9) {
            List<ItemStack> list = new ArrayList<>();

            for (int i = 0; i < 9; i++) {
               list.add(this.items.getStackInSlot(i + 0));
            }

            CraftingInput input = CraftingInput.of(3, 3, list);
            Recipe recipe = CraftingRecipe.findRecipe(this.level, input);
            if (recipe != null) {
               ItemStack result = BaseRecipe.assemble(recipe, input, this.level);
               this.items.setStackInSlot(9, result);
            } else {
               this.items.setStackInSlot(9, ItemStack.EMPTY);
            }
         }
      }
   }

   public CrafterBaseTE(BlockEntityType type, BlockPos pos, BlockState state, int supportedRecipes) {
      super(type, pos, state);
      this.supportedRecipes = supportedRecipes;
      List<CraftingRecipe> recipes = new ArrayList<>(supportedRecipes);

      for (int i = 0; i < supportedRecipes; i++) {
         recipes.add(new CraftingRecipe());
      }

      this.setData(CrafterModule.CRAFTER_DATA, CrafterData.createDefault().withRecipes(recipes));
   }

   public int getSelected() {
      return this.selected;
   }

   private void setSelected(int sel) {
      if (sel != this.selected) {
         if (sel >= 0 && sel < this.supportedRecipes) {
            this.selected = sel;
         } else {
            this.selected = -1;
         }

         if (this.selected < 0) {
            for (int i = 0; i < 10; i++) {
               this.items.setStackInSlot(0 + i, ItemStack.EMPTY);
            }
         } else {
            CrafterData data = (CrafterData)this.getData(CrafterModule.CRAFTER_DATA);
            CraftingRecipe recipe = data.getRecipeSafe(this.selected);
            this.items.setStackInSlot(9, recipe.getResult());
            List<ItemStack> list = recipe.convertTo3x3Grid();

            for (int i = 0; i < 9; i++) {
               this.items.setStackInSlot(0 + i, list.get(i));
            }

            this.items.setStackInSlot(9, recipe.getResult());
         }

         this.setChanged();
      }
   }

   private void applyRecipe() {
      if (this.selected >= 0 && this.selected < this.supportedRecipes) {
         CrafterData data = (CrafterData)this.getData(CrafterModule.CRAFTER_DATA);
         CraftingRecipe recipe = data.getRecipeSafe(this.selected).copy();
         ItemStack[] recipeItems = new ItemStack[9];

         for (int i = 0; i < 9; i++) {
            recipeItems[i] = this.items.getStackInSlot(i + 0).copy();
         }

         recipe.setRecipe(recipeItems, this.items.getStackInSlot(9).copy());
         data = data.setRecipeSafe(this.selected, recipe);
         this.setData(CrafterModule.CRAFTER_DATA, data);
         this.markDirtyClient();
      }
   }

   private CraftMode getCraftMode() {
      return this.selected >= 0 && this.selected < this.supportedRecipes
         ? ((CrafterData)this.getData(CrafterModule.CRAFTER_DATA)).getRecipeSafe(this.selected).getCraftMode()
         : CraftMode.EXT;
   }

   private void setCraftMode(CraftMode mode) {
      CrafterData data = (CrafterData)this.getData(CrafterModule.CRAFTER_DATA);
      if (this.selected >= 0 && this.selected < this.supportedRecipes && data.getRecipeSafe(this.selected).getCraftMode() != mode) {
         data.getRecipeSafe(this.selected).setCraftMode(mode);
         this.setData(CrafterModule.CRAFTER_DATA, data);
      }
   }

   private KeepMode getKeepOne() {
      return this.selected >= 0 && this.selected < this.supportedRecipes
         ? ((CrafterData)this.getData(CrafterModule.CRAFTER_DATA)).getRecipeSafe(this.selected).getKeepOne()
         : KeepMode.ALL;
   }

   private void setKeepOne(KeepMode keepOne) {
      CrafterData data = (CrafterData)this.getData(CrafterModule.CRAFTER_DATA);
      if (this.selected >= 0 && this.selected < this.supportedRecipes && data.getRecipeSafe(this.selected).getKeepOne() != keepOne) {
         data.getRecipeSafe(this.selected).setKeepOne(keepOne);
         this.setData(CrafterModule.CRAFTER_DATA, data);
      }
   }

   protected boolean needsRedstoneMode() {
      return true;
   }

   public List<ItemStack> getGhostSlots() {
      return ((CrafterData)this.getData(CrafterModule.CRAFTER_DATA)).ghostSlots();
   }

   public void setGridContents(List<ItemStack> stacks) {
      this.items.setStackInSlot(9, stacks.get(0));

      for (int i = 1; i < stacks.size(); i++) {
         this.items.setStackInSlot(0 + i - 1, stacks.get(i));
      }

      this.setChanged();
   }

   public int getSupportedRecipes() {
      return this.supportedRecipes;
   }

   public SpeedMode getSpeedMode() {
      CrafterData data = (CrafterData)this.getData(CrafterModule.CRAFTER_DATA);
      return data.speedMode();
   }

   public void setSpeedMode(SpeedMode speedMode) {
      CrafterData data = (CrafterData)this.getData(CrafterModule.CRAFTER_DATA);
      data = data.withSpeedMode(speedMode);
      this.setData(CrafterModule.CRAFTER_DATA, data);
      this.markDirtyClient();
   }

   public CraftingRecipe getRecipe(int index) {
      return ((CrafterData)this.getData(CrafterModule.CRAFTER_DATA)).getRecipeSafe(index);
   }

   public Predicate<ItemStack> createFilterCache() {
      return FilterModuleItem.getCache(this.items.getStackInSlot(40));
   }

   protected void tickServer() {
      if (this.isMachineEnabled() && !this.noRecipesWork) {
         int defaultCost = (Integer)CrafterConfiguration.rfPerOperation.get();
         int rf = (int)(defaultCost * (2.0F - this.infusable.getInfusedFactor()) / 2.0F);
         CrafterData data = (CrafterData)this.getData(CrafterModule.CRAFTER_DATA);
         int steps = data.speedMode() == SpeedMode.FAST ? (Integer)CrafterConfiguration.speedOperations.get() : 1;
         if (rf > 0) {
            steps = (int)Math.min((long)steps, this.energyStorage.getEnergy() / rf);
         }

         int i;
         for (i = 0; i < steps; i++) {
            if (!this.craftOneCycle()) {
               this.noRecipesWork = true;
               break;
            }
         }

         rf *= i;
         if (rf > 0) {
            this.energyStorage.consumeEnergy(rf);
         }
      }
   }

   private boolean craftOneCycle() {
      boolean craftedAtLeastOneThing = false;
      CrafterData data = (CrafterData)this.getData(CrafterModule.CRAFTER_DATA);

      for (CraftingRecipe craftingRecipe : data.recipes()) {
         if (this.craftOneItem(craftingRecipe)) {
            craftedAtLeastOneThing = true;
         }
      }

      return craftedAtLeastOneThing;
   }

   private boolean craftOneItem(CraftingRecipe craftingRecipe) {
      Recipe recipe = craftingRecipe.getCachedRecipe(this.level);
      if (recipe == null) {
         return false;
      } else {
         UndoableItemHandler undoHandler = new UndoableItemHandler(this.items);
         if (!this.testAndConsume(craftingRecipe, undoHandler)) {
            undoHandler.restore();
            return false;
         } else {
            ItemStack result = ItemStack.EMPTY;

            try {
               result = BaseRecipe.assemble(recipe, workInventory, this.level);
            } catch (RuntimeException var10) {
               Logging.logError("Problem with recipe!", var10);
            }

            CraftMode mode = craftingRecipe.getCraftMode();
            if (!result.isEmpty() && this.placeResult(mode, undoHandler, result)) {
               List<ItemStack> remaining = (List<ItemStack>)(recipe instanceof net.minecraft.world.item.crafting.CraftingRecipe cr
                  ? cr.getRemainingItems(workInventory)
                  : List.of());
               CraftMode remainingMode = mode == CraftMode.EXTC ? CraftMode.INT : mode;

               for (ItemStack s : remaining) {
                  if (!s.isEmpty() && !this.placeResult(remainingMode, undoHandler, s)) {
                     undoHandler.restore();
                     return false;
                  }
               }

               return true;
            } else {
               undoHandler.restore();
               return false;
            }
         }
      }
   }

   private boolean testAndConsume(CraftingRecipe craftingRecipe, UndoableItemHandler undoHandler) {
      int keep = craftingRecipe.getKeepOne() == KeepMode.KEEP ? 1 : 0;
      Recipe recipe = craftingRecipe.getCachedRecipe(this.level);
      int w = 3;
      int h = 3;
      List<Ingredient> ingredients;
      if (recipe instanceof ShapedRecipe shapedRecipe) {
         w = shapedRecipe.getWidth();
         h = shapedRecipe.getHeight();
         ingredients = shapedRecipe.getIngredients().stream().map(opt -> (Ingredient)opt.orElse(null)).toList();
      } else {
         ingredients = craftingRecipe.convertTo3x3Grid().stream().map(st -> st.isEmpty() ? null : Ingredient.of(st.getItem())).toList();
      }

      List<ItemStack> list = new ArrayList<>(9);

      for (int i = 0; i < 9; i++) {
         list.add(ItemStack.EMPTY);
      }

      for (int x = 0; x < w; x++) {
         for (int y = 0; y < h; y++) {
            int index = y * w + x;
            if (index < ingredients.size()) {
               Ingredient ingredient = ingredients.get(index);
               if (ingredient != null) {
                  for (int j = 0; j < 26; j++) {
                     int slotIdx = 10 + j;
                     ItemStack input = undoHandler.getStackInSlot(slotIdx);
                     if (!input.isEmpty() && input.getCount() > keep && ingredient.test(input)) {
                        undoHandler.remember(slotIdx);
                        ItemStack copy = input.split(1);
                        list.set(y * 3 + x, copy);
                        break;
                     }
                  }
               }
            }
         }
      }

      workInventory = CraftingInput.of(3, 3, list);
      return recipe.matches(workInventory, this.level);
   }

   private boolean placeResult(CraftMode mode, IItemHandlerModifiable undoHandler, ItemStack result) {
      int start;
      int stop;
      if (mode == CraftMode.INT) {
         start = 10;
         stop = 36;
      } else {
         start = 36;
         stop = 40;
      }

      ItemStack remaining = InventoryTools.insertItemRanged(undoHandler, result, start, stop, true);
      if (remaining.isEmpty()) {
         InventoryTools.insertItemRanged(undoHandler, result, start, stop, false);
         return true;
      } else {
         return false;
      }
   }

   private void rememberItems(Player player) {
      CrafterData data = (CrafterData)this.getData(CrafterModule.CRAFTER_DATA);
      List<ItemStack> ghostSlots = new ArrayList<>(data.ghostSlots());

      for (int i = 0; i < ghostSlots.size(); i++) {
         int slotIdx;
         if (i < 26) {
            slotIdx = i + 10;
         } else {
            slotIdx = i + 36 - 26;
         }

         if (!this.items.getStackInSlot(slotIdx).isEmpty()) {
            ItemStack stack = this.items.getStackInSlot(slotIdx).copy();
            stack.setCount(1);
            ghostSlots.set(i, stack);
         }
      }

      this.setData(CrafterModule.CRAFTER_DATA, data.withGhostSlots(ghostSlots));
      this.noRecipesWork = false;
      this.syncRememberedLayout(player);
   }

   private void forgetItems(Player player) {
      CrafterData data = (CrafterData)this.getData(CrafterModule.CRAFTER_DATA);
      List<ItemStack> ghostSlots = new ArrayList<>(data.ghostSlots());

      for (int i = 0; i < ghostSlots.size(); i++) {
         ghostSlots.set(i, ItemStack.EMPTY);
      }

      this.setData(CrafterModule.CRAFTER_DATA, data.withGhostSlots(ghostSlots));
      this.noRecipesWork = false;
      this.syncRememberedLayout(player);
   }

   private void syncRememberedLayout(Player player) {
      this.setChanged();
      this.markDirtyClient();
      if (player != null && player.containerMenu instanceof GenericContainer container) {
         container.forceBroadcast();
         container.broadcastChanges();
      }
   }

   public void loadAdditional(ValueInput tag) {
      super.loadAdditional(tag);
      this.energyStorage.load(tag, "energy");
      this.items.load(tag, "items");
      this.infusable.load(tag, "infusable");
      tag.read("crafterData", CrafterData.CODEC).ifPresent(data -> this.setData(CrafterModule.CRAFTER_DATA, data));
   }

   public void saveAdditional(ValueOutput tag) {
      super.saveAdditional(tag);
      this.energyStorage.save(tag, "energy");
      this.items.save(tag, "items");
      this.infusable.save(tag, "infusable");
      tag.store("crafterData", CrafterData.CODEC, (CrafterData)this.getData(CrafterModule.CRAFTER_DATA));
   }

   protected void applyImplicitComponents(DataComponentGetter input) {
      super.applyImplicitComponents(input);
      CrafterData data = (CrafterData)input.get(CrafterModule.ITEM_CRAFTER_DATA);
      if (data != null) {
         this.setData(CrafterModule.CRAFTER_DATA, data);
      }

      this.energyStorage.applyImplicitComponents((ItemEnergy)input.get(Registration.ITEM_ENERGY));
      this.items.applyImplicitComponents((ItemInventory)input.get(Registration.ITEM_INVENTORY));
      this.infusable.applyImplicitComponents((ItemInfusable)input.get(Registration.ITEM_INFUSABLE));
   }

   protected void collectImplicitComponents(Builder builder) {
      super.collectImplicitComponents(builder);
      builder.set(CrafterModule.ITEM_CRAFTER_DATA, (CrafterData)this.getData(CrafterModule.CRAFTER_DATA));
      this.energyStorage.collectImplicitComponents(builder);
      this.items.collectImplicitComponents(builder);
      this.infusable.collectImplicitComponents(builder);
   }

   public boolean isItemValidForSlot(int slot, @Nonnull ItemStack stack) {
      if (slot >= 0 && slot <= 9) {
         return false;
      } else {
         CrafterData data = (CrafterData)this.getData(CrafterModule.CRAFTER_DATA);
         List<ItemStack> ghostSlots = data.ghostSlots();
         if (slot >= 10 && slot < 36) {
            ItemStack ghostSlot = ghostSlots.get(slot - 10);
            if (!ghostSlot.isEmpty() && !ItemStack.isSameItem(ghostSlot, stack)) {
               return false;
            }

            ItemStack filterModule = this.items.getStackInSlot(40);
            if (!filterModule.isEmpty() && this.filterCache.get() != null) {
               return ((Predicate)this.filterCache.get()).test(stack);
            }
         } else if (slot >= 36 && slot < 40) {
            ItemStack ghostSlotx = ghostSlots.get(slot - 36 + 26);
            if (!ghostSlotx.isEmpty() && !ItemStack.isSameItem(ghostSlotx, stack)) {
               return false;
            }
         } else if (slot == 40) {
            return stack.getItem() instanceof FilterModuleItem;
         }

         return true;
      }
   }
}
