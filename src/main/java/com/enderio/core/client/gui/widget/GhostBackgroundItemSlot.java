package com.enderio.core.client.gui.widget;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.NNList;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Util;

public class GhostBackgroundItemSlot extends GhostSlot {

  private @Nonnull ItemStack stack;
  private @Nullable final NNList<ItemStack> stacks;
  private int idx = 999;
  private final @Nullable Slot parent;
  private long lastSwitch = 0;

  private GhostBackgroundItemSlot(@Nonnull ItemStack stack, @Nullable List<ItemStack> stacks, @Nullable Slot parent, int x, int y) {
    this.stack = stack;
    if (stack.isEmpty() && stacks != null && !stacks.isEmpty()) {
      this.stacks = new NNList<ItemStack>(stacks);
    } else {
      this.stacks = null;
    }
    this.parent = parent;
    this.setX(x);
    this.setY(y);
    this.setGrayOut(true);
    this.setGrayOutLevel(.75f);
    this.setDrawStdTooltip(false);
    this.setdrawFakeHover(false);
  }

  public GhostBackgroundItemSlot(@Nonnull ItemStack stack, int x, int y) {
    this(stack, null, null, x, y);
  }

  public GhostBackgroundItemSlot(@Nonnull List<ItemStack> stacks, int x, int y) {
    this(ItemStack.EMPTY, stacks, null, x, y);
  }

  public GhostBackgroundItemSlot(@Nonnull ItemStack stack, @Nonnull Slot parent) {
    this(stack, null, parent, parent.xPos, parent.yPos);
  }

  public GhostBackgroundItemSlot(@Nonnull List<ItemStack> stacks, @Nonnull Slot parent) {
    this(ItemStack.EMPTY, stacks, parent, parent.xPos, parent.yPos);
  }

  public GhostBackgroundItemSlot(@Nonnull Item item, int x, int y) {
    this(new ItemStack(item), x, y);
  }

  public GhostBackgroundItemSlot(@Nonnull Block block, int x, int y) {
    this(new ItemStack(block), x, y);
  }

  public GhostBackgroundItemSlot(@Nonnull Item item, @Nonnull Slot parent) {
    this(new ItemStack(item), parent);
  }

  public GhostBackgroundItemSlot(@Nonnull Block block, @Nonnull Slot parent) {
    this(new ItemStack(block), parent);
  }

  @Override
  public boolean isMouseOver(double mx, double my) {
    return false;
  }

  @Override
  public @Nonnull ItemStack getStack() {
    final NonNullList<ItemStack> stacks2 = stacks;
    if (stacks2 != null && Util.milliTime() - lastSwitch > 1000L) {
      lastSwitch = Util.milliTime();
      if (++idx >= stacks2.size()) {
        idx = 0;
        Collections.shuffle(stacks2);
      }
      stack = stacks2.get(idx);
    }
    return stack;
  }

  @Override
  public void putStack(@Nonnull ItemStack stackIn, int realsize) {
  }

  @Override
  public boolean isVisible() {
    final Slot parent2 = parent;
    return parent2 != null ? parent2.xPos >= 0 && parent2.yPos >= 0 && !parent2.getHasStack() && super.isVisible() : super.isVisible();
  }

  @Override
  public int getX() {
    final Slot parent2 = parent;
    return parent2 != null ? parent2.xPos : super.getX();
  }

  @Override
  public int getY() {
    final Slot parent2 = parent;
    return parent2 != null ? parent2.yPos : super.getY();
  }

}