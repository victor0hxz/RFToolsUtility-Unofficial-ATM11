package mcjty.rftoolsutility.modules.spawner.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.items.BaseItem;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.spawner.SpawnerConfiguration;
import mcjty.rftoolsutility.modules.spawner.SpawnerModule;
import mcjty.rftoolsutility.modules.spawner.data.SyringeData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.Lazy;

public class SyringeItem extends BaseItem implements ITooltipSettings {
   public static final int MAX_SYRINGE_MODEL_LEVEL = 5;
   private final Lazy<TooltipBuilder> tooltipBuilder = Lazy.of(
      () -> new TooltipBuilder()
         .info(
            new InfoLine[]{
               TooltipBuilder.parameter("level", this::getLevelString),
               TooltipBuilder.parameter("mob", this::hasMob, SyringeItem::getMobName),
               TooltipBuilder.key("message.rftoolsutility.shiftmessage")
            }
         )
         .infoShift(
            new InfoLine[]{
               TooltipBuilder.header(),
               TooltipBuilder.parameter("level", this::getLevelString),
               TooltipBuilder.parameter("mob", this::hasMob, SyringeItem::getMobName)
            }
         )
   );

   public SyringeItem() {
      super(RFToolsUtility.setup.defaultProperties().stacksTo(1));
   }

   private String getLevelString(ItemStack stack) {
      return Integer.toString(getLevel(stack));
   }

   private boolean hasMob(ItemStack stack) {
      return getMobId(stack) != null;
   }

   public static int getLevel(ItemStack stack) {
      SyringeData data = (SyringeData)stack.get(SpawnerModule.ITEM_SYRINGE_DATA);
      return data != null ? data.level() * 100 / (Integer)SpawnerConfiguration.maxMobInjections.get() : 0;
   }

   public static void initOverrides(SyringeItem item) {
   }

   public void appendHoverText(
      @Nonnull ItemStack itemStack, TooltipContext context, TooltipDisplay display, @Nonnull Consumer<Component> list, @Nonnull TooltipFlag flag
   ) {
      super.appendHoverText(itemStack, context, display, list, flag);
      ((TooltipBuilder)this.tooltipBuilder.get()).makeTooltip(Tools.getId(this), itemStack, list, flag);
   }

   @Nullable
   public static LivingEntity getEntityLivingFromClickedEntity(Entity entity) {
      return entity instanceof LivingEntity ? (LivingEntity)entity : null;
   }

   public static ItemStack createMobSyringe(Identifier mobId) {
      ItemStack syringe = new ItemStack((ItemLike)SpawnerModule.SYRINGE.get());
      SyringeData data = new SyringeData(mobId, 0);
      syringe.set(SpawnerModule.ITEM_SYRINGE_DATA, data);
      return syringe;
   }

   public static Identifier getMobId(ItemStack stack) {
      SyringeData data = (SyringeData)stack.get(SpawnerModule.ITEM_SYRINGE_DATA);
      return data != null ? data.mob() : null;
   }

   public static String getMobName(ItemStack stack) {
      Identifier id = getMobId(stack);
      if (id == null) {
         return null;
      } else {
         EntityType<?> type = Tools.getEntity(id);
         return type != null ? type.getDescription().getString() : id.toString();
      }
   }

   public List<ItemStack> getItemsForTab() {
      List<ItemStack> items = new ArrayList<>();
      items.add(new ItemStack(this));

      for (Entry<ResourceKey<EntityType<?>>, EntityType<?>> entry : BuiltInRegistries.ENTITY_TYPE.entrySet()) {
         Identifier id = entry.getKey().identifier();
         if (entry.getValue().getCategory() != MobCategory.MISC) {
            items.add(createMobSyringe(id));
         }
      }

      return items;
   }

   @Nonnull
   public InteractionResult use(Level world, Player player, @Nonnull InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!world.isClientSide()) {
         SyringeData data = (SyringeData)stack.get(SpawnerModule.ITEM_SYRINGE_DATA);
         if (data != null) {
            String mobName = getMobName(stack);
            if (mobName != null) {
               Logging.message(player, ChatFormatting.BLUE + "Mob: " + mobName);
            }

            int level = data.level() == -1 ? (Integer)SpawnerConfiguration.maxMobInjections.get() : data.level();
            level = level * 100 / (Integer)SpawnerConfiguration.maxMobInjections.get();
            Logging.message(player, ChatFormatting.BLUE + "Essence level: " + level + "%");
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.SUCCESS;
      }
   }

   public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
      LivingEntity entityLiving = getEntityLivingFromClickedEntity(entity);
      if (entityLiving != null) {
         Identifier prevMobId = null;
         SyringeData data = (SyringeData)stack.get(SpawnerModule.ITEM_SYRINGE_DATA);
         if (data != null) {
            prevMobId = data.mob();
         } else {
            data = new SyringeData(null, 0);
         }

         Identifier id = this.findSelectedMobId(entityLiving);
         if (id != null) {
            if (!id.equals(prevMobId)) {
               data = data.withMob(id).withLevel(1);
            } else {
               int level = data.level();
               if (level == -1) {
                  level = 0;
               }

               level = Math.min(level + 1, (Integer)SpawnerConfiguration.maxMobInjections.get());
               data = data.withLevel(level);
            }

            stack.set(SpawnerModule.ITEM_SYRINGE_DATA, data);
         }
      }

      return super.onLeftClickEntity(stack, player, entity);
   }

   private Identifier findSelectedMobId(Entity entity) {
      return Tools.getId(entity.getType());
   }

   public ManualEntry getManualEntry() {
      return ManualHelper.create("rftoolsutility:machines/spawner");
   }
}
