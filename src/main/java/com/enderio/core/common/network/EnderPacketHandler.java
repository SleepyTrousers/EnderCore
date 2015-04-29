package com.enderio.core.common.network;

import com.enderio.core.EnderCore;
import com.enderio.core.common.config.PacketConfigSync;

import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class EnderPacketHandler
{
    public static final SimpleNetworkWrapper INSTANCE = new SimpleNetworkWrapper(EnderCore.NAME);

    static
    {
        INSTANCE.registerMessage(PacketConfigSync.class, PacketConfigSync.class, 0, Side.CLIENT);
    }
}
