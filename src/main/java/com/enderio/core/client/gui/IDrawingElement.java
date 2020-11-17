package com.enderio.core.client.gui;

import javax.annotation.Nullable;

import com.enderio.core.client.gui.widget.TooltipWidget;

public interface IDrawingElement {

  @Nullable
  TooltipWidget getTooltip();

  void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY);

}
