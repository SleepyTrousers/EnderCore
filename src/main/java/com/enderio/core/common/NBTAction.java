package com.enderio.core.common;

public enum NBTAction {
  /**
   * The TE is saved to/loaded from the save file.
   */
  SAVE,
  /**
   * The TE is initially synced to the client.
   */
  SYNC,
  /**
   * The TE is updated to the client.
   */
  UPDATE,
  /**
   * TE data is written to/read from an item.
   */
  ITEM;
}