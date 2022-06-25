package com.enderio.core.api.client.gui;

import net.minecraft.client.gui.GuiButton;

import com.enderio.core.api.client.render.IWidgetIcon;
import net.minecraft.util.ResourceLocation;

public interface ITabPanel {

  void onGuiInit(int x, int y, int width, int height);

  void deactivate();

  IWidgetIcon getIcon();

  void render(float par1, int par2, int par3);

  void actionPerformed(GuiButton guiButton);

  void mouseClicked(int x, int y, int par3);

  void keyTyped(char par1, int par2);

  void updateScreen();

  /**
   * @return The location of the texture used for slot backgrounds, etc.
   */
  default ResourceLocation getTexture() {
    return new ResourceLocation("enderio", "textures/gui/23/externalConduitConnection.png");
  }

}
