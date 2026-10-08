package mcjty.rftoolsutility.modules.crafter.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import mcjty.lib.varia.InventoryTools;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.CraftingInput.Positioned;
import net.minecraft.world.level.Level;

public class CraftingRecipe {
   private Positioned inv = CraftingInput.ofPositioned(3, 3, createList());
   private ItemStack result = ItemStack.EMPTY;
   private boolean recipePresent = false;
   private Recipe recipe = null;
   private KeepMode keepOne = KeepMode.ALL;
   private CraftMode craftMode = CraftMode.EXT;
   public static final Codec<CraftingRecipe> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("inv").forGetter(CraftingRecipe::convertTo3x3Grid),
            ItemStack.OPTIONAL_CODEC.fieldOf("result").forGetter(o -> o.result),
            KeepMode.CODEC.fieldOf("keepOne").forGetter(CraftingRecipe::getKeepOne),
            CraftMode.CODEC.fieldOf("craftMode").forGetter(CraftingRecipe::getCraftMode)
         )
         .apply(instance, (itemStacks, itemStack, keepMode, craftMode) -> {
            CraftingRecipe recipe = new CraftingRecipe();
            recipe.inv = CraftingInput.ofPositioned(3, 3, convertTo3x3List(itemStacks));
            recipe.result = itemStack;
            recipe.keepOne = keepMode;
            recipe.craftMode = craftMode;
            recipe.recipePresent = false;
            return recipe;
         })
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, CraftingRecipe> STREAM_CODEC = StreamCodec.composite(
      ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()),
      CraftingRecipe::convertTo3x3Grid,
      ItemStack.OPTIONAL_STREAM_CODEC,
      o -> o.result,
      KeepMode.STREAM_CODEC,
      CraftingRecipe::getKeepOne,
      CraftMode.STREAM_CODEC,
      CraftingRecipe::getCraftMode,
      (itemStacks, itemStack, keepMode, craftMode) -> {
         CraftingRecipe recipe = new CraftingRecipe();
         recipe.inv = CraftingInput.ofPositioned(3, 3, convertTo3x3List(itemStacks));
         recipe.result = itemStack;
         recipe.keepOne = keepMode;
         recipe.craftMode = craftMode;
         recipe.recipePresent = false;
         return recipe;
      }
   );
   private List<CraftingRecipe.CompressedIngredient> compressedIngredients = null;

   private static List<ItemStack> createList() {
      List<ItemStack> list = new ArrayList<>();

      for (int i = 0; i < 9; i++) {
         list.add(ItemStack.EMPTY);
      }

      return list;
   }

   public CraftingRecipe copy() {
      CraftingRecipe recipe = new CraftingRecipe();
      recipe.inv = CraftingInput.ofPositioned(3, 3, convertTo3x3List(this.inv.input().items()));
      recipe.result = this.result.copy();
      recipe.keepOne = this.keepOne;
      recipe.craftMode = this.craftMode;
      recipe.recipePresent = false;
      return recipe;
   }

   public List<ItemStack> convertTo3x3Grid() {
      List<ItemStack> list = new ArrayList<>(9);

      for (int i = 0; i < 9; i++) {
         list.add(ItemStack.EMPTY);
      }

      int left = this.inv.left();
      int top = this.inv.top();
      int size = this.inv.input().size();

      for (int x = 0; x < this.inv.input().width(); x++) {
         for (int y = 0; y < this.inv.input().height(); y++) {
            int idx = y * this.inv.input().width() + x;
            if (idx < size) {
               int gridIdx = (y + top) * 3 + x + left;
               list.set(gridIdx, this.inv.input().getItem(idx));
            }
         }
      }

      return list;
   }

   private static List<ItemStack> convertTo3x3List(List<ItemStack> list) {
      if (list.size() == 9) {
         return list;
      } else {
         List<ItemStack> newList = new ArrayList<>();

         for (int i = 0; i < 9; i++) {
            newList.add(i < list.size() ? list.get(i) : ItemStack.EMPTY);
         }

         return newList;
      }
   }

   public List<CraftingRecipe.CompressedIngredient> getCompressedIngredients() {
      if (this.compressedIngredients == null) {
         this.compressedIngredients = new ArrayList<>();

         for (int i = 0; i < this.inv.input().size(); i++) {
            ItemStack stack = this.inv.input().getItem(i);
            if (!stack.isEmpty()) {
               boolean found = false;

               for (CraftingRecipe.CompressedIngredient ingredient : this.compressedIngredients) {
                  if (InventoryTools.isItemStackConsideredEqual(stack, ingredient.getStack())) {
                     ingredient.getStack().grow(stack.getCount());
                     ingredient.getGridDistribution()[i] += stack.getCount();
                     found = true;
                     break;
                  }
               }

               if (!found) {
                  CraftingRecipe.CompressedIngredient ingredientx = new CraftingRecipe.CompressedIngredient(stack.copy());
                  ingredientx.getGridDistribution()[i] += stack.getCount();
                  this.compressedIngredients.add(ingredientx);
               }
            }
         }
      }

      return this.compressedIngredients;
   }

   public static Recipe findRecipe(Level world, CraftingInput inv) {
      if (world != null && !world.isClientSide() && world.getServer() != null) {
         RecipeManager recipeManager = world.getServer().getRecipeManager();
         Optional<RecipeHolder<net.minecraft.world.item.crafting.CraftingRecipe>> recipeFor = recipeManager.getRecipeFor(RecipeType.CRAFTING, inv, world);
         return recipeFor.isPresent() ? recipeFor.get().value() : null;
      } else {
         return null;
      }
   }

   public void setRecipe(ItemStack[] items, ItemStack result) {
      this.inv = CraftingInput.ofPositioned(3, 3, Arrays.asList(items));
      this.result = result;
      this.recipePresent = false;
   }

   public Positioned getInventory() {
      return this.inv;
   }

   public void setResult(ItemStack result) {
      this.result = result;
   }

   public ItemStack getResult() {
      return this.result;
   }

   public Recipe getCachedRecipe(Level world) {
      if (!this.recipePresent) {
         this.recipePresent = true;
         this.recipe = findRecipe(world, this.inv.input());
         this.compressedIngredients = null;
      }

      return this.recipe;
   }

   public KeepMode getKeepOne() {
      return this.keepOne;
   }

   public void setKeepOne(KeepMode keepOne) {
      this.keepOne = keepOne;
   }

   public CraftMode getCraftMode() {
      return this.craftMode;
   }

   public void setCraftMode(CraftMode craftMode) {
      this.craftMode = craftMode;
   }

   @Override
   public boolean equals(Object o) {
      return !(o instanceof CraftingRecipe that)
         ? false
         : Objects.equals(this.inv, that.inv)
            && ItemStack.isSameItemSameComponents(this.result, that.result)
            && this.keepOne == that.keepOne
            && this.craftMode == that.craftMode;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.inv, ItemStack.hashItemAndComponents(this.result), this.keepOne, this.craftMode);
   }

   public static class CompressedIngredient {
      private final ItemStack stack;
      private final int[] gridDistribution = new int[9];

      public CompressedIngredient(ItemStack stack) {
         this.stack = stack;
         Arrays.fill(this.gridDistribution, 0);
      }

      public ItemStack getStack() {
         return this.stack;
      }

      public int[] getGridDistribution() {
         return this.gridDistribution;
      }
   }
}
