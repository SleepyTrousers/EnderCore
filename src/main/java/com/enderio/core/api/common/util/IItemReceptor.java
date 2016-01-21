package com.enderio.core.api.common.util;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public interface IItemReceptor {

  boolean canInsertIntoObject(Object into, EnumFacing side);

  int doInsertItem(Object into, ItemStack item, EnumFacing side);

}
