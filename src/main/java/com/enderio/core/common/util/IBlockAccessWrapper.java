package com.enderio.core.common.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class IBlockAccessWrapper implements IBlockAccess {

  protected IBlockAccess wrapped;

  public IBlockAccessWrapper(IBlockAccess ba) {
    wrapped = ba;
  }

  @Override
  public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
    return wrapped.isSideSolid(pos, side, _default);
  }

  
  @Override
  public TileEntity getTileEntity(BlockPos pos) {
    if (pos.getY() >= 0 && pos.getY() < 256) {
      return wrapped.getTileEntity(pos);
    } else {
      return null;
    }
  }

  @Override
  public IBlockState getBlockState(BlockPos pos) {
    return wrapped.getBlockState(pos);
  }
  
  
  @SideOnly(Side.CLIENT)
  @Override
  public int getCombinedLight(BlockPos pos, int lightValue) {
    return 15 << 20 | 15 << 4;
  }

  @Override
  public boolean isAirBlock(BlockPos pos) {
    return wrapped.isAirBlock(pos);
  }

  @Override
  public BiomeGenBase getBiomeGenForCoords(BlockPos pos) {
    return wrapped.getBiomeGenForCoords(pos);
  }

  @Override
  public int getStrongPower(BlockPos pos, EnumFacing direction) {    
    return wrapped.getStrongPower(pos, direction);
  }

  @Override
  public WorldType getWorldType() {
    return wrapped.getWorldType();
  }  

  @Override
  @SideOnly(Side.CLIENT)
  public boolean extendedLevelsInChunkCache() {
    return wrapped.extendedLevelsInChunkCache();
  }

}
