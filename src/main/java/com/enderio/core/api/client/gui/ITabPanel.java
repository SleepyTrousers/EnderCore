package com.enderio.core.api.client.gui;

import javax.annotation.Nonnull;

import com.enderio.core.api.client.render.IWidgetIcon;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;

public interface ITabPanel {

  void onGuiInit(int x, int y, int width, int height);

  void deactivate();

  @Nonnull
  IWidgetIcon getIcon();

  void render(float par1, int par2, int par3);

  void actionPerformed(@Nonnull Button guiButton);

  void mouseClicked(int x, int y, int par3);

  void keyTyped(char par1, int par2);

  void updateScreen();

  /**
   * @return The location of the texture used for slot backgrounds, etc.
   */
  @Nonnull
  ResourceLocation getTexture();

}
