package com.enderio.core.common;

import java.awt.Point;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.client.gui.widget.GhostSlot;
import com.enderio.core.common.util.NullHelper;
import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

public abstract class ContainerEnderCap<T extends IItemHandler, S extends TileEntity> extends Container implements GhostSlot.IGhostSlotAware {

  protected final @Nonnull Map<Slot, Point> slotLocations = Maps.newLinkedHashMap();

  public Map<Slot, Point> getSlotLocations() {
    return slotLocations;
  }

  protected int startPlayerSlot;
  protected int endPlayerSlot;
  protected int startHotBarSlot;
  protected int endHotBarSlot;

  private final @Nonnull T inv;
  private final @Nonnull InventoryPlayer playerInv;
  private final @Nullable S te;

  private boolean initRan = false;

  @Nonnull
  private static <T> T checkNotNull(T reference) {
    if (reference == null) {
      throw new NullPointerException();
    }
    return reference;
  }

  public ContainerEnderCap(@Nonnull InventoryPlayer playerInv, @Nonnull T itemHandler, @Nullable S te) {
    inv = checkNotNull(itemHandler);
    this.playerInv = checkNotNull(playerInv);
    this.te = te;

    init(); // TODO: Drop this line and add the init() call whenever a Container is constructed
  }

  public ContainerEnderCap(@Nonnull InventoryPlayer playerInv, @Nonnull T itemHandler, @Nullable S te, boolean unused) {
    inv = checkNotNull(itemHandler);
    this.playerInv = checkNotNull(playerInv);
    this.te = te;
  }

  // use this if you need to chain it to the new call and care about the exact class
  @SuppressWarnings("unchecked")
  @Nonnull
  public final <X> X init() {
    if (initRan) {
      throw new RuntimeException("Ender IO Internal Error 10T (report this to the Ender IO devs)");
    }
    addSlots();

    int x = getPlayerInventoryOffset().x;
    int y = getPlayerInventoryOffset().y;

    // add players inventory
    startPlayerSlot = inventorySlots.size();
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 9; ++j) {
        Slot slot = new Slot(playerInv, j + i * 9 + 9, x + j * 18, y + i * 18);
        addSlotToContainer(slot);
      }
    }
    endPlayerSlot = inventorySlots.size();

    startHotBarSlot = inventorySlots.size();
    for (int i = 0; i < 9; ++i) {
      Slot slot = new Slot(playerInv, i, x + i * 18, y + 58);
      addSlotToContainer(slot);
    }
    endHotBarSlot = inventorySlots.size();

    initRan = true;
    return (X) this;
  }

  @Override
  protected @Nonnull Slot addSlotToContainer(@Nonnull Slot slotIn) {
    slotLocations.put(slotIn, new Point(slotIn.xPos, slotIn.yPos));
    return super.addSlotToContainer(slotIn);
  }

  @SuppressWarnings("null")
  public @Nonnull List<net.minecraft.inventory.Slot> getPlayerSlots() {
    return inventorySlots.stream().filter(x -> x.inventory == playerInv).collect(Collectors.toList());
  }

  public @Nonnull Point getPlayerInventoryOffset() {
    return new Point(0, 54);
  }

  public @Nonnull T getItemHandler() {
    return inv;
  }

  public @Nullable S getTileEntity() {
    return te;
  }

  public @Nonnull S getTileEntityNN() {
    return NullHelper.notnull(te, "Internal logic error, TE-less GUI accessing TE");
  }

  @Override
  public boolean canInteractWith(@Nonnull EntityPlayer player) {
    if (!initRan) {
      throw new RuntimeException("Ender IO Internal Error 10T (report this to the Ender IO devs)");
    }
    final S te2 = te;
    if (te2 != null) {
      World world = te2.getWorld();
      BlockPos pos = te2.getPos();
      if (player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D) {
        return false;
      }
      TileEntity tileEntity = world.getTileEntity(pos);
      if (te2 != tileEntity) {
        return false;
      }
    }
    return true;
  }

  protected abstract void addSlots();

  @Override
  public void setGhostSlotContents(int slot, @Nonnull ItemStack stack, int realsize) {
    if (te instanceof TileEntityBase) {
      ((TileEntityBase) te).setGhostSlotContents(slot, stack, realsize);
    }
  }

  @Override
  public @Nonnull ItemStack transferStackInSlot(@Nonnull EntityPlayer p_82846_1_, int p_82846_2_) {
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

      slot.onTake(p_82846_1_, itemstack1);
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

        if (slot.isEnabled() && !itemstack1.isEmpty() && itemstack1.getItem() == par1ItemStack.getItem()
            && (!par1ItemStack.getHasSubtypes() || par1ItemStack.getItemDamage() == itemstack1.getItemDamage())
            && ItemStack.areItemStackTagsEqual(par1ItemStack, itemstack1) && slot.isItemValid(par1ItemStack) && par1ItemStack != itemstack1) {

          int mergedSize = itemstack1.getCount() + par1ItemStack.getCount();
          int maxStackSize = Math.min(par1ItemStack.getMaxStackSize(), slot.getItemStackLimit(par1ItemStack));
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

        if (slot.isEnabled() && itemstack1.isEmpty() && slot.isItemValid(par1ItemStack)) {
          ItemStack in = par1ItemStack.copy();
          in.setCount(Math.min(in.getCount(), slot.getItemStackLimit(par1ItemStack)));

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
  public void detectAndSendChanges() {
    super.detectAndSendChanges();
    // keep in sync with ContainerEnder#detectAndSendChanges()
    final SPacketUpdateTileEntity updatePacket = te != null ? te.getUpdatePacket() : null;
    if (updatePacket != null) {
      for (IContainerListener containerListener : listeners) {
        if (containerListener instanceof EntityPlayerMP) {
          ((EntityPlayerMP) containerListener).connection.sendPacket(updatePacket);
        }
      }
    }
  }

}
