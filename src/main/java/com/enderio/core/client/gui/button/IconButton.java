package com.enderio.core.client.gui.button;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.opengl.GL11;

import com.enderio.core.api.client.gui.IGuiScreen;
import com.enderio.core.api.client.render.IWidgetIcon;
import com.enderio.core.client.render.EnderWidget;

import net.minecraft.client.Minecraft;

public class IconButton extends TooltipButton {

  public static final int DEFAULT_WIDTH = 16;
  public static final int DEFAULT_HEIGHT = 16;

  protected @Nullable IWidgetIcon icon;

  private int marginY = 0;
  private int marginX = 0;

  public IconButton(@Nonnull IGuiScreen gui, int x, int y, @Nullable IWidgetIcon icon) {
    super(gui, x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, new StringTextComponent(""));
    this.icon = icon;
  }

  public IconButton(@Nonnull IGuiScreen gui, int x, int y, @Nullable IWidgetIcon icon, IPressable pressedAction) {
    super(gui, x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, new StringTextComponent(""), pressedAction);
    this.icon = icon;
  }

  @Override
  public IconButton setPosition(int x, int y) {
    super.setPosition(x, y);
    return this;
  }

  public IconButton setIconMargin(int x, int y) {
    marginX = x;
    marginY = y;
    return this;
  }

  public @Nullable IWidgetIcon getIcon() {
    return icon;
  }

  public void setIcon(@Nullable IWidgetIcon icon) {
    this.icon = icon;
  }

  @Override
  public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    updateTooltip(mouseX, mouseY);
    if (isVisible()) {
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

      // TODO: WHY
//      mouseDragged(mouseX, mouseY);

      IWidgetIcon background = getIconForState();

      GL11.glColor3f(1, 1, 1);

      GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
      GL11.glEnable(GL11.GL_BLEND);
      GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

      background.getMap().render(background, x, y, width, height, 0, true);
      final @Nullable IWidgetIcon icon2 = icon;
      if (icon2 != null) {
        icon2.getMap().render(icon2, x + marginX, y + marginY, width - 2 * marginX, height - 2 * marginY, 0, true);
      }

      GL11.glPopAttrib();
    }
  }

  protected @Nonnull IWidgetIcon getIconForState() {
    if (!isActive()) {
      return EnderWidget.BUTTON_DISABLED;
    }
    if (isHovered()) {
      return EnderWidget.BUTTON_HIGHLIGHT;
    }
    return EnderWidget.BUTTON;
  }
}
