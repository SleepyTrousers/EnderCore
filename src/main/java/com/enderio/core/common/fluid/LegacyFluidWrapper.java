package com.enderio.core.common.fluid;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

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

  @Override
  @Nonnull
  public List<ITankInfoWrapper> getTankInfoWrappers() {
    List<ITankInfoWrapper> result = new ArrayList<ITankInfoWrapper>();
    FluidTankInfo[] tankProperties = fluidHandler.getTankInfo(side);
    if (tankProperties != null) {
      for (FluidTankInfo tankProperty : tankProperties) {
        if (tankProperty != null) {
          result.add(new InfoWrapper(tankProperty));
        }
      }
    }
    return result;
  }

  private static class InfoWrapper implements ITankInfoWrapper {

    private final FluidTankInfo prop;

    private InfoWrapper(FluidTankInfo prop) {
      this.prop = prop;
    }

    @Override
    public IFluidTankProperties getIFluidTankProperties() {
      return new IFluidTankProperties() {

        @Override
        @Nullable
        public FluidStack getContents() {
          return prop.fluid;
        }

        @Override
        public int getCapacity() {
          return prop.capacity;
        }

        @Override
        public boolean canFill() {
          return true;
        }

        @Override
        public boolean canDrain() {
          return true;
        }

        @Override
        public boolean canFillFluidType(FluidStack fluidStack) {
          return true;
        }

        @Override
        public boolean canDrainFluidType(FluidStack fluidStack) {
          return true;
        }
      };
    }

    @Override
    public FluidTankInfo getFluidTankInfo() {
      return prop;
    }

  }

}
