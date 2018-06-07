package com.enderio.core.client;

import javax.annotation.Nonnull;

import com.enderio.core.client.render.IconUtil;
import com.enderio.core.common.CommonProxy;
import com.enderio.core.common.util.Scheduler;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

  @Override
  public @Nonnull Scheduler getScheduler() {
    if (scheduler != null) {
      return scheduler;
    }
    return scheduler = new Scheduler(false);
  }

  @Override
  public @Nonnull World getClientWorld() {
    return Minecraft.getMinecraft().world;
  }

  @Override
  public void throwModCompatibilityError(@Nonnull String... msgs) {
    EnderCoreModConflictException ex = new EnderCoreModConflictException(msgs);
//    ReflectionHelper.setPrivateValue(FMLClientHandler.class, FMLClientHandler.instance(), ex, "customError");
    throw ex;
  }

  @Override
  public void onPreInit(@Nonnull FMLPreInitializationEvent event) {
    IconUtil.instance.init();
  }

}
