package com.enderio.core.common.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;

public class InventoryWrapper implements ISidedInventory {

  public static ISidedInventory asSidedInventory(IInventory inv) {
    if (inv == null) {
      return null;
    }
    if (inv instanceof ISidedInventory) {
      return (ISidedInventory) inv;
    }
    return new InventoryWrapper(inv);
  }

  private IInventory inv;

  public InventoryWrapper(IInventory inventory) {
    this.inv = ItemUtil.getInventory(inventory);
  }

  public IInventory getWrappedInv() {
    return inv;
  }

  @Override
  public int getSizeInventory() {
    return inv.getSizeInventory();
  }

  @Override
  public ItemStack getStackInSlot(int slot) {
    if (slot < 0 || slot >= inv.getSizeInventory()) {
      return null;
    }
    return inv.getStackInSlot(slot);
  }

  @Override
  public ItemStack decrStackSize(int slot, int amount) {
    return inv.decrStackSize(slot, amount);
  }

  @Override
  public void setInventorySlotContents(int slot, ItemStack itemStack) {
    if (slot >= 0 && slot < inv.getSizeInventory()) {
      inv.setInventorySlotContents(slot, itemStack);
    }
  }

  @Override
  public int getInventoryStackLimit() {
    return inv.getInventoryStackLimit();
  }

  @Override
  public void markDirty() {
    inv.markDirty();
  }

  @Override
  public boolean isUseableByPlayer(EntityPlayer entityplayer) {
    return inv.isUseableByPlayer(entityplayer);
  }

  @Override
  public boolean isItemValidForSlot(int slot, ItemStack itemStack) {
    return slot >= 0 && slot < getSizeInventory() && inv.isItemValidForSlot(slot, itemStack);
  }

  @Override
  public int[] getSlotsForFace(EnumFacing side) {
    int[] slots = new int[inv.getSizeInventory()];
    for (int i = 0; i < slots.length; i++) {
      slots[i] = i;
    }
    return slots;
  }

  @Override
  public boolean canInsertItem(int slot, ItemStack itemStack, EnumFacing side) {
    return isItemValidForSlot(slot, itemStack);
  }

  @Override
  public boolean canExtractItem(int slot, ItemStack itemStack, EnumFacing side) {
    return slot >= 0 && slot < getSizeInventory();
  }

  @Override
  public ItemStack removeStackFromSlot(int index) {
    return inv.removeStackFromSlot(index);
  }

  @Override
  public void openInventory(EntityPlayer player) {
    inv.openInventory(player);

  }

  @Override
  public void closeInventory(EntityPlayer player) {
    inv.closeInventory(player);

  }

  @Override
  public int getField(int id) {
    return inv.getField(id);
  }

  @Override
  public void setField(int id, int value) {
    inv.setField(id, value);

  }

  @Override
  public int getFieldCount() {
    return inv.getFieldCount();
  }

  @Override
  public void clear() {
    inv.clear();
  }

  @Override
  public String getName() {
    return inv.getName();
  }

  @Override
  public boolean hasCustomName() {
    return inv.hasCustomName();
  }

  @Override
  public IChatComponent getDisplayName() {
    return inv.getDisplayName();
  }

}