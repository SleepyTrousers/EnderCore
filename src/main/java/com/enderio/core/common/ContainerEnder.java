package com.enderio.core.common;

import java.awt.Point;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.client.gui.widget.GhostSlot;
import com.enderio.core.common.ContainerEnderCap.BaseSlotItemHandler;
import com.enderio.core.common.util.NullHelper;
import com.google.common.collect.Maps;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

@Deprecated
public class ContainerEnder<T extends IInventory> extends Container implements GhostSlot.IGhostSlotAware {

  protected final @Nonnull Map<Slot, Point> playerSlotLocations = Maps.newLinkedHashMap();

  protected final int startPlayerSlot;
  protected final int endPlayerSlot;
  protected final int startHotBarSlot;
  protected final int endHotBarSlot;

  private final @Nonnull T inv;
  private final @Nonnull PlayerInventory playerInv;

  @Nonnull
  private static <T> T checkNotNull(T reference) {
    if (reference == null) {
      throw new NullPointerException();
    }
    return reference;
  }

  public ContainerEnder(@Nullable ContainerType<?> type, int id, @Nonnull PlayerInventory playerInv, @Nonnull T inv) {
    super(type, id);
    this.inv = checkNotNull(inv);
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

  public @Nonnull T getInv() {
    return inv;
  }

//  @Override
//  @Nonnull
//  public Slot getSlotFromInventory(@Nonnull IInventory invIn, int slotIn) {
//    return NullHelper.notnull(super.getSlotFromInventory(invIn, slotIn), "Logic error, missing slot " + slotIn);
//  }
//
//  @Nonnull
//  public Slot getSlotFromInventory(int slotIn) {
//    return getSlotFromInventory(getInv(), slotIn);
//  }


  @Override
  public boolean canInteractWith(PlayerEntity playerIn) {
    return getInv().isUsableByPlayer(playerIn);
  }

  @Override
  public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = this.inventorySlots.get(index);

    if (slot != null && slot.getHasStack()) {
      ItemStack itemstack1 = slot.getStack();
      itemstack = itemstack1.copy();

      int minPlayerSlot = inventorySlots.size() - playerInv.mainInventory.size();
      if (index < minPlayerSlot) {
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

      if (itemstack1.getCount() == itemstack.getCount()) {
        return ItemStack.EMPTY;
      }

      slot.onTake(playerIn, itemstack1);
    }

    return itemstack;
  }

  /**
   * Added validation of slot input
   */
  @Override
  protected boolean mergeItemStack(@Nonnull ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
    boolean flag = false;
    int i = startIndex;
    if (reverseDirection) {
      i = endIndex - 1;
    }

    if (stack.isStackable()) {
      while(!stack.isEmpty()) {
        if (reverseDirection) {
          if (i < startIndex) {
            break;
          }
        } else if (i >= endIndex) {
          break;
        }

        Slot slot = this.inventorySlots.get(i);
        ItemStack itemstack = slot.getStack();
        if (!itemstack.isEmpty() && areItemsAndTagsEqual(stack, itemstack)) {
          int j = itemstack.getCount() + stack.getCount();
          int maxSize = Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());
          if (j <= maxSize) {
            stack.setCount(0);
            itemstack.setCount(j);
            slot.onSlotChanged();
            flag = true;
          } else if (itemstack.getCount() < maxSize) {
            stack.shrink(maxSize - itemstack.getCount());
            itemstack.setCount(maxSize);
            slot.onSlotChanged();
            flag = true;
          }
        }

        if (reverseDirection) {
          --i;
        } else {
          ++i;
        }
      }
    }

    if (!stack.isEmpty()) {
      if (reverseDirection) {
        i = endIndex - 1;
      } else {
        i = startIndex;
      }

      while(true) {
        if (reverseDirection) {
          if (i < startIndex) {
            break;
          }
        } else if (i >= endIndex) {
          break;
        }

        Slot slot1 = this.inventorySlots.get(i);
        ItemStack itemstack1 = slot1.getStack();
        if (itemstack1.isEmpty() && slot1.isItemValid(stack)) {
          if (stack.getCount() > slot1.getSlotStackLimit()) {
            slot1.putStack(stack.split(slot1.getSlotStackLimit()));
          } else {
            slot1.putStack(stack.split(stack.getCount()));
          }

          slot1.onSlotChanged();
          flag = true;
          break;
        }

        if (reverseDirection) {
          --i;
        } else {
          ++i;
        }
      }
    }

    return flag;
  }

  @Override
  public void setGhostSlotContents(int slot, @Nonnull ItemStack stack, int realsize) {
    if (inv instanceof TileEntityBase) {
      ((TileEntityBase) inv).setGhostSlotContents(slot, stack, realsize);
    }
  }

  private static final Field listeners;

  static {
    try {
      listeners = ObfuscationReflectionHelper.findField(Container.class, "listeners");
      listeners.setAccessible(true);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected List<IContainerListener> getListeners() {
    try {
      Object val = listeners.get(this);
      return (List<IContainerListener>) val;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void detectAndSendChanges() {
    super.detectAndSendChanges();
    if (inv instanceof TileEntityBase) {
      // keep in sync with ContainerEnderCap#detectAndSendChanges()
      final SUpdateTileEntityPacket updatePacket = ((TileEntityBase) inv).getUpdatePacket();
      if (updatePacket != null) {
        for (IContainerListener containerListener : getListeners()) {
          if (containerListener instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity) containerListener).connection.sendPacket(updatePacket);
          }
        }
      }
    }
  }

  private boolean isSlotEnabled(Slot slot) {
    return slot != null && (!(slot instanceof ContainerEnder.BaseSlot) || ((ContainerEnder.BaseSlot) slot).isEnabled())
        && (!(slot instanceof BaseSlotItemHandler) || ((BaseSlotItemHandler) slot).isEnabled());
  }

  public static abstract class BaseSlot extends Slot {

    public BaseSlot(@Nonnull IInventory inventoryIn, int index, int xPosition, int yPosition) {
      super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean isEnabled() {
      // don't super here, super is sided
      return true;
    }

  }

}
