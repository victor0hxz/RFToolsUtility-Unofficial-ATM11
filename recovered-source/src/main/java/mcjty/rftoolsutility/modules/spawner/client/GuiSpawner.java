package mcjty.rftoolsutility.modules.spawner.client;

import java.awt.Rectangle;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.BlockRender;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.gui.widgets.Widgets;
import mcjty.lib.network.Networking;
import mcjty.lib.network.PacketRequestDataFromServer;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.TagTools;
import mcjty.rftoolsutility.modules.spawner.SpawnerConfiguration;
import mcjty.rftoolsutility.modules.spawner.SpawnerModule;
import mcjty.rftoolsutility.modules.spawner.blocks.SpawnerTileEntity;
import mcjty.rftoolsutility.modules.spawner.items.SyringeItem;
import mcjty.rftoolsutility.modules.spawner.recipes.SpawnerRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class GuiSpawner extends GenericGuiContainer<SpawnerTileEntity, GenericContainer> {
   private static final int SPAWNER_WIDTH = 180;
   private static final int SPAWNER_HEIGHT = 152;
   private EnergyBar energyBar;
   private final BlockRender[] blocks = new BlockRender[3];
   private final Label[] labels = new Label[3];
   private Label name;
   private Label rfTick;
   private static final Identifier GUI = Identifier.fromNamespaceAndPath("rftoolsutility", "textures/gui/spawner.png");
   private static long lastTime = 0L;

   public GuiSpawner(GenericContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, ((BaseBlock)SpawnerModule.SPAWNER.block().get()).getManualEntry(), 180, 152);
   }

   public static void register(RegisterMenuScreensEvent event) {
      event.register(SpawnerModule.CONTAINER_SPAWNER.get(), GuiSpawner::new);
   }

   public void init() {
      super.init();
      this.energyBar = ((EnergyBar)new EnergyBar().vertical().hint(10, 7, 8, 54)).showText(false);
      this.blocks[0] = (BlockRender)new BlockRender().hint(100, 5, 18, 18);
      this.blocks[1] = (BlockRender)new BlockRender().hint(100, 25, 18, 18);
      this.blocks[2] = (BlockRender)new BlockRender().hint(100, 45, 18, 18);
      this.labels[0] = (Label)Widgets.label(120, 5, 74, 18, "").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
      this.labels[1] = (Label)Widgets.label(120, 25, 74, 18, "").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
      this.labels[2] = (Label)Widgets.label(120, 45, 74, 18, "").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
      this.name = (Label)Widgets.label(22, 31, 78, 16, "").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
      this.rfTick = (Label)Widgets.label(22, 47, 78, 16, "").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
      Panel toplevel = (Panel)((Panel)new Panel().background(GUI))
         .layout(new PositionalLayout())
         .children(
            new Widget[]{
               this.energyBar, this.blocks[0], this.labels[0], this.blocks[1], this.labels[1], this.blocks[2], this.labels[2], this.rfTick, this.name
            }
         );
      toplevel.setBounds(new Rectangle(this.leftPos, this.topPos, this.imageWidth, this.imageHeight));
      this.window = new Window(this, toplevel);
   }

   private void showSyringeInfo() {
      for (int i = 0; i < 3; i++) {
         this.blocks[i].renderItem(null);
         this.labels[i].text("");
      }

      this.name.text("");
      this.rfTick.text("");
      SpawnerTileEntity tileEntity = (SpawnerTileEntity)this.getBE();
      ItemStack stack = tileEntity.getItems().getStackInSlot(0);
      if (!stack.isEmpty()) {
         Identifier mobId = SyringeItem.getMobId(stack);
         if (mobId != null) {
            String mobName = SyringeItem.getMobName(stack);
            this.name.text(mobName);
            SpawnerRecipes.MobData mobData = SpawnerRecipes.getMobData(Minecraft.getInstance().level, mobId);
            if (mobData != null) {
               this.rfTick.text(mobData.getSpawnRf() + "RF");
               int i = 0;
               if (System.currentTimeMillis() - lastTime > 100L) {
                  lastTime = System.currentTimeMillis();
                  Networking.sendToServer(
                     PacketRequestDataFromServer.create(
                        tileEntity.getDimension(), tileEntity.getBlockPos(), SpawnerTileEntity.CMD_GET_SPAWNERINFO.name(), TypedMap.EMPTY, false
                     )
                  );
               }

               float[] matter = new float[]{tileEntity.matterReceived0, tileEntity.matterReceived1, tileEntity.matterReceived2};

               for (int index = 0; index < 3; index++) {
                  SpawnerRecipes.MobSpawnAmount item = mobData.getItem(index);
                  ItemStack[] matchingStacks = item.getObject()
                     .map(ingredient -> ingredient.items().map(h -> new ItemStack((ItemLike)h.value())).toArray(ItemStack[]::new))
                     .orElseGet(() -> new ItemStack[0]);
                  float amount = item.getAmount();
                  int size = matchingStacks.length;
                  if (size == 0) {
                     List<Item> items = new ArrayList<>();
                     TagTools.getItemsForTag(SpawnerConfiguration.TAG_LIVING).forEach(h -> items.add((Item)h.value()));
                     int itemSize = items.size();
                     if (itemSize > 0) {
                        int idx = (int)(System.currentTimeMillis() / 500L % itemSize);
                        this.blocks[i].renderItem(new ItemStack((ItemLike)items.get(idx), 1));
                     } else {
                        this.blocks[i].renderItem(ItemStack.EMPTY);
                     }
                  } else {
                     int idx = (int)(System.currentTimeMillis() / 500L % size);
                     ItemStack b = matchingStacks[idx];
                     this.blocks[i].renderItem(b);
                  }

                  DecimalFormat format = new DecimalFormat("#.##");
                  format.setRoundingMode(RoundingMode.DOWN);
                  String mf = format.format(matter[i]);
                  this.labels[i].text(mf + "/" + Float.toString(amount));
                  i++;
               }
            }
         }
      }
   }

   public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      this.showSyringeInfo();
      this.drawWindow(graphics, partialTicks, mouseX, mouseY);
      this.updateEnergyBar(this.energyBar);
   }
}
