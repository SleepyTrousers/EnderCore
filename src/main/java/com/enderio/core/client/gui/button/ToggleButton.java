package com.enderio.core.client.gui.button;

import net.minecraft.client.Minecraft;

import com.enderio.core.api.client.gui.IGuiScreen;
import com.enderio.core.api.client.render.IWidgetIcon;
import com.enderio.core.client.render.EnderWidget;

public class ToggleButton extends IconButton {

  private boolean selected;
  private final IWidgetIcon unselectedIcon;
  private final IWidgetIcon selectedIcon;

  private String[] selectedTooltip;
  private String[] unselectedTooltip;
  private boolean paintSelectionBorder;

  public ToggleButton(IGuiScreen gui, int id, int x, int y, IWidgetIcon unselectedIcon, IWidgetIcon selectedIcon) {
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
  protected IWidgetIcon getIconForHoverState(int hoverState) {
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
  public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3) {
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
    this.selectedTooltip = tt;
    setSelected(selected);
  }

  public void setUnselectedToolTip(String... tt) {
    this.unselectedTooltip = tt;
    setSelected(selected);
  }

  public void setPaintSelectedBorder(boolean b) {
    this.paintSelectionBorder = b;
  }

}
