package com.enderio.core.common;

import net.minecraft.world.World;

import com.enderio.core.common.util.Scheduler;

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
}
