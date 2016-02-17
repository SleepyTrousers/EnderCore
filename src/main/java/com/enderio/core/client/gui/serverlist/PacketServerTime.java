package com.enderio.core.client.gui.serverlist;

import java.lang.reflect.Field;

import net.minecraft.server.network.NetHandlerStatusServer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

//TODO: 1.8 On Hold
public class PacketServerTime {//extends net.minecraft.network.Packet{

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

//  @Override
//  public void readPacketData(PacketBuffer buf) throws IOException {
//    this.time = buf.readLong();
//    this.serverIP = ByteBufUtils.readUTF8String(buf);
//  }
//
//  @Override
//  public void writePacketData(PacketBuffer buf) throws IOException {
//    buf.writeLong(getTime());
//    ByteBufUtils.writeUTF8String(buf, serverIP);
//  }

  private static final Field _networkManager = ReflectionHelper.findField(NetHandlerStatusServer.class, "field_147313_b", "networkManager");

//  @Override
//  public void processPacket(INetHandler net) {
//    if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
//      try {
//        MinecraftServer serv = MinecraftServer.getServer();
//        NetworkManager resp = (NetworkManager) _networkManager.get(net);
//        resp.scheduleOutboundPacket(new PacketServerTime(serv.getEntityWorld().getWorldTime(), serverIP));
//      } catch (Exception e) {
//        Throwables.propagate(e);
//      }
//    } else {
//      ((NetHandlerTime) net).handleTime(this);
//    }
//  }

  public long getTime() {
    return time;
  }
}
