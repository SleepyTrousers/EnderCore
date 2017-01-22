package com.enderio.core.api.client.gui;

import javax.annotation.Nonnull;

import com.enderio.core.client.gui.widget.GuiScrollableList;

public interface ListSelectionListener<T> {

  void selectionChanged(@Nonnull GuiScrollableList<T> list, int selectedIndex);

}
