package com.enderio.core.client.gui.button;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import org.lwjgl.opengl.GL11;

import com.enderio.core.api.client.gui.IGuiScreen;
import com.enderio.core.client.render.ColorUtil;
import com.enderio.core.common.vecmath.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;

public class ColorButton extends IconButton {

  private int colorIndex = 0;

  private @Nonnull String tooltipPrefix = "";

  public ColorButton(@Nonnull IGuiScreen gui, int x, int y) {
    super(gui, x, y, null);
  }

  public ColorButton(@Nonnull IGuiScreen gui, int x, int y, IPressable pressedAction) {
    super(gui, x, y, null, pressedAction);
  }

  @Override
  public void onClick(double mouseX, double mouseY) {
    super.onClick(mouseX, mouseY);
    nextColor();
  }

  @Override
  public boolean buttonPressed(double mouseX, double mouseY, int button) {
    boolean result = button == 1 && super.mouseClicked(mouseX, mouseY, button);
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
    if (colorIndex >= DyeColor.values().length) {
      colorIndex = 0;
    }
    setColorIndex(colorIndex);
  }

  private void prevColor() {
    colorIndex--;
    if (colorIndex < 0) {
      colorIndex = DyeColor.values().length - 1;
    }
    setColorIndex(colorIndex);
  }

  public int getColorIndex() {
    return colorIndex;
  }

  public void setColorIndex(int colorIndex) {
    this.colorIndex = MathHelper.clamp(colorIndex, 0, DyeColor.values().length - 1);
    String colStr = DyeColor.values()[colorIndex].getTranslationKey();
    if (tooltipPrefix.length() > 0) {
      setToolTip(tooltipPrefix, colStr);
    } else {
      setToolTip(colStr);
    }
  }

  @Override
  public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
    if (this.isVisible()) {
//      BufferBuilder tes = Tessellator.getInstance().getBuffer();
//      tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

      int xAdj = this.x + 2;
      int yAdj = this.y + 2;

      RenderSystem.disableTexture();
      DyeColor col = DyeColor.values()[colorIndex];
      Vector3f c = ColorUtil.toFloat(col.getColorValue());

      RenderSystem.color3f(c.x, c.y, c.z);

      // TODO: Double check this
      blit(matrixStack, xAdj, yAdj, 0, 0, getWidth(), getHeight());

//      tes.pos(xAdj, yAdj + getWidth() - 4, zLevel).color(c.x, c.y, c.z, 1).endVertex();
//      tes.pos(xAdj + getWidth() - 4, yAdj + getHeight() - 4, zLevel).color(c.x, c.y, c.z, 1).endVertex();
//      tes.pos(xAdj + getWidth() - 4, yAdj + 0, zLevel).color(c.x, c.y, c.z, 1).endVertex();
//      tes.pos(xAdj, yAdj + 0, zLevel).color(c.x, c.y, c.z, 1).endVertex();

//      Tessellator.getInstance().draw();
      RenderSystem.enableTexture();

    }
  }
}
