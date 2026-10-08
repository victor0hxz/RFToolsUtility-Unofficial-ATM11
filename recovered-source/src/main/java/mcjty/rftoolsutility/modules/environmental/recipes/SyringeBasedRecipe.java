package mcjty.rftoolsutility.modules.environmental.recipes;

import java.util.List;
import javax.annotation.Nonnull;
import mcjty.rftoolsutility.modules.environmental.EnvironmentalModule;
import mcjty.rftoolsutility.modules.spawner.items.SyringeItem;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo;
import net.minecraft.world.item.crafting.Recipe.CommonInfo;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

public class SyringeBasedRecipe implements CraftingRecipe {
   private final Identifier mobId;
   private final int syringeIndex;
   private final String groupName;
   private final ItemStack resultStack;
   private final ShapedRecipePattern pattern;
   private final ShapedRecipe delegate;

   public SyringeBasedRecipe(String group, ShapedRecipePattern pattern, ItemStack result, Identifier mobId, int syringeIndex) {
      this.groupName = group;
      this.resultStack = result.copy();
      this.mobId = mobId;
      this.syringeIndex = syringeIndex;
      this.pattern = pattern;
      this.delegate = new ShapedRecipe(
         new CommonInfo(false), new CraftingBookInfo(CraftingBookCategory.MISC, group), pattern, ItemStackTemplate.fromNonEmptyStack(result)
      );
   }

   public String groupName() {
      return this.groupName;
   }

   public ItemStack resultStack() {
      return this.resultStack.copy();
   }

   public ShapedRecipePattern pattern() {
      return this.pattern;
   }

   public Identifier getMobId() {
      return this.mobId;
   }

   public int getSyringeIndex() {
      return this.syringeIndex;
   }

   public boolean matches(@Nonnull CraftingInput inv, @Nonnull Level level) {
      if (!this.delegate.matches(inv, level)) {
         return false;
      } else {
         for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof SyringeItem) {
               Identifier mob = SyringeItem.getMobId(stack);
               if (mob == null || !mob.equals(this.mobId) || SyringeItem.getLevel(stack) < 100) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   public ItemStack assemble(CraftingInput input) {
      return this.resultStack.copy();
   }

   public boolean showNotification() {
      return this.delegate.showNotification();
   }

   public String group() {
      return this.groupName;
   }

   public RecipeSerializer<SyringeBasedRecipe> getSerializer() {
      return EnvironmentalModule.SYRINGE_SERIALIZER.get();
   }

   public PlacementInfo placementInfo() {
      return this.delegate.placementInfo();
   }

   public List<RecipeDisplay> display() {
      return this.delegate.display();
   }

   public RecipeBookCategory recipeBookCategory() {
      return this.delegate.recipeBookCategory();
   }

   public CraftingBookCategory category() {
      return CraftingBookCategory.MISC;
   }
}
