package com.enderio.core.client.gui.button;

import java.awt.Rectangle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.api.client.gui.IGuiScreen;
import com.enderio.core.client.gui.widget.GuiToolTip;

import net.minecraft.client.Minecraft;

public class TooltipButton extends GuiButtonHideable {

  protected int xOrigin;
  protected int yOrigin;
  protected @Nonnull IGuiScreen gui;
  protected @Nullable String[] toolTipText;
  protected @Nullable GuiToolTip toolTip;

  public TooltipButton(@Nonnull IGuiScreen gui, int id, int x, int y, int widthIn, int heightIn, @Nonnull String buttonText) {
    super(id, x, y, widthIn, heightIn, buttonText);
    this.gui = gui;
    this.xOrigin = x;
    this.yOrigin = y;
  }

  public void setToolTip(String... tooltipText) {
    if (toolTip != null) {
      toolTip.setToolTipText(tooltipText);
    } else {
      toolTip = new GuiToolTip(getBounds(), tooltipText);
    }
    this.toolTipText = tooltipText;
    updateTooltipBounds();
  }

  public void setToolTip(GuiToolTip newToolTip) {
    boolean addTooltip = false;
    if (toolTip != null) {
      addTooltip = gui.removeToolTip(toolTip);
    }
    toolTip = newToolTip;
    if (addTooltip && toolTip != null) {
      gui.addToolTip(toolTip);
    }
    updateTooltipBounds();
  }

  public final @Nonnull Rectangle getBounds() {
    return new Rectangle(xOrigin, yOrigin, getWidth(), getHeight());
  }

  public void onGuiInit() {
    gui.addGuiButton(this);
    if (toolTip != null) {
      gui.addToolTip(toolTip);
    }
    this.x = xOrigin + gui.getGuiRootLeft();
    this.y = yOrigin + gui.getGuiRootTop();
  }

  public void detach() {
    if (toolTip != null) {
      gui.removeToolTip(toolTip);
    }
    gui.removeButton(this);
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public @Nullable GuiToolTip getToolTip() {
    return toolTip;
  }

  public void setSize(int width, int height) {
    this.width = width;
    this.height = height;
    updateTooltipBounds();
  }

  public TooltipButton setPosition(int x, int y) {
    xOrigin = x;
    yOrigin = y;
    updateTooltipBounds();
    return this;
  }

  public void setXOrigin(int xOrigin) {
    this.xOrigin = xOrigin;
  }

  public void setYOrigin(int yOrigin) {
    this.yOrigin = yOrigin;
  }

  private void updateTooltipBounds() {
    if (toolTip != null) {
      toolTip.setBounds(new Rectangle(xOrigin, yOrigin, width, height));
    }
  }

  protected void updateTooltip(@Nonnull Minecraft mc, int mouseX, int mouseY) {
    if (toolTip != null) {
      toolTip.setIsVisible(visible && enabled);
    }
  }

  protected final void doDrawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
    super.drawButton(mc, mouseX, mouseY, partialTicks);
  }

  /**
   * Draws this button to the screen.
   */
  @Override
  public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
    updateTooltip(mc, mouseX, mouseY);
    doDrawButton(mc, mouseX, mouseY, partialTicks);
  }

}