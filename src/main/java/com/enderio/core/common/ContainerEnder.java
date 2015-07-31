package com.enderio.core.common;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public abstract class ContainerEnder<T extends IInventory> extends Container {

  protected Map<Slot, Point> playerSlotLocations = new HashMap<Slot, Point>();

  protected final int startPlayerSlot;
  protected final int endPlayerSlot;
  protected final int startHotBarSlot;
  protected final int endHotBarSlot;
  
  private T inv;
  
  public ContainerEnder(InventoryPlayer playerInv, T inv) {
    this.inv = inv;
    
    addSlots(playerInv);

    int x = getPlayerInventoryOffset().x;
    int y = getPlayerInventoryOffset().y;

    // add players inventory
    startPlayerSlot = inventorySlots.size();
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 9; ++j) {
        Point loc = new Point(x + j * 18, y + i * 18);
        Slot slot = new Slot(playerInv, j + i * 9 + 9, loc.x, loc.y);
        addSlotToContainer(slot);
        playerSlotLocations.put(slot, loc);
      }
    }
    endPlayerSlot = inventorySlots.size();

    startHotBarSlot = inventorySlots.size();
    for (int i = 0; i < 9; ++i) {
      Point loc = new Point(x + i * 18, y + 58);
      Slot slot = new Slot(playerInv, i, loc.x, loc.y);
      addSlotToContainer(slot);
      playerSlotLocations.put(slot, loc);
    }
    endHotBarSlot = inventorySlots.size();
  }

  protected void addSlots(InventoryPlayer playerInv) {
  }

  public Point getPlayerInventoryOffset() {
    return new Point(8, 84);
  }

  public Point getUpgradeOffset() {
    return new Point(12, 60);
  }

  public T getInv() {
    return inv;
  }

  /**
   * Added validation of slot input
   */
  @Override
  protected boolean mergeItemStack(ItemStack par1ItemStack, int fromIndex, int toIndex, boolean reversOrder) {

    boolean result = false;
    int checkIndex = fromIndex;

    if (reversOrder) {
      checkIndex = toIndex - 1;
    }

    Slot slot;
    ItemStack itemstack1;

    if (par1ItemStack.isStackable()) {

      while (par1ItemStack.stackSize > 0 && (!reversOrder && checkIndex < toIndex || reversOrder && checkIndex >= fromIndex)) {
        slot = (Slot) this.inventorySlots.get(checkIndex);
        itemstack1 = slot.getStack();

        if (itemstack1 != null && itemstack1.getItem() == par1ItemStack.getItem()
            && (!par1ItemStack.getHasSubtypes() || par1ItemStack.getItemDamage() == itemstack1.getItemDamage())
            && ItemStack.areItemStackTagsEqual(par1ItemStack, itemstack1) && slot.isItemValid(par1ItemStack) && par1ItemStack != itemstack1) {

          int mergedSize = itemstack1.stackSize + par1ItemStack.stackSize;
          int maxStackSize = Math.min(par1ItemStack.getMaxStackSize(), slot.getSlotStackLimit());
          if (mergedSize <= maxStackSize) {
            par1ItemStack.stackSize = 0;
            itemstack1.stackSize = mergedSize;
            slot.onSlotChanged();
            result = true;
          } else if (itemstack1.stackSize < maxStackSize) {
            par1ItemStack.stackSize -= maxStackSize - itemstack1.stackSize;
            itemstack1.stackSize = maxStackSize;
            slot.onSlotChanged();
            result = true;
          }
        }

        if (reversOrder) {
          --checkIndex;
        } else {
          ++checkIndex;
        }
      }
    }

    if (par1ItemStack.stackSize > 0) {
      if (reversOrder) {
        checkIndex = toIndex - 1;
      } else {
        checkIndex = fromIndex;
      }

      while (!reversOrder && checkIndex < toIndex || reversOrder && checkIndex >= fromIndex) {
        slot = (Slot) this.inventorySlots.get(checkIndex);
        itemstack1 = slot.getStack();

        if (itemstack1 == null && slot.isItemValid(par1ItemStack)) {
          ItemStack in = par1ItemStack.copy();
          in.stackSize = Math.min(in.stackSize, slot.getSlotStackLimit());

          slot.putStack(in);
          slot.onSlotChanged();
          if (in.stackSize >= par1ItemStack.stackSize) {
            par1ItemStack.stackSize = 0;
          } else {
            par1ItemStack.stackSize -= in.stackSize;
          }
          result = true;
          break;
        }

        if (reversOrder) {
          --checkIndex;
        } else {
          ++checkIndex;
        }
      }
    }

    return result;
  }
}
