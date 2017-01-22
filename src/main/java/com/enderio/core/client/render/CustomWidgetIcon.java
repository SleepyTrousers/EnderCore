package com.enderio.core.client.render;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.api.client.render.IWidgetIcon;
import com.enderio.core.api.client.render.IWidgetMap;

public class CustomWidgetIcon implements IWidgetIcon {

  private final int x, y, width, height;
  private final @Nonnull IWidgetMap map;

  private @Nullable IWidgetIcon overlay;

  public CustomWidgetIcon(int x, int y, int width, int height, @Nonnull IWidgetMap map, @Nullable IWidgetIcon overlay) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.map = map;
    this.overlay = overlay;
  }

  public CustomWidgetIcon(int x, int y, int width, int height, @Nonnull IWidgetMap map) {
    this(x, y, width, height, map, null);
  }

  @Override
  public int getX() {
    return x;
  }

  @Override
  public int getY() {
    return y;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public @Nonnull IWidgetMap getMap() {
    return map;
  }

  @Override
  public @Nullable IWidgetIcon getOverlay() {
    return overlay;
  }
}
