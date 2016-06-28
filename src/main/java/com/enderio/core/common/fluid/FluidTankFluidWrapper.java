package com.enderio.core.common.fluid;

import javax.annotation.Nullable;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

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
  public FluidStack contents() {
    return fluidTank.getFluid();
  }

}
