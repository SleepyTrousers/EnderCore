package com.enderio.core.client.gui.button;

import com.enderio.core.api.client.gui.IHideable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

public class HideableButton extends BaseButton implements IHideable {
  public HideableButton(int x, int y, int width, int height, ITextComponent buttonText) {
    super(x, y, width, height, buttonText);
  }

  public HideableButton(int x, int y, int width, int height, ITextComponent buttonText, IPressable pressedAction) {
    super(x, y, width, height, buttonText, pressedAction);
  }

  public HideableButton(int x, int y, int width, int height, ITextComponent buttonText, Button.ITooltip onTooltip) {
    super(x, y, width, height, buttonText, onTooltip);
  }

  public HideableButton(int x, int y, int width, int height, ITextComponent buttonText, IPressable pressedAction, Button.ITooltip onTooltip) {
    super(x, y, width, height, buttonText, pressedAction, onTooltip);
  }

  @Override
  public void setIsVisible(boolean visible) {
    this.visible = visible;
  }

  @Override
  public boolean isVisible() {
    return visible;
  }
}
