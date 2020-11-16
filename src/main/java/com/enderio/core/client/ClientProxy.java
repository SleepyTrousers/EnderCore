package com.enderio.core.client;

import javax.annotation.Nonnull;

import com.enderio.core.client.render.IconUtil;
import com.enderio.core.common.CommonProxy;
import com.enderio.core.common.util.Scheduler;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

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
    return Minecraft.getInstance().world;
  }

  @Override
  public void throwModCompatibilityError(@Nonnull String... msgs) {
    AbstractLoadingException ex = new AbstractLoadingException(msgs);
//    ReflectionHelper.setPrivateValue(FMLClientHandler.class, FMLClientHandler.instance(), ex, "customError");
    throw ex;
  }

  @Override
  public void setup(@Nonnull FMLCommonSetupEvent event) {
    IconUtil.instance.init();
  }

}
