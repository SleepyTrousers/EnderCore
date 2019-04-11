package com.enderio.core.common.interfaces;

import javax.annotation.Nonnull;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public interface IElytraFlyingProvider {
  public boolean isElytraFlying(@Nonnull EntityLivingBase entity, @Nonnull ItemStack itemstack, boolean shouldStop);
}