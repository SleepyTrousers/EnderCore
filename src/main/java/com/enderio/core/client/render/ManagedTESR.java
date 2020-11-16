package com.enderio.core.client.render;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;

import com.enderio.core.common.TileEntityBase;

import net.minecraft.block.Block;
import net.minecraftforge.client.MinecraftForgeClient;

public abstract class ManagedTESR<T extends TileEntityBase> extends TileEntityRenderer<T> {

  protected final @Nullable Block block;

  public ManagedTESR(TileEntityRendererDispatcher rendererDispatcherIn, @Nullable Block block) {
    super(rendererDispatcherIn);
    this.block = block;
  }

  @Override
  public void render(@Nonnull T tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
    if (tileEntityIn != null && tileEntityIn.hasWorld() && !tileEntityIn.isRemoved()) {
      final BlockState blockState = tileEntityIn.getWorld().getBlockState(tileEntityIn.getPos());
      final int renderPass = Minecraft.getInstance().isRenderOnThread() ? 1 : 0;

      if ((block == null || block == blockState.getBlock()) && shouldRender(tileEntityIn, blockState, renderPass)) {

        RenderSystem.disableLighting();
        if (renderPass == 0) {
          RenderSystem.disableBlend();
          RenderSystem.depthMask(true);
        } else {
          RenderSystem.enableBlend();
          RenderSystem.depthMask(false);
          RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }

        RenderUtil.bindBlockTexture();
        matrixStackIn.push();
        matrixStackIn.translate(tileEntityIn.getPos().getX(), tileEntityIn.getPos().getY(), tileEntityIn.getPos().getZ());
        renderTileEntity(tileEntityIn, blockState, partialTicks);
        matrixStackIn.pop();
      }
    } else if (tileEntityIn == null) {
      renderItem();
    }
  }

  protected abstract void renderTileEntity(@Nonnull T te, @Nonnull BlockState blockState, float partialTicks);

  protected boolean shouldRender(@Nonnull T te, @Nonnull BlockState blockState, int renderPass) {
    return true;
  }

  protected void renderItem() {
  }

}
