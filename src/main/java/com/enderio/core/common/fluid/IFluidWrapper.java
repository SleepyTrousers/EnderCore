package com.enderio.core.common.fluid;

import javax.annotation.Nullable;

import com.enderio.core.api.common.util.ITankAccess;
import net.minecraft.util.Tuple;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.antlr.v4.runtime.misc.Triple;

import java.util.List;
import java.util.function.Function;

public interface IFluidWrapper {
  int offer(FluidStack resource);

  int fill(FluidStack resource);

  @Nullable FluidStack drain(FluidStack resource);

  @Nullable FluidStack getAvailableFluid();

  List<Tuple<FluidStack, Integer>> getFluidInTanks();
}
