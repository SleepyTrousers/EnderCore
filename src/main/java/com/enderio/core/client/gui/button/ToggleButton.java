package com.enderio.core.client.gui.button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.enderio.core.api.client.gui.IGuiScreen;
import com.enderio.core.api.client.render.IWidgetIcon;
import com.enderio.core.client.gui.widget.GuiToolTip;
import com.enderio.core.client.render.EnderWidget;

import net.minecraft.client.Minecraft;

public class ToggleButton extends IconButton {

  private boolean selected;
  private final @Nonnull IWidgetIcon unselectedIcon;
  private final @Nonnull IWidgetIcon selectedIcon;

  private GuiToolTip selectedTooltip, unselectedTooltip;
  private boolean paintSelectionBorder;

  public ToggleButton(@Nonnull IGuiScreen gui, int id, int x, int y, @Nonnull IWidgetIcon unselectedIcon, @Nonnull IWidgetIcon selectedIcon) {
    super(gui, id, x, y, unselectedIcon);
    this.unselectedIcon = unselectedIcon;
    this.selectedIcon = selectedIcon;
    selected = false;
    paintSelectionBorder = true;
  }

  public boolean isSelected() {
    return selected;
  }

  public ToggleButton setSelected(boolean selected) {
    this.selected = selected;
    icon = selected ? selectedIcon : unselectedIcon;
    if (selected && selectedTooltip != null) {
      setToolTip(selectedTooltip);
    } else if (!selected && unselectedTooltip != null) {
      setToolTip(unselectedTooltip);
    }
    return this;
  }

  @Override
  protected @Nonnull IWidgetIcon getIconForHoverState(int hoverState) {
    if (!selected || !paintSelectionBorder) {
      return super.getIconForHoverState(hoverState);
    }
    if (hoverState == 0) {
      return EnderWidget.BUTTON_DISABLED;
    }
    if (hoverState == 2) {
      return EnderWidget.BUTTON_DOWN_HIGHLIGHT;
    }
    return EnderWidget.BUTTON_DOWN;
  }

  @Override
  public boolean mousePressed(@Nonnull Minecraft par1Minecraft, int par2, int par3) {
    if (super.mousePressed(par1Minecraft, par2, par3)) {
      return toggleSelected();
    }
    return false;
  }

  protected boolean toggleSelected() {
    setSelected(!selected);
    return true;
  }

  public void setSelectedToolTip(String... tt) {
    selectedTooltip = new GuiToolTip(getBounds(), makeCombinedTooltipList(tt));
    setSelected(selected);
  }

  private @Nonnull List<String> makeCombinedTooltipList(String... tt) {
    final @Nonnull List<String> list = new ArrayList<String>();
    if (toolTipText != null) {
      Collections.addAll(list, toolTipText);
    }
    Collections.addAll(list, tt);
    return list;
  }

  public void setUnselectedToolTip(String... tt) {
    unselectedTooltip = new GuiToolTip(getBounds(), makeCombinedTooltipList(tt));
    setSelected(selected);
  }

  public void setPaintSelectedBorder(boolean b) {
    paintSelectionBorder = b;
  }
}
