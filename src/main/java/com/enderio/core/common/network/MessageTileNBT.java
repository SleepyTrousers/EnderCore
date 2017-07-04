package com.enderio.core.common.network;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.Log;
import com.enderio.core.common.util.NullHelper;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
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
    te.writeToNBT(tags = new NBTTagCompound());
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

  public @Nonnull BlockPos getPos() {
    return BlockPos.fromLong(pos);
  }

  @Override
  public IMessage onMessage(MessageTileNBT msg, MessageContext ctx) {
    te = handle(ctx.getServerHandler().player.world);
    if (te != null && renderOnUpdate) {
      IBlockState bs = te.getWorld().getBlockState(msg.getPos());
      te.getWorld().notifyBlockUpdate(msg.getPos(), bs, bs, 3);
    }
    return null;
  }

  private @Nullable TileEntity handle(World world) {
    if (world == null) {
      Log.warn("PacketUtil.handleTileEntityPacket: TileEntity null world processing tile entity packet.");
      return null;
    }
    TileEntity tileEntity = world.getTileEntity(getPos());
    if (tileEntity == null) {
      Log.warn("PacketUtil.handleTileEntityPacket: TileEntity null when processing tile entity packet.");
      return null;
    }
    tileEntity.readFromNBT(NullHelper.notnull(tags, "NetworkUtil.readNBTTagCompound()"));
    return tileEntity;
  }
}
