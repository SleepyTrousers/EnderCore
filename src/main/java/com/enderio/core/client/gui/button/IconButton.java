package com.enderio.core.client.gui.button;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.enderio.core.api.client.gui.IGuiScreen;
import com.enderio.core.api.client.render.IWidgetIcon;
import com.enderio.core.client.render.EnderWidget;

import net.minecraft.client.Minecraft;

public class IconButton extends TooltipButton implements IButtonAwareButton {

  public static final int DEFAULT_WIDTH = 16;
  public static final int DEFAULT_HEIGHT = 16;

  protected @Nullable IWidgetIcon icon;

  private int marginY = 0;
  private int marginX = 0;

  public IconButton(@Nonnull IGuiScreen gui, int id, int x, int y, @Nullable IWidgetIcon icon) {
    super(gui, id, x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, "");
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

  /**
   * @deprecated use {@link #mousePressedButton(Minecraft, int, int, int)}
   */
  @Deprecated
  @Override
  public boolean mousePressed(@Nonnull Minecraft mc, int mouseX, int mouseY) {
    return mousePressedButton(mc, mouseX, mouseY, 0);
  }

  @Override
  public boolean mousePressedButton(@Nonnull Minecraft mc, int mouseX, int mouseY, int button) {
    return button == 0 && checkMousePress(mc, mouseX, mouseY);
  }

  @Override
  public boolean checkMousePress(@Nonnull Minecraft mc, int mouseX, int mouseY) {
    // call super here so that we only get the area check
    return super.mousePressed(mc, mouseX, mouseY);
  }

  /**
   * Draws this button to the screen.
   */
  @Override
  public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
    updateTooltip(mc, mouseX, mouseY);
    if (isVisible()) {

      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
      int hoverState = getHoverState(hovered);
      mouseDragged(mc, mouseX, mouseY);

      IWidgetIcon background = getIconForHoverState(hoverState);

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

  protected @Nonnull IWidgetIcon getIconForHoverState(int hoverState) {
    if (hoverState == 0) {
      return EnderWidget.BUTTON_DISABLED;
    }
    if (hoverState == 2) {
      return EnderWidget.BUTTON_HIGHLIGHT;
    }
    return EnderWidget.BUTTON;
  }
}
