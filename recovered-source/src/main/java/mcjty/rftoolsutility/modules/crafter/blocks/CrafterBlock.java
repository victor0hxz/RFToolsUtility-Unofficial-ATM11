package mcjty.rftoolsutility.modules.crafter.blocks;

import java.util.Collection;
import java.util.List;
import mcjty.lib.api.container.ItemInventory;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.crafting.IComponentsToPreserve;
import mcjty.lib.setup.Registration;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsutility.compat.RFToolsUtilityTOPDriver;
import mcjty.rftoolsutility.modules.crafter.CrafterModule;
import mcjty.rftoolsutility.modules.crafter.data.CrafterData;
import mcjty.rftoolsutility.modules.crafter.data.CraftingRecipe;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;

public class CrafterBlock extends BaseBlock implements IComponentsToPreserve {
   public CrafterBlock(BlockEntitySupplier<BlockEntity> tileEntitySupplier) {
      super(
         new BlockBuilder()
            .topDriver(RFToolsUtilityTOPDriver.DRIVER)
            .manualEntry(ManualHelper.create("rftoolsbase:machines/crafter"))
            .infusable()
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsutility.shiftmessage")})
            .infoShift(
               new InfoLine[]{
                  TooltipBuilder.header(),
                  TooltipBuilder.gold(),
                  TooltipBuilder.parameter("contents", stack -> Integer.toString(countItems(stack))),
                  TooltipBuilder.parameter("recipes", stack -> Integer.toString(countRecipes(stack)))
               }
            )
            .tileEntitySupplier(tileEntitySupplier)
      );
   }

   private static int countRecipes(ItemStack itemStack) {
      CrafterData data = (CrafterData)itemStack.get(CrafterModule.ITEM_CRAFTER_DATA);
      if (data == null) {
         return 0;
      } else {
         int rc = 0;

         for (CraftingRecipe recipe : data.recipes()) {
            if (!recipe.getResult().isEmpty()) {
               rc++;
            }
         }

         return rc;
      }
   }

   private static int countItems(ItemStack itemStack) {
      ItemInventory items = (ItemInventory)itemStack.get(Registration.ITEM_INVENTORY);
      if (items == null) {
         return 0;
      } else {
         int rc = 0;

         for (int i = 0; i < items.items().size(); i++) {
            ItemStack stack = (ItemStack)items.items().get(i);
            if (!stack.isEmpty()) {
               rc++;
            }
         }

         return rc;
      }
   }

   public Collection<DataComponentType<?>> getComponentsToPreserve() {
      return List.of(
         (DataComponentType<?>)Registration.ITEM_INFUSABLE.get(),
         (DataComponentType<?>)Registration.ITEM_ENERGY.get(),
         (DataComponentType<?>)Registration.ITEM_INVENTORY.get(),
         (DataComponentType<?>)CrafterModule.ITEM_CRAFTER_DATA.get()
      );
   }
}
