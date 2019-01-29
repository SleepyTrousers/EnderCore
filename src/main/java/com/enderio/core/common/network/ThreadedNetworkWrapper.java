package com.enderio.core.common.network;

import java.util.IdentityHashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.enderio.core.common.util.Log;
import com.google.common.base.Throwables;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;

public class ThreadedNetworkWrapper {

  private static final @Nonnull Map<Class<? extends IMessage>, SimpleNetworkWrapper> typesClient = new IdentityHashMap<>();
  private static final @Nonnull Map<Class<? extends IMessage>, SimpleNetworkWrapper> typesServer = new IdentityHashMap<>();

  static synchronized void registerChannelMapping(Class<? extends IMessage> requestMessageType, @Nonnull SimpleNetworkWrapper parent, Side side) {
    (side == Side.CLIENT ? typesClient : typesServer).put(requestMessageType, parent);
  }

  private static synchronized SimpleNetworkWrapper getServerParent(IMessage message) {
    final SimpleNetworkWrapper parent = typesClient.get(message.getClass());
    if (parent == null) {
      throw new RuntimeException("Trying to send unregistered network packet: " + message.getClass());
    }
    return parent;
  }

  private static synchronized SimpleNetworkWrapper getClientParent(IMessage message) {
    final SimpleNetworkWrapper parent = typesServer.get(message.getClass());
    if (parent == null) {
      throw new RuntimeException("Trying to send unregistered network packet: " + message.getClass());
    }
    return parent;
  }

  private final @Nonnull SimpleNetworkWrapper parent;

  public ThreadedNetworkWrapper(String channelName) {
    if (channelName.length() > 20) {
      throw new RuntimeException("Channel name '" + channelName + "' is too long for Forge. Maximum length supported is 20 characters.");
    }
    parent = new SimpleNetworkWrapper(channelName);
  }

  private final class NullHandler<REQ extends IMessage, REPLY extends IMessage> implements IMessageHandler<REQ, REPLY> {

    @Override
    public REPLY onMessage(REQ message, MessageContext ctx) {
      return null;
    }

  }

  private final class Wrapper<REQ extends IMessage, REPLY extends IMessage> implements IMessageHandler<REQ, REPLY> {

    final IMessageHandler<REQ, REPLY> wrapped;

    public Wrapper(IMessageHandler<REQ, REPLY> wrapped) {
      this.wrapped = wrapped;
    }

    @Override
    public REPLY onMessage(final REQ message, final MessageContext ctx) {
      final IThreadListener target = ctx.side == Side.CLIENT ? Minecraft.getMinecraft() : FMLCommonHandler.instance().getMinecraftServerInstance();
      if (target != null) {
        target.addScheduledTask(new Runner(message, ctx));
      }
      return null;
    }

    @Override
    public String toString() {
      return wrapped.toString();
    }

    @Override
    public int hashCode() {
      return wrapped.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Wrapper) {
        return this.wrapped.equals(((Wrapper<?, ?>) obj).wrapped);
      } else {
        return this.wrapped.equals(obj);
      }
    }

    private final class Runner implements Runnable {
      private final REQ message;
      private final MessageContext ctx;

      public Runner(final REQ message, final MessageContext ctx) {
        this.message = message;
        this.ctx = ctx;
      }

      @Override
      public void run() {
        final REPLY reply = wrapped.onMessage(message, ctx);
        if (reply != null) {
          if (ctx.side == Side.CLIENT) {
            sendToServer(reply);
          } else {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            sendTo(reply, player);
          }
        }
      }

    }

  }

  public <REQ extends IMessage, REPLY extends IMessage> void registerMessage(Class<? extends IMessageHandler<REQ, REPLY>> messageHandler,
      Class<REQ> requestMessageType, int discriminator, Side side) {
    if (messageHandler == requestMessageType) {
      // we had to many mess-ups with combined handlers using its own fields instead those of the message, so:
      throw new RuntimeException("Network packet " + requestMessageType + " needs a dedicated handler");
    }
    if (side == Side.CLIENT && FMLLaunchHandler.side() != Side.CLIENT) {
      parent.registerMessage(new NullHandler<REQ, REPLY>(), requestMessageType, discriminator, side);
      registerChannelMapping(requestMessageType, parent, side);
      return;
    }
    registerMessage(instantiate(messageHandler), requestMessageType, discriminator, side);
  }

  static <REQ extends IMessage, REPLY extends IMessage> IMessageHandler<? super REQ, ? extends REPLY> instantiate(
      Class<? extends IMessageHandler<? super REQ, ? extends REPLY>> handler) {
    try {
      return handler.newInstance();
    } catch (Throwable e) {
      Log.error("Failed to instanciate " + handler);
      throw Throwables.propagate(e);
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <REQ extends IMessage, REPLY extends IMessage> void registerMessage(IMessageHandler<? super REQ, ? extends REPLY> messageHandler,
      Class<REQ> requestMessageType, int discriminator, Side side) {
    parent.registerMessage((Wrapper<REQ, REPLY>) new Wrapper(messageHandler), requestMessageType, discriminator, side);
    registerChannelMapping(requestMessageType, parent, side);
  }

  public void sendToAll(IMessage message) {
    getServerParent(message).sendToAll(message);
  }

  public void sendTo(IMessage message, EntityPlayerMP player) {
    getServerParent(message).sendTo(message, player);
  }

  public void sendToAllAround(IMessage message, NetworkRegistry.TargetPoint point) {
    getServerParent(message).sendToAllAround(message, point);
  }

  public void sendToDimension(IMessage message, int dimensionId) {
    getServerParent(message).sendToDimension(message, dimensionId);
  }

  public void sendToServer(IMessage message) {
    getClientParent(message).sendToServer(message);
  }

  public Packet<?> getPacketFrom(IMessage message) {
    return getServerParent(message).getPacketFrom(message);
  }

  // see https://github.com/MinecraftForge/MinecraftForge/issues/3677
  public void sendToAllAround(IMessage packet, BlockPos pos, World world) {
    if (!(world instanceof WorldServer)) {
      return;
    }

    WorldServer worldServer = (WorldServer) world;
    PlayerChunkMap playerManager = worldServer.getPlayerChunkMap();

    int chunkX = pos.getX() >> 4;
    int chunkZ = pos.getZ() >> 4;

    for (Object playerObj : world.playerEntities) {
      if (playerObj instanceof EntityPlayerMP) {
        EntityPlayerMP player = (EntityPlayerMP) playerObj;

        if (playerManager.isPlayerWatchingChunk(player, chunkX, chunkZ)) {
          sendTo(packet, player);
        }
      }
    }
  }

  public void sendToAllAround(IMessage message, TileEntity te) {
    sendToAllAround(message, te.getPos(), te.getWorld());
  }

}
