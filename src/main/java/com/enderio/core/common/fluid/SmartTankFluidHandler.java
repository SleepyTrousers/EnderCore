package com.enderio.core.common.fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

// TODO: When I'm not tired.

/**
 * Handles IFluidHandler, FluidTank and SmartTank
 */
public abstract class SmartTankFluidHandler {

  protected final @Nonnull IFluidHandler[] tanks;
  private final @Nonnull SideHandler[] sides = new SideHandler[Direction.values().length];
  private final @Nonnull InformationHandler nullSide = new InformationHandler();

  public SmartTankFluidHandler(IFluidHandler... tanks) {
    this.tanks = tanks != null ? tanks : new IFluidHandler[0];
  }

  public boolean has(@Nullable Direction facing) {
    return facing != null && canAccess(facing);
  }

  public IFluidHandler get(@Nullable Direction facing) {
    if (facing == null) {
      return nullSide;
    } else if (has(facing)) {
      if (sides[facing.ordinal()] == null) {
        sides[facing.ordinal()] = new SideHandler(facing);
      }
      return sides[facing.ordinal()];
    } else {
      return null;
    }
  }

  protected abstract boolean canFill(@Nonnull Direction from);

  protected abstract boolean canDrain(@Nonnull Direction from);

  protected abstract boolean canAccess(@Nonnull Direction from);

  // https://discordapp.com/channels/373534853259329536/373540201487925248/780523083458150470

  private class InformationHandler implements IFluidHandler {

    public InformationHandler() {
    }

    @Override public IFluidTankProperties[] getTankProperties() {
      if (tanks.length == 1) {
        return tanks[0].getTankProperties();
      }
      List<IFluidTankProperties> result = new ArrayList<IFluidTankProperties>();
      for (IFluidHandler smartTank : tanks) {
        IFluidTankProperties[] tankProperties = smartTank.getTankProperties();
        if (tankProperties != null) {
          for (IFluidTankProperties tankProperty : tankProperties) {
            result.add(tankProperty);
          }
        }
      }
      return result.toArray(new IFluidTankProperties[result.size()]);
    }

    @Override public int getTanks() {
      int tankCount = 0;
      for (IFluidHandler smartTank : tanks) {
        tankCount += smartTank.getTanks();
      }
      return tankCount;
    }

    @Nonnull @Override public FluidStack getFluidInTank(int tank) {
      return FluidStack.EMPTY;
    }

    @Override public int getTankCapacity(int tank) {
      return 0;
    }

    @Override public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
      return false;
    }

    @Override public int fill(FluidStack resource, FluidAction action) {
      return 0;
    }

    @Nonnull @Override public FluidStack drain(FluidStack resource, FluidAction action) {
      return FluidStack.EMPTY;
    }

    @Nonnull @Override public FluidStack drain(int maxDrain, FluidAction action) {
      return FluidStack.EMPTY;
    }

  }

  private class SideHandler extends InformationHandler {
    private final @Nonnull Direction facing;

    public SideHandler(@Nonnull Direction facing) {
      this.facing = facing;
    }

    @Override public int fill(FluidStack resource, FluidAction action) {
      if (!canFill(facing)) {
        return 0;
      }

      if (tanks.length == 1) {
        return tanks[0].fill(resource, action);
      }

      for (IFluidHandler smartTank : tanks) {
        if (smartTank instanceof SmartTank) {
          if (smartTank.fill(resource, FluidAction.SIMULATE) > 0) {
            return smartTank.fill(resource, action);
          }
        } else if (smartTank instanceof FluidTank) {
          if (((FluidTank) smartTank).isEmpty()) {
            return smartTank.fill(resource, action);
          }
        } else {
          return smartTank.fill(resource, action);
        }
      }
      return 0;
    }

    @Nonnull @Override public FluidStack drain(FluidStack resource, FluidAction action) {
      if (!canDrain(facing)) {
        return FluidStack.EMPTY;
      }

      if (tanks.length == 1) {
        return tanks[0].drain(resource, action);
      }

      for (IFluidHandler smartTank : tanks) {
        if (!(smartTank instanceof FluidTank) || smartTank.drain(resource, FluidAction.SIMULATE).getAmount() > 0) {
          return smartTank.drain(resource, action);
        }
      }
      return FluidStack.EMPTY;
    }

    @Nonnull @Override public FluidStack drain(int maxDrain, FluidAction action) {
      if (!canDrain(facing)) {
        return FluidStack.EMPTY;
      }

      if (tanks.length == 1) {
        return tanks[0].drain(maxDrain, action);
      }

      for (IFluidHandler smartTank : tanks) {
        if (!(smartTank instanceof FluidTank) || smartTank.drain(maxDrain, FluidAction.SIMULATE).getAmount() > 0) {
          return smartTank.drain(maxDrain, action);
        }
      }
      return FluidStack.EMPTY;
    }
  }

}
