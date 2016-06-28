package com.enderio.core.common.fluid;

import javax.annotation.Nullable;

import net.minecraftforge.fluids.FluidStack;

public interface IFluidWrapper {

  int offer(FluidStack resource);

  int fill(FluidStack resource);

  @Nullable
  FluidStack drain(FluidStack resource);

  @Nullable
  FluidStack contents();

}
