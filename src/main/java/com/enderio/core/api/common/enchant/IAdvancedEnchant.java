package com.enderio.core.api.common.enchant;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

/**
 * Allows your enchants to have some flavor or description text underneath them
 */
public interface IAdvancedEnchant {

  /**
   * Get the detail for this itemstack
   *
   * @param stack
   * @return a list of <code>String</code>s to be bulleted under the enchantment
   */
  public @Nonnull String[] getTooltipDetails(@Nonnull ItemStack stack);

}
