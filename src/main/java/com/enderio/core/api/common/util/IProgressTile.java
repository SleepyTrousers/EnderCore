package com.enderio.core.api.common.util;

import javax.annotation.Nonnull;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IProgressTile {

    float getProgress();

    /**
     * Client-only. Called to set clientside progress for syncing/rendering purposes.
     *
     * @param progress
     *            The % progress.
     */
    @SideOnly(Side.CLIENT)
    void setProgress(float progress);

  @Nonnull
  TileEntity getTileEntity();
}
