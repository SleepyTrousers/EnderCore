package com.enderio.core.common.fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.api.common.util.ITankAccess;
import com.enderio.core.common.util.FluidUtil;
import com.enderio.core.common.util.NullHelper;
import com.google.common.base.Strings;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class SmartTank extends FluidTank {

  // Note: NBT-safe as long as the restriction isn't using NBT

  protected @Nullable Fluid restriction;

  public SmartTank(@Nullable FluidStack liquid, int capacity) {
    super(liquid, capacity);
    if (liquid != null) {
      restriction = liquid.getFluid();
    } else {
      restriction = null;
    }
  }

  public SmartTank(int capacity) {
    super(capacity);
  }

  public SmartTank(@Nullable Fluid restriction, int capacity) {
    super(capacity);
    this.restriction = restriction;
  }

  public void setRestriction(@Nullable Fluid restriction) {
    this.restriction = restriction;
  }

  public float getFilledRatio() {
    return (float) getFluidAmount() / getCapacity();
  }

  public boolean isFull() {
    return getFluidAmount() >= getCapacity();
  }

  public boolean isEmpty() {
    return getFluidAmount() == 0;
  }

  /**
   * Checks if the given fluid can actually be removed from this tank
   * <p>
   * Used by: te.canDrain()
   */
  public boolean canDrain(@Nullable Fluid fl) {
    final FluidStack fluid2 = fluid;
    if (fluid2 == null || fl == null || !canDrain()) {
      return false;
    }

    return FluidUtil.areFluidsTheSame(fl, fluid2.getFluid());
  }

  /**
   * Checks if the given fluid can actually be removed from this tank
   * <p>
   * Used by: internal
   */
  public boolean canDrain(@Nullable FluidStack fluidStack) {
    final FluidStack fluid2 = fluid;
    if (fluid2 == null || fluidStack == null || !canDrain()) {
      return false;
    }

    return fluidStack.isFluidEqual(fluid2);
  }

  /**
   * Checks if the given fluid can actually be added to this tank (ignoring fill level)
   * <p>
   * Used by: internal
   */
  public boolean canFill(@Nullable FluidStack resource) {
    if (!canFillFluidType(resource)) {
      return false;
    } else if (fluid != null) {
      return fluid.isFluidEqual(resource);
    } else {
      return true;
    }
  }

  /**
   * Checks if the given fluid can actually be added to this tank (ignoring fill level)
   * <p>
   * Used by: te.canFill()
   */
  public boolean canFill(@Nullable Fluid fl) {
    if (fl == null || !canFillFluidType(new FluidStack(fl, 1))) {
      return false;
    } else if (fluid != null) {
      return FluidUtil.areFluidsTheSame(fluid.getFluid(), fl);
    } else {
      return true;
    }
  }

  @Override
  public boolean canFillFluidType(@Nullable FluidStack resource) {
    return super.canFillFluidType(resource)
        && (restriction == null || (resource != null && resource.getFluid() != null && FluidUtil.areFluidsTheSame(restriction, resource.getFluid())));
  }

  public void setFluidAmount(int amount) {
    if (amount > 0) {
      if (fluid != null) {
        fluid.amount = Math.min(capacity, amount);
      } else if (restriction != null) {
        setFluid(new FluidStack(restriction, Math.min(capacity, amount)));
      } else {
        throw new RuntimeException("Cannot set fluid amount of an empty tank");
      }
    } else {
      setFluid(null);
    }
    onContentsChanged();
  }

  @Override
  public int fill(@Nullable FluidStack resource, boolean doFill) {
    if (!canFill(resource)) {
      return 0;
    }
    return fillInternal(resource, doFill);
  }

  @Override
  public FluidStack drain(@Nullable FluidStack resource, boolean doDrain) {
    return super.drain(resource, doDrain);
  }

  @Override
  public FluidStack drain(int maxDrain, boolean doDrain) {
    return super.drain(maxDrain, doDrain);
  }

  @Override
  public @Nullable FluidStack getFluid() {
    if (fluid != null) {
      return fluid;
    } else if (restriction != null) {
      return new FluidStack(restriction, 0);
    } else {
      return null;
    }
  }

  public int getAvailableSpace() {
    return getCapacity() - getFluidAmount();
  }

  public void addFluidAmount(int amount) {
    setFluidAmount(getFluidAmount() + amount);
    if (tile != null) {
      FluidEvent.fireEvent(new FluidEvent.FluidFillingEvent(fluid, tile.getWorld(), tile.getPos(), this, amount));
    }
  }

  public int removeFluidAmount(int amount) {
    int drained = 0;
    if (getFluidAmount() > amount) {
      setFluidAmount(getFluidAmount() - amount);
      drained = amount;
    } else if (!isEmpty()) {
      drained = getFluidAmount();
      setFluidAmount(0);
    } else {
      return 0;
    }
    if (tile != null) {
      FluidEvent.fireEvent(new FluidEvent.FluidDrainingEvent(fluid, tile.getWorld(), tile.getPos(), this, drained));
    }
    return drained;
  }

  @Override
  public void setCapacity(int capacity) {
    super.setCapacity(capacity);
    if (getFluidAmount() > capacity) {
      setFluidAmount(capacity);
    }
  }

  public void writeCommon(@Nonnull String name, @Nonnull NBTTagCompound nbtRoot) {
    NBTTagCompound tankRoot = new NBTTagCompound();
    writeToNBT(tankRoot);
    if (restriction != null) {
      tankRoot.setString("FluidRestriction", NullHelper.notnullM(restriction.getName(), "encountered fluid with null name"));
    }
    tankRoot.setInteger("Capacity", capacity);
    nbtRoot.setTag(name, tankRoot);
  }

  public void readCommon(@Nonnull String name, @Nonnull NBTTagCompound nbtRoot) {
    if (nbtRoot.hasKey(name)) {
      NBTTagCompound tankRoot = (NBTTagCompound) nbtRoot.getTag(name);
      readFromNBT(tankRoot);
      if (tankRoot.hasKey("FluidRestriction")) {
        String fluidName = tankRoot.getString("FluidRestriction");
        if (!Strings.isNullOrEmpty(fluidName)) {
          restriction = FluidRegistry.getFluid(fluidName);
        }
      }
      if (tankRoot.hasKey("Capacity")) {
        capacity = tankRoot.getInteger("Capacity");
      }
    } else {
      setFluid(null);
      // not reseting 'restriction' here on purpose---it would destroy the one that was set at tank creation
    }
  }

  public static SmartTank createFromNBT(@Nonnull String name, @Nonnull NBTTagCompound nbtRoot) {
    SmartTank result = new SmartTank(0);
    result.readCommon(name, nbtRoot);
    if (result.getFluidAmount() > result.getCapacity()) {
      result.setCapacity(result.getFluidAmount());
    }
    return result;
  }

  @Override
  protected void onContentsChanged() {
    super.onContentsChanged();
    if (tile instanceof ITankAccess) {
      ((ITankAccess) tile).setTanksDirty();
    } else if (tile != null) {
      tile.markDirty();
    }
  }

}
