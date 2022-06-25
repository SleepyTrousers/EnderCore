package com.enderio.core.client.gui.button;

import javax.annotation.Nonnull;

import com.enderio.core.api.client.gui.IGuiScreen;

import net.minecraft.client.Minecraft;

public class InvisibleButton extends TooltipButton {

    private static final int DEFAULT_WIDTH = 8;
    private static final int DEFAULT_HEIGHT = 6;

    public InvisibleButton(@Nonnull IGuiScreen gui, int id, int x, int y) {
        super(gui, id, x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, "");
    }

    public InvisibleButton(@Nonnull IGuiScreen gui, int id, int x, int y, int width, int height) {
        super(gui, id, x, y, width, height, "");
    }

    /**
     * Draws this button to the screen.
     */
    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY) {
        updateTooltip(mc, mouseX, mouseY);
    }

}
