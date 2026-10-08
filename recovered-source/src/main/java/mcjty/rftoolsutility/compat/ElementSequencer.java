package mcjty.rftoolsutility.compat;

import io.netty.buffer.ByteBuf;
import mcjty.theoneprobe.api.IElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;

public class ElementSequencer implements IElement {
   private final long bits;
   private final int current;
   private final boolean large;

   public ElementSequencer(long bits, int current, boolean large) {
      this.bits = bits;
      this.current = current;
      this.large = large;
   }

   public ElementSequencer(ByteBuf buf) {
      this.bits = buf.readLong();
      this.current = buf.readInt();
      this.large = buf.readBoolean();
   }

   private int getSize() {
      return this.large ? 5 : 3;
   }

   public void render(GuiGraphicsExtractor graphics, int x, int y) {
      int size = this.getSize();

      for (int row = 0; row < 8; row++) {
         for (int col = 0; col < 8; col++) {
            int bit = row * 8 + col;
            if (this.large && bit == this.current) {
               graphics.fill(6 + x + col * size, y + row * size, 6 + x + col * size + size - 1, y + row * size + size - 1, -65536);
               graphics.fill(
                  6 + x + col * size + 1,
                  y + row * size + 1,
                  6 + x + col * size + size - 2,
                  y + row * size + size - 2,
                  (this.bits >> bit & 1L) == 1L ? -1 : -16777216
               );
            } else {
               graphics.fill(
                  6 + x + col * size, y + row * size, 6 + x + col * size + size - 1, y + row * size + size - 1, (this.bits >> bit & 1L) == 1L ? -1 : -16777216
               );
            }
         }
      }
   }

   public int getWidth() {
      return 12 + this.getSize() * 8;
   }

   public int getHeight() {
      return this.getSize() * 8;
   }

   public void toBytes(RegistryFriendlyByteBuf buf) {
      buf.writeLong(this.bits);
      buf.writeInt(this.current);
      buf.writeBoolean(this.large);
   }

   public Identifier getID() {
      return TheOneProbeSupport.ELEMENT_SEQUENCER;
   }
}
