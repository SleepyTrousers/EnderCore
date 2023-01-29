package com.enderio.core.api.client.gui;

import java.util.List;

import net.minecraft.client.gui.GuiButton;

import com.enderio.core.client.gui.widget.GhostSlot;
import com.enderio.core.client.gui.widget.GuiToolTip;

public interface IGuiScreen {

    void addToolTip(GuiToolTip toolTip);

    boolean removeToolTip(GuiToolTip toolTip);

    int getGuiLeft();

    int getGuiTop();

    int getXSize();

    int getYSize();

    void addButton(GuiButton button);

    void removeButton(GuiButton button);

    int getOverlayOffsetX();

    void doActionPerformed(GuiButton but);

    List<GhostSlot> getGhostSlots();

}
