package com.enderio.core.client.gui;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

import com.enderio.core.api.client.gui.IGuiScreen;
import com.enderio.core.client.gui.ToolTipManager.ToolTipRenderer;
import com.enderio.core.client.gui.widget.GuiToolTip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;

public abstract class GuiScreenBase extends GuiScreen implements ToolTipRenderer, IGuiScreen {

  protected @Nonnull ToolTipManager ttMan = new ToolTipManager();

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
  public void addToolTip(@Nonnull GuiToolTip toolTip) {
    ttMan.addToolTip(toolTip);
  }

  @Override
  public boolean removeToolTip(@Nonnull GuiToolTip toolTip) {
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

  @Override
  public void drawHoveringToolTipText(@Nonnull List<String> par1List, int par2, int par3, @Nonnull FontRenderer font) {
    super.drawHoveringText(par1List, par2, par3, font);
  }

  @Override
  public int getGuiRootLeft() {
    return guiLeft;
  }

  @Override
  public int getGuiRootTop() {
    return guiTop;
  }

  @Override
  public int getGuiXSize() {
    return xSize;
  }

  @Override
  public int getGuiYSize() {
    return xSize;
  }

  @Override
  public @Nonnull FontRenderer getFontRenderer() {
    return Minecraft.getMinecraft().fontRenderer;
  }

  @Override
  public @Nonnull <T extends GuiButton> T addButton(@Nonnull T button) {
    if (!buttonList.contains(button)) {
      buttonList.add(button);
    }
    return button;
  }

  @Override
  public void removeButton(@Nonnull GuiButton button) {
    buttonList.remove(button);
  }

  @Override
  public void doActionPerformed(@Nonnull GuiButton guiButton) throws IOException {
    actionPerformed(guiButton);
  }

  @Override
  public int getOverlayOffsetXLeft() {
    return 0;
  }

  @Override
  public int getOverlayOffsetXRight() {
    return 0;
  }

  @Override
  @Nonnull
  public final <T extends GuiButton> T addGuiButton(@Nonnull T button) {
    return addButton(button);
  }

}
