package com.enderio.core.client.gui.widget;

import com.enderio.core.common.TileEntityBase;
import com.enderio.core.common.network.EnderPacketHandler;
import com.enderio.core.common.network.PacketGhostSlot;

import net.minecraft.item.ItemStack;

public abstract class GhostSlot {

  public static interface IGhostSlotAware {
    /**
     * Called server-side on the container when a GhostSlot is changed. Check that the given slot number really is a ghost slot before storing the given stack.
     * 
     * @param slot
     *          The slot number that was given to the ghost slot
     * @param stack
     *          The stack that should be placed, null to clear
     */
    void setGhostSlotContents(int slot, ItemStack stack);
  }

  public TileEntityBase te = null;
  public int slot = -1;
  public int x;
  public int y;

  public boolean visible = true;
  public boolean grayOut = true;
  public float grayOutLevel = 0.5f;
  public boolean displayStdOverlay = false;
  public int stackSizeLimit = 1;
  public boolean updateServer = false;

  public boolean isMouseOver(int mx, int my) {
    return mx >= x && mx < (x + 16) && my >= y && my < (y + 16);
  }

  public abstract ItemStack getStack();

  public void putStack(ItemStack stack) {
    if (updateServer) {
      EnderPacketHandler.sendToServer(PacketGhostSlot.setGhostSlotContents(slot, stack));
    }
  }

  /**
   * Should the slot be displayed at all?
   */
  public boolean isVisible() {
    return visible;
  }

  /**
   * Should the slot be grayed out?
   */
  public boolean shouldGrayOut() {
    return grayOut;
  }

  /**
   * If it should be grayed out, how far? (1=no graying, 0=fully invisible)
   */
  public float getGrayOutLevel() {
    return grayOutLevel;
  }

  /**
   * Should the items in the slot have their standard overlay (stacksize)?
   */
  public boolean shouldDisplayStdOverlay() {
    return displayStdOverlay;
  }

  /**
   * Limit the stack size? (Enable the standard overlay if this is not 1.)
   */
  public int getStackSizeLimit() {
    return stackSizeLimit;
  }
}

