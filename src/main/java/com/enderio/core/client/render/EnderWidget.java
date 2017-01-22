package com.enderio.core.client.render;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.EnderCore;
import com.enderio.core.api.client.render.IWidgetIcon;
import com.enderio.core.api.client.render.IWidgetMap;

import net.minecraft.util.ResourceLocation;

public enum EnderWidget implements IWidgetIcon {

  BUTTON_BASE(0, 0),
  UP_ARROW_OFF(212, 64, 11, 8),
  UP_ARROW_ON(223, 64, 11, 8),
  UP_ARROW_HOVER_OFF(234, 64, 11, 8),
  UP_ARROW_HOVER_ON(245, 64, 11, 8),
  DOWN_ARROW_OFF(212, 72, 11, 8),
  DOWN_ARROW_ON(223, 72, 11, 8),
  DOWN_ARROW_HOVER_OFF(234, 72, 11, 8),
  DOWN_ARROW_HOVER_ON(245, 72, 11, 8),

  TICK(0, 192),
  MINUS(16, 192),
  CROSS(64, 192),
  PLUS(80, 192),

  VSCROLL_THUMB_OFF(234, 80, 11, 8),
  VSCROLL_THUMB_HOVER_OFF(234, 88, 11, 8),
  VSCROLL_THUMB_ON(245, 80, 11, 8),
  VSCROLL_THUMB_HOVER_ON(245, 88, 11, 8),

  BUTTON(0, 208),
  BUTTON_HIGHLIGHT(16, 208),
  BUTTON_DISABLED(32, 208),
  BUTTON_DOWN(48, 208),
  BUTTON_DOWN_HIGHLIGHT(64, 208),
  BUTTON_CHECKED(112, 208),

  LEFT_ARROW(224, 32, 8, 16),
  LEFT_ARROW_PRESSED(240, 32, 8, 16),
  LEFT_ARROW_HOVER(224, 48, 8, 16),
  LEFT_ARROW_HOVER_PRESSED(240, 48, 8, 16),

  RIGHT_ARROW(232, 32, 8, 16),
  RIGHT_ARROW_PRESSED(248, 32, 8, 16),
  RIGHT_ARROW_HOVER(232, 48, 8, 16),
  RIGHT_ARROW_HOVER_PRESSED(248, 48, 8, 16),

  ADD_BUT(208, 32, 8, 8),
  ADD_BUT_PRESSED(216, 32, 8, 8),
  ADD_BUT_HOVER(208, 48, 8, 8),
  ADD_BUT_HOVER_PRESSED(216, 48, 8, 8),

  MINUS_BUT(208, 40, 8, 8),
  MINUS_BUT_PRESSED(216, 40, 8, 8),
  MINUS_BUT_HOVER(208, 56, 8, 8),
  MINUS_BUT_HOVER_PRESSED(216, 56, 8, 8),

  X_BUT(200, 32, 8, 8),
  X_BUT_PRESSED(200, 40, 8, 8),
  X_BUT_HOVER(200, 48, 8, 8),
  X_BUT_HOVER_PRESSED(200, 56, 8, 8),

  STOP_BUT(200, 64, 8, 8),
  RETURN_BUT(200, 72, 8, 8),
  STOP_BUT_HOVER(200, 80, 8, 8),
  RETURN_BUT_HOVER(200, 88, 8, 8),

  ;

  public static final @Nonnull ResourceLocation TEXTURE = new ResourceLocation(EnderCore.MODID, "textures/gui/widgets.png");

  public static final @Nonnull IWidgetMap map = new IWidgetMap.WidgetMapImpl(256, TEXTURE);

  public final int x;
  public final int y;
  public final int width;
  public final int height;
  public final @Nullable IWidgetIcon overlay;

  EnderWidget(int x, int y) {
    this(x, y, null);
  }

  EnderWidget(int x, int y, @Nullable IWidgetIcon overlay) {
    this(x, y, 16, 16, overlay);
  }

  EnderWidget(int x, int y, int width, int height) {
    this(x, y, width, height, null);
  }

  EnderWidget(int x, int y, int width, int height, @Nullable IWidgetIcon overlay) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.overlay = overlay;
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
  public @Nullable IWidgetIcon getOverlay() {
    return overlay;
  }

  @Override
  public @Nonnull IWidgetMap getMap() {
    return map;
  }
}
