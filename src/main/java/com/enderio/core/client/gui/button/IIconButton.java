package com.enderio.core.client.gui.button;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.opengl.GL11;

import com.enderio.core.client.render.RenderUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

public class IIconButton extends BaseButton {

  public static final int DEFAULT_WIDTH = 24;
  public static final int HWIDTH = DEFAULT_WIDTH / 2;
  public static final int DEFAULT_HEIGHT = 24;
  public static final int HHEIGHT = DEFAULT_HEIGHT / 2;

  protected int hwidth;
  protected int hheight;

  protected @Nullable TextureAtlasSprite icon;
  protected @Nullable ResourceLocation texture;

  public IIconButton(@Nonnull FontRenderer fr, int x, int y, @Nullable TextureAtlasSprite icon, @Nullable ResourceLocation texture) {
    super(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, new StringTextComponent(""));
    hwidth = HWIDTH;
    hheight = HHEIGHT;
    this.icon = icon;
    this.texture = texture;
  }

  public void setSize(int width, int height) {
    this.width = width;
    this.height = height;
    hwidth = width / 2;
    hheight = height / 2;
  }

  public @Nullable TextureAtlasSprite getIcon() {
    return icon;
  }

  public void setIcon(@Nullable TextureAtlasSprite icon) {
    this.icon = icon;
  }

  public @Nullable ResourceLocation getTexture() {
    return texture;
  }

  public void setTexture(@Nullable ResourceLocation textureName) {
    this.texture = textureName;
  }

  @Override
  public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    RenderUtil.bindTexture("textures/gui/widgets.png");
    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

    // TODO: Necessary?
    int hoverState = isHovered() ? isMouseOver(mouseX, mouseY) ? 2 : 1 : 0;

    // x, y, u, v, width, height

    // top half
    blit(matrixStack, x, y, 0, 46 + hoverState * 20, hwidth, hheight);
    blit(matrixStack, x + hwidth, y, 200 - hwidth, 46 + hoverState * 20, hwidth, hheight);

    // bottom half
    blit(matrixStack, x, y + hheight, 0, 66 - hheight + (hoverState * 20), hwidth, hheight);
    blit(matrixStack, x + hwidth, y + hheight, 200 - hwidth, 66 - hheight + (hoverState * 20), hwidth, hheight);

    final TextureAtlasSprite icon2 = icon;
    final ResourceLocation texture2 = texture;
    if (icon2 != null && texture2 != null) {

      RenderSystem.enableBlend();
      RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

      RenderUtil.bindTexture(texture2);
      int xLoc = x + 2;
      int yLoc = y + 2;
      blit(matrixStack, xLoc, yLoc, 0, width - 4, height - 4, icon2);

      RenderSystem.disableBlend();
    }
  }
}
