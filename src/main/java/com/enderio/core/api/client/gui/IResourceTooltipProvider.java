package com.enderio.core.api.client.gui;

import net.minecraft.item.ItemStack;

public interface IResourceTooltipProvider {

    String getUnlocalizedNameForTooltip(ItemStack itemStack);

}
