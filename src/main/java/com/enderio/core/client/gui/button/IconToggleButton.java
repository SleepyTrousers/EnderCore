package com.enderio.core.client.gui.button;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

public class IconToggleButton extends IIconButton {

  private boolean selected = false;

  public IconToggleButton(@Nonnull FontRenderer fr, int x, int y, @Nullable TextureAtlasSprite icon, @Nullable ResourceLocation texture) {
    super(fr, x, y, icon, texture);
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  @Override
  public boolean isHovered() {
    if (selected)
      return false;
    return super.isHovered();
  }
}
