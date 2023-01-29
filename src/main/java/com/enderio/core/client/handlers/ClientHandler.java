package com.enderio.core.client.handlers;

import org.lwjgl.input.Keyboard;

import com.enderio.core.common.Handlers.Handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

@Handler
public class ClientHandler {

    private static int ticksElapsed;
    private static boolean shiftDown = false;

    public static int getTicksElapsed() {
        return ticksElapsed;
    }

    public static boolean isShiftDown() {
        return shiftDown;
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if (event.phase == Phase.START) {
            shiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        }
        if (event.phase == Phase.END) {
            ticksElapsed++;
        }
    }
}
