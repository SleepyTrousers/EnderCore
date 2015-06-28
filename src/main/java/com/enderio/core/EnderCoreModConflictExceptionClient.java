package com.enderio.core;

import cpw.mods.fml.client.CustomModLoadingErrorDisplayException;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;

import static com.enderio.core.EnderCore.lang;

@SideOnly(Side.CLIENT)
public class EnderCoreModConflictExceptionClient extends CustomModLoadingErrorDisplayException {

  @Override
  public void initGui(GuiErrorScreen errorScreen, FontRenderer fontRenderer) {
  }

  @Override
  public void drawScreen(GuiErrorScreen errorScreen, FontRenderer fontRenderer, int mouseRelX, int mouseRelY, float tickTime) {
    errorScreen.drawCenteredString(fontRenderer, lang.localize("error.ttcore.1"), errorScreen.width / 2, errorScreen.height / 2 - 20, 0xFFFFFF);
    errorScreen.drawCenteredString(fontRenderer, lang.localize("error.ttcore.2"), errorScreen.width / 2, errorScreen.height / 2 - 10, 0xFFFFFF);
    errorScreen.drawCenteredString(fontRenderer, lang.localize("error.ttcore.3"), errorScreen.width / 2, errorScreen.height / 2, 0xFFFFFF);
  }
}
