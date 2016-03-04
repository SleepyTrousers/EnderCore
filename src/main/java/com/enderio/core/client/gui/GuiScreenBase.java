package com.enderio.core.client.gui;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.enderio.core.api.client.gui.IGuiScreen;
import com.enderio.core.client.gui.ToolTipManager.ToolTipRenderer;
import com.enderio.core.client.gui.widget.GhostSlot;
import com.enderio.core.client.gui.widget.GuiToolTip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;

public abstract class GuiScreenBase extends GuiScreen implements ToolTipRenderer, IGuiScreen {

  protected ToolTipManager ttMan = new ToolTipManager();

  /** The X size of the inventory window in pixels. */
  protected int xSize = 176;

  /** The Y size of the inventory window in pixels. */
  protected int ySize = 166;

  /**
   * Starting X position for the Gui. Inconsistent use for Gui backgrounds.
   */
  protected int guiLeft;

  /**
   * Starting Y position for the Gui. Inconsistent use for Gui backgrounds.
   */
  protected int guiTop;

  protected boolean drawButtons = true;

  protected GuiScreenBase() {
  }

  protected GuiScreenBase(int xSize, int ySize) {
    this.xSize = xSize;
    this.ySize = ySize;
  }

  @Override
  public void addToolTip(GuiToolTip toolTip) {
    ttMan.addToolTip(toolTip);
  }

  @Override
  public boolean removeToolTip(GuiToolTip toolTip) {
    return ttMan.removeToolTip(toolTip);
  }

  @Override
  public void initGui() {
    super.initGui();
    guiLeft = (width - xSize) / 2;
    guiTop = (height - ySize) / 2;
  }

  @Override
  public void drawScreen(int par1, int par2, float par3) {
    drawDefaultBackground();

    drawBackgroundLayer(par3, par1, par2);

    RenderHelper.disableStandardItemLighting();
    GlStateManager.disableLighting();
    GlStateManager.disableDepth();
    GlStateManager.disableNormalize();
    if (drawButtons) {
      RenderHelper.enableGUIStandardItemLighting();
      super.drawScreen(par1, par2, par3);
    }
    GlStateManager.enableNormalize();

    GlStateManager.pushMatrix();
    GlStateManager.translate(guiLeft, guiTop, 0.0F);
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

    GlStateManager.disableLighting();
    drawForegroundLayer(par1, par2);
    GlStateManager.enableLighting();

    GlStateManager.popMatrix();

    GlStateManager.enableDepth();
    RenderHelper.enableStandardItemLighting();
  }

  protected abstract void drawBackgroundLayer(float par3, int par1, int par2);

  protected final void drawForegroundLayer(int mouseX, int mouseY) {
    drawForegroundImpl(mouseX, mouseY);
    ttMan.drawTooltips(this, mouseX, mouseY);
  }

  protected void drawForegroundImpl(int mouseX, int mouseY) {
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void drawHoveringText(List par1List, int par2, int par3, FontRenderer font) {

    if (!par1List.isEmpty()) {

      GlStateManager.disableNormalize();

      RenderHelper.disableStandardItemLighting();
      GlStateManager.disableLighting();
      GlStateManager.disableDepth();

      int k = 0;
      Iterator iterator = par1List.iterator();

      while (iterator.hasNext()) {
        String s = (String) iterator.next();
        int l = font.getStringWidth(s);

        if (l > k) {
          k = l;
        }
      }

      int i1 = par2 + 12;
      int j1 = par3 - 12;
      int k1 = 8;

      if (par1List.size() > 1) {
        k1 += 2 + (par1List.size() - 1) * 10;
      }

      if (i1 + k > width) {
        i1 -= 28 + k;
      }

      if (j1 + k1 + 6 > height) {
        j1 = height - k1 - 6;
      }

      zLevel = 300.0F;

      int l1 = -267386864;
      drawGradientRect(i1 - 3, j1 - 4, i1 + k + 3, j1 - 3, l1, l1);
      drawGradientRect(i1 - 3, j1 + k1 + 3, i1 + k + 3, j1 + k1 + 4, l1, l1);
      drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 + k1 + 3, l1, l1);
      drawGradientRect(i1 - 4, j1 - 3, i1 - 3, j1 + k1 + 3, l1, l1);
      drawGradientRect(i1 + k + 3, j1 - 3, i1 + k + 4, j1 + k1 + 3, l1, l1);
      int i2 = 1347420415;
      int j2 = (i2 & 16711422) >> 1 | i2 & -16777216;
      drawGradientRect(i1 - 3, j1 - 3 + 1, i1 - 3 + 1, j1 + k1 + 3 - 1, i2, j2);
      drawGradientRect(i1 + k + 2, j1 - 3 + 1, i1 + k + 3, j1 + k1 + 3 - 1, i2, j2);
      drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 - 3 + 1, i2, i2);
      drawGradientRect(i1 - 3, j1 + k1 + 2, i1 + k + 3, j1 + k1 + 3, j2, j2);

      for (int k2 = 0; k2 < par1List.size(); ++k2) {
        String s1 = (String) par1List.get(k2);
        font.drawStringWithShadow(s1, i1, j1, -1);

        if (k2 == 0) {
          j1 += 2;
        }

        j1 += 10;
      }

      zLevel = 0.0F;

      GlStateManager.enableLighting();
      GlStateManager.enableDepth();

      RenderHelper.enableStandardItemLighting();
      GlStateManager.enableNormalize();

    }
  }

  @Override
  public int getGuiLeft() {
    return guiLeft;
  }

  @Override
  public int getGuiTop() {
    return guiTop;
  }

  @Override
  public int getXSize() {
    return xSize;
  }

  @Override
  public int getYSize() {
    return xSize;
  }

  @Override
  public FontRenderer getFontRenderer() {
    return Minecraft.getMinecraft().fontRendererObj;
  }

  @Override
  public void addButton(GuiButton button) {
    if (!buttonList.contains(button)) {
      buttonList.add(button);
    }
  }

  @Override
  public void removeButton(GuiButton button) {
    buttonList.remove(button);
  }

  @Override
  public void doActionPerformed(GuiButton guiButton) throws IOException {
    actionPerformed(guiButton);
  }

  @Override
  public int getOverlayOffsetX() {
    return 0;
  }

  @Override
  public List<GhostSlot> getGhostSlots() {
    return null;
  }
}
