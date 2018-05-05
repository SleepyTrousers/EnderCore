package com.enderio.core.common.tweaks;

import com.enderio.core.common.config.AbstractConfigHandler.RestartReqs;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;

public abstract class Tweak {
  private final String name, comment;
  private final RestartReqs restartReq;
  
  private boolean enabled;

  public Tweak(String key, String comment) {
    this(key, comment, RestartReqs.REQUIRES_MC_RESTART);
  }
  
  public Tweak(String key, String comment, RestartReqs restartReq) {
    this.name = key;
    this.comment = comment;
    this.restartReq = restartReq;
  }

  public String getName() {
    return name;
  }

  public String getComment() {
    return comment;
  }

  public RestartReqs getRestartReq() {
    return restartReq;
  }
  
  protected void load() {}
  
  protected void unload() {}
  
  private boolean checkGameState() {
    return restartReq == RestartReqs.NONE ||
        (restartReq == RestartReqs.REQUIRES_WORLD_RESTART && FMLCommonHandler.instance().getMinecraftServerInstance() == null) ||
        !Loader.instance().hasReachedState(LoaderState.AVAILABLE);
  }

  public final void enable() {
    if (!enabled && checkGameState()) {
      load();
      enabled = true;
    }
  }
  
  public final void disable() {
    if (enabled && checkGameState()) {
      unload();
      enabled = false;
    }
  }
}
