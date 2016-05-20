package com.enderio.core.client.gui.button;

import org.lwjgl.opengl.GL11;

import com.enderio.core.api.client.gui.IGuiScreen;
import com.enderio.core.client.render.ColorUtil;
import com.enderio.core.common.util.DyeColor;
import com.enderio.core.common.vecmath.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemDye;
import net.minecraft.util.math.MathHelper;

public class ColorButton extends IconButton {

  private int colorIndex = 0;

  private String tooltipPrefix = "";

  public ColorButton(IGuiScreen gui, int id, int x, int y) {
    super(gui, id, x, y, null);
  }

  @Override
  public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3) {
    boolean result = super.mousePressed(par1Minecraft, par2, par3);
    if (result) {
      nextColor();
    }
    return result;
  }

  @Override
  public boolean mousePressedButton(Minecraft mc, int x, int y, int button) {
    boolean result = button == 1 && super.checkMousePress(mc, x, y);
    if (result) {
      prevColor();
    }
    return result;
  }

  public String getTooltipPrefix() {
    return tooltipPrefix;
  }

  public void setToolTipHeading(String tooltipPrefix) {
    if (tooltipPrefix == null) {
      this.tooltipPrefix = "";
    } else {
      this.tooltipPrefix = tooltipPrefix;
    }
  }

  private void nextColor() {
    colorIndex++;
    if (colorIndex >= ItemDye.DYE_COLORS.length) {
      colorIndex = 0;
    }
    setColorIndex(colorIndex);
  }

  private void prevColor() {
    colorIndex--;
    if (colorIndex < 0) {
      colorIndex = ItemDye.DYE_COLORS.length - 1;
    }
    setColorIndex(colorIndex);
  }

  public int getColorIndex() {
    return colorIndex;
  }

  public void setColorIndex(int colorIndex) {
    this.colorIndex = MathHelper.clamp_int(colorIndex, 0, ItemDye.DYE_COLORS.length - 1);
    String colStr = DyeColor.values()[colorIndex].getLocalisedName();
    if (tooltipPrefix != null && tooltipPrefix.length() > 0) {
      setToolTip(tooltipPrefix, colStr);
    } else {
      setToolTip(colStr);
    }
  }

  @Override
  public void drawButton(Minecraft mc, int mouseX, int mouseY) {
    super.drawButton(mc, mouseX, mouseY);
    if (visible) {
      VertexBuffer tes = Tessellator.getInstance().getBuffer();
      tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
      
      int x = xPosition + 2;
      int y = yPosition + 2;

      GL11.glDisable(GL11.GL_TEXTURE_2D);

      int col = ItemDye.DYE_COLORS[colorIndex];
      Vector3f c = ColorUtil.toFloat(col);      
      GlStateManager.color(c.x, c.y, c.z);
      
      tes.pos(x, y + height - 4, zLevel).endVertex();
      tes.pos(x + width - 4, y + height - 4, zLevel).endVertex();
      tes.pos(x + width - 4, y + 0, zLevel).endVertex();
      tes.pos(x, y + 0, zLevel).endVertex();

      Tessellator.getInstance().draw();

      GL11.glEnable(GL11.GL_TEXTURE_2D);

    }
  }
}
