package com.enderio.core.common;

import java.awt.Point;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.client.gui.widget.GhostSlot;
import com.google.common.collect.Maps;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

@Deprecated // not used by anyone, so probably should be used without good reason?
public abstract class ContainerItemHandler<T extends ICapabilityProvider> extends Container implements GhostSlot.IGhostSlotAware {

  protected final @Nonnull Map<Slot, Point> playerSlotLocations = Maps.newLinkedHashMap();

  protected final int startPlayerSlot;
  protected final int endPlayerSlot;
  protected final int startHotBarSlot;
  protected final int endHotBarSlot;

  private final @Nonnull T owner;
  private final @Nonnull IItemHandler inv;
  private final @Nonnull PlayerInventory playerInv;

  @Nonnull
  private static <T> T checkNotNull(T reference) {
    if (reference == null) {
      throw new NullPointerException();
    }
    return reference;
  }

  public ContainerItemHandler(@Nullable ContainerType<?> type, int id, @Nonnull PlayerInventory playerInv, @Nonnull T owner, @Nullable Direction direction) {
    super(type, id);
    this.owner = checkNotNull(owner);
    this.inv = checkNotNull(owner.getCapability(checkNotNull(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY), direction).resolve().orElse(null));
    this.playerInv = checkNotNull(playerInv);

    addSlots(this.playerInv);

    int x = getPlayerInventoryOffset().x;
    int y = getPlayerInventoryOffset().y;

    // add players inventory
    startPlayerSlot = inventorySlots.size();
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 9; ++j) {
        Point loc = new Point(x + j * 18, y + i * 18);
        Slot slot = new Slot(this.playerInv, j + i * 9 + 9, loc.x, loc.y);
        addSlot(slot);
        playerSlotLocations.put(slot, loc);
      }
    }
    endPlayerSlot = inventorySlots.size();

    startHotBarSlot = inventorySlots.size();
    for (int i = 0; i < 9; ++i) {
      Point loc = new Point(x + i * 18, y + 58);
      Slot slot = new Slot(this.playerInv, i, loc.x, loc.y);
      addSlot(slot);
      playerSlotLocations.put(slot, loc);
    }
    endHotBarSlot = inventorySlots.size();
  }

  protected void addSlots(@Nonnull PlayerInventory playerInventory) {
  }

  public @Nonnull Point getPlayerInventoryOffset() {
    return new Point(8, 84);
  }

  public @Nonnull Point getUpgradeOffset() {
    return new Point(12, 60);
  }

  public @Nonnull T getOwner() {
    return owner;
  }

  public @Nonnull IItemHandler getInv() {
    return inv;
  }

  @Override
  public @Nonnull ItemStack transferStackInSlot(@Nonnull PlayerEntity p_82846_1_, int p_82846_2_) {
    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = this.inventorySlots.get(p_82846_2_);

    if (slot != null && slot.getHasStack()) {
      ItemStack itemstack1 = slot.getStack();
      itemstack = itemstack1.copy();

      int minPlayerSlot = inventorySlots.size() - playerInv.mainInventory.size();
      if (p_82846_2_ < minPlayerSlot) {
        if (!this.mergeItemStack(itemstack1, minPlayerSlot, this.inventorySlots.size(), true)) {
          return ItemStack.EMPTY;
        }
      } else if (!this.mergeItemStack(itemstack1, 0, minPlayerSlot, false)) {
        return ItemStack.EMPTY;
      }

      if (itemstack1.isEmpty()) {
        slot.putStack(ItemStack.EMPTY);
      } else {
        slot.onSlotChanged();
      }
    }

    return itemstack;
  }

  /**
   * Added validation of slot input
   */
  @Override
  protected boolean mergeItemStack(@Nonnull ItemStack par1ItemStack, int fromIndex, int toIndex, boolean reversOrder) {

    boolean result = false;
    int checkIndex = fromIndex;

    if (reversOrder) {
      checkIndex = toIndex - 1;
    }

    Slot slot;
    ItemStack itemstack1;

    if (par1ItemStack.isStackable()) {

      while (!par1ItemStack.isEmpty() && (!reversOrder && checkIndex < toIndex || reversOrder && checkIndex >= fromIndex)) {
        slot = this.inventorySlots.get(checkIndex);
        itemstack1 = slot.getStack();

        if (!itemstack1.isEmpty() && itemstack1.getItem() == par1ItemStack.getItem()
            && ItemStack.areItemStackTagsEqual(par1ItemStack, itemstack1) && slot.isItemValid(par1ItemStack) && par1ItemStack != itemstack1) {

          int mergedSize = itemstack1.getCount() + par1ItemStack.getCount();
          int maxStackSize = Math.min(par1ItemStack.getMaxStackSize(), slot.getSlotStackLimit());
          if (mergedSize <= maxStackSize) {
            par1ItemStack.setCount(0);
            itemstack1.setCount(mergedSize);
            slot.onSlotChanged();
            result = true;
          } else if (itemstack1.getCount() < maxStackSize) {
            par1ItemStack.shrink(maxStackSize - itemstack1.getCount());
            itemstack1.setCount(maxStackSize);
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

    if (!par1ItemStack.isEmpty()) {
      if (reversOrder) {
        checkIndex = toIndex - 1;
      } else {
        checkIndex = fromIndex;
      }

      while (!reversOrder && checkIndex < toIndex || reversOrder && checkIndex >= fromIndex) {
        slot = this.inventorySlots.get(checkIndex);
        itemstack1 = slot.getStack();

        if (itemstack1.isEmpty() && slot.isItemValid(par1ItemStack)) {
          ItemStack in = par1ItemStack.copy();
          in.setCount(Math.min(in.getCount(), slot.getSlotStackLimit()));

          slot.putStack(in);
          slot.onSlotChanged();
          par1ItemStack.shrink(in.getCount());
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

  @Override
  public void setGhostSlotContents(int slot, @Nonnull ItemStack stack, int realsize) {
    if (owner instanceof TileEntityBase) {
      ((TileEntityBase) owner).setGhostSlotContents(slot, stack, realsize);
    }

  }
}
