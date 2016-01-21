package com.enderio.core.client.gui;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.enderio.core.api.client.gui.IGuiOverlay;
import com.enderio.core.api.client.gui.IGuiScreen;
import com.enderio.core.client.gui.ToolTipManager.ToolTipRenderer;
import com.enderio.core.client.gui.button.IconButton;
import com.enderio.core.client.gui.widget.GhostSlot;
import com.enderio.core.client.gui.widget.GuiToolTip;
import com.enderio.core.client.gui.widget.TextFieldEnder;
import com.enderio.core.client.gui.widget.VScrollbar;
import com.enderio.core.client.render.RenderUtil;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Timer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.Optional;

@Optional.InterfaceList({ @Optional.Interface(iface = "codechicken.nei.api.INEIGuiHandler", modid = "NotEnoughItems") })
public abstract class GuiContainerBase extends GuiContainer implements ToolTipRenderer, IGuiScreen {//, INEIGuiHandler {

  protected ToolTipManager ttMan = new ToolTipManager();
  protected List<IGuiOverlay> overlays = Lists.newArrayList();
  protected List<TextFieldEnder> textFields = Lists.newArrayList();
  protected List<VScrollbar> scrollbars = Lists.newArrayList();
  protected GhostSlotHandler ghostSlotHandler = new GhostSlotHandler();

  @Deprecated
  protected List<GhostSlot> ghostSlots = ghostSlotHandler.getGhostSlots();
  @Deprecated
  protected GhostSlot hoverGhostSlot;

  protected VScrollbar draggingScrollbar;

  protected GuiContainerBase(Container par1Container) {
    super(par1Container);
  }

  @Override
  public void initGui() {
    super.initGui();
    fixupGuiPosition();
    for (IGuiOverlay overlay : overlays) {
      overlay.init(this);
    }
    for (TextFieldEnder f : textFields) {
      f.init(this);
    }
  }

  protected void fixupGuiPosition() {
  }

  @Override
  protected void keyTyped(char c, int key) throws IOException {
    TextFieldEnder focused = null;
    for (TextFieldEnder f : textFields) {
      if (f.isFocused()) {
        focused = f;
      }
    }

    // If esc is pressed
    if (key == 1) {
      // If there is a focused text field unfocus it
      if (focused != null && key == 1) {
        focused.setFocused(false);
        focused = null;
        return;
      } else if (!hideOverlays()) { // Otherwise close overlays/GUI
        this.mc.thePlayer.closeScreen();
        return;
      }
    }

    // If the user pressed tab, switch to the next text field, or unfocus if there are none
    if (c == '\t') {
      for (int i = 0; i < textFields.size(); i++) {
        TextFieldEnder f = textFields.get(i);
        if (f.isFocused()) {
          textFields.get((i + 1) % textFields.size()).setFocused(true);
          f.setFocused(false);
          return;
        }
      }
    }

    // If there is a focused text field, attempt to type into it
    if (focused != null) {
      String old = focused.getText();
      if (focused.textboxKeyTyped(c, key)) {
        onTextFieldChanged(focused, old);
        return;
      }
    }

    // More NEI behavior, f key focuses first text field
    if (c == 'f' && focused == null && !textFields.isEmpty()) {
      focused = textFields.get(0);
      focused.setFocused(true);
    }

    // Finally if 'e' was pressed but not captured by a text field, close the overlays/GUI
    if (key == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
      if (!hideOverlays()) {
        this.mc.thePlayer.closeScreen();
      }
      return;
    }

    // If the key was not captured, let NEI do its thing
    super.keyTyped(c, key);
  }

  protected final void setText(TextFieldEnder tf, String newText) {
    String old = tf.getText();
    tf.setText(newText);
    onTextFieldChanged(tf, old);
  }

  protected void onTextFieldChanged(TextFieldEnder tf, String old) {

  }

  public boolean hideOverlays() {
    for (IGuiOverlay overlay : overlays) {
      if (overlay.isVisible()) {
        overlay.setIsVisible(false);
        return true;
      }
    }
    return false;
  }

  @Override
  public void addToolTip(GuiToolTip toolTip) {
    ttMan.addToolTip(toolTip);
  }

  @Override
  public void updateScreen() {
    super.updateScreen();

    if (!ForgeHooks.canInteractWith(mc.thePlayer, inventorySlots)) {
      mc.thePlayer.closeScreen();
    }

    for (GuiTextField f : textFields) {
      f.updateCursorCounter();
    }
  }

  @Override
  public void handleMouseInput() throws IOException {
    int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
    int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
    int b = Mouse.getEventButton();
    for (IGuiOverlay overlay : overlays) {
      if (overlay != null && overlay.isVisible() && overlay.handleMouseInput(x, y, b)) {
        return;
      }
    }
    int delta = Mouse.getEventDWheel();
    if (delta != 0) {
      mouseWheel(x, y, delta);
    }
    super.handleMouseInput();
  }

