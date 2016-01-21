package com.enderio.core.common.network;

import com.google.common.reflect.TypeToken;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public abstract class MessageTileEntity<T extends TileEntity> implements IMessage {

  protected int x;
  protected int y;
  protected int z;

  protected MessageTileEntity() {
  }

  protected MessageTileEntity(T tile) {
    x = tile.getPos().getX();
    y = tile.getPos().getY();
    z = tile.getPos().getZ();
  }

  public void toBytes(ByteBuf buf) {
    buf.writeInt(x);
    buf.writeInt(y);
    buf.writeInt(z);
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    x = buf.readInt();
    y = buf.readInt();
    z = buf.readInt();
  }

  @SuppressWarnings("unchecked")
  protected T getTileEntity(World worldObj) {
    if (worldObj == null) {
      return null;
    }
    TileEntity te = worldObj.getTileEntity(new BlockPos(x, y, z));
    if (te == null) {
      return null;
    }
    TypeToken<?> teType = TypeToken.of(getClass()).resolveType(MessageTileEntity.class.getTypeParameters()[0]);
    if (teType.isAssignableFrom(te.getClass())) {
      return (T) te;
    }
    return null;
  }

  protected World getWorld(MessageContext ctx) {
    return ctx.getServerHandler().playerEntity.worldObj;
  }
}
