package com.enderio.core.client.gui.serverlist;

import java.io.IOException;
import java.lang.reflect.Field;

import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.NetHandlerStatusServer;

import com.enderio.core.client.gui.serverlist.TimeServerPinger.NetHandlerTime;
import com.google.common.base.Throwables;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class PacketServerTime extends Packet {

  private long time;
  private String serverIP;
  
  public PacketServerTime() {
    this("");
  }

  public PacketServerTime(String serverIP) {
    this(0, serverIP);
  }

  private PacketServerTime(long time, String serverIP) {
    this.time = time;
    this.serverIP = serverIP;
  }

  @Override
  public void readPacketData(PacketBuffer buf) throws IOException {
    this.time = buf.readLong();
    this.serverIP = ByteBufUtils.readUTF8String(buf);
  }

  @Override
  public void writePacketData(PacketBuffer buf) throws IOException {
    buf.writeLong(getTime());
    ByteBufUtils.writeUTF8String(buf, serverIP);
  }

  private static final Field _networkManager = ReflectionHelper.findField(NetHandlerStatusServer.class, "field_147313_b", "networkManager");

  @Override
  public void processPacket(INetHandler net) {
    if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
      try {
        MinecraftServer serv = MinecraftServer.getServer();
        NetworkManager resp = (NetworkManager) _networkManager.get(net);
        resp.scheduleOutboundPacket(new PacketServerTime(serv.getEntityWorld().getWorldTime(), serverIP));
      } catch (Exception e) {
        Throwables.propagate(e);
      }
    } else {
      ((NetHandlerTime) net).handleTime(this);
    }
  }

  public long getTime() {
    return time;
  }
}
