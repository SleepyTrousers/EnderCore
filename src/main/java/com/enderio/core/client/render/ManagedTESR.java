package com.enderio.core.client.render;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.TileEntityBase;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.client.MinecraftForgeClient;

public abstract class ManagedTESR<T extends TileEntityBase> extends TileEntitySpecialRenderer<T> {

  protected final @Nullable Block block;

  public ManagedTESR(@Nullable Block block) {
    super();
    this.block = block;
  }

  @Override
  public final void renderTileEntityAt(T te, double x, double y, double z, float partialTicks, int destroyStage) {
    if (te != null && te.hasWorldObj() && !te.isInvalid()) {
      final IBlockState blockState = te.getWorld().getBlockState(te.getPos());
      final int renderPass = MinecraftForgeClient.getRenderPass();
      if (blockState != null && (block == null || block == blockState.getBlock()) && shouldRender(te, blockState, renderPass)) {
        if (renderPass == 0) {
          TESRState.setBlockMode();
        } else {
          TESRState.reset();
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        renderTileEntity(te, blockState, partialTicks, destroyStage);
        GlStateManager.popMatrix();
        TESRState.reset();
      }
    } else if (te == null) {
      renderItem();
    }
  }

  protected abstract void renderTileEntity(@Nonnull T te, @Nonnull IBlockState blockState, float partialTicks, int destroyStage);

  protected boolean shouldRender(@Nonnull T te, @Nonnull IBlockState blockState, int renderPass) {
    return true;
  }

  protected void renderItem() {

  }

}
