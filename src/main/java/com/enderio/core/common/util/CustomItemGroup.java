package com.enderio.core.common.util;

import javax.annotation.Nonnull;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CustomItemGroup extends ItemGroup {

  private @Nonnull ItemStack displayStack = ItemStack.EMPTY;

  public CustomItemGroup(@Nonnull String label) {
    super(label);
  }

  /**
   * @param item
   *          Item to display
   */
  public CustomItemGroup setDisplay(@Nonnull Item item) {
    return setDisplay(item, 0);
  }

  /**
   * @param item
   *          Item to display
   * @param damage
   *          Damage of item to display
   */
  public CustomItemGroup setDisplay(@Nonnull Item item, int damage) {
    return setDisplay(new ItemStack(item, 1));
  }

  /**
   * @param display
   *          ItemStack to display
   */
  public CustomItemGroup setDisplay(@Nonnull ItemStack display) {
    this.displayStack = display.copy();
    return this;
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public ItemStack createIcon() {
    return displayStack;
  }
}
