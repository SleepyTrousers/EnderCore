package com.enderio.core.api.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import com.enderio.core.client.render.RenderUtil;

public interface IWidgetMap {

    int getSize();

    ResourceLocation getTexture();

    void render(IWidgetIcon widget, double x, double y);

    void render(IWidgetIcon widget, double x, double y, boolean doDraw);

    void render(IWidgetIcon widget, double x, double y, boolean doDraw, boolean flipY);

    void render(IWidgetIcon widget, double x, double y, double zLevel, boolean doDraw);

    void render(IWidgetIcon widget, double x, double y, double zLevel, boolean doDraw, boolean flipY);

    void render(IWidgetIcon widget, double x, double y, double width, double height, double zLevel, boolean doDraw);

    void render(IWidgetIcon widget, double x, double y, double width, double height, double zLevel, boolean doDraw,
            boolean flipY);

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
        public void render(IWidgetIcon widget, double x, double y, double width, double height, double zLevel,
                boolean doDraw) {
            render(widget, x, y, width, height, zLevel, doDraw, false);
        }

        @Override
        public void render(IWidgetIcon widget, double x, double y, double width, double height, double zLevel,
                boolean doDraw, boolean flipY) {

            Tessellator tessellator = Tessellator.instance;
            if (doDraw) {
                RenderUtil.bindTexture(getTexture());
                tessellator.startDrawingQuads();
            }
            double minU = (double) widget.getX() / getSize();
            double maxU = (double) (widget.getX() + widget.getWidth()) / getSize();
            double minV = (double) widget.getY() / getSize();
            double maxV = (double) (widget.getY() + widget.getHeight()) / getSize();

            if (flipY) {
                tessellator.addVertexWithUV(x, y + height, zLevel, minU, minV);
                tessellator.addVertexWithUV(x + width, y + height, zLevel, maxU, minV);
                tessellator.addVertexWithUV(x + width, y + 0, zLevel, maxU, maxV);
                tessellator.addVertexWithUV(x, y + 0, zLevel, minU, maxV);
            } else {
                tessellator.addVertexWithUV(x, y + height, zLevel, minU, maxV);
                tessellator.addVertexWithUV(x + width, y + height, zLevel, maxU, maxV);
                tessellator.addVertexWithUV(x + width, y + 0, zLevel, maxU, minV);
                tessellator.addVertexWithUV(x, y + 0, zLevel, minU, minV);
            }
            if (widget.getOverlay() != null) {
                widget.getOverlay().getMap().render(widget.getOverlay(), x, y, width, height, zLevel, false, flipY);
            }
            if (doDraw) {
                tessellator.draw();
            }
        }
    }
}
