package com.enderio.core.common.network;

import javax.annotation.Nonnull;

import com.enderio.core.api.common.util.IProgressTile;

import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketProgress extends PacketTileEntity<TileEntity> {

  float progress;

  public PacketProgress() {
  }

  public PacketProgress(@Nonnull IProgressTile tile) {
    super(tile.getTileEntity());
    progress = tile.getProgress();
  }

  public PacketProgress(PacketBuffer buffer) {
    super(buffer);
    progress = buffer.readFloat();
  }

  @Override
  public void toBytes(PacketBuffer buffer) {
    super.toBytes(buffer);
    buffer.writeFloat(progress);
  }

  @Override
  public void onReceived(@Nonnull TileEntity te, @Nonnull Supplier<NetworkEvent.Context> context) {
    if (te instanceof IProgressTile) {
      ((IProgressTile) te).setProgress(progress);
    }
  }
}
