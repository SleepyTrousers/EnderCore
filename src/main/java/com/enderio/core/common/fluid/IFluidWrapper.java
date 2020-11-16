package com.enderio.core.common.fluid;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraftforge.fluids.FluidStack;

public interface IFluidWrapper {

  int offer(FluidStack resource);

  int fill(FluidStack resource);

  @Nullable
  FluidStack drain(FluidStack resource);

  @Nullable
  FluidStack getAvailableFluid();

  // TODO: What else... I'm not sure what this interface's purpose is just yet.
}
