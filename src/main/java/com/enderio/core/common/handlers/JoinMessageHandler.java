package com.enderio.core.common.handlers;

import com.enderio.core.EnderCore;
import com.enderio.core.common.Handlers.Handler;
import com.enderio.core.common.config.ConfigHandler;

import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

@Handler
public class JoinMessageHandler {

  @SubscribeEvent
  public void onPlayerJoin(PlayerLoggedInEvent event) {
    if(ConfigHandler.invisibleMode == 1 && ConfigHandler.instance().showInvisibleWarning()) {
      String unlocBase = "chat.invis";
      String warnBase = unlocBase + ".warn.";
      String reasonBase = unlocBase + ".reason.";
      String reason = String.format(EnderCore.lang.localize(reasonBase + (EnderCore.instance.invisibilityRequested() ? "1" : "2"),
          EnderCore.instance.getInvisibleRequsters()));
      String text1 = String.format(EnderCore.lang.localize(warnBase + "1"), TextFormatting.RED, TextFormatting.WHITE, reason);
      String text2 = String.format(EnderCore.lang.localize(warnBase + "2"), TextFormatting.BOLD, TextFormatting.WHITE);
      String text3 = EnderCore.lang.localize(warnBase + "3");
      String text4 = EnderCore.lang.localize(warnBase + "4");

      event.player.sendMessage(new TextComponentString(text1));
      event.player.sendMessage(new TextComponentString(text2));
      if(EnderCore.instance.invisibilityRequested()) {
        event.player.sendMessage(new TextComponentString(text3));
      }
      event.player.sendMessage(new TextComponentString(text4));
    }
  }
}
