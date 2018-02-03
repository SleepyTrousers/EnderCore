package com.enderio.core.common.util;

import javax.annotation.Nonnull;

import com.google.common.base.Strings;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;

public class BlockCoord {

  private BlockCoord() {
  }

  public static @Nonnull BlockPos get(TileEntity tile) {
    return get(tile.getPos());
  }

  public static @Nonnull BlockPos get(Entity e) {
    return get(e.posX, e.posY, e.posZ);
  }

  public static @Nonnull BlockPos get(BlockPos bc) {
    return get(bc.getX(), bc.getY(), bc.getZ());
  }

  public static @Nonnull BlockPos get(int x, int y, int z) {
    return new BlockPos(x, y, z);
  }

  private static @Nonnull BlockPos get(double x, double y, double z) {
    return get((int) x, (int) y, (int) z);
  }

  public static @Nonnull BlockPos get(String x, String y, String z) {
    return get(Strings.isNullOrEmpty(x) ? 0 : Integer.parseInt(x), Strings.isNullOrEmpty(y) ? 0 : Integer.parseInt(y),
        Strings.isNullOrEmpty(z) ? 0 : Integer.parseInt(z));
  }

  public static @Nonnull BlockPos get(RayTraceResult mop) {
    return get(mop.getBlockPos());
  }

  public static int getDistSq(BlockPos a, BlockPos b) {
    int xDiff = a.getX() - b.getX();
    int yDiff = a.getY() - b.getY();
    int zDiff = a.getZ() - b.getZ();
    return xDiff * xDiff + yDiff * yDiff + zDiff * zDiff;
  }

  public static int getDistSq(BlockPos a, TileEntity other) {
    return getDistSq(a, get(other));
  }

  public static int getDist(BlockPos a, BlockPos b) {
    double dsq = getDistSq(a, b);
    return (int) Math.ceil(Math.sqrt(dsq));
  }

  public static int getDist(BlockPos a, TileEntity other) {
    return getDist(a, get(other));
  }

  public static @Nonnull String chatString(BlockPos pos, TextFormatting defaultColor) {
    return String.format("x%s%d%s y%s%d%s z%s%d", TextFormatting.GREEN, pos.getX(), defaultColor, TextFormatting.GREEN, pos.getY(), defaultColor,
        TextFormatting.GREEN, pos.getZ());
  }

  public static @Nonnull BlockPos withX(BlockPos pos, final int x) {
    return pos.getX() == x ? pos : get(x, pos.getY(), pos.getZ());
  }

  public static @Nonnull BlockPos withY(BlockPos pos, final int y) {
    return pos.getY() == y ? pos : get(pos.getX(), y, pos.getZ());
  }

  public static @Nonnull BlockPos withZ(BlockPos pos, final int z) {
    return pos.getZ() == z ? pos : get(pos.getX(), pos.getY(), z);
  }
}
