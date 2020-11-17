package com.enderio.core.common;

import java.awt.Point;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.client.gui.widget.GhostSlot;
import com.enderio.core.common.util.NullHelper;
import com.google.common.collect.Maps;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

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
  private final @Nonnull PlayerInventory playerInv;
  private final @Nullable S te;

  private boolean initRan = false;

  @Nonnull
  private static <T> T checkNotNull(T reference) {
    if (reference == null) {
      throw new NullPointerException();
    }
    return reference;
  }

  public ContainerEnderCap(@Nullable ContainerType<?> type, int id, @Nonnull PlayerInventory playerInv, @Nonnull T itemHandler, @Nullable S te) {
    super(type, id);
    inv = checkNotNull(itemHandler);
    this.playerInv = checkNotNull(playerInv);
    this.te = te;

    init(); // TODO: Drop this line and add the init() call whenever a Container is constructed
  }

  public ContainerEnderCap(@Nullable ContainerType<?> type, int id, @Nonnull PlayerInventory playerInv, @Nonnull T itemHandler, @Nullable S te, boolean unused) {
    super(type, id);
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
        addSlot(slot);
      }
    }
    endPlayerSlot = inventorySlots.size();

    startHotBarSlot = inventorySlots.size();
    for (int i = 0; i < 9; ++i) {
      Slot slot = new Slot(playerInv, i, x + i * 18, y + 58);
      addSlot(slot);
    }
    endHotBarSlot = inventorySlots.size();

    initRan = true;
    return (X) this;
  }

  @Override
  protected Slot addSlot(Slot slotIn) {
    slotLocations.put(slotIn, new Point(slotIn.xPos, slotIn.yPos));
    return super.addSlot(slotIn);
  }

  @SuppressWarnings("null")
  public @Nonnull List<Slot> getPlayerSlots() {
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
  public boolean canInteractWith(@Nonnull PlayerEntity player) {
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
  public @Nonnull ItemStack transferStackInSlot(@Nonnull PlayerEntity player, int fromSlotId) {
    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = inventorySlots.get(fromSlotId);

    if (slot != null && slot.getHasStack()) {
      ItemStack stackToMove = slot.getStack();
      itemstack = stackToMove.copy();

      if (!mergeItemStack(stackToMove, mapSlotToTargets(fromSlotId))) {
        return ItemStack.EMPTY;
      }

      slot.putStack(stackToMove); // slot may not be tracking the stack it handed out
      slot.onSlotChanged();
      slot.onTake(player, stackToMove);

      if (player.world.isRemote && ItemStack.areItemsEqual(slot.getStack(), itemstack)) {
        // it seems this slot depends on the server executing the move. Return a different value on client and server to force a sync after the move is
        // executed. And to prevent the client from going into an infinite loop...
        return ItemStack.EMPTY;
      }
    }

    return itemstack;
  }

  /**
   * Creates a mapping for shift-clicks from a slot ID (the on that was shift-clicked) to a list of {@link Slot}s (the ones that can be inserted into).
   * <p>
   * Please note that the "try to fill up stacks" logic will look at <em>all</em> slots before the "move into empty slot" logic runs.
   * 
   * @param fromSlotId
   *          The slot that was clicked
   * @return slots the item can go in order of preference
   */
  protected @Nonnull Collection<Slot> mapSlotToTargets(int fromSlotId) {
    List<Slot> result = new ArrayList<>();
    if (fromSlotId < startPlayerSlot) {
      for (int i = startPlayerSlot; i < inventorySlots.size(); i++) {
        result.add(0, inventorySlots.get(i));
      }
    } else {
      for (int i = 0; i < startPlayerSlot; i++) {
        result.add(inventorySlots.get(i));
      }
    }
    return result;
  }

  /**
   * @deprecated unused, see {@link #mergeItemStack(ItemStack, Collection)}
   */
  @Override
  @Deprecated
  protected final boolean mergeItemStack(@Nonnull ItemStack par1ItemStack, int fromIndex, int toIndex, boolean reversOrder) {
    return false;
  }

  protected boolean mergeItemStack(ItemStack stackToMove, Collection<Slot> targets) {
    boolean result = false;

    if (stackToMove.isStackable()) {
      for (Slot slot : targets) {
        if (isSlotEnabled(slot) && slot.getHasStack()) {
          ItemStack stackInSlot = slot.getStack();
          if (stackInSlot.getItem() == stackToMove.getItem()
              && ItemStack.areItemStackTagsEqual(stackToMove, stackInSlot) && slot.isItemValid(stackToMove) && stackToMove != stackInSlot) {
            int mergedSize = stackInSlot.getCount() + stackToMove.getCount();
            int maxStackSize = Math.min(stackToMove.getMaxStackSize(), slot.getItemStackLimit(stackToMove));
            if (mergedSize <= maxStackSize) {
              stackToMove.setCount(0);
              stackInSlot.setCount(mergedSize);
              slot.onSlotChanged();
              return true;
            } else if (stackInSlot.getCount() < maxStackSize) {
              stackToMove.shrink(maxStackSize - stackInSlot.getCount());
              stackInSlot.setCount(maxStackSize);
              slot.onSlotChanged();
              result = true;
            }
          }
        }
      }
    }

    for (Slot slot : targets) {
      if (isSlotEnabled(slot) && !slot.getHasStack() && slot.isItemValid(stackToMove)) {
        ItemStack in = stackToMove.copy();
        in.setCount(Math.min(in.getCount(), slot.getItemStackLimit(stackToMove)));
        slot.putStack(in);
        slot.onSlotChanged();
        stackToMove.shrink(in.getCount());
        if (stackToMove.isEmpty()) {
          return true;
        }
        result = true;
      }
    }

    return result;
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
    // keep in sync with ContainerEnder#detectAndSendChanges()
    final SUpdateTileEntityPacket updatePacket = te != null ? te.getUpdatePacket() : null;
    if (updatePacket != null) {
      for (IContainerListener containerListener : getListeners()) {
        if (containerListener instanceof ServerPlayerEntity) {
          ((ServerPlayerEntity) containerListener).connection.sendPacket(updatePacket);
        }
      }
    }
  }

  protected boolean isSlotEnabled(Slot slot) {
    return slot != null && (!(slot instanceof ContainerEnder.BaseSlot) || ((ContainerEnder.BaseSlot) slot).isEnabled())
        && (!(slot instanceof BaseSlotItemHandler) || ((BaseSlotItemHandler) slot).isEnabled());
  }

  public static abstract class BaseSlotItemHandler extends SlotItemHandler {

    public BaseSlotItemHandler(@Nonnull IItemHandler itemHandler, int index, int xPosition, int yPosition) {
      super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean isEnabled() {
      // don't super here, super is sided
      return true;
    }

  }

}
