package com.enderio.core.common.handlers;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.JANUARY;
import static java.util.Calendar.MONTH;

import java.util.Calendar;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatisticsFile;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.AchievementEvent;

import com.enderio.core.EnderCore;
import com.enderio.core.common.Handlers.Handler;
import com.enderio.core.common.config.ConfigHandler;
import com.enderio.core.common.util.BlockCoord;
import com.enderio.core.common.util.EntityUtil;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;

@Handler
public class FireworkHandler {

    @SubscribeEvent
    public void onAchievement(AchievementEvent event) {
        StatisticsFile file = ((EntityPlayerMP) event.entityPlayer).func_147099_x();
        if (!event.entity.worldObj.isRemote && file.canUnlockAchievement(event.achievement)
                && !file.hasAchievementUnlocked(event.achievement)
                && ConfigHandler.betterAchievements) {
            event.entityPlayer.getEntityData().setInteger("fireworksLeft", 9);
            event.entityPlayer.getEntityData().setBoolean("fireworkDelay", false);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event) {
        EntityPlayer player = event.player;

        if (!player.worldObj.isRemote && event.phase == Phase.END) {
            if (player.worldObj.getTotalWorldTime() % 100 == 0) {
                Calendar cal = Calendar.getInstance();
                if (cal.get(DAY_OF_MONTH) == 1 && cal.get(MONTH) == JANUARY
                        && !player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG)
                                .getBoolean("celebrated")) {
                    player.getEntityData().setInteger("fireworksLeft", 15);
                    player.getEntityData().setBoolean("fireworkDelay", false);
                    NBTTagCompound tag = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
                    tag.setBoolean("celebrated", true);
                    player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, tag);
                    player.addChatMessage(
                            new ChatComponentText(EnumChatFormatting.AQUA + EnderCore.lang.localize("celebrate")));
                }
            }

            int fireworksLeft = player.getEntityData().getInteger("fireworksLeft");
            if (fireworksLeft > 0 && (!player.getEntityData().getBoolean("fireworkDelay")
                    || player.worldObj.getTotalWorldTime() % 20 == 0)) {
                BlockCoord pos = getBlockCoord(player);
                pos = pos.withY(pos.y + 2);
                EntityUtil.spawnFirework(pos, player.worldObj.provider.dimensionId, 12);
                player.getEntityData().setInteger("fireworksLeft", fireworksLeft - 1);

                if (fireworksLeft > 5) {
                    player.getEntityData().setBoolean("fireworkDelay", true);
                } else {
                    player.getEntityData().setBoolean("fireworkDelay", false);
                }
            }
        }
    }

    private BlockCoord getBlockCoord(EntityPlayer player) {
        return new BlockCoord(
                (int) Math.floor(player.posX),
                (int) Math.floor(player.posY),
                (int) Math.floor(player.posZ));
    }
}
