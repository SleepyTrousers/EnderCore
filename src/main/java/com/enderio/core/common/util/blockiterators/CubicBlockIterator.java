package com.enderio.core.common.util.blockiterators;

import javax.annotation.Nonnull;

import net.minecraft.util.math.BlockPos;

public class CubicBlockIterator extends AbstractBlockIterator {
  protected int radius;
  protected int minX, minY, minZ;
  protected int curX, curY, curZ;
  protected int maxX, maxY, maxZ;

  public CubicBlockIterator(@Nonnull BlockPos base, int radius) {
    super(base);
    this.radius = radius;

    curX = minX = base.getX() - radius;
    curY = minY = base.getY() - radius;
    curZ = minZ = base.getZ() - radius;

    maxX = base.getX() + radius;
    maxY = base.getY() + radius;
    maxZ = base.getZ() + radius;
  }

  @Override
  public BlockPos next() {
    BlockPos ret = new BlockPos(curX, curY, curZ);
    curX = curX == maxX ? minX : curX + 1;
    curY = curX == minX ? curY == maxY ? minY : curY + 1 : curY;
    curZ = curY == minY && curX == minX ? curZ + 1 : curZ;
    return ret;
  }

  @Override
  public boolean hasNext() {
    return curZ <= maxZ;
  }
}
