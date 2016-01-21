package com.enderio.core.client.gui.button;

import org.lwjgl.opengl.GL11;

import com.enderio.core.client.render.RenderUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemButton extends GuiButton {

  public static final int DEFAULT_WIDTH = 24;
  public static final int HWIDTH = DEFAULT_WIDTH / 2;
  public static final int DEFAULT_HEIGHT = 24;
  public static final int HHEIGHT = DEFAULT_HEIGHT / 2;

  private ItemStack item;

  protected int hwidth;
  protected int hheight;

  public ItemButton(int id, int x, int y, Item item) {
    super(id, x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, "");    
    this.item = new ItemStack(item, 1, 0);
    hwidth = HWIDTH;
    hheight = HHEIGHT;
  }

  public void setSize(int width, int height) {
    this.width = width;
    this.height = height;
    hwidth = width / 2;
    hheight = height / 2;
  }

  /**
   * Draws this button to the screen.
   */
  @SuppressWarnings("synthetic-access")
  @Override
  public void drawButton(Minecraft par1Minecraft, int par2, int par3) {
    if (visible) {

      RenderUtil.bindTexture("textures/gui/widgets.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.hovered = par2 >= this.xPosition && par3 >= this.yPosition && par2 < this.xPosition + width && par3 < this.yPosition + height;
      int hoverState = this.getHoverState(this.hovered);

      // x, y, u, v, width, height

      // top half
      drawTexturedModalRect(xPosition, yPosition, 0, 46 + hoverState * 20, hwidth, hheight);
      drawTexturedModalRect(xPosition + hwidth, yPosition, 200 - hwidth, 46 + hoverState * 20, hwidth, hheight);

      // bottom half
      drawTexturedModalRect(xPosition, yPosition + hheight, 0, 66 - hheight + (hoverState * 20), hwidth, hheight);
      drawTexturedModalRect(xPosition + hwidth, yPosition + hheight, 200 - hwidth, 66 - hheight + (hoverState * 20), hwidth, hheight);

      mouseDragged(par1Minecraft, par2, par3);

      int xLoc = xPosition + hwidth - 8;
      int yLoc = yPosition + hheight - 10;      
      par1Minecraft.getRenderItem().renderItemIntoGUI(item, xLoc, yLoc);
    }
  }

}