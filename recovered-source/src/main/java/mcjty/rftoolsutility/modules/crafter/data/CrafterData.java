package mcjty.rftoolsutility.modules.crafter.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import mcjty.lib.varia.ItemStackList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record CrafterData(List<ItemStack> ghostSlots, List<CraftingRecipe> recipes, SpeedMode speedMode) {
   public static final Codec<CrafterData> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("ghostSlots").forGetter(CrafterData::ghostSlots),
            CraftingRecipe.CODEC.listOf().fieldOf("recipes").forGetter(CrafterData::recipes),
            SpeedMode.CODEC.fieldOf("speedMode").forGetter(CrafterData::speedMode)
         )
         .apply(instance, CrafterData::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, CrafterData> STREAM_CODEC = StreamCodec.composite(
      ItemStack.OPTIONAL_LIST_STREAM_CODEC,
      CrafterData::ghostSlots,
      CraftingRecipe.STREAM_CODEC.apply(ByteBufCodecs.list()),
      CrafterData::recipes,
      SpeedMode.STREAM_CODEC,
      CrafterData::speedMode,
      CrafterData::new
   );

   public static CrafterData createDefault() {
      return new CrafterData(ItemStackList.create(30), new ArrayList<>(), SpeedMode.SLOW);
   }

   public CrafterData withGhostSlots(List<ItemStack> ghostSlots) {
      return new CrafterData(ghostSlots, this.recipes, this.speedMode);
   }

   public CrafterData withRecipes(List<CraftingRecipe> recipes) {
      return new CrafterData(this.ghostSlots, recipes, this.speedMode);
   }

   public CraftingRecipe getRecipeSafe(int index) {
      return index >= 0 && index < this.recipes.size() ? this.recipes.get(index) : new CraftingRecipe();
   }

   public CrafterData setRecipeSafe(int index, CraftingRecipe recipe) {
      if (index < 0) {
         return this;
      } else {
         List<CraftingRecipe> newRecipes = new ArrayList<>(this.recipes);

         while (newRecipes.size() <= index) {
            newRecipes.add(new CraftingRecipe());
         }

         newRecipes.set(index, recipe);
         return new CrafterData(this.ghostSlots, newRecipes, this.speedMode);
      }
   }

   public CrafterData withSpeedMode(SpeedMode speedMode) {
      return new CrafterData(this.ghostSlots, this.recipes, speedMode);
   }

   @Override
   public boolean equals(Object o) {
      return !(o instanceof CrafterData that)
         ? false
         : this.speedMode == that.speedMode && ItemStack.listMatches(this.ghostSlots, that.ghostSlots) && Objects.equals(this.recipes, that.recipes);
   }

   @Override
   public int hashCode() {
      return Objects.hash(ItemStack.hashStackList(this.ghostSlots), this.recipes, this.speedMode);
   }
}
