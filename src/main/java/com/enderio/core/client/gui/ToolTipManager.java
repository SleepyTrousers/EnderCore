package com.enderio.core.client.gui;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import com.enderio.core.client.gui.widget.GuiToolTip;
import com.enderio.core.common.util.NNList;
import com.google.common.collect.Sets;

import net.minecraft.client.gui.FontRenderer;

public class ToolTipManager {

  public static interface ToolTipRenderer {

    int getGuiRootLeft();

    int getGuiRootTop();

    int getGuiXSize();

    @Nonnull
    FontRenderer getFontRenderer();

    void drawHoveringToolTipText(@Nonnull List<String> par1List, int par2, int par3, @Nonnull FontRenderer font);
  }

  private final @Nonnull Set<GuiToolTip> toolTips = Sets.newHashSet();

  public void addToolTip(@Nonnull GuiToolTip toolTip) {
    toolTips.add(toolTip);
  }

  public boolean removeToolTip(@Nonnull GuiToolTip toolTip) {
    return toolTips.remove(toolTip);
  }

  public void clearToolTips() {
    toolTips.clear();
  }

  protected final void drawTooltips(@Nonnull ToolTipRenderer renderer, int mouseX, int mouseY) {
    for (GuiToolTip toolTip : toolTips) {
      toolTip.onTick(mouseX - renderer.getGuiRootLeft(), mouseY - renderer.getGuiRootTop());
      if (toolTip.shouldDraw()) {
        drawTooltip(toolTip, mouseX, mouseY, renderer);
      }
    }
  }

  protected void drawTooltip(@Nonnull GuiToolTip toolTip, int mouseX, int mouseY, @Nonnull ToolTipRenderer renderer) {
    List<String> list = toolTip.getToolTipText();
    if (list.isEmpty()) {
      return;
    }

    NNList<String> formatted = new NNList<String>();
    for (int i = 0; i < list.size(); i++) {
      if (i == 0) {
        formatted.add("\u00a7f" + list.get(i));
      } else {
        formatted.add("\u00a77" + list.get(i));
      }
    }

    renderer.drawHoveringToolTipText(formatted, mouseX, mouseY, renderer.getFontRenderer());
  }

}
