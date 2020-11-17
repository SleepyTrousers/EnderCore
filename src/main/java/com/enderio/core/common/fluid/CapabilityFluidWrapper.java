package com.enderio.core.common.fluid;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class CapabilityFluidWrapper implements IFluidWrapper {

  private final net.minecraftforge.fluids.capability.IFluidHandler fluidHandler;

  public CapabilityFluidWrapper(IFluidHandler fluidHandler) {
    this.fluidHandler = fluidHandler;
  }

  @Override
  public int offer(FluidStack resource) {
    return fluidHandler.fill(resource, IFluidHandler.FluidAction.SIMULATE);
  }

  @Override
  public int fill(FluidStack resource) {
    return fluidHandler.fill(resource, IFluidHandler.FluidAction.EXECUTE);
  }

  @Override
  @Nullable
  public FluidStack drain(FluidStack resource) {
    return fluidHandler.drain(resource, IFluidHandler.FluidAction.EXECUTE);
  }

  @Override
  @Nullable
  public FluidStack getAvailableFluid() {
    return fluidHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
  }
}
