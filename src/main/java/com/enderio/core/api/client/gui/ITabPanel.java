package com.enderio.core.api.client.gui;

import com.enderio.core.api.client.render.IWidgetIcon;

import net.minecraft.client.gui.GuiButton;

public interface ITabPanel {

    void onGuiInit(int x, int y, int width, int height);

    void deactivate();

    IWidgetIcon getIcon();

    void render(float par1, int par2, int par3);

    void actionPerformed(GuiButton guiButton);

    void mouseClicked(int x, int y, int par3);

    void keyTyped(char par1, int par2);

    void updateScreen();

}
