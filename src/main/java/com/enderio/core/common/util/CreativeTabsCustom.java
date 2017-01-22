package com.enderio.core.common.util;

import javax.annotation.Nonnull;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class CreativeTabsCustom extends CreativeTabs {

  private @Nonnull ItemStack displayStack = ItemStack.EMPTY;

  public CreativeTabsCustom(@Nonnull String unloc) {
    super(unloc);
  }

  /**
   * @param item
   *          Item to display
   */
  public CreativeTabsCustom setDisplay(@Nonnull Item item) {
    return setDisplay(item, 0);
  }

  /**
   * @param item
   *          Item to display
   * @param damage
   *          Damage of item to display
   */
  public CreativeTabsCustom setDisplay(@Nonnull Item item, int damage) {
    return setDisplay(new ItemStack(item, 1, damage));
  }

  /**
   * @param display
   *          ItemStack to display
   */
  public CreativeTabsCustom setDisplay(@Nonnull ItemStack display) {
    this.displayStack = display.copy();
    return this;
  }

  @Override
  public @Nonnull ItemStack getTabIconItem() {
    return displayStack;
  }
}
