package com.enderio.core.common.fluid;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

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
    if (resource.isFluidEqual(getAvailableFluid())) {
      return fluidContainerItem.drain(itemStack, resource.amount, true);
    }
    return null;
  }

  @Override
  @Nullable
  public FluidStack getAvailableFluid() {
    return fluidContainerItem.getFluid(itemStack);
  }

  @SuppressWarnings("null")
  @Override
  @Nonnull
  public List<ITankInfoWrapper> getTankInfoWrappers() {
    return Collections.<ITankInfoWrapper> singletonList(new InfoWrapper());
  }

  private class InfoWrapper implements ITankInfoWrapper {

    @Override
    public IFluidTankProperties getIFluidTankProperties() {
      return new IFluidTankProperties() {

        @Override
        @Nullable
        public FluidStack getContents() {
          return fluidContainerItem.getFluid(itemStack);
        }

        @Override
        public int getCapacity() {
          return fluidContainerItem.getCapacity(itemStack);
        }

        @Override
        public boolean canFill() {
          return true;
        }

        @Override
        public boolean canDrain() {
          return true;
        }

        @Override
        public boolean canFillFluidType(FluidStack fluidStack) {
          return true;
        }

        @Override
        public boolean canDrainFluidType(FluidStack fluidStack) {
          return true;
        }

      };
    }

    @Override
    public FluidTankInfo getFluidTankInfo() {
      return new FluidTankInfo(fluidContainerItem.getFluid(itemStack), fluidContainerItem.getCapacity(itemStack));
    }

  }

}
