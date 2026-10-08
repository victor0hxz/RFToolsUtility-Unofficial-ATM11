package mcjty.rftoolsutility.modules.spawner.recipes;

import java.util.List;
import mcjty.lib.crafting.BaseRecipe;
import mcjty.rftoolsutility.modules.spawner.SpawnerModule;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

public class SpawnerRecipe implements BaseRecipe<CraftingInput> {
   private final Identifier id;
   private final SpawnerRecipes.MobSpawnAmount item1;
   private final SpawnerRecipes.MobSpawnAmount item2;
   private final SpawnerRecipes.MobSpawnAmount item3;
   private final int spawnRf;
   private final Identifier entity;

   public SpawnerRecipe(
      Identifier id,
      SpawnerRecipes.MobSpawnAmount item1,
      SpawnerRecipes.MobSpawnAmount item2,
      SpawnerRecipes.MobSpawnAmount item3,
      int spawnRf,
      Identifier entity
   ) {
      this.id = id;
      this.item1 = item1;
      this.item2 = item2;
      this.item3 = item3;
      this.spawnRf = spawnRf;
      this.entity = entity;
   }

   public Identifier getId() {
      return this.id;
   }

   public SpawnerRecipes.MobSpawnAmount getItem1() {
      return this.item1;
   }

   public SpawnerRecipes.MobSpawnAmount getItem2() {
      return this.item2;
   }

   public SpawnerRecipes.MobSpawnAmount getItem3() {
      return this.item3;
   }

   public int getSpawnRf() {
      return this.spawnRf;
   }

   public Identifier getEntity() {
      return this.entity;
   }

   public boolean matches(CraftingInput input, Level level) {
      return false;
   }

   public ItemStack assemble(CraftingInput input) {
      return ItemStack.EMPTY;
   }

   public boolean isSpecial() {
      return true;
   }

   public boolean showNotification() {
      return false;
   }

   public String group() {
      return "";
   }

   public RecipeSerializer<SpawnerRecipe> getSerializer() {
      return SpawnerModule.SPAWNER_SERIALIZER.get();
   }

   public RecipeType<SpawnerRecipe> getType() {
      return SpawnerModule.SPAWNER_RECIPE_TYPE.get();
   }

   public PlacementInfo placementInfo() {
      return PlacementInfo.NOT_PLACEABLE;
   }

   public List<RecipeDisplay> display() {
      return List.of();
   }

   public RecipeBookCategory recipeBookCategory() {
      return RecipeBookCategories.CRAFTING_MISC;
   }
}
