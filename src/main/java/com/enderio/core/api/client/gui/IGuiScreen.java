package com.enderio.core.api.client.gui;

import java.io.IOException;
import java.util.List;

import com.enderio.core.client.gui.widget.GhostSlot;
import com.enderio.core.client.gui.widget.GuiToolTip;

import net.minecraft.client.gui.GuiButton;

public interface IGuiScreen {

    void addToolTip(GuiToolTip toolTip);

    boolean removeToolTip(GuiToolTip toolTip);

    void clearToolTips();

    int getGuiLeft();

    int getGuiTop();

    int getXSize();

    int getYSize();

    void addButton(GuiButton button);

    void removeButton(GuiButton button);

    int getOverlayOffsetX();

    void doActionPerformed(GuiButton but) throws IOException;

    List<GhostSlot> getGhostSlots();

}
