package mcjty.rftoolsutility.modules.spawner.blocks;

import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.container.ItemInventory;
import mcjty.lib.api.infusable.DefaultInfusable;
import mcjty.lib.api.infusable.IInfusable;
import mcjty.lib.api.infusable.ItemInfusable;
import mcjty.lib.api.module.DefaultModuleSupport;
import mcjty.lib.api.module.IModuleSupport;
import mcjty.lib.api.power.ItemEnergy;
import mcjty.lib.blockcommands.ResultCommand;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.setup.Registration;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.OrientationTools;
import mcjty.lib.varia.SoundTools;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.RFToolsBase;
import mcjty.rftoolsbase.api.machineinfo.IMachineInformation;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsutility.compat.RFToolsUtilityTOPDriver;
import mcjty.rftoolsutility.modules.spawner.SpawnerConfiguration;
import mcjty.rftoolsutility.modules.spawner.SpawnerModule;
import mcjty.rftoolsutility.modules.spawner.data.SpawnerData;
import mcjty.rftoolsutility.modules.spawner.data.SyringeData;
import mcjty.rftoolsutility.modules.spawner.recipes.SpawnerRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.Lazy;

public class SpawnerTileEntity extends TickingTileEntity {
   public float matterReceived0 = -1.0F;
   public float matterReceived1 = -1.0F;
   public float matterReceived2 = -1.0F;
   public static final int SLOT_SYRINGE = 0;
   public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(
      () -> new ContainerFactory(1)
         .box(SlotDefinition.specific(new Item[]{(Item)SpawnerModule.SYRINGE.get()}).in().out(), 0, 22, 8, 1, 18, 1, 18)
         .playerSlots(10, 70)
   );
   private final GenericItemHandler items = GenericItemHandler.create(this, CONTAINER_FACTORY)
      .itemValid(GenericItemHandler.match(SpawnerModule.SYRINGE))
      .onUpdate((slot, stack) -> {
         this.checkSyringe = true;
         SpawnerData data = (SpawnerData)this.getData(SpawnerModule.SPAWNER_DATA);
         this.prevMobId = data.mob();
      })
      .build();
   @Cap(type = CapType.ITEMS_AUTOMATION)
   private static final Function<SpawnerTileEntity, GenericItemHandler> ITEM_CAP = tile -> tile.items;
   private final GenericEnergyStorage energyStorage = new GenericEnergyStorage(
      this, true, SpawnerConfiguration.SPAWNER_MAXENERGY, SpawnerConfiguration.SPAWNER_RECEIVEPERTICK
   );
   @Cap(type = CapType.ENERGY)
   private static final Function<SpawnerTileEntity, GenericEnergyStorage> ENERGY_CAP = tile -> tile.energyStorage;
   @Cap(type = CapType.CONTAINER)
   private static final Function<SpawnerTileEntity, MenuProvider> SCREEN_CAP = be -> new DefaultContainerProvider("Spawner")
      .containerSupplier(DefaultContainerProvider.container(SpawnerModule.CONTAINER_SPAWNER, CONTAINER_FACTORY, be))
      .itemHandler(() -> be.items)
      .energyHandler(() -> be.energyStorage)
      .setupSync(be);
   private final DefaultInfusable infusable = new DefaultInfusable(this);
   @Cap(type = CapType.INFUSABLE)
   private static final Function<SpawnerTileEntity, IInfusable> INFUSABLE_CAP = tile -> tile.infusable;
   @Cap(type = CapType.MODULE)
   private static final Function<SpawnerTileEntity, IModuleSupport> MODULE_CAP = be -> new DefaultModuleSupport(0) {
      public boolean isModule(ItemStack itemStack) {
         return itemStack.getItem() == SpawnerModule.SYRINGE.get();
      }
   };
   private boolean checkSyringe = true;
   private Identifier prevMobId = null;
   private AABB entityCheckBox = null;
   public static final Key<Double> PARAM_MATTER0 = new Key("matter0", Type.DOUBLE);
   public static final Key<Double> PARAM_MATTER1 = new Key("matter1", Type.DOUBLE);
   public static final Key<Double> PARAM_MATTER2 = new Key("matter2", Type.DOUBLE);
   @ServerCommand
   public static final ResultCommand<?> CMD_GET_SPAWNERINFO = ResultCommand.create(
      "getSpawnerInfo",
      (te, player, params) -> TypedMap.builder()
         .put(PARAM_MATTER0, (double)te.getMatter(0))
         .put(PARAM_MATTER1, (double)te.getMatter(1))
         .put(PARAM_MATTER2, (double)te.getMatter(2))
         .build(),
      (te, player, params) -> {
         te.matterReceived0 = ((Double)params.get(PARAM_MATTER0)).floatValue();
         te.matterReceived1 = ((Double)params.get(PARAM_MATTER1)).floatValue();
         te.matterReceived2 = ((Double)params.get(PARAM_MATTER2)).floatValue();
      }
   );

