package com.enderio.core.common.network;

import com.enderio.core.client.gui.widget.GhostSlot;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketGhostSlot implements IMessage {

  private int windowId;
  private int slot;
  private ItemStack stack;

  public PacketGhostSlot() {
  }

  public static PacketGhostSlot setGhostSlotContents(int slot, ItemStack stack) {
    PacketGhostSlot msg = new PacketGhostSlot();
    msg.slot = slot;
    msg.stack = stack;
    msg.windowId = Minecraft.getMinecraft().thePlayer.openContainer.windowId;
    return msg;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    windowId = buf.readInt();
    slot = buf.readShort();
    stack = ByteBufUtils.readItemStack(buf);
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeInt(windowId);
    buf.writeShort(slot);
    ByteBufUtils.writeItemStack(buf, stack);
  }

  public static class Handler implements IMessageHandler<PacketGhostSlot, IMessage> {

    @Override
    public IMessage onMessage(PacketGhostSlot msg, MessageContext ctx) {
      Container openContainer = ctx.getServerHandler().playerEntity.openContainer;
      if (openContainer instanceof GhostSlot.IGhostSlotAware && openContainer.windowId == msg.windowId) {
        ((GhostSlot.IGhostSlotAware) openContainer).setGhostSlotContents(msg.slot, msg.stack);
      }
      return null;
    }
  }

}
