package com.enderio.core.common.inventory;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class EnderSlot extends SlotItemHandler {

  protected final @Nonnull EnderInventory.Type type;

  public EnderSlot(@Nonnull EnderInventory.Type type, @Nonnull InventorySlot itemHandler, int xPosition, int yPosition) {
    super(itemHandler, 0, xPosition, yPosition);
    this.type = type;
  }

  public EnderSlot(@Nonnull EnderInventory.Type type, @Nonnull EnderInventory enderInventory, @Nonnull String ident, int xPosition, int yPosition) {
    super(enderInventory.getSlot(ident), 0, xPosition, yPosition);
    this.type = type;
  }

  public EnderSlot(@Nonnull EnderInventory.View enderInventory, @Nonnull String ident, int xPosition, int yPosition) {
    this(enderInventory.getType(), enderInventory.getParent(), ident, xPosition, yPosition);
  }

  public EnderSlot(@Nonnull EnderInventory.Type type, @Nonnull EnderInventory enderInventory, @Nonnull Enum<?> ident, int xPosition, int yPosition) {
    super(enderInventory.getSlot(ident), 0, xPosition, yPosition);
    this.type = type;
  }

  public EnderSlot(@Nonnull EnderInventory.View enderInventory, @Nonnull Enum<?> ident, int xPosition, int yPosition) {
    this(enderInventory.getType(), enderInventory.getParent(), ident, xPosition, yPosition);
  }

  public static List<EnderSlot> create(@Nonnull EnderInventory enderInventory, @Nonnull EnderInventory.Type type, int xPosition, int yPosition, int cols,
      int rows) {
    return create(enderInventory, type, xPosition, yPosition, 18, 18, cols, rows);
  }

  public static List<EnderSlot> create(@Nonnull EnderInventory enderInventory, @Nonnull EnderInventory.Type type, int xPosition, int yPosition, int xOffset,
      int yOffset, int cols, int rows) {
    List<EnderSlot> result = new ArrayList<EnderSlot>();
    int x = 0, y = 0;
    EnderInventory.View view = enderInventory.getView(type);
    for (int i = 0; i < view.getSlots(); i++) {
      InventorySlot slot = view.getSlot(i);
      if (slot != null) {
        result.add(new EnderSlot(view.getType(), slot, xPosition + x * xOffset, yPosition + y * yOffset));
        x++;
        if (x >= cols) {
          y++;
          x = 0;
          if (y >= rows) {
            return result;
          }
        }
      }
    }

    return result;
  }

  @Override
  public void putStack(@Nonnull ItemStack stack) {
    ((InventorySlot) getItemHandler()).set(stack);
    this.onSlotChanged();
  }

  @Override
  public boolean isItemValid(@Nonnull ItemStack stack) {
    return ((InventorySlot) getItemHandler()).isItemValidForSlot(stack);
  }

  @Override
  public int getItemStackLimit(@Nonnull ItemStack stack) {
    return getSlotStackLimit();
  }

  @Override
  public int getSlotStackLimit() {
    return ((InventorySlot) getItemHandler()).getMaxStackSize();
  }

  @Override
  public boolean isSameInventory(Slot other) {
    return other instanceof EnderSlot && ((InventorySlot) ((EnderSlot) other).getItemHandler()).getOwner() == ((InventorySlot) getItemHandler()).getOwner();
  }

  public @Nonnull EnderInventory.Type getType() {
    return type;
  }

  public boolean is(@Nonnull EnderInventory.Type typeIn) {
    return this.type == typeIn;
  }

  public static boolean is(@Nullable Slot slot, @Nonnull EnderInventory.Type typeIn) {
    return slot != null && slot instanceof EnderSlot && ((EnderSlot) slot).is(typeIn);
  }

}
