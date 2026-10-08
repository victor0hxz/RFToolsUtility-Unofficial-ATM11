package mcjty.rftoolsutility.modules.spawner.recipes;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mcjty.lib.varia.TagTools;
import mcjty.rftoolsutility.modules.spawner.SpawnerConfiguration;
import mcjty.rftoolsutility.modules.spawner.SpawnerModule;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

public class SpawnerRecipes {
   private static final Map<Identifier, SpawnerRecipes.MobData> mobData = new HashMap<>();

   public static SpawnerRecipes.MobData getMobData(Level world, Identifier id) {
      if (mobData.isEmpty()) {
         loadRecipes(world);
      }

      SpawnerRecipes.MobData data = mobData.get(id);
      return data == null ? null : data;
   }

   private static void loadRecipes(Level world) {
      mobData.clear();

      for (RecipeHolder<SpawnerRecipe> recipeHolder : world.getServer()
         .getRecipeManager()
         .getRecipes()
         .stream()
         .filter(h -> h.value().getType() == SpawnerModule.SPAWNER_RECIPE_TYPE.get())
         .map(h -> (RecipeHolder)h)
         .toList()) {
         SpawnerRecipe recipe = (SpawnerRecipe)recipeHolder.value();
         mobData.put(
            recipe.getEntity(),
            SpawnerRecipes.MobData.create().item1(recipe.getItem1()).item2(recipe.getItem2()).item3(recipe.getItem3()).spawnRf(recipe.getSpawnRf())
         );
      }
   }

   public static class MobData {
      private SpawnerRecipes.MobSpawnAmount item1;
      private SpawnerRecipes.MobSpawnAmount item2;
      private SpawnerRecipes.MobSpawnAmount item3;
      private int spawnRf;

      public static SpawnerRecipes.MobData create() {
         return new SpawnerRecipes.MobData();
      }

      public SpawnerRecipes.MobData item1(SpawnerRecipes.MobSpawnAmount item1) {
         this.item1 = item1;
         return this;
      }

      public SpawnerRecipes.MobData item2(SpawnerRecipes.MobSpawnAmount item2) {
         this.item2 = item2;
         return this;
      }

      public SpawnerRecipes.MobData item3(SpawnerRecipes.MobSpawnAmount item3) {
         this.item3 = item3;
         return this;
      }

      public SpawnerRecipes.MobData spawnRf(int spawnRf) {
         this.spawnRf = spawnRf;
         return this;
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

      public SpawnerRecipes.MobSpawnAmount getItem(int index) {
         switch (index) {
            case 0:
               return this.item1;
            case 1:
               return this.item2;
            case 2:
               return this.item3;
            default:
               throw new IllegalStateException("Bad index for MobData.getItem()!");
         }
      }

      public int getSpawnRf() {
         return this.spawnRf;
      }
   }

   public static class MobSpawnAmount {
      private static final Codec<Optional<Ingredient>> OPTIONAL_OR_EMPTY_INGREDIENT_CODEC = Codec.either(Ingredient.CODEC, Codec.list(Codec.STRING))
         .xmap(
            either -> (Optional)either.map(Optional::of, ignored -> Optional.empty()),
            optional -> optional.isPresent() ? Either.left((Ingredient)optional.get()) : Either.right(List.of())
         );
      public static final Codec<SpawnerRecipes.MobSpawnAmount> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(
               OPTIONAL_OR_EMPTY_INGREDIENT_CODEC.fieldOf("object").forGetter(SpawnerRecipes.MobSpawnAmount::getObject),
               Codec.FLOAT.fieldOf("amount").forGetter(SpawnerRecipes.MobSpawnAmount::getAmount)
            )
            .apply(instance, SpawnerRecipes.MobSpawnAmount::new)
      );
      public static final StreamCodec<RegistryFriendlyByteBuf, SpawnerRecipes.MobSpawnAmount> STREAM_CODEC = StreamCodec.composite(
         Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC, d -> d.object, ByteBufCodecs.FLOAT, d -> d.amount, SpawnerRecipes.MobSpawnAmount::new
      );
      private final Optional<Ingredient> object;
      private final float amount;

      public MobSpawnAmount(Optional<Ingredient> object, float amount) {
         this.object = object;
         this.amount = amount;
      }

      public static SpawnerRecipes.MobSpawnAmount create(Ingredient object, float amount) {
         return new SpawnerRecipes.MobSpawnAmount(Optional.of(object), amount);
      }

      public static SpawnerRecipes.MobSpawnAmount living(float amount) {
         return new SpawnerRecipes.MobSpawnAmount(Optional.empty(), amount);
      }

      public Optional<Ingredient> getObject() {
         return this.object;
      }

      public float getAmount() {
         return this.amount;
      }

      public Float match(ItemStack stack) {
         if (this.object.isEmpty()) {
            Item item = stack.getItem();
            Collection<TagKey<Item>> tags = TagTools.getTags(item);
            if (tags.contains(SpawnerConfiguration.TAG_HIGHYIELD)) {
               return 1.5F;
            } else if (tags.contains(SpawnerConfiguration.TAG_AVERAGEYIELD)) {
               return 1.0F;
            } else {
               return tags.contains(SpawnerConfiguration.TAG_LOWYIELD) ? 0.5F : 0.0F;
            }
         } else {
            return this.object.get().test(stack) ? 1.0F : null;
         }
      }
   }
}
