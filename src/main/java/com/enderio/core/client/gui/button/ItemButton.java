package com.enderio.core.client.gui.button;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.opengl.GL11;

import com.enderio.core.client.render.RenderUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemButton extends BaseButton {

  public static final int DEFAULT_WIDTH = 24;
  public static final int HWIDTH = DEFAULT_WIDTH / 2;
  public static final int DEFAULT_HEIGHT = 24;
  public static final int HHEIGHT = DEFAULT_HEIGHT / 2;

  private @Nonnull ItemStack item;

  protected int hwidth;
  protected int hheight;

  public ItemButton(int x, int y, @Nonnull Item item) {
    super(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, new StringTextComponent(""));
    this.item = new ItemStack(item, 1);
    hwidth = HWIDTH;
    hheight = HHEIGHT;
  }

  public ItemButton(int x, int y, @Nonnull Item item, IPressable pressedEvent) {
    super(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, new StringTextComponent(""), pressedEvent);
    this.item = new ItemStack(item, 1);
    hwidth = HWIDTH;
    hheight = HHEIGHT;
  }

  public void setSize(int width, int height) {
    this.width = width;
    this.height = height;
    hwidth = width / 2;
    hheight = height / 2;
  }

  @Override
  public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    RenderUtil.bindTexture("textures/gui/widgets.png");
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

    // TODO: Necessary?
    int hoverState = isHovered() ? isMouseOver(mouseX, mouseY) ? 2 : 1 : 0;

    // x, y, u, v, width, height

    // top half
    blit(matrixStack, x, y, 0, 46 + hoverState * 20, hwidth, hheight);
    blit(matrixStack, x + hwidth, y, 200 - hwidth, 46 + hoverState * 20, hwidth, hheight);

    // bottom half
    blit(matrixStack, x, y + hheight, 0, 66 - hheight + (hoverState * 20), hwidth, hheight);
    blit(matrixStack, x + hwidth, y + hheight, 200 - hwidth, 66 - hheight + (hoverState * 20), hwidth, hheight);

    // TODO: AGAIN WHY!!!
//    mouseDragged(par1Minecraft, par2, par3);

    int xLoc = x + hwidth - 8;
    int yLoc = y + hheight - 10;
    Minecraft.getInstance().getItemRenderer().renderItemIntoGUI(item, xLoc, yLoc);
  }

}