package com.enderio.core.common.fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.api.common.util.ITankAccess;
import com.enderio.core.common.util.FluidUtil;
import com.enderio.core.common.util.NullHelper;
import com.google.common.base.Strings;

import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

// Custom implementation based on FluidTank, but with a Fluid type restriction instead of a predicate so it can be saved in NBT.
public class SmartTank implements IFluidHandler, IFluidTank {

    // Note: NBT-safe as long as the restriction isn't using NBT

    protected @Nullable
    Fluid fluidRestriction;

    @Nonnull
    protected FluidStack fluid = FluidStack.EMPTY;
    protected int capacity;

    protected boolean canFill = true;
    protected boolean canDrain = true;

    public SmartTank(@Nullable FluidStack fluidStack, int capacity) {
        this.fluid = fluidStack;
        this.capacity = capacity;
        if (fluidStack != null) {
            fluidRestriction = fluidStack.getFluid();
        } else {
            fluidRestriction = null;
        }
    }

    public SmartTank(int capacity) {
        this.capacity = capacity;
    }

    public SmartTank(@Nullable Fluid restriction, int capacity) {
        this.capacity = capacity;
        this.fluidRestriction = restriction;
    }

    public void setFluidRestriction(@Nullable Fluid fluidRestriction) {
        this.fluidRestriction = fluidRestriction;
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

    public boolean hasFluid(@Nullable Fluid candidate) {
        final FluidStack fluid2 = fluid;
        return !(fluid2 == null || candidate == null || fluid2.getAmount() <= 0 || fluid2.getFluid() != candidate);
    }

    @Override
    public int getFluidAmount() {
        return fluid.getAmount();
    }

    @Override
    public int getCapacity() {
        return this.capacity;
    }

    @Override
    public boolean isFluidValid(FluidStack stack) {
        return (fluidRestriction == null || stack != null && stack.getFluid() != null && FluidUtil.areFluidsTheSame(fluidRestriction, stack.getFluid()));
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !isFluidValid(resource)) {
            return 0;
        }
        if (action.simulate()) {
            if (fluid.isEmpty()) {
                return Math.min(capacity, resource.getAmount());
            }
            if (!fluid.isFluidEqual(resource)) {
                return 0;
            }
            return Math.min(capacity - fluid.getAmount(), resource.getAmount());
        }
        if (fluid.isEmpty()) {
            fluid = new FluidStack(resource, Math.min(capacity, resource.getAmount()));
            onContentsChanged();
            return fluid.getAmount();
        }
        if (!fluid.isFluidEqual(resource)) {
            return 0;
        }
        int filled = capacity - fluid.getAmount();

        if (resource.getAmount() < filled) {
            fluid.grow(resource.getAmount());
            filled = resource.getAmount();
        } else {
            fluid.setAmount(capacity);
        }
        if (filled > 0)
            onContentsChanged();
        return filled;
    }

    /**
     * Checks if the given fluid can actually be removed from this tank
     * <p>
     * Used by: internal
     */
    public boolean canDrain(@Nullable FluidStack fluidStack) {
        final FluidStack fluid2 = this.fluid;
        if (fluid2 == FluidStack.EMPTY || fluidStack == null || !isFluidValid(fluidStack)) {
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
        if (!isFluidValid(resource) || resource == null) {
            return false;
        } else if (fluid != FluidStack.EMPTY) {
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
        if (fl == null || !isFluidValid(new FluidStack(fl, 1))) {
            return false;
        } else if (fluid != FluidStack.EMPTY) {
            return FluidUtil.areFluidsTheSame(fluid.getFluid(), fl);
        } else {
            return true;
        }
    }

    public void setFluidAmount(int amount) {
        if (amount > 0) {
            if (fluid != FluidStack.EMPTY) {
                fluid.setAmount(Math.min(capacity, amount));
            } else if (fluidRestriction != null) {
                this.fluid = new FluidStack(fluidRestriction, Math.min(capacity, amount));
            } else {
                throw new RuntimeException("Cannot set fluid amount of an empty tank");
            }
        } else {
            this.fluid = FluidStack.EMPTY;
        }
        onContentsChanged();
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !resource.isFluidEqual(fluid)) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        int drained = maxDrain;
        if (fluid.getAmount() < drained) {
            drained = fluid.getAmount();
        }
        FluidStack stack = new FluidStack(fluid, drained);
        if (action.execute() && drained > 0) {
            fluid.shrink(drained);
            onContentsChanged();
        }
        return stack;
    }

    @Nonnull
    @Override
    public FluidStack getFluid() {
        if (fluid != FluidStack.EMPTY) {
            return fluid;
        } else if (fluidRestriction != null) {
            return new FluidStack(fluidRestriction, 0);
        } else {
            return FluidStack.EMPTY;
        }
    }

    public @Nonnull
    FluidStack getFluidNN() {
        return NullHelper.notnull(getFluid(), "Internal Logic Error. Non-Empty tank has no fluid.");
    }

    public int getAvailableSpace() {
        return getCapacity() - getFluidAmount();
    }

    public void addFluidAmount(int amount) {
        setFluidAmount(getFluidAmount() + amount);
//    if (tile != null) {
//      FluidEvent.fireEvent(new FluidEvent.FluidFillingEvent(fluid, tile.getWorld(), tile.getPos(), this, amount));
//    }
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
//    if (tile != null) {
//      FluidEvent.fireEvent(new FluidEvent.FluidDrainingEvent(fluid, tile.getWorld(), tile.getPos(), this, drained));
//    }
        return drained;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
        if (getFluidAmount() > capacity) {
            setFluidAmount(capacity);
        }
    }

    public void writeCommon(@Nonnull String name, @Nonnull CompoundNBT nbtRoot) {
        CompoundNBT tankRoot = new CompoundNBT();
        fluid.writeToNBT(nbtRoot);
        if (fluidRestriction != null) {
            tankRoot.putString("FluidRestriction", NullHelper.notnullF(fluidRestriction.getRegistryName().toString(), "encountered fluid with null name"));
        }
        tankRoot.putInt("Capacity", capacity);
        nbtRoot.put(name, tankRoot);
    }

    public void readCommon(@Nonnull String name, @Nonnull CompoundNBT nbtRoot) {
        if (nbtRoot.contains(name)) {
            CompoundNBT tankRoot = (CompoundNBT) nbtRoot.get(name);
            fluid = FluidStack.loadFluidStackFromNBT(nbtRoot);
            if (tankRoot.contains("FluidRestriction")) {
                String fluidName = tankRoot.getString("FluidRestriction");
                if (!Strings.isNullOrEmpty(fluidName)) {
                    fluidRestriction = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidName));
                }
            }
            if (tankRoot.contains("Capacity")) {
                capacity = tankRoot.getInt("Capacity");
            }
        } else {
            this.fluid = FluidStack.EMPTY;
            // not reseting 'restriction' here on purpose---it would destroy the one that was set at tank creation
        }
    }

    public static SmartTank createFromNBT(@Nonnull String name, @Nonnull CompoundNBT nbtRoot) {
        SmartTank result = new SmartTank(0);
        result.readCommon(name, nbtRoot);
        if (result.getFluidAmount() > result.getCapacity()) {
            result.setCapacity(result.getFluidAmount());
        }
        return result;
    }

    protected void onContentsChanged() {
//        if (tile instanceof ITankAccess) {
//            ((ITankAccess) tile).setTanksDirty();
//        } else if (tile != null) {
//            tile.markDirty();
//        }
    }
}
