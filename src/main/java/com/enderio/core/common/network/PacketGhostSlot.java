package com.enderio.core.common.network;

import javax.annotation.Nonnull;

import com.enderio.core.client.gui.widget.GhostSlot;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketGhostSlot {

  int windowId;
  int slot;
  @Nonnull
  ItemStack stack = ItemStack.EMPTY;
  int realsize;

  public PacketGhostSlot() {
  }

  public PacketGhostSlot(PacketBuffer buffer) {
    windowId = buffer.readInt();
    slot = buffer.readShort();
    stack = buffer.readItemStack();
    realsize = buffer.readInt();
  }

  public static PacketGhostSlot setGhostSlotContents(int slot, @Nonnull ItemStack stack, int realsize) {
    PacketGhostSlot msg = new PacketGhostSlot();
    msg.slot = slot;
    msg.stack = stack;
    msg.realsize = realsize;
    msg.windowId = Minecraft.getInstance().player.openContainer.windowId;
    return msg;
  }

  public void toBytes(PacketBuffer buffer) {
    buffer.writeInt(windowId);
    buffer.writeShort(slot);
    buffer.writeItemStack(stack);
    buffer.writeInt(realsize);
  }

  public boolean handle(Supplier<NetworkEvent.Context> context) {
    Container openContainer = context.get().getSender().openContainer;
    if (openContainer instanceof GhostSlot.IGhostSlotAware && openContainer.windowId == windowId) {
      ((GhostSlot.IGhostSlotAware) openContainer).setGhostSlotContents(slot, stack, realsize);
    }
    return false;
  }
}
