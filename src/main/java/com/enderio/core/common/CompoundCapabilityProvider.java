package com.enderio.core.common;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public class CompoundCapabilityProvider implements ICapabilityProvider {

  private final List<ICapabilityProvider> providers = new ArrayList<ICapabilityProvider>();

  public CompoundCapabilityProvider(ICapabilityProvider... provs) {
    if (provs != null) {
      for (ICapabilityProvider p : provs) {
        if (p != null) {
          providers.add(p);
        }
      }
    }
  }

  @Nonnull
  @Override
  public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
    for (ICapabilityProvider prov : providers) {
      return prov.getCapability(cap, side);
    }
    return null;
  }

  @Nonnull
  @Override
  public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
    for (ICapabilityProvider prov : providers) {
      return prov.getCapability(cap);
    }
    return null;
  }

}
