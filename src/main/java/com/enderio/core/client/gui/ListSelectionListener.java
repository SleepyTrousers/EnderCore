package com.enderio.core.client.gui;

public interface ListSelectionListener<T> {

  void selectionChanged(GuiScrollableList<T> list, int selectedIndex);

}
