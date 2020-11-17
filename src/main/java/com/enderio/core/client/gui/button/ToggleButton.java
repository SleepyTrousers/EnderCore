package com.enderio.core.client.gui.button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.enderio.core.api.client.gui.IGuiScreen;
import com.enderio.core.api.client.render.IWidgetIcon;
import com.enderio.core.client.gui.widget.TooltipWidget;
import com.enderio.core.client.render.EnderWidget;

public class ToggleButton extends IconButton {

  private boolean selected;
  private final @Nonnull IWidgetIcon unselectedIcon;
  private final @Nonnull IWidgetIcon selectedIcon;

  private TooltipWidget selectedTooltip, unselectedTooltip;
  private boolean paintSelectionBorder;

  public ToggleButton(@Nonnull IGuiScreen gui, int x, int y, @Nonnull IWidgetIcon unselectedIcon, @Nonnull IWidgetIcon selectedIcon) {
    super(gui, x, y, unselectedIcon);
    this.unselectedIcon = unselectedIcon;
    this.selectedIcon = selectedIcon;
    selected = false;
    paintSelectionBorder = true;
  }

  public ToggleButton(@Nonnull IGuiScreen gui, int x, int y, @Nonnull IWidgetIcon unselectedIcon, @Nonnull IWidgetIcon selectedIcon, IPressable pressedAction) {
    super(gui, x, y, unselectedIcon, pressedAction);
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
  protected @Nonnull IWidgetIcon getIconForState() {
    if (!selected || !paintSelectionBorder) {
      return super.getIconForState();
    }
    if (!isActive()) {
      return EnderWidget.BUTTON_DISABLED;
    }
    if (isHovered()) {
      return EnderWidget.BUTTON_DOWN_HIGHLIGHT;
    }
    return EnderWidget.BUTTON_DOWN;
  }

  @Override
  public void onClick(double mouseX, double mouseY) {
    super.onClick(mouseX, mouseY);
    toggleSelected();
  }

  protected boolean toggleSelected() {
    setSelected(!selected);
    return true;
  }

  public void setSelectedToolTip(String... tt) {
    selectedTooltip = new TooltipWidget(getBounds(), makeCombinedTooltipList(tt));
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
    unselectedTooltip = new TooltipWidget(getBounds(), makeCombinedTooltipList(tt));
    setSelected(selected);
  }

  public void setPaintSelectedBorder(boolean b) {
    paintSelectionBorder = b;
  }
}
