package com.enderio.core.client;

import cpw.mods.fml.client.CustomModLoadingErrorDisplayException;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;

@SideOnly(Side.CLIENT)
public class EnderCoreModConflictException extends CustomModLoadingErrorDisplayException {

  private final String[] msgs;

  public EnderCoreModConflictException(String[] msgs) {
    this.msgs = msgs;
  }

  @Override
  public void initGui(GuiErrorScreen errorScreen, FontRenderer fontRenderer) {
  }

  @Override
  public void drawScreen(GuiErrorScreen errorScreen, FontRenderer fontRenderer, int mouseRelX, int mouseRelY, float tickTime) {
    int y = errorScreen.height / 2 - msgs.length * 5;
    for(String msg : msgs) {
      errorScreen.drawCenteredString(fontRenderer, msg, errorScreen.width / 2, y, 0xFFFFFF);
      y += 10;
    }
  }
}
