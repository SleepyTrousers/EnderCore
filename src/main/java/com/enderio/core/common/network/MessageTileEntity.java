package com.enderio.core.common.network;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.google.common.reflect.TypeToken;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public abstract class MessageTileEntity<T extends TileEntity> implements IMessage {

    protected int x;
    protected int y;
    protected int z;

    protected MessageTileEntity() {}

    protected MessageTileEntity(T tile) {
        x = tile.xCoord;
        y = tile.yCoord;
        z = tile.zCoord;
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
        TileEntity te = worldObj.getTileEntity(x, y, z);
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
