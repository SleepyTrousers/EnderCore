package com.enderio.core.common.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class ArrayInventory implements IInventory {

  protected ItemStack[] items;

  public ArrayInventory(ItemStack[] items) {
    this.items = items;
  }

  public ArrayInventory(int size) {
    items = new ItemStack[size];
  }

  @Override
  public int getSizeInventory() {
    return items.length;
  }

  @Override
  public ItemStack getStackInSlot(int slot) {
    return items[slot];
  }

  @Override
  public ItemStack decrStackSize(int slot, int amount) {
    return Util.decrStackSize(this, slot, amount);
  }

  @Override
  public void setInventorySlotContents(int slot, ItemStack stack) {
    items[slot] = stack;
    markDirty();
  }

  @Override
  public int getInventoryStackLimit() {
    return 64;
  }

  @Override
  public boolean isUseableByPlayer(EntityPlayer var1) {
    return true;
  }

  @Override
  public boolean isItemValidForSlot(int i, ItemStack itemstack) {
    return true;
  }

  @Override
  public void markDirty() {

  }

  @Override
  public String getName() {
    return "ArrayInventory";
  }

  @Override
  public boolean hasCustomName() {
    return false;
  }

  @Override
  public ITextComponent getDisplayName() {
    return null;
  }

  @Override
  public ItemStack removeStackFromSlot(int index) {
    ItemStack res = items[index];
    items[index] = null;
    return res;
  }

  @Override
  public void openInventory(EntityPlayer player) {
  }

  @Override
  public void closeInventory(EntityPlayer player) {
  }

  @Override
  public int getField(int id) {
    return 0;
  }

  @Override
  public void setField(int id, int value) {
  }

  @Override
  public int getFieldCount() {
    return 0;
  }

  @Override
  public void clear() {
    for(int i=0;i<items.length;i++) {
      items[i] = null;
    }

  }

}
