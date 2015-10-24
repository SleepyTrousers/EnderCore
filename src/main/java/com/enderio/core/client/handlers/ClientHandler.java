package com.enderio.core.client.handlers;

import com.enderio.core.common.Handlers.Handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

@Handler
public class ClientHandler {

  private static int ticksElapsed;
  
  public static int getTicksElapsed() {
    return ticksElapsed;
  }

  @SubscribeEvent
  public void onClientTick(ClientTickEvent event) {
    if (event.phase == Phase.END) {
      ticksElapsed++;
    }
  }
}
