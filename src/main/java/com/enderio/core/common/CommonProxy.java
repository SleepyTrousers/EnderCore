package com.enderio.core.common;

import com.enderio.core.common.util.Scheduler;

import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
  private static final Scheduler scheduler = new Scheduler();

  /**
   * Returns a scheduler for the current side
   * <p>
   * For internal use only, please call {@link Scheduler#instance()} to obtain
   * an {@link Scheduler} instance.
   */
  public Scheduler getScheduler() {
    return scheduler;
  }

  public World getClientWorld() {
    return null;
  }

  public void throwModCompatibilityError(String... msgs) {
    StringBuilder sb = new StringBuilder();
    for (String msg : msgs) {
      if (sb.length() > 0) {
        sb.append("\n");
      }
      sb.append(msg);
    }
    throw new RuntimeException(sb.toString());
  }
  
  public void onPreInit(FMLPreInitializationEvent event) {
    
  }
}
