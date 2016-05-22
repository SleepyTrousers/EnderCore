package com.enderio.core.common.handlers;

import java.util.Calendar;

import com.enderio.core.EnderCore;
import com.enderio.core.common.Handlers.Handler;
import com.enderio.core.common.config.ConfigHandler;
import com.enderio.core.common.util.BlockCoord;
import com.enderio.core.common.util.EntityUtil;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.JANUARY;
import static java.util.Calendar.MONTH;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatisticsManagerServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.AchievementEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

@Handler
public class FireworkHandler {
  @SubscribeEvent
  public void onAchievement(AchievementEvent event) {
    StatisticsManagerServer file = ((EntityPlayerMP) event.getEntityPlayer()).getStatFile();
    if (!event.getEntity().worldObj.isRemote && file.canUnlockAchievement(event.getAchievement()) && !file.hasAchievementUnlocked(event.getAchievement())
        && ConfigHandler.betterAchievements) {
      event.getEntityPlayer().getEntityData().setInteger("fireworksLeft", 9);
      event.getEntityPlayer().getEntityData().setBoolean("fireworkDelay", false);
    }
  }

  @SubscribeEvent
  public void onPlayerTick(PlayerTickEvent event) {
    if(!ConfigHandler.newYearsFireworks) {
      return;
    }
    
    EntityPlayer player = event.player;

    if (!player.worldObj.isRemote && event.phase == Phase.END) {
      if (player.worldObj.getTotalWorldTime() % 100 == 0) {
        Calendar cal = Calendar.getInstance();
        if (cal.get(DAY_OF_MONTH) == 1 && cal.get(MONTH) == JANUARY
            && !player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getBoolean("celebrated")) {
          player.getEntityData().setInteger("fireworksLeft", 15);
          player.getEntityData().setBoolean("fireworkDelay", false);
          NBTTagCompound tag = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
          tag.setBoolean("celebrated", true);
          player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, tag);
          player.addChatMessage(new TextComponentString(TextFormatting.AQUA + EnderCore.lang.localize("celebrate")));
        }
      }

      int fireworksLeft = player.getEntityData().getInteger("fireworksLeft");
      if (fireworksLeft > 0 && (!player.getEntityData().getBoolean("fireworkDelay") || player.worldObj.getTotalWorldTime() % 20 == 0)) {
        BlockCoord pos = getBlockCoord(player);
        pos = pos.withY(pos.y + 2);
        EntityUtil.spawnFirework(pos, player.worldObj.provider.getDimension(), 12);
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
    return new BlockCoord((int) Math.floor(player.posX), (int) Math.floor(player.posY), (int) Math.floor(player.posZ));
  }
}
