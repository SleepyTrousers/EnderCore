package com.enderio.core.common.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

import com.enderio.core.EnderCore;
import com.enderio.core.common.config.PacketConfigSync;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class EnderPacketHandler {
  
  public static final SimpleNetworkWrapper INSTANCE = new SimpleNetworkWrapper(EnderCore.NAME);

  public static void init() {
    INSTANCE.registerMessage(PacketConfigSync.class, PacketConfigSync.class, 0, Side.CLIENT);
    INSTANCE.registerMessage(PacketProgress.class, PacketProgress.class, 1, Side.CLIENT);
  }

  public static void sendToAllAround(IMessage message, TileEntity te, int range) {
    INSTANCE.sendToAllAround(message, new TargetPoint(te.getWorldObj().provider.dimensionId, te.xCoord, te.yCoord, te.zCoord, range));
  }

  public static void sendToAllAround(IMessage message, TileEntity te) {
    sendToAllAround(message, te, 64);
  }

  public static void sendTo(IMessage message, EntityPlayerMP player) {
    INSTANCE.sendTo(message, player);
  }
}
