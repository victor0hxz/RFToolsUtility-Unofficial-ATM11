package mcjty.rftoolsutility.playerprops;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.GlobalPos;

public class FavoriteDestinationsProperties {
   private Set<GlobalPos> favoriteDestinations = new HashSet<>();
   public static final Codec<FavoriteDestinationsProperties> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(GlobalPos.CODEC.listOf().fieldOf("destinations").forGetter(FavoriteDestinationsProperties::getFavoriteDestinations))
         .apply(instance, FavoriteDestinationsProperties::new)
   );

   public FavoriteDestinationsProperties() {
   }

   public void copyFrom(FavoriteDestinationsProperties source) {
      this.favoriteDestinations = new HashSet<>(source.favoriteDestinations);
   }

   public boolean isDestinationFavorite(GlobalPos coordinate) {
      return this.favoriteDestinations.contains(coordinate);
   }

   public FavoriteDestinationsProperties(List<GlobalPos> favoriteDestinations) {
      this.favoriteDestinations = new HashSet<>(favoriteDestinations);
   }

   public List<GlobalPos> getFavoriteDestinations() {
      return List.copyOf(this.favoriteDestinations);
   }

   public void setDestinationFavorite(GlobalPos coordinate, boolean favorite) {
      if (favorite) {
         this.favoriteDestinations.add(coordinate);
      } else {
         this.favoriteDestinations.remove(coordinate);
      }
   }
}
