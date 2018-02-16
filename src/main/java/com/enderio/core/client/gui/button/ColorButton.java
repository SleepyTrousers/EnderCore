package com.enderio.core.client.gui.button;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.enderio.core.api.client.gui.IGuiScreen;
import com.enderio.core.client.render.ColorUtil;
import com.enderio.core.common.util.DyeColor;
import com.enderio.core.common.vecmath.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemDye;
import net.minecraft.util.math.MathHelper;

public class ColorButton extends IconButton {

  private int colorIndex = 0;

  private @Nonnull String tooltipPrefix = "";

  public ColorButton(@Nonnull IGuiScreen gui, int id, int x, int y) {
    super(gui, id, x, y, null);
  }

  @Override
  public boolean mousePressed(@Nonnull Minecraft par1Minecraft, int par2, int par3) {
    boolean result = super.mousePressed(par1Minecraft, par2, par3);
    if (result) {
      nextColor();
    }
    return result;
  }

  @Override
  public boolean mousePressedButton(@Nonnull Minecraft mc, int mouseX, int mouseY, int button) {
    boolean result = button == 1 && super.checkMousePress(mc, mouseX, mouseY);
    if (result) {
      prevColor();
    }
    return result;
  }

  public @Nonnull String getTooltipPrefix() {
    return tooltipPrefix;
  }

  public void setToolTipHeading(@Nullable String tooltipPrefix) {
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
    this.colorIndex = MathHelper.clamp(colorIndex, 0, ItemDye.DYE_COLORS.length - 1);
    String colStr = DyeColor.values()[colorIndex].getLocalisedName();
    if (tooltipPrefix.length() > 0) {
      setToolTip(tooltipPrefix, colStr);
    } else {
      setToolTip(colStr);
    }
  }

  @Override
  public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
    super.drawButton(mc, mouseX, mouseY, partialTicks);
    if (visible) {
      BufferBuilder tes = Tessellator.getInstance().getBuffer();
      tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

      int xAdj = this.x + 2;
      int yAdj = this.y + 2;

      GlStateManager.disableTexture2D();
      int col = ItemDye.DYE_COLORS[colorIndex];
      Vector3f c = ColorUtil.toFloat(col);
      GlStateManager.color(c.x, c.y, c.z);

      tes.pos(xAdj, yAdj + height - 4, zLevel).color(c.x, c.y, c.z, 1).endVertex();
      tes.pos(xAdj + width - 4, yAdj + height - 4, zLevel).color(c.x, c.y, c.z, 1).endVertex();
      tes.pos(xAdj + width - 4, yAdj + 0, zLevel).color(c.x, c.y, c.z, 1).endVertex();
      tes.pos(xAdj, yAdj + 0, zLevel).color(c.x, c.y, c.z, 1).endVertex();

      Tessellator.getInstance().draw();
      GlStateManager.enableTexture2D();

    }
  }
}
