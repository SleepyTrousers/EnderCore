package com.enderio.core.api.client.gui;

import java.io.IOException;

import javax.annotation.Nonnull;

import com.enderio.core.client.gui.GhostSlotHandler;
import com.enderio.core.client.gui.widget.GuiToolTip;

import net.minecraft.client.gui.GuiButton;

public interface IGuiScreen {

  void addToolTip(@Nonnull GuiToolTip toolTip);

  boolean removeToolTip(@Nonnull GuiToolTip toolTip);

  void clearToolTips();

  int getGuiLeft();

  int getGuiTop();

  int getXSize();

  int getYSize();

  @Nonnull
  <T extends GuiButton> T addButton(@Nonnull T button);

  void removeButton(@Nonnull GuiButton button);

  int getOverlayOffsetX();

  void doActionPerformed(@Nonnull GuiButton but) throws IOException;

  @Nonnull
  GhostSlotHandler getGhostSlotHandler();

}
