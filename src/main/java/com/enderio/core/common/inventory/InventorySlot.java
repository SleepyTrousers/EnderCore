package com.enderio.core.common.inventory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.ItemUtil;
import com.google.common.base.Predicate;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;

public class InventorySlot implements IItemHandler {

  private static final @Nonnull ItemStack LIE = new ItemStack(Blocks.CAKE, 0);

  private @Nonnull ItemStack itemStack;
  private final @Nonnull Predicate<ItemStack> filterIn, filterOut;
  private final @Nonnull Callback<ItemStack> callback;
  private final int limit;
  private @Nullable TileEntity owner;

  public InventorySlot() {
    this(LIE, null, null, null, -1);
  }

  public InventorySlot(@Nullable Callback<ItemStack> callback) {
    this(LIE, null, null, callback, -1);
  }

  public InventorySlot(@Nonnull ItemStack itemStack) {
    this(itemStack, null, null, null, -1);
  }

  public InventorySlot(@Nonnull ItemStack itemStack, @Nullable Callback<ItemStack> callback) {
    this(itemStack, null, null, callback, -1);
  }

  public InventorySlot(@Nullable Predicate<ItemStack> filterIn, @Nullable Predicate<ItemStack> filterOut) {
    this(LIE, filterIn, filterOut, null, -1);
  }

  public InventorySlot(@Nullable Predicate<ItemStack> filterIn, @Nullable Predicate<ItemStack> filterOut, @Nullable Callback<ItemStack> callback) {
    this(LIE, filterIn, filterOut, callback, -1);
  }

  public InventorySlot(int limit) {
    this(LIE, null, null, null, limit);
  }

  public InventorySlot(@Nullable Callback<ItemStack> callback, int limit) {
    this(LIE, null, null, callback, limit);
  }

  public InventorySlot(@Nonnull ItemStack itemStack, int limit) {
    this(itemStack, null, null, null, limit);
  }

  public InventorySlot(@Nonnull ItemStack itemStack, @Nullable Callback<ItemStack> callback, int limit) {
    this(itemStack, null, null, callback, limit);
  }

  public InventorySlot(@Nullable Predicate<ItemStack> filterIn, int limit) {
    this(LIE, filterIn, null, null, limit);
  }

  public InventorySlot(@Nullable Predicate<ItemStack> filterIn, @Nullable Predicate<ItemStack> filterOut, int limit) {
    this(LIE, filterIn, filterOut, null, limit);
  }

  public InventorySlot(@Nullable Predicate<ItemStack> filterIn, @Nullable Predicate<ItemStack> filterOut, @Nullable Callback<ItemStack> callback, int limit) {
    this(LIE, filterIn, filterOut, callback, limit);
  }

  public InventorySlot(@Nonnull ItemStack itemStack, @Nullable Predicate<ItemStack> filterIn, @Nullable Predicate<ItemStack> filterOut) {
    this(itemStack, filterIn, filterOut, null, -1);
  }

  public InventorySlot(@Nonnull ItemStack itemStack, @Nullable Predicate<ItemStack> filterIn, @Nullable Predicate<ItemStack> filterOut,
      @Nullable Callback<ItemStack> callback) {
    this(itemStack, filterIn, filterOut, callback, -1);
  }

  public InventorySlot(@Nonnull ItemStack itemStack, @Nullable Predicate<ItemStack> filterIn, @Nullable Predicate<ItemStack> filterOut, int limit) {
    this(itemStack, filterIn, filterOut, null, limit);
  }

  public InventorySlot(@Nonnull ItemStack itemStack, @Nullable Predicate<ItemStack> filterIn, @Nullable Predicate<ItemStack> filterOut,
      @Nullable Callback<ItemStack> callback, int limit) {
    this.itemStack = itemStack;
    this.filterIn = filterIn != null ? filterIn : Filters.ALWAYS_TRUE;
    this.filterOut = filterOut != null ? filterOut : Filters.ALWAYS_TRUE;
    this.callback = callback != null ? callback : Filters.NO_CALLBACK;
    this.limit = limit > 0 ? limit : 64;
  }

