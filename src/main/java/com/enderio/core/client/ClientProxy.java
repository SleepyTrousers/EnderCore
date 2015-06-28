package com.enderio.core.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

import com.enderio.core.common.CommonProxy;
import com.enderio.core.common.util.Scheduler;

public class ClientProxy extends CommonProxy {
  private static final Scheduler scheduler = new Scheduler();

  @Override
  public Scheduler getScheduler() {
    return scheduler;
  }

  @Override
  public World getClientWorld() {
    return Minecraft.getMinecraft().theWorld;
  }

  @Override
  public void throwModCompatibilityError(String... msgs) {
    throw new EnderCoreModConflictException(msgs);
  }


}
