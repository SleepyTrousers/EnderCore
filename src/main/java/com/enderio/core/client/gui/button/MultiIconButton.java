package com.enderio.core.client.gui.button;

import javax.annotation.Nonnull;

import com.enderio.core.api.client.gui.IGuiScreen;
import com.enderio.core.api.client.render.IWidgetIcon;
import com.enderio.core.client.render.EnderWidget;

public class MultiIconButton extends IconButton {

  private final @Nonnull IWidgetIcon unpressed;
  private final @Nonnull IWidgetIcon pressed;
  private final @Nonnull IWidgetIcon hover;

  public MultiIconButton(@Nonnull IGuiScreen gui, int id, int x, int y, @Nonnull IWidgetIcon unpressed, @Nonnull IWidgetIcon pressed,
      @Nonnull IWidgetIcon hover) {
    super(gui, id, x, y, null);
    this.unpressed = unpressed;
    this.pressed = pressed;
    this.hover = hover;
    setSize(unpressed.getWidth(), unpressed.getHeight());
  }

  @Override
  protected @Nonnull IWidgetIcon getIconForHoverState(int hoverState) {
    if (hoverState == 0) {
      return pressed;
    }
    if (hoverState == 2) {
      return hover;
    }
    return unpressed;
  }

  public static @Nonnull MultiIconButton createRightArrowButton(@Nonnull IGuiScreen gui, int id, int x, int y) {
    return new MultiIconButton(gui, id, x, y, EnderWidget.RIGHT_ARROW, EnderWidget.RIGHT_ARROW_PRESSED, EnderWidget.RIGHT_ARROW_HOVER);
  }

  public static @Nonnull MultiIconButton createLeftArrowButton(@Nonnull IGuiScreen gui, int id, int x, int y) {
    return new MultiIconButton(gui, id, x, y, EnderWidget.LEFT_ARROW, EnderWidget.LEFT_ARROW_PRESSED, EnderWidget.LEFT_ARROW_HOVER);
  }

  public static @Nonnull MultiIconButton createAddButton(@Nonnull IGuiScreen gui, int id, int x, int y) {
    return new MultiIconButton(gui, id, x, y, EnderWidget.ADD_BUT, EnderWidget.ADD_BUT_PRESSED, EnderWidget.ADD_BUT_HOVER);
  }

  public static @Nonnull MultiIconButton createMinusButton(@Nonnull IGuiScreen gui, int id, int x, int y) {
    return new MultiIconButton(gui, id, x, y, EnderWidget.MINUS_BUT, EnderWidget.MINUS_BUT_PRESSED, EnderWidget.MINUS_BUT_HOVER);
  }
}
