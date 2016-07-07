package com.enderio.core.common.fluid;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class FluidTankFluidWrapper implements IFluidWrapper {

  private final FluidTank fluidTank;
  
  public FluidTankFluidWrapper(FluidTank fluidTank) {
    this.fluidTank = fluidTank;
  }

  @Override
  public int offer(FluidStack resource) {
    return fluidTank.fill(resource, false);
  }

  @Override
  public int fill(FluidStack resource) {
    return fluidTank.fill(resource, true);
  }

  @Override
  @Nullable
  public FluidStack drain(FluidStack resource) {
    return fluidTank.drain(resource, true);
  }

  @Override
  @Nullable
  public FluidStack getAvailableFluid() {
    return fluidTank.getFluid();
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
          return fluidTank.getFluid();
        }

        @Override
        public int getCapacity() {
          return fluidTank.getCapacity();
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
      return new FluidTankInfo(fluidTank);
    }

  }

}
