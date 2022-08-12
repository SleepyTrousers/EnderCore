package com.enderio.core.common.config;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.google.common.base.Throwables;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketConfigSync implements IMessage {

  public PacketConfigSync() {
  }

  private Map<String, Object> configValues;
  private String modid;

  public PacketConfigSync(ConfigProcessor toSync) {
    this.configValues = toSync.configValues;
    this.modid = toSync.modid;
  }

  @Override
  public void toBytes(ByteBuf buf) {
    ByteArrayOutputStream obj = new ByteArrayOutputStream();

    try {
      GZIPOutputStream gzip = new GZIPOutputStream(obj);
      ObjectOutputStream objStream = new ObjectOutputStream(gzip);
      objStream.writeObject(configValues);
      objStream.close();
    } catch (IOException e) {
      Throwables.propagate(e);
    }

    buf.writeShort(obj.size());
    buf.writeBytes(obj.toByteArray());

    ByteBufUtils.writeUTF8String(buf, modid);
  }

  /**
   * Pa4ok
   * you can trick forge network system & send client packet to server
   * and then use different hacks with serialization of untrusted data
   */
  @SuppressWarnings("unchecked")
  @Override
  public void fromBytes(ByteBuf buf) {
    if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
      return;
    }

    short len = buf.readShort();
    byte[] compressedBody = new byte[len];

    for (short i = 0; i < len; i++)
      compressedBody[i] = buf.readByte();

    try {
      ObjectInputStream obj = new ObjectInputStream(new GZIPInputStream(new ByteArrayInputStream(compressedBody)));
      configValues = (Map<String, Object>) obj.readObject();
      obj.close();
    } catch (Exception e) {
      Throwables.propagate(e);
    }

    modid = ByteBufUtils.readUTF8String(buf);
  }

  public static class Handler implements IMessageHandler<PacketConfigSync, PacketConfigSync> {
    @Override
    public PacketConfigSync onMessage(PacketConfigSync message, MessageContext ctx) {
      ConfigProcessor processor = ConfigProcessor.processorMap.get(message.modid);
      if (processor != null) {
        processor.syncTo(message.configValues);
      }
      return null;
    }
  }
}
