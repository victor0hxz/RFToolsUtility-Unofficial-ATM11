package mcjty.rftoolsutility.modules.screen.modulesclient.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import mcjty.lib.gui.events.BlockRenderEvent;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.widgets.BlockRender;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.ColorSelector;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.gui.widgets.Widgets;
import mcjty.rftoolsbase.api.screens.BarMode;
import mcjty.rftoolsbase.api.screens.FormatStyle;
import mcjty.rftoolsbase.api.screens.IModuleGuiBuilder;
import mcjty.rftoolsbase.api.screens.IModuleGuiBuilder.Choice;
import mcjty.rftoolsutility.modules.screen.IModuleGuiChanged;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class ScreenModuleGuiBuilder implements IModuleGuiBuilder {
   private Minecraft mc;
   private Screen gui;
   private ItemStack module;
   private IModuleGuiChanged moduleGuiChanged;
   private Panel panel;
   private List<Widget<?>> row = new ArrayList<>();

   public ScreenModuleGuiBuilder(Minecraft mc, Screen gui, ItemStack module, IModuleGuiChanged moduleGuiChanged) {
      this.gui = gui;
      this.mc = mc;
      this.moduleGuiChanged = moduleGuiChanged;
      this.module = module;
      this.panel = Widgets.vertical(3, 1);
   }

   public ItemStack getCurrentModule() {
      return this.module;
   }

   public Level getWorld() {
      return this.mc.player.level();
   }

   public Panel build() {
      this.nl();
      return this.panel;
   }

   public ScreenModuleGuiBuilder label(String text) {
      this.row.add(Widgets.label(text));
      return this;
   }

   public ScreenModuleGuiBuilder leftLabel(String text) {
      this.row.add(Widgets.label(text).horizontalAlignment(HorizontalAlignment.ALIGN_LEFT));
      return this;
   }

   public IModuleGuiBuilder text(BiConsumer<ItemStack, String> setter, Function<ItemStack, String> getter, String... tooltip) {
      TextField textField = ((TextField)((TextField)new TextField().desiredHeight(15)).tooltips(tooltip)).event(newText -> {
         setter.accept(this.module, newText);
         this.moduleGuiChanged.updateData();
      });
      this.row.add(textField);
      if (this.module != null) {
         textField.text(getter.apply(this.module));
      }

      return this;
   }

   public ScreenModuleGuiBuilder integer(BiConsumer<ItemStack, Integer> setter, Function<ItemStack, Integer> getter, String... tooltip) {
      TextField textField = ((TextField)((TextField)new TextField().desiredHeight(15)).tooltips(tooltip)).event(newText -> {
         int value;
         try {
            value = Integer.parseInt(newText);
         } catch (NumberFormatException var5) {
            value = 0;
         }

         setter.accept(this.module, value);
         this.moduleGuiChanged.updateData();
      });
      this.row.add(textField);
      if (this.module != null) {
         textField.text(Integer.toString(getter.apply(this.module)));
      }

      return this;
   }

   public IModuleGuiBuilder toggle(BiConsumer<ItemStack, Boolean> setter, Function<ItemStack, Boolean> getter, String label, String... tooltip) {
      ToggleButton toggleButton = ((ToggleButton)((ToggleButton)((ToggleButton)new ToggleButton().text(label)).tooltips(tooltip)).desiredHeight(14))
         .checkMarker(true);
      toggleButton.event(() -> {
         setter.accept(this.module, toggleButton.isPressed());
         this.moduleGuiChanged.updateData();
      });
      this.row.add(toggleButton);
      if (this.module != null) {
         toggleButton.pressed(getter.apply(this.module));
      }

      return this;
   }

   public ScreenModuleGuiBuilder toggleNegative(BiConsumer<ItemStack, Boolean> setter, Function<ItemStack, Boolean> getter, String label, String... tooltip) {
      ToggleButton toggleButton = ((ToggleButton)((ToggleButton)((ToggleButton)((ToggleButton)new ToggleButton().text(label)).tooltips(tooltip))
               .desiredHeight(14))
            .desiredWidth(36))
         .checkMarker(true);
      toggleButton.event(() -> {
         setter.accept(this.module, !toggleButton.isPressed());
         this.moduleGuiChanged.updateData();
      });
      this.row.add(toggleButton);
      if (this.module != null) {
         toggleButton.pressed(!getter.apply(this.module));
      } else {
         toggleButton.pressed(true);
      }

      return this;
   }

   public IModuleGuiBuilder color(BiConsumer<ItemStack, Integer> setter, Function<ItemStack, Integer> getter, String... tooltip) {
      ColorSelector colorSelector = ((ColorSelector)((ColorSelector)((ColorSelector)new ColorSelector().tooltips(tooltip)).desiredWidth(20)).desiredHeight(14))
         .event(newColor -> {
            setter.accept(this.module, newColor);
            this.moduleGuiChanged.updateData();
         });
      this.row.add(colorSelector);
      if (this.module != null) {
         int currentColor = getter.apply(this.module);
         if (currentColor != 0) {
            colorSelector.currentColor(currentColor);
         }
      }

      return this;
   }

   public IModuleGuiBuilder choices(BiConsumer<ItemStack, String> setter, Function<ItemStack, String> getter, String tooltip, String... choices) {
      ChoiceLabel choiceLabel = (ChoiceLabel)((ChoiceLabel)((ChoiceLabel)new ChoiceLabel().tooltips(new String[]{tooltip})).desiredWidth(50)).desiredHeight(14);

      for (String s : choices) {
         choiceLabel.choices(new String[]{s});
      }

      choiceLabel.event(newChoice -> {
         setter.accept(this.module, newChoice);
         this.moduleGuiChanged.updateData();
      });
      this.row.add(choiceLabel);
      if (this.module != null) {
         String currentChoice = getter.apply(this.module);
         if (!currentChoice.isEmpty()) {
            choiceLabel.choice(currentChoice);
         }
      }

      return this;
   }

   public IModuleGuiBuilder choices(BiConsumer<ItemStack, Integer> setter, Function<ItemStack, Integer> getter, Choice... choices) {
      ChoiceLabel choiceLabel = (ChoiceLabel)((ChoiceLabel)new ChoiceLabel().desiredWidth(50)).desiredHeight(14);
      Map<String, Integer> choicesMap = new HashMap<>(choices.length);

      for (int i = 0; i < choices.length; i++) {
         Choice c = choices[i];
         String name = c.getName();
         choicesMap.put(name, i);
         choiceLabel.choices(new String[]{name});
         choiceLabel.choiceTooltip(name, c.getTooltips());
      }

      choiceLabel.event(newChoice -> {
         setter.accept(this.module, choicesMap.get(newChoice));
         this.moduleGuiChanged.updateData();
      });
      this.row.add(choiceLabel);
      if (this.getCurrentModule() != null) {
         int currentChoice = getter.apply(this.getCurrentModule());
         if (currentChoice < choices.length && currentChoice >= 0) {
            choiceLabel.choice(choices[currentChoice].getName());
         }
      }

      return this;
   }

   public ScreenModuleGuiBuilder format(BiConsumer<ItemStack, FormatStyle> setter, Function<ItemStack, FormatStyle> getter) {
      ChoiceLabel label = setupFormatCombo(this.mc, this.gui, setter, getter, this.module, this.moduleGuiChanged);
      this.row.add(label);
      return this;
   }

   public ScreenModuleGuiBuilder mode(BiConsumer<ItemStack, BarMode> setter, Function<ItemStack, BarMode> getter, String componentName) {
      ChoiceLabel label = setupModeCombo(this.mc, this.gui, setter, getter, componentName, this.module, this.moduleGuiChanged);
      this.row.add(label);
      return this;
   }

   public IModuleGuiBuilder block(Function<ItemStack, GlobalPos> getter, Function<ItemStack, String> nameGetter) {
      String monitoring;
      if (this.module == null) {
         monitoring = "<not set>";
      } else {
         GlobalPos pos = getter.apply(this.module);
         if (pos != null) {
            Level world = this.getWorld();
            if (pos.dimension().equals(world.dimension())) {
               BlockPos p = pos.pos();
               int x = p.getX();
               int y = p.getY();
               int z = p.getZ();
               monitoring = nameGetter.apply(this.module);
               Block block = world.getBlockState(p).getBlock();
               this.row.add(new BlockRender().renderItem(block).desiredWidth(20));
               this.row.add(((Label)Widgets.label(x + "," + y + "," + z).horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)).desiredWidth(150));
            } else {
               monitoring = "<unreachable>";
            }
         } else {
            monitoring = "<not set>";
         }
      }

      this.row.add(Widgets.label(monitoring));
      return this;
   }

   public IModuleGuiBuilder ghostStack(final BiConsumer<ItemStack, ItemStack> setter, Function<ItemStack, ItemStack> getter) {
      ItemStack stack = getter.apply(this.module);
      final BlockRender blockRender = (BlockRender)((BlockRender)((BlockRender)((BlockRender)new BlockRender().renderItem(stack).desiredWidth(18))
               .desiredHeight(18))
            .filledRectThickness(1))
         .filledBackground(-11184811);
      this.row.add(blockRender);
      blockRender.event(new BlockRenderEvent() {
         {
            Objects.requireNonNull(ScreenModuleGuiBuilder.this);
         }

         public void select() {
            ItemStack holding = Minecraft.getInstance().player.containerMenu.getCarried();
            if (holding.isEmpty()) {
               setter.accept(ScreenModuleGuiBuilder.this.module, ItemStack.EMPTY);
               blockRender.renderItem(null);
            } else {
               ItemStack copy = holding.copy();
               copy.setCount(1);
               blockRender.renderItem(copy);
               setter.accept(ScreenModuleGuiBuilder.this.module, copy);
            }

            ScreenModuleGuiBuilder.this.moduleGuiChanged.updateData();
         }

         public void doubleClick() {
         }
      });
      return this;
   }

   public ScreenModuleGuiBuilder nl() {
      if (this.row.size() == 1) {
         this.panel.children(new Widget[]{this.row.get(0).desiredHeight(16)});
         this.row.clear();
      } else if (!this.row.isEmpty()) {
         Panel rowPanel = (Panel)Widgets.horizontal().desiredHeight(16);

         for (Widget<?> widget : this.row) {
            rowPanel.children(new Widget[]{widget});
         }

         this.panel.children(new Widget[]{rowPanel});
         this.row.clear();
      }

      return this;
   }

   private static ChoiceLabel setupFormatCombo(
      Minecraft mc,
      Screen gui,
      BiConsumer<ItemStack, FormatStyle> setter,
      Function<ItemStack, FormatStyle> getter,
      ItemStack module,
      IModuleGuiChanged moduleGuiChanged
   ) {
      String modeFull = FormatStyle.MODE_FULL.getName();
      String modeCompact = FormatStyle.MODE_COMPACT.getName();
      String modeCommas = FormatStyle.MODE_COMMAS.getName();
      ChoiceLabel modeButton = ((ChoiceLabel)((ChoiceLabel)new ChoiceLabel().desiredWidth(58)).desiredHeight(14))
         .choices(new String[]{modeFull, modeCompact, modeCommas})
         .choiceTooltip(modeFull, new String[]{"Full format: 3123555"})
         .choiceTooltip(modeCompact, new String[]{"Compact format: 3.1M"})
         .choiceTooltip(modeCommas, new String[]{"Comma format: 3,123,555"})
         .event(newChoice -> {
            setter.accept(module, FormatStyle.getStyle(newChoice));
            moduleGuiChanged.updateData();
         });
      FormatStyle currentFormat = getter.apply(module);
      modeButton.choice(currentFormat.getName());
      return modeButton;
   }

   private static ChoiceLabel setupModeCombo(
      Minecraft mc,
      Screen gui,
      BiConsumer<ItemStack, BarMode> setter,
      Function<ItemStack, BarMode> getter,
      String componentName,
      ItemStack module,
      IModuleGuiChanged moduleGuiChanged
   ) {
      String modeNone = "None";
      String modePertick = componentName + "/t";
      String modePct = componentName + "%";
      ChoiceLabel modeButton = ((ChoiceLabel)((ChoiceLabel)new ChoiceLabel().desiredWidth(50)).desiredHeight(14))
         .choices(new String[]{modeNone, componentName, modePertick, modePct})
         .choiceTooltip(modeNone, new String[]{"No text is shown"})
         .choiceTooltip(componentName, new String[]{"Show the amount of " + componentName})
         .choiceTooltip(modePertick, new String[]{"Show the average " + componentName + "/tick", "gain or loss"})
         .choiceTooltip(modePct, new String[]{"Show the amount of " + componentName, "as a percentage"})
         .event(newChoice -> {
            if (componentName.equals(newChoice)) {
               setter.accept(module, BarMode.MODE_TEXT);
            } else if (modePertick.equals(newChoice)) {
               setter.accept(module, BarMode.MODE_PERTICK);
            } else if (modePct.equals(newChoice)) {
               setter.accept(module, BarMode.MODE_PERCENTAGE);
            } else {
               setter.accept(module, BarMode.MODE_NONE);
            }

            moduleGuiChanged.updateData();
         });
      BarMode current = getter.apply(module);

      modeButton.choice(switch (current) {
         case MODE_NONE -> modeNone;
         case MODE_PERTICK -> modePertick;
         case MODE_PERCENTAGE -> modePct;
         default -> componentName;
      });
      return modeButton;
   }
}
