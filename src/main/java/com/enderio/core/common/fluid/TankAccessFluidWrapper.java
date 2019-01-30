package com.enderio.core.common.fluid;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.api.common.util.ITankAccess;
import com.enderio.core.common.util.NNList;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class TankAccessFluidWrapper implements IFluidWrapper {

  private final ITankAccess tankAccess;

  public TankAccessFluidWrapper(ITankAccess tankAccess) {
    this.tankAccess = tankAccess;
  }

  @Override
  public int offer(FluidStack resource) {
    FluidTank inputTank = tankAccess.getInputTank(resource);
    if (inputTank != null) {
      return inputTank.fill(resource, false);
    }
    return 0;
  }

  @Override
  public int fill(FluidStack resource) {
    FluidTank inputTank = tankAccess.getInputTank(resource);
    if (inputTank != null) {
      tankAccess.setTanksDirty();
      return inputTank.fill(resource, true);
    }
    return 0;
  }

  @Override
  @Nullable
  public FluidStack drain(FluidStack resource) {
    FluidTank[] outputTanks = tankAccess.getOutputTanks();
    if (outputTanks.length >= 1 && outputTanks[0] != null) {
      tankAccess.setTanksDirty();
      return outputTanks[0].drain(resource, true);
    }
    return null;
  }

  @Override
  @Nullable
  public FluidStack getAvailableFluid() {
    FluidTank[] outputTanks = tankAccess.getOutputTanks();
    if (outputTanks.length >= 1 && outputTanks[0] != null) {
      return outputTanks[0].getFluid();
    }
    return null;
  }

  @Override
  @Nonnull
  public List<ITankInfoWrapper> getTankInfoWrappers() {
    return NNList.emptyList();
  }

}
