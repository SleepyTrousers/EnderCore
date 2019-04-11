package com.enderio.core.common.interfaces;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

public interface IOverlayRenderAware {
  public void renderItemOverlayIntoGUI(@Nonnull ItemStack stack, int xPosition, int yPosition);
}