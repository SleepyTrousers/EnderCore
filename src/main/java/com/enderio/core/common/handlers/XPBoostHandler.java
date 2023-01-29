package com.enderio.core.common.handlers;

import java.lang.reflect.Method;
import java.util.Map;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;

import com.enderio.core.common.Handlers.Handler;
import com.enderio.core.common.enchant.EnchantXPBoost;
import com.enderio.core.common.util.Scheduler;
import com.google.common.base.Throwables;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;

@Handler
public class XPBoostHandler {

    private static final Method getExperiencePoints = ReflectionHelper.findMethod(
            EntityLivingBase.class,
            null,
            new String[] { "e", "func_70693_a", "getExperiencePoints" },
            EntityPlayer.class);

    private static final String NBT_KEY = "ttCore:xpboost";

    @SubscribeEvent
    public void handleEntityKill(LivingDeathEvent event) {
        EntityLivingBase entity = event.entityLiving;
        Entity killer = event.source.getSourceOfDamage();

        if (!entity.worldObj.isRemote && killer != null) {
            if (killer instanceof EntityPlayer) {
                scheduleXP(entity, getXPBoost(entity, (EntityPlayer) killer));
            } else if (killer instanceof EntityArrow && ((EntityArrow) killer).shootingEntity instanceof EntityPlayer) {
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
    public void handleArrowFire(EntityJoinWorldEvent event) {
        if (event.entity instanceof EntityArrow) {
            EntityArrow arrow = (EntityArrow) event.entity;
            if (arrow.shootingEntity != null && arrow.shootingEntity instanceof EntityPlayer) {
                arrow.getEntityData().setInteger(
                        NBT_KEY,
                        getXPBoostLevel(((EntityPlayer) arrow.shootingEntity).getCurrentEquippedItem()));
            }
        }
    }

    @SubscribeEvent
    public void handleBlockBreak(BreakEvent event) {
        ItemStack held = event.getPlayer().getCurrentEquippedItem();
        if (held != null) {
            int level = getXPBoostLevel(held);
            int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, held);

            if (level >= 0) {
                int xp = event.block.getExpDrop(event.world, event.blockMetadata, fortune);
                if (xp > 0) {
                    event.world.spawnEntityInWorld(
                            new EntityXPOrb(
                                    event.world,
                                    event.x + 0.5,
                                    event.y + 0.5,
                                    event.z + 0.5,
                                    getXPBoost(xp, level)));
                }
            }
        }
    }

    private int getXPBoost(EntityLivingBase killed, EntityPlayer player) {
        return getXPBoost(killed, player, getXPBoostLevel(player.getCurrentEquippedItem()));
    }

    private int getXPBoost(EntityLivingBase killed, EntityPlayer player, int level) {
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

    private int getXPBoost(int xp, int level) {
        return Math.round(xp * ((float) Math.log10(level + 1) * 2));
    }

    @SuppressWarnings("unchecked")
    private int getXPBoostLevel(ItemStack weapon) {
        if (weapon == null) {
            return -1;
        }

        Map<Integer, Integer> enchants = EnchantmentHelper.getEnchantments(weapon);
        for (int i : enchants.keySet()) {
            Enchantment enchant = Enchantment.enchantmentsList[i];
            if (enchant == EnchantXPBoost.INSTANCE) {
                return enchants.get(i);
            }
        }
        return -1;
    }

    private void scheduleXP(Entity entity, int boost) {
        scheduleXP(entity.worldObj, entity.posX, entity.posY, entity.posZ, boost);
    }

    private void scheduleXP(final World world, final double x, final double y, final double z, final int boost) {
        if (boost <= 0) {
            return;
        }

        Scheduler.instance().schedule(20, new Runnable() {

            @Override
            public void run() {
                world.spawnEntityInWorld(new EntityXPOrb(world, x, y, z, boost));
            }
        });
    }

}
