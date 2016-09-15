package com.enderio.core.common;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class CompoundCapabilityProvider implements ICapabilityProvider {

  private final List<ICapabilityProvider> providers = new ArrayList<ICapabilityProvider>();
  
  public CompoundCapabilityProvider(ICapabilityProvider... provs) {
    if(provs != null) {
      for(ICapabilityProvider p : provs) {
        if(p != null) {
          providers.add(p);
        }
      }
    }
  }
  
  @Override
  public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
    for(ICapabilityProvider prov : providers) {
      if(prov.hasCapability(capability, facing)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
    for(ICapabilityProvider prov : providers) {
      T res = prov.getCapability(capability, facing);
      if(res != null) {
        return res;
      }
    }
    return null;
  }
}
