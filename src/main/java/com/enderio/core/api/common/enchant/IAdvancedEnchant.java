package com.enderio.core.api.common.enchant;

import net.minecraft.item.ItemStack;

/**
 * Allows your enchants to have some flavor or descrtiption text underneath them
 */
public interface IAdvancedEnchant {

    /**
     * Get the detail for this itemstack
     * 
     * @param stack
     * @return a list of <code>String</code>s to be bulleted under the enchantment
     */
    public String[] getTooltipDetails(ItemStack stack);
}
