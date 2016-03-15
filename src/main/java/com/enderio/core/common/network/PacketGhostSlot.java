package com.enderio.core.common.network;

import com.enderio.core.common.TileEntityBase;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketGhostSlot extends MessageTileEntity<TileEntityBase> {

  private int slot;
  private ItemStack stack;

  public PacketGhostSlot() {
  }

  private PacketGhostSlot(TileEntityBase tile) {
    super(tile);
  }

  public static PacketGhostSlot setGhostSlotContents(TileEntityBase te, int slot, ItemStack stack) {
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
      TileEntityBase te = msg.getTileEntity(ctx.getServerHandler().playerEntity.worldObj);
      if (te != null) {               
        te.setGhostSlotContents(msg.slot, msg.stack);
        te.getWorld().markBlockForUpdate(msg.getPos());
      }
      return null;
    }
  }

}
