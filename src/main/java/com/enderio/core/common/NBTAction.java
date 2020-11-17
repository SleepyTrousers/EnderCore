package com.enderio.core.common;

// TODO: Check if we're depending on AS here, or if we're moving anything that does that to EIO
/**
 * @deprecated Moved to AutoSave
 */
@Deprecated
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