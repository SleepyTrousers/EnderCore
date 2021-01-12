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
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;

public class SmartTank extends FluidTank {

    // Note: NBT-safe as long as the restriction isn't using NBT

    protected @Nullable
    Fluid fluidRestriction;

    public SmartTank(@Nonnull FluidStack fluidStack, int capacity) {
        super(capacity, stack -> false); // Yucky workaround
        this.fluid = fluidStack;
        this.validator = this::stackValidator;
        this.fluidRestriction = fluidStack.getFluid();
    }

    public SmartTank(int capacity) {
        super(capacity, stack -> false); // Yucky workaround
        this.validator = this::stackValidator;
    }

    public SmartTank(@Nullable Fluid restriction, int capacity) {
        super(capacity, stack -> false);  // Yucky workaround
        this.validator = this::stackValidator;
        this.fluidRestriction = restriction;
    }

    private boolean stackValidator(FluidStack stack) {
        return (fluidRestriction == null || (stack != null && stack.getFluid() != null && FluidUtil.areFluidsTheSame(fluidRestriction, stack.getFluid())));
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
    public boolean isFluidValid(FluidStack stack) {
        return (fluidRestriction == null || stack != null && stack.getFluid() != null && FluidUtil.areFluidsTheSame(fluidRestriction, stack.getFluid()));
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
        return drained;
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

    @Override
    protected void onContentsChanged() {
        super.onContentsChanged();
//        if (tile instanceof ITankAccess) {
//            ((ITankAccess) tile).setTanksDirty();
//        } else if (tile != null) {
//            tile.markDirty();
//        }
    }
}
