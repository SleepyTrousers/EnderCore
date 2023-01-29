package com.enderio.core.common.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

import com.enderio.core.EnderCore;
import com.enderio.core.common.config.PacketConfigSync;
import com.enderio.core.common.util.ChatUtil.PacketNoSpamChat;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class EnderPacketHandler {

    public static final SimpleNetworkWrapper INSTANCE = new SimpleNetworkWrapper(EnderCore.NAME);

    public static void init() {
        INSTANCE.registerMessage(PacketConfigSync.Handler.class, PacketConfigSync.class, 0, Side.CLIENT);
        INSTANCE.registerMessage(PacketProgress.Handler.class, PacketProgress.class, 1, Side.CLIENT);
        INSTANCE.registerMessage(PacketNoSpamChat.Handler.class, PacketNoSpamChat.class, 2, Side.CLIENT);
        INSTANCE.registerMessage(PacketGhostSlot.Handler.class, PacketGhostSlot.class, 3, Side.SERVER);
    }

    public static void sendToAllAround(IMessage message, TileEntity te, int range) {
        INSTANCE.sendToAllAround(
                message,
                new TargetPoint(te.getWorldObj().provider.dimensionId, te.xCoord, te.yCoord, te.zCoord, range));
    }

    public static void sendToAllAround(IMessage message, TileEntity te) {
        sendToAllAround(message, te, 64);
    }

    public static void sendTo(IMessage message, EntityPlayerMP player) {
        INSTANCE.sendTo(message, player);
    }

    public static void sendToServer(IMessage message) {
        INSTANCE.sendToServer(message);
    }
}
