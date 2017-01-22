package com.enderio.core.api.client.gui;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

public interface IResourceTooltipProvider {

  @Nonnull
  String getUnlocalizedNameForTooltip(@Nonnull ItemStack itemStack);

}