   public static BaseBlock createBlock() {
      return new BaseBlock(
         new BlockBuilder()
            .tileEntitySupplier(SpawnerTileEntity::new)
            .topDriver(RFToolsUtilityTOPDriver.DRIVER)
            .infusable()
            .manualEntry(ManualHelper.create("rftoolsutility:machines/spawner"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsutility.shiftmessage")})
            .infoShift(new InfoLine[]{TooltipBuilder.header(), TooltipBuilder.gold()})
      );
   }

   public SpawnerTileEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)SpawnerModule.SPAWNER.be().get(), pos, state);
   }

   private void testSyringe() {
      if (this.checkSyringe) {
         this.checkSyringe = false;
         SpawnerData data = (SpawnerData)this.getData(SpawnerModule.SPAWNER_DATA);
         data = data.withMob(null);
         this.setData(SpawnerModule.SPAWNER_DATA, data);
         ItemStack itemStack = this.items.getStackInSlot(0);
         if (itemStack.isEmpty()) {
            this.clearMatter();
         } else {
            SyringeData syringeData = (SyringeData)itemStack.get(SpawnerModule.ITEM_SYRINGE_DATA);
            if (syringeData == null) {
               this.clearMatter();
            } else {
               Identifier mobId = syringeData.mob();
               if (mobId == null) {
                  this.clearMatter();
               } else {
                  int level = syringeData.level();
                  if (level < (Integer)SpawnerConfiguration.maxMobInjections.get()) {
                     this.clearMatter();
                  } else {
                     if (this.prevMobId != null && !this.prevMobId.equals(mobId)) {
                        this.clearMatter();
                     }

                     data = data.withMob(mobId);
                     this.setData(SpawnerModule.SPAWNER_DATA, data);
                  }
               }
            }
         }
      }
   }

   public GenericItemHandler getItems() {
      return this.items;
   }

   private void clearMatter() {
      SpawnerData data = (SpawnerData)this.getData(SpawnerModule.SPAWNER_DATA);
      data = data.withMatter0(0.0F).withMatter1(0.0F).withMatter2(0.0F);
      this.setData(SpawnerModule.SPAWNER_DATA, data);
   }

   public boolean addMatter(ItemStack stack, int m, float beamerInfusionFactor) {
      this.testSyringe();
      SpawnerData data = (SpawnerData)this.getData(SpawnerModule.SPAWNER_DATA);
      Identifier mobId = data.mob();
      if (mobId == null) {
         return false;
      } else {
         int materialType = 0;
         Float factor = null;
         SpawnerRecipes.MobData mobData = this.getMobData();
         if (mobData == null) {
            return false;
         } else {
            for (int i = 0; i < 3; i++) {
               factor = mobData.getItem(i).match(stack);
               if (factor != null) {
                  break;
               }

               materialType++;
            }

            if (factor == null) {
               return false;
            } else {
               float mm = data.getMatter(materialType);
               mm += m * factor * 3.0F / (3.0F - beamerInfusionFactor);
               if (mm > SpawnerConfiguration.maxMatterStorage) {
                  mm = SpawnerConfiguration.maxMatterStorage;
               }

               data = data.withMatter(materialType, mm);
               this.setData(SpawnerModule.SPAWNER_DATA, data);
               return true;
            }
         }
      }
   }

   @Nullable
   private SpawnerRecipes.MobData getMobData() {
      SpawnerData data = (SpawnerData)this.getData(SpawnerModule.SPAWNER_DATA);
      SpawnerRecipes.MobData mobData = SpawnerRecipes.getMobData(this.level, data.mob());
      if (mobData == null) {
         Logging.logError("The mob spawn amounts list for mob " + data.mob() + " is missing!");
      }

      return mobData;
   }

   public float getMatter(int i) {
      SpawnerData data = (SpawnerData)this.getData(SpawnerModule.SPAWNER_DATA);
      return data.getMatter(i);
   }

   protected void tickServer() {
      this.testSyringe();
      SpawnerData data = (SpawnerData)this.getData(SpawnerModule.SPAWNER_DATA);
      Identifier mobId = data.mob();
      if (mobId != null) {
         SpawnerRecipes.MobData mobData = this.getMobData();
         if (mobData != null) {
            for (int i = 0; i < 3; i++) {
               if (data.getMatter(i) < mobData.getItem(i).getAmount()) {
                  return;
               }
            }

            int rf = mobData.getSpawnRf();
            rf = (int)(rf * (2.0F - this.infusable.getInfusedFactor()) / 2.0F);
            if (this.energyStorage.getEnergyStored() >= rf) {
               this.energyStorage.consumeEnergy(rf);

               for (int ix = 0; ix < 3; ix++) {
                  data = data.withMatter(ix, data.getMatter(ix) - mobData.getItem(ix).getAmount());
               }

               this.setData(SpawnerModule.SPAWNER_DATA, data);
               BlockState state = this.level.getBlockState(this.getBlockPos());
               Direction k = OrientationTools.getOrientation(state);
               int sx = this.getBlockPos().getX();
               int sy = this.getBlockPos().getY();
               int sz = this.getBlockPos().getZ();
               Vec3i dir = k.getUnitVec3i();
               sx += dir.getX();
               sy += dir.getY();
               sz += dir.getZ();
               EntityType<?> type = Tools.getEntity(mobId);
               if (type == null) {
                  Logging.logError("Fail to spawn mob: " + mobId);
               } else {
                  Entity entityLiving = type.create(this.level, EntitySpawnReason.MOB_SUMMONED);
                  if (entityLiving == null) {
                     Logging.logError("Fail to spawn mob: " + mobId);
                  } else {
                     entityLiving.getPersistentData().putBoolean("rftoolsutility:spawner", true);
                     if (k == Direction.DOWN) {
                        sy = (int)(sy - (entityLiving.getEyeHeight() - 1.0F));
                     }

                     entityLiving.snapTo(sx + 0.5, sy, sz + 0.5, 0.0F, 0.0F);
                     this.level.addFreshEntity(entityLiving);
                  }
               }
            }
         }
      }
   }

   public void useWrench(Player player) {
      BlockPos coord = RFToolsBase.instance.clientInfo.getSelectedTE();
      if (coord != null) {
         BlockEntity tileEntity = this.level.getBlockEntity(coord);
         double d = new Vec3(coord.getX(), coord.getY(), coord.getZ())
            .distanceTo(new Vec3(this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ()));
         if (d > SpawnerConfiguration.maxBeamDistance) {
            Logging.message(player, "Destination distance is too far!");
         } else if (tileEntity instanceof MatterBeamerTileEntity beamer) {
            beamer.setDestination(this.getBlockPos());
            Logging.message(player, "Destination set!");
         }

         RFToolsBase.instance.clientInfo.setSelectedTE(null);
         RFToolsBase.instance.clientInfo.setDestinationTE(null);
      }
   }

   public boolean wrenchUse(Level world, BlockPos pos, Direction side, Player player) {
      if (world.isClientSide()) {
         world.playLocalSound(
            pos.getX(),
            pos.getY(),
            pos.getZ(),
            SoundTools.findSound(Identifier.fromNamespaceAndPath("minecraft", "block.note_block.pling")),
            SoundSource.BLOCKS,
            1.0F,
            1.0F,
            false
         );
         this.useWrench(player);
      }

      return true;
   }

   protected void saveAdditional(ValueOutput tag) {
      super.saveAdditional(tag);
      this.energyStorage.save(tag, "energy");
      this.items.save(tag, "items");
      this.infusable.save(tag, "infusable");
   }

   protected void loadAdditional(ValueInput tag) {
      super.loadAdditional(tag);
      this.energyStorage.load(tag, "energy");
      this.items.load(tag, "items");
      this.infusable.load(tag, "infusable");
   }

   protected void applyImplicitComponents(DataComponentGetter input) {
      super.applyImplicitComponents(input);
      SpawnerData data = (SpawnerData)input.get(SpawnerModule.ITEM_SPAWNER_DATA);
      if (data != null) {
         this.setData(SpawnerModule.SPAWNER_DATA, data);
      }

      this.energyStorage.applyImplicitComponents((ItemEnergy)input.get(Registration.ITEM_ENERGY));
      this.items.applyImplicitComponents((ItemInventory)input.get(Registration.ITEM_INVENTORY));
      this.infusable.applyImplicitComponents((ItemInfusable)input.get(Registration.ITEM_INFUSABLE));
   }

   protected void collectImplicitComponents(Builder builder) {
      super.collectImplicitComponents(builder);
      builder.set(SpawnerModule.ITEM_SPAWNER_DATA, (SpawnerData)this.getData(SpawnerModule.SPAWNER_DATA));
      this.energyStorage.collectImplicitComponents(builder);
      this.items.collectImplicitComponents(builder);
      this.infusable.collectImplicitComponents(builder);
   }

   @Nonnull
   private IMachineInformation createMachineInfo() {
      return new IMachineInformation() {
         private final String[] TAGS;
         private final String[] TAG_DESCRIPTIONS;

         {
            Objects.requireNonNull(SpawnerTileEntity.this);
            this.TAGS = new String[]{"matter1", "matter2", "matter3", "mob"};
            this.TAG_DESCRIPTIONS = new String[]{
               "The amount of matter in the first slot",
               "The amount of matter in the second slot",
               "The amount of matter in the third slot",
               "The name of the mob being spawned"
            };
         }

         public int getTagCount() {
            return this.TAGS.length;
         }

         public String getTagName(int index) {
            return this.TAGS[index];
         }

         public String getTagDescription(int index) {
            return this.TAG_DESCRIPTIONS[index];
         }

         public String getData(int index, long millis) {
            switch (index) {
               case 0:
                  return Float.toString(SpawnerTileEntity.this.getMatter(0));
               case 1:
                  return Float.toString(SpawnerTileEntity.this.getMatter(1));
               case 2:
                  return Float.toString(SpawnerTileEntity.this.getMatter(2));
               case 3:
                  return ((SpawnerData)SpawnerTileEntity.this.getData(SpawnerModule.SPAWNER_DATA)).mob().toString();
               default:
                  return null;
            }
         }
      };
   }
}
