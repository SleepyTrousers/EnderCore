package com.enderio.core.common;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class MappedCapabilityProvider implements ICapabilityProvider {

  private final Map<Capability<?>, Object> providers = new HashMap<>();

  public MappedCapabilityProvider() {
  }

  public @Nonnull <T> MappedCapabilityProvider add(@Nullable Capability<T> capability, @Nonnull T cap) {
    providers.put(capability, cap);
    return this;
  }

  @Override
  public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
    return providers.containsKey(capability);
  }

  @SuppressWarnings("unchecked")
  @Override
  public @Nullable <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
    return (T) providers.get(capability);
  }

}
