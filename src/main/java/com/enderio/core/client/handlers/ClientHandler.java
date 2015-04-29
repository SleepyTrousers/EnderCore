package com.enderio.core.client.handlers;

import com.enderio.core.common.Handlers.Handler;
import com.enderio.core.common.Handlers.Handler.HandlerType;

import lombok.Getter;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

@Handler(HandlerType.FML)
public class ClientHandler
{
    @Getter
    private static int ticksElapsed;

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event)
    {
        if (event.phase == Phase.END)
        {
            ticksElapsed++;
        }
    }
}
