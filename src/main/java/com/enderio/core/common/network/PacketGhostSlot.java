package com.enderio.core.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import com.enderio.core.EnderCore;
import com.enderio.core.api.common.util.IProgressTile;
import com.enderio.core.common.TileEntityEnder;
import com.enderio.core.common.network.MessageTileEntity;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketGhostSlot extends MessageTileEntity<TileEntityEnder> {

  private int slot;
  private ItemStack stack;

  public PacketGhostSlot() {
  }

  private PacketGhostSlot(TileEntityEnder tile) {
    super(tile);
  }

  public static PacketGhostSlot setGhostSlotContents(TileEntityEnder te, int slot, ItemStack stack) {
    PacketGhostSlot msg = new PacketGhostSlot(te);
    msg.slot = slot;
    msg.stack = stack;
    return msg;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    super.fromBytes(buf);
    slot = buf.readShort();
    stack = ByteBufUtils.readItemStack(buf);
  }

  @Override
  public void toBytes(ByteBuf buf) {
    super.toBytes(buf);
    buf.writeShort(slot);
    ByteBufUtils.writeItemStack(buf, stack);
  }

  public static class Handler implements IMessageHandler<PacketGhostSlot, IMessage> {

    @Override
    public IMessage onMessage(PacketGhostSlot msg, MessageContext ctx) {
      TileEntityEnder te = msg.getTileEntity(ctx.getServerHandler().playerEntity.worldObj);
      if (te != null) {
        te.setGhostSlotContents(msg.slot, msg.stack);
        te.getWorldObj().markBlockForUpdate(msg.x, msg.y, msg.z);
      }
      return null;
    }
  }

}
