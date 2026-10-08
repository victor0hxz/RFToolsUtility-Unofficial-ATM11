package mcjty.rftoolsutility.modules.crafter.client;

import java.util.List;
import javax.annotation.Nonnull;
import mcjty.lib.base.StyleConfig;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.client.RenderHelper;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.widgets.BlockRender;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.gui.widgets.WidgetList;
import mcjty.lib.gui.widgets.Widgets;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsutility.modules.crafter.CrafterModule;
import mcjty.rftoolsutility.modules.crafter.blocks.CrafterBaseTE;
import mcjty.rftoolsutility.modules.crafter.blocks.CrafterContainer;
import mcjty.rftoolsutility.modules.crafter.data.CraftingRecipe;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class GuiCrafter extends GenericGuiContainer<CrafterBaseTE, CrafterContainer> {
   private EnergyBar energyBar;
   private WidgetList recipeList;
   private Button applyButton;
   private static final Identifier iconGuiElements = Identifier.fromNamespaceAndPath("rftoolsbase", "textures/gui/guielements.png");

   public GuiCrafter(CrafterContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, ((BaseBlock)CrafterModule.CRAFTER1.block().get()).getManualEntry());
   }

   public static void register(RegisterMenuScreensEvent event) {
      event.register(CrafterModule.CONTAINER_CRAFTER.get(), GuiCrafter::new);
   }

   public void init() {
      this.window = new Window(this, this.getBE(), Identifier.fromNamespaceAndPath("rftoolsutility", "gui/crafter.gui"));
      super.init();
      this.initializeFields();
      this.window.event("select", (source, params) -> this.selectRecipe());
   }

   private void initializeFields() {
      this.recipeList = (WidgetList)this.window.findChild("recipes");
      this.energyBar = (EnergyBar)this.window.findChild("energybar");
      this.applyButton = (Button)this.window.findChild("apply");
   }

   private void updateFields() {
      if (this.window != null) {
         CrafterBaseTE tileEntity = (CrafterBaseTE)this.getBE();
         this.recipeList.selected(tileEntity.getSelected());
         ((ImageChoiceLabel)this.window.findChild("redstone")).setCurrentChoice(tileEntity.getRSMode().ordinal());
         this.populateList();
         this.updateEnergyBar(this.energyBar);
      }
   }

   private void populateList() {
      this.recipeList.removeChildren();
      CrafterBaseTE tileEntity = (CrafterBaseTE)this.getBE();

      for (int i = 0; i < tileEntity.getSupportedRecipes(); i++) {
         CraftingRecipe recipe = tileEntity.getRecipe(i);
         this.addRecipeLine(recipe.getResult());
      }
   }

   private void addRecipeLine(ItemStack craftingResult) {
      String readableName = Tools.getReadableName(craftingResult);
      int color = StyleConfig.colorTextInListNormal;
      if (craftingResult.isEmpty()) {
         readableName = "<no recipe>";
         color = -11513776;
      }

      Panel panel = (Panel)Widgets.horizontal()
         .children(
            new Widget[]{
               new BlockRender().renderItem(craftingResult).tooltips(new String[]{"Double click to edit this recipe"}),
               ((Label)((Label)((Label)Widgets.label(readableName).color(color)).horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)).dynamic(true))
                  .tooltips(new String[]{"Double click to edit this recipe"})
            }
         );
      this.recipeList.children(new Widget[]{panel});
   }

   private void selectRecipe() {
      int selected = this.recipeList.getSelected();
      this.setValue(CrafterBaseTE.SELECTED, selected);
   }

   private void updateButtons() {
      if (this.recipeList != null) {
         boolean selected = this.recipeList.getSelected() != -1;
         this.applyButton.enabled(selected);
      } else {
         this.applyButton.enabled(false);
      }
   }

   public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      if (this.window != null) {
         this.updateFields();
         this.updateButtons();
         this.drawWindow(graphics, partialTicks, mouseX, mouseY);
         this.drawGhostSlots(graphics);
      }
   }

   private void drawGhostSlots(GuiGraphicsExtractor graphics) {
      CrafterBaseTE tileEntity = (CrafterBaseTE)this.getBE();
      if (tileEntity != null) {
         List<ItemStack> ghostSlots = tileEntity.getGhostSlots();

         for (int i = 0; i < ghostSlots.size(); i++) {
            ItemStack stack = ghostSlots.get(i);
            if (!stack.isEmpty()) {
               int slotIdx = i < 26 ? i + 10 : i + 36 - 26;
               Slot slot = ((CrafterContainer)this.menu).getSlot(slotIdx);
               if (!slot.hasItem()) {
                  RenderHelper.renderAndDecorateItem(graphics, stack, this.leftPos + slot.x, this.topPos + slot.y);
               }
            }
         }
      }
   }
}
