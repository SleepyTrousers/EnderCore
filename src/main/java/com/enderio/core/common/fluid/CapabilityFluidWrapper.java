package com.enderio.core.common.fluid;

import javax.annotation.Nullable;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class CapabilityFluidWrapper implements IFluidWrapper {

  private final net.minecraftforge.fluids.capability.IFluidHandler fluidHandler;

  public CapabilityFluidWrapper(IFluidHandler fluidHandler) {
    this.fluidHandler = fluidHandler;
  }

  @Override
  public int offer(FluidStack resource) {
    return fluidHandler.fill(resource, false);
  }

  @Override
  public int fill(FluidStack resource) {
    return fluidHandler.fill(resource, true);
  }

  @Override
  @Nullable
  public FluidStack drain(FluidStack resource) {
    return fluidHandler.drain(resource, true);
  }

  @Override
  @Nullable
  public FluidStack contents() {
    return fluidHandler.drain(Integer.MAX_VALUE, false);
  }

}
