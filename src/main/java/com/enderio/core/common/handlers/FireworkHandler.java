package com.enderio.core.common.handlers;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.JANUARY;
import static java.util.Calendar.MONTH;

import java.util.Calendar;

import javax.annotation.Nonnull;

import com.enderio.core.EnderCore;
import com.enderio.core.common.Handlers.Handler;
import com.enderio.core.common.config.ConfigHandler;
import com.enderio.core.common.util.EntityUtil;
import com.enderio.core.common.util.NullHelper;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

@Handler
public class FireworkHandler {
  @SubscribeEvent
  public void onAchievement(AdvancementEvent event) {
    final @Nonnull Advancement advancement = NullHelper.notnullF(event.getAdvancement(), "AdvancementEvent.getAdvancement()");
    DisplayInfo display = advancement.getDisplay();
    if (ConfigHandler.betterAchievements && !event.getEntity().world.isRemote && display != null && display.shouldAnnounceToChat()) {
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

    if (!player.world.isRemote && event.phase == Phase.END) {
      if (player.world.getTotalWorldTime() % 100 == 0) {
        Calendar cal = Calendar.getInstance();
        if (cal.get(DAY_OF_MONTH) == 1 && cal.get(MONTH) == JANUARY
            && !player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getBoolean("celebrated")) {
          player.getEntityData().setInteger("fireworksLeft", 15);
          player.getEntityData().setBoolean("fireworkDelay", false);
          NBTTagCompound tag = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
          tag.setBoolean("celebrated", true);
          player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, tag);
          player.sendMessage(new TextComponentString(TextFormatting.AQUA + EnderCore.lang.localize("celebrate")));
        }
      }

      int fireworksLeft = player.getEntityData().getInteger("fireworksLeft");
      if (fireworksLeft > 0 && (!player.getEntityData().getBoolean("fireworkDelay") || player.world.getTotalWorldTime() % 20 == 0)) {
        BlockPos pos = new BlockPos(player).up(2);
        EntityUtil.spawnFirework(pos, player.world.provider.getDimension(), 12);
        player.getEntityData().setInteger("fireworksLeft", fireworksLeft - 1);

        if (fireworksLeft > 5) {
          player.getEntityData().setBoolean("fireworkDelay", true);
        } else {
          player.getEntityData().setBoolean("fireworkDelay", false);
        }
      }
    }
  }

}
