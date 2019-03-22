package com.enderio.core.common.util;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.lifecycle.ModLifecycleEvent;

public enum RegisterTime {
  SETUP(FMLCommonSetupEvent.class),
  PREIMC(InterModEnqueueEvent.class),
  POSTIMC(InterModProcessEvent.class);

  private Class<? extends ModLifecycleEvent> clazz;

  private RegisterTime(Class<? extends ModLifecycleEvent> clazz) {
    this.clazz = clazz;
  }

  public static RegisterTime timeFor(ModLifecycleEvent event) {
    for (RegisterTime time : values()) {
      if (time.clazz == event.getClass()) {
        return time;
      }
    }
    return null;
  }
}
