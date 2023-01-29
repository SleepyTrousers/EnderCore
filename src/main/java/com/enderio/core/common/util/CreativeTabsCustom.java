package com.enderio.core.common.util;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class CreativeTabsCustom extends CreativeTabs {

    private ItemStack displayStack;

    public CreativeTabsCustom(String unloc) {
        super(unloc);
    }

    /**
     * @param item Item to display
     */
    public CreativeTabsCustom setDisplay(Item item) {
        return setDisplay(item, 0);
    }

    /**
     * @param item   Item to display
     * @param damage Damage of item to display
     */
    public CreativeTabsCustom setDisplay(Item item, int damage) {
        return setDisplay(new ItemStack(item, 1, damage));
    }

    /**
     * @param display ItemStack to display
     */
    public CreativeTabsCustom setDisplay(ItemStack display) {
        this.displayStack = display.copy();
        return this;
    }

    @Override
    public ItemStack getIconItemStack() {
        return displayStack == null ? new ItemStack(Items.diamond) : displayStack;
    }

    @Override
    public Item getTabIconItem() {
        return null; // defaults to getIconItemStack()
    }
}
