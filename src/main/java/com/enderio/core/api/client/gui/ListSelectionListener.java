package com.enderio.core.api.client.gui;

import com.enderio.core.client.gui.widget.GuiScrollableList;

public interface ListSelectionListener<T> {

    void selectionChanged(GuiScrollableList<T> list, int selectedIndex);

}
