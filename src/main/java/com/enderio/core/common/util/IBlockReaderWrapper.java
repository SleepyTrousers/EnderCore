package com.enderio.core.common.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class IBlockReaderWrapper implements IBlockReader {

  protected @Nonnull IBlockReader wrapped;

  public IBlockReaderWrapper(@Nonnull IBlockReader ba) {
    wrapped = ba;
  }

  @Override
  public @Nullable TileEntity getTileEntity(@Nonnull BlockPos pos) {
    if (pos.getY() >= 0 && pos.getY() < 256) {
      return wrapped.getTileEntity(pos);
    } else {
      return null;
    }
  }

  @Override
  public @Nonnull BlockState getBlockState(@Nonnull BlockPos pos) {
    return wrapped.getBlockState(pos);
  }

  @Override
  public FluidState getFluidState(BlockPos pos) {
    return wrapped.getFluidState(pos);
  }

  @Override
  public int getLightValue(BlockPos pos) {
    return 15 << 20 | 15 << 4;
  }
}
