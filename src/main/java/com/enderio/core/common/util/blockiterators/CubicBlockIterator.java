package com.enderio.core.common.util.blockiterators;

import javax.annotation.Nonnull;

import com.enderio.core.client.render.BoundingBox;

import net.minecraft.util.math.BlockPos;

public class CubicBlockIterator extends AbstractBlockIterator {
  protected final int minX, minY, minZ;
  protected final int maxX, maxY, maxZ;
  protected int curX, curY, curZ;

  protected CubicBlockIterator(@Nonnull BlockPos base, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    super(base);
    this.minX = curX = minX;
    this.minY = curY = minY;
    this.minZ = curZ = minZ;
    this.maxX = maxX;
    this.maxY = maxY;
    this.maxZ = maxZ;
  }

  public CubicBlockIterator(@Nonnull BlockPos base, int radius) {
    this(base, base.getX() - radius, base.getY() - radius, base.getZ() - radius, base.getX() + radius, base.getY() + radius, base.getZ() + radius);
  }

  public CubicBlockIterator(@Nonnull BlockPos pos0, @Nonnull BlockPos pos1) {
    this(pos0, pos0.getX(), pos0.getY(), pos0.getZ(), pos1.getX(), pos1.getY(), pos1.getZ());
  }

  public CubicBlockIterator(@Nonnull BoundingBox bb) {
    this(new BlockPos(bb.getCenter()), (int) bb.minX, (int) bb.minY, (int) bb.minZ, (int) bb.maxX, (int) bb.maxY, (int) bb.maxZ);
  }

  @Override
  public @Nonnull BlockPos next() {
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
