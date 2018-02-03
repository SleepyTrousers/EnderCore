package com.enderio.core.common.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class IBlockAccessWrapper implements IBlockAccess {

  protected @Nonnull IBlockAccess wrapped;

  public IBlockAccessWrapper(@Nonnull IBlockAccess ba) {
    wrapped = ba;
  }

  @Override
  public boolean isSideSolid(@Nonnull BlockPos pos, @Nonnull EnumFacing side, boolean _default) {
    return wrapped.isSideSolid(pos, side, _default);
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
  public @Nonnull IBlockState getBlockState(@Nonnull BlockPos pos) {
    return wrapped.getBlockState(pos);
  }

  @SideOnly(Side.CLIENT)
  @Override
  public int getCombinedLight(@Nonnull BlockPos pos, int lightValue) {
    return 15 << 20 | 15 << 4;
  }

  @Override
  public boolean isAirBlock(@Nonnull BlockPos pos) {
    return wrapped.isAirBlock(pos);
  }

  @Override
  public @Nonnull Biome getBiome(@Nonnull BlockPos pos) {
    return wrapped.getBiome(pos);
  }

  @Override
  public int getStrongPower(@Nonnull BlockPos pos, @Nonnull EnumFacing direction) {
    return wrapped.getStrongPower(pos, direction);
  }

  @Override
  public @Nonnull WorldType getWorldType() {
    return wrapped.getWorldType();
  }

}
