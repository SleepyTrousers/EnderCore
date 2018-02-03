package com.enderio.core.client.render;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

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

  @SuppressWarnings({ "null", "unused" })
  @Override
  public final void render(@Nonnull T te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
    if (te != null && te.hasWorld() && !te.isInvalid()) {
      final IBlockState blockState = te.getWorld().getBlockState(te.getPos());
      final int renderPass = MinecraftForgeClient.getRenderPass();
      if ((block == null || block == blockState.getBlock()) && shouldRender(te, blockState, renderPass)) {
        GlStateManager.disableLighting();
        if (renderPass == 0) {
          GlStateManager.disableBlend();
          GlStateManager.depthMask(true);
        } else {
          GlStateManager.enableBlend();
          GlStateManager.depthMask(false);
          GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }

        RenderUtil.bindBlockTexture();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        renderTileEntity(te, blockState, partialTicks, destroyStage);
        GlStateManager.popMatrix();
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
