package mcjty.rftoolsutility.setup;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mcjty.lib.api.smartwrench.SmartWrench;
import mcjty.rftoolsutility.commands.ModCommands;
import mcjty.rftoolsutility.modules.environmental.NoTeleportAreaManager;
import mcjty.rftoolsutility.modules.environmental.PeacefulAreaManager;
import mcjty.rftoolsutility.modules.screen.blocks.IAttackableBlock;
import mcjty.rftoolsutility.modules.screen.blocks.ScreenBlock;
import mcjty.rftoolsutility.modules.screen.blocks.ScreenHitBlock;
import mcjty.rftoolsutility.modules.teleporter.TeleportationTools;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestination;
import mcjty.rftoolsutility.playerprops.BuffProperties;
import mcjty.rftoolsutility.playerprops.PlayerBuff;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent.EnderEntity;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent.EnderPearl;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.neoforged.neoforge.event.tick.LevelTickEvent.Pre;
import org.apache.commons.lang3.tuple.Pair;

public class ForgeEventHandlers {
   private static final List<Pair<TeleportDestination, Player>> playersToTeleportHere = new ArrayList<>();

   public static void addPlayerToTeleportHere(TeleportDestination destination, Player player) {
      playersToTeleportHere.add(Pair.of(destination, player));
   }

   private static void performDelayedTeleports() {
      if (!playersToTeleportHere.isEmpty()) {
         ArrayList<Pair<TeleportDestination, Player>> copy = new ArrayList<>(playersToTeleportHere);
         playersToTeleportHere.clear();

         for (Pair<TeleportDestination, Player> pair : copy) {
            TeleportationTools.performTeleport((Player)pair.getRight(), (TeleportDestination)pair.getLeft(), 0, 10, false);
         }
      }
   }

   @SubscribeEvent
   public void onWorldTick(Pre event) {
      if (!event.getLevel().isClientSide() && event.getLevel().dimension().equals(Level.OVERWORLD)) {
         performDelayedTeleports();
      }
   }

   @SubscribeEvent
   public void onPlayerTickEvent(net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre event) {
      Player player = event.getEntity();
      if (!player.level().isClientSide()) {
         BuffProperties data = (BuffProperties)player.getData(Registration.ATTACHMENT_TYPE_BUFF_PROPERTIES);
         data.tickBuffs((ServerPlayer)player);
      }
   }

   @SubscribeEvent
   public void onRightClickItem(RightClickItem event) {
      Level world = event.getLevel();
      if (!world.isClientSide()) {
         Player player = event.getEntity();
         ItemStack heldItem = player.getMainHandItem();
         if (heldItem.isEmpty() || !(heldItem.getItem() instanceof SmartWrench)) {
            double blockReachDistance = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).getValue();
            BlockHitResult rayTrace = rayTraceEyes(player, blockReachDistance + 1.0);
            if (rayTrace.getType() == Type.BLOCK) {
               Block block = world.getBlockState(rayTrace.getBlockPos()).getBlock();
               if (block instanceof ScreenBlock) {
                  event.setCanceled(true);
               } else if (block instanceof ScreenHitBlock) {
                  event.setCanceled(true);
               }
            }
         }
      }
   }

   @Nonnull
   public static BlockHitResult rayTraceEyes(LivingEntity entity, double length) {
      Vec3 startPos = new Vec3(entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ());
      Vec3 endPos = startPos.add(new Vec3(entity.getLookAngle().x * length, entity.getLookAngle().y * length, entity.getLookAngle().z * length));
      ClipContext context = new ClipContext(startPos, endPos, net.minecraft.world.level.ClipContext.Block.COLLIDER, Fluid.NONE, entity);
      return entity.level().clip(context);
   }

   @SubscribeEvent
   public void onPlayerInteractEventLeftClick(LeftClickBlock event) {
      Player player = event.getEntity();
      this.checkCreativeClick(event);
      ItemStack heldItem = player.getItemInHand(event.getHand());
      if (!heldItem.isEmpty()) {
         heldItem.getItem();
      }
   }

   @SubscribeEvent
   public void onPlayerInteractEventRightClick(RightClickBlock event) {
      Player player = event.getEntity();
      if (player.isShiftKeyDown()) {
         ItemStack heldItem = player.getMainHandItem();
         if (heldItem.isEmpty() || !(heldItem.getItem() instanceof SmartWrench)) {
            Level world = event.getLevel();
            BlockState state = world.getBlockState(event.getPos());
            Block var6 = state.getBlock();
         }
      }

      ItemStack heldItem = player.getItemInHand(event.getHand());
      if (!heldItem.isEmpty()) {
         heldItem.getItem();
      }
   }

   private void checkCreativeClick(LeftClickBlock event) {
      if (event.getEntity().isCreative()) {
         BlockState state = event.getLevel().getBlockState(event.getPos());
         if (state.getBlock() instanceof IAttackableBlock attackableBlock && !event.getEntity().isShiftKeyDown()) {
            if (event.getLevel().isClientSide()) {
               attackableBlock.doAttack(event.getLevel(), event.getPos());
            }

            event.setCanceled(true);
         }
      }
   }

   @SubscribeEvent
   public void onLivingFall(LivingFallEvent event) {
      if (event.getEntity() instanceof Player player) {
         BuffProperties h = (BuffProperties)player.getData(Registration.ATTACHMENT_TYPE_BUFF_PROPERTIES);
         if (h.hasBuff(PlayerBuff.BUFF_FEATHERFALLING)) {
            event.setDamageMultiplier(event.getDamageMultiplier() / 2.0F);
         }

         if (h.hasBuff(PlayerBuff.BUFF_FEATHERFALLINGPLUS)) {
            event.setCanceled(true);
         }
      }
   }

   @SubscribeEvent
   public void onPearlTeleport(EnderPearl event) {
      this.checkTeleport(event);
   }

   @SubscribeEvent
   public void onEntityTeleport(EnderEntity event) {
      this.checkTeleport(event);
   }

   private void checkTeleport(EntityTeleportEvent event) {
      Level world = event.getEntity().level();
      ResourceKey<Level> id = world.dimension();
      Entity entity = event.getEntity();
      BlockPos coordinate = new BlockPos((int)entity.getX(), (int)entity.getY(), (int)entity.getZ());
      if (NoTeleportAreaManager.isTeleportPrevented(entity, GlobalPos.of(id, coordinate))) {
         event.setCanceled(true);
      } else {
         coordinate = new BlockPos((int)event.getTargetX(), (int)event.getTargetY(), (int)event.getTargetZ());
         if (NoTeleportAreaManager.isTeleportPrevented(entity, GlobalPos.of(id, coordinate))) {
            event.setCanceled(true);
         }
      }
   }

   public static boolean onEntitySpawnEvent(Entity entity) {
      Level world = entity.level();
      ResourceKey<Level> id = world.dimension();
      if (entity instanceof Enemy) {
         BlockPos coordinate = new BlockPos((int)entity.getX(), (int)entity.getY(), (int)entity.getZ());
         if (PeacefulAreaManager.isPeaceful(GlobalPos.of(id, coordinate))) {
            return true;
         }
      }

      return false;
   }

   @SubscribeEvent
   public void serverLoad(RegisterCommandsEvent event) {
      ModCommands.register(event.getDispatcher());
   }
}
