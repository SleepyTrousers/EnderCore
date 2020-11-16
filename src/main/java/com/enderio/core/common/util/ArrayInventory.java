package com.enderio.core.common.util;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class ArrayInventory implements IInventory {

  protected final @Nonnull ItemStack[] items;

  public ArrayInventory(@Nonnull ItemStack[] items) {
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
  public @Nonnull ItemStack getStackInSlot(int slot) {
    final ItemStack itemStack = items[slot];
    return itemStack != null ? itemStack : ItemStack.EMPTY;
  }

  @Override
  public @Nonnull ItemStack decrStackSize(int slot, int amount) {
    return Util.decrStackSize(this, slot, amount);
  }

  @Override
  public void setInventorySlotContents(int slot, @Nonnull ItemStack stack) {
    items[slot] = stack;
    markDirty();
  }

  @Override
  public int getInventoryStackLimit() {
    return 64;
  }

  @Override
  public boolean isUsableByPlayer(@Nonnull PlayerEntity var1) {
    return true;
  }

  @Override
  public boolean isItemValidForSlot(int i, @Nonnull ItemStack itemstack) {
    return true;
  }

  @Override
  public void markDirty() {

  }

  /*@Override
  public @Nonnull String getName() {
    return "ArrayInventory";
  }

  @Override
  public boolean hasCustomName() {
    return false;
  }

  @Override
  public @Nonnull ITextComponent getDisplayName() {
    return new StringTextComponent(getName());
  }*/

  @Override
  public @Nonnull ItemStack removeStackFromSlot(int index) {
    ItemStack res = items[index];
    items[index] = ItemStack.EMPTY;
    return res != null ? res : ItemStack.EMPTY;
  }

  @Override
  public void openInventory(@Nonnull PlayerEntity player) {
  }

  @Override
  public void closeInventory(@Nonnull PlayerEntity player) {
  }


  /*@Override
  public int getField(int id) {
    return 0;
  }

  @Override
  public void setField(int id, int value) {
  }

  @Override
  public int getFieldCount() {
    return 0;
  }*/

  @Override
  public void clear() {
    for (int i = 0; i < items.length; i++) {
      items[i] = ItemStack.EMPTY;
    }

  }

  @Override
  public boolean isEmpty() {
    for (ItemStack itemstack : items) {
      if (itemstack != null && !itemstack.isEmpty()) {
        return false;
      }
    }
    return true;
  }

}
