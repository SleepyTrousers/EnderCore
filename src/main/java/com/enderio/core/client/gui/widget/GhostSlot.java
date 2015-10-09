package com.enderio.core.client.gui.widget;

import com.enderio.core.common.TileEntityEnder;
import com.enderio.core.common.network.EnderPacketHandler;
import com.enderio.core.common.network.PacketGhostSlot;

import net.minecraft.item.ItemStack;

public abstract class GhostSlot {

  public TileEntityEnder te = null;
  public int slot = -1;
  public int x;
  public int y;

  public boolean visible = true;
  public boolean grayOut = true;
  public boolean displayStdOverlay = false;
  public int stackSizeLimit = 1;

  public boolean isMouseOver(int mx, int my) {
    return mx >= x && mx < (x + 16) && my >= y && my < (y + 16);
  }

  public abstract ItemStack getStack();

  public void putStack(ItemStack stack) {
    if (te != null) {
      EnderPacketHandler.sendToServer(PacketGhostSlot.setGhostSlotContents(te, slot, stack));
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
