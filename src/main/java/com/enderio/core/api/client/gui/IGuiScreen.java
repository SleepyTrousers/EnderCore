package com.enderio.core.api.client.gui;

import java.io.IOException;

import javax.annotation.Nonnull;

import com.enderio.core.client.gui.GhostSlotHandler;
import com.enderio.core.client.gui.widget.TooltipWidget;
import net.minecraft.client.gui.widget.button.Button;


public interface IGuiScreen {

  void addToolTip(@Nonnull TooltipWidget toolTip);

  boolean removeToolTip(@Nonnull TooltipWidget toolTip);

  void clearToolTips();

  int getGuiRootLeft();

  int getGuiRootTop();

  int getGuiXSize();

  int getGuiYSize();

  @Nonnull
  <T extends Button> T addGuiButton(@Nonnull T button);

  void removeButton(@Nonnull Button button);

  int getOverlayOffsetXLeft();

  int getOverlayOffsetXRight();

  void doActionPerformed(@Nonnull Button but) throws IOException;

  @Nonnull
  GhostSlotHandler getGhostSlotHandler();

}
