package com.enderio.core.common.network;

import com.enderio.core.EnderCore;
import com.enderio.core.common.config.PacketConfigSync;
import com.enderio.core.common.util.ChatUtil.PacketNoSpamChat;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

public class EnderPacketHandler {

  public static final ThreadedNetworkWrapper INSTANCE = new ThreadedNetworkWrapper(EnderCore.NAME);

  public static void init() {
    INSTANCE.registerMessage(PacketConfigSync.Handler.class, PacketConfigSync.class, 0, Side.CLIENT);
    INSTANCE.registerMessage(PacketProgress.Handler.class, PacketProgress.class, 1, Side.CLIENT);
    INSTANCE.registerMessage(PacketNoSpamChat.Handler.class, PacketNoSpamChat.class, 2, Side.CLIENT);
    INSTANCE.registerMessage(PacketGhostSlot.Handler.class, PacketGhostSlot.class, 3, Side.SERVER);
  }

  public static void sendToAllAround(IMessage message, TileEntity te) {
    INSTANCE.sendToAllAround(message, te);
  }

  public static void sendTo(IMessage message, EntityPlayerMP player) {
    INSTANCE.sendTo(message, player);
  }

  public static void sendToServer(IMessage message) {
    INSTANCE.sendToServer(message);
  }
}
