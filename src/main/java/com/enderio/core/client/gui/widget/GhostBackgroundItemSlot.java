package com.enderio.core.client.gui.widget;

import net.minecraft.block.Block;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class GhostBackgroundItemSlot extends GhostSlot {

    private final ItemStack stack;
    private final Slot parent;

    public GhostBackgroundItemSlot(ItemStack stack, int x, int y) {
        this.stack = stack;
        this.parent = null;
        this.x = x;
        this.y = y;
        this.grayOut = true;
        this.grayOutLevel = .75f;
    }

    public GhostBackgroundItemSlot(ItemStack stack, Slot parent) {
        this.stack = stack;
        this.parent = parent;
        this.x = parent.xDisplayPosition;
        this.y = parent.yDisplayPosition;
        this.grayOut = true;
        this.grayOutLevel = .75f;
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
        return stack;
    }

    @Override
    public void putStack(ItemStack stack) {}

    @Override
    public boolean isVisible() {
        return parent != null ? parent.xDisplayPosition >= 0 && parent.yDisplayPosition >= 0 : super.isVisible();
    }

}
