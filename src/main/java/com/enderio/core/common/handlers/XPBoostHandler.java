package com.enderio.core.common.handlers;

import java.lang.reflect.Method;
import java.util.Map;

import javax.annotation.Nonnull;

import com.enderio.core.common.Handlers.Handler;
import com.enderio.core.common.enchant.EnchantXPBoost;
import com.enderio.core.common.util.NullHelper;
import com.enderio.core.common.util.Scheduler;
import com.google.common.base.Throwables;

import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

@Handler
public class XPBoostHandler {
  private static final Method getExperiencePoints = ReflectionHelper.findMethod(EntityLivingBase.class, null,
      new String[] { "e", "func_70693_a", "getExperiencePoints" }, EntityPlayer.class);

  private static final @Nonnull String NBT_KEY = "endercore:xpboost";

  @SubscribeEvent
  public static void handleEntityKill(LivingDeathEvent event) {
    EntityLivingBase entity = event.getEntityLiving();
    Entity killer = event.getSource().getSourceOfDamage();

    if (!entity.world.isRemote && killer != null) {
      if (killer instanceof EntityPlayer && !(killer instanceof FakePlayer)) {
        scheduleXP(entity, getXPBoost(entity, (EntityPlayer) killer));
      } else if (killer instanceof EntityArrow && ((EntityArrow) killer).shootingEntity instanceof EntityPlayer
          && !(((EntityArrow) killer).shootingEntity instanceof FakePlayer)) {
        NBTTagCompound tag = killer.getEntityData();
        if (tag.hasKey(NBT_KEY) && tag.getInteger(NBT_KEY) >= 0) {
          int level = tag.getInteger(NBT_KEY);
          EntityArrow arrow = (EntityArrow) killer;
          scheduleXP(entity, getXPBoost(entity, (EntityPlayer) arrow.shootingEntity, level));
        }
      }
    }
  }

  @SubscribeEvent
  public static void handleArrowFire(EntityJoinWorldEvent event) {
    if (event.getEntity() instanceof EntityArrow) {
      EntityArrow arrow = (EntityArrow) event.getEntity();
      if (arrow.shootingEntity instanceof EntityPlayer) {
        arrow.getEntityData().setInteger(NBT_KEY, getXPBoostLevel(((EntityPlayer) arrow.shootingEntity).getHeldItemMainhand()));
      }
    }
  }

  @SubscribeEvent
  public static void handleBlockBreak(BreakEvent event) {
    ItemStack held = event.getPlayer().getHeldItemMainhand();
    if (!held.isEmpty() && !(event.getPlayer() instanceof FakePlayer)) {
      int level = getXPBoostLevel(held);
      int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, held);

      if (level >= 0) {
        final @Nonnull IBlockState state = NullHelper.notnullF(event.getState(), "BreakEvent.getState()");
        final @Nonnull World world = NullHelper.notnullF(event.getWorld(), "BreakEvent.getWorld()");
        final @Nonnull BlockPos pos = NullHelper.notnullF(event.getPos(), "BreakEvent.getPos()");
        int xp = state.getBlock().getExpDrop(state, world, pos, fortune);
        if (xp > 0) {
          world.spawnEntity(new EntityXPOrb(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, getXPBoost(xp, level)));
        }
      }
    }
  }

  private static int getXPBoost(EntityLivingBase killed, EntityPlayer player) {
    return getXPBoost(killed, player, getXPBoostLevel(player.getHeldItemMainhand()));
  }

  private static int getXPBoost(EntityLivingBase killed, EntityPlayer player, int level) {
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

  private static int getXPBoostLevel(@Nonnull ItemStack weapon) {
    if (weapon.isEmpty()) {
      return -1;
    }

    int result = -1;
    Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(weapon);
    for (Enchantment i : enchants.keySet()) {
      if (i == EnchantXPBoost.instance()) {
        result = enchants.get(i);
      } else if (i == Enchantments.SILK_TOUCH) {
        // No XP boost on silk touch
        return -1;
      }
    }
    return result;
  }

  private static void scheduleXP(Entity entity, int boost) {
    scheduleXP(entity.world, entity.posX, entity.posY, entity.posZ, boost);
  }

  private static void scheduleXP(final World world, final double x, final double y, final double z, final int boost) {
    if (boost <= 0) {
      return;
    }

    Scheduler.instance().schedule(20, new Runnable() {
      @Override
      public void run() {
        world.spawnEntity(new EntityXPOrb(world, x, y, z, boost));
      }
    });
  }

  private XPBoostHandler() {
  }

}
