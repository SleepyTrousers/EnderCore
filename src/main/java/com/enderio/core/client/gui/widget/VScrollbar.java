package com.enderio.core.client.gui.widget;

import java.awt.Rectangle;

import com.enderio.core.api.client.gui.IHideable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

import com.enderio.core.api.client.gui.IGuiScreen;
import com.enderio.core.api.client.render.IWidgetIcon;
import com.enderio.core.client.render.EnderWidget;
import com.enderio.core.client.render.RenderUtil;

public class VScrollbar implements IHideable {

  protected final IGuiScreen gui;

  protected int xOrigin;
  protected int yOrigin;
  protected int height;

  protected int xPosition;
  protected int yPosition;
  protected Rectangle wholeArea;
  protected Rectangle btnUp;
  protected Rectangle btnDown;
  protected Rectangle thumbArea;

  protected int scrollPos;
  protected int scrollMax;

  protected boolean pressedUp;
  protected boolean pressedDown;
  protected boolean pressedThumb;
  protected int scrollDir;
  protected long timeNextScroll;

  protected boolean visible = true;

  public VScrollbar(IGuiScreen gui, int xOrigin, int yOrigin, int height) {
    this.gui = gui;
    this.xOrigin = xOrigin;
    this.yOrigin = yOrigin;
    this.height = height;
  }

  public void adjustPosition() {
    xPosition = xOrigin + gui.getGuiLeft();
    yPosition = yOrigin + gui.getGuiTop();
    wholeArea = new Rectangle(xPosition, yPosition, (int) EnderWidget.VSCROLL_THUMB_OFF.width, height);
    btnUp = new Rectangle(xPosition, yPosition, (int) EnderWidget.UP_ARROW_OFF.width, (int) EnderWidget.UP_ARROW_OFF.height);
    btnDown = new Rectangle(xPosition, yPosition + Math.max(0, height - (int) EnderWidget.DOWN_ARROW_OFF.height), (int) EnderWidget.DOWN_ARROW_OFF.width,
        (int) EnderWidget.DOWN_ARROW_OFF.height);
    thumbArea = new Rectangle(xPosition, yPosition + btnUp.height, (int) EnderWidget.VSCROLL_THUMB_OFF.width, Math.max(0, height
        - (btnUp.height + btnDown.height)));
  }

  public int getScrollPos() {
    return scrollPos;
  }

  public void setScrollPos(int scrollPos) {
    this.scrollPos = limitPos(scrollPos);
  }

  public void scrollBy(int amount) {
    setScrollPos(scrollPos + amount);
  }

  public int getScrollMax() {
    return scrollMax;
  }

  public void setScrollMax(int scrollMax) {
    this.scrollMax = scrollMax;
    setScrollPos(scrollPos);
  }

  public void drawScrollbar(int mouseX, int mouseY) {
    if (visible) {
      boolean hoverUp = btnUp.contains(mouseX, mouseY);
      boolean hoverDown = btnDown.contains(mouseX, mouseY);

      IWidgetIcon iconUp;
      if (pressedUp) {
        iconUp = hoverUp ? EnderWidget.UP_ARROW_HOVER_ON : EnderWidget.UP_ARROW_ON;
      } else {
        iconUp = hoverUp ? EnderWidget.UP_ARROW_HOVER_OFF : EnderWidget.UP_ARROW_OFF;
      }

      IWidgetIcon iconDown;
      if (pressedDown) {
        iconDown = hoverDown ? EnderWidget.DOWN_ARROW_HOVER_ON : EnderWidget.DOWN_ARROW_ON;
      } else {
        iconDown = hoverDown ? EnderWidget.DOWN_ARROW_HOVER_OFF : EnderWidget.DOWN_ARROW_OFF;
      }

      if (scrollDir != 0) {
        long time = Minecraft.getSystemTime();
        if ((timeNextScroll - time) <= 0) {
          timeNextScroll = time + 100;
          scrollBy(scrollDir);
        }
      }

      RenderUtil.bindTexture(EnderWidget.TEXTURE);
      GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
      GL11.glEnable(GL11.GL_BLEND);
      GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
      GL11.glColor3f(1, 1, 1);

      Tessellator tes = Tessellator.instance;
      tes.startDrawingQuads();

      iconUp.getMap().render(iconUp, btnUp.x, btnUp.y, false);
      iconDown.getMap().render(iconDown, btnDown.x, btnDown.y, false);

      if (scrollMax > 0) {
        int thumbPos = getThumbPosition();
        boolean hoverThumb = thumbArea.contains(mouseX, mouseY) && mouseY >= thumbPos && mouseY < thumbPos + (int) EnderWidget.VSCROLL_THUMB_OFF.height;

        EnderWidget iconThumb;
        if (pressedThumb) {
          iconThumb = EnderWidget.VSCROLL_THUMB_HOVER_ON;
        } else {
          iconThumb = hoverThumb ? EnderWidget.VSCROLL_THUMB_HOVER_OFF : EnderWidget.VSCROLL_THUMB_OFF;
        }
        iconThumb.getMap().render(iconThumb, thumbArea.x, thumbPos, false);
      }

      tes.draw();
      GL11.glPopAttrib();
    }
  }

  public boolean mouseClicked(int x, int y, int button) {
    if (button == 0) {
      if (scrollMax > 0 && thumbArea.contains(x, y)) {
        int thumbPos = getThumbPosition();
        pressedUp = y < thumbPos;
        pressedDown = y >= thumbPos + (int) EnderWidget.VSCROLL_THUMB_OFF.height;
        pressedThumb = !pressedUp && !pressedDown;
      } else {
        pressedUp = btnUp.contains(x, y);
        pressedDown = btnDown.contains(x, y);
        pressedThumb = false;
      }

      scrollDir = (pressedDown ? 1 : 0) - (pressedUp ? 1 : 0);
      if (scrollDir != 0) {
        timeNextScroll = Minecraft.getSystemTime() + 200;
        scrollBy(scrollDir);
      }
    }
    return isDragActive();
  }

  public boolean mouseClickMove(int x, int y, int button, long time) {
    if (pressedThumb) {
      int pos = y - (thumbArea.y + (int) EnderWidget.VSCROLL_THUMB_OFF.height / 2);
      int len = thumbArea.height - (int) EnderWidget.VSCROLL_THUMB_OFF.height;
      if (len > 0) {
        setScrollPos(Math.round(pos * (float) scrollMax / (float) len));
      }
      return true;
    }
    return false;
  }

  public void mouseMovedOrUp(int x, int y, int button) {
    pressedUp = false;
    pressedDown = false;
    pressedThumb = false;
    scrollDir = 0;
  }

  public void mouseWheel(int x, int y, int delta) {
    if (!isDragActive() && wholeArea.contains(x, y)) {
      scrollBy(-Integer.signum(delta));
    }
  }

  public boolean isDragActive() {
    return pressedUp || pressedDown || pressedThumb;
  }

  protected int getThumbPosition() {
    return thumbArea.y + (thumbArea.height - (int) EnderWidget.VSCROLL_THUMB_OFF.height) * scrollPos / scrollMax;
  }

  protected int limitPos(int pos) {
    return Math.max(0, Math.min(pos, scrollMax));
  }

  @Override
  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  @Override
  public boolean isVisible() {
    return this.visible;
  }
}
