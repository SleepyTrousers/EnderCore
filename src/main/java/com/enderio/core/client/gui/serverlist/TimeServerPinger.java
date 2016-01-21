package com.enderio.core.client.gui.serverlist;

import java.io.Closeable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import com.enderio.core.EnderCore;
import com.google.common.collect.Maps;

import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.status.INetHandlerStatusClient;
import net.minecraft.network.status.server.S00PacketServerInfo;
import net.minecraft.network.status.server.S01PacketPong;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TimeServerPinger {//implements Closeable {

  //TODO: 1.8
  
//  public static class NetHandlerTime implements INetHandlerStatusClient {
//
//    private NetworkManager manager;
//    private ServerData data;
//    private boolean done;
//
//    public NetHandlerTime(NetworkManager manager, ServerData data) {
//      this.manager = manager;
//      this.data = data;
//    }
//
//    public void onDisconnect(IChatComponent p_147231_1_) {
//      if (!done) {
//        logger.error("Can\'t ping " + data.serverIP + " for time info" + ": " + p_147231_1_.getUnformattedText());
//      }
//    }
//
//    public void handleTime(PacketServerTime packet) {
//      ServerListHandler.timeCache.put(data.serverIP, packet.getTime());
//      done = true;
//      manager.closeChannel(new ChatComponentText("Finished"));
//    }
//
//    @Override
//    public void handlePong(S01PacketPong packet) {
//      // NO-OP
//    }
//
//    @Override
//    public void handleServerInfo(S00PacketServerInfo p_147397_1_) {
//      // NO-OP 
//    }
//
//    public void onConnectionStateTransition(EnumConnectionState p_147232_1_, EnumConnectionState p_147232_2_) {
//      if (p_147232_2_ != EnumConnectionState.STATUS) {
//        throw new UnsupportedOperationException("Unexpected change in protocol to " + p_147232_2_);
//      }
//    }
//
//    public void onNetworkTick() {
//    }
//  }
//
//  private static final Logger logger = EnderCore.logger;
//  private final Map<NetworkManager, ServerData> managers = Collections.synchronizedMap(Maps.<NetworkManager, ServerData> newHashMap());
//
//  public void addServer(final ServerData data) throws UnknownHostException {
//    ServerAddress serveraddress = ServerAddress.func_78860_a(data.serverIP);
//    final NetworkManager networkmanager = NetworkManager.provideLanClient(InetAddress.getByName(serveraddress.getIP()), serveraddress.getPort());
//    this.managers.put(networkmanager, data);
//
//    networkmanager.setNetHandler(new NetHandlerTime(networkmanager, data));
//
//    try {
//      networkmanager.scheduleOutboundPacket(new C00Handshake(5, serveraddress.getIP(), serveraddress.getPort(), EnumConnectionState.STATUS));
//      networkmanager.scheduleOutboundPacket(new PacketServerTime(data.serverIP));
//    } catch (Throwable throwable) {
//      logger.error(throwable);
//    }
//  }
//
//  public void update(boolean sendPacket) {
//    synchronized (this.managers) {
//      Iterator<NetworkManager> iterator = this.managers.keySet().iterator();
//
//      while (iterator.hasNext()) {
//        NetworkManager manager = iterator.next();
//
//        if (manager.isChannelOpen()) {
//          manager.processReceivedPackets();
//        } else {
//          iterator.remove();
//
//          if (manager.getExitMessage() != null) {
//            manager.getNetHandler().onDisconnect(manager.getExitMessage());
//          }
//        }
//      }
//    }
//  }
//
//  @Override
//  public void close() {
//    synchronized (this.managers) {
//      Iterator<NetworkManager> iterator = this.managers.keySet().iterator();
//
//      while (iterator.hasNext()) {
//        NetworkManager networkmanager = (NetworkManager) iterator.next();
//
//        if (networkmanager.isChannelOpen()) {
//          iterator.remove();
//          networkmanager.closeChannel(new ChatComponentText("Cancelled"));
//        }
//      }
//    }
//  }
}