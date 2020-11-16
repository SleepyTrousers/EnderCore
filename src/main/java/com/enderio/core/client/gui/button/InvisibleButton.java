package com.enderio.core.client.gui.button;

import javax.annotation.Nonnull;

import com.enderio.core.api.client.gui.IGuiScreen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;

public class InvisibleButton extends TooltipButton {
  private static final int DEFAULT_WIDTH = 8;
  private static final int DEFAULT_HEIGHT = 6;

  public InvisibleButton(@Nonnull IGuiScreen gui, int x, int y, IPressable pressedAction) {
    super(gui, x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, new StringTextComponent(""), pressedAction);
  }

  public InvisibleButton(@Nonnull IGuiScreen gui, int x, int y, int width, int height, IPressable pressedAction) {
    super(gui, x, y, width, height, new StringTextComponent(""), pressedAction);
  }

  @Override
  public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    updateTooltip(mouseX, mouseY);
  }
}
