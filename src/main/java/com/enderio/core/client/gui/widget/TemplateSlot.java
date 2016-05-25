package com.enderio.core.client.gui.widget;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class TemplateSlot extends Slot {

    protected int slotIndex;

    public TemplateSlot(IInventory inventory, int slotIndex, int x, int y) {
        super(inventory, slotIndex, x, y);
        this.slotIndex = slotIndex;
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        return true;
    }

    @Override
    public ItemStack decrStackSize(int par1) {
        return null;
    }

    @Override
    public boolean isItemValid(@Nullable ItemStack stack) {
        return true;
    }

    @Override
    public void putStack(@Nullable ItemStack par1ItemStack) {
        if (par1ItemStack != null) {
            par1ItemStack.stackSize = 0;
        }
        inventory.setInventorySlotContents(slotIndex, par1ItemStack);
        onSlotChanged();
    }

}
