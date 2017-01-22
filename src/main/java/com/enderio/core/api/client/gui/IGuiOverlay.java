package com.enderio.core.api.client.gui;

import java.awt.Rectangle;

import javax.annotation.Nonnull;

public interface IGuiOverlay extends IHideable {

    void init(@Nonnull IGuiScreen screen);

    @Nonnull Rectangle getBounds();

    void draw(int mouseX, int mouseY, float partialTick);

    // //consume event?
    // boolean mouseClicked(int par1, int par2, int par3);
    //
    // boolean mouseClickMove(int par1, int par2, int par3, long p_146273_4_);
    //
    // boolean mouseMovedOrUp(int par1, int par2, int par3);

    boolean handleMouseInput(int x, int y, int b);

    boolean isMouseInBounds(int mouseX, int mouseY);
    
    void guiClosed();

}
