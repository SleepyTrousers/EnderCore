package com.enderio.core.common.fluid;

import javax.annotation.Nullable;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

public class LegacyFluidWrapper implements IFluidWrapper {

  private final IFluidHandler fluidHandler;
  private final EnumFacing side;

  public LegacyFluidWrapper(IFluidHandler fluidHandler, EnumFacing side) {
    this.fluidHandler = fluidHandler;
    this.side = side;
  }

  @Override
  public int offer(FluidStack resource) {
    return fluidHandler.fill(side, resource, false);
  }

  @Override
  public int fill(FluidStack resource) {
    return fluidHandler.fill(side, resource, true);
  }

  @Override
  @Nullable
  public FluidStack drain(FluidStack resource) {
    return fluidHandler.drain(side, resource, true);
  }

  @Override
  @Nullable
  public FluidStack getAvailableFluid() {    
    return fluidHandler.drain(side, Integer.MAX_VALUE, false);
  }

}
