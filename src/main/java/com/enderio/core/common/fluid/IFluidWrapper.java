package com.enderio.core.common.fluid;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public interface IFluidWrapper {

  int offer(FluidStack resource);

  int fill(FluidStack resource);

  @Nullable
  FluidStack drain(FluidStack resource);

  @Nullable
  FluidStack getAvailableFluid();

  @Nonnull
  List<ITankInfoWrapper> getTankInfoWrappers();

  public static interface ITankInfoWrapper {

    IFluidTankProperties getIFluidTankProperties();

    FluidTankInfo getFluidTankInfo();
  }
}
