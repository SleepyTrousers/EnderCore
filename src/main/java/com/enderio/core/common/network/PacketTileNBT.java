package com.enderio.core.common.network;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.Log;
import com.enderio.core.common.util.NullHelper;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Created by CrazyPants on 27/02/14.
 */
public class PacketTileNBT {

  TileEntity te;

  long pos;
  CompoundNBT tags;

  boolean renderOnUpdate = false;

  public PacketTileNBT() {

  }

  public PacketTileNBT(TileEntity te) {
    this.te = te;
    pos = te.getPos().toLong();
    te.write(tags = new CompoundNBT());
  }

  public void toBytes(PacketBuffer buffer) {
    buffer.writeLong(pos);
    buffer.writeCompoundTag(tags);
  }

  public void fromBytes(PacketBuffer buffer) {
    pos = buffer.readLong();
    tags = buffer.readCompoundTag();
  }

  public @Nonnull BlockPos getPos() {
    return BlockPos.fromLong(pos);
  }

  public boolean handle(Supplier<NetworkEvent.Context> context) {
    context.get().enqueueWork(() -> {
      te = handle(context.get().getSender().world);
      if (te != null && renderOnUpdate) {
        BlockState bs = te.getWorld().getBlockState(getPos());
        te.getWorld().notifyBlockUpdate(getPos(), bs, bs, 3);
      }
    });
    return true;
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
    tileEntity.read(tileEntity.getBlockState(), NullHelper.notnull(tags, "NetworkUtil.readNBTTagCompound()"));
    return tileEntity;
  }
}