  @Override
  protected boolean isPointInRegion(int p_146978_1_, int p_146978_2_, int p_146978_3_, int p_146978_4_, int p_146978_5_, int p_146978_6_) {
    int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
    int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
    for (IGuiOverlay overlay : overlays) {
      if (overlay != null && overlay.isVisible() && overlay.isMouseInBounds(x, y)) {
        return false;
      }
    }
    return super.isPointInRegion(p_146978_1_, p_146978_2_, p_146978_3_, p_146978_4_, p_146978_5_, p_146978_6_);
  }

  @Override
  public List<GhostSlot> getGhostSlots() {
    return ghostSlotHandler.getGhostSlots();
  }

  @Deprecated
  protected void ghostSlotClicked(GhostSlot slot, int x, int y, int button) {
    ghostSlotHandler.ghostSlotClicked(this, slot, x, y, button);
  }

  @Override
  protected void mouseClicked(int x, int y, int button) throws IOException {
    for (GuiTextField f : textFields) {
      f.mouseClicked(x, y, button);
    }
    if (!scrollbars.isEmpty()) {
      if (draggingScrollbar != null) {
        draggingScrollbar.mouseClicked(x, y, button);
        return;
      }
      for (VScrollbar vs : scrollbars) {
        if (vs.mouseClicked(x, y, button)) {
          draggingScrollbar = vs;
          return;
        }
      }
    }
    if (!ghostSlotHandler.getGhostSlots().isEmpty()) {
      GhostSlot slot = getGhostSlot(x, y);
      if (slot != null) {
        ghostSlotClicked(slot, x, y, button);
        return;
      }
    }
    // Right click field clearing
    if (button == 1) {
      for (TextFieldEnder tf : textFields) {
        if (tf.contains(x, y)) {
          setText(tf, "");
        }
      }
    }
    // Button events for non-left-clicks
    if (button >= 1) {
      for (Object obj : buttonList) {
        if (obj instanceof IconButton) {
          IconButton btn = (IconButton) obj;
          if (btn.mousePressedButton(mc, x, y, button)) {
            btn.playPressSound(this.mc.getSoundHandler());
            actionPerformedButton(btn, button);
          }
        }
      }
    }
    super.mouseClicked(x, y, button);
  }

  @Override
  protected void mouseReleased(int x, int y, int button) {
      if (draggingScrollbar != null) {
          draggingScrollbar.mouseMovedOrUp(x, y, button);
          draggingScrollbar = null;
      }
      super.mouseReleased(x, y, button);
  }

  @Override
  protected void mouseClickMove(int x, int y, int button, long time) {
    if (draggingScrollbar != null) {
      draggingScrollbar.mouseClickMove(x, y, button, time);
      return;
    }
    super.mouseClickMove(x, y, button, time);
  }

  protected void mouseWheel(int x, int y, int delta) {
    if (!scrollbars.isEmpty()) {
      for (VScrollbar vs : scrollbars) {
        vs.mouseWheel(x, y, delta);
      }
    }
    if (!ghostSlotHandler.getGhostSlots().isEmpty()) {
      GhostSlot slot = getGhostSlot(x, y);
      if (slot != null) {
        ghostSlotClicked(slot, x, y, delta < 0 ? -1 : -2);
      }
    }
  }

  protected void actionPerformedButton(IconButton btn, int mouseButton) throws IOException {
    actionPerformed(btn);
  }

  public void addOverlay(IGuiOverlay overlay) {
    overlays.add(overlay);
  }

  public void removeOverlay(IGuiOverlay overlay) {
    overlays.remove(overlay);
  }

  public void addScrollbar(VScrollbar vs) {
    scrollbars.add(vs);
    vs.adjustPosition();
  }

  public void removeScrollbar(VScrollbar vs) {
    scrollbars.remove(vs);
    if (draggingScrollbar == vs) {
      draggingScrollbar = null;
    }
  }

  private int realMx, realMy;

  @Override
  protected final void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    drawForegroundImpl(mouseX, mouseY);

    Timer t = RenderUtil.getTimer();

    if (t != null) {
      GL11.glPushMatrix();
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glDisable(GL11.GL_DEPTH_TEST);
      for (IGuiOverlay overlay : overlays) {
        if (overlay != null && overlay.isVisible()) {
          overlay.draw(realMx, realMy, t.renderPartialTicks);
        }
      }
      GL11.glEnable(GL11.GL_DEPTH_TEST);
      GL11.glPopMatrix();
    }
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float par1, int mouseX, int mouseY) {
    for (GuiTextField f : textFields) {
      f.drawTextBox();
    }
    if (!scrollbars.isEmpty()) {
      for (VScrollbar vs : scrollbars) {
        vs.drawScrollbar(mouseX, mouseY);
      }
    }
    if (!ghostSlotHandler.getGhostSlots().isEmpty()) {
      drawGhostSlots(mouseX, mouseY);
    }
  }

  @Override
  public void drawScreen(int par1, int par2, float par3) {
    int mx = realMx = par1;
    int my = realMy = par2;
    for (IGuiOverlay overlay : overlays) {
      if (overlay != null && overlay.isVisible() && isMouseInOverlay(par1, par2, overlay)) {
        mx = -5000;
        my = -5000;
        this.drawItemStack(this.mc.thePlayer.inventory.getItemStack(), par1 - this.guiLeft - 8, par2 - this.guiTop - 8, null);
      }
    }

    super.drawScreen(mx, my, par3);

    if (draggingScrollbar == null) {
      ghostSlotHandler.drawGhostSlotToolTip(this, par1, par2);
      ttMan.drawTooltips(this, par1, par2);
    }
  }

