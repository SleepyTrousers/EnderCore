package com.enderio.core.common.handlers;

import java.lang.reflect.Method;
import java.util.Map;

import javax.annotation.Nonnull;

import com.enderio.core.common.enchantment.EnchantmentXPBoost;
import com.enderio.core.common.util.NullHelper;
import com.enderio.core.common.util.Scheduler;
import com.google.common.base.Throwables;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

@Mod.EventBusSubscriber
public class XPBoostHandler {

  private static final Method getExperiencePoints = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "func_70693_a", PlayerEntity.class);

  private static final @Nonnull String NBT_KEY = "endercore:xpboost";

  @SubscribeEvent
  public static void handleEntityKill(LivingDeathEvent event) {
    LivingEntity entity = event.getEntityLiving();
    Entity killer = event.getSource().getTrueSource();

    if (!entity.world.isRemote && killer != null) {
      if (killer instanceof PlayerEntity) {
        scheduleXP(entity, getXPBoost(entity, (PlayerEntity) killer));
      } else if (killer instanceof ArrowEntity) {
        CompoundNBT tag = killer.getPersistentData();
        if (tag.contains(NBT_KEY) && tag.getInt(NBT_KEY) >= 0) {
          int level = tag.getInt(NBT_KEY);
          ArrowEntity arrow = (ArrowEntity) killer;
          scheduleXP(entity, getXPBoost(entity, (PlayerEntity) arrow.func_234616_v_(), level)); //func_234616_v_ - getShooter
        }
      }
    }
  }

  @SubscribeEvent
  public static void handleArrowFire(EntityJoinWorldEvent event) {
    if (event.getEntity() instanceof ArrowEntity) {
      ArrowEntity arrow = (ArrowEntity) event.getEntity();
      arrow.getPersistentData().putInt(NBT_KEY, getXPBoostLevel(arrow.func_234616_v_()));
    }
  }

  @SubscribeEvent
  public static void handleBlockBreak(BreakEvent event) {
    int level = getXPBoostLevel(event.getPlayer());

    if (level >= 0) {
      final @Nonnull BlockState state = NullHelper.notnullF(event.getState(), "BreakEvent.getState()");
      final @Nonnull World world = (World) NullHelper.notnullF(event.getWorld(), "BreakEvent.getWorld()");
      final @Nonnull BlockPos pos = NullHelper.notnullF(event.getPos(), "BreakEvent.getPos()");
      final int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, event.getPlayer().getHeldItemMainhand());
      final int xp = state.getBlock().getExpDrop(state, world, pos, fortune, 0);
      if (xp > 0) {
        world.addEntity(new ExperienceOrbEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, getXPBoost(xp, level)));
      }
    }
  }

  private static int getXPBoost(LivingEntity killed, PlayerEntity player) {
    return getXPBoost(killed, player, getXPBoostLevel(player));
  }

  private static int getXPBoost(LivingEntity killed, PlayerEntity player, int level) {
    if (level >= 0) {
      try {
        int xp = (Integer) getExperiencePoints.invoke(killed, player);
        return getXPBoost(xp, level);
      } catch (Exception e) {
        Throwables.propagate(e);
      }
    }
    return 0;
  }

  private static int getXPBoost(int xp, int level) {
    return Math.round(xp * ((float) Math.log10(level + 1) * 2));
  }

  private static int getXPBoostLevel(Entity player) {
    if (player == null || !(player instanceof PlayerEntity) || player instanceof FakePlayer) {
      return -1;
    }
    ItemStack weapon = ((LivingEntity) player).getHeldItemMainhand();
    if (weapon.isEmpty()) {
      return -1;
    }

    int result = -1;
    Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(weapon);
    for (Enchantment i : enchants.keySet()) {
      if (i == EnchantmentXPBoost.instance()) {
        result = enchants.get(i);
      } else if (i == Enchantments.SILK_TOUCH) {
        // No XP boost on silk touch
        return -1;
      }
    }
    return result;
  }

  private static void scheduleXP(Entity entity, int boost) {
    scheduleXP(entity.world, entity.getPosX(), entity.getPosY(), entity.getPosZ(), boost);
  }

  private static void scheduleXP(final World world, final double x, final double y, final double z, final int boost) {
    if (boost <= 0) {
      return;
    }

    Scheduler.instance().schedule(20, new Runnable() {
      @Override
      public void run() {
        world.addEntity(new ExperienceOrbEntity(world, x, y, z, boost));
      }
    });
  }

  private XPBoostHandler() {
  }

}
