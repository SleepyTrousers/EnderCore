package com.enderio.core.client.gui.button;

import net.minecraft.client.Minecraft;

import org.apache.commons.lang3.ArrayUtils;

import com.enderio.core.api.client.gui.IGuiScreen;
import com.enderio.core.api.client.render.IWidgetIcon;
import com.enderio.core.client.gui.widget.GuiToolTip;
import com.enderio.core.client.render.EnderWidget;
import com.google.common.collect.Lists;

public class ToggleButton extends IconButton {

    private boolean selected;
    private final IWidgetIcon unselectedIcon;
    private final IWidgetIcon selectedIcon;

    private GuiToolTip selectedTooltip, unselectedTooltip;
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
        String[] combinedTooltip = ArrayUtils.addAll(toolTipText, tt);
        selectedTooltip = new GuiToolTip(getBounds(), Lists.newArrayList(combinedTooltip));
        setSelected(selected);
    }

    public void setUnselectedToolTip(String... tt) {
        String[] combinedTooltip = ArrayUtils.addAll(toolTipText, tt);
        unselectedTooltip = new GuiToolTip(getBounds(), Lists.newArrayList(combinedTooltip));
        setSelected(selected);
    }

    public void setPaintSelectedBorder(boolean b) {
        this.paintSelectionBorder = b;
    }
}
