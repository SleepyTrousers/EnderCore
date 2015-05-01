package com.enderio.core.api.client.render;

public interface IWidgetIcon {

  int getX();

  int getY();

  int getWidth();

  int getHeight();

  IWidgetIcon getOverlay();

  IWidgetMap getMap();
}