//copied from super with hate
  protected void drawItemStack(ItemStack stack, int mouseX, int mouseY, String str) {
      if (stack == null) {
          return;
      }

      GL11.glTranslatef(0.0F, 0.0F, 32.0F);
      zLevel = 200.0F;
      itemRender.zLevel = 200.0F;
      FontRenderer font = null;
      font = stack.getItem().getFontRenderer(stack);
      if (font == null) {
          font = fontRendererObj;
      }
      itemRender.renderItemIntoGUI(stack, mouseX, mouseY);
      itemRender.renderItemOverlayIntoGUI(font, stack, mouseX, mouseY, str);
      zLevel = 0.0F;
      itemRender.zLevel = 0.0F;
  }

  protected void drawFakeItemsStart() {
      zLevel = 100.0F;
      itemRender.zLevel = 100.0F;

      GlStateManager.enableLighting();
      GlStateManager.enableRescaleNormal();
      GlStateManager.enableDepth();
      RenderHelper.enableGUIStandardItemLighting();
  }

  protected void drawFakeItemStack(int x, int y, ItemStack stack) {
      //itemRender.renderItemIntoGUI(stack, x, y);
    itemRender.renderItemAndEffectIntoGUI(stack, x, y);
  }
  
  public void drawFakeItemStackStdOverlay(int x, int y, ItemStack stack) {
    itemRender.renderItemOverlayIntoGUI(fontRendererObj, stack, x, y, null);      
  }

  protected void drawFakeItemHover(int x, int y) {
    GL11.glDisable(GL11.GL_LIGHTING);
    GL11.glDisable(GL11.GL_DEPTH_TEST);
    GL11.glColorMask(true, true, true, false);
    drawGradientRect(x, y, x + 16, y + 16, 0x80FFFFFF, 0x80FFFFFF);
    GL11.glColorMask(true, true, true, true);
    GL11.glEnable(GL11.GL_DEPTH_TEST);
    GL11.glEnable(GL11.GL_LIGHTING);
  }

  protected void drawFakeItemsEnd() {
    GL11.glPopAttrib();
    itemRender.zLevel = 0.0F;
    zLevel = 0.0F;
  }

  @Override
  public void renderToolTip(ItemStack p_146285_1_, int p_146285_2_, int p_146285_3_) {
    super.renderToolTip(p_146285_1_, p_146285_2_, p_146285_3_);
  }

  @Deprecated
  protected void drawGhostSlotTooltip(GhostSlot slot, int mouseX, int mouseY) {
    ghostSlotHandler.drawGhostSlotTooltip(this, slot, mouseX, mouseY);
  }

  /**
   * Override this to allow GhostSlots to gray out with a custom background. Not
   * needed if the slot has the default "Minecraft-gray" background---but it may
   * be nicer to texture pack creators.
   */
  protected String getGuiTexture() {
    return null;
  }

  @Deprecated
  protected void drawGhostSlots(int mouseX, int mouseY) {
    ghostSlotHandler.drawGhostSlots(this, mouseX, mouseY);
  }

  @Deprecated
  protected GhostSlot getGhostSlot(int mouseX, int mouseY) {
    return ghostSlotHandler.getGhostSlot(this, mouseX, mouseY);
  }

  private boolean isMouseInOverlay(int mouseX, int mouseY, IGuiOverlay overlay) {
    int x = mouseX - getGuiLeft();
    int y = mouseY - getGuiTop();
    return overlay.getBounds().contains(x, y);
  }

  @Override
  public boolean removeToolTip(GuiToolTip toolTip) {
    return ttMan.removeToolTip(toolTip);
  }

  protected void drawForegroundImpl(int mouseX, int mouseY) {
    super.drawGuiContainerForegroundLayer(mouseX, mouseY);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public void drawHoveringText(List par1List, int par2, int par3, FontRenderer font) {
    GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
    GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
    copyOfdrawHoveringText(par1List, par2, par3, font);
    GL11.glPopAttrib();
    GL11.glPopAttrib();
  }

  //This is a copy of the super class method due to 'Method not found' errors
  // reported with some mods installed.
  protected void copyOfdrawHoveringText(List<String> par1List, int par2, int par3, FontRenderer font) {
    if (!par1List.isEmpty()) {
      GL11.glDisable(GL12.GL_RESCALE_NORMAL);
      RenderHelper.disableStandardItemLighting();
      GL11.glDisable(GL11.GL_LIGHTING);
      GL11.glDisable(GL11.GL_DEPTH_TEST);
      int k = 0;
      Iterator<String> iterator = par1List.iterator();

      while (iterator.hasNext()) {
        String s = (String) iterator.next();
        int l = font.getStringWidth(s);

        if (l > k) {
          k = l;
        }
      }

      int i1 = par2 + 12;
      int j1 = par3 - 12;
      int k1 = 8;

      if (par1List.size() > 1) {
        k1 += 2 + (par1List.size() - 1) * 10;
      }

      if (i1 + k > this.width) {
        i1 -= 28 + k;
      }

      if (j1 + k1 + 6 > this.height) {
        j1 = this.height - k1 - 6;
      }

      this.zLevel = 300.0F;
      //itemRenderer.zLevel = 300.0F;
      int l1 = -267386864;
      this.drawGradientRect(i1 - 3, j1 - 4, i1 + k + 3, j1 - 3, l1, l1);
      this.drawGradientRect(i1 - 3, j1 + k1 + 3, i1 + k + 3, j1 + k1 + 4, l1, l1);
      this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 + k1 + 3, l1, l1);
      this.drawGradientRect(i1 - 4, j1 - 3, i1 - 3, j1 + k1 + 3, l1, l1);
      this.drawGradientRect(i1 + k + 3, j1 - 3, i1 + k + 4, j1 + k1 + 3, l1, l1);
      int i2 = 1347420415;
      int j2 = (i2 & 16711422) >> 1 | i2 & -16777216;
      this.drawGradientRect(i1 - 3, j1 - 3 + 1, i1 - 3 + 1, j1 + k1 + 3 - 1, i2, j2);
      this.drawGradientRect(i1 + k + 2, j1 - 3 + 1, i1 + k + 3, j1 + k1 + 3 - 1, i2, j2);
      this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 - 3 + 1, i2, i2);
      this.drawGradientRect(i1 - 3, j1 + k1 + 2, i1 + k + 3, j1 + k1 + 3, j2, j2);

      for (int k2 = 0; k2 < par1List.size(); ++k2) {
        String s1 = (String) par1List.get(k2);
        font.drawStringWithShadow(s1, i1, j1, -1);

        if (k2 == 0) {
          j1 += 2;
        }

        j1 += 10;
      }

      this.zLevel = 0.0F;
      //itemRenderer.zLevel = 0.0F;
      GL11.glEnable(GL11.GL_LIGHTING);
      GL11.glEnable(GL11.GL_DEPTH_TEST);
      RenderHelper.enableStandardItemLighting();
      GL11.glEnable(GL12.GL_RESCALE_NORMAL);
    }
  }

  public float getZlevel() {
    return zLevel;
  }

  @Override
  public int getGuiLeft() {
    return guiLeft;
  }

  @Override
  public int getGuiTop() {
    return guiTop;
  }

  @Override
  public int getXSize() {
    return xSize;
  }

  @Override
  public int getYSize() {
    return ySize;
  }

  public void setGuiLeft(int i) {
    guiLeft = i;
  }

  public void setGuiTop(int i) {
    guiTop = i;
  }

  public void setXSize(int i) {
    xSize = i;
  }

  public void setYSize(int i) {
    ySize = i;
  }

  @Override
  public FontRenderer getFontRenderer() {
    return Minecraft.getMinecraft().fontRendererObj;
  }

  @Override
  public void addButton(GuiButton button) {
    if (!buttonList.contains(button)) {
      buttonList.add(button);
    }
  }

  @Override
  public void removeButton(GuiButton button) {
    buttonList.remove(button);
  }

  @Override
  public int getOverlayOffsetX() {
    return 0;
  }

  @Override
  public void doActionPerformed(GuiButton guiButton) throws IOException {
    actionPerformed(guiButton);
  }

