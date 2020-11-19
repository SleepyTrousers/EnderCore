package com.enderio.core.common.handlers;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.JANUARY;
import static java.util.Calendar.MONTH;

import java.util.Calendar;

import javax.annotation.Nonnull;

import com.enderio.core.EnderCore;
import com.enderio.core.common.util.EntityUtil;
import com.enderio.core.common.util.NullHelper;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class FireworkHandler {
  @SubscribeEvent
  public static void onAchievement(AdvancementEvent event) {
    final @Nonnull Advancement advancement = NullHelper.notnullF(event.getAdvancement(), "AdvancementEvent.getAdvancement()");
    DisplayInfo display = advancement.getDisplay();
    // TODO: Config:
//    if (ConfigHandler.betterAchievements && !event.getEntity().world.isRemote && display != null && display.shouldAnnounceToChat()) {
    if (!event.getEntity().world.isRemote && display != null && display.shouldAnnounceToChat()) {
      event.getPlayer().getPersistentData().putInt("fireworksLeft", 9);
      event.getPlayer().getPersistentData().putBoolean("fireworkDelay", false);
    }
  }

  @SubscribeEvent
  public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
    // TODO: Config:
//    if(!ConfigHandler.newYearsFireworks) {
//      return;
//    }
    
    PlayerEntity player = event.player;

    if (!player.world.isRemote && event.phase == TickEvent.Phase.END) {
      if (player.world.getGameTime() % 100 == 0) {
        Calendar cal = Calendar.getInstance();
        if (cal.get(DAY_OF_MONTH) == 1 && cal.get(MONTH) == JANUARY
            && !player.getPersistentData().getBoolean("celebrated")) {
          player.getPersistentData().putInt("fireworksLeft", 15);
          player.getPersistentData().putBoolean("fireworkDelay", false);
          player.getPersistentData().putBoolean("celebrated", false);
          player.sendMessage(new StringTextComponent(TextFormatting.AQUA + EnderCore.lang.localize("celebrate")), Util.DUMMY_UUID);
        }
      }

      int fireworksLeft = player.getPersistentData().getInt("fireworksLeft");
      if (fireworksLeft > 0 && (!player.getPersistentData().getBoolean("fireworkDelay") || player.world.getGameTime() % 20 == 0)) {
        BlockPos pos = player.getPosition().up(2);
        EntityUtil.spawnFirework(pos, player.world, 12);
        player.getPersistentData().putInt("fireworksLeft", fireworksLeft - 1);

        if (fireworksLeft > 5) {
          player.getPersistentData().putBoolean("fireworkDelay", true);
        } else {
          player.getPersistentData().putBoolean("fireworkDelay", false);
        }
      }
    }
  }

}
