package com.enderio.core.api.client.render;

import org.lwjgl.opengl.GL11;

import com.enderio.core.client.render.RenderUtil;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public interface IWidgetMap {

  int getSize();

  ResourceLocation getTexture();

  void render(IWidgetIcon widget, double x, double y);

  void render(IWidgetIcon widget, double x, double y, boolean doDraw);

  void render(IWidgetIcon widget, double x, double y, boolean doDraw, boolean flipY);

  void render(IWidgetIcon widget, double x, double y, double zLevel, boolean doDraw);

  void render(IWidgetIcon widget, double x, double y, double zLevel, boolean doDraw, boolean flipY);

  void render(IWidgetIcon widget, double x, double y, double width, double height, double zLevel, boolean doDraw);

  void render(IWidgetIcon widget, double x, double y, double width, double height, double zLevel, boolean doDraw, boolean flipY);

  static class WidgetMapImpl implements IWidgetMap {

    private final int size;
    private final ResourceLocation res;
    
    public WidgetMapImpl(int size, ResourceLocation res) {
      this.size = size;
      this.res = res;
    }

    @Override
    public int getSize() {
      return size;
    }

    @Override
    public ResourceLocation getTexture() {
      return res;
    }

    @Override
    public void render(IWidgetIcon widget, double x, double y) {
      render(widget, x, y, false);
    }

    @Override
    public void render(IWidgetIcon widget, double x, double y, boolean doDraw) {
      render(widget, x, y, widget.getWidth(), widget.getHeight(), 0, doDraw);
    }

    @Override
    public void render(IWidgetIcon widget, double x, double y, boolean doDraw, boolean flipY) {
      render(widget, x, y, widget.getWidth(), widget.getHeight(), 0, doDraw, flipY);
    }

    @Override
    public void render(IWidgetIcon widget, double x, double y, double zLevel, boolean doDraw) {
      render(widget, x, y, widget.getWidth(), widget.getHeight(), zLevel, doDraw);
    }

    @Override
    public void render(IWidgetIcon widget, double x, double y, double zLevel, boolean doDraw, boolean flipY) {
      render(widget, x, y, widget.getWidth(), widget.getHeight(), zLevel, doDraw, flipY);
    }

    @Override
    public void render(IWidgetIcon widget, double x, double y, double width, double height, double zLevel, boolean doDraw) {
      render(widget, x, y, width, height, zLevel, doDraw, false);
    }

    @Override
    public void render(IWidgetIcon widget, double x, double y, double width, double height, double zLevel, boolean doDraw, boolean flipY) {

      WorldRenderer tes = Tessellator.getInstance().getWorldRenderer();
      if (doDraw) {
        RenderUtil.bindTexture(getTexture());
        //tessellator.startDrawingQuads();           
        tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
      }
      double minU = (double) widget.getX() / getSize();
      double maxU = (double) (widget.getX() + widget.getWidth()) / getSize();
      double minV = (double) widget.getY() / getSize();
      double maxV = (double) (widget.getY() + widget.getHeight()) / getSize();

      if (flipY) {
        tes.pos(x, y + height, zLevel).tex(minU, minV).endVertex();
        
        tes.pos(x + width, y + height, zLevel).tex(maxU, minV);
        tes.pos(x + width, y + 0, zLevel).tex( maxU, maxV);
        tes.pos(x, y + 0, zLevel).tex( minU, maxV);
      } else {
        tes.pos(x, y + height, zLevel).tex( minU, maxV);
        tes.pos(x + width, y + height, zLevel).tex( maxU, maxV);
        tes.pos(x + width, y + 0, zLevel).tex( maxU, minV);
        tes.pos(x, y + 0, zLevel).tex( minU, minV);
      }
      if (widget.getOverlay() != null) {
        widget.getOverlay().getMap().render(widget.getOverlay(), x, y, width, height, zLevel, false, flipY);
      }
      if (doDraw) {
        Tessellator.getInstance().draw();
      }
    }
  }
}