//  @Override
//  @Optional.Method(modid = "NotEnoughItems")
//  public VisiblityData modifyVisiblity(GuiContainer gc, VisiblityData vd) {
//    return vd;
//  }
//
//  @Override
//  @Optional.Method(modid = "NotEnoughItems")
//  public Iterable<Integer> getItemSpawnSlots(GuiContainer gc, ItemStack is) {
//    return null;
//  }
//
//  @Override
//  @Optional.Method(modid = "NotEnoughItems")
//  public List<TaggedInventoryArea> getInventoryAreas(GuiContainer gc) {
//    return Collections.<TaggedInventoryArea> emptyList();
//  }
//
//  @Override
//  @Optional.Method(modid = "NotEnoughItems")
//  public boolean handleDragNDrop(GuiContainer gc, int i, int i1, ItemStack is, int i2) {
//    return false;
//  }
//
//  @Override
//  @Optional.Method(modid = "NotEnoughItems")
//  public boolean hideItemPanelSlot(GuiContainer gc, int x, int y, int w, int h) {
//    return false;
//  }
}

//package com.enderio.core.client.gui;
//
//import java.io.IOException;
//import java.util.Iterator;
//import java.util.List;
//
//import org.lwjgl.input.Mouse;
//import org.lwjgl.opengl.GL11;
//
//import com.enderio.core.api.client.gui.IGuiOverlay;
//import com.enderio.core.api.client.gui.IGuiScreen;
//import com.enderio.core.client.gui.ToolTipManager.ToolTipRenderer;
//import com.enderio.core.client.gui.button.IconButton;
//import com.enderio.core.client.gui.widget.GhostSlot;
//import com.enderio.core.client.gui.widget.GuiToolTip;
//import com.enderio.core.client.gui.widget.TextFieldEnder;
//import com.enderio.core.client.gui.widget.VScrollbar;
//import com.enderio.core.client.render.RenderUtil;
//import com.google.common.collect.Lists;
//
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.FontRenderer;
//import net.minecraft.client.gui.GuiButton;
//import net.minecraft.client.gui.GuiTextField;
//import net.minecraft.client.gui.inventory.GuiContainer;
//import net.minecraft.client.renderer.GlStateManager;
//import net.minecraft.client.renderer.RenderHelper;
//import net.minecraft.inventory.Container;
//import net.minecraft.item.ItemStack;
//import net.minecraft.util.Timer;
//import net.minecraftforge.common.ForgeHooks;
//
//public abstract class GuiContainerBase extends GuiContainer implements ToolTipRenderer, IGuiScreen {
//
//    protected ToolTipManager ttMan = new ToolTipManager();
//    protected List<IGuiOverlay> overlays = Lists.newArrayList();
//    protected List<TextFieldEnder> textFields = Lists.newArrayList();
//    protected List<VScrollbar> scrollbars = Lists.newArrayList();
//    protected List<GhostSlot> ghostSlots = Lists.newArrayList();
//
//    protected GhostSlot hoverGhostSlot;
//    protected VScrollbar draggingScrollbar;
//
//    protected GuiContainerBase(Container par1Container) {
//        super(par1Container);
//    }
//
//    @Override
//    public void initGui() {
//        super.initGui();
//        fixupGuiPosition();
//        for (IGuiOverlay overlay : overlays) {
//            overlay.init(this);
//        }
//        for (TextFieldEnder f : textFields) {
//            f.init(this);
//        }
//    }
//
//    protected void fixupGuiPosition() {}
//
//    @Override
//    protected void keyTyped(char c, int key) throws IOException {
//        TextFieldEnder focused = null;
//        for (TextFieldEnder f : textFields) {
//            if (f.isFocused()) {
//                focused = f;
//            }
//        }
//
//        // If esc is pressed
//        if (key == 1) {
//            // If there is a focused text field unfocus it
//            if (focused != null && key == 1) {
//                focused.setFocused(false);
//                focused = null;
//                return;
//            } else if (!hideOverlays()) { // Otherwise close overlays/GUI
//                mc.thePlayer.closeScreen();
//                return;
//            }
//        }
//
//        // If the user pressed tab, switch to the next text field, or unfocus if there are none
//        if (c == '\t') {
//            for (int i = 0; i < textFields.size(); i++) {
//                TextFieldEnder f = textFields.get(i);
//                if (f.isFocused()) {
//                    textFields.get((i + 1) % textFields.size()).setFocused(true);
//                    f.setFocused(false);
//                    return;
//                }
//            }
//        }
//
//        // If there is a focused text field, attempt to type into it
//        if (focused != null) {
//            String old = focused.getText();
//            if (focused.textboxKeyTyped(c, key)) {
//                onTextFieldChanged(focused, old);
//                return;
//            }
//        }
//
//        // More NEI behavior, f key focuses first text field
//        if (c == 'f' && focused == null && !textFields.isEmpty()) {
//            focused = textFields.get(0);
//            focused.setFocused(true);
//        }
//
//        // Finally if 'e' was pressed but not captured by a text field, close the overlays/GUI
//        if (key == mc.gameSettings.keyBindInventory.getKeyCode()) {
//            if (!hideOverlays()) {
//                mc.thePlayer.closeScreen();
//            }
//            return;
//        }
//
//        // If the key was not captured, let NEI do its thing
//        super.keyTyped(c, key);
//    }
//
//    protected final void setText(TextFieldEnder tf, String newText) {
//        String old = tf.getText();
//        tf.setText(newText);
//        onTextFieldChanged(tf, old);
//    }
//
//    protected void onTextFieldChanged(TextFieldEnder tf, String old) {
//
//    }
//
//    public boolean hideOverlays() {
//        for (IGuiOverlay overlay : overlays) {
//            if (overlay.isVisible()) {
//                overlay.setIsVisible(false);
//                return true;
//            }
//        }
//        return false;
//    }
//
//    @Override    
//    public void addToolTip(GuiToolTip toolTip) {
//      synchronized (ttMan) {
//        ttMan.addToolTip(toolTip);
//      }        
//    }
//
//    @Override
//    public void updateScreen() {
//        super.updateScreen();
//
//        if (!ForgeHooks.canInteractWith(mc.thePlayer, inventorySlots)) {
//            mc.thePlayer.closeScreen();
//        }
//
//        for (GuiTextField f : textFields) {
//            f.updateCursorCounter();
//        }
//    }
//
//    @Override
//    public void handleMouseInput() throws IOException {
//        int x = Mouse.getEventX() * width / mc.displayWidth;
//        int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
//        int b = Mouse.getEventButton();
//        for (IGuiOverlay overlay : overlays) {
//            if (overlay != null && overlay.isVisible() && overlay.handleMouseInput(x, y, b)) {
//                return;
//            }
//        }
//        int delta = Mouse.getEventDWheel();
//        if (delta != 0) {
//            mouseWheel(x, y, delta);
//        }
//        super.handleMouseInput();
//    }
//
//    @Override
//    protected boolean isPointInRegion(int p_146978_1_, int p_146978_2_, int p_146978_3_, int p_146978_4_, int p_146978_5_, int p_146978_6_) {
//        int x = Mouse.getEventX() * width / mc.displayWidth;
//        int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
//        for (IGuiOverlay overlay : overlays) {
//            if (overlay != null && overlay.isVisible() && overlay.isMouseInBounds(x, y)) {
//                return false;
//            }
//        }
//        return super.isPointInRegion(p_146978_1_, p_146978_2_, p_146978_3_, p_146978_4_, p_146978_5_, p_146978_6_);
//    }
//
//    @Override
//    public List<GhostSlot> getGhostSlots() {
//        return ghostSlots;
//    }
//
//    protected void ghostSlotClicked(GhostSlot slot, int x, int y, int button) {
//        ItemStack st = Minecraft.getMinecraft().thePlayer.inventory.getItemStack();
//        slot.putStack(st);
//    }
//
//    @Override
//    protected void mouseClicked(int x, int y, int button) throws IOException {
//        for (GuiTextField f : textFields) {
//            f.mouseClicked(x, y, button);
//        }
//        if (!scrollbars.isEmpty()) {
//            if (draggingScrollbar != null) {
//                draggingScrollbar.mouseClicked(x, y, button);
//                return;
//            }
//            for (VScrollbar vs : scrollbars) {
//                if (vs.mouseClicked(x, y, button)) {
//                    draggingScrollbar = vs;
//                    return;
//                }
//            }
//        }
//        if (!ghostSlots.isEmpty()) {
//            GhostSlot slot = getGhostSlot(x, y);
//            if (slot != null) {
//                ghostSlotClicked(slot, x, y, button);
//                return;
//            }
//        }
//        // Right click field clearing
//        if (button == 1) {
//            for (TextFieldEnder tf : textFields) {
//                if (tf.contains(x, y)) {
//                    setText(tf, "");
//                }
//            }
//        }
//        // Button events for non-left-clicks
//        if (button >= 1) {
//            for (Object obj : buttonList) {
//                if (obj instanceof IconButton) {
//                    IconButton btn = (IconButton) obj;
//                    if (btn.mousePressedButton(mc, x, y, button)) {
//                        btn.playPressSound(mc.getSoundHandler());
//                        actionPerformedButton(btn, button);
//                    }
//                }
//            }
//        }
//        super.mouseClicked(x, y, button);
//    }
//
//    @Override
//    protected void mouseReleased(int x, int y, int button) {
//        if (draggingScrollbar != null) {
//            draggingScrollbar.mouseMovedOrUp(x, y, button);
//            draggingScrollbar = null;
//        }
//        super.mouseReleased(x, y, button);
//    }
//
//    @Override
//    protected void mouseClickMove(int x, int y, int button, long time) {
//        if (draggingScrollbar != null) {
//            draggingScrollbar.mouseClickMove(x, y, button, time);
//            return;
//        }
//        super.mouseClickMove(x, y, button, time);
//    }
//
//    protected void mouseWheel(int x, int y, int delta) {
//        if (!scrollbars.isEmpty()) {
//            for (VScrollbar vs : scrollbars) {
//                vs.mouseWheel(x, y, delta);
//            }
//        }
//    }
//
//    protected void actionPerformedButton(IconButton btn, int mouseButton) throws IOException {
//        actionPerformed(btn);
//    }
//
//    public void addOverlay(IGuiOverlay overlay) {
//        overlays.add(overlay);
//    }
//
//    public void removeOverlay(IGuiOverlay overlay) {
//        overlays.remove(overlay);
//    }
//
//    public void addScrollbar(VScrollbar vs) {
//        if (!scrollbars.contains(vs)) {
//            scrollbars.add(vs);
//        }
//        vs.adjustPosition();
//    }
//
//    public void removeScrollbar(VScrollbar vs) {
//        scrollbars.remove(vs);
//        if (draggingScrollbar == vs) {
//            draggingScrollbar = null;
//        }
//    }
//
//    private int realMx, realMy;
//
//    @Override
//    protected final void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
//        drawForegroundImpl(mouseX, mouseY);
//
//        Timer t = RenderUtil.getTimer();
//
//        if (t != null) {
//            GlStateManager.pushMatrix();
//            GlStateManager.color(1, 1, 1, 1);
//            GlStateManager.disableDepth();
//            for (IGuiOverlay overlay : overlays) {
//                if (overlay != null && overlay.isVisible()) {
//                    overlay.draw(realMx, realMy, t.renderPartialTicks);
//                }
//            }
//            GlStateManager.enableDepth();
//            GlStateManager.popMatrix();
//        }
//    }
//
//    @Override
//    protected void drawGuiContainerBackgroundLayer(float par1, int mouseX, int mouseY) {
//        for (GuiTextField f : textFields) {
//            f.drawTextBox();
//        }
//        if (!scrollbars.isEmpty()) {
//            for (VScrollbar vs : scrollbars) {
//                vs.drawScrollbar(mouseX, mouseY);
//            }
//        }
//        drawGhostSlots(mouseX, mouseY);
//        GlStateManager.color(1, 1, 1, 1);
//    }
//
//    @Override
//    public void drawScreen(int par1, int par2, float par3) {
//        hoverGhostSlot = null;
//        int mx = realMx = par1;
//        int my = realMy = par2;
//        for (IGuiOverlay overlay : overlays) {
//            if (overlay != null && overlay.isVisible() && isMouseInOverlay(par1, par2, overlay)) {
//                mx = -5000;
//                my = -5000;
//                drawItemStack(mc.thePlayer.inventory.getItemStack(), par1 - guiLeft - 8, par2 - guiTop - 8, null);
//            }
//        }
//
//        super.drawScreen(mx, my, par3);
//
//        if (draggingScrollbar == null) {
//            if (hoverGhostSlot != null && mc.thePlayer.inventory.getItemStack() == null) {
//                drawGhostSlotTooltip(hoverGhostSlot, par1, par2);
//            }
//
//            synchronized (ttMan) {
//                ttMan.drawTooltips(this, par1, par2);
//            }
//        }
//    }
//
//    // copied from super with hate
//    protected void drawItemStack(ItemStack stack, int mouseX, int mouseY, String str) {
//        if (stack == null) {
//            return;
//        }
//
//        GL11.glTranslatef(0.0F, 0.0F, 32.0F);
//        zLevel = 200.0F;
//        itemRender.zLevel = 200.0F;
//        FontRenderer font = null;
//        font = stack.getItem().getFontRenderer(stack);
//        if (font == null) {
//            font = fontRendererObj;
//        }
//        itemRender.renderItemIntoGUI(stack, mouseX, mouseY);
//        itemRender.renderItemOverlayIntoGUI(font, stack, mouseX, mouseY, str);
//        zLevel = 0.0F;
//        itemRender.zLevel = 0.0F;
//    }
//
//    protected void drawFakeItemsStart() {
//        zLevel = 100.0F;
//        itemRender.zLevel = 100.0F;
//
//        GlStateManager.enableLighting();
//        GlStateManager.enableRescaleNormal();
//        GlStateManager.enableDepth();
//        RenderHelper.enableGUIStandardItemLighting();
//    }
//
//    protected void drawFakeItemStack(int x, int y, ItemStack stack) {
//        itemRender.renderItemIntoGUI(stack, x, y);
//    }
//
//    protected void drawFakeItemHover(int x, int y) {
//        GlStateManager.disableLighting();
//        GlStateManager.disableDepth();
//        GlStateManager.colorMask(true, true, true, false);
//        drawGradientRect(x, y, x + 16, y + 16, 0x80FFFFFF, 0x80FFFFFF);
//        GlStateManager.colorMask(true, true, true, true);
//        GlStateManager.enableDepth();
//        GlStateManager.enableLighting();
//    }
//
//    protected void drawFakeItemsEnd() {
//        GL11.glPopAttrib();
//        itemRender.zLevel = 0.0F;
//        zLevel = 0.0F;
//    }
//
//    protected void drawGhostSlotTooltip(GhostSlot slot, int mouseX, int mouseY) {
//        ItemStack stack = slot.getStack();
//        if (stack != null) {
//            renderToolTip(stack, mouseX, mouseY);
//        }
//    }
//
//    protected void drawGhostSlots(int mouseX, int mouseY) {
//        if (ghostSlots.isEmpty()) {
//            return;
//        }
//        int sx = getGuiLeft();
//        int sy = getGuiTop();
//        drawFakeItemsStart();
//        try {
//            hoverGhostSlot = null;
//            for (GhostSlot slot : ghostSlots) {
//                ItemStack stack = slot.getStack();
//                if (slot.isVisible()) {
//                    if (stack != null) {
//                        drawFakeItemStack(slot.x + sx, slot.y + sy, stack);
//                    }
//                    if (slot.isMouseOver(mouseX - sx, mouseY - sy)) {
//                        hoverGhostSlot = slot;
//                    }
//                }
//            }
//            if (hoverGhostSlot != null) {
//                // draw hover last to prevent it from affecting rendering of other slots ...
//                drawFakeItemHover(hoverGhostSlot.x + sx, hoverGhostSlot.y + sy);
//            }
//        } finally {
//            drawFakeItemsEnd();
//        }
//    }
//
//    protected GhostSlot getGhostSlot(int mouseX, int mouseY) {
//        mouseX -= getGuiLeft();
//        mouseY -= getGuiTop();
//        for (GhostSlot slot : ghostSlots) {
//            if (slot.isVisible() && slot.isMouseOver(mouseX, mouseY)) {
//                return slot;
//            }
//        }
//        return null;
//    }
//
//    private boolean isMouseInOverlay(int mouseX, int mouseY, IGuiOverlay overlay) {
//        int x = mouseX - getGuiLeft();
//        int y = mouseY - getGuiTop();
//        return overlay.getBounds().contains(x, y);
//    }
//
//    @Override
//    public boolean removeToolTip(GuiToolTip toolTip) {
//      synchronized (ttMan) {
//        return ttMan.removeToolTip(toolTip);
//      }
//        
//    }
//
//    @Override
//    public void clearToolTips() {
//      synchronized (ttMan) {
//        ttMan.clearToolTips();
//      }
//    }
//
//    protected void drawForegroundImpl(int mouseX, int mouseY) {
//        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
//    }
//
//    @SuppressWarnings({ "rawtypes", "unchecked" })
//    @Override
//    public void drawHoveringText(List par1List, int par2, int par3, FontRenderer font) {
//        copyOfdrawHoveringText(par1List, par2, par3, font);
//    }
//
//    // This is a copy of the super class method due to 'Method not found' errors
//    // reported with some mods installed.
//    protected void copyOfdrawHoveringText(List<String> par1List, int par2, int par3, FontRenderer font) {
//        if (!par1List.isEmpty()) {
//            GlStateManager.disableRescaleNormal();
//            RenderHelper.disableStandardItemLighting();
//            GlStateManager.disableLighting();
//            GlStateManager.disableDepth();
//            int k = 0;
//            Iterator<String> iterator = par1List.iterator();
//
//            while (iterator.hasNext()) {
//                String s = iterator.next();
//                int l = font.getStringWidth(s);
//
//                if (l > k) {
//                    k = l;
//                }
//            }
//
//            int i1 = par2 + 12;
//            int j1 = par3 - 12;
//            int k1 = 8;
//
//            if (par1List.size() > 1) {
//                k1 += 2 + (par1List.size() - 1) * 10;
//            }
//
//            if (i1 + k > width) {
//                i1 -= 28 + k;
//            }
//
//            if (j1 + k1 + 6 > height) {
//                j1 = height - k1 - 6;
//            }
//
//            zLevel = 300.0F;
//            // itemRenderer.zLevel = 300.0F;
//            int l1 = -267386864;
//            drawGradientRect(i1 - 3, j1 - 4, i1 + k + 3, j1 - 3, l1, l1);
//            drawGradientRect(i1 - 3, j1 + k1 + 3, i1 + k + 3, j1 + k1 + 4, l1, l1);
//            drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 + k1 + 3, l1, l1);
//            drawGradientRect(i1 - 4, j1 - 3, i1 - 3, j1 + k1 + 3, l1, l1);
//            drawGradientRect(i1 + k + 3, j1 - 3, i1 + k + 4, j1 + k1 + 3, l1, l1);
//            int i2 = 1347420415;
//            int j2 = (i2 & 16711422) >> 1 | i2 & -16777216;
//            drawGradientRect(i1 - 3, j1 - 3 + 1, i1 - 3 + 1, j1 + k1 + 3 - 1, i2, j2);
//            drawGradientRect(i1 + k + 2, j1 - 3 + 1, i1 + k + 3, j1 + k1 + 3 - 1, i2, j2);
//            drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 - 3 + 1, i2, i2);
//            drawGradientRect(i1 - 3, j1 + k1 + 2, i1 + k + 3, j1 + k1 + 3, j2, j2);
//
//            for (int k2 = 0; k2 < par1List.size(); ++k2) {
//                String s1 = par1List.get(k2);
//                font.drawStringWithShadow(s1, i1, j1, -1);
//
//                if (k2 == 0) {
//                    j1 += 2;
//                }
//
//                j1 += 10;
//            }
//
//            zLevel = 0.0F;
//            // itemRenderer.zLevel = 0.0F;
//            GlStateManager.enableLighting();
//            GlStateManager.enableDepth();
//            RenderHelper.enableStandardItemLighting();
//            GlStateManager.enableRescaleNormal();
//        }
//    }
//
//    @Override
//    public int getGuiLeft() {
//        return guiLeft;
//    }
//
//    @Override
//    public int getGuiTop() {
//        return guiTop;
//    }
//
//    @Override
//    public int getXSize() {
//        return xSize;
//    }
//
//    @Override
//    public int getYSize() {
//        return ySize;
//    }
//
//    public void setGuiLeft(int i) {
//        guiLeft = i;
//    }
//
//    public void setGuiTop(int i) {
//        guiTop = i;
//    }
//
//    public void setXSize(int i) {
//        xSize = i;
//    }
//
//    public void setYSize(int i) {
//        ySize = i;
//    }
//
//    @Override
//    public FontRenderer getFontRenderer() {
//        return Minecraft.getMinecraft().fontRendererObj;
//    }
//
//    @Override
//    public void addButton(GuiButton button) {
//        if (!buttonList.contains(button)) {
//            buttonList.add(button);
//        }
//    }
//
//    @Override
//    public void removeButton(GuiButton button) {
//        buttonList.remove(button);
//    }
//
//    @Override
//    public int getOverlayOffsetX() {
//        return 0;
//    }
//
//    @Override
//    public void doActionPerformed(GuiButton guiButton) throws IOException {
//        actionPerformed(guiButton);
//    }
//}
