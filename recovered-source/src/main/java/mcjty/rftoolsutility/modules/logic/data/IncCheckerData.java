package mcjty.rftoolsutility.modules.logic.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import mcjty.rftoolsutility.modules.logic.blocks.InvCheckerDamageMode;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public record IncCheckerData(int amount, int slot, InvCheckerDamageMode useDamage, TagKey<Item> tag) {
   public static final Codec<IncCheckerData> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.INT.fieldOf("amount").forGetter(IncCheckerData::amount),
            Codec.INT.fieldOf("slot").forGetter(IncCheckerData::slot),
            InvCheckerDamageMode.CODEC.fieldOf("useDamage").forGetter(IncCheckerData::useDamage),
            TagKey.codec(Registries.ITEM).optionalFieldOf("tag").forGetter(data -> Optional.ofNullable(data.tag))
         )
         .apply(instance, (amount, slot, useDamage, tag) -> new IncCheckerData(amount, slot, useDamage, (TagKey<Item>)tag.orElse(null)))
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, IncCheckerData> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.INT, d -> d.amount, ByteBufCodecs.INT, d -> d.slot, InvCheckerDamageMode.STREAM_CODEC, d -> d.useDamage, StreamCodec.of((buf, tag) -> {
         if (tag != null) {
            buf.writeBoolean(true);
            Identifier.STREAM_CODEC.encode(buf, tag.location());
         } else {
            buf.writeBoolean(false);
         }
      }, buf -> buf.readBoolean() ? TagKey.create(Registries.ITEM, (Identifier)Identifier.STREAM_CODEC.decode(buf)) : null), d -> d.tag, IncCheckerData::new
   );

   public static IncCheckerData createDefault() {
      return new IncCheckerData(1, 0, InvCheckerDamageMode.DMG_IGNORE, null);
   }

   public IncCheckerData withAmount(int amount) {
      return new IncCheckerData(amount, this.slot, this.useDamage, this.tag);
   }

   public IncCheckerData withSlot(int slot) {
      return new IncCheckerData(this.amount, slot, this.useDamage, this.tag);
   }

   public IncCheckerData withUseDamage(InvCheckerDamageMode useDamage) {
      return new IncCheckerData(this.amount, this.slot, useDamage, this.tag);
   }

   public IncCheckerData withTag(TagKey<Item> tag) {
      return new IncCheckerData(this.amount, this.slot, this.useDamage, tag);
   }
}
