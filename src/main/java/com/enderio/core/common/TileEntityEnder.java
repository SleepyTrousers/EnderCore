package com.enderio.core.common;

import com.enderio.core.common.config.ConfigHandler;
import com.enderio.core.common.network.EnderPacketHandler;
import com.enderio.core.common.network.PacketProgress;
import com.enderio.lib.api.common.util.IProgressTile;
import com.enderio.lib.common.TileEntityBase;
import com.enderio.lib.common.util.Util;

public abstract class TileEntityEnder extends TileEntityBase {

  protected final boolean isProgressTile;

  protected int lastProgressScaled = -1;
  protected int ticksSinceLastProgressUpdate;

  public TileEntityEnder() {
    isProgressTile = this instanceof IProgressTile;
  }

  @Override
  public final boolean canUpdate() {
    return shouldUpdate() || isProgressTile;
  }

  protected boolean shouldUpdate() {
    return true;
  }

  private long lastUpdate = 0;

  @Override
  public final void updateEntity() {
    if (ConfigHandler.allowExternalTickSpeedup || worldObj.getTotalWorldTime() != lastUpdate) {
      lastUpdate = worldObj.getTotalWorldTime();
      doUpdate();
      if (isProgressTile && !worldObj.isRemote) {
        int curScaled = getProgressScaled(16);
        if (++ticksSinceLastProgressUpdate >= getProgressUpdateFreq() || curScaled != lastProgressScaled) {
          sendTaskProgressPacket();
          lastProgressScaled = curScaled;
        }
      }
    }
  }

  public final int getProgressScaled(int scale) {
    if (isProgressTile) {
      return Util.getProgressScaled(scale, (IProgressTile) this);
    }
    return 0;
  }

  protected void doUpdate() {

  }

  protected void sendTaskProgressPacket() {
    if (isProgressTile) {
      EnderPacketHandler.sendToAllAround(new PacketProgress((IProgressTile) this), this);
    }
    ticksSinceLastProgressUpdate = 0;
  }

  /**
   * Controls how often progress updates. Has no effect if your TE is not
   * {@link IProgressTile}.
   */
  protected int getProgressUpdateFreq() {
    return 20;
  }
}
