package com.enderio.core.common;

import javax.annotation.Nonnull;

import com.enderio.core.common.util.Scheduler;

import net.minecraft.world.World;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CommonProxy {
  protected Scheduler scheduler;

  /**
   * Returns a scheduler for the current side
   * <p>
   * For internal use only, please call {@link Scheduler#instance()} to obtain an {@link Scheduler} instance.
   */
  public @Nonnull Scheduler getScheduler() {
    if (scheduler != null) {
      return scheduler;
    }
    return scheduler = new Scheduler(true);
  }

  public World getClientWorld() {
    return null;
  }

  public void throwModCompatibilityError(@Nonnull String... msgs) {
    StringBuilder sb = new StringBuilder();
    for (String msg : msgs) {
      if (sb.length() > 0) {
        sb.append("\n");
      }
      sb.append(msg);
    }
    throw new RuntimeException(sb.toString());
  }

  public void setup(@Nonnull FMLCommonSetupEvent event) {
  }

}
