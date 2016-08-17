package com.enderio.core.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraftforge.client.MinecraftForgeClient;

public class TESRState {

  /**
   * Reset the GlStateManager managed GL state to the values that are set by the WorldRenderer for the TESR rendering.
   * <p>
   * Values are set as the state manager sets them originally for the "entity+tile entity" rendering, not as the entity renderer resets them if it has some
   * entities to render.
   * <p>
   * Not changed are:
   * <ul>
   * <li>Flags that are both off and are not disabled by the WorldRenderer just before the entity rendering
   * <li>Values that are not set by the WorldRenderer just before the entity rendering
   * <li>Values that are computed (e.g. fog color and density)
   * <li>Texture and matrix related values
   * <li>Flags and values that are not managed by the GLStateManager
   * </ol>
   * 
   * <p>
   * These values have been confirmed by a state dump in an empty flat world (profile "redstone ready").
   * <p>
   * Differences to the vanilla state: When looking towards the ground (view vector pointing below the horizon) or having an entity in view, vanilla/forge sets
   * slightly different values. This sets the values as if there was no entity and the player was looking (slightly) upwards.
   */
  public static void reset() {
    RenderHelper.enableStandardItemLighting();
    GlStateManager.enableTexture2D();
    GlStateManager.enableDepth();
    GlStateManager.depthFunc(GL11.GL_LEQUAL);
    GlStateManager.enableFog();
    GlStateManager.enableAlpha();
    GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
    GlStateManager.enableCull();
    GlStateManager.enableColorMaterial();
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    if (MinecraftForgeClient.getRenderPass() == 0) {
      GlStateManager.disableBlend();
      GlStateManager.shadeModel(GL11.GL_FLAT);
      GlStateManager.depthMask(true);
    } else {
      GlStateManager.enableBlend();
      GlStateManager.shadeModel(GL11.GL_SMOOTH);
      GlStateManager.depthMask(false);
    }
    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
        GlStateManager.DestFactor.ZERO);
  }

  /**
   * Sets the GL state appropriate for rendering blocks.
   * <p>
   * This sets all flags reset() would reset, so there's no need to call both at the beginning of a method.
   * <p>
   * The state that is set is basically what a FastTESR in pass 0 would get, just with culling enabled.
   */
  public static void setBlockMode() {
    GlStateManager.enableTexture2D();
    GlStateManager.enableDepth();
    GlStateManager.depthFunc(GL11.GL_LEQUAL);
    GlStateManager.enableFog();
    GlStateManager.enableAlpha();
    GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
    GlStateManager.enableCull();
    GlStateManager.enableColorMaterial();
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    GlStateManager.depthMask(true);

    RenderUtil.bindBlockTexture();
    RenderHelper.disableStandardItemLighting();
    GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    GlStateManager.enableBlend();

    if (Minecraft.isAmbientOcclusionEnabled()) {
      GlStateManager.shadeModel(GL11.GL_SMOOTH);
    } else {
      GlStateManager.shadeModel(GL11.GL_FLAT);
    }
  }

}
