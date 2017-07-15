package com.enderio.core.common.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.annotation.Nonnull;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * Created by CrazyPants on 27/02/14.
 */
public class NetworkUtil {

  public static @Nonnull NBTTagCompound readNBTTagCompound(ByteBuf dataIn) {
    try {
      short size = dataIn.readShort();
      if (size < 0) {
        return new NBTTagCompound();
      } else {
        byte[] buffer = new byte[size];
        dataIn.readBytes(buffer);
        return CompressedStreamTools.readCompressed(new ByteArrayInputStream(buffer));
      }
    } catch (IOException e) {
      FMLCommonHandler.instance().raiseException(e, "Custom Packet", true);
      return new NBTTagCompound();
    }
  }

  public static void writeNBTTagCompound(NBTTagCompound compound, ByteBuf dataout) {
    try {
      if (compound == null) {
        dataout.writeShort(-1);
      } else {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CompressedStreamTools.writeCompressed(compound, baos);
        byte[] buf = baos.toByteArray();
        dataout.writeShort((short) buf.length);
        dataout.writeBytes(buf);
      }
    } catch (IOException e) {
      FMLCommonHandler.instance().raiseException(e, "PacketUtil.readTileEntityPacket.writeNBTTagCompound", true);
    }
  }

  public static byte[] readByteArray(ByteBuf buf) {
    int size = buf.readMedium();
    byte[] res = new byte[size];
    buf.readBytes(res);
    return res;
  }

  public static void writeByteArray(ByteBuf buf, byte[] arr) {
    buf.writeMedium(arr.length);
    buf.writeBytes(arr);
  }
}
