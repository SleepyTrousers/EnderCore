package com.enderio.core.common.fluid;

import javax.annotation.Nullable;

import com.enderio.core.common.fluid.IFluidWrapper;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

public class FluidContainerItemFluidWrapper implements IFluidWrapper {

  private final IFluidContainerItem fluidContainerItem;
  private final ItemStack itemStack;
  
  public FluidContainerItemFluidWrapper(IFluidContainerItem fluidContainerItem, ItemStack itemStack) {
    this.fluidContainerItem = fluidContainerItem;
    this.itemStack = itemStack;
  }

  @Override
  public int offer(FluidStack resource) {
    return fluidContainerItem.fill(itemStack, resource, false);
  }

  @Override
  public int fill(FluidStack resource) {
    return fluidContainerItem.fill(itemStack, resource, true);
  }

  @Override
  @Nullable
  public FluidStack drain(FluidStack resource) {
    if (resource.isFluidEqual(contents())) {
      return fluidContainerItem.drain(itemStack, resource.amount, true);
    }
    return null;
  }

  @Override
  @Nullable
  public FluidStack contents() {
    return fluidContainerItem.getFluid(itemStack);
  }

}
