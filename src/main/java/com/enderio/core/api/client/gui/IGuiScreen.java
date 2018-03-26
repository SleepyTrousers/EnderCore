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

  int getGuiRootLeft();

  int getGuiRootTop();

  int getGuiXSize();

  int getGuiYSize();

  @Nonnull
  <T extends GuiButton> T addGuiButton(@Nonnull T button);

  void removeButton(@Nonnull GuiButton button);

  int getOverlayOffsetXLeft();

  int getOverlayOffsetXRight();

  void doActionPerformed(@Nonnull GuiButton but) throws IOException;

  @Nonnull
  GhostSlotHandler getGhostSlotHandler();

}
