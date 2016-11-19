package com.enderio.core.client.gui.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class GhostBackgroundItemSlot extends GhostSlot {

  private ItemStack stack;
  private final List<ItemStack> stacks;
  private int idx = 999;
  private final Slot parent;
  private long lastSwitch = 0;

  private GhostBackgroundItemSlot(ItemStack stack, List<ItemStack> stacks, Slot parent, int x, int y) {
    this.stack = stack;
    if (stack == null && stacks != null && !stacks.isEmpty()) {
      this.stacks = new ArrayList<ItemStack>(stacks);
    } else {
      this.stacks = null;
    }
    this.parent = parent;
    this.x = x;
    this.y = y;
    this.grayOut = true;
    this.grayOutLevel = .75f;
  }

  public GhostBackgroundItemSlot(ItemStack stack, int x, int y) {
    this(stack, null, null, x, y);
  }

  public GhostBackgroundItemSlot(List<ItemStack> stacks, int x, int y) {
    this(null, stacks, null, x, y);
  }

  public GhostBackgroundItemSlot(ItemStack stack, Slot parent) {
    this(stack, null, parent, parent.xDisplayPosition, parent.yDisplayPosition);
  }

  public GhostBackgroundItemSlot(List<ItemStack> stacks, Slot parent) {
    this(null, stacks, parent, parent.xDisplayPosition, parent.yDisplayPosition);
  }

  public GhostBackgroundItemSlot(Item item, int x, int y) {
    this(new ItemStack(item), x, y);
  }

  public GhostBackgroundItemSlot(Block block, int x, int y) {
    this(new ItemStack(block), x, y);
  }

  public GhostBackgroundItemSlot(Item item, Slot parent) {
    this(new ItemStack(item), parent);
  }

  public GhostBackgroundItemSlot(Block block, Slot parent) {
    this(new ItemStack(block), parent);
  }

  @Override
  public boolean isMouseOver(int mx, int my) {
    return false;
  }

  @Override
  public ItemStack getStack() {
    if (stacks != null && Minecraft.getSystemTime() - lastSwitch > 1000L) {
      lastSwitch = Minecraft.getSystemTime();
      if (++idx >= stacks.size()) {
        idx = 0;
        Collections.shuffle(stacks);
      }
      stack = stacks.get(idx);
    }
    return stack;
  }

  @Override
  public void putStack(ItemStack stackIn) {
  }

  @Override
  public boolean isVisible() {
    return parent != null ? parent.xDisplayPosition >= 0 && parent.yDisplayPosition >= 0 && !parent.getHasStack() && super.isVisible() : super.isVisible();
  }

}