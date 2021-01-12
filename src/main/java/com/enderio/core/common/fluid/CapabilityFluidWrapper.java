package com.enderio.core.common.fluid;

import javax.annotation.Nullable;

import net.minecraft.util.Tuple;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

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

  @Override
  public List<Tuple<FluidStack, Integer>> getFluidInTanks() {
    List<Tuple<FluidStack, Integer>> result = new ArrayList<>();
    for (int i = 0; i < fluidHandler.getTanks(); i++) {
      result.add(new Tuple<>(fluidHandler.getFluidInTank(i), fluidHandler.getTankCapacity(i)));

    }
    return result;
  }
}