  @Override
  public int getSlots() {
    return 1;
  }

  @Override
  public @Nonnull ItemStack getStackInSlot(int slot) {
    return slot == 0 ? itemStack : LIE;
  }

  public boolean isItemValidForSlot(@Nonnull ItemStack stack) {
    return !stack.isEmpty() && filterIn.apply(stack);
  }

  @Override
  public @Nonnull ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
    if (stack.isEmpty()) {
      return LIE;
    }
    if (slot == 0 && filterIn.apply(stack)) {
      if (itemStack.isEmpty()) {
        int max = Math.min(getMaxStackSize(), stack.getMaxStackSize());
        if (!simulate) {
          itemStack = stack.copy();
        }
        if (stack.getCount() <= max) {
          if (!simulate) {
            onChange(LIE, itemStack);
          }
          return LIE;
        }
        if (!simulate) {
          itemStack.setCount(max);
          onChange(LIE, itemStack);
        }
        ItemStack result = stack.copy();
        result.shrink(max);
        return result;
      }
      if (ItemUtil.areStackMergable(itemStack, stack)) {
        int max = Math.min(getMaxStackSize(), stack.getMaxStackSize());
        int target = itemStack.getCount() + stack.getCount();
        if (target <= max) {
          if (!simulate) {
            ItemStack oldStack = itemStack.copy();
            itemStack.setCount(target);
            onChange(oldStack, itemStack);
          }
          return LIE;
        }
        int tomove = max - itemStack.getCount();
        if (tomove > 0) {
          if (!simulate) {
            ItemStack oldStack = itemStack.copy();
            itemStack.setCount(max);
            onChange(oldStack, itemStack);
          }
          ItemStack result = stack.copy();
          result.shrink(tomove);
          return result;
        }
      }
    }
    return stack;
  }

  @Override
  public @Nonnull ItemStack extractItem(int slot, int amount, boolean simulate) {
    if (slot == 0 && !itemStack.isEmpty() && filterOut.apply(itemStack)) {
      if (amount >= itemStack.getCount()) {
        if (!simulate) {
          ItemStack oldStack = itemStack;
          itemStack = LIE;
          onChange(oldStack, itemStack);
          return oldStack;
        } else {
          return itemStack.copy();
        }
      } else {
        if (!simulate) {
          ItemStack oldStack = itemStack.copy();
          itemStack.shrink(amount);
          onChange(oldStack, itemStack);
          oldStack.setCount(amount);
          return oldStack;
        } else {
          ItemStack result = itemStack.copy();
          result.setCount(amount);
          return result;
        }
      }
    }
    return LIE;
  }

  private void onChange(@Nonnull ItemStack oldStack, @Nonnull ItemStack newStack) {
    callback.onChange(oldStack, newStack);
    if (owner != null) {
      owner.markDirty();
    }
  }

  public void writeToNBT(@Nonnull CompoundNBT tag) {
    if (!itemStack.isEmpty()) {
      itemStack.write(tag);
    }
  }

  public void readFromNBT(@Nonnull CompoundNBT tag) {
    itemStack = ItemStack.read(tag);
  }

  public void clear() {
    itemStack = LIE;
  }

  public void set(@Nonnull ItemStack stack) {
    ItemStack oldStack = itemStack;
    itemStack = stack;
    onChange(oldStack, itemStack);
  }

  public @Nonnull ItemStack get() {
    return itemStack;
  }

  public @Nonnull ItemStack getCopy() {
    return itemStack.copy();
  }

  public boolean isEmpty() {
    return itemStack.isEmpty();
  }

  public int getMaxStackSize() {
    return limit;
  }

  void setOwner(@Nullable TileEntity owner) {
    this.owner = owner;
  }

  TileEntity getOwner() {
    return owner;
  }

  @Override
  public int getSlotLimit(int slot) {
    return getMaxStackSize();
  }

  @Override
  public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
    // TODO: Do we need validation or smth
    return false;
  }

}
