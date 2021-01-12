package com.enderio.core.common.fluid;

import javax.annotation.Nullable;

import com.enderio.core.api.common.util.ITankAccess;

import com.enderio.core.common.util.NNList;
import net.minecraft.util.Tuple;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.antlr.v4.runtime.misc.Triple;

import java.util.List;
import java.util.function.Function;

public class TankAccessFluidWrapper implements IFluidWrapper {

  private final ITankAccess tankAccess;

  public TankAccessFluidWrapper(ITankAccess tankAccess) {
    this.tankAccess = tankAccess;
  }

  @Override
  public int offer(FluidStack resource) {
    FluidTank inputTank = tankAccess.getInputTank(resource);
    if (inputTank != null) {
      return inputTank.fill(resource, IFluidHandler.FluidAction.SIMULATE);
    }
    return 0;
  }

  @Override
  public int fill(FluidStack resource) {
    FluidTank inputTank = tankAccess.getInputTank(resource);
    if (inputTank != null) {
      tankAccess.setTanksDirty();
      return inputTank.fill(resource, IFluidHandler.FluidAction.EXECUTE);
    }
    return 0;
  }

  @Override
  @Nullable
  public FluidStack drain(FluidStack resource) {
    FluidTank[] outputTanks = tankAccess.getOutputTanks();
    if (outputTanks.length >= 1 && outputTanks[0] != null) {
      tankAccess.setTanksDirty();
      return outputTanks[0].drain(resource, IFluidHandler.FluidAction.EXECUTE);
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
  public List<Tuple<FluidStack, Integer>> getFluidInTanks() {
    return NNList.emptyList();
  }
}
