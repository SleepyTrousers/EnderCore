package com.enderio.core.client.gui.widget;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
     * @param realsize
     *          The real size of the stack if the stack has a size that cannot be stored in an itemstack.
     */
    void setGhostSlotContents(int slot, @Nonnull ItemStack stack, int realsize);
  }

  public @Nullable TileEntityBase te = null;
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

  public abstract @Nonnull ItemStack getStack();

  @Deprecated
  public void putStack(@Nonnull ItemStack stack) {
    putStack(stack, stack.getCount());
  }

  public void putStack(@Nonnull ItemStack stack, int realsize) {
    if (updateServer) {
      EnderPacketHandler.sendToServer(PacketGhostSlot.setGhostSlotContents(slot, stack, realsize));
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
   * Should the items in the slot have their standard overlay (stacksize, damage/energy/fluid bar)?
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
