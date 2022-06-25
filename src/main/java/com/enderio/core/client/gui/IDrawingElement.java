package com.enderio.core.client.gui;

import javax.annotation.Nullable;

import com.enderio.core.client.gui.widget.GuiToolTip;

public interface IDrawingElement {

    @Nullable
    GuiToolTip getTooltip();

    void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY);

}
