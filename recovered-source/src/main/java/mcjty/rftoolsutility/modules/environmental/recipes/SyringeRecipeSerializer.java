package mcjty.rftoolsutility.modules.environmental.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

public final class SyringeRecipeSerializer {
   public static final MapCodec<SyringeBasedRecipe> CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(
            Codec.STRING.fieldOf("group").forGetter(SyringeBasedRecipe::groupName),
            ShapedRecipePattern.MAP_CODEC.fieldOf("pattern").forGetter(SyringeBasedRecipe::pattern),
            ItemStack.CODEC.fieldOf("result").forGetter(SyringeBasedRecipe::resultStack),
            Identifier.CODEC.fieldOf("mob").forGetter(SyringeBasedRecipe::getMobId),
            Codec.INT.fieldOf("syringe").forGetter(SyringeBasedRecipe::getSyringeIndex)
         )
         .apply(instance, SyringeBasedRecipe::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, SyringeBasedRecipe> STREAM_CODEC = StreamCodec.of(
      (buf, r) -> {
         buf.writeUtf(r.groupName());
         ShapedRecipePattern.STREAM_CODEC.encode(buf, r.pattern());
         ItemStack.STREAM_CODEC.encode(buf, r.resultStack());
         Identifier.STREAM_CODEC.encode(buf, r.getMobId());
         buf.writeInt(r.getSyringeIndex());
      },
      buf -> new SyringeBasedRecipe(
         buf.readUtf(32767),
         (ShapedRecipePattern)ShapedRecipePattern.STREAM_CODEC.decode(buf),
         (ItemStack)ItemStack.STREAM_CODEC.decode(buf),
         (Identifier)Identifier.STREAM_CODEC.decode(buf),
         buf.readInt()
      )
   );
   public static final RecipeSerializer<SyringeBasedRecipe> SERIALIZER = new RecipeSerializer(CODEC, STREAM_CODEC);

   private SyringeRecipeSerializer() {
   }
}
