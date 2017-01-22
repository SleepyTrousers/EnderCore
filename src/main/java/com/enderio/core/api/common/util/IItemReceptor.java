package com.enderio.core.api.common.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public interface IItemReceptor {

  boolean canInsertIntoObject(@Nonnull Object into, @Nullable EnumFacing side);

  int doInsertItem(@Nonnull Object into, @Nonnull ItemStack item, @Nullable EnumFacing side);

}
