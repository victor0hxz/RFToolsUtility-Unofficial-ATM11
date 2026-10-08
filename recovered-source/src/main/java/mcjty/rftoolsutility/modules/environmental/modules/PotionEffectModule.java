package mcjty.rftoolsutility.modules.environmental.modules;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Supplier;
import mcjty.rftoolsutility.modules.environmental.blocks.EnvironmentalControllerTileEntity;
import mcjty.rftoolsutility.modules.environmental.blocks.EnvironmentalMode;
import mcjty.rftoolsutility.playerprops.BuffProperties;
import mcjty.rftoolsutility.playerprops.PlayerBuff;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public abstract class PotionEffectModule implements EnvironmentModule {
   public static final int MAXTICKS = 180;
   private final Holder<MobEffect> potion;
   private final int amplifier;
   private boolean active = false;
   private int ticks = 180;

   public PotionEffectModule(String potionname, int amplifier) {
      this.potion = (Holder<MobEffect>)BuiltInRegistries.MOB_EFFECT
         .get(ResourceKey.create(BuiltInRegistries.MOB_EFFECT.key(), Identifier.parse(potionname)))
         .orElseThrow();
      this.amplifier = amplifier;
   }

   protected abstract PlayerBuff getBuff();

   protected boolean allowedForPlayers() {
      return true;
   }

   @Override
   public void tick(Level world, BlockPos pos, int radius, int miny, int maxy, EnvironmentalControllerTileEntity controllerTileEntity) {
      if (this.active) {
         this.ticks--;
         if (this.ticks <= 0) {
            this.ticks = 180;
            EnvironmentalMode mode = controllerTileEntity.getMode();
            switch (mode) {
               case MODE_BLACKLIST:
               case MODE_WHITELIST:
                  if (this.allowedForPlayers()) {
                     this.processPlayers(world, pos, radius, miny, maxy, controllerTileEntity);
                  }
                  break;
               case MODE_HOSTILE:
               case MODE_PASSIVE:
               case MODE_MOBS:
               case MODE_ALL:
                  this.processEntities(world, pos, radius, miny, maxy, controllerTileEntity);
            }
         }
      }
   }

   private void processPlayers(Level world, BlockPos pos, int radius, int miny, int maxy, EnvironmentalControllerTileEntity controllerTileEntity) {
      double maxsqdist = radius * radius;

      for (Player player : new ArrayList(world.players())) {
         double py = player.getY();
         if (py >= miny && py <= maxy) {
            double px = player.getX();
            double pz = player.getZ();
            double sqdist = (px - pos.getX()) * (px - pos.getX()) + (pz - pos.getZ()) * (pz - pos.getZ());
            if (sqdist < maxsqdist && controllerTileEntity.isPlayerAffected(player)) {
               player.addEffect(new MobEffectInstance(this.potion, 540, this.amplifier, true, false));
               PlayerBuff buff = this.getBuff();
               if (buff != null) {
                  BuffProperties.addBuffToPlayer(player, buff, 180);
               }
            }
         }
      }
   }

   private void processEntities(Level world, BlockPos pos, int radius, int miny, int maxy, EnvironmentalControllerTileEntity controllerTileEntity) {
      double maxsqdist = radius * radius;

      for (LivingEntity entity : world.getEntities(
         new EntityTypeTest<Entity, LivingEntity>() {
            {
               Objects.requireNonNull(PotionEffectModule.this);
            }

            @Nullable
            public LivingEntity tryCast(Entity pEntity) {
               return pEntity instanceof LivingEntity livingEntity ? livingEntity : null;
            }

            public Class<? extends Entity> getBaseClass() {
               return LivingEntity.class;
            }
         },
         new AABB(pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius, pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius),
         Objects::nonNull
      )) {
         double py = entity.getY();
         if (py >= miny && py <= maxy) {
            double px = entity.getX();
            double pz = entity.getZ();
            double sqdist = (px - pos.getX()) * (px - pos.getX()) + (pz - pos.getZ()) * (pz - pos.getZ());
            if (sqdist < maxsqdist) {
               if (controllerTileEntity.isEntityAffected(entity)) {
                  if (!(entity instanceof Player) || this.allowedForPlayers()) {
                     entity.addEffect(new MobEffectInstance(this.potion, 540, this.amplifier, true, false));
                     PlayerBuff buff = this.getBuff();
                     if (buff != null && entity instanceof Player) {
                        BuffProperties.addBuffToPlayer((Player)entity, buff, 180);
                     }
                  }
               } else if (entity instanceof Player) {
                  PlayerBuff buff = this.getBuff();
                  if (buff != null) {
                     BuffProperties.addBuffToPlayer((Player)entity, buff, 180);
                  }
               }
            }
         }
      }
   }

   @Override
   public boolean apply(Level world, BlockPos pos, LivingEntity entity, int duration) {
      entity.addEffect(new MobEffectInstance(this.potion, duration, this.amplifier, true, false));
      return true;
   }

   @Override
   public void activate(boolean a) {
      if (this.active != a) {
         this.active = a;
         this.ticks = 1;
      }
   }

   public static PotionEffectModule create(String name, int amplifier, final PlayerBuff buff, final double rfPerTick) {
      return new PotionEffectModule(name, amplifier) {
         @Override
         protected PlayerBuff getBuff() {
            return buff;
         }

         @Override
         public float getRfPerTick() {
            return (float)rfPerTick;
         }
      };
   }

   public static PotionEffectModule create(String name, int amplifier, final PlayerBuff buff, final double rfPerTick, final Supplier<Boolean> isAllowed) {
      return new PotionEffectModule(name, amplifier) {
         @Override
         protected PlayerBuff getBuff() {
            return buff;
         }

         @Override
         public float getRfPerTick() {
            return (float)rfPerTick;
         }

         @Override
         protected boolean allowedForPlayers() {
            return isAllowed.get();
         }
      };
   }
}
