package com.enderio.core.client.gui.widget;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GuiToolTip implements com.enderio.core.api.client.gui.IHideable {

  private static final long DELAY = 0;

  protected @Nonnull Rectangle bounds;

  private long mouseOverStart;

  protected final @Nonnull List<String> text;

  private int lastMouseX = -1;

  private int lastMouseY = -1;

  private boolean visible = true;

  public GuiToolTip(@Nonnull Rectangle bounds, String... lines) {
    this.bounds = bounds;
    if (lines != null) {
      text = new ArrayList<String>(lines.length);
      for (String line : lines) {
        text.add(line);
      }
    } else {
      text = new ArrayList<String>();
    }
  }

  public GuiToolTip(@Nonnull Rectangle bounds, @Nullable List<String> lines) {
    this.bounds = bounds;
    if (lines == null) {
      text = new ArrayList<String>();
    } else {
      text = new ArrayList<String>(lines);
    }
  }

  @Override
  public boolean isVisible() {
    return visible;
  }

  @Override
  public void setIsVisible(boolean visible) {
    this.visible = visible;
  }

  public @Nonnull Rectangle getBounds() {
    return bounds;
  }

  public void setBounds(@Nonnull Rectangle bounds) {
    this.bounds = bounds;
  }

  public void onTick(int mouseX, int mouseY) {
    if (lastMouseX != mouseX || lastMouseY != mouseY) {
      mouseOverStart = 0;
    }

    if (isVisible() && getBounds().contains(mouseX, mouseY)) {

      if (mouseOverStart == 0) {
        mouseOverStart = System.currentTimeMillis();
      }
    } else {
      mouseOverStart = 0;
    }

    lastMouseX = mouseX;
    lastMouseY = mouseY;
  }

  public boolean shouldDraw() {
    if (!isVisible()) {
      return false;
    }
    updateText();
    if (mouseOverStart == 0) {
      return false;
    }
    return System.currentTimeMillis() - mouseOverStart >= DELAY;
  }

  protected void updateText() {
  }

  public int getLastMouseX() {
    return lastMouseX;
  }

  public void setLastMouseX(int lastMouseX) {
    this.lastMouseX = lastMouseX;
  }

  public int getLastMouseY() {
    return lastMouseY;
  }

  public void setLastMouseY(int lastMouseY) {
    this.lastMouseY = lastMouseY;
  }

  public void setToolTipText(String... txt) {
    text.clear();
    if (txt != null) {
      for (String line : txt) {
        text.add(line);
      }
    }
  }

  public @Nonnull List<String> getToolTipText() {
    return text;
  }

}
