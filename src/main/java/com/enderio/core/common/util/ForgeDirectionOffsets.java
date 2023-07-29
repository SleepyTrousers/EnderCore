package com.enderio.core.common.util;

import com.enderio.core.common.vecmath.Vector3d;

import net.minecraft.util.EnumFacing;

public final class ForgeDirectionOffsets {

  public static final Vector3d[] OFFSETS = new Vector3d[EnumFacing.values().length];

  static {
    for (EnumFacing dir : EnumFacing.values()) {
      OFFSETS[dir.ordinal()] = new Vector3d(dir.getXOffset(), dir.getYOffset(), dir.getZOffset());
    }
  }

  public static Vector3d forDir(EnumFacing dir) {
    return OFFSETS[dir.ordinal()];
  }

  public static Vector3d forDirCopy(EnumFacing dir) {
    return new Vector3d(OFFSETS[dir.ordinal()]);
  }

  public static Vector3d offsetScaled(EnumFacing dir, double scale) {
    Vector3d res = forDirCopy(dir);
    res.scale(scale);
    return res;
  }

  public static Vector3d absolueOffset(EnumFacing dir) {
    Vector3d res = forDirCopy(dir);
    res.x = Math.abs(res.x);
    res.y = Math.abs(res.y);
    res.z = Math.abs(res.z);
    return res;
  }

  public static EnumFacing closest(float x, float y, float z) {
    float ax = Math.abs(x);
    float ay = Math.abs(y);
    float az = Math.abs(z);

    if (ax >= ay && ax >= az) {
      return x > 0 ? EnumFacing.EAST : EnumFacing.WEST;
    }
    if (ay >= ax && ay >= az) {
      return y > 0 ? EnumFacing.UP : EnumFacing.DOWN;
    }
    return z > 0 ? EnumFacing.SOUTH : EnumFacing.NORTH;
  }

  private ForgeDirectionOffsets() {
  }

  public static boolean isPositiveOffset(EnumFacing dir) {
    return dir == EnumFacing.SOUTH || dir == EnumFacing.EAST || dir == EnumFacing.UP;
  }

}
