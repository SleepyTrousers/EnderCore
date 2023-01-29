package com.enderio.core.api.common.util;

import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IProgressTile {

    float getProgress();

    /**
     * Client-only. Called to set clientside progress for syncing/rendering purposes.
     * 
     * @param progress The % progress.
     */
    @SideOnly(Side.CLIENT)
    void setProgress(float progress);

    TileEntity getTileEntity();
}
