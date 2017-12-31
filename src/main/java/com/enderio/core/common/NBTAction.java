package com.enderio.core.common;

public enum NBTAction {
  /**
   * The TE is saved to/loaded from the save file.
   */
  SAVE,
  /**
   * The TE is synced to the client.
   */
  CLIENT,
  /**
   * TE data is written to/read from an item.
   */
  ITEM;
}