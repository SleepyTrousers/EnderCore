package com.enderio.core.common.network;

import com.enderio.core.common.util.Log;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created by CrazyPants on 27/02/14.
 */
public class MessageTileNBT implements IMessage, IMessageHandler<MessageTileNBT, IMessage> {

  TileEntity te;

  long pos;
  NBTTagCompound tags;

  boolean renderOnUpdate = false;

  public MessageTileNBT() {

  }

  public MessageTileNBT(TileEntity te) {
    this.te = te;
    pos = te.getPos().toLong();
    tags = new NBTTagCompound();
    te.writeToNBT(tags);
  }

  @Override
  public void toBytes(ByteBuf buffer) {
    buffer.writeLong(pos);
    NetworkUtil.writeNBTTagCompound(tags, buffer);
  }

  @Override
  public void fromBytes(ByteBuf dis) {
    pos = dis.readLong();
    tags = NetworkUtil.readNBTTagCompound(dis);
  }

  public BlockPos getPos() {
    return BlockPos.fromLong(pos);
  }

  @Override
  public IMessage onMessage(MessageTileNBT msg, MessageContext ctx) {
    te = handle(ctx.getServerHandler().playerEntity.worldObj);
    if (te != null && renderOnUpdate) {
      te.getWorld().markBlockForUpdate(getPos());
    }
    return null;
  }

  private TileEntity handle(World world) {
    if (world == null) {
      Log.warn("PacketUtil.handleTileEntityPacket: TileEntity null world processing tile entity packet.");
      return null;
    }
    TileEntity te = world.getTileEntity(getPos());
    if (te == null) {
      Log.warn("PacketUtil.handleTileEntityPacket: TileEntity null when processing tile entity packet.");
      return null;
    }
    te.readFromNBT(tags);
    return te;
  }
}
