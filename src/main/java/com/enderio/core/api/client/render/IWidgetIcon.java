package com.enderio.core.api.client.render;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public interface IWidgetIcon {

  int getX();

  int getY();

  int getWidth();

  int getHeight();

  @Nullable
  IWidgetIcon getOverlay();

  @Nonnull
  IWidgetMap getMap();

  @Nonnull
  default TextureAtlasSprite getAsTextureAtlasSprite() {
    return new TAS(this);
  }

  /**
   * TextureAtlasSprite that only has the data needed by Slot for a background image. Won't work anywhere where's animation data is needed.
   *
   */
  static class TAS extends TextureAtlasSprite {

    protected TAS(IWidgetIcon icon) {
      super(icon.getMap().getTexture().toString());
      setIconWidth(icon.getWidth());
      setIconHeight(icon.getHeight());
      initSprite(icon.getMap().getSize(), icon.getMap().getSize(), icon.getX(), icon.getY(), false);
    }

  }

}
