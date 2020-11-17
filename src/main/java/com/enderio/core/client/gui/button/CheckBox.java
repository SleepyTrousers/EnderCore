package com.enderio.core.client.gui.button;

import javax.annotation.Nonnull;

import com.enderio.core.api.client.gui.IGuiScreen;
import com.enderio.core.client.render.EnderWidget;

public class CheckBox extends ToggleButton {
  public CheckBox(@Nonnull IGuiScreen gui, int x, int y) {
    super(gui, x, y, EnderWidget.BUTTON, EnderWidget.BUTTON_CHECKED);
  }

  public CheckBox(@Nonnull IGuiScreen gui, int x, int y, IPressable pressedAction) {
    super(gui, x, y, EnderWidget.BUTTON, EnderWidget.BUTTON_CHECKED, pressedAction);
  }
}
